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
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Objects;

import tn.rabini.dogadoption.models.Dog;

public class MyDogAdapter extends BaseAdapter<String, MyDogAdapter.MyDogViewHolder> {

    private final Context context;
    private final FragmentActivity activity;
    private final boolean isUser;
    private final double lat, lng;
    private DatabaseReference mDogReference, mDogsReference;
    private ValueEventListener mDogListener, mDogsListener;

    public MyDogAdapter(@NonNull FirebaseRecyclerOptions<String> options, Context context, FragmentActivity activity, boolean isUser, double lat, double lng) {
        super(options);
        this.context = context;
        this.activity = activity;
        this.isUser = isUser;
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    protected void onBindViewHolder(@NonNull MyDogAdapter.MyDogViewHolder holder, int position, @NonNull String model) {
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
                    holder.itemView.setOnClickListener(view -> switchToDetails(context, dog, distance, 2));
                    holder.dogName.setText(dog.getName());
                    if (isUser) {
                        holder.editButton.setVisibility(View.VISIBLE);
                        holder.deleteButton.setVisibility(View.VISIBLE);

                        holder.editButton.setOnClickListener(view -> {
                            Bundle flipBundle = new Bundle();
                            flipBundle.putString("flip", "ToEditDog");
                            flipBundle.putString("id", dog.getId());
                            flipBundle.putString("image", dog.getImage());
                            flipBundle.putString("name", dog.getName());
                            flipBundle.putString("race", dog.getRace());
                            flipBundle.putString("age", dog.getAge());
                            flipBundle.putString("gender", dog.getGender());
                            flipBundle.putString("description", dog.getDescription());
                            flipBundle.putBoolean("ready", dog.isReady());
                            flipBundle.putString("lat", dog.getLat());
                            flipBundle.putString("lng", dog.getLng());
                            ((AppCompatActivity) context).getSupportFragmentManager().setFragmentResult("flipResult", flipBundle);
                        });

                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                        holder.deleteButton.setOnClickListener(view -> {
                            MaterialAlertDialogBuilder deleteBuilder = new MaterialAlertDialogBuilder(context)
                                    .setTitle(context.getString(R.string.delete_dialog))
                                    .setPositiveButton("Yes", (dialogInterface, i) ->
                                            FirebaseStorage.getInstance()
                                                    .getReferenceFromUrl(dog.getImage())
                                                    .delete()
                                                    .addOnSuccessListener(aVoid -> {
                                                        if (currentUser != null) {
                                                            mDogsListener = new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                                        String dogID = dataSnapshot.getValue(String.class);
                                                                        if (dogID != null) {
                                                                            if (dogID.equals(model)) {
                                                                                dataSnapshot.getRef().removeValue()
                                                                                        .addOnSuccessListener(aVoid -> FirebaseDatabase
                                                                                                .getInstance()
                                                                                                .getReference("Dogs")
                                                                                                .child(dogID)
                                                                                                .removeValue()
                                                                                                .addOnSuccessListener(aVoid1 -> FirebaseDatabase.getInstance()
                                                                                                        .getReference("Dogs")
                                                                                                        .child(model)
                                                                                                        .removeValue()
                                                                                                        .addOnSuccessListener(aVoid2 -> Snackbar.make(activity.findViewById(R.id.coordinatorLayout), "Dog deleted successfully!", Snackbar.LENGTH_LONG)
                                                                                                                .setAnchorView(activity.findViewById(R.id.bottom_navigation))
                                                                                                                .show())
                                                                                                        .addOnFailureListener(e -> Snackbar.make(activity.findViewById(R.id.coordinatorLayout), Objects.requireNonNull(e.getMessage()), Snackbar.LENGTH_LONG)
                                                                                                                .setAnchorView(activity.findViewById(R.id.bottom_navigation))
                                                                                                                .show()))
                                                                                                .addOnFailureListener(e -> Snackbar.make(activity.findViewById(R.id.coordinatorLayout), Objects.requireNonNull(e.getMessage()), Snackbar.LENGTH_LONG)
                                                                                                        .setAnchorView(activity.findViewById(R.id.bottom_navigation))
                                                                                                        .show()))
                                                                                        .addOnFailureListener(e -> Snackbar.make(activity.findViewById(R.id.coordinatorLayout), Objects.requireNonNull(e.getMessage()), Snackbar.LENGTH_LONG)
                                                                                                .setAnchorView(activity.findViewById(R.id.bottom_navigation))
                                                                                                .show());
                                                                            }
                                                                        }
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError error) {
                                                                    Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
                                                                }
                                                            };
                                                            mDogsReference = FirebaseDatabase.getInstance()
                                                                    .getReference("Users")
                                                                    .child(currentUser.getUid())
                                                                    .child("dogs");
                                                            mDogsReference.addListenerForSingleValueEvent(mDogsListener);
                                                        }
                                                    }))
                                    .setNegativeButton("No", (dialog, which) -> dialog.cancel());
                            deleteBuilder.show();
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
        mDogReference.addListenerForSingleValueEvent(mDogListener);

    }

    @NonNull
    @Override
    public MyDogAdapter.MyDogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_my_dog, parent, false);
        return new MyDogAdapter.MyDogViewHolder(v);
    }

    public void cleanupListeners() {
        if (mDogListener != null)
            mDogReference.removeEventListener(mDogListener);
        if (mDogsListener != null)
            mDogsReference.removeEventListener(mDogsListener);
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
