package com.aslanovaslan.skypeclone.ui.videocating;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.aslanovaslan.skypeclone.R;
import com.aslanovaslan.skypeclone.model.TalkingModel;
import com.aslanovaslan.skypeclone.model.UserModel;
import com.aslanovaslan.skypeclone.ui.home.Contacts;
import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoChatActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener {

private static String API_KEY = "46900184";
private static String SESSION_ID = "1_MX40NjkwMDE4NH5-MTU5ODU4MzM5MjA1Mn5lbmR0TFhYN1RpVzJRbjk0RHMxNVVzTHl-fg";//3f5c4ce3c16eb046cd05835f168a6a1b11cc47f3
private static String TOKEN = "T1==cGFydG5lcl9pZD00NjkwMDE4NCZzaWc9OTEyMzQ1ODMwYWRiZTBhYzk1MWE3MTZhZjhlMjNkY2RmYmMzZWM1ZjpzZXNzaW9uX2lkPTFfTVg0ME5qa3dNREU0Tkg1LU1UVTVPRFU0TXpNNU1qQTFNbjVsYm1SMFRGaFlOMVJwVnpKUmJqazBSSE14TlZWelRIbC1mZyZjcmVhdGVfdGltZT0xNTk4NTgzNTI2Jm5vbmNlPTAuODYzMzkzNDE4NjMzNzI0NCZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNjAxMjI1OTMwJmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9";
private static final String LOG_TAG = VideoChatActivity.class.getSimpleName();
private static final int RC_SETTINGS_SCREEN_PERM = 123;
private static final int RC_VIDEO_APP_PERM = 124;
private static String PERMISSIONS_TEXT = "Hello this app needs Mic and Camera permissions please allow!!";

private static final int AA_VIDEO_CHAT_REQUEST_COD = 5964;

private Session mSession;
private Publisher mPublisher;
private Subscriber mSubscriber;

private FrameLayout mPublisherFrameLayout, mSubscriberFrameLayout;
private TextView publisherFrameLayoutHeader;
private Button imageViewCancelVideoCalling;
private String userID = "";
private String userKey = "";
private FirebaseUser mUser;
private DatabaseReference mTalkingRef;
private DocumentReference documentReference;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_video_chat);
    userKey = getIntent().getStringExtra("aslan");

    initializeVariable();
    requestPermissions();
}

private void initializeVariable() {
    imageViewCancelVideoCalling = findViewById(R.id.imageViewCancelVideoCalling);
    mPublisherFrameLayout = findViewById(R.id.publisherFrameLayout);
    mSubscriberFrameLayout = findViewById(R.id.subscriberFrameLayout);
    publisherFrameLayoutHeader = findViewById(R.id.publisherFrameLayoutHeader);
    mUser = FirebaseAuth.getInstance().getCurrentUser();
    documentReference = FirebaseFirestore.getInstance().collection("users").document(mUser.getUid());
    mTalkingRef = FirebaseDatabase.getInstance().getReference("talking");
    imageViewCancelVideoCalling.setEnabled(true);
    if (mUser != null) {

        imageViewCancelVideoCalling.setOnClickListener(view -> {
            documentReference.get()
                    .addOnSuccessListener(VideoChatActivity.this, new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                UserModel userModel = documentSnapshot.toObject(UserModel.class);
                                if (userModel != null) {

                                    mTalkingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.hasChild(userModel.getChannel())) {
                                               // TalkingModel talkingModel = snapshot.child(userModel.getChannel()).getValue(TalkingModel.class);

                                                mTalkingRef.child(userModel.getChannel()).removeValue().addOnSuccessListener(VideoChatActivity.this, new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        disconnectSession();
                                                    }
                                                });

                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.d(LOG_TAG, "onCancelled: " + error.toException());
                                        }
                                    });
                                }
                            }
                        }
                    });


        });
    }

}

private void disconnectSession() {
    if (mSession == null) {
        return;
    }

    if (mSubscriber != null) {
        mSubscriberFrameLayout.removeView(mSubscriber.getView());
        mSession.unsubscribe(mSubscriber);
        mSubscriber.destroy();
        mSubscriber = null;
    }

    if (mPublisher != null) {
        mPublisherFrameLayout.removeView(mPublisher.getView());
        mSession.unpublish(mPublisher);
        mPublisher.destroy();
        mPublisher = null;
    }
    mSession.disconnect();
    startContactsActivity();
}

@Override
protected void onStart() {
    super.onStart();
    documentReference.addSnapshotListener(VideoChatActivity.this, new EventListener<DocumentSnapshot>() {
        @Override
        public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
            if (error != null) {
                Log.e(LOG_TAG, "onEvent: ", error);
            } else {
                if (value != null && value.getData() != null && value.exists()) {
                    UserModel model = value.toObject(UserModel.class);
                    if (model != null && !model.getChannel().equals("")) {
                        mTalkingRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.hasChild(model.getChannel())) {
                                    TalkingModel talkingModel = snapshot.child(model.getChannel()).getValue(TalkingModel.class);
                                    if (talkingModel != null) {
                                        Log.d(LOG_TAG, "onDataChange: danisilir");
                                    } else {
                                        disconnectSession();
                                    }

                                } else {
                                    documentReference.update("channel", "").addOnSuccessListener(VideoChatActivity.this,
                                            aVoid -> disconnectSession());
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d(LOG_TAG, "onCancelled: " + error.toException());
                            }
                        });
                    } else {
                        disconnectSession();
                    }
                }
            }
        }
    });


}

@Override
protected void onPause() {

    Log.d(LOG_TAG, "onPause");

    super.onPause();

    if (mSession != null) {
        mSession.onPause();
    }

}

@Override
protected void onResume() {

    Log.d(LOG_TAG, "onResume");

    super.onResume();

    if (mSession != null) {
        mSession.onResume();
    }
}

private void startContactsActivity() {
    Intent intentToContacts = new Intent(VideoChatActivity.this, Contacts.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
    startActivity(intentToContacts);
    finish();
}

@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, VideoChatActivity.this);
}

@AfterPermissionGranted(AA_VIDEO_CHAT_REQUEST_COD)
private void requestPermissions() {
    String[] permission = {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};
    if (EasyPermissions.hasPermissions(this, permission)) {
        mPublisherFrameLayout = findViewById(R.id.publisherFrameLayout);
        mSubscriberFrameLayout = findViewById(R.id.subscriberFrameLayout);
        mSession = new Session.Builder(VideoChatActivity.this, API_KEY, SESSION_ID).build();
        mSession.setSessionListener(VideoChatActivity.this);
        mSession.connect(TOKEN);
    } else {
        // AppSettingsDialog
        EasyPermissions.requestPermissions(this, PERMISSIONS_TEXT, AA_VIDEO_CHAT_REQUEST_COD, permission);
    }
}


@Override
public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

}

@Override
public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

}

@Override
public void onError(PublisherKit publisherKit, OpentokError opentokError) {

}

//2.ikinci  strimi publishere gondermek lazimdi
@Override
public void onConnected(Session session) {
    Log.i(LOG_TAG, "Session connected: ");

    mPublisher = new Publisher.Builder(this).build();

    mPublisher.setPublisherListener(VideoChatActivity.this);

    mPublisherFrameLayout.addView(mPublisher.getView());

    if (mPublisher.getView() instanceof GLSurfaceView) {
        ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
    }
    mSession.publish(mPublisher);
}


@Override
public void onDisconnected(Session session) {
    Log.i(LOG_TAG, "onDisconnected: " + session);
}

//3.subscribe To The Streams //yeni paylasilan strimlere qosulmaq
@Override
public void onStreamReceived(Session session, Stream stream) {
    Log.i(LOG_TAG, "Stream : catdirildi ** ");

    if (mSubscriber == null) {
        mSubscriber = new Subscriber.Builder(this, stream).build();
        mSubscriber.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
        mSession.subscribe(mSubscriber);
        mSubscriberFrameLayout.addView(mSubscriber.getView(), FrameLayout.LayoutParams.MATCH_PARENT);
        // mSubscriberFrameLayout.add
    }
}

@Override
public void onStreamDropped(Session session, Stream stream) {
    Log.i(LOG_TAG, "onStreamDropped: Dropped" + stream);
    if (mSubscriber != null) {
        mSubscriber = null;
        mSubscriberFrameLayout.removeAllViews();
    }
}

@Override
public void onError(Session session, OpentokError opentokError) {
    Log.i(LOG_TAG, "onError: Error" + opentokError);
}

@Override
public void onPointerCaptureChanged(boolean hasCapture) {

}
}