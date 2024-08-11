package com.white.streambeat.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.white.streambeat.Adapters.TracksAdapter;
import com.white.streambeat.Connections.ServerConnector;
import com.white.streambeat.Models.SharedViewModel;
import com.white.streambeat.R;

public class LikedSongsFragment extends Fragment {

    RecyclerView rvLikedSongs;
    TracksAdapter tracksAdapter;
    FirebaseUser firebaseUser;
    SharedViewModel sharedViewModel;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_liked_songs, container, false);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.small_push);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        rvLikedSongs = view.findViewById(R.id.rvLikedSongs);
        rvLikedSongs.setLayoutManager(new LinearLayoutManager(getContext()));
        tracksAdapter = new TracksAdapter(getContext());
        rvLikedSongs.setAdapter(tracksAdapter);

        ImageView btnBack = view.findViewById(R.id.btnBackLikedSongs);
        btnBack.setOnClickListener(v -> {
            v.startAnimation(animation);

            new Handler().postDelayed(() -> {
                Fragment libraryFragment = new LibraryFragment();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.frameLayout, libraryFragment);
                transaction.commit();
            },100);
        });

        tracksAdapter.setTracksList(ServerConnector.likedTracksList);
        tracksAdapter.notifyDataSetChanged();

        return view;
    }

}