package com.white.streambeat.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.white.streambeat.Adapters.SearchAdapter;
import com.white.streambeat.Connections.ServerConnector;
import com.white.streambeat.Models.SharedViewModel;
import com.white.streambeat.Models.Tracks;
import com.white.streambeat.R;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExploreFragment extends Fragment {

    private SharedViewModel sharedViewModel;
    private RecyclerView recyclerViewSearchResults;
    private SearchAdapter searchAdapter;
    private List<Object> searchResults;
    private EditText searchView;
    private LinearLayout no_keyword_found;
    FirebaseUser firebaseUser;
    private List<Integer> likedTracksIds;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        FragmentManager fragmentManager = getParentFragmentManager();
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        recyclerViewSearchResults = view.findViewById(R.id.recyclerViewSearchResults);
        recyclerViewSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        fetchUsersLikedTracks();

        no_keyword_found = view.findViewById(R.id.llNoKeywordFound);

        no_keyword_found.setVisibility(View.INVISIBLE);

        searchView = view.findViewById(R.id.searchView);
        searchResults = new ArrayList<>();
        searchAdapter = new SearchAdapter(getContext(), fragmentManager, searchResults);
        recyclerViewSearchResults.setAdapter(searchAdapter);


        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();
                search(query);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Observe search results from ViewModel
        sharedViewModel.getSearchResults().observe(getViewLifecycleOwner(), objects -> {
            if (objects != null) {
                searchAdapter.updateData(objects);
                Log.d("ExploreFragment", "Search results updated: " + searchResults.size() + " items");

                if (searchResults.isEmpty()) {
                    no_keyword_found.setVisibility(View.GONE);
                    recyclerViewSearchResults.setVisibility(View.GONE);
                } else {
                    recyclerViewSearchResults.setVisibility(View.VISIBLE);
                    no_keyword_found.setVisibility(View.GONE);
                }
            } else {
                no_keyword_found.setVisibility(View.GONE);
                recyclerViewSearchResults.setVisibility(View.INVISIBLE);
            }
        });

        return view;
    }

    private void search(String query) {
        if (query.isEmpty()) {
            sharedViewModel.clearSearchResults();
        } else {
            // Trigger search in sharedViewModel
            sharedViewModel.search(query);
            Log.d("ExploreFragment", "Performing search with query: " + query);
        }
    }

    public void updateCurrentlyPlayingPosition(int position) {
        if (searchAdapter != null) {
            searchAdapter.setCurrentlyPlayingPosition(position);
        }
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
//                        displayLikedTracks();

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