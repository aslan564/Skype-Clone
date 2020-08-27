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
import com.aslanovaslan.skypeclone.author.AuthorActivity;
import com.aslanovaslan.skypeclone.util.BottomNavigationHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Logout extends AppCompatActivity implements View.OnClickListener {

private BottomNavigationView navView;
private DocumentReference mRreference;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_logout);
    initializeVariable();
    setBottomNav();

}

private void initializeVariable() {
    navView = findViewById(R.id.nav_view_logout);
    Button buttonDeleteAccount = findViewById(R.id.buttonDeleteAccount);
    Button buttonLogOutProfile = findViewById(R.id.buttonLogOutProfile);
    Button buttonReadMore = findViewById(R.id.buttonReadMore);
    CircleImageView imageViewAuthor = findViewById(R.id.imageViewAuthor);
    imageViewAuthor.setOnClickListener(this);
    buttonReadMore.setOnClickListener(this);
    buttonLogOutProfile.setOnClickListener(this);
    buttonDeleteAccount.setOnClickListener(this);
    mRreference = FirebaseFirestore.getInstance().collection("author").document("dhtoX4U9o0X3eiFRJWVD");
    mRreference.get().addOnSuccessListener(documentSnapshot -> {
     /*Map<String,Object> author= documentSnapshot.getData();
     author.*/
    }).addOnFailureListener(e -> {

    });
}

private void setBottomNav() {
    BottomNavigationView.OnNavigationItemSelectedListener bottomNavigationHelper = BottomNavigationHelper.changeView(Logout.this);
    navView.setOnNavigationItemSelectedListener(bottomNavigationHelper);
    MenuItem menuItem = navView.getMenu().getItem(3);
    menuItem.setChecked(true);


}


@Override
protected void onResume() {
    super.onResume();


}

@Override
public void onClick(View view) {
    switch (view.getId()) {
        case R.id.buttonLogOutProfile:
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(Logout.this, RegisterActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

            break;
        case R.id.buttonDeleteAccount:

            Toast.makeText(Logout.this, "account  deleted", Toast.LENGTH_SHORT).show();

            break;
        case R.id.buttonReadMore:

            Toast.makeText(Logout.this, "read more", Toast.LENGTH_SHORT).show();

            break;
        case R.id.imageViewAuthor:
            Intent intentAuthor = new Intent(Logout.this, AuthorActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intentAuthor);
            break;
    }
}
}