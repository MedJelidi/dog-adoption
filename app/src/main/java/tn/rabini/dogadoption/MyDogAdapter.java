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
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.maps.android.SphericalUtil;

import java.util.Locale;
import java.util.Objects;

import tn.rabini.dogadoption.models.Dog;

public class MyDogAdapter extends FirebaseRecyclerAdapter<String, MyDogAdapter.MyDogViewHolder> {

    private final Context context;
    private final FragmentActivity activity;
    private final boolean isUser;
    private final double lat, lng;

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
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .placeholder(circularProgressDrawable)
                                    .error(R.drawable.ic_baseline_error_24)
                                    .into(holder.dogImage);

                            holder.dogName.setText(dog.getName());

                            LatLng loc1 = new LatLng(lat, lng);
                            LatLng loc2 = new LatLng(Double.parseDouble(dog.getLat()), Double.parseDouble(dog.getLng()));
                            double distance = SphericalUtil.computeDistanceBetween(loc1, loc2);
                            holder.itemView.setOnClickListener(view -> {
                                Bundle flipBundle = new Bundle();
                                flipBundle.putString("flip", "ToDogDetails");
                                flipBundle.putInt("previous_fragment", 2);
                                flipBundle.putString("id", dog.getId());
                                flipBundle.putString("image", dog.getImage());
                                flipBundle.putString("name", dog.getName());
                                flipBundle.putString("race", dog.getRace());
                                flipBundle.putString("age", dog.getAge());
                                flipBundle.putString("gender", dog.getGender());
                                flipBundle.putString("description", dog.getDescription());
                                flipBundle.putBoolean("ready", dog.isReady());
                                flipBundle.putString("distance", String.format(Locale.CANADA, "%.2f", distance / 1000)+"km away");
                                flipBundle.putString("owner", dog.getOwner());
                                ((AppCompatActivity) context).getSupportFragmentManager().setFragmentResult("flipResult", flipBundle);
                            });

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
                                                                    FirebaseDatabase.getInstance()
                                                                            .getReference("Users")
                                                                            .child(currentUser.getUid())
                                                                            .child("dogs")
                                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
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
                                                                            });
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
