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
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.Locale;

import tn.rabini.dogadoption.models.Dog;

public class DogAdapter extends FirebaseRecyclerAdapter<Dog, DogAdapter.DogViewHolder> {

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
        LatLng loc1 = new LatLng(lat, lng);
        LatLng loc2 = new LatLng(Double.parseDouble(model.getLat()), Double.parseDouble(model.getLng()));
        double distance = SphericalUtil.computeDistanceBetween(loc1, loc2);
        holder.itemView.setOnClickListener(view -> {
                            Bundle flipBundle = new Bundle();
                            flipBundle.putString("flip", "ToDogDetails");
                            flipBundle.putInt("previous_fragment", 0);
                            flipBundle.putString("id", model.getId());
                            flipBundle.putString("image", model.getImage());
                            flipBundle.putString("name", model.getName());
                            flipBundle.putString("race", model.getRace());
                            flipBundle.putString("age", model.getAge());
                            flipBundle.putString("gender", model.getGender());
                            flipBundle.putString("description", model.getDescription());
                            flipBundle.putString("distance", String.format(Locale.CANADA, "%.2f", distance / 1000)+"km away");
                            flipBundle.putBoolean("ready", model.isReady());
                            flipBundle.putString("owner", model.getOwner());
                            ((AppCompatActivity) context).getSupportFragmentManager().setFragmentResult("flipResult", flipBundle);
                        });
                        holder.dogRace.setText(model.getRace());
                        holder.dogName.setText(model.getName());
                        holder.dogLocation.setText(String.format(Locale.CANADA, "%.2f", distance / 1000)+"km away");
    }

    @NonNull
    @Override
    public DogAdapter.DogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_dog, parent, false);
        return new DogAdapter.DogViewHolder(v);
    }

//    private void calculateDistance(DogAdapter.DogViewHolder holder, Dog model) {
//        if (ActivityCompat.checkSelfPermission(
//                context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, new LocationListener() {
//                @Override
//                public void onLocationChanged(Location location) {
//                    if (location != null) {
//                        LatLng loc1 = new LatLng(location.getLatitude(), location.getLongitude());
//                        LatLng loc2 = new LatLng(Double.parseDouble(model.getLat()), Double.parseDouble(model.getLng()));
//                        double distance = SphericalUtil.computeDistanceBetween(loc1, loc2);
//                        holder.itemView.setOnClickListener(view -> {
//                            Bundle flipBundle = new Bundle();
//                            flipBundle.putString("flip", "ToDogDetails");
//                            flipBundle.putInt("previous_fragment", 0);
//                            flipBundle.putString("id", model.getId());
//                            flipBundle.putString("image", model.getImage());
//                            flipBundle.putString("name", model.getName());
//                            flipBundle.putString("race", model.getRace());
//                            flipBundle.putString("age", model.getAge());
//                            flipBundle.putString("gender", model.getGender());
//                            flipBundle.putString("description", model.getDescription());
//                            flipBundle.putString("distance", String.format(Locale.CANADA, "%.2f", distance / 1000)+"km away");
//                            flipBundle.putBoolean("ready", model.isReady());
//                            flipBundle.putString("owner", model.getOwner());
//                            ((AppCompatActivity) context).getSupportFragmentManager().setFragmentResult("flipResult", flipBundle);
//                        });
//                        holder.dogRace.setText(model.getRace());
//                        holder.dogName.setText(model.getName());
//                        holder.dogLocation.setText(String.format(Locale.CANADA, "%.2f", distance / 1000)+"km away");
//                    }
//                }
//
//                @Override
//                public void onStatusChanged(String s, int i, Bundle bundle) {
//
//                }
//
//                @Override
//                public void onProviderEnabled(String s) {
//
//                }
//
//                @Override
//                public void onProviderDisabled(String s) {
//
//                }
//            });
//        }
//    }

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
