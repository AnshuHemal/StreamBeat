package com.white.streambeat.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.white.streambeat.R;

public class PlaybackFragment extends Fragment {

    AppCompatSeekBar seekBar;
    private int[] seekBarSteps = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
    TextView startText;
    ImageView btnBackPB;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_playback, container, false);

        seekBar = view.findViewById(R.id.seekBar);
        startText = view.findViewById(R.id.startSecTxt);
        btnBackPB = view.findViewById(R.id.btnBackPB);

        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.small_push);

        seekBar.setMax(seekBarSteps.length);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int stepIndex = Math.round(progress);
                seekBar.setProgress(stepIndex);
                startText.setText(stepIndex + " s");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        startText.setText("0 s");

        btnBackPB.setOnClickListener(v -> {
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