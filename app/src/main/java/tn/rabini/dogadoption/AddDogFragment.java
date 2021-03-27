package tn.rabini.dogadoption;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.CircularProgressIndicator;
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
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.UUID;

import tn.rabini.dogadoption.models.Dog;
import tn.rabini.dogadoption.models.User;

public class AddDogFragment extends Fragment {

    // private final int PICK_IMAGE_REQUEST = 69;
    ActivityResultLauncher<Intent> startImageIntent;
    private View v;
    private TextInputLayout nameLayout, descriptionLayout, locationLayout;
    private TextInputEditText nameInput, descriptionInput, locationInput;
    private String nameValue, descriptionValue, locationValue;
    private boolean readyValue;
    private TextView errorView;
    private Button imageButton, submitButton, cancelButton;
    private SwitchMaterial readySwitch;
    private Uri imagePath;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private CircularProgressIndicator spinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startImageIntent = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result != null
                                && result.getResultCode() == Activity.RESULT_OK
                                && result.getData() != null) {
                            // There are no request codes
                            imagePath = result.getData().getData();
                        }
                    }

                });
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_add_dog, container, false);
        nameLayout = (TextInputLayout) v.findViewById(R.id.nameLayout);
        descriptionLayout = (TextInputLayout) v.findViewById(R.id.descriptionLayout);
        locationLayout = (TextInputLayout) v.findViewById(R.id.locationLayout);
        nameInput = (TextInputEditText) v.findViewById(R.id.nameInput);
        descriptionInput = (TextInputEditText) v.findViewById(R.id.descriptionInput);
        locationInput = (TextInputEditText) v.findViewById(R.id.locationInput);
        errorView = (TextView) v.findViewById(R.id.errorView);
        imageButton = (Button) v.findViewById(R.id.imagePickerButton);
        readySwitch = (SwitchMaterial) v.findViewById(R.id.readySwitch);
        submitButton = (Button) v.findViewById(R.id.submitButton);
        cancelButton = (Button) v.findViewById(R.id.cancelButton);
        spinner = (CircularProgressIndicator) v.findViewById(R.id.spinner);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startImageIntent.launch(intent);
            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSubmit();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchTo("ToHome");
            }
        });
        return v;
    }

    private void onSubmit() {
        nameValue = nameInput.getText().toString();
        descriptionValue = descriptionInput.getText().toString();
        locationValue = locationInput.getText().toString();
        readyValue = readySwitch.isChecked();
        if (formValid()) {
            submitButton.setEnabled(false);
            spinner.setVisibility(View.VISIBLE);
            String imageID = UUID.randomUUID().toString();
            StorageReference ref = storageReference.child("images/"+ imageID);
            ref.putFile(imagePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override public void onSuccess(
                                UploadTask.TaskSnapshot taskSnapshot) {
                            final Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                            firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Dog dog = new Dog(UUID.randomUUID().toString(),
                                            nameValue,
                                            descriptionValue,
                                            locationValue,
                                            uri.toString(),
                                            mAuth.getCurrentUser().getUid(),
                                            readyValue);
                                    FirebaseDatabase.getInstance()
                                            .getReference("Dogs")
                                            .child(dog.getId())
                                            .setValue(dog)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        FirebaseDatabase.getInstance()
                                                                .getReference("Users")
                                                                .child(mAuth.getCurrentUser().getUid())
                                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                        User user = snapshot.getValue(User.class);
//                                                        Toast.makeText(requireContext(), user.toString(), Toast.LENGTH_SHORT).show();
                                                                        if (user != null) {
                                                                            ArrayList<Dog> dogs = new ArrayList<>();
                                                                            if (user.getDogs() != null) {
                                                                                dogs = user.getDogs();
                                                                            }
                                                                            dogs.add(dog);
                                                                            FirebaseDatabase.getInstance()
                                                                                    .getReference("Users")
                                                                                    .child(mAuth.getCurrentUser().getUid())
                                                                                    .child("dogs")
                                                                                    .setValue(dogs);
                                                                        }
                                                                        spinner.setVisibility(View.INVISIBLE);
                                                                        submitButton.setEnabled(true);
                                                                        switchTo("ToHome");
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                                    }
                                                                });
                                                    }
                                                    submitButton.setEnabled(true);
                                                }

                                            });
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            spinner.setVisibility(View.INVISIBLE);
                            submitButton.setEnabled(true);
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void switchTo(String fragmentName) {
        Bundle flipBundle = new Bundle();
        flipBundle.putString("flip", fragmentName);
        getParentFragmentManager().setFragmentResult("flipResult", flipBundle);
    }

    private boolean formValid() {
        nameLayout.setError(null);
        descriptionLayout.setError(null);
        locationLayout.setError(null);
        errorView.setVisibility(View.INVISIBLE);
        if (nameValue == null || nameValue.length() == 0) {
            nameLayout.setError("Name required.");
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