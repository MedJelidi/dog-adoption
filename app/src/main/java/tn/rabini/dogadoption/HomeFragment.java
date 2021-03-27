package tn.rabini.dogadoption;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import tn.rabini.dogadoption.models.Dog;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private Button addDogButton;
    private View v;
    private RecyclerView dogList;
    private DogAdapter dogAdapter;
    private DatabaseReference ref;
    private CircularProgressIndicator spinner;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        ref = FirebaseDatabase.getInstance().getReference().child("Dogs");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_home, container, false);
        spinner = (CircularProgressIndicator) v.findViewById(R.id.spinner);
        dogList = (RecyclerView) v.findViewById(R.id.dogList);
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
            }
        };
        dogList.setAdapter(dogAdapter);

        addDogButton = (Button) v.findViewById(R.id.addDogButton);
        addDogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchTo("ToAddDog");
            }
        });
        return v;
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