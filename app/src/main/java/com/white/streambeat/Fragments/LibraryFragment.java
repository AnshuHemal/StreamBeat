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

public class LibraryFragment extends Fragment {
    LinearLayout llLikedSongs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_library, container, false);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.small_push);

        llLikedSongs = view.findViewById(R.id.llLikedSongs);

        llLikedSongs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(animation);

                new Handler().postDelayed(() -> {
                    Fragment likedSongsFragment = new LikedSongsFragment();
                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.frameLayout, likedSongsFragment);
                    transaction.commit();
                },100);
            }
        });

        return view;
    }
}