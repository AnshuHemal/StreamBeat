package com.white.streambeat.Models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.white.streambeat.Connections.ServerConnector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<List<Object>> searchResults = new MutableLiveData<>();
    private final MutableLiveData<List<Integer>> albumIdsList = new MutableLiveData<>();

    public void setAlbumIdsList(List<Integer> albumIds) {
        albumIdsList.setValue(albumIds);
    }

    public LiveData<List<Tracks>> getTracksForAlbum(String albumTitle) {
        MutableLiveData<List<Tracks>> filteredTracks = new MutableLiveData<>();
        List<Tracks> allTracks = ServerConnector.allTracksList;

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

    public void search(String query) {
        List<Object> results = new ArrayList<>();
        String lowerCaseQuery = query.toLowerCase();

        List<Artists> artists = ServerConnector.allArtistsList;
        List<Albums> albums = ServerConnector.allAlbumsList;
        List<Tracks> tracks = ServerConnector.allTracksList;

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