package tn.rabini.dogadoption;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import tn.rabini.dogadoption.models.Dog;

public class DogAdapter extends FirebaseRecyclerAdapter<Dog, DogAdapter.DogViewHolder> {

    private final Context context;

    public DogAdapter(@NonNull FirebaseRecyclerOptions<Dog> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull DogAdapter.DogViewHolder holder, int position, @NonNull Dog model) {
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

        holder.dogRace.setText(model.getRace());
        holder.dogName.setText(model.getName());
        holder.dogLocation.setText(model.getLocation());

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
    }

    @NonNull
    @Override
    public DogAdapter.DogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_dog, parent, false);
        return new DogAdapter.DogViewHolder(v);
    }

    public static class DogViewHolder extends RecyclerView.ViewHolder {

        ImageView dogImage;
        TextView dogRace, dogName, dogLocation;

        public DogViewHolder(@NonNull View itemView) {
            super(itemView);
            dogImage = itemView.findViewById(R.id.dogImage);
            dogRace = itemView.findViewById(R.id.dogRace);
            dogName = itemView.findViewById(R.id.dogName);
            dogLocation = itemView.findViewById(R.id.dogLocation);
        }
    }
}
