package com.aslanovaslan.skypeclone.util;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.aslanovaslan.skypeclone.R;
import com.aslanovaslan.skypeclone.ui.home.Contacts;
import com.aslanovaslan.skypeclone.ui.logout.Logout;
import com.aslanovaslan.skypeclone.ui.notifications.NotificationActivity;
import com.aslanovaslan.skypeclone.ui.settings.Settings;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavigationHelper {
public static synchronized BottomNavigationView.OnNavigationItemSelectedListener changeView(final Activity contextActivity) {
   /* synchronized (this) {

    }*/
    return new BottomNavigationView.OnNavigationItemSelectedListener() {
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                Intent intentHome = new Intent(contextActivity, Contacts.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                contextActivity.startActivity(intentHome);
                break;
            case R.id.navigation_settings:
                Intent intentSettings = new Intent(contextActivity, Settings.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                contextActivity.startActivity(intentSettings);
                break;
            case R.id.navigation_notifications:
                Intent intentNotification = new Intent(contextActivity, NotificationActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                contextActivity.startActivity(intentNotification);
                break;
            case R.id.navigation_logout:
                Intent intentLogout = new Intent(contextActivity, Logout.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                contextActivity.startActivity(intentLogout);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + item.getItemId());
        }
        return false;
    }
};
}
}
