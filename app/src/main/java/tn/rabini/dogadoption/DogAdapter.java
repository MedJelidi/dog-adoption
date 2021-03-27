package tn.rabini.dogadoption;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import tn.rabini.dogadoption.models.Dog;
import tn.rabini.dogadoption.models.User;

public class DogAdapter extends FirebaseRecyclerAdapter<Dog, DogAdapter.DogViewHolder> {

    private Context context;

    public DogAdapter(@NonNull FirebaseRecyclerOptions<Dog> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull DogAdapter.DogViewHolder holder, int position, @NonNull Dog model) {
        Glide.with(context)
                .load(model.getImage())
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.ALL)         //ALL or NONE as your requirement
                .thumbnail(Glide.with(context).load(R.drawable.ic_baseline_refresh_24))
                .error(R.drawable.ic_baseline_error_24)
                .into(holder.dogImage);

        // holder.dogImage.setImageResource(R.drawable.maxresdefault);
        holder.dogName.setText(model.getName());
        holder.dogDescription.setText(model.getDescription());
        if (model.isReady()) {
            holder.dogReady.setText(context.getString(R.string.ready));
            holder.dogReady.setTextColor(context.getResources().getColor(R.color.green));
        } else {
            holder.dogReady.setText(context.getString(R.string.not_ready));
            holder.dogReady.setTextColor(context.getResources().getColor(R.color.red));
        }
        holder.dogLocation.setText(model.getLocation());
//        FirebaseDatabase.getInstance().getReference("Users").child(model.getOwner()).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                User user = snapshot.getValue(User.class);
//                if (user != null) {
//                    holder.dogOwner.setText(user.getUsername());
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle flipBundle = new Bundle();
                flipBundle.putString("flip", "ToDogDetails");
                flipBundle.putString("image", model.getImage());
                flipBundle.putString("name", model.getName());
                flipBundle.putString("description", model.getDescription());
                flipBundle.putBoolean("ready", model.isReady());
                flipBundle.putString("location", model.getLocation());
                flipBundle.putString("owner", model.getOwner());
                ((AppCompatActivity)context).getSupportFragmentManager().setFragmentResult("flipResult", flipBundle);
            }
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
        TextView dogName, dogDescription, dogReady, dogLocation, dogOwner;

        public DogViewHolder(@NonNull View itemView) {
            super(itemView);
            dogImage = (ImageView) itemView.findViewById(R.id.dogImage);
            dogName = (TextView) itemView.findViewById(R.id.dogName);
            dogDescription = (TextView) itemView.findViewById(R.id.dogDescription);
            dogReady = (TextView) itemView.findViewById(R.id.dogReady);
            dogLocation = (TextView) itemView.findViewById(R.id.dogLocation);
//            dogOwner = (TextView) itemView.findViewById(R.id.dogOwner);
        }
    }
}
