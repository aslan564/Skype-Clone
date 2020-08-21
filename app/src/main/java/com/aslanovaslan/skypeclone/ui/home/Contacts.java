package com.aslanovaslan.skypeclone.ui.home;

import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;
import android.widget.Toolbar;

import com.aslanovaslan.skypeclone.R;
import com.aslanovaslan.skypeclone.RegisterActivity;
import com.aslanovaslan.skypeclone.activity.FindPeopleActivity;
import com.aslanovaslan.skypeclone.util.BottomNavigationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static com.aslanovaslan.skypeclone.RegisterActivity.USER;

public class Contacts extends AppCompatActivity {


private Toolbar toolbar;
private RecyclerView recyclerViewContactsList;
private ImageView imageViewContacts;
private FirebaseAuth mAuth;
private FirebaseUser mUser;
private BottomNavigationView navView;
private BottomNavigationView.OnNavigationItemSelectedListener bottomNavigationHelper;
private static int AA_SELECTED_ITEM = 1;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_contacts);

    initializeVariable();
    setActionBar(toolbar);
    final String intent = (getIntent().getStringExtra(USER));
    if (intent != null) {
        Toast.makeText(this, "user movcuddur", Toast.LENGTH_SHORT).show();
    }
  /*  if (mUser == null) {
        checkUserState();
    }*/

    imageViewContacts.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intentFindPeople = new Intent(Contacts.this, FindPeopleActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intentFindPeople);
        }
    });

    setBottomNav();
}
private void checkUserState( ) {
    Intent intent = new Intent(Contacts.this, RegisterActivity.class);
    startActivity(intent);
    finish();
}
private void initializeVariable() {
    navView = findViewById(R.id.nav_view_main);
    toolbar = findViewById(R.id.toolbar);
    recyclerViewContactsList = findViewById(R.id.recyclerViewContactsList);
    imageViewContacts = findViewById(R.id.imageViewContacts);
    mAuth=FirebaseAuth.getInstance();
    mUser=mAuth.getCurrentUser();
    recyclerViewContactsList.setHasFixedSize(true);
    recyclerViewContactsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
}

private void setBottomNav() {
    bottomNavigationHelper = BottomNavigationHelper.changeView(Contacts.this);
    navView.setOnNavigationItemSelectedListener(bottomNavigationHelper);
    MenuItem menuItem = navView.getMenu().getItem(0);
    menuItem.setChecked(true);
}

}