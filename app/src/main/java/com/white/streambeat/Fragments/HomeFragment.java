package com.white.streambeat.Fragments;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
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
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.white.streambeat.Adapters.FavArtistsAdapter;
import com.white.streambeat.Adapters.HomeLogAlbumsAdapter;
import com.white.streambeat.Adapters.PopularAlbumsAdapter;
import com.white.streambeat.Connections.ServerConnector;
import com.white.streambeat.LoadingDialog;
import com.white.streambeat.Models.Albums;
import com.white.streambeat.Models.Artists;
import com.white.streambeat.Models.SharedViewModel;
import com.white.streambeat.Models.Tracks;
import com.white.streambeat.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class HomeFragment extends Fragment {

    TextView greetingTxt, unameTxt;
    FirebaseUser firebaseUser;
    LinearLayout homeLikedSongsLL, homeLocalFilesLL;
    RecyclerView recyclerViewFavArtists, recyclerViewPopularAlbums, recyclerViewReleases, recyclerViewRecommended,
            homeLogAlbumRV;

    List<Artists> artists = new ArrayList<>();
    List<Albums> popularAlbums;
    List<Albums> recommendedAlbums;
    List<Albums> newReleasesAlbums;
    List<Artists> allArtists = new ArrayList<>();
    List<Albums> allAlbums = new ArrayList<>();
    List<Tracks> allTracks = new ArrayList<>();
    List<Integer> likedTracksIds = new ArrayList<>();

    LoadingDialog dialog;
    private int fetchCount = 0;
    final int TOTAL_FETCHES = 7;

    FavArtistsAdapter favArtistsAdapter;
    PopularAlbumsAdapter popularAlbumsAdapter;
    PopularAlbumsAdapter recommendedAlbumsAdapter;
    PopularAlbumsAdapter newReleasesAlbumsAdapter;
    HomeLogAlbumsAdapter logAlbumsAdapter;

    private SharedViewModel sharedViewModel;

    private static final String PREF_NAME = "MyPrefs";
    private static final String PREF_DATA_FETCHED = "data_fetched";
    SharedPreferences sharedPreferences;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.small_push);

        dialog = new LoadingDialog(requireContext());
        fetchCount = 0;

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        greetingTxt = view.findViewById(R.id.txtGreeting);
        unameTxt = view.findViewById(R.id.txtUsername);
        homeLikedSongsLL = view.findViewById(R.id.homeLikedSongsLL);
        homeLocalFilesLL = view.findViewById(R.id.homeLocalFilesLL);
        recyclerViewFavArtists = view.findViewById(R.id.recyclerViewFavoriteArtists);
        recyclerViewPopularAlbums = view.findViewById(R.id.recyclerViewPopularAlbums);
        recyclerViewReleases = view.findViewById(R.id.recyclerViewReleases);
        recyclerViewRecommended = view.findViewById(R.id.recyclerViewRecommended);
        homeLogAlbumRV = view.findViewById(R.id.homeLogAlbumRV);

        sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        showGreetingMsg();
        fetchAllArtists();
        fetchAllAlbums();
        fetchAllTracks();
        fetchUserInfo();
        fetchFavoriteArtists();
        fetchLikedSongsCount();
        fetchUsersLikedTracks();
        fetchPopularAlbums();
        fetchRecommendedAlbums();
        fetchNewReleasesAlbums();
        fetchUserLogs();

        recyclerViewFavArtists.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        artists = new ArrayList<>();
        favArtistsAdapter = new FavArtistsAdapter(getContext(), artists);
        recyclerViewFavArtists.setAdapter(favArtistsAdapter);

        recyclerViewPopularAlbums.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        popularAlbums = new ArrayList<>();
        popularAlbumsAdapter = new PopularAlbumsAdapter(getContext(), popularAlbums, getParentFragmentManager());
        recyclerViewPopularAlbums.setAdapter(popularAlbumsAdapter);

        recyclerViewRecommended.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recommendedAlbums = new ArrayList<>();
        recommendedAlbumsAdapter = new PopularAlbumsAdapter(getContext(), recommendedAlbums, getParentFragmentManager());
        recyclerViewRecommended.setAdapter(recommendedAlbumsAdapter);

        recyclerViewReleases.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        newReleasesAlbums = new ArrayList<>();
        newReleasesAlbumsAdapter = new PopularAlbumsAdapter(getContext(), newReleasesAlbums, getParentFragmentManager());
        recyclerViewReleases.setAdapter(newReleasesAlbumsAdapter);

        homeLikedSongsLL.setOnClickListener(v -> {
            v.startAnimation(animation);

            new Handler().postDelayed(() -> {
                Fragment likedSongsFragment = new LikedSongsFragment();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.frameLayout, likedSongsFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            },100);
        });

        homeLocalFilesLL.setOnClickListener(v -> {
            v.startAnimation(animation);
            Toast.makeText(getContext(), "This Feature is coming soon..", Toast.LENGTH_SHORT).show();
        });

        boolean dataFetched = sharedPreferences.getBoolean(PREF_DATA_FETCHED, false);
        if (dataFetched) {
            restoreDataFromSharedPreferences();
        } else {
            fetchAllArtists();
            fetchAllAlbums();
            fetchAllTracks();
//            fetchPopularAlbums();
//            fetchRecommendedAlbums();
//            fetchNewReleasesAlbums();
            fetchFavoriteArtists();
            fetchUserInfo();
        }
        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void restoreDataFromSharedPreferences() {
        String artistsJson = sharedPreferences.getString("artists_list", "");
        String albumsJson = sharedPreferences.getString("popular_albums_list", "");
        String recommendedAlbumsJson = sharedPreferences.getString("recommended_albums_list", "");
        String newReleasesAlbumsJson = sharedPreferences.getString("new_releases_albums_list", "");
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
                JSONArray albumsArray = new JSONArray(albumsJson);
                popularAlbums.clear();
                for (int i = 0; i < albumsArray.length(); i++) {
                    JSONObject albumObj = albumsArray.getJSONObject(i);
                    int album_id = albumObj.getInt("album_id");
                    String albumTitle = albumObj.getString("album_title");
                    String image_url = albumObj.getString("cover_image_url");
                    popularAlbums.add(new Albums(album_id, albumTitle, image_url));
                }
                popularAlbumsAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                Toast.makeText(getContext(), "Error restoring popular albums: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        if (!recommendedAlbumsJson.isEmpty()) {
            try {
                JSONArray albumsArray = new JSONArray(albumsJson);
                recommendedAlbums.clear();
                for (int i = 0; i < albumsArray.length(); i++) {
                    JSONObject albumObj = albumsArray.getJSONObject(i);
                    int album_id = albumObj.getInt("album_id");
                    String albumTitle = albumObj.getString("album_title");
                    String image_url = albumObj.getString("cover_image_url");
                    recommendedAlbums.add(new Albums(album_id, albumTitle, image_url));
                }
                recommendedAlbumsAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                Toast.makeText(getContext(), "Error restoring popular albums: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        if (!newReleasesAlbumsJson.isEmpty()) {
            try {
                JSONArray albumsArray = new JSONArray(albumsJson);
                newReleasesAlbums.clear();
                for (int i = 0; i < albumsArray.length(); i++) {
                    JSONObject albumObj = albumsArray.getJSONObject(i);
                    int album_id = albumObj.getInt("album_id");
                    String albumTitle = albumObj.getString("album_title");
                    String image_url = albumObj.getString("cover_image_url");
                    newReleasesAlbums.add(new Albums(album_id, albumTitle, image_url));
                }
                newReleasesAlbumsAdapter.notifyDataSetChanged();
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
            greetingMessage = "Good Morning, \uD83D\uDC4B ";
        }
        greetingTxt.setText(greetingMessage);
    }

    private void fetchUserInfo() {
        dialog.show();
        @SuppressLint("SetTextI18n") StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                ServerConnector.GETUSERINFO_URL,
                response -> {
                    dialog.dismiss();
                    ServerConnector.userFullName = null;
                    ServerConnector.userEmailAddress = null;
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray jsonArray = jsonObject.getJSONArray("response_obj");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject j1 = jsonArray.getJSONObject(i);
                            String fname = j1.optString("fname");
                            String lname = j1.optString("lname");
                            String email = j1.optString("email");
                            unameTxt.setText(fname + " " + lname);
                            ServerConnector.userFullName = fname + " " + lname;
                            ServerConnector.userEmailAddress = email;
                        }
                        saveUserInfoToSharedPreferences(unameTxt.getText().toString());
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    } finally {
                        onFetchComplete();
                    }
                }, error -> {
            Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            onFetchComplete();
        }
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
        dialog.show();
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                ServerConnector.GET_ALL_ARTISTS_DETAILS,
                response -> {
                    ServerConnector.allArtistsList.clear();
                    Log.d(TAG, "fetchAllArtists response: " + response);
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
                            ServerConnector.allArtistsList.addAll(allArtists);
                        }

                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    } finally {
                        onFetchComplete();
                    }
                }, error -> {
            Toast.makeText(requireContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            onFetchComplete();
        }
        );
        Volley.newRequestQueue(requireContext()).add(stringRequest);
    }

    private void fetchAllAlbums() {
        dialog.show();
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                ServerConnector.GET_ALL_ALBUMS_DETAILS,
                response -> {
                    ServerConnector.allAlbumsList.clear();
                    Log.d(TAG, "fetchAllAlbums response: " + response);
                    if (response == null || response.isEmpty()) {
                        Toast.makeText(requireContext(), "Empty response from server", Toast.LENGTH_SHORT).show();
                        return;
                    }
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
                                Albums albums = new Albums(album_id, album_title, cover_image_url);
                                allAlbums.add(albums);
                            }
                            saveAlbumsDetailsToSharedPreferences();
                            ServerConnector.allAlbumsList.addAll(allAlbums);
                            fetchUserLogs();
                        }

                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    } finally {
                        onFetchComplete();
                    }
                }, error -> {
            Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            onFetchComplete();
        }
        );
        Volley.newRequestQueue(requireContext()).add(stringRequest);
    }

    private void fetchAllTracks() {
        dialog.show();
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                ServerConnector.GET_ALL_TRACKS_DETAILS,
                response -> {
                    Log.d(TAG, "fetchAllTracks response: " + response);
                    if (response == null || response.trim().isEmpty()) {
                        Toast.makeText(requireContext(), "Empty response from server", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        ServerConnector.allTracksList.clear();
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray jsonArray1 = jsonObject.getJSONArray("response_all_tracks");
                        allTracks.clear();
                        for (int i = 0; i < jsonArray1.length(); i++) {
                            JSONObject trackObj = jsonArray1.getJSONObject(i);
                            int track_id = trackObj.getInt("track_id");
                            String track_name = trackObj.getString("track_name");
                            String file_url = trackObj.getString("file_url");
                            String track_image_url = trackObj.getString("track_image_url");
                            String albumTitle = trackObj.getString("album_title");

                            String artistNamesString = trackObj.getString("artist_names");
                            String[] artistNamesArray = artistNamesString.split(", ");
                            ArrayList<String> artistNames = new ArrayList<>(Arrays.asList(artistNamesArray));

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
                        ServerConnector.allTracksList.addAll(allTracks);
                    } catch (JSONException e) {
                        Toast.makeText(getContext(), "JSON Parsing Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    } finally {
                        onFetchComplete();
                    }
                },
                error -> {
                    Log.e(TAG, "Volley Error: ", error);
                    Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    onFetchComplete();
                }
        ) {
        };
        Volley.newRequestQueue(requireContext()).add(stringRequest);
    }

    private void fetchPopularAlbums() {
        dialog.show();
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
                            popularAlbumsAdapter.notifyDataSetChanged();
                            savePopularAlbumsToSharedPreferences();
                        } catch (Exception e) {
                            unameTxt.setText(e.getMessage());
                            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        } finally {
                            onFetchComplete();
                        }
                    }
                }, error -> {
            Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            onFetchComplete();
        }
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

    private void fetchRecommendedAlbums() {
        dialog.show();
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                ServerConnector.GET_RECOMMENDED_ALBUMS,
                new Response.Listener<String>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onResponse(String response) {
                        try {
                            recommendedAlbums.clear();
                            JSONObject j = new JSONObject(response);
                            JSONArray jsonArray = j.getJSONArray("response_recommended_albums");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject artistObj = jsonArray.getJSONObject(i);

                                int album_id = artistObj.getInt("album_id");
                                String albumTitle = artistObj.getString("album_title");
                                String imageUrl = "";
                                for (Albums album : ServerConnector.allAlbumsList) {
                                    if (album.getAlbum_title().equals(albumTitle)) {
                                        imageUrl = album.getCover_image_url();
                                        break;
                                    }
                                }
                                recommendedAlbums.add(new Albums(album_id, albumTitle, imageUrl));
                            }
                            recommendedAlbumsAdapter.notifyDataSetChanged();
                            saveRecommendedAlbumsToSharedPreferences();
                        } catch (Exception e) {
                            unameTxt.setText(e.getMessage());
                            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        } finally {
                            onFetchComplete();
                        }
                    }
                }, error -> {
            Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            onFetchComplete();
        }
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

    private void fetchNewReleasesAlbums() {
        dialog.show();
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                ServerConnector.GET_NEW_RELEASES_ALBUMS,
                new Response.Listener<String>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onResponse(String response) {
                        try {
                            newReleasesAlbums.clear();
                            JSONObject j = new JSONObject(response);
                            JSONArray jsonArray = j.getJSONArray("response_new_releases_albums");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject artistObj = jsonArray.getJSONObject(i);

                                int album_id = artistObj.getInt("album_id");
                                String albumTitle = artistObj.getString("album_title");
                                String imageUrl = "";
                                for (Albums album : ServerConnector.allAlbumsList) {
                                    if (album.getAlbum_title().equals(albumTitle)) {
                                        imageUrl = album.getCover_image_url();
                                        break;
                                    }
                                }
                                newReleasesAlbums.add(new Albums(album_id, albumTitle, imageUrl));
                            }
                            newReleasesAlbumsAdapter.notifyDataSetChanged();
                            saveNewReleasesAlbumsToSharedPreferences();
                        } catch (Exception e) {
                            unameTxt.setText(e.getMessage());
                            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        } finally {
                            onFetchComplete();
                        }
                    }
                }, error -> {
            Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            onFetchComplete();
        }
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

    private void fetchFavoriteArtists() {
        dialog.show();
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

                                Artists artist = new Artists(artist_id, artistName, image_url);
                                if (!artists.contains(artist)) {
                                    artists.add(new Artists(artist_id, artistName, image_url));
                                }
                            }
                            ServerConnector.favoriteArtists.clear();
                            ServerConnector.favoriteArtists.addAll(artists);
                            favArtistsAdapter.notifyDataSetChanged();
                            saveArtistsListToSharedPreferences();
                        } catch (Exception e) {
                            unameTxt.setText(e.getMessage());
                            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        } finally {
                            onFetchComplete();
                        }
                    }
                }, error -> {
            Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            onFetchComplete();
        }
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
                hashMap.put("key_phone", firebaseUser.getPhoneNumber());
                return hashMap;
            }
        };
        Volley.newRequestQueue(requireContext()).add(stringRequest);
    }

    public void fetchUsersLikedTracks() {
        dialog.show();
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

                            for (Tracks track : ServerConnector.allTracksList) {
                                if (track.getTrack_id() == trackId) {
                                    track.setLikedByUser(true);
                                }
                            }
                        }
                        storeLikedTracksToServerConnector();

                    } catch (Exception e) {
                        Log.d(TAG, "fetchUsersLikedTracks: " + e.getMessage());
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    } finally {
                        onFetchComplete();
                    }
                }, error -> {
                    onFetchComplete();
            Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
        }
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

    private void storeLikedTracksToServerConnector() {
        List<Tracks> allTracks = ServerConnector.allTracksList;
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

    private void savePopularAlbumsToSharedPreferences() {
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

    private void saveRecommendedAlbumsToSharedPreferences() {
        JSONArray jsonArray = new JSONArray();
        for (Albums artist : recommendedAlbums) {
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
        sharedPreferences.edit().putString("recommended_albums_list", jsonArray.toString()).apply();
        sharedPreferences.edit().putBoolean(PREF_DATA_FETCHED, true).apply();
    }

    private void saveNewReleasesAlbumsToSharedPreferences() {
        JSONArray jsonArray = new JSONArray();
        for (Albums artist : newReleasesAlbums) {
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
        sharedPreferences.edit().putString("new_releases_albums_list", jsonArray.toString()).apply();
        sharedPreferences.edit().putBoolean(PREF_DATA_FETCHED, true).apply();
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

    private void saveAlbumsDetailsToSharedPreferences() {
        JSONArray jsonArray = new JSONArray();
        for (Albums album : popularAlbums) {
            JSONObject albumObj = new JSONObject();
            try {
                albumObj.put("album_id", album.getAlbum_id());
                albumObj.put("album_title", album.getAlbum_title());
                albumObj.put("cover_image_url", album.getCover_image_url());
                jsonArray.put(albumObj);
            } catch (JSONException e) {
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        sharedPreferences.edit().putString("popular_albums_list", jsonArray.toString()).apply();
        sharedPreferences.edit().putBoolean(PREF_DATA_FETCHED, true).apply();
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

    private void fetchUserLogs() {
        dialog.show();
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
                            JSONArray albumIdsArray = logObject.getJSONArray("album_ids");

                            for (int j = 0; j < albumIdsArray.length(); j++) {
                                albumIdsList.add(albumIdsArray.getInt(j));
                            }
                            sharedViewModel.setAlbumIdsList(albumIdsList);
                            filterLogAlbums(albumIdsList);
                        }

                    } catch (Exception e) {
                        Log.d(TAG, "fetchUserLogs: " + e.getMessage());
                    } finally {
                        onFetchComplete();
                    }
                }, error -> {
            Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            onFetchComplete();
        }
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

    private void filterLogAlbums(List<Integer> albumIdsFromHistory) {
        ServerConnector.logAlbumsList.clear();
        List<Albums> filteredAlbums = ServerConnector.allAlbumsList.stream()
                .filter(album -> albumIdsFromHistory.contains(album.getAlbum_id()))
                .collect(Collectors.toList());
        ServerConnector.logAlbumsList.addAll(filteredAlbums);

        homeLogAlbumRV.setLayoutManager(new GridLayoutManager(getContext(), 2));
        logAlbumsAdapter = new HomeLogAlbumsAdapter(getContext(), ServerConnector.logAlbumsList, getParentFragmentManager());
        homeLogAlbumRV.setAdapter(logAlbumsAdapter);
    }

    private void onFetchComplete() {
        fetchCount++;
        if (fetchCount >= TOTAL_FETCHES) {
            dialog.dismiss();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (firebaseUser != null) {
            fetchUserLogs();
        }
    }
}