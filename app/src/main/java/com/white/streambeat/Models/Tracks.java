package com.white.streambeat.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Tracks implements Parcelable {
    int track_id;
    String track_name;
    String file_url;
    String track_image_url;
    List<String> artist_names;
    String album_title;
    boolean likedByUser;

    public Tracks(int track_id, String track_name, String file_url, String track_image_url, List<String> artist_names, String album_title) {
        this.track_id = track_id;
        this.track_name = track_name;
        this.file_url = file_url;
        this.track_image_url = track_image_url;
        this.artist_names = artist_names;
        this.album_title = album_title;
        likedByUser = false;
    }

    public boolean isLikedByUser() {
        return likedByUser;
    }

    public void setLikedByUser(boolean likedByUser) {
        this.likedByUser = likedByUser;
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

    protected Tracks(Parcel in) {
        track_name = in.readString();
        artist_names = in.createStringArrayList();
        track_image_url = in.readString();
        likedByUser = in.readByte() != 0;
    }

    public static final Creator<Tracks> CREATOR = new Creator<Tracks>() {
        @Override
        public Tracks createFromParcel(Parcel in) {
            return new Tracks(in);
        }

        @Override
        public Tracks[] newArray(int size) {
            return new Tracks[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(track_name);
        dest.writeStringList(artist_names);
        dest.writeString(track_image_url);
        dest.writeByte((byte) (likedByUser ? 1 : 0));
    }
}
