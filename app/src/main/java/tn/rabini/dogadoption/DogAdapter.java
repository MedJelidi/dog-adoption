package tn.rabini.dogadoption;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.util.Locale;

import tn.rabini.dogadoption.models.Dog;

public class DogAdapter extends BaseAdapter<Dog, DogAdapter.DogViewHolder> {

    private final Context context;
    private final double lat, lng;

    public DogAdapter(@NonNull FirebaseRecyclerOptions<Dog> options, Context context, double lat, double lng) {
        super(options);
        this.context = context;
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    protected void onBindViewHolder(@NonNull DogAdapter.DogViewHolder holder, int position, @NonNull Dog model) {
        glideHandle(context, model.getImage(), holder.dogImage);
        double distance = getDistance(lat, lng, Double.parseDouble(model.getLat()), Double.parseDouble(model.getLng()));
        holder.itemView.setOnClickListener(view -> switchToDetails(context, model, distance, 0));
        holder.dogRace.setText(model.getRace());
        holder.dogName.setText(model.getName());
        holder.dogLocation.setText(context.getString(R.string.distance, String.format(Locale.CANADA, "%.2f", distance / 1000)));
        if (isNew(model.getPublishedDate()) < 0)
            holder.dogPublished.setText("");
        else
            holder.dogPublished.setText(context.getString(R.string.new_word));
    }

    @NonNull
    @Override
    public DogAdapter.DogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_dog, parent, false);
        return new DogAdapter.DogViewHolder(v);
    }

    public static class DogViewHolder extends RecyclerView.ViewHolder {

        ImageView dogImage;
        TextView dogRace, dogName, dogLocation, dogPublished;

        public DogViewHolder(@NonNull View itemView) {
            super(itemView);
            dogImage = itemView.findViewById(R.id.dogImage);
            dogRace = itemView.findViewById(R.id.dogRace);
            dogName = itemView.findViewById(R.id.dogName);
            dogLocation = itemView.findViewById(R.id.dogLocation);
            dogPublished = itemView.findViewById(R.id.dogPublished);
        }
    }
}