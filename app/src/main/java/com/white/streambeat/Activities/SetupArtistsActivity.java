package com.white.streambeat.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.white.streambeat.Adapters.SetupArtistAdapter;
import com.white.streambeat.Connections.ServerConnector;
import com.white.streambeat.Models.Artists;
import com.white.streambeat.Models.SharedViewModel;
import com.white.streambeat.databinding.ActivitySetupArtistsBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SetupArtistsActivity extends AppCompatActivity {

    ActivitySetupArtistsBinding binding;
    FirebaseUser firebaseUser;
    List<Artists> artistsList = new ArrayList<>();
    SharedPreferences sharedPreferences;
    private static final String PREF_DATA_FETCHED = "data_fetched";
    private SharedViewModel sharedViewModel;
    SetupArtistAdapter artistAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySetupArtistsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        binding.artistsRV.setLayoutManager(new LinearLayoutManager(this));
        artistAdapter = new SetupArtistAdapter(artistsList, this);
        binding.artistsRV.setAdapter(artistAdapter);

        boolean dataFetched = sharedPreferences.getBoolean(PREF_DATA_FETCHED, false);
        if (dataFetched) {
            restoreArtistsDetailsFromSharedPreferences();
        } else {
            fetchAllArtists();
        }

        sharedViewModel.getArtistList().observe(this, artists -> artistAdapter.updateData(artists));

        binding.btnContinue.setOnClickListener(v -> saveSelectedArtists());
    }

    @SuppressLint("NotifyDataSetChanged")
    private void restoreArtistsDetailsFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String artistsJson = sharedPreferences.getString("artists_details", "");

        if (!artistsJson.isEmpty()) {
            try {
                JSONArray artistsArray = new JSONArray(artistsJson);
                artistsList.clear();
                for (int i = 0; i < artistsArray.length(); i++) {
                    JSONObject artistObj = artistsArray.getJSONObject(i);
                    int artist_id = artistObj.getInt("artist_id");
                    String artistName = artistObj.getString("artist_name");
                    String image_url = artistObj.getString("image_url");
                    artistsList.add(new Artists(artist_id, artistName, image_url));
                }
                artistAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                Toast.makeText(this, "Error restoring artists: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveSelectedArtists() {
        List<Artists> selectedArtists = artistAdapter.getSelectedArtists();

        if (selectedArtists.size() < 5 || selectedArtists.size() > 15) {
            Toast.makeText(this, "Select Artists between 5 to 15", Toast.LENGTH_SHORT).show();
        } else {
            List<Integer> selectedArtistIds = new ArrayList<>();
            for (Artists artist : selectedArtists) {
                if (artist.isSelected()) {
                    selectedArtistIds.add(artist.getArtist_id());
                }
            }

            JSONArray jsonArray = new JSONArray(selectedArtistIds);
            JSONObject postData = new JSONObject();

            try {
                postData.put("phone", firebaseUser.getPhoneNumber());
                postData.put("selected_artists", jsonArray);
            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    ServerConnector.SAVE_SELECTED_ARTIST,
                    response -> {
                        startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                        finish();
                    }, error -> Toast.makeText(SetupArtistsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
            ) {
                @Override
                public byte[] getBody() {
                    return postData.toString().getBytes();
                }

                @Override
                public String getBodyContentType() {
                    return "application/json";
                }
            };
            Volley.newRequestQueue(this).add(stringRequest);
        }
    }

    public void fetchAllArtists() {
        @SuppressLint("NotifyDataSetChanged") StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                ServerConnector.GET_ALL_ARTISTS_DETAILS,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray jsonArray = jsonObject.getJSONArray("response_artists");
                        artistsList.clear();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject artistObj = jsonArray.getJSONObject(i);
                            int artistId = artistObj.getInt("artist_id");
                            String artistName = artistObj.getString("artist_name");
                            String imageUrl = artistObj.getString("image_url");

                            Artists artist = new Artists(artistId, artistName, imageUrl);
                            artistsList.add(artist);
                        }
                        artistAdapter.notifyDataSetChanged();
                        saveArtistsDetailsToSharedPreferences();
                        sharedViewModel.setArtistList(artistsList);

                    } catch (JSONException e) {
                        Toast.makeText(SetupArtistsActivity.this, "JSON Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(SetupArtistsActivity.this, "Error fetching artists: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(stringRequest);
    }
    private void saveArtistsDetailsToSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        JSONArray jsonArray = new JSONArray();
        for (Artists artist : artistsList) {
            JSONObject artistObj = new JSONObject();
            try {
                artistObj.put("artist_id", artist.getArtist_id());
                artistObj.put("artist_name", artist.getArtist_name());
                artistObj.put("image_url", artist.getImage_url());
                jsonArray.put(artistObj);
            } catch (JSONException e) {
                Toast.makeText(this, "Error saving artists: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        editor.putString("artists_details", jsonArray.toString());
        editor.apply();
    }
}