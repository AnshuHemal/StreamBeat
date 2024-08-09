package com.white.streambeat.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.white.streambeat.Adapters.ArtistAdapter;
import com.white.streambeat.Connections.ServerConnector;
import com.white.streambeat.R;

public class LibraryFragment extends Fragment {
    LinearLayout llLikedSongs;
    TextView likeSongsCountTxt;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_library, container, false);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.small_push);

        llLikedSongs = view.findViewById(R.id.llLikedSongs);
        likeSongsCountTxt = view.findViewById(R.id.likeSongsCountTxt);
        RecyclerView artistsRecyclerView = view.findViewById(R.id.selectedArtistsRV);
        artistsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ArtistAdapter adapter = new ArtistAdapter(getContext(), ServerConnector.favoriteArtists);
        artistsRecyclerView.setAdapter(adapter);

        likeSongsCountTxt.setText(ServerConnector.likedSongsCount + " songs");

        llLikedSongs.setOnClickListener(v -> {
            v.startAnimation(animation);

            new Handler().postDelayed(() -> {
                Fragment likedSongsFragment = new LikedSongsFragment();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.frameLayout, likedSongsFragment);
                transaction.commit();
            },100);
        });

        return view;
    }
}