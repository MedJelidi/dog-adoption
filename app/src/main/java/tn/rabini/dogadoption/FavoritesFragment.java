package tn.rabini.dogadoption;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import tn.rabini.dogadoption.models.Dog;

public class FavoritesFragment extends Fragment {

    private RecyclerView dogList;
    private LikedDogAdapter likedDogAdapter;
    private DatabaseReference ref;
    private CircularProgressIndicator spinner;

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
        dogList = v.findViewById(R.id.dogList);
        dogList.setLayoutManager(new LinearLayoutManager(requireContext()));
        FirebaseRecyclerOptions<Dog> options = new FirebaseRecyclerOptions.Builder<Dog>()
                .setQuery(ref, Dog.class)
                .build();
        likedDogAdapter = new LikedDogAdapter(options, requireContext()) {
            @Override
            public void onDataChanged() {
                super.onDataChanged();
                spinner.setVisibility(View.GONE);
                dogList.setVisibility(View.VISIBLE);
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