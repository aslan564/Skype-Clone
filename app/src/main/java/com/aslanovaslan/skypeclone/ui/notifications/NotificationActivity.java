package com.aslanovaslan.skypeclone.ui.notifications;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.aslanovaslan.skypeclone.R;
import com.aslanovaslan.skypeclone.RegisterActivity;
import com.aslanovaslan.skypeclone.ui.home.Contacts;
import com.aslanovaslan.skypeclone.util.BottomNavigationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class NotificationActivity extends AppCompatActivity {


private FirebaseAuth mAuth;
private FirebaseUser mUser;
private BottomNavigationView navView;
private RecyclerView recyclerViewNotificationList;
private BottomNavigationView.OnNavigationItemSelectedListener bottomNavigationHelper;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_notification);
    navView = findViewById(R.id.nav_view_notification);
    initializeVariable();


    if (mUser == null) {
        checkUserState();
    }
    setBottomNav();
}

private void initializeVariable() {
    recyclerViewNotificationList = findViewById(R.id.recyclerViewNotification);
    recyclerViewNotificationList.setHasFixedSize(true);
    recyclerViewNotificationList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

    mAuth= FirebaseAuth.getInstance();
    mUser=mAuth.getCurrentUser();
}

private void setBottomNav() {
    bottomNavigationHelper = BottomNavigationHelper.changeView(NotificationActivity.this);
    navView.setOnNavigationItemSelectedListener(bottomNavigationHelper);
    MenuItem menuItem = navView.getMenu().getItem(1);
    menuItem.setChecked(true);
}
private void checkUserState( ) {
    Intent intent = new Intent(NotificationActivity.this, RegisterActivity.class);
    startActivity(intent);
    finish();
}
public static class NotificationViewHolder extends RecyclerView.ViewHolder {

    TextView userName;
    Button accept, cancel;
    ImageView userImage;
    ConstraintLayout cardViewNotification;

    public NotificationViewHolder(@NonNull View itemView) {
        super(itemView);
        userName = itemView.findViewById(R.id.textViewUserName);
        accept = itemView.findViewById(R.id.buttonAcceptFriendRequest);
        cancel = itemView.findViewById(R.id.buttonCancelFriendRequest);
        userImage = itemView.findViewById(R.id.imageViewNotification);
        cardViewNotification = itemView.findViewById(R.id.cardViewNotification);

    }
}
}