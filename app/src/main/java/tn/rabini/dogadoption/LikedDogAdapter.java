package tn.rabini.dogadoption;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import tn.rabini.dogadoption.models.Dog;
import tn.rabini.dogadoption.models.User;

public class LikedDogAdapter extends FirebaseRecyclerAdapter<String, LikedDogAdapter.LikedDogViewHolder> {

    private final Context context;

    public LikedDogAdapter(@NonNull FirebaseRecyclerOptions<String> options, Context context) {
        super(options);
        this.context = context;
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
                            Glide.with(context)
                                    .load(dog.getImage())
                                    .fitCenter()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .error(R.drawable.ic_baseline_error_24)
                                    .into(holder.dogImage);

                            holder.dogName.setText(dog.getName());
                            holder.itemView.setOnClickListener(view -> {
                                Bundle flipBundle = new Bundle();
                                flipBundle.putString("flip", "ToDogDetails");
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
                        ArrayList<String> likedDogs;
                        if (user.getLikedDogs() != null) {
                            likedDogs = user.getLikedDogs();
                            for (int i = 0; i < likedDogs.size(); i++) {
                                if (likedDogs.get(i).equals(model)) {
                                    likedDogs.remove(i);
                                    break;
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
                                            Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
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
