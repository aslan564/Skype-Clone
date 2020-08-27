package com.aslanovaslan.skypeclone.ui.notifications;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aslanovaslan.skypeclone.R;
import com.aslanovaslan.skypeclone.RegisterActivity;
import com.aslanovaslan.skypeclone.activity.ProfileActivity;
import com.aslanovaslan.skypeclone.model.FriendModel;
import com.aslanovaslan.skypeclone.model.RequestModel;
import com.aslanovaslan.skypeclone.model.UserModel;
import com.aslanovaslan.skypeclone.util.BottomNavigationHelper;
import com.aslanovaslan.skypeclone.util.glide.GlideApp;
import com.aslanovaslan.skypeclone.util.internal.MessageEvent;
import com.aslanovaslan.skypeclone.util.internal.StateRequest;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.aslanovaslan.skypeclone.activity.FindPeopleActivity.AA_SEND_VISIT_USER;

public class NotificationActivity extends AppCompatActivity {


private FirebaseAuth mAuth;
private FirebaseUser mUser;
private Query query;
private String currentUserId;
private BottomNavigationView navView;
private RecyclerView recyclerViewNotificationList;
private ProgressBar progressBarLoadNotificationData;
private FirebaseRecyclerAdapter<RequestModel, NotificationViewHolder> adapter;
private BottomNavigationView.OnNavigationItemSelectedListener bottomNavigationHelper;

private static final String TAG = "NotificationActivity";
private UserModel eventBusUserModel;
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_notification);


    initializeVariable();

    setBottomNav();


}

private void initializeVariable() {
    navView = findViewById(R.id.nav_view_notification);
    progressBarLoadNotificationData = findViewById(R.id.progressBarLoadNotificationData);
    recyclerViewNotificationList = findViewById(R.id.recyclerViewNotification);
    recyclerViewNotificationList.setHasFixedSize(true);
    recyclerViewNotificationList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    mAuth = FirebaseAuth.getInstance();
    mUser = mAuth.getCurrentUser();
    if (mUser == null) {
        checkUserState();
    }
    currentUserId = mUser.getUid();
    query = FirebaseDatabase.getInstance().getReference("friend_request")
                    .child(currentUserId);
}

private void setBottomNav() {
    bottomNavigationHelper = BottomNavigationHelper.changeView(NotificationActivity.this);
    navView.setOnNavigationItemSelectedListener(bottomNavigationHelper);
    MenuItem menuItem = navView.getMenu().getItem(1);
    menuItem.setChecked(true);
}

private void checkUserState() {
    Intent intent = new Intent(NotificationActivity.this, RegisterActivity.class);
    startActivity(intent);
    finish();
}

@Override
protected void onStart() {
    super.onStart();
    EventBus.getDefault().register(this);

    FirebaseRecyclerOptions<RequestModel> options = new FirebaseRecyclerOptions.Builder<RequestModel>()
                                                            .setQuery(query, RequestModel.class).build();

    adapter = new FirebaseRecyclerAdapter<RequestModel, NotificationViewHolder>(options) {

        @NonNull
        @Override
        public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.notification_item_view, parent, false);
            return new NotificationViewHolder(view);
        }

        @Override
        protected void onBindViewHolder(@NonNull NotificationViewHolder holder, int position, @NonNull RequestModel requestModel) {
            String keyUserId = getRef(position).getKey();
            setUserData(holder, requestModel, keyUserId);

        }
    };
    adapter.notifyDataSetChanged();
    recyclerViewNotificationList.setAdapter(adapter);
    adapter.startListening();
}

@Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
public void onMessageEvent(MessageEvent.MessageShareEvent event) {
    eventBusUserModel = event.getUserModel();
    Log.d("MessageEvent", "MessageEvent: " + eventBusUserModel);
}

@Override
protected void onStop() {
    super.onStop();
    adapter.stopListening();
    EventBus.getDefault().unregister(this);

}

private void setUserData(final NotificationViewHolder holder, RequestModel requestModel, final String keyUserId) {
    if (requestModel.getState().equals(StateRequest.RECEIVER.name())) {
        progressBarLoadNotificationData.setVisibility(View.VISIBLE);
        holder.cardViewNotification.setVisibility(View.VISIBLE);
        holder.progressBarNotificationView.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance().collection("users").document(keyUserId).get()
                .addOnSuccessListener(NotificationActivity.this, new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            final UserModel model = documentSnapshot.toObject(UserModel.class);
                            if (model != null) {
                                progressBarLoadNotificationData.setVisibility(View.GONE);

                                holder.userName.setText(model.getNameSurname());
                                GlideApp.with(holder.itemView.getContext()).load(model.getProfilePicturePath()).placeholder(R.drawable.ic_baseline_person_pin_24)
                                        .into(holder.userImage);
                                holder.userImage.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        intentProfileActivity(keyUserId);
                                    }
                                });
                                holder.accept.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        saveUserContacts(model,holder, keyUserId);
                                    }
                                });
                                holder.cancel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        holder.progressBarNotificationView.setVisibility(View.VISIBLE);
                                        deleteUserFriendRequest(holder, keyUserId, false);
                                    }
                                });
                                holder.progressBarNotificationView.setVisibility(View.GONE);

                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NotificationActivity.this, "tklif  yoxunuzdu ", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });

    } else {
        progressBarLoadNotificationData.setVisibility(View.GONE);
        holder.cardViewNotification.setVisibility(View.GONE);
        holder.progressBarNotificationView.setVisibility(View.GONE);
    }
}

private void intentProfileActivity(String keyUserId) {
    Intent intent = new Intent(NotificationActivity.this, ProfileActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

    // Toast.makeText(FindPeopleActivity.this, "path: "+path+" id: "+id, Toast.LENGTH_SHORT).show();
    intent.putExtra(AA_SEND_VISIT_USER, keyUserId);
    startActivity(intent);
}

private void deleteUserFriendRequest(final NotificationViewHolder holder, String keyUserId, final boolean state) {

   DatabaseReference senderCollectionRef= FirebaseDatabase.getInstance().getReference("friend_request").child(mUser.getUid()).child(keyUserId);
    final DatabaseReference receiverCollectionRef  =FirebaseDatabase.getInstance().getReference("friend_request").child(keyUserId).child(mUser.getUid());
    senderCollectionRef.removeValue().addOnSuccessListener(NotificationActivity.this, new OnSuccessListener<Void>() {
        @Override
        public void onSuccess(Void aVoid) {
            receiverCollectionRef.removeValue().addOnSuccessListener(NotificationActivity.this, new OnSuccessListener<Void>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onSuccess(Void aVoid) {
                    if (state) {
                        Log.d(TAG, "onSuccess: delete deleteUserFriendRequest");
                    }else {
                        holder.progressBarNotificationView.setVisibility(View.GONE);
                        Toast.makeText(NotificationActivity.this, "user deleted", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            e.printStackTrace();
            holder.progressBarNotificationView.setVisibility(View.GONE);
        }
    });


}

private void saveUserContacts(UserModel model, final NotificationViewHolder holder, String keyUserId) {
    deleteUserFriendRequest(holder,keyUserId,true);
    holder.progressBarNotificationView.setVisibility(View.VISIBLE);
    DatabaseReference contactSender = FirebaseDatabase.getInstance().getReference("contacts")
                                              .child(mUser.getUid())
                                              .child(keyUserId);
    final DatabaseReference contactReceiver = FirebaseDatabase.getInstance().getReference("contacts")
                                                      .child(keyUserId)
                                                      .child(mUser.getUid());
    FriendModel senderFriendModel =
            new FriendModel(model.getNameSurname(), model.getUid(), StateRequest.FRIENDS.name());
    contactSender.setValue(senderFriendModel)
            .addOnSuccessListener(NotificationActivity.this, new OnSuccessListener<Void>() {

                @Override
                public void onSuccess(Void aVoid) {
                    FriendModel receiverFriendModel = new FriendModel(eventBusUserModel.getNameSurname(), eventBusUserModel.getUid(), StateRequest.FRIENDS.name());
                    contactReceiver.setValue(receiverFriendModel).addOnSuccessListener(NotificationActivity.this, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(NotificationActivity.this,  "contact added ", Toast.LENGTH_SHORT).show();
                            holder.progressBarNotificationView.setVisibility(View.GONE);
                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            e.printStackTrace();
            holder.progressBarNotificationView.setVisibility(View.GONE);
        }
    });

}

public static class NotificationViewHolder extends RecyclerView.ViewHolder {

    TextView userName;
    ProgressBar progressBarNotificationView;
    ImageView accept, cancel;
    ImageView userImage;
    CardView cardViewNotification;

    public NotificationViewHolder(@NonNull View itemView) {
        super(itemView);
        userName = itemView.findViewById(R.id.textViewUserName);
        accept = itemView.findViewById(R.id.buttonAcceptFriendRequestNotification);
        cancel = itemView.findViewById(R.id.buttonCancelFriendRequestNotification);
        userImage = itemView.findViewById(R.id.imageViewNotification);
        cardViewNotification = itemView.findViewById(R.id.cardViewNotification);
        progressBarNotificationView = itemView.findViewById(R.id.progressBarNotficationView);

    }
}
}