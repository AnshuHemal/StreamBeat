package com.white.streambeat.Fragments;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.white.streambeat.Activities.SetupArtistsActivity;
import com.white.streambeat.Adapters.ArtistAdapter;
import com.white.streambeat.Connections.ServerConnector;
import com.white.streambeat.Models.SharedViewModel;
import com.white.streambeat.Models.Tracks;
import com.white.streambeat.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LibraryFragment extends Fragment {
    LinearLayout llLikedSongs, llAddMoreArtists;
    TextView likeSongsCountTxt;
    List<Integer> likedTracksIds = new ArrayList<>();
    SharedViewModel sharedViewModel;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_library, container, false);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.small_push);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        llLikedSongs = view.findViewById(R.id.llLikedSongs);
        llAddMoreArtists = view.findViewById(R.id.addMoreArtistsLL);
        likeSongsCountTxt = view.findViewById(R.id.likeSongsCountTxt);
        RecyclerView artistsRecyclerView = view.findViewById(R.id.selectedArtistsRV);
        artistsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ArtistAdapter adapter = new ArtistAdapter(getContext(), ServerConnector.favoriteArtists);
        artistsRecyclerView.setAdapter(adapter);

        fetchLikedSongsCount();

        likeSongsCountTxt.setText(ServerConnector.likedTracksList.size() + " songs");

        llLikedSongs.setOnClickListener(v -> {
            v.startAnimation(animation);

            new Handler().postDelayed(() -> {
                Fragment likedSongsFragment = new LikedSongsFragment();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.frameLayout, likedSongsFragment);
                transaction.commit();
            },100);
        });

        llAddMoreArtists.setOnClickListener(v -> {
            v.startAnimation(animation);
            new Handler().postDelayed(() -> {
                startActivity(new Intent(getActivity(), SetupArtistsActivity.class));
                requireActivity().finish();
            }, 100);
        });

        return view;
    }

    private void fetchLikedSongsCount() {
        ServerConnector.likedSongsCount = 0;
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                ServerConnector.GET_LIKED_SONGS_COUNT,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        ServerConnector.likedSongsCount = jsonObject.getInt("liked_tracks_count");
                    } catch (Exception e) {
                        Log.d(TAG, "fetchLikedSongsCount: " + e.getMessage());
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("key_phone", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber());
                return hashMap;
            }
        };
        Volley.newRequestQueue(requireContext()).add(stringRequest);
    }

    public void fetchUsersLikedTracks() {
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                ServerConnector.FETCH_USERS_LIKED_TRACKS,
                response -> {
                    ServerConnector.likedTracksList.clear();
                    likedTracksIds.clear();
                    try {
                        JSONArray jsonArray = new JSONArray(response);

                        for (int i = 0; i < jsonArray.length(); i++) {
                            int trackId = jsonArray.getJSONObject(i).getInt("track_id");
                            likedTracksIds.add(trackId);

                            for (Tracks track : Objects.requireNonNull(sharedViewModel.getAllTracksList().getValue())) {
                                if (track.getTrack_id() == trackId) {
                                    track.setLikedByUser(true);
                                }
                            }
                        }
                        storeLikedTracksToServerConnector();

                    } catch (Exception e) {
                        Log.d(TAG, "fetchUsersLikedTracks: " + e.getMessage());
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }, error -> Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> map = new HashMap<>();
                map.put("user_phone", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber());
                return map;
            }
        };
        Volley.newRequestQueue(requireContext()).add(stringRequest);
    }

    private void storeLikedTracksToServerConnector() {
        List<Tracks> allTracks = sharedViewModel.getAllTracksList().getValue();
        if (ServerConnector.likedTracksList == null) {
            ServerConnector.likedTracksList = new ArrayList<>();
        } else {
            ServerConnector.likedTracksList.clear();
        }
        if (allTracks != null) {
            for (Tracks track : allTracks) {
                if (likedTracksIds.contains(track.getTrack_id())) {
                    ServerConnector.likedTracksList.add(track);
                }
            }
        }
    }
}