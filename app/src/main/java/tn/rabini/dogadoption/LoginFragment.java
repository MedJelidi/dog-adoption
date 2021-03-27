package tn.rabini.dogadoption;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;

public class LoginFragment extends Fragment {

    private TextView errorView;
    private TextInputLayout emailLayout, passwordLayout;
    private TextInputEditText emailInput, passwordInput;
    private CircularProgressIndicator spinner;
    private View v;
    private SharedPreferences sharedPreferences;
    private final String SP_KEY = "PROFILE_INFO";
    private FirebaseAuth mAuth;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_login, container, false);
        emailLayout = (TextInputLayout) v.findViewById(R.id.emailLayout);
        passwordLayout = (TextInputLayout) v.findViewById(R.id.passwordLoginLayout);
        emailInput = (TextInputEditText) v.findViewById(R.id.emailInput);
        passwordInput = (TextInputEditText) v.findViewById(R.id.passwordLoginInput);
        errorView = (TextView) v.findViewById(R.id.errorView);
        spinner = (CircularProgressIndicator) v.findViewById(R.id.spinner);

        TextView signUpLink = (TextView) v.findViewById(R.id.signUpLink);
        signUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchTo("ToRegister");
            }
        });


        Button signIn = (Button) v.findViewById(R.id.signInButton);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSignIn();
            }
        });

        return v;
    }

    private void onSignIn() {
        errorView.setVisibility(View.INVISIBLE);
        String emailValue = emailInput.getText().toString();
        String passwordValue = passwordInput.getText().toString();
        if (TextUtils.isEmpty(emailValue) || TextUtils.isEmpty(passwordValue) || passwordValue.length() < 6) {

            if (TextUtils.isEmpty(emailValue)) {
                emailLayout.setError(getString(R.string.field_empty));
            }

            if (TextUtils.isEmpty(passwordValue)) {
                passwordLayout.setError(getString(R.string.field_empty));
            }

            if (passwordValue.length() < 6) {
                passwordLayout.setError(getString(R.string.password_short));
            }
            return;
        }

        spinner.setVisibility(View.VISIBLE);
        emailLayout.setError(null);
        passwordLayout.setError(null);
        errorView.setVisibility(View.INVISIBLE);

        mAuth.signInWithEmailAndPassword(emailValue, passwordValue).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    switchTo("ToProfile");
                } else {
                    spinner.setVisibility(View.INVISIBLE);
                    Toast.makeText(requireContext(), "Error occurred.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // ON SUCCESS
//    spinner.setVisibility(View.INVISIBLE);
//                if (response.isSuccessful()) {
//        Toast.makeText(getContext(), response.body(), Toast.LENGTH_LONG).show();
//        sharedPreferences = requireActivity().getSharedPreferences(SP_KEY, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString("TOKEN", response.body());
//        editor.apply();
//        switchTo("ToProfile");
//    } else {
//        try {
//            errorView.setText(response.errorBody().string());
//        } catch (IOException e) {
//            errorView.setText(R.string.error);
//            e.printStackTrace();
//        }
//        errorView.setVisibility(View.VISIBLE);
//    }


    // ON FAILURE
//    spinner.setVisibility(View.INVISIBLE);
//                Toast.makeText(getContext(), R.string.error, Toast.LENGTH_LONG).show();
//                t.printStackTrace();

    private void switchTo(String fragmentName) {
        Bundle flipBundle = new Bundle();
        flipBundle.putString("flip", fragmentName);
        getParentFragmentManager().setFragmentResult("flipResult", flipBundle);
    }
}