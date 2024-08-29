package com.white.streambeat.Connections;

import com.white.streambeat.Models.Albums;
import com.white.streambeat.Models.Artists;
import com.white.streambeat.Models.Tracks;

import java.util.ArrayList;
import java.util.List;

public class ServerConnector {
//    public static String serverUrl = "https://anantpolymers.com/StreamBeat/sb_user.php/";
    public static String serverUrl = "http://192.168.129.36/StreamBeat/sb_users.php/";
    public static String LOGIN_URL = serverUrl + "login";
    public static String REGISTER_URL = serverUrl + "register";
    public static String CHECK_LOGIN_STATUS = serverUrl + "check_login_status";
    public static String LOGOUT_URL = serverUrl + "logout";
    public static String GETUSERINFO_URL = serverUrl + "get_user_info";

    public static String SAVE_SELECTED_ARTIST = serverUrl + "save_selected_artists";
    public static String GET_SELECTED_ARTISTS = serverUrl + "get_selected_artists";
    public static String GET_ALL_ARTISTS_DETAILS = serverUrl + "get_all_artists_details";
    public static String GET_ALL_ALBUMS_DETAILS = serverUrl + "get_all_albums_details";
    public static String GET_ALL_TRACKS_DETAILS = serverUrl + "get_all_tracks_details";
    public static String STORE_LIKED_TRACKS = serverUrl + "store_liked_tracks";
    public static String REMOVE_LIKED_TRACKS = serverUrl + "remove_liked_tracks";
    public static String FETCH_USERS_LIKED_TRACKS = serverUrl + "fetch_users_liked_tracks";
    public static String GET_POPULAR_ALBUMS = serverUrl + "get_popular_albums";
    public static String GET_RECOMMENDED_ALBUMS = serverUrl + "get_recommended_albums";
    public static String GET_NEW_RELEASES_ALBUMS = serverUrl + "get_new_releases_albums";
    public static String GET_USER_LOGS = serverUrl + "get_user_logs";
    public static String SAVE_USER_LOGS = serverUrl + "save_user_logs";
    public static String GET_LIKED_SONGS_COUNT = serverUrl + "get_liked_songs_count";
    public static String WRITE_LOGS_TO_FILE = serverUrl + "write_user_history_to_file";

    public static List<Tracks> allTracksList = new ArrayList<>();
    public static List<Artists> allArtistsList = new ArrayList<>();
    public static List<Albums> allAlbumsList = new ArrayList<>();

    public static String userFullName = "";
    public static String userEmailAddress = "";
    public static List<Artists> favoriteArtists = new ArrayList<>();
    public static int likedSongsCount = 0;
    public static List<Tracks> likedTracksList = new ArrayList<>();
    public static List<Albums> logAlbumsList = new ArrayList<>();
}
