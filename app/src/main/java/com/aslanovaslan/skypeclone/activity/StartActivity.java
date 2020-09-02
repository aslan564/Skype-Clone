package com.aslanovaslan.skypeclone.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.aslanovaslan.skypeclone.R;
import com.aslanovaslan.skypeclone.RegisterActivity;
import com.aslanovaslan.skypeclone.ui.home.Contacts;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartActivity extends AppCompatActivity {
private FirebaseAuth mAuth;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mAuth = FirebaseAuth.getInstance();
}

@Override
protected void onStart() {
    super.onStart();
    FirebaseUser mUser = mAuth.getCurrentUser();
    if (mUser == null) {
        Intent intentRegister = new Intent(StartActivity.this, RegisterActivity.class);
        startActivity(intentRegister);
        finish();
    } else {
        Intent intentContacts = new Intent(StartActivity.this, Contacts.class);
        startActivity(intentContacts);
        finish();
    }
}
}