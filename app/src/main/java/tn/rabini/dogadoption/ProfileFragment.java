package tn.rabini.dogadoption;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import tn.rabini.dogadoption.models.User;

public class ProfileFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private MyDogAdapter myDogAdapter;
    private DatabaseReference ref;
    private TextView usernameView, phoneView, emailView;
    private ImageView profileImage;
    private CircularProgressIndicator spinner;
    private RelativeLayout allLayouts;
    private ActivityResultLauncher<Intent> startImageIntent;
    private Uri imagePath;
    private LinearLayout topBar;
    private String userID;
    private boolean isUser = false;

    public ProfileFragment() {
        // Required empty public constructor
    }

    private void updatePicture(Uri uri) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(currentUser.getUid());
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("picture", uri.toString());
        userRef.updateChildren(userUpdates, (error, ref1) -> {
            if (error != null) {
                Snackbar.make(requireActivity().findViewById(R.id.coordinatorLayout), error.getMessage(), Snackbar.LENGTH_LONG)
                        .setAnchorView(requireActivity().findViewById(R.id.bottom_navigation))
                        .show();
            } else {
                CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(requireContext());
                circularProgressDrawable.setStrokeWidth(5f);
                circularProgressDrawable.setCenterRadius(30f);
                circularProgressDrawable.start();

                Glide.with(requireContext())
                        .load(uri.toString())
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(circularProgressDrawable)
                        .circleCrop()
                        .error(R.drawable.ic_baseline_error_24)
                        .into(profileImage);
                Snackbar.make(requireActivity().findViewById(R.id.coordinatorLayout), getString(R.string.image_updated), Snackbar.LENGTH_LONG)
                        .setAnchorView(requireActivity().findViewById(R.id.bottom_navigation))
                        .show();
            }
            spinner.setVisibility(View.GONE);
            allLayouts.setVisibility(View.VISIBLE);
            setHasOptionsMenu(true);
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            userID = getArguments().getString("userID");

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null)
            isUser = userID.equals(currentUser.getUid());

        if (currentUser != null && isUser)
            startImageIntent = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result != null
                                && result.getResultCode() == Activity.RESULT_OK
                                && result.getData() != null) {
                            imagePath = result.getData().getData();
                            StorageReference ref = FirebaseStorage.getInstance().getReference().child("images/" + UUID.randomUUID().toString());
                            allLayouts.setVisibility(View.GONE);
                            setHasOptionsMenu(false);
                            spinner.setVisibility(View.VISIBLE);
                            ref.putFile(imagePath)
                                    .addOnSuccessListener(taskSnapshot -> {
                                        final Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                                        firebaseUri.addOnSuccessListener(uri -> FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid())
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (getActivity() == null) {
                                                            return;
                                                        }
                                                        User user = snapshot.getValue(User.class);
                                                        if (user != null) {
                                                            if (user.getPicture().equals("https://firebasestorage.googleapis.com/v0/b/dogadoption-94cad.appspot.com/o/images%2Fdefault_profile_picture.png?alt=media&token=8c2794b6-2f3a-40fd-9b0c-9964a212bcf4")) {
                                                                updatePicture(uri);
                                                            } else {
                                                                FirebaseStorage.getInstance()
                                                                        .getReferenceFromUrl(user.getPicture())
                                                                        .delete()
                                                                        .addOnSuccessListener(aVoid -> updatePicture(uri))
                                                                        .addOnFailureListener(e -> Snackbar.make(requireActivity().findViewById(R.id.coordinatorLayout), e.getMessage(), Snackbar.LENGTH_LONG)
                                                                                .setAnchorView(requireActivity().findViewById(R.id.bottom_navigation))
                                                                                .show());
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        Snackbar.make(requireActivity().findViewById(R.id.coordinatorLayout), error.getMessage(), Snackbar.LENGTH_LONG)
                                                                .setAnchorView(requireActivity().findViewById(R.id.bottom_navigation))
                                                                .show();
                                                    }
                                                }));
                                    })
                                    .addOnFailureListener(e -> {
                                        spinner.setVisibility(View.GONE);
                                        allLayouts.setVisibility(View.VISIBLE);
                                        setHasOptionsMenu(true);
                                        Snackbar.make(requireActivity().findViewById(R.id.coordinatorLayout), e.getMessage(), Snackbar.LENGTH_LONG)
                                                .setAnchorView(requireActivity().findViewById(R.id.bottom_navigation))
                                                .show();
                                    });
                        }
                    });

        FirebaseDatabase.getInstance().getReference().child("Users").child(userID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (getActivity() == null) {
                            return;
                        }
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            usernameView.setText(user.getUsername());
                            phoneView.setText(user.getPhone());
                            emailView.setText(user.getEmail());

                            CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(requireContext());
                            circularProgressDrawable.setStrokeWidth(5f);
                            circularProgressDrawable.setCenterRadius(30f);
                            circularProgressDrawable.start();

                            Glide.with(requireContext())
                                    .load(user.getPicture())
                                    .fitCenter()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .placeholder(circularProgressDrawable)
                                    .circleCrop()
                                    .error(R.drawable.ic_baseline_error_24)
                                    .into(profileImage);

                            spinner.setVisibility(View.GONE);
                            allLayouts.setVisibility(View.VISIBLE);
                            setHasOptionsMenu(true);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        ref = FirebaseDatabase.getInstance()
                .getReference()
                .child("Users")
                .child(userID)
                .child("dogs");
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) requireActivity()).getSupportActionBar().show();
    }

    @Override
    public void onStop() {
        super.onStop();
        ((AppCompatActivity) requireActivity()).getSupportActionBar().hide();
        myDogAdapter.stopListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        myDogAdapter.startListening();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        topBar = v.findViewById(R.id.topBar);
        LinearLayout myDogsLayout = v.findViewById(R.id.myDogsLayout);
        Button resendButton = v.findViewById(R.id.resendButton);
        spinner = v.findViewById(R.id.spinner);
        allLayouts = v.findViewById(R.id.allLayouts);
        usernameView = v.findViewById(R.id.usernameView);
        phoneView = v.findViewById(R.id.phoneView);
        emailView = v.findViewById(R.id.emailView);
        profileImage = v.findViewById(R.id.profileImage);
        RecyclerView myDogList = v.findViewById(R.id.myDogList);
        myDogList.setLayoutManager(new LinearLayoutManager(requireContext()));
        FirebaseRecyclerOptions<String> options = new FirebaseRecyclerOptions.Builder<String>()
                .setQuery(ref, String.class)
                .build();
        myDogAdapter = new MyDogAdapter(options, requireContext(), requireActivity(), isUser);
        myDogList.setAdapter(myDogAdapter);
        Button logOutButton = v.findViewById(R.id.logOutButton);
        if (isUser)
            logOutButton.setOnClickListener(view -> {
                mAuth.signOut();
                switchTo();
            });
        else
            logOutButton.setVisibility(View.GONE);

        if (mAuth.getCurrentUser() != null && isUser) {
            if (!mAuth.getCurrentUser().isEmailVerified()) {
                topBar.setVisibility(View.VISIBLE);
                myDogsLayout.setVisibility(View.GONE);
                resendButton.setOnClickListener(view -> mAuth.getCurrentUser().sendEmailVerification()
                        .addOnSuccessListener(aVoid -> {
                            topBar.setVisibility(View.GONE);
                            Snackbar.make(requireActivity().findViewById(R.id.coordinatorLayout), "An email verification has been sent. Please verify your email.", Snackbar.LENGTH_LONG)
                                    .setAnchorView(requireActivity().findViewById(R.id.bottom_navigation))
                                    .show();
                        })
                        .addOnFailureListener(e -> Snackbar.make(requireActivity().findViewById(R.id.coordinatorLayout), e.getMessage(), Snackbar.LENGTH_LONG)
                                .setAnchorView(requireActivity().findViewById(R.id.bottom_navigation))
                                .show()));
            }
        }
        return v;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (isUser)
            requireActivity().getMenuInflater().inflate(R.menu.profile_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int option = item.getItemId();
        if (option == R.id.editPicture) {
            editPicture();
        } else if (option == R.id.editPhone) {
            editPhone();
        } else if (option == R.id.editUsername) {
            editUsername();
        } else if (option == R.id.editPassword) {
            editPassword();
        }
        return super.onOptionsItemSelected(item);
    }

    private void editUsername() {
        AlertDialog usernameBuilder = new MaterialAlertDialogBuilder(requireContext())
                .setView(R.layout.change_username)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .create();

        usernameBuilder.setOnShowListener(dialogInterface -> {
            TextInputLayout newUsernameLayout = ((AlertDialog) dialogInterface).findViewById(R.id.newUsernameLayout);
            TextInputEditText newUsernameInput = ((AlertDialog) dialogInterface).findViewById(R.id.newUsernameInput);
            TextView editUsernameError = ((AlertDialog) dialogInterface).findViewById(R.id.editUsernameError);
            CircularProgressIndicator spinner = ((AlertDialog) dialogInterface).findViewById(R.id.spinner);
            Button saveButton = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(view -> {
                String usernameValue = newUsernameInput.getText().toString().trim();
                if (usernameValue == null || usernameValue.length() < 2 || usernameValue.length() > 30) {
                    newUsernameLayout.setError(getString(R.string.username_error));
                    return;
                }
                newUsernameLayout.setError(null);
                editUsernameError.setVisibility(View.GONE);
                spinner.setVisibility(View.VISIBLE);
                FirebaseDatabase.getInstance().getReference("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            User user = dataSnapshot.getValue(User.class);
                            if (user.getUsername().equals(usernameValue)) {
                                newUsernameLayout.setError(getString(R.string.username_exists));
                                spinner.setVisibility(View.GONE);
                                return;
                            }
                        }
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
                        Map<String, Object> userUpdates = new HashMap<>();
                        userUpdates.put("username", usernameValue);
                        userRef.updateChildren(userUpdates, (error, ref) -> {
                            if (error != null) {
                                editUsernameError.setText(error.getMessage());
                                editUsernameError.setVisibility(View.VISIBLE);
                            } else {
                                dialogInterface.dismiss();
                                usernameView.setText(usernameValue);
                                Snackbar.make(requireActivity().findViewById(R.id.coordinatorLayout), getString(R.string.username_updated), Snackbar.LENGTH_LONG)
                                        .setAnchorView(requireActivity().findViewById(R.id.bottom_navigation))
                                        .show();
                            }
                            spinner.setVisibility(View.GONE);
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        spinner.setVisibility(View.INVISIBLE);
                    }
                });
            });
        });
        usernameBuilder.show();
    }

    private boolean validPhone(String phoneValue) {
        return (phoneValue.startsWith("9")
                || phoneValue.startsWith("7")
                || phoneValue.startsWith("5")
                || phoneValue.startsWith("4")
                || phoneValue.startsWith("2"))
                && phoneValue.length() == 8;
    }

    private void editPhone() {
        AlertDialog phoneBuilder = new MaterialAlertDialogBuilder(requireContext())
                .setView(R.layout.change_phone)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .create();

        phoneBuilder.setOnShowListener(dialogInterface -> {
            Button saveButton = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
            CircularProgressIndicator spinner = ((AlertDialog) dialogInterface).findViewById(R.id.spinner);
            TextView editPhoneError = ((AlertDialog) dialogInterface).findViewById(R.id.editPhoneError);
            TextInputLayout newPhoneLayout = ((AlertDialog) dialogInterface).findViewById(R.id.newPhoneLayout);
            saveButton.setOnClickListener(view -> {
                TextInputEditText newPhoneInput = ((AlertDialog) dialogInterface).findViewById(R.id.newPhoneInput);
                newPhoneLayout.setError(null);
                editPhoneError.setVisibility(View.GONE);

                if (!validPhone(newPhoneInput.getText().toString().trim())) {
                    newPhoneLayout.setError(getString(R.string.phone_error));
                    return;
                }

                spinner.setVisibility(View.VISIBLE);
                FirebaseDatabase.getInstance().getReference("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            User user = dataSnapshot.getValue(User.class);
                            if (user.getPhone().equals(newPhoneInput.getText().toString().trim())) {
                                spinner.setVisibility(View.GONE);
                                newPhoneLayout.setError(getString(R.string.phone_exists));
                                return;
                            }
                        }
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
                        Map<String, Object> userUpdates = new HashMap<>();
                        userUpdates.put("phone", newPhoneInput.getText().toString().trim());
                        userRef.updateChildren(userUpdates, (error, ref) -> {
                            if (error != null) {
                                editPhoneError.setText(error.getMessage());
                                spinner.setVisibility(View.GONE);
                                editPhoneError.setVisibility(View.VISIBLE);
                            } else {
                                spinner.setVisibility(View.GONE);
                                dialogInterface.dismiss();
                                phoneView.setText(newPhoneInput.getText().toString().trim());
                                Snackbar.make(requireActivity().findViewById(R.id.coordinatorLayout), getString(R.string.phone_updated), Snackbar.LENGTH_LONG)
                                        .setAnchorView(requireActivity().findViewById(R.id.bottom_navigation))
                                        .show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        spinner.setVisibility(View.GONE);
                    }
                });
            });
        });

        phoneBuilder.show();
    }

    private void editPicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        setHasOptionsMenu(false);
        startImageIntent.launch(intent);
    }

    private void editPassword() {
        AlertDialog passwordBuilder = new MaterialAlertDialogBuilder(requireContext())
                .setView(R.layout.change_password)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .create();
        passwordBuilder.setOnShowListener(dialogInterface -> {
            Button saveButton = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
            CircularProgressIndicator spinner = ((AlertDialog) dialogInterface).findViewById(R.id.spinner);
            saveButton.setOnClickListener(view -> {
                TextInputLayout currentPasswordLayout = ((AlertDialog) dialogInterface).findViewById(R.id.currentPasswordLayout);
                TextInputEditText currentPasswordInput = ((AlertDialog) dialogInterface).findViewById(R.id.currentPasswordInput);
                TextInputLayout newPasswordLayout = ((AlertDialog) dialogInterface).findViewById(R.id.newPasswordLayout);
                TextInputEditText newPasswordInput = ((AlertDialog) dialogInterface).findViewById(R.id.newPasswordInput);
                TextInputLayout repeatPasswordLayout = ((AlertDialog) dialogInterface).findViewById(R.id.repeatPasswordLayout);
                TextInputEditText repeatPasswordInput = ((AlertDialog) dialogInterface).findViewById(R.id.repeatPasswordInput);
                TextView editPasswordError = ((AlertDialog) dialogInterface).findViewById(R.id.editPasswordError);
                currentPasswordLayout.setError(null);
                newPasswordLayout.setError(null);
                repeatPasswordLayout.setError(null);
                editPasswordError.setVisibility(View.INVISIBLE);
                if (newPasswordInput.getText().toString().length() >= 6) {
                    if (newPasswordInput.getText().toString().equals(repeatPasswordInput.getText().toString())) {
                        spinner.setVisibility(View.VISIBLE);

                        AuthCredential credential = EmailAuthProvider
                                .getCredential(currentUser.getEmail(), currentPasswordInput.getText().toString().trim());
                        mAuth.getCurrentUser().reauthenticate(credential)
                                .addOnSuccessListener(aVoid -> currentUser.updatePassword(newPasswordInput.getText().toString())
                                        .addOnSuccessListener(aVoid1 -> {
                                            spinner.setVisibility(View.GONE);
                                            dialogInterface.dismiss();
                                            Snackbar.make(requireActivity().findViewById(R.id.coordinatorLayout), "Password updated successfully!", Snackbar.LENGTH_LONG)
                                                    .setAnchorView(requireActivity().findViewById(R.id.bottom_navigation))
                                                    .show();
                                        })
                                        .addOnFailureListener(e -> {
                                            editPasswordError.setText(e.getMessage());
                                            spinner.setVisibility(View.GONE);
                                            editPasswordError.setVisibility(View.VISIBLE);
                                        }))
                                .addOnFailureListener(e -> {
                                    editPasswordError.setText(e.getMessage());
                                    spinner.setVisibility(View.GONE);
                                    editPasswordError.setVisibility(View.VISIBLE);
                                });
                    } else {
                        repeatPasswordLayout.setError(getString(R.string.password_error));
                    }
                } else {
                    newPasswordLayout.setError(getString(R.string.password_short));
                }
            });
        });

        passwordBuilder.show();
    }

    private void switchTo() {
        Bundle flipBundle = new Bundle();
        flipBundle.putString("flip", "ToLogin");
        getParentFragmentManager().setFragmentResult("flipResult", flipBundle);
    }

}