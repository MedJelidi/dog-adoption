package tn.rabini.dogadoption;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FavoritesFragment extends Fragment {

    private RecyclerView dogList;
    private LikedDogAdapter likedDogAdapter;
    private DatabaseReference ref;
    private CircularProgressIndicator spinner;
    private LinearLayout noLikeLayout;
    private TextView dogListLink;

    public FavoritesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Bundle flipBundle = new Bundle();
            flipBundle.putString("flip", "ToLogin");
            getParentFragmentManager().setFragmentResult("flipResult", flipBundle);
        }
        ref = FirebaseDatabase.getInstance()
                .getReference()
                .child("Users")
                .child(mAuth.getCurrentUser().getUid())
                .child("likedDogs");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_favorites, container, false);
        spinner = v.findViewById(R.id.spinner);
        noLikeLayout = v.findViewById(R.id.noLikeLayout);
        dogListLink = v.findViewById(R.id.dogListLink);
        dogList = v.findViewById(R.id.dogList);
        dogList.setLayoutManager(new LinearLayoutManager(requireContext()));
        FirebaseRecyclerOptions<String> options = new FirebaseRecyclerOptions.Builder<String>()
                .setQuery(ref, String.class)
                .build();
        likedDogAdapter = new LikedDogAdapter(options, requireContext(), requireActivity()) {
            @Override
            public void onDataChanged() {
                super.onDataChanged();
                spinner.setVisibility(View.GONE);
                if (getItemCount() == 0) {
                    noLikeLayout.setVisibility(View.VISIBLE);
                    dogListLink.setOnClickListener(view -> {
                        Bundle flipBundle = new Bundle();
                        flipBundle.putString("flip", "ToHome");
                        getParentFragmentManager().setFragmentResult("flipResult", flipBundle);
                    });
                } else {
                    dogList.setVisibility(View.VISIBLE);
                }
            }
        };

        dogList.setAdapter(likedDogAdapter);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        likedDogAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        likedDogAdapter.stopListening();
    }
}