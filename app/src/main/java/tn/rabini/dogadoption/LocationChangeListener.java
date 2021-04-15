package tn.rabini.dogadoption;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

public class LocationChangeListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
//        ContentResolver contentResolver = context.getContentResolver();
//        int mode = Settings.Secure.getInt(
//                contentResolver, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
//
//        if (mode == Settings.Secure.LOCATION_MODE_OFF) {
//            Log.v("locationchangelistener", "offfff");
//        } else {
//            Log.v("locationchangelistener", "onnnnn");
//        }
    }
}
