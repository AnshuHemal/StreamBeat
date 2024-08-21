package com.white.streambeat.Fragments;

import android.annotation.SuppressLint;
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
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.white.streambeat.Adapters.TracksAdapter;
import com.white.streambeat.Connections.ServerConnector;
import com.white.streambeat.Models.Albums;
import com.white.streambeat.Models.SharedViewModel;
import com.white.streambeat.Models.Tracks;
import com.white.streambeat.R;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlbumTracksFragment extends Fragment {

    private RecyclerView recyclerView;
    private TracksAdapter tracksAdapter;
    SharedViewModel sharedViewModel;
    String albumTitle, albumCoverImageUrl;
    private List<Integer> likedTracksIds;
    FirebaseUser firebaseUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album_tracks, container, false);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        ImageView backBtn = view.findViewById(R.id.btnBackLF);
        TextView txtAlbumTitle = view.findViewById(R.id.txtAlbumTitle);
        ImageView albumImage = view.findViewById(R.id.albumImage);
        recyclerView = view.findViewById(R.id.albumTracksRV);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tracksAdapter = new TracksAdapter(getContext());
        recyclerView.setAdapter(tracksAdapter);

        assert getArguments() != null;
        albumTitle = getArguments().getString("album_title", "");
        for (Albums album : ServerConnector.allAlbumsList) {
            if (album.getAlbum_title().equals(albumTitle)) {
                albumCoverImageUrl = album.getCover_image_url();
            }
        }
        Glide.with(view).load(albumCoverImageUrl).into(albumImage);
        txtAlbumTitle.setText(albumTitle);
        fetchUsersLikedTracks();

        backBtn.setOnClickListener(v -> {
            FragmentManager fragmentManager = getParentFragmentManager();
            if (!fragmentManager.isDestroyed()) {
                fragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, new ExploreFragment())
                        .commit();
            }
        });

//        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
//            @Override
//            public void handleOnBackPressed() {
//                FragmentManager fragmentManager = getParentFragmentManager();
//                if (!fragmentManager.isDestroyed()) {
//                    fragmentManager.beginTransaction()
//                            .replace(R.id.frameLayout, new HomeFragment())
//                            .commit();
//                }
//            }
//        });

        return view;
    }

    private void observeTracksForAlbum(String albumTitle) {
        sharedViewModel.getTracksForAlbum(albumTitle).observe(getViewLifecycleOwner(), tracks -> {
            if (tracks != null) {
                for (Tracks track : tracks) {
                    track.setLikedByUser(checkIfTrackIsLiked(track.getTrack_id()));
                }

                tracksAdapter = new TracksAdapter(getContext());
                tracksAdapter.setTracksList(tracks);
                recyclerView.setAdapter(tracksAdapter);
            }
        });
    }

    private boolean checkIfTrackIsLiked(int trackId) {
        return likedTracksIds != null && likedTracksIds.contains(trackId);
    }

    public void updateCurrentlyPlayingPosition(int position) {
        if (tracksAdapter != null) {
            tracksAdapter.setCurrentlyPlayingPosition(position);
        }
    }

    public void fetchUsersLikedTracks() {
        @SuppressLint("NotifyDataSetChanged") StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                ServerConnector.FETCH_USERS_LIKED_TRACKS,
                response -> {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        likedTracksIds = new ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            int trackId = jsonArray.getJSONObject(i).getInt("track_id");
                            likedTracksIds.add(trackId);
                        }
                        observeTracksForAlbum(albumTitle);
                        tracksAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }, error -> Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> map = new HashMap<>();
                map.put("user_phone", firebaseUser.getPhoneNumber());
                return map;
            }
        };
        Volley.newRequestQueue(requireContext()).add(stringRequest);
    }
}