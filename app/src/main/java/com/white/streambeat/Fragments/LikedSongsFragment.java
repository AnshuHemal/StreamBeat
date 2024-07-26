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
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.white.streambeat.Adapters.TracksAdapter;
import com.white.streambeat.Connections.ServerConnector;
import com.white.streambeat.Models.SharedViewModel;
import com.white.streambeat.Models.Tracks;
import com.white.streambeat.R;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LikedSongsFragment extends Fragment {

    RecyclerView rvLikedSongs;
    private TracksAdapter tracksAdapter;
    FirebaseUser firebaseUser;
    private List<Integer> likedTracksIds;
    SharedViewModel sharedViewModel;

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

        fetchUsersLikedTracks();

        sharedViewModel.getAllTracksList().observe(getViewLifecycleOwner(), tracks -> {
            if (tracks != null) {
                fetchUsersLikedTracks();
            }
        });

        return view;
    }

    public void fetchUsersLikedTracks() {
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                ServerConnector.FETCH_USERS_LIKED_TRACKS,
                response -> {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        likedTracksIds = new ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            int trackId = jsonArray.getJSONObject(i).getInt("track_id");
                            likedTracksIds.add(trackId);

                            for (Tracks track : sharedViewModel.getAllTracksList().getValue()) {
                                if (track.getTrack_id() == trackId) {
                                    track.setLikedByUser(true);
                                }
                            }
                        }
                        displayLikedTracks();

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

    @SuppressLint("NotifyDataSetChanged")
    private void displayLikedTracks() {
        List<Tracks> likedTracks = new ArrayList<>();

        List<Tracks> allTracks = sharedViewModel.getAllTracksList().getValue();

        if (allTracks != null) {
            for (Tracks track : allTracks) {
                if (likedTracksIds.contains(track.getTrack_id())) {
                    likedTracks.add(track);
                }
            }
        }

        tracksAdapter.setTracksList(likedTracks);
        tracksAdapter.notifyDataSetChanged();
    }
}