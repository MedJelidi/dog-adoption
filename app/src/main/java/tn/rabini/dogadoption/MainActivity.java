package tn.rabini.dogadoption;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.rbddevs.splashy.Splashy;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

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
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int option = item.getItemId();
            toFragment(option == R.id.homeItem
                    ? "ToHome" : option == R.id.favoritesItem
                    ? "ToFavorites" : "ToProfile", null);
            return true;
        });

        bottomNavigationView.setOnNavigationItemReselectedListener(item -> {

        });

        getSupportFragmentManager().setFragmentResultListener("flipResult",
                this,
                (requestKey, result) -> toFragment(result.getString("flip"), result));
    }

    public void toFragment(String fragment, Bundle result) {
        switch (fragment) {
            case "ToLogin":
                replaceFragment(LoginFragment.class, null);
                bottomNavigationView.getMenu().findItem(R.id.profileItem).setChecked(true);
                break;
            case "ToRegister":
                replaceFragment(RegisterFragment.class, null);
                bottomNavigationView.getMenu().findItem(R.id.profileItem).setChecked(true);
                break;
            case "ToProfile":
                replaceFragment(FirebaseAuth.getInstance().getCurrentUser() == null ? LoginFragment.class : ProfileFragment.class, null);
                bottomNavigationView.getMenu().findItem(R.id.profileItem).setChecked(true);
                break;
            case "ToAddDog":
                replaceFragment(AddDogFragment.class, null);
                bottomNavigationView.getMenu().findItem(R.id.homeItem).setChecked(true);
                break;
            case "ToHome":
                replaceFragment(HomeFragment.class, null);
                bottomNavigationView.getMenu().findItem(R.id.homeItem).setChecked(true);
                break;
            case "ToFavorites":
                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    replaceFragment(LoginFragment.class, null);
                    bottomNavigationView.getMenu().findItem(R.id.profileItem).setChecked(true);
                } else {
                    replaceFragment(FavoritesFragment.class, null);
                    bottomNavigationView.getMenu().findItem(R.id.favoritesItem).setChecked(true);
                }
                break;
            case "ToDogDetails":
                replaceFragment(DogDetailsFragment.class, result);
                bottomNavigationView.getMenu().findItem(R.id.homeItem).setChecked(true);
                break;
            case "ToEditDog":
                replaceFragment(EditDogFragment.class, result);
                bottomNavigationView.getMenu().findItem(R.id.profileItem).setChecked(true);
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