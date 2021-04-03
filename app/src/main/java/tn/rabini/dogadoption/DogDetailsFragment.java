package tn.rabini.dogadoption;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.snackbar.Snackbar;
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


public class DogDetailsFragment extends Fragment {

    private String id, name, race, age, gender, description, image, location, owner;

    private ToggleButton likeButton;
    private TextView dogOwner, dogContact;
    private FirebaseAuth mAuth;
    private String contactNumber;
    private boolean ready;

    public DogDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            id = getArguments().getString("id");
            name = getArguments().getString("name");
            race = getArguments().getString("race");
            age = getArguments().getString("age");
            gender = getArguments().getString("gender");
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
        ImageView dogImage = v.findViewById(R.id.dogImage);
        likeButton = v.findViewById(R.id.likeButton);
        TextView dogName = v.findViewById(R.id.dogName);
        TextView dogRace = v.findViewById(R.id.dogRace);
        TextView dogAge = v.findViewById(R.id.dogAge);
        TextView dogDescription = v.findViewById(R.id.dogDescription);
        TextView dogLocation = v.findViewById(R.id.dogLocation);
        TextView dogReady = v.findViewById(R.id.dogReady);
        dogOwner = v.findViewById(R.id.dogOwner);
        dogContact = v.findViewById(R.id.dogContact);
        dogDescription.setMovementMethod(new ScrollingMovementMethod());

        Glide.with(requireContext())
                .load(image)
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_baseline_error_24)
                .into(dogImage);
        dogName.setText(name);
        dogRace.setText(race);
        dogReady.setText(ready ? "Available" : "Not available at the moment");
        dogReady.setCompoundDrawablesWithIntrinsicBounds(ready ? ContextCompat.getDrawable(requireContext(), R.drawable.baseline_check_circle_24)
                : ContextCompat.getDrawable(requireContext(), R.drawable.baseline_report_gmailerrorred_24), null, null, null);
        dogAge.setText(getString(R.string.gender_age_detail, gender, age));
        dogDescription.setText(description);
        dogLocation.setText(location);
        FirebaseDatabase.getInstance().getReference("Users").child(owner)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            contactNumber = user.getPhone();
                            dogOwner.setText(user.getUsername());
                            dogContact.setText(user.getPhone());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        dogContact.setOnClickListener(view -> {
            Intent dialIntent = new Intent();
            dialIntent.setAction(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:" + contactNumber));
            startActivity(dialIntent);
        });

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            likeButton.setBackgroundResource(R.drawable.ic_baseline_favorite_border_24);
            likeButton.setVisibility(View.VISIBLE);
        } else {
            FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("likedDogs")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                if (dataSnapshot.getValue(String.class).equals(id)) {
                                    likeButton.setBackgroundResource
                                            (R.drawable.ic_baseline_favorite_24);
                                    likeButton.setVisibility(View.VISIBLE);
                                    return;
                                }
                            }
                            likeButton.setBackgroundResource
                                    (R.drawable.ic_baseline_favorite_border_24);
                            likeButton.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

        }

        likeButton.setOnClickListener(view -> {
            if (mAuth.getCurrentUser() == null) {
                Bundle flipBundle = new Bundle();
                flipBundle.putString("flip", "ToLogin");
                getParentFragmentManager().setFragmentResult("flipResult", flipBundle);
                return;
            }
            view.setEnabled(false);
            if (view.getBackground().getConstantState().equals(ResourcesCompat
                    .getDrawable(getResources(), R.drawable.ic_baseline_favorite_border_24
                            , null).getConstantState())) {
                DatabaseReference userRef = FirebaseDatabase.getInstance()
                        .getReference("Users")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            ArrayList<String> likedDogs = new ArrayList<>();
                            if (user.getLikedDogs() != null) {
                                likedDogs = user.getLikedDogs();
                            }
                            likedDogs.add(id);
                            user.setLikedDogs(likedDogs);
                            Map<String, Object> userUpdates = new HashMap<>();
                            userUpdates.put("likedDogs", user.getLikedDogs());
                            FirebaseDatabase.getInstance()
                                    .getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .updateChildren(userUpdates, (error, ref) -> {
                                        if (error != null) {
                                            Snackbar.make(getActivity().findViewById(R.id.coordinatorLayout), error.getMessage(), Snackbar.LENGTH_LONG)
                                                    .setAnchorView(getActivity().findViewById(R.id.bottom_navigation))
                                                    .show();
                                        } else {
                                            Snackbar.make(getActivity().findViewById(R.id.coordinatorLayout), "Dog added to favorites!", Snackbar.LENGTH_SHORT)
                                                    .setAnchorView(getActivity().findViewById(R.id.bottom_navigation))
                                                    .show();
                                            view.setBackgroundResource(R.drawable.ic_baseline_favorite_24);
                                        }
                                        view.setEnabled(true);
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        view.setEnabled(true);
                    }
                });
            } else {
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
                                    if (likedDogs.get(i).equals(id)) {
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
                                                Snackbar.make(getActivity().findViewById(R.id.coordinatorLayout), error.getMessage(), Snackbar.LENGTH_LONG)
                                                        .setAnchorView(getActivity().findViewById(R.id.bottom_navigation))
                                                        .show();
                                            } else {
                                                Snackbar.make(getActivity().findViewById(R.id.coordinatorLayout), "Dog removed from favorites!", Snackbar.LENGTH_SHORT)
                                                        .setAnchorView(getActivity().findViewById(R.id.bottom_navigation))
                                                        .show();
                                                view.setBackgroundResource(R.drawable.ic_baseline_favorite_border_24);
                                            }
                                            view.setEnabled(true);
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        view.setEnabled(true);
                    }
                });
            }
        });
        return v;
    }
}