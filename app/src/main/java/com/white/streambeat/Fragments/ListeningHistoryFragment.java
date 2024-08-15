package com.white.streambeat.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.white.streambeat.Adapters.UserLogsAdapter; // Update this adapter to handle albums
import com.white.streambeat.Connections.ServerConnector;
import com.white.streambeat.Models.Albums;
import com.white.streambeat.Models.SharedViewModel;
import com.white.streambeat.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ListeningHistoryFragment extends Fragment {

    private SharedViewModel sharedViewModel;
    private RecyclerView recyclerView;
    private List<Albums> allAlbums = new ArrayList<>();
    private UserLogsAdapter adapter; // Update this adapter to handle albums
    private final Map<String, List<Albums>> dateWithAlbumsMap = new HashMap<>();

    @SuppressLint("ConstantLocale")
    private static final SimpleDateFormat INPUT_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    @SuppressLint("ConstantLocale")
    private static final SimpleDateFormat OUTPUT_FORMAT = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_listening_history, container, false);

        recyclerView = view.findViewById(R.id.listeningHistoryRV);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        fetchAllAlbums();
        fetchUserLogs();

        return view;
    }

    private void fetchAllAlbums() {
        sharedViewModel.getAllAlbumsList().observe(getViewLifecycleOwner(), albums -> {
            if (albums != null) {
                allAlbums = new ArrayList<>(albums);
                filterAlbumsByDate();
            }
        });
    }

    private void fetchUserLogs() {
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                ServerConnector.GET_USER_LOGS,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray logsArray = jsonObject.getJSONArray("response_logs");
                        List<Integer> albumIdsList = new ArrayList<>();

                        for (int i = 0; i < logsArray.length(); i++) {
                            JSONObject logObject = logsArray.getJSONObject(i);
                            String listenDate = logObject.getString("listen_date");
                            JSONArray albumIdsArray = logObject.getJSONArray("album_ids");

                            for (int j = 0; j < albumIdsArray.length(); j++) {
                                albumIdsList.add(albumIdsArray.getInt(j));
                            }

                            sharedViewModel.setAlbumIdsList(albumIdsList);
                            filterAlbumsByDate(listenDate, albumIdsList);
                        }

                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }, error -> Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
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

    private String getDateLabel(String inputDate) {
        try {
            Date date = INPUT_FORMAT.parse(inputDate);
            if (date == null) return inputDate;

            Date today = new Date();
            Date yesterday = new Date(today.getTime() - 1000 * 60 * 60 * 24);

            if (INPUT_FORMAT.format(date).equals(INPUT_FORMAT.format(today))) {
                return "Today";
            } else if (INPUT_FORMAT.format(date).equals(INPUT_FORMAT.format(yesterday))) {
                return "Yesterday";
            } else {
                return OUTPUT_FORMAT.format(date);
            }
        } catch (Exception e) {
            return inputDate;
        }
    }

    private void filterAlbumsByDate(String listenDate, List<Integer> albumIdsFromHistory) {
        List<Albums> filteredAlbums = allAlbums.stream()
                .filter(album -> albumIdsFromHistory.contains(album.getAlbum_id()))
                .collect(Collectors.toList());

        dateWithAlbumsMap.put(getDateLabel(listenDate), filteredAlbums);
        displayAlbums();
    }

    private void filterAlbumsByDate() {
        if (!dateWithAlbumsMap.isEmpty()) {
            displayAlbums();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void displayAlbums() {
        if (adapter == null) {
            adapter = new UserLogsAdapter(getContext(), dateWithAlbumsMap); // Update the adapter for albums
            recyclerView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }
}
