package tn.rabini.dogadoption;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentResultListener;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.main_fragment, HomeFragment.class, null)
                .commit();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                replaceFragment(item.getItemId(), null);
                return true;
            }
        });

        getSupportFragmentManager().setFragmentResultListener("flipResult", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                if (result.getString("flip").equals("ToLogin")) {
                    replaceFragment(R.layout.fragment_login, null);
                } else if (result.getString("flip").equals("ToRegister")) {
                    replaceFragment(R.layout.fragment_register, null);
                } else if (result.getString("flip").equals("ToProfile")) {
                    replaceFragment(R.id.profileItem, null);
                } else if (result.getString("flip").equals("ToAddDog")) {
                    replaceFragment(R.layout.fragment_add_dog, null);
                } else if (result.getString("flip").equals("ToHome")) {
                    replaceFragment(R.id.homeItem, null);
                } else if (result.getString("flip").equals("ToDogDetails")) {
                    replaceFragment(R.layout.fragment_dog_details, result);
                }
            }
        });
    }

    public void replaceFragment(int fragmentClass, @Nullable Bundle result) {
        switch (fragmentClass) {
            case R.id.homeItem:
                getSupportFragmentManager()
                        .beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.main_fragment, HomeFragment.class, null)
                        .commit();
                break;

            case R.id.favoritesItem:
                getSupportFragmentManager()
                        .beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.main_fragment, FavoritesFragment.class, null)
                        .commit();
                break;

            case R.id.profileItem:
                // TO MODIFY
                if (mAuth.getCurrentUser() != null) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.main_fragment, ProfileFragment.class, null)
                            .commit();
                } else {
                    replaceFragment(R.layout.fragment_login, null);
                }
                break;

            case R.layout.fragment_login:
                getSupportFragmentManager()
                        .beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.main_fragment, LoginFragment.class, null)
                        .commit();
                break;

            case R.layout.fragment_register:
                getSupportFragmentManager()
                        .beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.main_fragment, RegisterFragment.class, null)
                        .commit();
                break;

            case R.layout.fragment_add_dog:
                getSupportFragmentManager()
                        .beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.main_fragment, AddDogFragment.class, null)
                        .commit();
                break;

            case R.layout.fragment_dog_details:
                getSupportFragmentManager()
                        .beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.main_fragment, DogDetailsFragment.class, result)
                        .commit();
                break;

            default:
                break;
        }

    }
}