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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.CompositeDateValidator;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import tn.rabini.dogadoption.models.Dog;
import tn.rabini.dogadoption.models.User;

public class AddDogFragment extends Fragment {

    ActivityResultLauncher<Intent> startImageIntent;
    private TextInputLayout nameLayout, descriptionLayout;
    private TextInputEditText nameInput, descriptionInput;
    private String nameValue, raceValue, ageValue, descriptionValue, locationValue,
            genderValue = "Male";
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
        View v = inflater.inflate(R.layout.fragment_add_dog, container, false);
        nameLayout = v.findViewById(R.id.nameLayout);
        MaterialSpinner genderLayout = v.findViewById(R.id.genderLayout);
        raceLayout = v.findViewById(R.id.raceLayout);
        Button ageButton = v.findViewById(R.id.ageButton);
        descriptionLayout = v.findViewById(R.id.descriptionLayout);
        locationLayout = v.findViewById(R.id.locationLayout);
        nameInput = v.findViewById(R.id.nameInput);
        descriptionInput = v.findViewById(R.id.descriptionInput);
        errorView = v.findViewById(R.id.errorView);
        Button imageButton = v.findViewById(R.id.imagePickerButton);
        readySwitch = v.findViewById(R.id.readySwitch);
        submitCancelLayout = v.findViewById(R.id.submitCancelLayout);
        submitButton = v.findViewById(R.id.submitButton);
        Button cancelButton = v.findViewById(R.id.cancelButton);
        spinner = v.findViewById(R.id.spinner);

        MaterialDatePicker<Long> agePicker = handleCalendar();

        ageButton.setOnClickListener(view -> agePicker.show(getParentFragmentManager(), "BIRTHDAY_PICKER"));

        agePicker.addOnPositiveButtonClickListener(selection -> {
            TimeZone timeZoneUTC = TimeZone.getDefault();
            int offsetFromUTC = timeZoneUTC.getOffset(new Date().getTime()) * -1;
            SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = new Date(selection + offsetFromUTC);
            ageValue = simpleFormat.format(date);
        });

        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.genders, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderLayout.setAdapter(genderAdapter);

        genderLayout.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(@NonNull MaterialSpinner materialSpinner, View view, int i, long l) {
                genderValue = materialSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(@NonNull MaterialSpinner materialSpinner) {
                genderValue = "Male";
            }
        });

        ArrayAdapter<CharSequence> raceAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.dog_races, android.R.layout.simple_spinner_item);
        raceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        raceLayout.setAdapter(raceAdapter);

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

        locationLayout.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(@NonNull MaterialSpinner materialSpinner, View view, int i, long l) {
                locationValue = materialSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(@NonNull MaterialSpinner materialSpinner) {
            }
        });

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

    private MaterialDatePicker<Long> handleCalendar() {
        MaterialDatePicker.Builder<Long> agePickerBuilder = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select birthday")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds());
        Calendar minDate = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) - 20,
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        CalendarConstraints.Builder calendarConstraintBuilder = new CalendarConstraints.Builder();
        CalendarConstraints.DateValidator dateValidatorMin = DateValidatorPointForward.from(minDate.getTimeInMillis());
        CalendarConstraints.DateValidator dateValidatorMax = DateValidatorPointBackward.now();
        ArrayList<CalendarConstraints.DateValidator> listValidators =
                new ArrayList<>();
        listValidators.add(dateValidatorMin);
        listValidators.add(dateValidatorMax);
        CalendarConstraints.DateValidator validators = CompositeDateValidator.allOf(listValidators);
        calendarConstraintBuilder.setValidator(validators);
        agePickerBuilder.setCalendarConstraints(calendarConstraintBuilder.build());
        return agePickerBuilder.build();
    }

    private void onSubmit() {
        nameValue = nameInput.getText().toString().trim();
        descriptionValue = descriptionInput.getText().toString().trim();
        readyValue = readySwitch.isChecked();
        if (formValid()) {
            submitButton.setEnabled(false);
            submitCancelLayout.setVisibility(View.GONE);
            spinner.setVisibility(View.VISIBLE);
            String imageID = UUID.randomUUID().toString();
            StorageReference ref = storageReference.child("images/" + imageID);
            ref.putFile(imagePath)
                    .addOnSuccessListener(taskSnapshot -> {
                        final Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                        firebaseUri.addOnSuccessListener(uri -> {
                            Dog dog = new Dog(UUID.randomUUID().toString(),
                                    nameValue.substring(0, 1).toUpperCase() + nameValue.substring(1),
                                    raceValue,
                                    ageValue,
                                    genderValue,
                                    descriptionValue,
                                    locationValue,
                                    uri.toString(),
                                    mAuth.getCurrentUser().getUid(),
                                    readyValue);
                            FirebaseDatabase.getInstance()
                                    .getReference("Dogs")
                                    .child(dog.getId())
                                    .setValue(dog)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            FirebaseDatabase.getInstance()
                                                    .getReference("Users")
                                                    .child(mAuth.getCurrentUser().getUid())
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            User user = snapshot.getValue(User.class);
                                                            if (user != null) {
                                                                HashMap<String, String> dogs = new HashMap<>();
                                                                if (user.getDogs() != null) {
                                                                    dogs = user.getDogs();
                                                                }
                                                                dogs.put(UUID.randomUUID().toString(), dog.getId());
                                                                FirebaseDatabase.getInstance()
                                                                        .getReference("Users")
                                                                        .child(mAuth.getCurrentUser().getUid())
                                                                        .child("dogs")
                                                                        .setValue(dogs);
                                                            }
                                                            spinner.setVisibility(View.INVISIBLE);
                                                            Snackbar.make(requireActivity().findViewById(R.id.coordinatorLayout), "Dog added successfully!", Snackbar.LENGTH_LONG)
                                                                    .setAnchorView(requireActivity().findViewById(R.id.bottom_navigation))
                                                                    .show();
                                                            switchTo();
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {
                                                            submitButton.setEnabled(true);
                                                            submitCancelLayout.setVisibility(View.VISIBLE);
                                                        }
                                                    });
                                        } else {
                                            submitButton.setEnabled(true);
                                            submitCancelLayout.setVisibility(View.VISIBLE);
                                        }

                                    });
                        });
                    })
                    .addOnFailureListener(e -> {
                        spinner.setVisibility(View.INVISIBLE);
                        submitButton.setEnabled(true);
                        submitCancelLayout.setVisibility(View.VISIBLE);
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }

    private void switchTo() {
        Bundle flipBundle = new Bundle();
        flipBundle.putString("flip", "ToHome");
        getParentFragmentManager().setFragmentResult("flipResult", flipBundle);
    }

    private boolean formValid() {
        nameLayout.setError(null);
        descriptionLayout.setError(null);
        locationLayout.setError(null);
        raceLayout.setError(null);
        errorView.setVisibility(View.INVISIBLE);
        if (nameValue == null || nameValue.length() < 2 || nameValue.length() > 20) {
            nameLayout.setError("2 < name < 20");
            return false;
        }
        if (raceValue == null || raceValue.length() == 0) {
            raceLayout.setError("Race required.");
            return false;
        }
        if (ageValue == null || ageValue.length() == 0) {
            errorView.setText(getString(R.string.age_required));
            errorView.setVisibility(View.VISIBLE);
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
        if (imagePath == null) {
            errorView.setText(getString(R.string.image_required));
            errorView.setVisibility(View.VISIBLE);
            return false;
        }
        return true;
    }
}