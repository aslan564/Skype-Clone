package com.aslanovaslan.skypeclone.ui.home;

import android.app.Activity;
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
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.aslanovaslan.skypeclone.R;
import com.aslanovaslan.skypeclone.RegisterActivity;
import com.aslanovaslan.skypeclone.activity.CallingActivity;
import com.aslanovaslan.skypeclone.activity.FindPeopleActivity;
import com.aslanovaslan.skypeclone.activity.ProfileActivity;
import com.aslanovaslan.skypeclone.model.CallingModel;
import com.aslanovaslan.skypeclone.model.FriendModel;
import com.aslanovaslan.skypeclone.model.UserModel;
import com.aslanovaslan.skypeclone.util.BottomNavigationHelper;
import com.aslanovaslan.skypeclone.util.CallingStateHelper;
import com.aslanovaslan.skypeclone.util.glide.GlideApp;
import com.aslanovaslan.skypeclone.util.internal.MessageEvent;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

import org.greenrobot.eventbus.EventBus;

import java.util.Map;
import java.util.Objects;

import static com.aslanovaslan.skypeclone.RegisterActivity.USER;
import static com.aslanovaslan.skypeclone.activity.FindPeopleActivity.AA_SEND_VISIT_USER;

public class Contacts extends AppCompatActivity {


public static final String AA_RECEIVER_CALL_USER = "AA_RECEIVER_CALL_USER";
private Toolbar toolbar;
private ImageView imageViewContacts;
private FirebaseUser mUser;
private BottomNavigationView navView;
private ContactAdapter contactAdapter;
private DatabaseReference mReferenceCalling;
private static final String TAG = Contacts.class.getSimpleName();

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
    if (mUser == null) {
        checkUserState();
    }

    imageViewContacts.setOnClickListener(view -> {
        Intent intentFindPeople = new Intent(Contacts.this, FindPeopleActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intentFindPeople);
    });

    setBottomNav();


    contactAdapter.setOnItemClickListener((userKey, position) -> {

        Intent intent1 = new Intent(Contacts.this, ProfileActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent1.putExtra(AA_SEND_VISIT_USER, userKey);
        startActivity(intent1);
    });

}

private void initializeVariable() {
    navView = findViewById(R.id.nav_view_main);
    toolbar = findViewById(R.id.toolbar);
    ProgressBar progressBarContactLoading = findViewById(R.id.progressBarContactLoading);
    RecyclerView recyclerViewContactsList = findViewById(R.id.recyclerViewContactsList);
    imageViewContacts = findViewById(R.id.imageViewContacts);
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    mUser = mAuth.getCurrentUser();
    assert mUser != null;
    String currentUserId = mUser.getUid();
    recyclerViewContactsList.setHasFixedSize(true);
    recyclerViewContactsList.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
    DatabaseReference query = FirebaseDatabase.getInstance().getReference("contacts");

    FirebaseRecyclerOptions<FriendModel> firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<FriendModel>().setQuery(query.child(currentUserId), FriendModel.class).build();
    contactAdapter = new ContactAdapter(firebaseRecyclerOptions, Contacts.this, progressBarContactLoading);
    recyclerViewContactsList.setAdapter(contactAdapter);
    contactAdapter.notifyDataSetChanged();

    mReferenceCalling = FirebaseDatabase.getInstance().getReference("calling");
    FirebaseFirestore.getInstance().collection("users").document(currentUserId).update("channel", "");


}

@Override
protected void onStart() {
    super.onStart();
    contactAdapter.startListening();
    if (mUser != null) {
        String currentUserID = mUser.getUid();
        mReferenceCalling.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.hasChild(currentUserID) && snapshot.child(currentUserID).hasChild("caller")) {
                        String callingUser = Objects.requireNonNull(snapshot.child(currentUserID).child("caller").getValue()).toString();
                        if (!callingUser.equals("")) {
                            Intent intent = new Intent(Contacts.this, CallingActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            intent.putExtra(AA_RECEIVER_CALL_USER, callingUser);
                            startActivity(intent);
                            finish();
                        }
                    } else if (snapshot.hasChild(currentUserID) && snapshot.child(currentUserID).hasChild("state")) {
                        String callingState = Objects.requireNonNull(snapshot.child(currentUserID).child("state").getValue()).toString();
                        if (callingState.equals(CallingStateHelper.CLOSE.name())) {
                            mReferenceCalling.child(currentUserID).removeValue();
                            Log.i(TAG, "hele calling silindi : ");

                        }
                    }
                }else {
                    Log.i(TAG, "hele calling yaradilmiyib : ");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: ");
            }
        });
    }
}

@Override
protected void onStop() {
    super.onStop();
    contactAdapter.stopListening();
}

private void checkUserState() {
    Intent intent = new Intent(Contacts.this, RegisterActivity.class);
    startActivity(intent);
    finish();
}

@Override
public void onBackPressed() {
    super.onBackPressed();
    finish();
}


private void setBottomNav() {
    BottomNavigationView.OnNavigationItemSelectedListener bottomNavigationHelper = BottomNavigationHelper.changeView(Contacts.this);
    navView.setOnNavigationItemSelectedListener(bottomNavigationHelper);
    MenuItem menuItem = navView.getMenu().getItem(0);
    menuItem.setChecked(true);
}


}

class ContactAdapter extends FirebaseRecyclerAdapter<FriendModel, ContactAdapter.ContactViewHolder> {
private static final String AA_SEND_CALL_USER = "AA_SEND_CALL_USER";
public OnItemClickListener listener;
public Activity activity;
public ProgressBar progressBar;

public ContactAdapter(@NonNull FirebaseRecyclerOptions<FriendModel> options, Activity activity, ProgressBar progressBar) {
    super(options);
    this.activity = activity;
    this.progressBar = progressBar;
}

@Override
protected void onBindViewHolder(@NonNull ContactViewHolder holder, int position, @NonNull FriendModel friendModel) {
    final String userKeyId = getRef(position).getKey();
    progressBar.setVisibility(View.VISIBLE);
    setUserDataFromDatabase(holder, userKeyId, friendModel);
    holder.imageViewMakeCall.setOnClickListener(view -> intentCallingActivity(view, userKeyId));
}

private void intentCallingActivity(View view, String key) {
    Intent callActivityIntent = new Intent(activity, CallingActivity.class);
    callActivityIntent.putExtra(AA_SEND_CALL_USER, key);
    activity.startActivity(callActivityIntent);
    activity.finish();
}

private void setUserDataFromDatabase(final ContactViewHolder holder, String keyUserId, FriendModel friendModel) {
    FirebaseFirestore.getInstance().collection("users").document(keyUserId).get()
            .addOnSuccessListener(activity, documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    UserModel userModel = documentSnapshot.toObject(UserModel.class);
                    if (userModel != null) {
                        GlideApp.with(activity).load(userModel.getProfilePicturePath()).placeholder(R.drawable.ic_baseline_person_pin_24)
                                .into(holder.imageViewContactUserImage);
                        holder.textViewContactUserName.setText(userModel.getNameSurname());
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }).addOnFailureListener(e -> {
        Log.d("ContactAdapter", "onFailure: " + e);
        e.printStackTrace();
    });

}

@NonNull
@Override
public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_friend_item, parent, false);
    return new ContactViewHolder(view);
}

public class ContactViewHolder extends RecyclerView.ViewHolder {
    public TextView textViewContactUserName;
    public ImageView imageViewContactUserImage, imageViewMakeCall;

    public ContactViewHolder(@NonNull View itemView) {
        super(itemView);
        imageViewMakeCall = itemView.findViewById(R.id.imageViewMakeCall);
        textViewContactUserName = itemView.findViewById(R.id.textViewContactUserName);
        imageViewContactUserImage = itemView.findViewById(R.id.imageViewContactUserImage);

        itemView.setOnClickListener(view -> {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && listener != null) {
                listener.onItemClick(getRef(position).getKey(), position);
            }
        });
    }
}

public interface OnItemClickListener {
    void onItemClick(String userKey, int position);
}

public void setOnItemClickListener(OnItemClickListener listener) {
    this.listener = listener;
}
}