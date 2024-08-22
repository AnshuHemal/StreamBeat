package com.white.streambeat.Fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.white.streambeat.Connections.ServerConnector;
import com.white.streambeat.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProfileFragment extends Fragment {

    LinearLayout llWhatsNew, llHistory, llSettings;
    TextView txtFullName, txtEmailAddress, profileInitials;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.small_push);

        llSettings = view.findViewById(R.id.llSettings);
        llHistory = view.findViewById(R.id.llListeningHistory);
        llWhatsNew = view.findViewById(R.id.llWhatsNew);
        txtFullName = view.findViewById(R.id.txtFullName);
        txtEmailAddress = view.findViewById(R.id.txtEmailAddress);
        profileInitials = view.findViewById(R.id.profileInitials);

        txtFullName.setText(ServerConnector.userFullName);
        profileInitials.setText(getInitials(ServerConnector.userFullName));
//        profileInitials.setBackgroundColor(getRandomColor());

        txtEmailAddress.setText(formatEmail(ServerConnector.userEmailAddress));

        llSettings.setOnClickListener(v -> {
            v.startAnimation(animation);

            new Handler().postDelayed(() -> {
                Fragment settingsFragment = new SettingsFragment();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.frameLayout, settingsFragment);
                transaction.commit();
            }, 100);
        });

        llHistory.setOnClickListener(v -> {
            v.startAnimation(animation);

            new Handler().postDelayed(() -> {
                Fragment historyFragment = new ListeningHistoryFragment();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.frameLayout, historyFragment);
                transaction.commit();
            }, 100);
        });

        llWhatsNew.setOnClickListener(v -> {
            v.startAnimation(animation);

            new Handler().postDelayed(() -> {
                @SuppressLint("QueryPermissionsNeeded") StringRequest stringRequest = new StringRequest(
                        Request.Method.POST,
                        ServerConnector.WRITE_LOGS_TO_FILE,
                        response -> {
                            try {
                                JSONObject jsonResponse = new JSONObject(response);
                                String content = jsonResponse.optString("content", "No data available");

                                File file = new File(requireContext().getExternalFilesDir(null), "userLogs.txt");
                                try (FileOutputStream fos = new FileOutputStream(file)) {
                                    fos.write(content.getBytes());
                                    fos.flush();
                                }

                                Uri fileUri = FileProvider.getUriForFile(requireContext(), "com.white.streambeat.fileprovider", file);
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(fileUri, "text/plain");
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(getContext(), "No app found to open this file.", Toast.LENGTH_SHORT).show();
                                }
                            } catch (IOException | JSONException e) {
                                Toast.makeText(getContext(), "Error handling response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }, error -> Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
                ) {
                    @Override
                    protected Map<String, String> getParams() {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("key_phone", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber());
                        return map;
                    }
                };

                Volley.newRequestQueue(requireContext()).add(stringRequest);

            }, 100);
        });

        return view;
    }

    private String formatEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        String displayLocalPart = localPart.length() > 5 ? localPart.substring(0, 5) + "*****" : localPart;
        return displayLocalPart + "@" + domain;
    }

    private String getInitials(String name) {
        String[] words = name.split(" ");
        StringBuilder initials = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                initials.append(word.charAt(0));
            }
        }
        return initials.length() >= 2 ? initials.substring(0, 2).toUpperCase() : initials.toString().toUpperCase();
    }
}