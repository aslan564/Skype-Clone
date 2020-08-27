package com.aslanovaslan.skypeclone.ui.videocating;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.aslanovaslan.skypeclone.R;
import com.aslanovaslan.skypeclone.ui.home.Contacts;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoChatActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener {

private static String API_KEY = "";
private static String SESSION_ID = "";
private static String TOKEN = "";
private static String PERMISSIONS_TEXT = "Hello this app needs Mic and Camera permissions please allow!!";

private static final String TAG = VideoChatActivity.class.getSimpleName();
private static final int AA_VIDEO_CHAT_REQUEST_COD = 5964;

private Session mSession;
private Publisher mPublisher;
private Subscriber mSubscriber;

private FrameLayout mPublisherFrameLayout, mSubscriberFrameLayout;
private TextView publisherFrameLayoutHeader;
private ImageView imageViewCancelVideoCalling;
private String userID = "";
private String userKey = "";
private FirebaseUser mUser;
private DatabaseReference mReference;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_video_chat);
    userKey = getIntent().getStringExtra("aslan");

    initializeVariable();

}

private void initializeVariable() {
    imageViewCancelVideoCalling = findViewById(R.id.imageViewCancelVideoCalling);
    mPublisherFrameLayout = findViewById(R.id.publisherFrameLayout);
    mSubscriberFrameLayout = findViewById(R.id.subscriberFrameLayout);
    publisherFrameLayoutHeader = findViewById(R.id.publisherFrameLayoutHeader);
    mUser = FirebaseAuth.getInstance().getCurrentUser();
    mReference = FirebaseDatabase.getInstance().getReference("calling");
    if (mUser != null) {
        userID = mUser.getUid();
    }
    imageViewCancelVideoCalling.setOnClickListener(view -> {
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("sender").hasChild(userID)) {
                    mReference.child("sender").removeValue();
                    if (mPublisher != null) {
                        mPublisher.destroy();
                    }
                    if (mSubscriber != null) {
                        mSubscriber.destroy();
                    }
                    startContactsActivity();
                }
                if (snapshot.child("receiver").hasChild(userID)) {
                    mReference.child("receiver").removeValue();
                    if (mPublisher != null) {
                        mPublisher.destroy();
                    }
                    if (mSubscriber != null) {
                        mSubscriber.destroy();
                    }

                    startContactsActivity();
                } else {

                    if (mPublisher != null) {
                        mPublisher.destroy();
                    }
                    if (mSubscriber != null) {
                        mSubscriber.destroy();
                    }
                    startContactsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: " + error.toException());
            }
        });

    });
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
    Log.i(TAG, "Session connected: ");

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
    Log.i(TAG, "onDisconnected: " + session);
}

//3.subscribe To The Streams //yeni paylasilan strimlere qosulmaq
@Override
public void onStreamReceived(Session session, Stream stream) {
    Log.i(TAG, "Stream : catdirildi ** ");

    if (mSubscriber == null) {
        mSubscriber = new Subscriber.Builder(this, stream).build();
        mSession.subscribe(mSubscriber);
        mSubscriberFrameLayout.addView(mSubscriber.getView());
    }
}

@Override
public void onStreamDropped(Session session, Stream stream) {
    Log.i(TAG, "onStreamDropped: Dropped" + stream);
    if (mSubscriber != null) {
        mSubscriber = null;
        mSubscriberFrameLayout.removeAllViews();
    }
}

@Override
public void onError(Session session, OpentokError opentokError) {
    Log.i(TAG, "onError: Error" + opentokError);
}

@Override
public void onPointerCaptureChanged(boolean hasCapture) {

}
}