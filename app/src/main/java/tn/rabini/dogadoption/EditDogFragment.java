package tn.rabini.dogadoption;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tiper.MaterialSpinner;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EditDogFragment extends Fragment {

    ActivityResultLauncher<Intent> startImageIntent;
    private TextInputLayout nameLayout, ageLayout, descriptionLayout;
    private TextInputEditText nameInput, ageInput, descriptionInput;
    private String nameValue, raceValue, ageValue, descriptionValue, locationValue, imageValue,
            genderValue, dogId;
    private boolean readyValue;
    private TextView errorView;
    private Button submitButton;
    private SwitchMaterial readySwitch;
    private Uri imagePath;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private CircularProgressIndicator spinner;
    private LinearLayout submitCancelLayout;
    private MaterialSpinner raceLayout, locationLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dogId = getArguments().getString("id");
            nameValue = getArguments().getString("name");
            raceValue = getArguments().getString("race");
            ageValue = getArguments().getString("age");
            genderValue = getArguments().getString("gender");
            descriptionValue = getArguments().getString("description");
            locationValue = getArguments().getString("location");
            imageValue = getArguments().getString("image");
            readyValue = getArguments().getBoolean("ready");
        }
        startImageIntent = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result != null
                            && result.getResultCode() == Activity.RESULT_OK
                            && result.getData() != null) {
                        imagePath = result.getData().getData();
                    }
                });
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_edit_dog, container, false);
        nameLayout = v.findViewById(R.id.nameLayout);
        raceLayout = v.findViewById(R.id.raceLayout);
        ageLayout = v.findViewById(R.id.ageLayout);
        MaterialSpinner genderLayout = v.findViewById(R.id.genderLayout);
        descriptionLayout = v.findViewById(R.id.descriptionLayout);
        locationLayout = v.findViewById(R.id.locationLayout);
        nameInput = v.findViewById(R.id.nameInput);
        ageInput = v.findViewById(R.id.ageInput);
        descriptionInput = v.findViewById(R.id.descriptionInput);
        errorView = v.findViewById(R.id.errorView);
        Button imageButton = v.findViewById(R.id.imagePickerButton);
        readySwitch = v.findViewById(R.id.readySwitch);
        submitCancelLayout = v.findViewById(R.id.submitCancelLayout);
        submitButton = v.findViewById(R.id.submitButton);
        Button cancelButton = v.findViewById(R.id.cancelButton);
        spinner = v.findViewById(R.id.spinner);

        nameInput.setText(nameValue);
        ageInput.setText(ageValue);
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.genders, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderLayout.setAdapter(genderAdapter);

        genderLayout.setSelection(genderAdapter.getPosition(genderValue));

        genderLayout.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(@NonNull MaterialSpinner materialSpinner, View view, int i, long l) {
                genderValue = materialSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(@NonNull MaterialSpinner materialSpinner) {
            }
        });

        ArrayAdapter<CharSequence> raceAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.dog_races, android.R.layout.simple_spinner_item);
        raceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        raceLayout.setAdapter(raceAdapter);

        raceLayout.setSelection(raceAdapter.getPosition(raceValue));

        raceLayout.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(@NonNull MaterialSpinner materialSpinner, View view, int i, long l) {
                raceValue = materialSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(@NonNull MaterialSpinner materialSpinner) {
            }
        });

        ArrayAdapter<CharSequence> locationAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.location_names, android.R.layout.simple_spinner_item);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationLayout.setAdapter(locationAdapter);

        locationLayout.setSelection(locationAdapter.getPosition(locationValue));

        locationLayout.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(@NonNull MaterialSpinner materialSpinner, View view, int i, long l) {
                locationValue = materialSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(@NonNull MaterialSpinner materialSpinner) {
            }
        });

        descriptionInput.setText(descriptionValue);
        readySwitch.setChecked(readyValue);
        imagePath = Uri.parse(imageValue);

        imageButton.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startImageIntent.launch(intent);
        });
        submitButton.setOnClickListener(view -> onSubmit());
        cancelButton.setOnClickListener(view -> switchTo());
        return v;
    }

    private void onSubmit() {
        nameValue = nameInput.getText().toString().trim();
        ageValue = ageInput.getText().toString().trim();
        descriptionValue = descriptionInput.getText().toString().trim();
        readyValue = readySwitch.isChecked();
        if (formValid()) {
            submitButton.setEnabled(false);
            submitCancelLayout.setVisibility(View.GONE);
            spinner.setVisibility(View.VISIBLE);
            String imageID = UUID.randomUUID().toString();
            StorageReference ref = storageReference.child("images/" + imageID);

            if (imagePath.toString().equals(imageValue)) {
                editDog(imageValue);
            } else {
                ref.putFile(imagePath)
                        .addOnSuccessListener(taskSnapshot -> {
                            final Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                            firebaseUri.addOnSuccessListener(uri -> FirebaseStorage.getInstance()
                                    .getReferenceFromUrl(imageValue)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> editDog(uri.toString()))
                                    .addOnFailureListener(e -> {
                                        spinner.setVisibility(View.INVISIBLE);
                                        submitCancelLayout.setVisibility(View.VISIBLE);
                                        submitButton.setEnabled(true);
                                        Snackbar.make(requireActivity().findViewById(R.id.coordinatorLayout), e.getMessage(), Snackbar.LENGTH_LONG)
                                                .setAnchorView(requireActivity().findViewById(R.id.bottom_navigation))
                                                .show();
                                    }));
                        })
                        .addOnFailureListener(e -> {
                            spinner.setVisibility(View.INVISIBLE);
                            submitCancelLayout.setVisibility(View.VISIBLE);
                            submitButton.setEnabled(true);
                            Snackbar.make(requireActivity().findViewById(R.id.coordinatorLayout), e.getMessage(), Snackbar.LENGTH_LONG)
                                    .setAnchorView(requireActivity().findViewById(R.id.bottom_navigation))
                                    .show();
                        });
            }
        }
    }

    private void editDog(String imageUrl) {

        FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(mAuth.getCurrentUser().getUid())
                .child("dogs")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String dogID = dataSnapshot.getValue(String.class);
                            if (dogID != null) {
                                if (dogID.equals(dogId)) {
                                    Map<String, Object> dogUpdates = new HashMap<>();
                                    dogUpdates.put("description", descriptionValue);
                                    dogUpdates.put("gender", genderValue);
                                    dogUpdates.put("name", nameValue);
                                    dogUpdates.put("race", raceValue);
                                    dogUpdates.put("age", ageValue);
                                    dogUpdates.put("location", locationValue);
                                    dogUpdates.put("image", imageUrl);
                                    dogUpdates.put("ready", readyValue);
                                    FirebaseDatabase.getInstance()
                                            .getReference("Dogs")
                                            .child(dogId)
                                            .updateChildren(dogUpdates, (error1, ref1) -> {
                                                if (error1 != null) {
                                                    spinner.setVisibility(View.INVISIBLE);
                                                    submitCancelLayout.setVisibility(View.VISIBLE);
                                                    submitButton.setEnabled(true);
                                                    Snackbar.make(requireActivity().findViewById(R.id.coordinatorLayout), error1.getMessage(), Snackbar.LENGTH_LONG)
                                                            .setAnchorView(requireActivity().findViewById(R.id.bottom_navigation))
                                                            .show();
                                                } else {
                                                    Snackbar.make(requireActivity().findViewById(R.id.coordinatorLayout), "Dog updated successfully!", Snackbar.LENGTH_LONG)
                                                            .setAnchorView(requireActivity().findViewById(R.id.bottom_navigation))
                                                            .show();
                                                    switchTo();
                                                }
                                            });
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void switchTo() {
        Bundle flipBundle = new Bundle();
        flipBundle.putString("flip", "ToProfile");
        getParentFragmentManager().setFragmentResult("flipResult", flipBundle);
    }

    private boolean formValid() {
        nameLayout.setError(null);
        descriptionLayout.setError(null);
        locationLayout.setError(null);
        errorView.setVisibility(View.INVISIBLE);
        if (nameValue == null || nameValue.length() < 2 || nameValue.length() > 20) {
            nameLayout.setError("Name should be between 2 and 20 characters.");
            return false;
        }
        if (raceValue == null || raceValue.length() == 0) {
            raceLayout.setError("Race required.");
            return false;
        }
        if (ageValue == null || ageValue.length() == 0) {
            ageLayout.setError("Age required.");
            return false;
        }
        if (descriptionValue == null || descriptionValue.length() == 0) {
            descriptionLayout.setError("Description required.");
            return false;
        }
        if (locationValue == null || locationValue.length() == 0) {
            locationLayout.setError("Location required.");
            return false;
        }
        return true;
    }
}