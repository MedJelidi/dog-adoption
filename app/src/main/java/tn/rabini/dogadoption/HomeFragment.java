package tn.rabini.dogadoption;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.tiper.MaterialSpinner;

import tn.rabini.dogadoption.models.Dog;

public class HomeFragment extends Fragment {

    private RecyclerView dogList;
    private DogAdapter dogAdapter;
    private DatabaseReference ref;
    private CircularProgressIndicator spinner;
    private String optionSelected = "race", searchQuery = "";
    private LinearLayout searchBar;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ref = FirebaseDatabase.getInstance().getReference().child("Dogs");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        searchBar = v.findViewById(R.id.searchBar);
        SearchView searchView = v.findViewById(R.id.searchView);
        MaterialSpinner searchOptions = v.findViewById(R.id.searchOptions);
        spinner = v.findViewById(R.id.spinner);
        dogList = v.findViewById(R.id.dogList);

        ArrayAdapter<CharSequence> searchOptionsAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.search_options, android.R.layout.simple_spinner_item);
        searchOptionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        searchOptions.setAdapter(searchOptionsAdapter);

        searchOptions.setSelection(0);

        searchOptions.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(@NonNull MaterialSpinner materialSpinner, View view, int i, long l) {
                optionSelected = materialSpinner.getSelectedItem().toString();
                updateSearch();
            }

            @Override
            public void onNothingSelected(@NonNull MaterialSpinner materialSpinner) {
                optionSelected = "race";
            }
        });

        dogList.setLayoutManager(new LinearLayoutManager(requireContext()));
        FirebaseRecyclerOptions<Dog> options = new FirebaseRecyclerOptions.Builder<Dog>()
                .setQuery(ref, Dog.class)
                .build();
        dogAdapter = new DogAdapter(options, requireContext()) {
            @Override
            public void onDataChanged() {
                super.onDataChanged();
                spinner.setVisibility(View.GONE);
                dogList.setVisibility(View.VISIBLE);
                searchBar.setVisibility(View.VISIBLE);
            }
        };
        dogList.setAdapter(dogAdapter);

        Button addDogButton = v.findViewById(R.id.addDogButton);
        addDogButton.setOnClickListener(view -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                switchTo("ToLogin");
            } else if (!FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                MaterialAlertDialogBuilder warningBuilder = new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.warning_verify_email)).setPositiveButton("Done", (dialogInterface, i) -> {
                            FirebaseAuth.getInstance().signOut();
                            switchTo("ToLogin");
                            dialogInterface.dismiss();
                        });
                warningBuilder.show();
            } else {
                switchTo("ToAddDog");
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                searchQuery = s;
                updateSearch();
                return false;
            }
        });

        return v;
    }

    private void updateSearch() {
        Query newRef = ref.orderByChild(optionSelected)
                .startAt(searchQuery).endAt(searchQuery + "\uf8ff");
        FirebaseRecyclerOptions<Dog> newOptions = new FirebaseRecyclerOptions.Builder<Dog>()
                .setQuery(newRef, Dog.class)
                .build();
        dogAdapter.updateOptions(newOptions);
    }

    @Override
    public void onStart() {
        super.onStart();
        dogAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        dogAdapter.stopListening();
    }

    private void switchTo(String fragmentName) {
        Bundle flipBundle = new Bundle();
        flipBundle.putString("flip", fragmentName);
        getParentFragmentManager().setFragmentResult("flipResult", flipBundle);
    }
}