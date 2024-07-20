package com.white.streambeat.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.white.streambeat.Connections.ServerConnector;
import com.white.streambeat.R;

import java.util.HashMap;
import java.util.Map;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        getSupportActionBar().hide();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser == null) {
            new Handler().postDelayed(() -> {
                startActivity(new Intent(getApplicationContext(), LoginSignupActivity.class));
                finish();
            }, 500);
        } else {
            checkLoginStatus(firebaseUser.getPhoneNumber());
        }
    }

    private void checkLoginStatus(String phoneNumber) {
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                ServerConnector.CHECK_LOGIN_STATUS,
                response -> {
                    if (response.equals("Success")) {
                        new Handler().postDelayed(() -> {
                            startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                            finish();
                        }, 50);
                    } else {
                        new Handler().postDelayed(() -> {
                            startActivity(new Intent(getApplicationContext(), LoginSignupActivity.class));
                            finish();
                        }, 50);
                    }
                }, error -> Toast.makeText(SplashActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> params = new HashMap<>();
                params.put("key_phone", phoneNumber);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);
    }
}