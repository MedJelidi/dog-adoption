package tn.rabini.dogadoption;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import tn.rabini.dogadoption.models.Dog;
import tn.rabini.dogadoption.models.User;

public class LikedDogAdapter extends BaseAdapter<String, LikedDogAdapter.LikedDogViewHolder> {

    private final Context context;
    private final FragmentActivity activity;
    private final double lat, lng;
    private final DatabaseReference mCurrentUserReference = FirebaseDatabase.getInstance()
            .getReference("Users")
            .child(FirebaseAuth.getInstance().getCurrentUser().getUid()),
            mLikedDogsReference = mCurrentUserReference.child("likedDogs");
    private ValueEventListener mLikedDogsListener, mDogListener, mCurrentUserListener;
    private DatabaseReference mDogReference;

    public LikedDogAdapter(@NonNull FirebaseRecyclerOptions<String> options, Context context, FragmentActivity activity, double lat, double lng) {
        super(options);
        this.context = context;
        this.activity = activity;
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    protected void onBindViewHolder(@NonNull LikedDogAdapter.LikedDogViewHolder holder, int position, @NonNull String model) {

        mDogReference = FirebaseDatabase.getInstance()
                .getReference("Dogs")
                .child(model);
        mDogListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Dog dog = snapshot.getValue(Dog.class);
                if (dog != null) {
                    glideHandle(context, dog.getImage(), holder.dogImage);
                    double distance = getDistance(lat, lng, Double.parseDouble(dog.getLat()), Double.parseDouble(dog.getLng()));
                    holder.dogName.setText(dog.getName());
                    holder.itemView.setOnClickListener(view -> switchToDetails(context, dog, distance, 1));
                } else {
                    mLikedDogsListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                if (dataSnapshot.getValue(String.class).equals(model)) {
                                    dataSnapshot.getRef().removeValue();
                                    return;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    };
                    mLikedDogsReference.addListenerForSingleValueEvent(mLikedDogsListener);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        mDogReference.addListenerForSingleValueEvent(mDogListener);

        holder.unlikeButton.setOnClickListener(view -> {
            mCurrentUserListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        HashMap<String, String> likedDogs;
                        if (user.getLikedDogs() != null) {
                            likedDogs = user.getLikedDogs();
                            for (String k : likedDogs.keySet()) {
                                String dogID = likedDogs.get(k);
                                if (dogID != null) {
                                    if (dogID.equals(model)) {
                                        likedDogs.remove(k);
                                        break;
                                    }
                                }
                            }
                            user.setLikedDogs(likedDogs);
                            Map<String, Object> userUpdates = new HashMap<>();
                            userUpdates.put("likedDogs", user.getLikedDogs());
                            FirebaseDatabase.getInstance()
                                    .getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .updateChildren(userUpdates, (error, ref) -> {
                                        if (error != null) {
                                            Snackbar.make(activity.findViewById(R.id.coordinatorLayout), error.getMessage(), Snackbar.LENGTH_LONG)
                                                    .setAnchorView(activity.findViewById(R.id.bottom_navigation))
                                                    .show();
                                        } else {
                                            Snackbar.make(activity.findViewById(R.id.coordinatorLayout), "Dog removed from favorites!", Snackbar.LENGTH_LONG)
                                                    .setAnchorView(activity.findViewById(R.id.bottom_navigation))
                                                    .show();
                                        }
                                    });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            mCurrentUserReference.addListenerForSingleValueEvent(mCurrentUserListener);
        });

    }

    @NonNull
    @Override
    public LikedDogAdapter.LikedDogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_liked_dog, parent, false);
        return new LikedDogAdapter.LikedDogViewHolder(v);
    }

    public void cleanupListeners() {
        if (mCurrentUserListener != null)
            mCurrentUserReference.removeEventListener(mCurrentUserListener);
        if (mDogListener != null)
            mDogReference.removeEventListener(mDogListener);
        if (mLikedDogsListener != null)
            mLikedDogsReference.removeEventListener(mLikedDogsListener);
    }

    public static class LikedDogViewHolder extends RecyclerView.ViewHolder {

        ImageView dogImage;
        TextView dogName;
        Button unlikeButton;

        public LikedDogViewHolder(@NonNull View itemView) {
            super(itemView);
            dogImage = itemView.findViewById(R.id.dogImage);
            dogName = itemView.findViewById(R.id.dogName);
            unlikeButton = itemView.findViewById(R.id.unlikeButton);
        }
    }
}
