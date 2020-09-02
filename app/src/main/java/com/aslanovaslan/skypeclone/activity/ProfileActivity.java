package com.aslanovaslan.skypeclone.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aslanovaslan.skypeclone.R;
import com.aslanovaslan.skypeclone.model.FriendModel;
import com.aslanovaslan.skypeclone.model.UserModel;
import com.aslanovaslan.skypeclone.model.RequestModel;
import com.aslanovaslan.skypeclone.util.glide.GlideApp;
import com.aslanovaslan.skypeclone.util.internal.MessageEvent;
import com.aslanovaslan.skypeclone.util.internal.StateRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.aslanovaslan.skypeclone.activity.FindPeopleActivity.AA_SEND_VISIT_USER;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {
private ImageView imageViewOtherFriendProfile;
private TextView textViewUserNameOtherPeople;
private ProgressBar progressBarOtherProfile;
private Button buttonSendFriendRequest, buttonCancelFriendRequest;
private String otherUserUid, currentUserId;
private String userRequestState = StateRequest.NEW.name();
private UserModel eventBusUserModel;

private FirebaseUser mUser;
private DatabaseReference databaseReference;

private UserModel otherUserModel = null;
private static final String TAG = "ProfileActivity";

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_profile);
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    mUser = mAuth.getCurrentUser();
    if (mUser != null) {

        initializeVariable();
        String otherUserId = getIntent().getStringExtra(AA_SEND_VISIT_USER);
        if (otherUserId != null) {
            setImageToProfile(otherUserId);

        }
    }

}

@Override
public void onBackPressed() {
    super.onBackPressed();
    userRequestState = StateRequest.NEW.name();
}

private void setImageToProfile(final String userModelId) {
    FirebaseFirestore.getInstance().collection("users").document(userModelId).get()
            .addOnSuccessListener(ProfileActivity.this, documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    otherUserModel = documentSnapshot.toObject(UserModel.class);
                    if (otherUserModel != null) {
                        GlideApp.with(ProfileActivity.this).load(otherUserModel.getProfilePicturePath()).placeholder(R.drawable.ic_baseline_person_pin_24).into(imageViewOtherFriendProfile);
                        textViewUserNameOtherPeople.setText(otherUserModel.getNameSurname());
                        otherUserUid = otherUserModel.getUid();
                        progressBarOtherProfile.setVisibility(View.GONE);
                        isState(userModelId);
                    }
                }
            }).addOnFailureListener(Throwable::printStackTrace);


}

@SuppressLint("SetTextI18n")
private void uiStateCheck(String state) {
    if (!currentUserId.equals(otherUserUid)) {
        if (state.equals(StateRequest.RECEIVER.name())) {
            buttonCancelFriendRequest.setVisibility(View.VISIBLE);
            buttonSendFriendRequest.setVisibility(View.VISIBLE);
            buttonSendFriendRequest.setText("Add Friend");
            buttonCancelFriendRequest.setText("Cancel Friend Request");
        } else if (state.equals(StateRequest.SENDER.name())) {
            buttonCancelFriendRequest.setVisibility(View.GONE);
            buttonSendFriendRequest.setVisibility(View.VISIBLE);
            buttonSendFriendRequest.setText("Cancel Friend Request");
            userRequestState = StateRequest.SENDER.name();
        } else if (state.equals(StateRequest.NEW.name())) {
            buttonSendFriendRequest.setVisibility(View.VISIBLE);
            buttonCancelFriendRequest.setVisibility(View.GONE);
            buttonSendFriendRequest.setText("Send Friend Request");
        } else {
            buttonCancelFriendRequest.setVisibility(View.GONE);
            buttonSendFriendRequest.setVisibility(View.VISIBLE);
            buttonSendFriendRequest.setText("Delete Friends");
        }

        //
    } else {
        buttonSendFriendRequest.setVisibility(View.GONE);
        buttonCancelFriendRequest.setVisibility(View.GONE);
    }
}

private void isState(final String otherUserId) {
    currentUserId = mUser.getUid();
    FirebaseDatabase.getInstance().getReference("friend_request")
            .child(currentUserId).child(otherUserId)

            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                        RequestModel requestModel = dataSnapshot.getValue(RequestModel.class);

                        if (requestModel != null) {
                            userRequestState = requestModel.getState();
                            uiStateCheck(requestModel.getState());
                        }
                    } else {
                        FirebaseDatabase.getInstance().getReference("contacts")
                                .child(currentUserId).child(otherUserUid)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists() && snapshot.hasChildren()) {
                                            FriendModel friendModel = snapshot.getValue(FriendModel.class);
                                            if (friendModel != null) {
                                                userRequestState = friendModel.getState();
                                                uiStateCheck(friendModel.getState());
                                            }
                                        } else {
                                            uiStateCheck(StateRequest.NEW.name());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        uiStateCheck(StateRequest.NEW.name());
                                    }
                                });

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    uiStateCheck(null);
                }
            });


}

private void initializeVariable() {
    imageViewOtherFriendProfile = findViewById(R.id.imageViewOtherFriendProfile);
    textViewUserNameOtherPeople = findViewById(R.id.textViewUserNameOtherPeople);
    progressBarOtherProfile = findViewById(R.id.progressBarOtherProfile);
    buttonSendFriendRequest = findViewById(R.id.buttonSendFriendRequest);
    buttonCancelFriendRequest = findViewById(R.id.buttonCancelFriendRequest);
    databaseReference = FirebaseDatabase.getInstance().getReference("friend_request");
    buttonSendFriendRequest.setOnClickListener(this);
    buttonCancelFriendRequest.setOnClickListener(this);
}

@Override
public void onClick(View view) {
    switch (view.getId()) {
        case R.id.buttonSendFriendRequest:
            checkButtonState();
            break;
        case R.id.buttonCancelFriendRequest:
            deleteFriendRequest(userRequestState);
            break;
    }
}

@SuppressLint("SetTextI18n")
private void checkButtonState() {

    if (userRequestState.equals(StateRequest.NEW.name())) {

        if (mUser != null) {
            RequestModel requestSenderModel = new RequestModel(currentUserId, otherUserUid, StateRequest.SENDER.name());
            DatabaseReference senderCollectionRef = databaseReference.child(mUser.getUid()).child(otherUserUid);
            final DatabaseReference receiverCollectionRef = databaseReference.child(otherUserUid).child(mUser.getUid());
            senderCollectionRef.setValue(requestSenderModel).addOnSuccessListener(aVoid -> {
                RequestModel requestReceiverModel = new RequestModel(currentUserId, otherUserUid, StateRequest.RECEIVER.name());
                receiverCollectionRef.setValue(requestReceiverModel).addOnSuccessListener(ProfileActivity.this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        buttonSendFriendRequest.setText("Cancel Friend Request");
                        userRequestState = StateRequest.SENDER.name();
                    }
                });
            }).addOnFailureListener(e -> {
                e.printStackTrace();
                Log.e(TAG, "onFailure: ", e);
            });
        }
    } else if (userRequestState.equals(StateRequest.SENDER.name())) {
        if (mUser != null) {
            deleteFriendRequest(userRequestState);
        }
    } else if (userRequestState.equals(StateRequest.RECEIVER.name())) {
        if (mUser != null) {
            //butttonlar deyismelidi
            deleteFriendRequest(userRequestState);
            progressBarOtherProfile.setVisibility(View.VISIBLE);
            buttonCancelFriendRequest.setVisibility(View.VISIBLE);
            DatabaseReference contactSender = FirebaseDatabase.getInstance().getReference("contacts")
                                                      .child(currentUserId)
                                                      .child(otherUserUid);
            final DatabaseReference contactReceiver = FirebaseDatabase.getInstance().getReference("contacts")
                                                              .child(otherUserUid)
                                                              .child(currentUserId);
            FriendModel senderFriendModel =
                    new FriendModel(otherUserModel.getNameSurname(), otherUserModel.getUid(), StateRequest.FRIENDS.name());
            contactSender.setValue(senderFriendModel)
                    .addOnSuccessListener(ProfileActivity.this, aVoid -> {
                        FriendModel receiverFriendModel = new FriendModel(eventBusUserModel.getNameSurname(), eventBusUserModel.getUid(), StateRequest.FRIENDS.name());
                        contactReceiver.setValue(receiverFriendModel).addOnSuccessListener(ProfileActivity.this, aVoid1 -> {
                            userRequestState = StateRequest.FRIENDS.name();
                            uiStateCheck(userRequestState);
                        });

                    }).addOnFailureListener(e -> {
                e.printStackTrace();
                progressBarOtherProfile.setVisibility(View.GONE);
            });
        }
    } else {
        if (mUser != null) {
            deleteFriendRequest(userRequestState);
        }
    }
}

private void deleteFriendRequest(String userRequestState) {
    if (userRequestState.equals(StateRequest.FRIENDS.name())) {
        DatabaseReference contactSender = FirebaseDatabase.getInstance().getReference("contacts")
                                                  .child(currentUserId)
                                                  .child(otherUserUid);
        final DatabaseReference contactReceiver = FirebaseDatabase.getInstance().getReference("contacts")
                                                          .child(otherUserUid)
                                                          .child(currentUserId);
        contactSender.removeValue().addOnSuccessListener(ProfileActivity.this, aVoid -> contactReceiver.removeValue().addOnSuccessListener(ProfileActivity.this, new OnSuccessListener<Void>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(Void aVoid) {
                buttonSendFriendRequest.setText("Send Friend Request");
                ProfileActivity.this.userRequestState = StateRequest.NEW.name();
                progressBarOtherProfile.setVisibility(View.GONE);
            }
        })).addOnFailureListener(e -> {
            e.printStackTrace();
            progressBarOtherProfile.setVisibility(View.GONE);
        });
    } else {
        DatabaseReference senderCollectionRef = databaseReference.child(mUser.getUid()).child(otherUserUid);
        final DatabaseReference receiverCollectionRef = databaseReference.child(otherUserUid).child(mUser.getUid());
        senderCollectionRef.removeValue().addOnSuccessListener(ProfileActivity.this, aVoid -> receiverCollectionRef.removeValue().addOnSuccessListener(ProfileActivity.this, new OnSuccessListener<Void>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(Void aVoid) {
                buttonSendFriendRequest.setText("Send Friend Request");
                ProfileActivity.this.userRequestState = StateRequest.NEW.name();
                progressBarOtherProfile.setVisibility(View.GONE);
            }
        })).addOnFailureListener(e -> {
            e.printStackTrace();
            progressBarOtherProfile.setVisibility(View.GONE);
        });
    }

}

@Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
public void onMessageEvent(MessageEvent.MessageShareEvent event) {
    eventBusUserModel = event.getUserModel();
    Log.d("MessageEvent", "MessageEvent: " + eventBusUserModel);
}


@Override
protected void onStart() {
    super.onStart();
    EventBus.getDefault().register(this);
}

@Override
protected void onStop() {
    super.onStop();
    EventBus.getDefault().unregister(this);
}
}