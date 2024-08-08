package com.white.streambeat.Activities;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.white.streambeat.Adapters.SetupArtistAdapter;
import com.white.streambeat.Connections.ServerConnector;
import com.white.streambeat.LoadingDialog;
import com.white.streambeat.Models.Artists;
import com.white.streambeat.databinding.ActivitySetupArtistsBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SetupArtistsActivity extends AppCompatActivity {

    ActivitySetupArtistsBinding binding;
    FirebaseUser firebaseUser;
    List<Artists> artistsList = new ArrayList<>();
    SetupArtistAdapter artistAdapter;
    LoadingDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        binding = ActivitySetupArtistsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).hide();

        dialog = new LoadingDialog(this);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        binding.artistsRV.setLayoutManager(new GridLayoutManager(this, 3));
        artistAdapter = new SetupArtistAdapter(artistsList, this);
        binding.artistsRV.setAdapter(artistAdapter);

        fetchAllArtists();

        binding.btnContinue.setOnClickListener(v -> saveSelectedArtists());
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

            dialog.show();

            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    ServerConnector.SAVE_SELECTED_ARTIST,
                    response -> {
                        dialog.dismiss();
                        startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                        finish();
                    }, error -> {
                dialog.dismiss();
                Toast.makeText(SetupArtistsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
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
        dialog.show();
        @SuppressLint("NotifyDataSetChanged") StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                ServerConnector.GET_ALL_ARTISTS_DETAILS,
                response -> {
                    Log.d(TAG, "fetchAllArtists response: " + response); // Add logging
                    dialog.dismiss();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray jsonArray = jsonObject.getJSONArray("response_all_artists");
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
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON Error: " + e.getMessage()); // Add logging
                        Toast.makeText(SetupArtistsActivity.this, "JSON Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Error fetching artists: " + error.getMessage()); // Add logging
                    dialog.dismiss();
                    Toast.makeText(SetupArtistsActivity.this, "Error fetching artists: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(stringRequest);
    }
}