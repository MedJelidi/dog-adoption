package tn.rabini.dogadoption;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.rbddevs.splashy.Splashy;

public class MainActivity extends AppCompatActivity {

    private String currentFragment = "ToHome";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Splashy(this)
                .setLogo(R.drawable.splash_logo_black)
                .setTitle("Dog Adoption")
                .setTitleColor(R.color.black)
                .setDuration(1000)
                .show();
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().hide();
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.main_fragment, HomeFragment.class, null)
                .commit();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int option = item.getItemId();
            toFragment(option == R.id.homeItem
                    ? "ToHome" : option == R.id.favoritesItem
                    ? "ToFavorites" : "ToProfile", null);
            return true;
        });

        getSupportFragmentManager().setFragmentResultListener("flipResult",
                this,
                (requestKey, result) -> toFragment(result.getString("flip"), result));
    }

    public void toFragment(String fragment, Bundle result) {
        if (currentFragment.equals(fragment)) {
            return;
        }
        currentFragment = fragment;
        switch (fragment) {
            case "ToLogin":
                replaceFragment(LoginFragment.class, null);
                break;
            case "ToRegister":
                replaceFragment(RegisterFragment.class, null);
                break;
            case "ToProfile":
                replaceFragment(FirebaseAuth.getInstance().getCurrentUser() == null ? LoginFragment.class : ProfileFragment.class, null);
                break;
            case "ToAddDog":
                replaceFragment(AddDogFragment.class, null);
                break;
            case "ToHome":
                replaceFragment(HomeFragment.class, null);
                break;
            case "ToFavorites":
                replaceFragment(FirebaseAuth.getInstance().getCurrentUser() == null ? LoginFragment.class : FavoritesFragment.class, null);
                break;
            case "ToDogDetails":
                replaceFragment(DogDetailsFragment.class, result);
                break;
            case "ToEditDog":
                replaceFragment(EditDogFragment.class, result);
                break;
        }
    }

    public void replaceFragment(Class fragmentClass, @Nullable Bundle result) {
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.main_fragment, fragmentClass, result)
                .commit();
    }
}