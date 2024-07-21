package com.white.streambeat.Fragments;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.white.streambeat.Activities.DashboardActivity;
import com.white.streambeat.Adapters.TracksAdapter;
import com.white.streambeat.Models.SharedViewModel;
import com.white.streambeat.Models.Tracks;
import com.white.streambeat.R;

import java.util.ArrayList;
import java.util.List;

public class AlbumTracksFragment extends Fragment {

    private RecyclerView recyclerView;
    private TracksAdapter tracksAdapter;
    SharedViewModel sharedViewModel;
    String albumTitle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album_tracks, container, false);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        ImageView backBtn = view.findViewById(R.id.btnBackLF);
        TextView txtAlbumTitle = view.findViewById(R.id.txtAlbumTitle);
        recyclerView = view.findViewById(R.id.albumTracksRV);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tracksAdapter = new TracksAdapter(getContext());
        recyclerView.setAdapter(tracksAdapter);

        assert getArguments() != null;
        albumTitle = getArguments().getString("album_title", "");
        txtAlbumTitle.setText(albumTitle);
        observeTracksForAlbum(albumTitle);

        backBtn.setOnClickListener(v -> {
            FragmentManager fragmentManager = getParentFragmentManager();
            if (!fragmentManager.isDestroyed()) {
                fragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, new ExploreFragment())
                        .commit();
            }
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                FragmentManager fragmentManager = getParentFragmentManager();
                if (!fragmentManager.isDestroyed()) {
                    fragmentManager.beginTransaction()
                            .replace(R.id.frameLayout, new ExploreFragment())
                            .commit();
                }
            }
        });

        return view;
    }

    private void observeTracksForAlbum(String albumTitle) {
        sharedViewModel.getTracksForAlbum(albumTitle).observe(getViewLifecycleOwner(), tracks -> {
            if (tracks != null) {
                tracksAdapter = new TracksAdapter(getContext());
                tracksAdapter.setTracksList(tracks);
                recyclerView.setAdapter(tracksAdapter);
            }
        });
    }
    public void updateCurrentlyPlayingPosition(int position) {
        if (tracksAdapter != null) {
            tracksAdapter.setCurrentlyPlayingPosition(position);
        }
    }
}