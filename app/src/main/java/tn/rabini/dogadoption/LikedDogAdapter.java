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
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

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

public class LikedDogAdapter extends FirebaseRecyclerAdapter<Dog, LikedDogAdapter.LikedDogViewHolder> {

    private final Context context;

    public LikedDogAdapter(@NonNull FirebaseRecyclerOptions<Dog> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull LikedDogAdapter.LikedDogViewHolder holder, int position, @NonNull Dog model) {
        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
        circularProgressDrawable.setStrokeWidth(5f);
        circularProgressDrawable.setCenterRadius(30f);
        circularProgressDrawable.start();
        Glide.with(context)
                .load(model.getImage())
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(circularProgressDrawable)
                .error(R.drawable.ic_baseline_error_24)
                .into(holder.dogImage);

        holder.dogName.setText(model.getName());
        holder.itemView.setOnClickListener(view -> {
            Bundle flipBundle = new Bundle();
            flipBundle.putString("flip", "ToDogDetails");
            flipBundle.putString("id", model.getId());
            flipBundle.putString("image", model.getImage());
            flipBundle.putString("name", model.getName());
            flipBundle.putString("race", model.getRace());
            flipBundle.putString("age", model.getAge());
            flipBundle.putString("gender", model.getGender());
            flipBundle.putString("description", model.getDescription());
            flipBundle.putBoolean("ready", model.isReady());
            flipBundle.putString("location", model.getLocation());
            flipBundle.putString("owner", model.getOwner());
            ((AppCompatActivity) context).getSupportFragmentManager().setFragmentResult("flipResult", flipBundle);
        });

        holder.unlikeButton.setOnClickListener(view -> {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            DatabaseReference dogRef = FirebaseDatabase.getInstance().getReference("Dogs").child(model.getId());
            dogRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Dog dog = snapshot.getValue(Dog.class);
                    if (dog != null) {
                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User user = snapshot.getValue(User.class);
                                if (user != null) {
                                    ArrayList<Dog> likedDogs;
                                    if (user.getLikedDogs() != null) {
                                        likedDogs = user.getLikedDogs();
                                        for (int i = 0; i < likedDogs.size(); i++) {
                                            if (likedDogs.get(i).getId().equals(dog.getId())) {
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
