package tn.rabini.dogadoption;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import tn.rabini.dogadoption.models.User;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DogDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DogDetailsFragment extends Fragment {

    private String name = "name";
    private String description = "description";
    private String image = "image";
    private boolean ready = true;
    private String location = "location";
    private String owner = "owner";

    private ImageView dogImage, arrowBack;
    private TextView dogName, dogDescription, dogLocation, dogOwner, dogContact;

    public DogDetailsFragment() {
        // Required empty public constructor
    }

    public static DogDetailsFragment newInstance(String param1, String param2) {
        DogDetailsFragment fragment = new DogDetailsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            name = getArguments().getString("name");
            description = getArguments().getString("description");
            image = getArguments().getString("image");
            ready = getArguments().getBoolean("ready");
            location = getArguments().getString("location");
            owner = getArguments().getString("owner");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_dog_details, container, false);
        arrowBack = (ImageView) v.findViewById(R.id.arrowBack);
        dogImage = (ImageView) v.findViewById(R.id.dogImage);
        dogName = (TextView) v.findViewById(R.id.dogName);
        dogDescription = (TextView) v.findViewById(R.id.dogDescription);
        dogLocation = (TextView) v.findViewById(R.id.dogLocation);
        dogOwner = (TextView) v.findViewById(R.id.dogOwner);
        dogContact = (TextView) v.findViewById(R.id.dogContact);
        dogDescription.setMovementMethod(new ScrollingMovementMethod());
        Glide.with(requireContext())
                .load(image)
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.ALL)         //ALL or NONE as your requirement
                .thumbnail(Glide.with(requireContext()).load(R.drawable.ic_baseline_refresh_24))
                .error(R.drawable.ic_baseline_error_24)
                .into(dogImage);
        dogName.setText(name);
        dogDescription.setText(description);
        dogLocation.setText(getString(R.string.location_details, location));
        FirebaseDatabase.getInstance().getReference("Users").child(owner).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    dogOwner.setText(getString(R.string.owner_details, user.getUsername()));
                    dogContact.setText(getString(R.string.contact_details, user.getPhone()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        arrowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle flipBundle = new Bundle();
                flipBundle.putString("flip", "ToHome");
                getParentFragmentManager().setFragmentResult("flipResult", flipBundle);
            }
        });
        return v;
    }
}