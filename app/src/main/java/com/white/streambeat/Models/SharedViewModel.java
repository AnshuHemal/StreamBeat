package com.white.streambeat.Models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<List<Artists>> allArtistsList = new MutableLiveData<>();
    private final MutableLiveData<List<Albums>> allAlbumsList = new MutableLiveData<>();
    private final MutableLiveData<List<Tracks>> allTracksList = new MutableLiveData<>();
    private final MutableLiveData<List<Artists>> artistList = new MutableLiveData<>();
    private final MutableLiveData<List<Object>> searchResults = new MutableLiveData<>();

/*    public MutableLiveData<List<Tracks>> getAllTracksList() {
        return allTracksList;
    }

    public LiveData<List<Artists>> getAllArtistsList() {
        return allArtistsList;
    }

    public LiveData<List<Albums>> getAllAlbumsList() {
        return allAlbumsList;
    }

    public MutableLiveData<List<Artists>> getArtistList() {
        return artistList;
    }*/

    public LiveData<List<Tracks>> getTracksForAlbum(String albumTitle) {
        MutableLiveData<List<Tracks>> filteredTracks = new MutableLiveData<>();
        List<Tracks> allTracks = allTracksList.getValue();

        if (allTracks != null) {
            List<Tracks> tracksForAlbum = allTracks.stream()
                    .filter(track -> track.getAlbum_title().equals(albumTitle))
                    .collect(Collectors.toList());
            filteredTracks.setValue(tracksForAlbum);
        } else {
            filteredTracks.setValue(new ArrayList<>());
        }

        return filteredTracks;
    }

    public LiveData<List<Object>> getSearchResults() {
        return searchResults;
    }

    public void setAllArtistsList(List<Artists> artists) {
        allArtistsList.setValue(artists);
    }

    public void setAllAlbumsList(List<Albums> albums) {
        allAlbumsList.setValue(albums);
    }

    public void setAllTracksList(List<Tracks> tracks) {
        allTracksList.setValue(tracks);
    }

    public void search(String query) {
        List<Object> results = new ArrayList<>();
        String lowerCaseQuery = query.toLowerCase();

        List<Artists> artists = allArtistsList.getValue();
        List<Albums> albums = allAlbumsList.getValue();
        List<Tracks> tracks = allTracksList.getValue();

        if (artists != null) {
            List<Artists> filteredArtists = artists.stream()
                    .filter(artist -> artist.getArtist_name().toLowerCase().contains(lowerCaseQuery))
                    .collect(Collectors.toList());
            results.addAll(filteredArtists);

            if (tracks != null) {
                for (Artists artist : filteredArtists) {
                    List<Tracks> artistTracks = tracks.stream()
                            .filter(track -> track.getArtist_names().contains(artist.getArtist_name()))
                            .collect(Collectors.toList());
                    results.addAll(artistTracks);
                }
            }
        }

        if (albums != null) {
            results.addAll(albums.stream()
                    .filter(album -> album.getAlbum_title().toLowerCase().contains(lowerCaseQuery))
                    .collect(Collectors.toList()));
        }

        if (tracks != null) {
            results.addAll(tracks.stream()
                    .filter(tracks1 -> tracks1.getTrack_name().toLowerCase().contains(lowerCaseQuery))
                    .collect(Collectors.toList()));
        }

        searchResults.setValue(results);
    }

    public void clearSearchResults() {
        searchResults.setValue(new ArrayList<>());
    }

}
