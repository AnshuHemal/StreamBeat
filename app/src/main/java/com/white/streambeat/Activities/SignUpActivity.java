package com.white.streambeat.Activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.DatePicker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.white.streambeat.Connections.ServerConnector;
import com.white.streambeat.LoadingDialog;
import com.white.streambeat.R;
import com.white.streambeat.databinding.ActivitySignUpBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    ActivitySignUpBinding binding;
    FirebaseUser firebaseUser;
    DatePickerDialog.OnDateSetListener dateSetListener;
    LoadingDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).hide();

        dialog = new LoadingDialog(this);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        binding.etBirthDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(SignUpActivity.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                    dateSetListener, year, month, day);
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        });

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String date = dayOfMonth + "/" + month + "/" + year;
                binding.etBirthDate.setText(date);
            }
        };

        binding.agreeAndContinueBtn.setOnClickListener(v -> {
            if (checkUtilsAreFillOrNot()) {
                dialog.show();
                StringRequest stringRequest = new StringRequest(
                        Request.Method.POST,
                        ServerConnector.REGISTER_URL,
                        response -> {
                            if (response.equals("Success")) {
                                dialog.dismiss();
                                startActivity(new Intent(getApplicationContext(), SetupArtistsActivity.class));
                                finish();
                            } else {
                                dialog.dismiss();
                                showSnackbar(response);
                            }
                        }, error -> {
                    dialog.dismiss();
                    showSnackbar(error.getMessage());
                }
                ) {
                    @Override
                    protected Map<String, String> getParams() {
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("key_fname", binding.etFirstName.getText().toString());
                        hashMap.put("key_lname", binding.etLastName.getText().toString());
                        hashMap.put("key_email", binding.etEmailAddress.getText().toString());
                        hashMap.put("key_password", binding.etPassword.getText().toString());
                        hashMap.put("key_phone", firebaseUser.getPhoneNumber());

                        return hashMap;
                    }
                };
                Volley.newRequestQueue(getApplicationContext()).add(stringRequest);
            }
        });

    }

    public boolean checkUtilsAreFillOrNot() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date dateOfBirth = null;
        String birthDateText = binding.etBirthDate.getText().toString().trim();

        try {
            if (!TextUtils.isEmpty(birthDateText)) {
                dateOfBirth = sdf.parse(birthDateText);
            }
        } catch (ParseException e) {
            showSnackbar(e.getMessage());
            binding.etBirthDate.setError("Error parsing date.");
        }

        Calendar today = Calendar.getInstance();

        if (dateOfBirth != null) {
            Calendar dob = Calendar.getInstance();
            dob.setTime(dateOfBirth);

            int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }

            if (TextUtils.isEmpty(binding.etFirstName.getText()) || binding.etFirstName.getText().toString().contains(" ")) {
                binding.etFirstName.setError("Field cannot be Empty.");
                binding.etFirstName.requestFocus();
                return false;
            } else if (TextUtils.isEmpty(binding.etLastName.getText()) || binding.etLastName.getText().toString().contains(" ")) {
                binding.etLastName.setError("Field cannot be Empty.");
                binding.etLastName.requestFocus();
                return false;
            } else if (TextUtils.isEmpty(binding.etBirthDate.getText()) || binding.etBirthDate.getText().toString().contains(" ")) {
                binding.etBirthDate.setError("Select your Birth Date.");
                binding.etBirthDate.requestFocus();
                return false;
            } else if (age < 14) {
                showSnackbar("Sorry, you don't meet StreamBeat's age requirements.");
                return false;
            } else if (TextUtils.isEmpty(binding.etEmailAddress.getText()) || binding.etEmailAddress.getText().toString().contains(" ")) {
                binding.etEmailAddress.setError("Field cannot be Empty.");
                binding.etEmailAddress.requestFocus();
                return false;
            } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.etEmailAddress.getText().toString()).matches()) {
                binding.etEmailAddress.setError("Enter a valid Email Address.");
                binding.etEmailAddress.requestFocus();
                return false;
            } else if (TextUtils.isEmpty(binding.etPassword.getText()) || binding.etPassword.getText().toString().contains(" ")) {
                binding.etPassword.setError("Field cannot be Empty.");
                binding.etPassword.requestFocus();
                return false;
            } else if (binding.etPassword.getText().length() >= 8) {
                boolean isDigit = false;
                boolean isSymbol = false;
                boolean isUpperCase = false;
                char[] ch = binding.etPassword.getText().toString().toCharArray();
                for (char c : ch) {
                    if (Character.isDigit(c)) {
                        isDigit = true;
                    } else if (Character.isUpperCase(c)) {
                        isUpperCase = true;
                    } else if (!Character.isLetter(c)) {
                        isSymbol = true;
                    }
                }
                if (isDigit && isSymbol && isUpperCase) {
                    return true;
                } else {
                    showSnackbar("Password must contain at least one uppercase letter, one symbol, and one digit.");
                    return false;
                }
            } else {
                showSnackbar("Password length should be 8 or more characters.");
                return false;
            }
        } else {
            if (TextUtils.isEmpty(binding.etBirthDate.getText())) {
                binding.etBirthDate.setError("Select your Birth Date.");
                binding.etBirthDate.requestFocus();
            }
            return false;
        }
    }

    public void showSnackbar(String message) {
        Snackbar.make(findViewById(R.id.main),
                        message,
                        Snackbar.LENGTH_SHORT)
                .show();
    }


    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert!!")
                .setMessage("Do you want to Exit and close all process ?")
                .setCancelable(false)
                .setPositiveButton("YES", (dialog, which) -> {
                    finish();
                }).setNegativeButton("NO", (dialog, which) -> dialog.cancel()).show();
    }
}