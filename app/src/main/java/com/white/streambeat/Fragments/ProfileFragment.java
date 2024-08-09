package com.white.streambeat.Fragments;

import android.graphics.Color;
import android.os.Bundle;

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

import com.white.streambeat.Connections.ServerConnector;
import com.white.streambeat.R;

import java.util.Random;

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
            },100);
        });

        llHistory.setOnClickListener(v -> {
            v.startAnimation(animation);

            new Handler().postDelayed(() -> {
                Fragment historyFragment = new ListeningHistoryFragment();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.frameLayout, historyFragment);
                transaction.commit();
            },100);
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

    private int getRandomColor() {
        Random random = new Random();
        return Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }
}