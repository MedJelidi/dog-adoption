package tn.rabini.dogadoption;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.ObservableSnapshotArray;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.tiper.MaterialSpinner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import tn.rabini.dogadoption.models.Dog;

public class HomeFragment extends Fragment {

    private final DatabaseReference mDogsReference = FirebaseDatabase.getInstance().getReference().child("Dogs");
    private final FirebaseRecyclerOptions<Dog> adapterOptions = new FirebaseRecyclerOptions.Builder<Dog>()
            .setQuery(mDogsReference, Dog.class)
            .build();
    private ObservableSnapshotArray<Dog> dogsArray = adapterOptions.getSnapshots();
    private RecyclerView dogList;
    private DogAdapter dogAdapter;
    private CircularProgressIndicator spinner;
    private String optionSelected = "race", currentOption = "race", searchQuery = "";
    private boolean distanceAdded = false;
    private LinearLayout searchBar;
    private List<DataSnapshot> list;
    private Double lat, lng;
    private final BroadcastReceiver cordReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle result = intent.getExtras();
            if (result != null) {
                lat = result.getDouble("lat");
                lng = result.getDouble("lng");
                if (getActivity() != null) {
                    dogAdapter = new DogAdapter(adapterOptions, requireContext(), lat, lng);
                    dogList.swapAdapter(dogAdapter, true);
                    dogAdapter.startListening();
                }
            }
        }
    };

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            lat = getArguments().getDouble("lat");
            lng = getArguments().getDouble("lng");
        }
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(cordReceiver,
                new IntentFilter("my-cord"));
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
                updateSearch(false);
            }

            @Override
            public void onNothingSelected(@NonNull MaterialSpinner materialSpinner) {
                optionSelected = "race";
            }
        });

        dogList.setLayoutManager(new LinearLayoutManager(requireContext()));
        dogAdapter = new DogAdapter(adapterOptions, requireContext(), lat, lng) {
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
                if (!optionSelected.equals("distance")) {
                    searchQuery = s;
                    updateSearch(true);
                }
                return false;
            }
        });

        return v;
    }

    private String capitalize(String s) {
        return s.length() < 1 ? s : s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public ObservableSnapshotArray<Dog> initArray() {
        SnapshotParser<Dog> snapshotParser = snapshot -> Objects.requireNonNull(snapshot.getValue(Dog.class));
        return new ObservableSnapshotArray<Dog>(snapshotParser) {
            @NonNull
            @Override
            protected List<DataSnapshot> getSnapshots() {
                return list;
            }
        };
    }

    public void sortByDistance() {
        if (!distanceAdded) {
            list = new ArrayList<>();
            for (int i = 0; i < dogsArray.size(); i++)
                list.add(dogsArray.getSnapshot(i));
            Collections.sort(list, Dog.distanceComparator(lat, lng));
            dogsArray = initArray();
            distanceAdded = true;
        }
        FirebaseRecyclerOptions<Dog> newOptions = new FirebaseRecyclerOptions.Builder<Dog>()
                .setSnapshotArray(dogsArray)
                .build();
        dogAdapter.updateOptions(newOptions);
        dogsArray = initArray();
    }

    private void updateSearch(boolean typing) {
        if (optionSelected.equals("distance")) {
            if (!optionSelected.equals(currentOption)) {
                sortByDistance();
                currentOption = optionSelected;
            }
        } else {
            if (!optionSelected.equals(currentOption) || typing) {
                Query newRef = mDogsReference.orderByChild(optionSelected)
                        .startAt(capitalize(searchQuery)).endAt(capitalize(searchQuery) + "\uf8ff");
                FirebaseRecyclerOptions<Dog> newOptions = new FirebaseRecyclerOptions.Builder<Dog>()
                        .setQuery(newRef, Dog.class)
                        .build();
                dogAdapter = new DogAdapter(newOptions, requireContext(), lat, lng);
                dogList.swapAdapter(dogAdapter, true);
                dogAdapter.startListening();
                currentOption = optionSelected;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        dogAdapter.startListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dogAdapter.stopListening();
    }

    private void switchTo(String fragmentName) {
        Bundle flipBundle = new Bundle();
        flipBundle.putString("flip", fragmentName);
        getParentFragmentManager().setFragmentResult("flipResult", flipBundle);
    }

}