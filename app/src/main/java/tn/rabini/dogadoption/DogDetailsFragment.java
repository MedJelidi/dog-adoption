package tn.rabini.dogadoption;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import tn.rabini.dogadoption.models.User;


public class DogDetailsFragment extends Fragment {

    private String id, name, race, age, gender, description, distance, image, owner, contactNumber
            , publishedDate;
    private int previousFragment;
    private ToggleButton likeButton;
    private TextView dogOwner, dogContact;
    private FirebaseAuth mAuth;
    private boolean ready;
    private CircularProgressIndicator spinner;
    private RelativeLayout allLayouts;
    private DatabaseReference mUserReference, mLikedDogsReference, mCurrentUserReference;
    private ValueEventListener mUserListener, mLikedDogsListener, mCurrentUserListener;

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
            distance = getArguments().getString("distance");
            image = getArguments().getString("image");
            ready = getArguments().getBoolean("ready");
            owner = getArguments().getString("owner");
            publishedDate = getArguments().getString("published_at");
            previousFragment = getArguments().getInt("previous_fragment");
            mUserReference = FirebaseDatabase.getInstance().getReference("Users").child(owner);
        }
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            mCurrentUserReference = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid());
            mLikedDogsReference = mCurrentUserReference.child("likedDogs");
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_dog_details, container, false);
        spinner = v.findViewById(R.id.spinner);
        allLayouts = v.findViewById(R.id.allLayouts);
        ScrollView parentScroll = v.findViewById(R.id.parentScroll);
        ImageView arrowBack = v.findViewById(R.id.arrowBack);
        ImageView dogImage = v.findViewById(R.id.dogImage);
        likeButton = v.findViewById(R.id.likeButton);
        TextView dogName = v.findViewById(R.id.dogName);
        TextView dogGender = v.findViewById(R.id.dogGender);
        TextView dogRace = v.findViewById(R.id.dogRace);
        TextView dogAge = v.findViewById(R.id.dogAge);
        TextView dogDescription = v.findViewById(R.id.dogDescription);
        TextView dogLocation = v.findViewById(R.id.dogLocation);
        TextView dogReady = v.findViewById(R.id.dogReady);
        TextView publishedAt = v.findViewById(R.id.publishedAt);
        dogOwner = v.findViewById(R.id.dogOwner);
        dogContact = v.findViewById(R.id.dogContact);
        dogDescription.setMovementMethod(new ScrollingMovementMethod());

        arrowBack.setOnClickListener(view -> {
            switch (previousFragment) {
                case 0:
                    switchTo("ToHome");
                    break;
                case 1:
                    switchTo("ToFavorites");
                    break;
                case 2:
                    switchTo("ToProfile");
                    break;
                default:
                    break;
            }
        });

        // IMAGE FULLSCREEN ON CLICK
        dogImage.setOnClickListener(v1 -> {
            AlertDialog fullscreenBuilder = new MaterialAlertDialogBuilder(requireContext())
                    .setNegativeButton("Close", (dialogInterface, i) -> dialogInterface.dismiss())
                    .setView(R.layout.fullscreen_image)
                    .create();

            fullscreenBuilder.setOnShowListener(dialogInterface -> {
                ImageView fullscreenImage = ((AlertDialog) dialogInterface).findViewById(R.id.fullscreenImage);
                assert fullscreenImage != null;
                Glide.with(requireContext())
                        .load(image)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .error(R.drawable.ic_baseline_error_24)
                        .into(fullscreenImage);
            });
            fullscreenBuilder.show();
        });

        dogName.setText(name);
        dogRace.setText(race);
        dogLocation.setText(distance);
        dogReady.setText(ready ? "Available" : "Not available at the moment");
        publishedAt.setText(getString(R.string.published_at, publishedDate));

        // SET AVAILABLE OR NOT IMAGE
        dogReady.setCompoundDrawablesWithIntrinsicBounds(ready ? ContextCompat.getDrawable(requireContext(), R.drawable.baseline_check_circle_24)
                : ContextCompat.getDrawable(requireContext(), R.drawable.baseline_report_gmailerrorred_24), null, null, null);

        // CALCULATE DATE
        String[] dates = age.split("-");
        LocalDate l = LocalDate.of(Integer.parseInt(dates[0]), Integer.parseInt(dates[1]), Integer.parseInt(dates[2])); //specify year, month, date directly
        LocalDate now = LocalDate.now();
        Period diff = Period.between(l, now);

        String years = diff.getYears() > 0 ? diff.getYears() + " years, " : "";
        String days = diff.getDays() > 0 ? diff.getDays() + " days" : "";
        String months = diff.getMonths() > 0 ? days.equals("") ? diff.getMonths() + " months" : diff.getMonths() + " months, " : "";
        String fullAge = years + months + days;
        dogAge.setText(fullAge);

        dogGender.setText(getString(R.string.gender_detail, gender));
        dogDescription.setText(description);


        // GET OWNER INFO
        mUserListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    if (mAuth.getCurrentUser() == null) {
                        dogOwner.setText(getString(R.string.login_to_show));
                        dogContact.setText(getString(R.string.login_to_show));
                    } else if (!mAuth.getCurrentUser().isEmailVerified()) {
                        dogOwner.setText(getString(R.string.verify_to_show));
                        dogContact.setText(getString(R.string.verify_to_show));
                    } else {
                        dogOwner.setText(user.getUsername());
                        dogContact.setText(user.getPhone());
                    }
                    contactNumber = user.getPhone();

                    Glide.with(requireContext())
                            .load(image)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    spinner.setVisibility(View.GONE);
                                    allLayouts.setVisibility(View.VISIBLE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    spinner.setVisibility(View.GONE);
                                    allLayouts.setVisibility(View.VISIBLE);
                                    return false;
                                }
                            })
                            .fitCenter()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .error(R.drawable.ic_baseline_error_24)
                            .into(dogImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        mUserReference.addListenerForSingleValueEvent(mUserListener);

        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isEmailVerified()) {
            dogOwner.setOnClickListener(view -> {
                Bundle flipBundle = new Bundle();
                flipBundle.putString("flip", "ToProfile");
                flipBundle.putString("userID", owner);
                getParentFragmentManager().setFragmentResult("flipResult", flipBundle);
            });

            dogContact.setOnClickListener(view -> {
                Intent dialIntent = new Intent();
                dialIntent.setAction(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:" + contactNumber));
                startActivity(dialIntent);
            });
        }

        // CHECK IF DOG ALREADY LIKED OR NOT
        if (mAuth.getCurrentUser() == null) {
            likeButton.setBackgroundResource(R.drawable.ic_baseline_favorite_border_24);
            likeButton.setVisibility(View.VISIBLE);
        } else {
            mLikedDogsListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String dogID = dataSnapshot.getValue(String.class);
                        if (dogID != null) {
                            if (dogID.equals(id)) {
                                likeButton.setBackgroundResource
                                        (R.drawable.ic_baseline_favorite_24);
                                likeButton.setVisibility(View.VISIBLE);
                                return;
                            }
                        }
                    }
                    likeButton.setBackgroundResource
                            (R.drawable.ic_baseline_favorite_border_24);
                    likeButton.setVisibility(View.VISIBLE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            mLikedDogsReference.addListenerForSingleValueEvent(mLikedDogsListener);
        }

        // ON LIKE BUTTON CLICK
        likeButton.setOnClickListener(view -> {
            if (mAuth.getCurrentUser() == null) {
                Bundle flipBundle = new Bundle();
                flipBundle.putString("flip", "ToLogin");
                getParentFragmentManager().setFragmentResult("flipResult", flipBundle);
                return;
            }
            view.setEnabled(false);
            if (view.getBackground().getConstantState().equals(Objects.requireNonNull(ResourcesCompat
                    .getDrawable(getResources(), R.drawable.ic_baseline_favorite_border_24
                            , null)).getConstantState())) {
                mCurrentUserListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            HashMap<String, String> likedDogs = new HashMap<>();
                            if (user.getLikedDogs() != null) {
                                likedDogs = user.getLikedDogs();
                            }
                            likedDogs.put(UUID.randomUUID().toString(), id);
                            user.setLikedDogs(likedDogs);
                            Map<String, Object> userUpdates = new HashMap<>();
                            userUpdates.put("likedDogs", user.getLikedDogs());
                            FirebaseDatabase.getInstance()
                                    .getReference("Users")
                                    .child(mAuth.getCurrentUser().getUid())
                                    .updateChildren(userUpdates, (error, ref) -> {
                                        if (error != null) {
                                            Snackbar.make(requireActivity().findViewById(R.id.coordinatorLayout), error.getMessage(), Snackbar.LENGTH_LONG)
                                                    .setAnchorView(requireActivity().findViewById(R.id.bottom_navigation))
                                                    .show();
                                        } else {
                                            Snackbar.make(requireActivity().findViewById(R.id.coordinatorLayout), "Dog added to favorites!", Snackbar.LENGTH_SHORT)
                                                    .setAnchorView(requireActivity().findViewById(R.id.bottom_navigation))
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
                };
            } else {
                mCurrentUserListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            HashMap<String, String> likedDogs;
                            if (user.getLikedDogs() != null) {
                                likedDogs = user.getLikedDogs();
                                for (String k : likedDogs.keySet()) {
                                    String dogID = likedDogs.get(k);
                                    if (dogID != null) {
                                        if (dogID.equals(id)) {
                                            likedDogs.remove(k);
                                            break;
                                        }
                                    }
                                }
                                user.setLikedDogs(likedDogs);
                                Map<String, Object> userUpdates = new HashMap<>();
                                userUpdates.put("likedDogs", user.getLikedDogs());
                                FirebaseDatabase.getInstance()
                                        .getReference("Users")
                                        .child(mAuth.getCurrentUser().getUid())
                                        .updateChildren(userUpdates, (error, ref) -> {
                                            if (error != null) {
                                                Snackbar.make(requireActivity().findViewById(R.id.coordinatorLayout), error.getMessage(), Snackbar.LENGTH_LONG)
                                                        .setAnchorView(requireActivity().findViewById(R.id.bottom_navigation))
                                                        .show();
                                            } else {
                                                Snackbar.make(requireActivity().findViewById(R.id.coordinatorLayout), "Dog removed from favorites!", Snackbar.LENGTH_SHORT)
                                                        .setAnchorView(requireActivity().findViewById(R.id.bottom_navigation))
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
                };
            }
            mCurrentUserReference.addListenerForSingleValueEvent(mCurrentUserListener);
        });

        parentScroll.setOnTouchListener((view, motionEvent) -> {
            dogDescription.getParent().requestDisallowInterceptTouchEvent(false);
            view.performClick();
            return false;
        });

        dogDescription.setOnTouchListener((view, motionEvent) -> {
            dogDescription.getParent().requestDisallowInterceptTouchEvent(true);
            view.performClick();
            return false;
        });

        return v;
    }

    private void switchTo(String fragmentName) {
        Bundle flipBundle = new Bundle();
        flipBundle.putString("flip", fragmentName);
        if (fragmentName.equals("ToProfile"))
            flipBundle.putString("userID", owner);
        getParentFragmentManager().setFragmentResult("flipResult", flipBundle);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mUserListener != null)
            mUserReference.removeEventListener(mUserListener);
        if (mLikedDogsListener != null)
            mLikedDogsReference.removeEventListener(mLikedDogsListener);
        if (mCurrentUserListener != null)
            mCurrentUserReference.removeEventListener(mCurrentUserListener);
    }
}