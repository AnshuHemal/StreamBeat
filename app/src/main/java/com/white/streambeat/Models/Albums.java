package com.white.streambeat.Models;

import java.util.ArrayList;
import java.util.List;

public class Albums {
    int album_id;
    String album_title;
    String cover_image_url;
    List<Tracks> tracks;

    public Albums(int album_id, String album_title, String cover_image_url) {
        this.album_id = album_id;
        this.album_title = album_title;
        this.cover_image_url = cover_image_url;
        tracks = new ArrayList<>();
    }

    public int getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(int album_id) {
        this.album_id = album_id;
    }

    public String getAlbum_title() {
        return album_title;
    }

    public void setAlbum_title(String album_title) {
        this.album_title = album_title;
    }

    public String getCover_image_url() {
        return cover_image_url;
    }

    public List<Tracks> getTracks() {
        return tracks;
    }

    public void setCover_image_url(String cover_image_url) {
        this.cover_image_url = cover_image_url;
    }

    public void addTrack(Tracks track) {
        tracks.add(track);
    }
}