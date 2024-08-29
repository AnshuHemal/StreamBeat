package com.white.streambeat.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.white.streambeat.Connections.ServerConnector;
import com.white.streambeat.LoadingDialog;
import com.white.streambeat.R;
import com.white.streambeat.databinding.ActivityLoginSignupBinding;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class LoginSignupActivity extends AppCompatActivity {
    ActivityLoginSignupBinding binding;
    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    String mVerificationId;
    PhoneAuthProvider.ForceResendingToken mResendToken;
    String phone;
    LoadingDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        binding = ActivityLoginSignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).hide();

        dialog = new LoadingDialog(this);

        auth = FirebaseAuth.getInstance();

        binding.btnContinue.setOnClickListener(v -> {
            if (binding.btnContinue.getText().equals("Continue")) {
                if (checkUtilsAreFillOrNot()) {
                    phone = binding.ccp.getSelectedCountryCodeWithPlus() + binding.phoneNumber.getText().toString();
                    startPhoneNumberVerification(phone);
                    showDialog();
                }
            } else {
                String otp = binding.etCode.getText().toString();
                PhoneAuthCredential credential;
                if (!TextUtils.isEmpty(binding.etCode.getText()) || !otp.contains(" ")) {
                    credential = PhoneAuthProvider.getCredential(mVerificationId, otp);
                    showDialog();
                    signInWithPhoneAuthCredential(credential);
                } else {
                    binding.etCode.setError("Enter Verification code..");
                    binding.etCode.requestFocus();
                }
            }
        });
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                hideDialog();
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                hideDialog();
                showSnackbar(e.getMessage());
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;
                binding.llOTP.setVisibility(View.VISIBLE);
                binding.ccp.setEnabled(false);
                binding.phoneNumber.setEnabled(false);
                binding.btnContinue.setText("Verify Code");
                hideDialog();
                showSnackbar("Code Sent to : " + phone);
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        firebaseUser = task.getResult().getUser();

                        StringRequest stringRequest = new StringRequest(
                                Request.Method.POST,
                                ServerConnector.LOGIN_URL,
                                response -> {
                                    if (response.equals("Success")){
                                        hideDialog();
                                        startActivity(new Intent(LoginSignupActivity.this, DashboardActivity.class));
                                        finish();
                                    } else {
                                        hideDialog();
                                        startActivity(new Intent(LoginSignupActivity.this, SignUpActivity.class));
                                        showSnackbar(phone + " is Verified..");
                                        finish();
                                    }
                                }, error -> showSnackbar(error.getMessage())
                        ) {
                            @Override
                            protected Map<String, String> getParams() {
                                HashMap<String, String> hm = new HashMap<>();
                                hm.put("key_phone", firebaseUser.getPhoneNumber());
                                return hm;
                            }
                        };
                        Volley.newRequestQueue(this).add(stringRequest);

                    } else {
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            showSnackbar(task.getException().getMessage());
                        }
                    }
                });
    }

    public boolean checkUtilsAreFillOrNot() {
        if (TextUtils.isEmpty(binding.phoneNumber.getText()) || binding.phoneNumber.getText().toString().contains(" ")) {
            binding.phoneNumber.setError("Field cannot be Empty..");
            binding.phoneNumber.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    public void startPhoneNumberVerification(String phone) {
        showSnackbar("Sending Code to : " + phone);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks
        );
    }

    public void showSnackbar(String message) {
        runOnUiThread(() -> {
            if (!isFinishing()) {
                Snackbar.make(findViewById(R.id.main), message, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void showDialog() {
        runOnUiThread(() -> {
            if (!isFinishing() && !dialog.isShowing()) {
                dialog.show();
            }
        });
    }

    private void hideDialog() {
        runOnUiThread(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        });
    }
}