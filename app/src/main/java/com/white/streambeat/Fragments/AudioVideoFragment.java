package com.white.streambeat.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.white.streambeat.R;

public class AudioVideoFragment extends Fragment {

    ImageView btnBackAudioVideo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_audio_video, container, false);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.small_push);
        btnBackAudioVideo = view.findViewById(R.id.btnBackAudioVideo);

        btnBackAudioVideo.setOnClickListener(v -> {
            v.startAnimation(animation);
            new Handler().postDelayed(() -> {
                Fragment settingsFragment = new SettingsFragment();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.frameLayout, settingsFragment);
                transaction.commit();
            },100);
        });
        return view;
    }
}