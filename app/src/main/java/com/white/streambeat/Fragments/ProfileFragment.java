package com.white.streambeat.Fragments;

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

import com.white.streambeat.R;

public class ProfileFragment extends Fragment {

    LinearLayout llWhatsNew, llHistory, llSettings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.small_push);

        llSettings = view.findViewById(R.id.llSettings);
        llHistory = view.findViewById(R.id.llListeningHistory);

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
}