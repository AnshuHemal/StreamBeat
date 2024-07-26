package com.white.streambeat.Models;

public class Artists {
    int artist_id;
    String artist_name;
    String image_url;
    boolean isSelected;

    public Artists(int artist_id, String artist_name, String image_url) {
        this.artist_id = artist_id;
        this.artist_name = artist_name;
        this.image_url = image_url;
        this.isSelected = false;
    }

    public int getArtist_id() {
        return artist_id;
    }

    public void setArtist_id(int artist_id) {
        this.artist_id = artist_id;
    }

    public String getArtist_name() {
        return artist_name;
    }

    public void setArtist_name(String artist_name) {
        this.artist_name = artist_name;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
