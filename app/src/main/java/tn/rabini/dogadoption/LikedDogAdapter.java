package tn.rabini.dogadoption;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
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

public class LikedDogAdapter extends FirebaseRecyclerAdapter<String, LikedDogAdapter.LikedDogViewHolder> {

    private final Context context;
    private final FragmentActivity activity;

    public LikedDogAdapter(@NonNull FirebaseRecyclerOptions<String> options, Context context, FragmentActivity activity) {
        super(options);
        this.context = context;
        this.activity = activity;
    }

    @Override
    protected void onBindViewHolder(@NonNull LikedDogAdapter.LikedDogViewHolder holder, int position, @NonNull String model) {

        FirebaseDatabase.getInstance()
                .getReference("Dogs")
                .child(model)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Dog dog = snapshot.getValue(Dog.class);
                        if (dog != null) {
                            CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
                            circularProgressDrawable.setStrokeWidth(5f);
                            circularProgressDrawable.setCenterRadius(30f);
                            circularProgressDrawable.start();

                            Glide.with(context)
                                    .load(dog.getImage())
                                    .fitCenter()
                                    .placeholder(circularProgressDrawable)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .error(R.drawable.ic_baseline_error_24)
                                    .into(holder.dogImage);

                            holder.dogName.setText(dog.getName());
                            holder.itemView.setOnClickListener(view -> {
                                Bundle flipBundle = new Bundle();
                                flipBundle.putString("flip", "ToDogDetails");
                                flipBundle.putInt("previous_fragment", 1);
                                flipBundle.putString("id", dog.getId());
                                flipBundle.putString("image", dog.getImage());
                                flipBundle.putString("name", dog.getName());
                                flipBundle.putString("race", dog.getRace());
                                flipBundle.putString("age", dog.getAge());
                                flipBundle.putString("gender", dog.getGender());
                                flipBundle.putString("description", dog.getDescription());
                                flipBundle.putBoolean("ready", dog.isReady());
                                flipBundle.putString("location", dog.getLocation());
                                flipBundle.putString("owner", dog.getOwner());
                                ((AppCompatActivity) context).getSupportFragmentManager().setFragmentResult("flipResult", flipBundle);
                            });
                        } else {
                            FirebaseDatabase.getInstance()
                                    .getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child("likedDogs")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
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
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        holder.unlikeButton.setOnClickListener(view -> {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        HashMap<String, String> likedDogs;
                        if (user.getLikedDogs() != null) {
                            likedDogs = user.getLikedDogs();
                            for (String k: likedDogs.keySet()) {
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
            });
        });

    }

    @NonNull
    @Override
    public LikedDogAdapter.LikedDogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_liked_dog, parent, false);
        return new LikedDogAdapter.LikedDogViewHolder(v);
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
