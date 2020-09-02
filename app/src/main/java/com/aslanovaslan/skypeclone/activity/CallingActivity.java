package com.aslanovaslan.skypeclone.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aslanovaslan.skypeclone.R;
import com.aslanovaslan.skypeclone.model.CallingModel;
import com.aslanovaslan.skypeclone.model.TalkingModel;
import com.aslanovaslan.skypeclone.model.UserModel;
import com.aslanovaslan.skypeclone.ui.home.Contacts;
import com.aslanovaslan.skypeclone.ui.videocating.VideoChatActivity;
import com.aslanovaslan.skypeclone.util.CallingStateHelper;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static android.provider.Settings.System.DEFAULT_RINGTONE_URI;
import static com.aslanovaslan.skypeclone.ui.home.Contacts.AA_RECEIVER_CALL_USER;


public class CallingActivity extends AppCompatActivity implements View.OnClickListener {
private static final String AA_SEND_CALL_USER = "AA_SEND_CALL_USER";
private TextView textViewCallUsername, textViewCallingUser;
private ImageView callActivityUserProfileImage;
private Button circleImageViewOpenCall, circleImageViewCloseCall;

private FirebaseUser mUser;
private String userCallerKey, answeringUserId;
private CallingStateHelper callingState = CallingStateHelper.START;
private DatabaseReference callingRef, talkingRef;
private CollectionReference documentReference;

private static final String TAG = "CallVideoActivity";
private Ringtone defaultRingtone = null;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_call_video);

    userCallerKey = getIntent().getStringExtra(AA_SEND_CALL_USER);
    answeringUserId = getIntent().getStringExtra(AA_RECEIVER_CALL_USER);

    initializeVariable();


}

private void initializeVariable() {
    textViewCallUsername = findViewById(R.id.textViewCallUsername);
    circleImageViewOpenCall = findViewById(R.id.circleImageViewOpenCall);
    callActivityUserProfileImage = findViewById(R.id.callActivityUserProfileImage);
    circleImageViewCloseCall = findViewById(R.id.circleImageViewCloseCall);

    textViewCallingUser = findViewById(R.id.textViewCallingUser);
    circleImageViewCloseCall.setOnClickListener(this);
    circleImageViewOpenCall.setOnClickListener(this);
    callingRef = FirebaseDatabase.getInstance().getReference("calling");
    talkingRef = FirebaseDatabase.getInstance().getReference("talking").child(UUID.randomUUID().toString());
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    mUser = mAuth.getCurrentUser();

    documentReference = FirebaseFirestore.getInstance().collection("users");
    defaultRingtone = RingtoneManager.getRingtone(CallingActivity.this, DEFAULT_RINGTONE_URI);


    if (userCallerKey != null) {
        getCallerDataUser(userCallerKey);
    }
    if (answeringUserId != null) {
        getAnsweringUserData(answeringUserId);
    }

}


@Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
public void onMessageEvent(MessageEvent.MessageShareEvent event) {
    UserModel currentUserModel = event.getUserModel();
    Log.d("MessageEvent", "MessageEvent: " + currentUserModel);
}

@Override
protected void onResume() {
    super.onResume();

}

@Override
protected void onRestart() {
    super.onRestart();
}

@Override
protected void onStart() {
    super.onStart();
    if (callingState.equals(CallingStateHelper.START)) {
        EventBus.getDefault().register(this);
    }
    if (mUser != null) {

        if (userCallerKey != null) {
            String currentUserKey = mUser.getUid();
            callingRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {

                        if (snapshot.hasChild(currentUserKey)) {
                            String state = Objects.requireNonNull(snapshot.child(currentUserKey).child("state").getValue()).toString();
                            if (state.equals(CallingStateHelper.CLOSE.name())) {
                                intentContactActivity();
                            }
                        }

                    } else {
                        Log.i(TAG, "davam eden danisq : yoxdur");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d(TAG, "onCancelled: " + error);
                }
            });
            documentReference.document(currentUserKey).addSnapshotListener(CallingActivity.this, (value, error) -> {
                if (error != null) {
                    Log.e(TAG, "onEvent: ", error);
                }

                if (value != null && value.getData() != null && value.exists()) {
                    UserModel userModel = value.toObject(UserModel.class);
                    if (userModel != null && !userModel.getChannel().equals("")) {
                        FirebaseDatabase.getInstance().getReference("talking").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.hasChild(userModel.getChannel()) && snapshot.child(userModel.getChannel()).hasChild(currentUserKey)) {
                                    intentToVideoCallActivity();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d(TAG, "onCancelled: " + error);

                            }
                        });
                    }
                }
            });

        }
        if (answeringUserId != null) {
            String currentUserKey = mUser.getUid();
            callingRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {

                            if (snapshot.hasChild(currentUserKey)) {
                                String state = Objects.requireNonNull(snapshot.child(currentUserKey).child("state").getValue()).toString();
                                if (state.equals(CallingStateHelper.CLOSE.name())) {
                                    intentContactActivity();
                                }
                            }

                    } else {
                        Log.d(TAG, "yaradilmiyib: ");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d(TAG, "onCancelled: " + error);
                }
            });


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
private void getAnsweringUserData(final String callerId) {
    documentReference.document(callerId).addSnapshotListener(CallingActivity.this, (documentSnapshot, error) -> {
        if (error != null) {
            error.printStackTrace();
        }
        if (documentSnapshot != null && documentSnapshot.exists()) {
            UserModel userOtherModel = documentSnapshot.toObject(UserModel.class);
            if (userOtherModel != null) {

                defaultRingtone.play();
                circleImageViewOpenCall.setVisibility(View.VISIBLE);
                circleImageViewCloseCall.setEnabled(true);
                textViewCallingUser.setText("Calling…" + userOtherModel.getNameSurname());
                textViewCallUsername.setText(userOtherModel.getNameSurname());
                GlideApp.with(CallingActivity.this).load(userOtherModel.getProfilePicturePath()).placeholder(R.drawable.ic_baseline_person_outline_24)
                        .into(callActivityUserProfileImage);
            }
        }
    });
}

private void getCallerDataUser(final String answeringUserID) {
    documentReference.document(answeringUserID).addSnapshotListener(CallingActivity.this, (documentSnapshot, error) -> {
        if (error != null) {
            error.printStackTrace();
        }
        if (documentSnapshot != null && documentSnapshot.exists()) {
            UserModel userOtherModel = documentSnapshot.toObject(UserModel.class);
            if (userOtherModel != null) {

                setCallingAndRinging(userOtherModel);

                textViewCallUsername.setText(userOtherModel.getNameSurname());
                GlideApp.with(CallingActivity.this).load(userOtherModel.getProfilePicturePath()).placeholder(R.drawable.ic_baseline_person_outline_24)
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
    //Toast.makeText(CallingActivity.this, "Call cancelled", Toast.LENGTH_SHORT).show();
    Intent intent = new Intent(CallingActivity.this, Contacts.class);
    startActivity(intent);
    finish();
}

@Override
public void onClick(View view) {
    switch (view.getId()) {

        case R.id.circleImageViewOpenCall:
            callingState = CallingStateHelper.WAITING;
            defaultRingtone.stop();

            String currentUserID = mUser.getUid();
            TalkingModel talkingModel = new TalkingModel(answeringUserId, currentUserID);


            talkingRef.child(currentUserID).setValue(talkingModel.toMap())
                    .addOnSuccessListener(
                            CallingActivity.this,
                            aVoids -> talkingRef.child(answeringUserId).setValue(talkingModel.toMap()).addOnSuccessListener(CallingActivity.this, new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    documentReference
                                            .document(answeringUserId)
                                            .update("channel", talkingRef.getKey())
                                            .addOnSuccessListener(CallingActivity.this, new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    documentReference.document(currentUserID)
                                                            .update("channel", talkingRef.getKey())
                                                            .addOnSuccessListener(CallingActivity.this, new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    deleteCallerAndRinging(true);
                                                                }
                                                            });
                                                }
                                            });
                                }
                            }));
            break;
        case R.id.circleImageViewCloseCall:
            callingState = CallingStateHelper.WAITING;
            defaultRingtone.stop();
            deleteCallerAndRinging(false);
            break;

        default:
            throw new IllegalStateException("Unexpected value: " + view.getId());
    }
}

private void setCallingState(CallingStateHelper state) {
    String currentUserID = mUser.getUid();
    final Map<String, Object> callingModel = new HashMap<>();
    if (state.equals(CallingStateHelper.CLOSE)) {
        callingModel.put("state", CallingStateHelper.CLOSE);
        talkingRef.child(currentUserID).updateChildren(callingModel).addOnSuccessListener(CallingActivity.this, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                intentContactActivity();
            }
        });

    } else if (state.equals(CallingStateHelper.OPEN)) {
        callingModel.put("state", CallingStateHelper.OPEN);
        talkingRef.child(currentUserID).updateChildren(callingModel);
    }
    else if (state.equals(CallingStateHelper.WAITING)) {
        callingModel.put("state", CallingStateHelper.WAITING);
        talkingRef.child(currentUserID).updateChildren(callingModel);
    }
}

private void intentToVideoCallActivity() {
    callingState = CallingStateHelper.OPEN;
    Intent intentVideoCallAct = new Intent(CallingActivity.this, VideoChatActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
    startActivity(intentVideoCallAct);
    finish();
}

private void deleteCallerAndRinging(boolean b) {
    String currentUser = mUser.getUid();

    callingRef.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.hasChild(currentUser)) {
                if (snapshot.child(currentUser).hasChild("answering")) {

                    final Map<String, Object> callingModel = new HashMap<>();
                    callingModel.put("state", CallingStateHelper.CLOSE);

                    String answeringID = Objects.requireNonNull(snapshot.child(currentUser).child("answering").getValue()).toString();
                    callingRef.child(currentUser).removeValue();

                    if (!b) {
                        callingRef.child(answeringID).setValue(callingModel);
                        intentContactActivity();
                    } else {
                        callingRef.child(answeringID).removeValue();
                        intentToVideoCallActivity();
                    }
                } else if (snapshot.child(currentUser).hasChild("caller")) {
                    final Map<String, Object> callingModel = new HashMap<>();
                    callingModel.put("state", CallingStateHelper.CLOSE);

                    String callerID = Objects.requireNonNull(snapshot.child(currentUser).child("caller").getValue()).toString();
                    callingRef.child(currentUser).removeValue();

                    if (!b) {
                        callingRef.child(callerID).setValue(callingModel);
                        intentContactActivity();
                    } else {
                        callingRef.child(callerID).removeValue();
                        intentToVideoCallActivity();
                    }
                } else {
                    Log.d(TAG, "onDataChange: bos geldi zengin datasi");
                }
            } else {
                Log.d(TAG, "onDataChange: zeng yoxdu");
                intentContactActivity();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.e(TAG, "onCancelled: ", error.toException());
        }
    });
}


@SuppressLint("SetTextI18n")
private void setCallingAndRinging(final UserModel answeringUserModel) {
    if (answeringUserModel != null && mUser != null) {

        String currentUserUID = mUser.getUid();

        final Map<String, Object> callingModel = new HashMap<>();
        callingModel.put("answering", answeringUserModel.getUid());
        callingModel.put("state", CallingStateHelper.WAITING);

        callingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (callingState.equals(CallingStateHelper.START) && !snapshot.hasChild(currentUserUID)) {
                    callingRef
                            .child(currentUserUID)
                            .updateChildren(callingModel)
                            .addOnSuccessListener(CallingActivity.this, new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    final Map<String, Object> answeringModel = new HashMap<>();
                                    answeringModel.put("caller", currentUserUID);
                                    answeringModel.put("state", CallingStateHelper.WAITING);

                                    callingRef
                                            .child(answeringUserModel.getUid())
                                            .updateChildren(answeringModel)
                                            .addOnSuccessListener(CallingActivity.this, new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    textViewCallingUser.setText("Calling…" + answeringUserModel.getNameSurname());
                                                    circleImageViewCloseCall.setEnabled(true);
                                                    setCallingState(CallingStateHelper.WAITING);
                                                }
                                            });

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: " + error);
            }
        });
    }
}


}