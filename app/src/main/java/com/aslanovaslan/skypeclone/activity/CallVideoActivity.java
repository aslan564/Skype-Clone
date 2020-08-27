package com.aslanovaslan.skypeclone.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aslanovaslan.skypeclone.R;
import com.aslanovaslan.skypeclone.model.CallReceiverModel;
import com.aslanovaslan.skypeclone.model.CallSenderModel;
import com.aslanovaslan.skypeclone.model.UserModel;
import com.aslanovaslan.skypeclone.ui.home.Contacts;
import com.aslanovaslan.skypeclone.ui.settings.Settings;
import com.aslanovaslan.skypeclone.ui.videocating.VideoChatActivity;
import com.aslanovaslan.skypeclone.util.glide.GlideApp;
import com.aslanovaslan.skypeclone.util.internal.MessageEvent;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.provider.Settings.System.DEFAULT_RINGTONE_URI;
import static com.aslanovaslan.skypeclone.ui.home.Contacts.AA_RECEIVER_CALL_USER;


public class CallVideoActivity extends AppCompatActivity implements View.OnClickListener {
private static final String AA_SEND_CALL_USER = "AA_SEND_CALL_USER";
private TextView textViewCallUsername, textViewCallingUser;
private ImageView callActivityUserProfileImage;
private CircleImageView circleImageViewOpenCall, circleImageViewCloseCall, imageViewAuthor;
private static String authorImageUri = "https://scontent.fgyd5-1.fna.fbcdn.net/v/t1.0-9/75388101_1146788538999220_6248375953307729920_o.jpg?_nc_cat=102&_nc_sid=a4a2d7&_nc_ohc=Cw-uDBUkIPwAX_ndzDa&_nc_ht=scontent.fgyd5-1.fna&oh=6ff89b3f2e67a53dedfc4b5a2df890bc&oe=5F6A2824";

private FirebaseUser mUser;
private String userSenderKey, userReceiverKey;
private UserModel eventBusUserModel = null;
private DatabaseReference callingRef;
private CollectionReference documentReference;

private static final String TAG = "CallVideoActivity";
private String isFirstTime = "";
private Ringtone defaultRingtone=null;




@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_call_video);

    userSenderKey = getIntent().getStringExtra(AA_SEND_CALL_USER);
    userReceiverKey = getIntent().getStringExtra(AA_RECEIVER_CALL_USER);

    initializeVariable();


}

private void initializeVariable() {
    textViewCallUsername = findViewById(R.id.textViewCallUsername);
    circleImageViewOpenCall = findViewById(R.id.circleImageViewOpenCall);
    callActivityUserProfileImage = findViewById(R.id.callActivityUserProfileImage);
    circleImageViewCloseCall = findViewById(R.id.circleImageViewCloseCall);
    imageViewAuthor = findViewById(R.id.imageViewAuthor);
    textViewCallingUser = findViewById(R.id.textViewCallingUser);
    circleImageViewCloseCall.setOnClickListener(this);
    circleImageViewOpenCall.setOnClickListener(this);
    callingRef = FirebaseDatabase.getInstance().getReference("calling");

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    mUser = mAuth.getCurrentUser();

    documentReference = FirebaseFirestore.getInstance().collection("users");
    defaultRingtone = RingtoneManager.getRingtone(CallVideoActivity.this, DEFAULT_RINGTONE_URI);

    //Picasso.get().load(authorImageUri).centerCrop().placeholder(R.drawable.ic_baseline_person_outline_24).into(imageViewAuthor);


}

private void setCallingAndRinging(final String userKey, final UserModel otherModel) {
    if (eventBusUserModel != null) {

        callingRef.child("receiver").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isFirstTime.equals("clicked") && !snapshot.hasChild(userKey)) {
                    final CallReceiverModel senderModel =
                            new CallReceiverModel(eventBusUserModel.getUid(), eventBusUserModel.getNameSurname(), eventBusUserModel.getProfilePicturePath(), eventBusUserModel.getUid());
                    callingRef.child("receiver").child(userKey).updateChildren(senderModel.toMap()).addOnSuccessListener(CallVideoActivity.this, aVoid -> callingRef.child("sender").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot1) {
                            if (!snapshot1.hasChild(eventBusUserModel.getUid())) {
                                CallSenderModel callSenderModel =
                                        new CallSenderModel(otherModel.getUid(), otherModel.getNameSurname(), otherModel.getProfilePicturePath(), userKey);
                                callingRef.child("sender").child(eventBusUserModel.getUid()).updateChildren(callSenderModel.toMap()).addOnSuccessListener(CallVideoActivity.this,
                                        new OnSuccessListener<Void>() {
                                            @SuppressLint("SetTextI18n")
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                textViewCallingUser.setText("Calling…");
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.d(TAG, "onCancelled: " + error);
                        }
                    }));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: " + error);
            }
        });


    }
}


@Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
public void onMessageEvent(MessageEvent.MessageShareEvent event) {
    eventBusUserModel = event.getUserModel();
    Log.d("MessageEvent", "MessageEvent: " + eventBusUserModel);
}

@Override
protected void onResume() {
    super.onResume();
 /*   new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(3000);
                if (mUser != null) {
                    if (userSenderKey != null) {
                        // Toast.makeText(CallVideoActivity.this, "isledi", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "run: isledi isledi isledi isledi");
                        checkCallingState(mUser.getUid());
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }).start();
*/

}

@Override
protected void onRestart() {
    super.onRestart();
}

@Override
protected void onStart() {
    super.onStart();
    if (isFirstTime.equals("")) {
        EventBus.getDefault().register(this);
    }
    if (mUser != null) {
        if (userReceiverKey != null) {
            getReceiverUserData(userReceiverKey);
            checkRingingState(mUser.getUid());
        } else {
            Log.d(TAG, "onCreate: userReceiverKey" + null);
        }
        if (userSenderKey != null) {
            getSenderUser(userSenderKey);
            // Toast.makeText(CallVideoActivity.this, "isledi", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "run: working working isledi isledi");
            checkCallingState(mUser.getUid());
        } else {
            Log.d(TAG, "onCreate: userSenderKey" + null);
        }
    }
}


@Override
protected void onStop() {
    super.onStop();
    EventBus.getDefault().unregister(this);
    if (defaultRingtone != null) {
        defaultRingtone.stop();
    }
}

@SuppressLint("SetTextI18n")
private void getReceiverUserData(final String userKey) {
    documentReference.document(userKey).addSnapshotListener(CallVideoActivity.this, (documentSnapshot, error) -> {
        if (error != null) {
            error.printStackTrace();
        }
        if (documentSnapshot != null && documentSnapshot.exists()) {
            UserModel userOtherModel = documentSnapshot.toObject(UserModel.class);
            if (userOtherModel != null) {

                defaultRingtone.play();
                circleImageViewOpenCall.setVisibility(View.VISIBLE);
                textViewCallingUser.setText("Calling…");
                textViewCallUsername.setText(userOtherModel.getNameSurname());
                GlideApp.with(CallVideoActivity.this).load(userOtherModel.getProfilePicturePath()).placeholder(R.drawable.ic_baseline_person_outline_24)
                        .into(callActivityUserProfileImage);
            }
        }
    });
}

private void getSenderUser(final String userKey) {
    documentReference.document(userKey).addSnapshotListener(CallVideoActivity.this, (documentSnapshot, error) -> {
        if (error != null) {
            error.printStackTrace();
        }
        if (documentSnapshot != null && documentSnapshot.exists()) {
            UserModel userOtherModel = documentSnapshot.toObject(UserModel.class);
            if (userOtherModel != null) {

                setCallingAndRinging(userKey, userOtherModel);

                textViewCallUsername.setText(userOtherModel.getNameSurname());
                GlideApp.with(CallVideoActivity.this).load(userOtherModel.getProfilePicturePath()).placeholder(R.drawable.ic_baseline_person_outline_24)
                        .into(callActivityUserProfileImage);
            }
        }
    });
}

@Override
public void onBackPressed() {
    super.onBackPressed();

}

private void intentContactActivity() {
    Toast.makeText(CallVideoActivity.this, "Call cancelled", Toast.LENGTH_SHORT).show();
    Intent intent = new Intent(CallVideoActivity.this, Contacts.class);
    startActivity(intent);
    finish();

}

@Override
public void onClick(View view) {
    switch (view.getId()) {

        case R.id.circleImageViewOpenCall:
            defaultRingtone.stop();
            Intent intentVideoCallAct = new Intent(CallVideoActivity.this, VideoChatActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intentVideoCallAct.putExtra("aslan", userReceiverKey);
            startActivity(intentVideoCallAct);
            finish();
            break;
        case R.id.circleImageViewCloseCall:
            isFirstTime = "clicked";
            defaultRingtone.stop();
            if (userSenderKey != null && mUser != null) {
                deleteCallerAndRinging();
            }
            if (userReceiverKey != null && mUser != null) {
                deleteCallerAndRinging();
            }
            break;

    }
}

private void deleteCallerAndRinging() {
    callingRef.child("sender").removeValue().addOnSuccessListener(CallVideoActivity.this, aVoid -> callingRef.child("receiver").removeValue().addOnSuccessListener(CallVideoActivity.this, new OnSuccessListener<Void>() {
        @Override
        public void onSuccess(Void aVoid) {
            intentContactActivity();
        }
    }));
}

private void checkCallingState(String uid) {
    callingRef.child("sender").child(uid).addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (!snapshot.exists() && isFirstTime.equals("clicked")) {

                Intent intent = new Intent(CallVideoActivity.this, Contacts.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d(TAG, "onCancelled: " + error.getDetails());
        }
    });
}

private void checkRingingState(final String uid) {

    callingRef.child("receiver").child(uid).addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (!snapshot.exists()) {

                Intent intent = new Intent(CallVideoActivity.this, Contacts.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d(TAG, "onCancelled: " + error.getDetails());
        }
    });
}
}