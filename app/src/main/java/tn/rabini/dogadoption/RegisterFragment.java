package tn.rabini.dogadoption;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import tn.rabini.dogadoption.models.User;

import static android.content.ContentValues.TAG;

public class RegisterFragment extends Fragment {


    private View v;
    private TextView errorView;
    private CircularProgressIndicator spinner;
    private TextInputEditText usernameInput, emailInput, phoneInput, passwordInput, confirmPasswordInput;
    private TextInputLayout usernameLayout, emailLayout, phoneLayout, passwordLayout, confirmPasswordLayout;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

//        V/USER: DataSnapshot { key = D74kPvKIiFWW28N6iE9huB5VZIq1, value = {password=123456, phone=12345678, email=mohammedjeflidi05@gmail.com, username=User12} }
//        V/USER: DataSnapshot { key = cXGopWyN4kMjHXEix1PiFGc1Jz12, value = {password=123456, phone=65478888, email=mohammedjelidi05@gmail.com, username=medjelidi} }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_register, container, false);
        errorView = v.findViewById(R.id.errorView);
        spinner = v.findViewById(R.id.spinner);

        usernameInput = v.findViewById(R.id.usernameInput);
        emailInput = v.findViewById(R.id.emailInput);
        phoneInput = v.findViewById(R.id.phoneInput);
        passwordInput = v.findViewById(R.id.passwordInput);
        confirmPasswordInput = v.findViewById(R.id.confirmPasswordInput);

        usernameLayout = v.findViewById(R.id.usernameLayout);
        emailLayout = v.findViewById(R.id.emailLayout);
        phoneLayout = v.findViewById(R.id.phoneLayout);
        passwordLayout = v.findViewById(R.id.passwordLayout);
        confirmPasswordLayout = v.findViewById(R.id.confirmPasswordLayout);

        TextView signInLink = v.findViewById(R.id.signInLink);
        signInLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchTo("ToLogin");
            }
        });


        Button signUpButton = v.findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               onSignUp();
            }
        });

        return v;
    }


    //    @Override
//    public void onResponse(Call<String> call, Response<String> response) {
//        spinner.setVisibility(View.INVISIBLE);
//        if (response.isSuccessful()) {
//            Toast.makeText(getContext(), response.body(), Toast.LENGTH_LONG).show();
//            switchTo("ToProfile");
//        } else {
//            try {
//                errorView.setText(response.errorBody().string());
//            } catch (IOException e) {
//                errorView.setText(R.string.error);
//                e.printStackTrace();
//            }
//            errorView.setVisibility(View.VISIBLE);
//        }
//    }
//
//    @Override
//    public void onFailure(Call<String> call, Throwable t) {
//        errorView.setText(R.string.error);
//        spinner.setVisibility(View.INVISIBLE);
//        errorView.setVisibility(View.VISIBLE);
//        t.printStackTrace();
//    }

    private void onSignUp() {
        errorView.setVisibility(View.INVISIBLE);

        String usernameValue = usernameInput.getText().toString();
        String emailValue = emailInput.getText().toString();
        String phoneValue = phoneInput.getText().toString();
        String passwordValue = passwordInput.getText().toString();
        String confirmPasswordValue = confirmPasswordInput.getText().toString();

        if (formValid(usernameLayout, emailLayout, phoneLayout, passwordLayout, confirmPasswordLayout, usernameValue, emailValue, phoneValue, passwordValue, confirmPasswordValue)) {
            spinner.setVisibility(View.VISIBLE);
            usernameLayout.setError(null);
            emailLayout.setError(null);
            phoneLayout.setError(null);
            passwordLayout.setError(null);
            confirmPasswordLayout.setError(null);
            errorView.setVisibility(View.INVISIBLE);

            createUser(usernameValue, emailValue, phoneValue, passwordValue);

        }
    }


//    private boolean userExists(String usernameValue, String phoneValue) {
//        CountDownLatch task = new CountDownLatch(1);
//        ArrayList<String> usernames = new ArrayList<>();
//        ArrayList<String> phones = new ArrayList<>();
//        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
//        databaseRef.child("Users").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot item : snapshot.getChildren()) {
//                    User user = item.getValue(User.class);
//                    usernames.add(user.getUsername());
//                    phones.add(user.getPhone());
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//        try {
//            task.await();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        if (usernames.contains(usernameValue)) {
//            spinner.setVisibility(View.INVISIBLE);
//            usernameLayout.setError("Username exists.");
//            return true;
//        }
//
//        if (phones.contains(phoneValue)) {
//            spinner.setVisibility(View.INVISIBLE);
//            phoneLayout.setError("Phone exists.");
//            return true;
//        }
//
//        return false;
//
//    }

    public void createUser(String usernameValue, String emailValue, String phoneValue, String passwordValue) {
        mAuth.createUserWithEmailAndPassword(emailValue, passwordValue).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    User user = new User(usernameValue, emailValue, phoneValue, passwordValue);
                    FirebaseDatabase.getInstance()
                            .getReference("Users")
                            .child(mAuth.getCurrentUser().getUid())
                            .setValue(user)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(requireContext(), "Successfully registered!", Toast.LENGTH_SHORT).show();
                                        spinner.setVisibility(View.INVISIBLE);
                                        switchTo("ToProfile");
                                    }
                                }
                            });
                } else {
                    spinner.setVisibility(View.INVISIBLE);
                    try {
                        throw Objects.requireNonNull(task.getException());
                    } catch(FirebaseAuthUserCollisionException e) {
                        emailLayout.setError("Email already exists.");
                    } catch(Exception e) {
                        Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private boolean formValid(TextInputLayout usernameLayout,
                              TextInputLayout emailLayout,
                              TextInputLayout phoneLayout,
                              TextInputLayout passwordLayout,
                              TextInputLayout confirmPasswordLayout,
                              String usernameValue,
                              String emailValue,
                              String phoneValue,
                              String passwordValue,
                              String confirmPasswordValue) {
        if (usernameValue == null || usernameValue.length() < 6) {
            usernameLayout.setError(getString(R.string.username_error));
            return false;
        }
        if (emailValue == null || !Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
            emailLayout.setError(getString(R.string.email_error));
            return false;
        }

        if (phoneValue == null || !validPhone(phoneValue)) {
            phoneLayout.setError(getString(R.string.phone_error));
            return false;
        }

        if (passwordValue == null || passwordValue.length() < 6) {
            passwordLayout.setError(getString(R.string.password_short));
            return false;
        }
        if (confirmPasswordValue == null || !confirmPasswordValue.equals(passwordValue)) {
            confirmPasswordLayout.setError(getString(R.string.password_error));
            return false;
        }
        return true;
    }

    private boolean validPhone(String phoneValue) {
        return (phoneValue.startsWith("9")
                || phoneValue.startsWith("7")
                || phoneValue.startsWith("5")
                || phoneValue.startsWith("4")
                || phoneValue.startsWith("2"))
                && phoneValue.length() == 8;
    }

    private void switchTo(String fragmentName) {
        Bundle flipBundle = new Bundle();
        flipBundle.putString("flip", fragmentName);
        getParentFragmentManager().setFragmentResult("flipResult", flipBundle);
    }
}