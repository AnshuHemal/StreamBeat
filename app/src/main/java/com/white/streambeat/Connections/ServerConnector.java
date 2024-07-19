package com.white.streambeat.Connections;

public class ServerConnector {
    public static String serverUrl = "https://anantpolymers.com/StreamBeat/sb_users.php/";
    public static String LOGIN_URL = serverUrl + "login";
    public static String REGISTER_URL = serverUrl + "register";
    public static String CHECK_LOGIN_STATUS = serverUrl + "check_login_status";
    public static String LOGOUT_URL = serverUrl + "logout";
    public static String GETUSERINFO_URL = serverUrl + "get_user_info";

    public static String GET_ARTISTS_URL = serverUrl + "get_fav_artists";
    public static String SAVE_SELECTED_ARTIST = serverUrl + "save_selected_artists";
    public static String GET_SELECTED_ARTISTS = serverUrl + "get_selected_artists";
    public static String GET_BESTS_OF_ARTISTS = serverUrl + "get_best_of_artists_albums";
    public static String GET_ALBUM_TRACKS_URL = serverUrl + "get_album_tracks";
    public static String GET_ALL_ARTISTS_DETAILS = serverUrl + "get_all_artists_details";
    public static String GET_ALL_ALBUMS_DETAILS = serverUrl + "get_all_albums_details";
    public static String GET_ALL_TRACKS_DETAILS = serverUrl + "get_all_tracks_details";
}
