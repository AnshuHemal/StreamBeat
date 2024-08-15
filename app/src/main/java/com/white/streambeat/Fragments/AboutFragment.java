package com.white.streambeat.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.white.streambeat.R;

public class AboutFragment extends Fragment {

    LinearLayout llVersion, llThirdParty, llTermsOfUse, llPrivacyPolicy, llPlatformRules, llSupport;
    ImageView btnBackAbout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.small_push);
        initialize(view);

        btnBackAbout.setOnClickListener(v -> {
            v.startAnimation(animation);

            new Handler().postDelayed(() -> {
                Fragment settingsFragment = new SettingsFragment();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.frameLayout, settingsFragment);
                transaction.commit();
            },100);
        });

        llVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(animation);
            }
        });

        llThirdParty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(animation);
            }
        });
        llTermsOfUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(animation);
            }
        });
        llPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(animation);
            }
        });
        llPlatformRules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(animation);
            }
        });
        llSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(animation);
            }
        });

        return view;
    }

    public void initialize(View view) {
        llVersion = view.findViewById(R.id.llVersion);
        llThirdParty = view.findViewById(R.id.llThirdPartyLicence);
        llTermsOfUse = view.findViewById(R.id.llTermsOfUse);
        llPrivacyPolicy = view.findViewById(R.id.llPrivacyPolicy);
        llPlatformRules = view.findViewById(R.id.llPlatformRules);
        llSupport = view.findViewById(R.id.llSupport);

        btnBackAbout = view.findViewById(R.id.btnBackAbout);
    }
}