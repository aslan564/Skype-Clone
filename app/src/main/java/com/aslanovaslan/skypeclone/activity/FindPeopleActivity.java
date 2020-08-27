package com.aslanovaslan.skypeclone.activity;

import android.content.Intent;
import android.os.Bundle;
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
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.aslanovaslan.skypeclone.R;
import com.aslanovaslan.skypeclone.model.UserModel;
import com.aslanovaslan.skypeclone.util.glide.GlideApp;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class FindPeopleActivity extends AppCompatActivity implements TextWatcher{

private RecyclerView recyclerViewFindFriendList;
private EditText editTextFindFriend;
private Toolbar toolbar;
private Query query;
private String str = "";
public static String AA_SEND_VISIT_USER = "AA_SEND_VISIT_USER";
private static final String TAG = "FindPeopleActivity";
private FindPeopleViewHolder adapter;
private FirestoreRecyclerOptions<UserModel> response;
private FirebaseAuth mAuth;
private FirebaseUser mUser;

@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_find_people);
    initializeVariable();
    setActionBar(toolbar);
    if (getSupportActionBar() != null)
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    recyclerViewFindFriendList.setHasFixedSize(true);

    RecyclerView.LayoutManager mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

    recyclerViewFindFriendList.setLayoutManager(mLayoutManager);
    recyclerViewFindFriendList.setItemAnimator(new DefaultItemAnimator());
    callFireStoreAdapter();


}

@Override
protected void onStart() {
    super.onStart();
    adapter.startListening();
}

private void callFireStoreAdapter() {
   /* response = new FirestoreRecyclerOptions.Builder<RegisterModel>()
                       .setQuery(query, RegisterModel.class)
                       .build();*/
    if (str.equals("")) {
        response = new FirestoreRecyclerOptions.Builder<UserModel>()
                           .setQuery(query, UserModel.class).build();
    } else {
        response = new FirestoreRecyclerOptions.Builder<UserModel>()
                           .setQuery(query.orderBy("nameSurname").startAt(str).endAt(str + "\uf8ff"), UserModel.class).build();
    }

    adapter = new FindPeopleViewHolder(response);
    adapter.setOnItemClickListener((documentSnapshot, position) -> {
        Intent intent = new Intent(FindPeopleActivity.this, ProfileActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        String id = documentSnapshot.getId();
        String path= documentSnapshot.getReference().getPath();
        intent.putExtra(AA_SEND_VISIT_USER, id);
        startActivity(intent);
    });
    adapter.notifyDataSetChanged();
    recyclerViewFindFriendList.setAdapter(adapter);
}

@Override
protected void onStop() {
    super.onStop();
    adapter.stopListening();
}

private void initializeVariable() {
    editTextFindFriend = findViewById(R.id.editTextFindFriend);
    editTextFindFriend.addTextChangedListener(this);
    recyclerViewFindFriendList = findViewById(R.id.recyclerViewFindFriendList);
    mAuth = FirebaseAuth.getInstance();
    mUser = mAuth.getCurrentUser();
    toolbar = findViewById(R.id.toolbar);
    query = FirebaseFirestore.getInstance().collection("users");
}

@Override
public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

}

@Override
public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    if (charSequence.length() == 0) {
        str = charSequence.toString();
        editTextFindFriend.setHint("please search contact here");
        callFireStoreAdapter();
        onStart();
    } else {
        str = charSequence.toString();
        callFireStoreAdapter();
        onStart();
        Log.d(TAG, "onTextChanged: " + str);
    }
}

@Override
public void afterTextChanged(Editable editable) {

}


}


class FindPeopleViewHolder extends FirestoreRecyclerAdapter<UserModel, FindPeopleViewHolder.FindPeopleHolder> {

    private OnItemClickListener listener;

    public FindPeopleViewHolder(@NonNull FirestoreRecyclerOptions<UserModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull final FindPeopleHolder findPeopleHolder, final int position, @NonNull UserModel userModel) {
        findPeopleHolder.userName.setText(userModel.getNameSurname());
        GlideApp.with(findPeopleHolder.itemView.getContext())
                .load(userModel.getProfilePicturePath())
                .placeholder(R.drawable.ic_baseline_person_pin_24)
                .into(findPeopleHolder.userImage);
        /*findPeopleHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(findPeopleHolder.itemView.getContext(), ProfileActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                UserModel userModel1 = new UserModel(
                        getItem(position).getNameSurname(),
                        getItem(position).getUserBio(),
                        getItem(position).getProfilePicturePath(),
                        getItem(position).getUid(),
                        getItem(position).getStatus());
                intent.putExtra(AA_SEND_VISIT_USER, userModel1);
                findPeopleHolder.itemView.getContext().startActivity(intent);
            }
        });*/
    }

    @NonNull
    @Override
    public FindPeopleHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.find_friend_item, parent, false);

        return new FindPeopleHolder(view);
    }

    class FindPeopleHolder extends RecyclerView.ViewHolder {
        TextView userName;
        Button videoCall;
        ImageView userImage;
        ConstraintLayout cardViewNotification;

        public FindPeopleHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.textViewFriend);
            videoCall = itemView.findViewById(R.id.buttonCallFriend);
            userImage = itemView.findViewById(R.id.imageViewFriend);
            cardViewNotification = itemView.findViewById(R.id.cardViewFriend);
            videoCall.setVisibility(View.GONE);
            itemView.setOnClickListener(view -> {

                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getSnapshots().getSnapshot(position), position);
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}

