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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import tn.rabini.dogadoption.models.Dog;

public class MyDogAdapter extends FirebaseRecyclerAdapter<Dog, MyDogAdapter.MyDogViewHolder> {

    private final Context context;

    public MyDogAdapter(@NonNull FirebaseRecyclerOptions<Dog> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull MyDogAdapter.MyDogViewHolder holder, int position, @NonNull Dog model) {
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

        holder.editButton.setOnClickListener(view -> {
            Bundle flipBundle = new Bundle();
            flipBundle.putString("flip", "ToEditDog");
            flipBundle.putString("id", model.getId());
            flipBundle.putString("image", model.getImage());
            flipBundle.putString("name", model.getName());
            flipBundle.putString("race", model.getRace());
            flipBundle.putString("age", model.getAge());
            flipBundle.putString("gender", model.getGender());
            flipBundle.putString("description", model.getDescription());
            flipBundle.putBoolean("ready", model.isReady());
            flipBundle.putString("location", model.getLocation());
            ((AppCompatActivity) context).getSupportFragmentManager().setFragmentResult("flipResult", flipBundle);
        });

        holder.deleteButton.setOnClickListener(view -> {
            MaterialAlertDialogBuilder deleteBuilder = new MaterialAlertDialogBuilder(context)
                    .setTitle(context.getString(R.string.delete_dialog))
                    .setPositiveButton("Yes", (dialogInterface, i) -> FirebaseDatabase.getInstance()
                            .getReference("Users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child("dogs")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                        Dog dog = dataSnapshot.getValue(Dog.class);
                                        if (dog != null) {
                                            if (dog.getId().equals(model.getId())) {
                                                dataSnapshot.getRef().removeValue()
                                                        .addOnSuccessListener(aVoid -> FirebaseDatabase
                                                                .getInstance()
                                                                .getReference("Dogs")
                                                                .child(dog.getId())
                                                                .removeValue()
                                                                .addOnSuccessListener(aVoid1 -> Toast.makeText(context, "Dog deleted successfully!", Toast.LENGTH_LONG).show())
                                                                .addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show()))
                                                        .addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show());
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            }))
                    .setNegativeButton("No", (dialog, which) -> dialog.cancel());
            deleteBuilder.show();
        });
    }

    @NonNull
    @Override
    public MyDogAdapter.MyDogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_my_dog, parent, false);
        return new MyDogAdapter.MyDogViewHolder(v);
    }

    public static class MyDogViewHolder extends RecyclerView.ViewHolder {

        ImageView dogImage;
        TextView dogName;
        Button editButton, deleteButton;

        public MyDogViewHolder(@NonNull View itemView) {
            super(itemView);
            dogImage = itemView.findViewById(R.id.dogImage);
            dogName = itemView.findViewById(R.id.dogName);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
