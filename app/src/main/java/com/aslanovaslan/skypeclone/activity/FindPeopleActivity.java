package com.aslanovaslan.skypeclone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.aslanovaslan.skypeclone.R;
import com.aslanovaslan.skypeclone.RegisterModel;
import com.aslanovaslan.skypeclone.util.glide.GlideApp;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class FindPeopleActivity extends AppCompatActivity implements TextWatcher {

private RecyclerView recyclerViewFindFriendList;
private EditText editTextFindFriend;
private Toolbar toolbar;
private Query query;
private String str = "";
private static String AA_SEND_VISIT_USER = "AA_SEND_VISIT_USER";
private static final String TAG = "FindPeopleActivity";

@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_find_people);
    initializeVariable();
    setActionBar(toolbar);
    if (getSupportActionBar() != null)
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    recyclerViewFindFriendList.setHasFixedSize(true);
    recyclerViewFindFriendList.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL));
}

@Override
protected void onStart() {
    super.onStart();


    FirestoreRecyclerOptions<RegisterModel> options = null;
    if (str.equals("")) {
        options = new FirestoreRecyclerOptions.Builder<RegisterModel>()
                          .setQuery(query, RegisterModel.class).build();
    } else {
        options = new FirestoreRecyclerOptions.Builder<RegisterModel>()
                          .setQuery(query.orderBy("nameSurname").startAt(str).endAt(str + "\uf8ff"), RegisterModel.class).build();
    }

    FirestoreRecyclerAdapter<RegisterModel, FindPeopleViewHolder> firestoreRecyclerAdapter =
            new FirestoreRecyclerAdapter<RegisterModel, FindPeopleViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull FindPeopleViewHolder holder, final int position, @NonNull final RegisterModel registerModel) {
                    holder.userName.setText(registerModel.getNameSurname());
                    GlideApp.with(FindPeopleActivity.this).load(registerModel.getProfilePicturePath()).placeholder(R.drawable.ic_baseline_person_pin_24)
                            .into(holder.userImage);
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(FindPeopleActivity.this, ProfileActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            intent.putExtra(AA_SEND_VISIT_USER, (Parcelable) getItem(position));
                            startActivity(intent);
                        }
                    });
                }

                @NonNull
                @Override
                public FindPeopleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.find_friend_item, parent);
                    return new FindPeopleViewHolder(view);
                }

            };
    recyclerViewFindFriendList.setAdapter(firestoreRecyclerAdapter);
}

private void initializeVariable() {
    editTextFindFriend = findViewById(R.id.editTextFindFriend);
    editTextFindFriend.addTextChangedListener(this);
    recyclerViewFindFriendList = findViewById(R.id.recyclerViewFindFriendList);
    toolbar = findViewById(R.id.toolbar);
    query = FirebaseFirestore.getInstance().collection("users");
}

@Override
public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

}

@Override
public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    if (charSequence.length() == 0) {
        editTextFindFriend.setHint("please search contact here");
    } else {
        str = charSequence.toString();
        Log.d(TAG, "onTextChanged: " + str);
        onStart();
    }
}

@Override
public void afterTextChanged(Editable editable) {

}


public static class FindPeopleViewHolder extends RecyclerView.ViewHolder {

    TextView userName;
    Button videoCall;
    ImageView userImage;
    ConstraintLayout cardViewNotification;

    public FindPeopleViewHolder(@NonNull View itemView) {
        super(itemView);
        userName = itemView.findViewById(R.id.textViewFriend);
        videoCall = itemView.findViewById(R.id.buttonCallFriend);
        userImage = itemView.findViewById(R.id.imageViewFriend);
        cardViewNotification = itemView.findViewById(R.id.cardViewFriend);
        videoCall.setVisibility(View.GONE);
    }
}
}
