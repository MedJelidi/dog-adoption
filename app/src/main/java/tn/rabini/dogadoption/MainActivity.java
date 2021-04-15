package tn.rabini.dogadoption;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private double lat, lng;
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        new Splashy(this)
//                .setLogo(R.drawable.splash_logo_black)
//                .setTitle("Dog Adoption")
//                .setTitleColor(R.color.black)
//                .setDuration(1000)
//                .show();
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = initLocationListener();
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().hide();
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int option = item.getItemId();
            if (option == R.id.homeItem)
                toFragment("ToHome", null);
            else if (option == R.id.favoritesItem)
                toFragment("ToFavorites", null);
            else {
                Bundle bundle = new Bundle();
                String userID = null;
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null)
                    userID = currentUser.getUid();
                bundle.putString("userID", userID);
                toFragment("ToProfile", bundle);
            }
            return true;
        });

        bottomNavigationView.setOnNavigationItemReselectedListener(item -> {

        });

        getSupportFragmentManager().setFragmentResultListener("flipResult",
                this,
                (requestKey, result) -> toFragment(result.getString("flip"), result));

        handleGPS();
        handleInternet();
        replaceFragment(HomeFragment.class, gpsBundle());
    }

    private LocationListener initLocationListener() {
        return new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    lat = location.getLatitude();
                    lng = location.getLongitude();
                    locationManager.removeUpdates(locationListener);
                    MyLocation.myLat = lat;
                    MyLocation.myLng = lng;
                    Intent intent = new Intent("my-cord");
                    intent.putExtra("lat", lat);
                    intent.putExtra("lng", lng);
                    LocalBroadcastManager.getInstance(getApplicationContext())
                            .sendBroadcast(intent);
                    Log.v("locatiiiiiiiiiiiiiion", "blabla");
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
    }

    private void handleInternet() {
        InternetAvailabilityChecker.init(this);
        InternetAvailabilityChecker internetAvailabilityChecker = InternetAvailabilityChecker.getInstance();
        internetAvailabilityChecker.addInternetConnectivityListener(isConnected -> {
            if (!isConnected) {
                internetLostDialog();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkRequest networkRequest = new NetworkRequest.Builder().build();
            connectivityManager.registerNetworkCallback(networkRequest, new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    super.onAvailable(network);
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        locationListener = initLocationListener();
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, locationListener);
                    }
                }

                @Override
                public void onLost(Network network) {
                    super.onLost(network);
                    internetLostDialog();
                }
            });
        }

    }

    private void internetLostDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Connection lost...");
        alertDialog.setMessage("No internet connection. Please make sure you have internet access.");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    private void locationLostDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("GPS lost...");
        alertDialog.setMessage("Please turn on GPS for accurate location data.");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    private void setLatLng() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    private void handleGPS() {
        BroadcastReceiver gpsReceiver = new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().matches(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                    if (locationManager.isLocationEnabled()) {
                        Log.v("locationCHANNNNNGED", "enabled");
                        setLatLng();
                    } else {
                        Log.v("locationCHANNNNNGED", "disabled");
                        locationLostDialog();
                    }
                }
            }
        };
        registerReceiver(gpsReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));


        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());


        result.addOnCompleteListener(task -> {
            try {
                LocationSettingsResponse response = task.getResult(ApiException.class);
                setLatLng();
                // All location settings are satisfied. The client can initialize location
                // requests here.
            } catch (ApiException exception) {
                switch (exception.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the
                        // user a dialog.
                        try {
                            // Cast to a resolvable exception.
                            ResolvableApiException resolvable = (ResolvableApiException) exception;
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            resolvable.startResolutionForResult(
                                    MainActivity.this,
                                    LocationRequest.PRIORITY_HIGH_ACCURACY);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        } catch (ClassCastException e) {
                            // Ignore, should be an impossible error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    private Bundle gpsBundle() {
        Bundle result = new Bundle();
        result.putDouble("lat", lat);
        result.putDouble("lng", lng);
        return result;
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
                Bundle res = gpsBundle();
                res.putString("userID", result.getString("userID"));
                replaceFragment(FirebaseAuth.getInstance().getCurrentUser() == null ? LoginFragment.class : ProfileFragment.class, res);
                bottomNavigationView.getMenu().findItem(R.id.profileItem).setChecked(true);
                break;
            case "ToAddDog":
                replaceFragment(AddDogFragment.class, null);
                bottomNavigationView.getMenu().findItem(R.id.homeItem).setChecked(true);
                break;
            case "ToHome":
                replaceFragment(HomeFragment.class, gpsBundle());
                bottomNavigationView.getMenu().findItem(R.id.homeItem).setChecked(true);
                break;
            case "ToFavorites":
                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    replaceFragment(LoginFragment.class, null);
                    bottomNavigationView.getMenu().findItem(R.id.profileItem).setChecked(true);
                } else {
                    replaceFragment(FavoritesFragment.class, gpsBundle());
                    bottomNavigationView.getMenu().findItem(R.id.favoritesItem).setChecked(true);
                }
                break;
            case "ToDogDetails":
                replaceFragment(DogDetailsFragment.class, result);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions,
                                           @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
                setLatLng();
            } else {
                final Snackbar snackBar = Snackbar.make(findViewById(R.id.coordinatorLayout),
                        "Please turn on GPS for accurate location data.", Snackbar.LENGTH_INDEFINITE);
                snackBar.setAction("Dismiss", v -> snackBar.dismiss());
                snackBar.show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LocationRequest.PRIORITY_HIGH_ACCURACY) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // All required changes were successfully made
                    Log.i("GPS", "onActivityResult: GPS Enabled by user");
                    setLatLng();
                    break;
                case Activity.RESULT_CANCELED:
                    final Snackbar snackBar = Snackbar.make(findViewById(R.id.coordinatorLayout),
                            "Please turn on GPS for accurate location data.", Snackbar.LENGTH_INDEFINITE);
                    snackBar.setAction("Dismiss", v -> snackBar.dismiss());
                    snackBar.show();
                    break;
                default:
                    break;
            }
        }

    }
}