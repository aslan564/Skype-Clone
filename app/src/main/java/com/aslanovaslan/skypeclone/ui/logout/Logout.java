package com.aslanovaslan.skypeclone.ui.logout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.aslanovaslan.skypeclone.R;
import com.aslanovaslan.skypeclone.RegisterActivity;
import com.aslanovaslan.skypeclone.util.BottomNavigationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;

public class Logout extends AppCompatActivity {

private BottomNavigationView navView;
private Button buttonLogOutProfile,buttonReadMore;
private BottomNavigationView.OnNavigationItemSelectedListener bottomNavigationHelper;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_logout);
    navView = findViewById(R.id.nav_view_logout);
    buttonLogOutProfile = findViewById(R.id.buttonLogOutProfile);
    buttonReadMore = findViewById(R.id.buttonReadMore);
    setBottomNav();
    buttonReadMore.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Toast.makeText(Logout.this, "read more", Toast.LENGTH_SHORT).show();
        }
    });
    buttonLogOutProfile.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            FirebaseAuth.getInstance().signOut();
            Intent intent=new Intent(Logout.this, RegisterActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    });
}

private void setBottomNav() {
    bottomNavigationHelper = BottomNavigationHelper.changeView(Logout.this);
    navView.setOnNavigationItemSelectedListener(bottomNavigationHelper);
    MenuItem menuItem = navView.getMenu().getItem(3);
    menuItem.setChecked(true);


}


@Override
protected void onResume() {
    super.onResume();


}

}