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
import com.aslanovaslan.skypeclone.RegisterModel;
import com.aslanovaslan.skypeclone.ui.notifications.NotificationActivity;
import com.aslanovaslan.skypeclone.util.BottomNavigationHelper;
import com.aslanovaslan.skypeclone.util.glide.GlideApp;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class Settings extends AppCompatActivity implements View.OnClickListener {
private BottomNavigationView navView;
private ImageView imageViewUserProfile;
private Button buttonSettingSave;
private ProgressBar progressBarSettings;
private Uri imageUri = null;
private static int AA_SELECT_IMAGE = 101;
private EditText editTextTextPersonName, editTextTextPersonBio;
private BottomNavigationView.OnNavigationItemSelectedListener bottomNavigationHelper;

private StorageReference userProfileImagePath;
private DocumentReference userDocumentReference;
private FirebaseAuth mAuth;
private FirebaseUser mUser;
private Task<Uri> downloadUrl;
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
    userDocumentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
        @Override
        public void onSuccess(DocumentSnapshot documentSnapshot) {
            if (documentSnapshot.exists()) {
                RegisterModel registerModel = documentSnapshot.toObject(RegisterModel.class);
                assert registerModel != null;
                if (!registerModel.getNameSurname().equals(""))editTextTextPersonName.setHint(registerModel.getNameSurname() );
                if (!registerModel.getUserBio().equals(""))editTextTextPersonBio.setHint(registerModel.getUserBio() );
                if (!registerModel.getProfilePicturePath().equals("")) {
                   GlideApp.with(Settings.this)
                           .load(registerModel.getProfilePicturePath())
                           .placeholder(R.drawable.ic_baseline_person_pin_24)
                           .into(imageViewUserProfile);

                }
                progressBarState(View.GONE);
            }
        }
    }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            e.printStackTrace();
        }
    });
}

private void initializeVariable() {
    navView = findViewById(R.id.nav_view_settings);
    imageViewUserProfile = findViewById(R.id.imageViewUserProfile);
    editTextTextPersonName = findViewById(R.id.editTextTextPersonName);
    editTextTextPersonBio = findViewById(R.id.editTextTextPersonBio);
    progressBarSettings = findViewById(R.id.progressBarSettings);
    buttonSettingSave = findViewById(R.id.buttonSettingSave);
    userProfileImagePath = FirebaseStorage.getInstance().getReference().child("Images");
    mAuth = FirebaseAuth.getInstance();
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
            progressBarState(View.VISIBLE);
            break;
        case R.id.imageViewUserProfile:
            uploadImageProfile();
            break;
    }
}

private void checkInputs() {
    final String bioData = editTextTextPersonBio.getText().toString();
    final String nameData = editTextTextPersonName.getText().toString();

    if (Objects.equals(bioData, "")) {
        editTextTextPersonBio.setError("Bio text require");
        editTextTextPersonBio.requestFocus();
        progressBarState(View.GONE);
        Log.d(TAG, "checkInputs: bio melumati doldurulmuyub");
    } else if (Objects.equals(nameData, "")) {
        editTextTextPersonName.setError("Name is require");
        editTextTextPersonName.requestFocus();
        progressBarState(View.GONE);
        Log.d(TAG, "checkInputs: ad soyad doldurulmuyub");
    } else {
        if (selectedImageBytes == null) {
            Log.d(TAG, "checkInputs: sekil yoxdu ");
            userDocumentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    RegisterModel registerModel = documentSnapshot.toObject(RegisterModel.class);
                    if (registerModel != null) {
                        if (registerModel.getProfilePicturePath().equals("")) {
                            progressBarState(View.GONE);

                            Toast.makeText(Settings.this, "please select image first time", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onSuccess: sekil yoxdu");
                        } else {
                            Log.d(TAG, "onSuccess: " + registerModel.getProfilePicturePath());
                            HashMap<String, Object> updateUser = new HashMap<>();
                            updateUser.put("nameSurname", nameData);
                            updateUser.put("userBio", bioData);

                            updateUserToFirestore(updateUser);
                        }
                    }
                }
            });

        } else {

            uploadImageStorage(selectedImageBytes, bioData, nameData);
        }


    }
}

private void updateUserToFirestore(HashMap<String, Object> updateUser) {
    userDocumentReference.update(updateUser).addOnSuccessListener(new OnSuccessListener<Void>() {
        @Override
        public void onSuccess(Void aVoid) {
            Toast.makeText(Settings.this, "profile updated", Toast.LENGTH_SHORT).show();
            progressBarState(View.GONE);
        }
    }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            e.printStackTrace();
            Log.e(TAG, "onFailure: ", e);
            Toast.makeText(Settings.this, "profile not updated", Toast.LENGTH_SHORT).show();
            progressBarState(View.GONE);
        }
    });
}



private void progressBarState(int gone) {
    progressBarSettings.setVisibility(gone);
}

private void uploadImageStorage(final byte[] selectedImageBytes, final String bioData, final String nameData) {
    if (mUser != null) {
        if (selectedImageBytes != null) {
            UUID uuid = UUID.nameUUIDFromBytes(selectedImageBytes);
            final StorageReference storageReference = userProfileImagePath.child(("profile/" + uuid));

            storageReference.putBytes(selectedImageBytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    downloadUrl = storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.getResult() != null) {

                                HashMap<String, Object> updateUser = new HashMap<>();
                                updateUser.put("nameSurname", nameData);
                                updateUser.put("userBio", bioData);
                                updateUser.put("profilePicturePath", task.getResult());

                                updateUserToFirestore(updateUser);
                            }
                        }
                    });


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "onFailure: ", e);
                }
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
    startActivityForResult(intent, AA_SELECT_IMAGE);

}

@Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == AA_SELECT_IMAGE && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
        imageUri = data.getData();
        imageViewUserProfile.setImageURI(imageUri);

        Bitmap selectedImageBitmap;
        if (imageUri != null) {
            if (Build.VERSION.SDK_INT >= 28) {
                try {
                    ImageDecoder.Source sources = ImageDecoder.createSource(this.getContentResolver(), imageUri);
                    selectedImageBitmap = ImageDecoder.decodeBitmap(sources);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
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
                    selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                    selectedImageBytes = outputStream.toByteArray();
                    // uploadImageStorage(selectedImageBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
}