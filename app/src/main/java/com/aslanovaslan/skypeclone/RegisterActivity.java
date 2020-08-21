package com.aslanovaslan.skypeclone;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aslanovaslan.skypeclone.ui.home.Contacts;
import com.aslanovaslan.skypeclone.ui.settings.Settings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
private static final String TAG = "RegisterActivity";
private CountryCodePicker ccp;
private Button continueNext;
private EditText phoneText, codeText;
private String checker = "", phoneNumber = "";
private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
private FirebaseAuth mAuth;
private FirebaseUser mUser;
private String mVerificationId;
private PhoneAuthProvider.ForceResendingToken mResendToken;
private ProgressDialog progressBar;
private String comeSmsCodeInPhone;
private RegisterModel userRegister;
private LinearLayout linearLayout;
private DocumentReference userDocumentReference;
private PhoneAuthCredential credential;
public static final String USER = "USER";


@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_register);

    mAuth = FirebaseAuth.getInstance();
    mUser=mAuth.getCurrentUser();
    isUserHave();

    initializeVariable();

    mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull @org.jetbrains.annotations.NotNull PhoneAuthCredential phoneAuthCredential) {
            if (phoneAuthCredential.getSmsCode() != null) {
                comeSmsCodeInPhone = phoneAuthCredential.getSmsCode();
                credential = phoneAuthCredential;
            }

            //signInWithPhoneAuthCredential(phoneAuthCredential);
        }

        @Override
        public void onVerificationFailed(@NonNull @org.jetbrains.annotations.NotNull FirebaseException e) {
            progressBar.dismiss();
            linearLayout.setVisibility(View.VISIBLE);
            phoneText.setVisibility(View.VISIBLE);
            continueNext.setText("Continue");
            codeText.setVisibility(View.GONE);
            Log.d("onVerificationFailed", "onVerificationFailed: faild oldu" + e.getMessage());
            Toast.makeText(RegisterActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            //Toast.makeText(this, "tesdiq kodunu duzgun yazin ", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCodeSent(@NonNull @NotNull String s, @NonNull @NotNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            linearLayout.setVisibility(View.GONE);
            progressBar.dismiss();
            mVerificationId = s;
            mResendToken = forceResendingToken;

            checker = "Code Sent";
            continueNext.setText("Submit");
            codeText.setVisibility(View.VISIBLE);
        }
    };
}

@Override
protected void onStart() {
    super.onStart();
    isUserHave();
}

private void isUserHave() {
    if (mUser != null) {
        sendUserToMainActivity(mUser);
    }
}

private void initializeVariable() {
    ccp = findViewById(R.id.ccp);
    phoneText = findViewById(R.id.phoneText);
    codeText = findViewById(R.id.codeText);
    continueNext = findViewById(R.id.continueNextButton);
    linearLayout = findViewById(R.id.linearLayout);
    ccp.registerPhoneNumberTextView(phoneText);
    continueNext.setOnClickListener(this);
    progressBar = new ProgressDialog(this);
    mAuth = FirebaseAuth.getInstance();
    mUser = mAuth.getCurrentUser();
    if (mUser != null) {
        userDocumentReference = FirebaseFirestore.getInstance().collection("users").document(mUser.getUid());
    } else {
        Log.e(TAG, "initializeVariable: user null geldi");
    }
}


@Override
public void onClick(View view) {
    if (view.getId() == R.id.continueNextButton) {
        if (continueNext.getText().equals("Submit") || checker.equals("Code Sent")) {

            if (comeSmsCodeInPhone != null) {
                String verificationCode = codeText.getText().toString().trim();
                if (!verificationCode.equals("") && verificationCode.equals(comeSmsCodeInPhone)) {
                    progressBar.setTitle("Code Verification");
                    progressBar.setMessage("Please wait ,while we are verifying your code.");
                    progressBar.setCanceledOnTouchOutside(false);
                    progressBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                } else {

                    Toast.makeText(this, "tesdiq kodunu duzgun yazin ", Toast.LENGTH_SHORT).show();

                }
                Log.d(TAG, "onClick: " + continueNext.getText());

            } else {
                Toast.makeText(this, "zehmet olmasa bir daha tektar edin nomreniz tesdiq edilmedi ", Toast.LENGTH_SHORT).show();
            }

        } else {
            if (!phoneText.getText().toString().equals(""))
                phoneNumber = ccp.getFullNumberWithPlus();
            if (!phoneNumber.equals("")) {
                progressBar.setTitle("Phone Number Verification");
                progressBar.setMessage("Please wait ,while we are verifying your phone number");
                progressBar.setCanceledOnTouchOutside(false);
                progressBar.show();

                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        phoneNumber,        // Phone number to verify
                        60,                 // Timeout duration
                        TimeUnit.SECONDS,   // Unit of timeout
                        this,               // Activity (for callback binding)
                        mCallbacks);        // OnVerificationStateChangedCallbacks
            } else {
                phoneText.setError("please enter your number");
                phoneText.requestFocus();
                Toast.makeText(RegisterActivity.this, "zegmet olmasa nomreni qeyd edin ", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
    mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        progressBar.dismiss();


                        userRegister=new RegisterModel("Customer","Your Bios Here","");
                        addUserToFirestore(userRegister);
                        // ...
                    } else {
                        progressBar.dismiss();
                        Toast.makeText(RegisterActivity.this, Objects.requireNonNull(task.getException()).getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        // Sign in failed, display a message and update the UI
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                            Log.e(TAG, "onComplete: ", task.getException());
                        }
                    }
                }
            });
}
private void addUserToFirestore(RegisterModel user) {
    userDocumentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
        @Override
        public void onSuccess(Void aVoid) {
            Toast.makeText(RegisterActivity.this, "profile updated", Toast.LENGTH_SHORT).show();
            sendUserToMainActivity(mUser);
        }
    }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            e.printStackTrace();
            Log.e(TAG, "onFailure: ", e);
            Toast.makeText(RegisterActivity.this, "profile not updated", Toast.LENGTH_SHORT).show();
        }
    });
}


private void sendUserToMainActivity(FirebaseUser user) {
    Intent intent = new Intent(RegisterActivity.this, Contacts.class);
    intent.putExtra(USER, user);
    startActivity(intent);
    finish();
}
}