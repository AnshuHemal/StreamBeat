package com.white.streambeat.Fragments;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.white.streambeat.Adapters.FavArtistsAdapter;
import com.white.streambeat.Adapters.PopularAlbumsAdapter;
import com.white.streambeat.Connections.ServerConnector;
import com.white.streambeat.Models.Albums;
import com.white.streambeat.Models.Artists;
import com.white.streambeat.Models.SharedViewModel;
import com.white.streambeat.Models.Tracks;
import com.white.streambeat.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    TextView greetingTxt, unameTxt;
    FirebaseUser firebaseUser;
    RecyclerView recyclerViewFavArtists, recyclerViewPopularAlbums;

    List<Artists> artists = new ArrayList<>();
    List<Albums> popularAlbums = new ArrayList<>();
    List<Artists> allArtists = new ArrayList<>();
    List<Albums> allAlbums = new ArrayList<>();
    List<Tracks> allTracks = new ArrayList<>();

    FavArtistsAdapter favArtistsAdapter;
    PopularAlbumsAdapter albumsAdapter;

    private SharedViewModel sharedViewModel;

    private static final String PREF_NAME = "MyPrefs";
    private static final String PREF_DATA_FETCHED = "data_fetched";
    SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        greetingTxt = view.findViewById(R.id.txtGreeting);
        unameTxt = view.findViewById(R.id.txtUsername);
        recyclerViewFavArtists = view.findViewById(R.id.recyclerViewFavoriteArtists);
        recyclerViewPopularAlbums = view.findViewById(R.id.recyclerViewPopularAlbums);

        showGreetingMsg();

        if (isAdded()) {
            sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
        fetchAllArtists();
        fetchAllAlbums();
        fetchAllTracks();
        fetchPopularAlbums();
        fetchUserInfo();

        recyclerViewFavArtists.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        artists = new ArrayList<>();
        favArtistsAdapter = new FavArtistsAdapter(getContext(), artists);
        recyclerViewFavArtists.setAdapter(favArtistsAdapter);

        recyclerViewPopularAlbums.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        popularAlbums = new ArrayList<>();
        albumsAdapter = new PopularAlbumsAdapter(getContext(), popularAlbums, getParentFragmentManager());
        recyclerViewPopularAlbums.setAdapter(albumsAdapter);

        boolean dataFetched = sharedPreferences.getBoolean(PREF_DATA_FETCHED, false);
        if (!dataFetched) {
            fetchUserInfo();
            fetchFavoriteArtists();
            fetchPopularAlbums();
//            fetchBestsOfArtistsAlbums();
        } else {
            restoreDataFromSharedPreferences();
        }

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void restoreDataFromSharedPreferences() {
        String artistsJson = sharedPreferences.getString("artists_list", "");
        String albumsJson = sharedPreferences.getString("popular_albums_list", "");
        if (!artistsJson.isEmpty()) {
            try {
                JSONArray artistsArray = new JSONArray(artistsJson);
                artists.clear();
                for (int i = 0; i < artistsArray.length(); i++) {
                    JSONObject artistObj = artistsArray.getJSONObject(i);
                    int artist_id = artistObj.getInt("artist_id");
                    String artistName = artistObj.getString("artist_name");
                    String image_url = artistObj.getString("image_url");
                    artists.add(new Artists(artist_id, artistName, image_url));
                }
                favArtistsAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                Toast.makeText(getContext(), "Error restoring favorite artists: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        if (!albumsJson.isEmpty()) {
            try {
                JSONArray artistsArray = new JSONArray(albumsJson);
                popularAlbums.clear();
                for (int i = 0; i < artistsArray.length(); i++) {
                    JSONObject artistObj = artistsArray.getJSONObject(i);
                    int artist_id = artistObj.getInt("album_id");
                    String artistName = artistObj.getString("album_title");
                    String image_url = artistObj.getString("cover_image_url");
                    popularAlbums.add(new Albums(artist_id, artistName, image_url));
                }
                albumsAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                Toast.makeText(getContext(), "Error restoring popular albums: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveUserInfoToSharedPreferences(String fullName) {
        sharedPreferences.edit()
                .putBoolean(PREF_DATA_FETCHED, true)
                .putString("user_name", fullName)
                .apply();
    }

    public void showGreetingMsg() {
        int hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greetingMessage;
        if (hourOfDay < 12) {
            greetingMessage = "Good Morning, \uD83D\uDC4B ";
        } else if (hourOfDay < 18) {
            greetingMessage = "Good Afternoon, \uD83D\uDC4B ";
        } else {
            greetingMessage = "Good Evening, \uD83D\uDC4B ";
        }
        greetingTxt.setText(greetingMessage);
    }

    private void fetchUserInfo() {
        @SuppressLint("SetTextI18n") StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                ServerConnector.GETUSERINFO_URL,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray jsonArray = jsonObject.optJSONArray("response_obj");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject j1 = jsonArray.getJSONObject(i);
                            String fname = j1.optString("fname");
                            String lname = j1.optString("lname");
                            unameTxt.setText(fname + " " + lname);
                        }
                        saveUserInfoToSharedPreferences(unameTxt.getText().toString());
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }, error -> Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> hm = new HashMap<>();
                hm.put("key_phone", firebaseUser.getPhoneNumber());
                return hm;
            }
        };
        Volley.newRequestQueue(requireContext()).add(stringRequest);
    }

    private void fetchAllArtists() {
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                ServerConnector.GET_ALL_ARTISTS_DETAILS,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray jsonArray = jsonObject.getJSONArray("response_all_artists");
                        allArtists.clear();
                        if (isAdded()) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject artistObj = jsonArray.getJSONObject(i);
                                int artistId = artistObj.getInt("artist_id");
                                String artistName = artistObj.getString("artist_name");
                                String imageUrlBase64 = artistObj.getString("image_url");

                                Artists artists = new Artists(artistId, artistName, imageUrlBase64);
                                allArtists.add(artists);
                            }
                            saveArtistsDetailsToSharedPreferences();
                            sharedViewModel.setAllArtistsList(allArtists);
                        }

                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }, error -> Toast.makeText(requireContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(requireContext()).add(stringRequest);
    }

    private void fetchAllAlbums() {
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                ServerConnector.GET_ALL_ALBUMS_DETAILS,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray jsonArray = jsonObject.getJSONArray("response_all_albums");
                        allAlbums.clear();
                        if (isAdded()) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject albumObj = jsonArray.getJSONObject(i);
                                int album_id = albumObj.getInt("album_id");
                                String album_title = albumObj.getString("album_title");
                                String cover_image_url = albumObj.getString("cover_image_url");
//                                String cover_image_url = "https://hollywoodlife.com/wp-content/uploads/2018/03/rexfeatures_9623254w.jpg?w=680";
                                Albums albums = new Albums(album_id, album_title, cover_image_url);
                                allAlbums.add(albums);
                            }
                            saveAlbumsDetailsToSharedPreferences();
                            sharedViewModel.setAllAlbumsList(allAlbums);
                        }

                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }, error -> Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(requireContext()).add(stringRequest);
    }

    private void fetchAllTracks() {
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                ServerConnector.GET_ALL_TRACKS_DETAILS,
                response -> {
                    Log.d(TAG, "onResponse: HomeFragment" + response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray jsonArray = jsonObject.getJSONArray("response_all_tracks");
                        allTracks.clear();
                        if (isAdded()) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject trackObj = jsonArray.getJSONObject(i);
                                int track_id = trackObj.getInt("track_id");
                                String track_name = trackObj.getString("track_name");
                                String file_url = trackObj.getString("file_url");
                                String track_image_url = trackObj.getString("track_image_url");
//                                String track_image_url = "https://hollywoodlife.com/wp-content/uploads/2018/03/rexfeatures_9623254w.jpg?w=680";
                                String albumTitle = trackObj.getString("album_title");

                                Object artistNamesObj = trackObj.get("artist_names");
                                List<String> artistNames = new ArrayList<>();
                                if (artistNamesObj instanceof JSONArray) {
                                    JSONArray artistNamesArray = (JSONArray) artistNamesObj;
                                    for (int j = 0; j < artistNamesArray.length(); j++) {
                                        artistNames.add(artistNamesArray.getString(j));
                                    }
                                } else if (artistNamesObj instanceof String) {
                                    artistNames.add((String) artistNamesObj);
                                }
                                Tracks track = new Tracks(track_id, track_name, file_url, track_image_url, artistNames, albumTitle);
                                allTracks.add(track);

                                for (Albums album : allAlbums) {
                                    if (album.getAlbum_title().equals(albumTitle)) {
                                        album.addTrack(track);
                                        break;
                                    }
                                }
                            }
                            saveTracksDetailsToSharedPreferences();
                            sharedViewModel.setAllTracksList(allTracks);
                        }

                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }, error -> Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(requireContext()).add(stringRequest);
    }

    private void fetchPopularAlbums() {
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                ServerConnector.GET_POPULAR_ALBUMS,
                new Response.Listener<String>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onResponse(String response) {
                        try {
                            popularAlbums.clear();
                            JSONObject j = new JSONObject(response);
                            JSONArray jsonArray = j.getJSONArray("response_all_albums");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject artistObj = jsonArray.getJSONObject(i);

                                int artist_id = artistObj.getInt("album_id");
                                String artistName = artistObj.getString("album_title");
                                String image_url = artistObj.getString("cover_image_url");

                                popularAlbums.add(new Albums(artist_id, artistName, image_url));
                            }
                            albumsAdapter.notifyDataSetChanged();
                            saveAlbumsToSharedPreferences();
                        } catch (Exception e) {
                            unameTxt.setText(e.getMessage());
                            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, error -> Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> map = new HashMap<>();
                map.put("key_phone", firebaseUser.getPhoneNumber());
                return map;
            }
        };
        Volley.newRequestQueue(requireContext()).add(stringRequest);
    }

    private void saveAlbumsToSharedPreferences() {
        JSONArray jsonArray = new JSONArray();
        for (Albums artist : popularAlbums) {
            JSONObject artistObj = new JSONObject();
            try {
                artistObj.put("album_id", artist.getAlbum_id());
                artistObj.put("album_title", artist.getAlbum_id());
                artistObj.put("cover_image_url", artist.getCover_image_url());
                jsonArray.put(artistObj);
            } catch (JSONException e) {
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        sharedPreferences.edit().putString("popular_albums_list", jsonArray.toString()).apply();
        sharedPreferences.edit().putBoolean(PREF_DATA_FETCHED, true).apply();
    }

    private void fetchFavoriteArtists() {
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                ServerConnector.GET_SELECTED_ARTISTS,
                new Response.Listener<String>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onResponse(String response) {
                        try {
                            artists.clear();
                            JSONObject j = new JSONObject(response);
                            JSONArray jsonArray = j.getJSONArray("response_artists");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject artistObj = jsonArray.getJSONObject(i);

                                int artist_id = artistObj.getInt("artist_id");
                                String artistName = artistObj.getString("artist_name");
                                String image_url = artistObj.getString("image_url");

                                artists.add(new Artists(artist_id, artistName, image_url));
                            }
                            favArtistsAdapter.notifyDataSetChanged();
                            saveArtistsListToSharedPreferences();
                        } catch (Exception e) {
                            unameTxt.setText(e.getMessage());
                            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, error -> Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> hm = new HashMap<>();
                hm.put("key_phone", firebaseUser.getPhoneNumber());
                return hm;
            }
        };
        Volley.newRequestQueue(requireContext()).add(stringRequest);
    }

    private void saveArtistsListToSharedPreferences() {
        JSONArray jsonArray = new JSONArray();
        for (Artists artist : artists) {
            JSONObject artistObj = new JSONObject();
            try {
                artistObj.put("artist_id", artist.getArtist_id());
                artistObj.put("artist_name", artist.getArtist_name());
                artistObj.put("image_url", artist.getImage_url());
                jsonArray.put(artistObj);
            } catch (JSONException e) {
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        sharedPreferences.edit().putString("artists_list", jsonArray.toString()).apply();
        sharedPreferences.edit().putBoolean(PREF_DATA_FETCHED, true).apply();
    }

    private void saveArtistsDetailsToSharedPreferences() {
        JSONArray jsonArray = new JSONArray();
        for (Artists artists : allArtists) {
            JSONObject artistObj = new JSONObject();
            try {
                artistObj.put("artist_id", artists.getArtist_id());
                artistObj.put("artist_name", artists.getArtist_name());
                artistObj.put("image_url", artists.getImage_url());
                jsonArray.put(artistObj);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        sharedPreferences.edit().putString("all_artists_details", jsonArray.toString()).apply();
    }

    private void saveAlbumsDetailsToSharedPreferences() {
        JSONArray jsonArray = new JSONArray();
        for (Albums albums : allAlbums) {
            JSONObject albumObj = new JSONObject();
            try {
                albumObj.put("album_id", albums.getAlbum_id());
                albumObj.put("album_title", albums.getAlbum_title());
                albumObj.put("cover_image_url", albums.getCover_image_url());
                jsonArray.put(albumObj);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        sharedPreferences.edit().putString("all_albums_details", jsonArray.toString()).apply();
    }

    private void saveTracksDetailsToSharedPreferences() {
        JSONArray jsonArray = new JSONArray();
        for (Tracks tracks : allTracks) {
            JSONObject trackObj = new JSONObject();
            try {
                trackObj.put("track_id", tracks.getTrack_id());
                trackObj.put("track_name", tracks.getTrack_name());
                trackObj.put("file_url", tracks.getFile_url());
                trackObj.put("track_image_url", tracks.getTrack_image_url());
                trackObj.put("album_title", tracks.getAlbum_title());

                JSONArray artistNamesArray = new JSONArray(tracks.getArtist_names());
                trackObj.put("artist_names", artistNamesArray);

                jsonArray.put(trackObj);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        sharedPreferences.edit().putString("all_tracks_details", jsonArray.toString()).apply();
    }
}