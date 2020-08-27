package com.aslanovaslan.skypeclone.ui.settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.aslanovaslan.skypeclone.R;
import com.aslanovaslan.skypeclone.RegisterActivity;
import com.aslanovaslan.skypeclone.model.UserModel;
import com.aslanovaslan.skypeclone.util.BottomNavigationHelper;
import com.aslanovaslan.skypeclone.util.glide.GlideApp;
import com.aslanovaslan.skypeclone.util.internal.MessageEvent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class Settings extends AppCompatActivity implements View.OnClickListener {
private BottomNavigationView navView;
private ImageView imageViewUserProfile;
private Button buttonSettingSave;
private ProgressBar progressBarSettings,progressBarSettingsImage;
private Uri imageUri = null;
private static int AA_SELECT_IMAGE = 101;
private EditText editTextTextPersonName, editTextTextPersonBio;
private BottomNavigationView.OnNavigationItemSelectedListener bottomNavigationHelper;

private StorageReference userProfileImagePath;
private DocumentReference userDocumentReference;
private FirebaseUser mUser;
private byte[] selectedImageBytes = null;

private static final String TAG = "Settingsqaqaqa";


@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);
    //first Initialize Variable
    initializeVariable();
    //second other process
    buttonSettingSave.setOnClickListener(this);
    imageViewUserProfile.setOnClickListener(this);



    if (mUser != null) {
        progressBarState(View.VISIBLE);

        getCurrentUserData();
    }else {
        checkUserState();
    }

    setBottomNav();
}
private void checkUserState( ) {
    Intent intent = new Intent(Settings.this, RegisterActivity.class);
    startActivity(intent);
    finish();
}
private void getCurrentUserData() {
    userDocumentReference
            .get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    UserModel userModel = documentSnapshot.toObject(UserModel.class);
                    assert userModel != null;
                    if (!userModel.getNameSurname().equals(""))editTextTextPersonName.setHint(userModel.getNameSurname() );
                    if (!userModel.getUserBio().equals(""))editTextTextPersonBio.setHint(userModel.getUserBio() );
                    if (!userModel.getProfilePicturePath().equals("")) {
                       GlideApp.with(Settings.this)
                               .load(userModel.getProfilePicturePath())
                               .placeholder(R.drawable.ic_baseline_person_pin_24)
                               .into(imageViewUserProfile);
                        progressBarSettingsImage.setVisibility(View.GONE);

                    }

                    progressBarState(View.GONE);

                }
            }).addOnFailureListener(e -> e.printStackTrace());
}

private void initializeVariable() {
    navView = findViewById(R.id.nav_view_settings);
    imageViewUserProfile = findViewById(R.id.imageViewUserProfile);
    editTextTextPersonName = findViewById(R.id.editTextTextPersonName);
    editTextTextPersonBio = findViewById(R.id.editTextTextPersonBio);
    progressBarSettings = findViewById(R.id.progressBarSettings);
    progressBarSettingsImage = findViewById(R.id.progressBarSettingsImage);
    buttonSettingSave = findViewById(R.id.buttonSettingSave);
    userProfileImagePath = FirebaseStorage.getInstance().getReference().child("Images");
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    mUser = mAuth.getCurrentUser();
    if (mUser != null) {
        userDocumentReference = FirebaseFirestore.getInstance().collection("users").document(mUser.getUid());
    } else {
        Log.e(TAG, "initializeVariable: user null geldi");
    }
}


private void setBottomNav() {
    bottomNavigationHelper = BottomNavigationHelper.changeView(Settings.this);
    navView.setOnNavigationItemSelectedListener(bottomNavigationHelper);
    MenuItem menuItem = navView.getMenu().getItem(2);
    menuItem.setChecked(true);
}

@Override
public void onClick(View view) {
    switch (view.getId()) {
        case R.id.buttonSettingSave:
            checkInputs();
            break;
        case R.id.imageViewUserProfile:
            uploadImageProfile();
            break;
    }
}

private void checkInputs() {
    progressBarState(View.VISIBLE);
    final String bioData = editTextTextPersonBio.getText().toString();
    final String nameData = editTextTextPersonName.getText().toString();

    if (Objects.equals(bioData, "")) {
        progressBarState(View.GONE);
        editTextTextPersonBio.setError("Bio text require");
        editTextTextPersonBio.requestFocus();
        Log.d(TAG, "checkInputs: bio melumati doldurulmuyub");
    } else if (Objects.equals(nameData, "")) {
        progressBarState(View.GONE);
        editTextTextPersonName.setError("Name is require");
        editTextTextPersonName.requestFocus();
        Log.d(TAG, "checkInputs: ad soyad doldurulmuyub");
    } else {
        if (selectedImageBytes == null) {
            Log.d(TAG, "checkInputs: sekil secilmeyib ");
            userDocumentReference
                    .get().addOnSuccessListener(documentSnapshot -> {
                        UserModel userModel = documentSnapshot.toObject(UserModel.class);
                        if (userModel != null) {
                            if (userModel.getProfilePicturePath().equals("")) {
                                progressBarState(View.GONE);

                                Toast.makeText(Settings.this, "please select image first time", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "onSuccess: sekil yoxdu");
                            } else {
                                Log.d(TAG, "onSuccess: " + userModel.getProfilePicturePath());
                                HashMap<String, Object> updateUser = new HashMap<>();
                                updateUser.put("nameSurname", nameData);
                                updateUser.put("userBio", bioData);
                                updateUser.put("uid", mUser.getUid());

                                updateUserToFirestore(updateUser);
                            }
                        }
                    });

        } else {

            uploadImageStorage(selectedImageBytes, bioData, nameData);
        }


    }
}

private void updateUserToFirestore(HashMap<String, Object> updateUser) {
    String uid=mUser.getUid();
    DocumentReference reference = FirebaseFirestore.getInstance().collection("users").document(uid);
    reference.update(updateUser).addOnSuccessListener(aVoid -> {
        Toast.makeText(Settings.this, "profile updated", Toast.LENGTH_SHORT).show();
        progressBarState(View.GONE);
    }).addOnFailureListener(e -> {
        e.printStackTrace();
        Log.e(TAG, "onFailure: ", e);
        Toast.makeText(Settings.this, "profile not updated", Toast.LENGTH_SHORT).show();
        progressBarState(View.GONE);
    });
}



private void progressBarState(int state) {
    progressBarSettings.setVisibility(state);
}

private void uploadImageStorage(final byte[] selectedImageBytes, final String bioData, final String nameData) {
    if (mUser != null) {
        if (selectedImageBytes != null) {
            UUID uuid = UUID.nameUUIDFromBytes(selectedImageBytes);
            final StorageReference storageReference = userProfileImagePath.child(("profile/" + uuid));

            storageReference.putBytes(selectedImageBytes).addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.getResult() != null) {

                        HashMap<String, Object> updateUser = new HashMap<>();
                        updateUser.put("nameSurname", nameData);
                        updateUser.put("userBio", bioData);
                        updateUser.put("profilePicturePath", task.getResult().toString());
                        updateUser.put("uid", mUser.getUid());
                        updateUserToFirestore(updateUser);
                    }
                }
            })).addOnFailureListener(e -> {
                e.printStackTrace();
                Log.e(TAG, "onFailure: ", e);
            });


        }
    }

}

/* final UploadTask uploadTask = storageReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    } else {

                    }


                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        // downloadUrl = task.getResult().toString();
                        Log.d(TAG, "onComplete: " + task.getResult());
                        // addUserToFirestore()
                    }
                }
            });*/
private void uploadImageProfile() {

    Intent intent = new Intent().addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
    intent.setAction(Intent.ACTION_GET_CONTENT);
    intent.setType("image/*");
    intent.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"image/jpeg", "image/png"});
    startActivityForResult(intent, AA_SELECT_IMAGE);

}

@Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == AA_SELECT_IMAGE && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
        imageUri = data.getData();

        Bitmap selectedImageBitmap;
        if (imageUri != null) {
            if (Build.VERSION.SDK_INT >= 28) {
                try {
                    ImageDecoder.Source sources = ImageDecoder.createSource(this.getContentResolver(), imageUri);
                    selectedImageBitmap = ImageDecoder.decodeBitmap(sources);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
                    selectedImageBytes = outputStream.toByteArray();
                    // uploadImageStorage(selectedImageBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                try {
                    selectedImageBitmap =
                            MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
                    selectedImageBytes = outputStream.toByteArray();
                    // uploadImageStorage(selectedImageBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            GlideApp.with(Settings.this)
                    .load(selectedImageBytes)
                    .into(imageViewUserProfile);
        }
    }
}
}