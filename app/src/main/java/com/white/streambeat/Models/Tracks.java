package com.white.streambeat.Models;

import java.util.List;

public class Tracks {
    int track_id;
    String track_name;
    String file_url;
    String track_image_url;
    List<String> artist_names;
    String album_title;

    public Tracks(int track_id, String track_name, String file_url, String track_image_url, List<String> artist_names, String album_title) {
        this.track_id = track_id;
        this.track_name = track_name;
        this.file_url = file_url;
        this.track_image_url = track_image_url;
        this.artist_names = artist_names;
        this.album_title = album_title;
    }

    public int getTrack_id() {
        return track_id;
    }

    public void setTrack_id(int track_id) {
        this.track_id = track_id;
    }

    public String getTrack_name() {
        return track_name;
    }

    public void setTrack_name(String track_name) {
        this.track_name = track_name;
    }

    public String getFile_url() {
        return file_url;
    }

    public void setFile_url(String file_url) {
        this.file_url = file_url;
    }

    public String getTrack_image_url() {
        return track_image_url;
    }

    public void setTrack_image_url(String track_image_url) {
        this.track_image_url = track_image_url;
    }

    public List<String> getArtist_names() {
        return artist_names;
    }

    public void setArtist_names(List<String> artist_names) {
        this.artist_names = artist_names;
    }

    public String getAlbum_title() {
        return album_title;
    }

    public void setAlbum_title(String album_title) {
        this.album_title = album_title;
    }
}
