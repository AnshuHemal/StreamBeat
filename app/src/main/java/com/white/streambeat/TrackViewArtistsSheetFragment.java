package com.white.streambeat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.white.streambeat.Adapters.ArtistAdapter;
import com.white.streambeat.Connections.ServerConnector;
import com.white.streambeat.Models.Artists;
import com.white.streambeat.Models.Tracks;

import java.util.ArrayList;
import java.util.List;

public class TrackViewArtistsSheetFragment extends BottomSheetDialogFragment {

    Tracks tracks;
    List<Artists> selectedArtists = new ArrayList<>();

    public TrackViewArtistsSheetFragment(Tracks tracks) {
        this.tracks = tracks;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.track_view_artists_sheet, container, false);

        RecyclerView artistsRecyclerView = view.findViewById(R.id.artistsRecyclerView);
        artistsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (tracks != null && tracks.getArtist_names() != null) {
            List<Artists> artistsList = new ArrayList<>(ServerConnector.allArtistsList);

            for (Artists artist : artistsList) {
                if (artist != null && artist.getArtist_name() != null) {
                    for (String artistName : tracks.getArtist_names()) {
                        if (artist.getArtist_name().equals(artistName)) {
                            selectedArtists.add(artist);
                        }
                    }
                }
            }
            ArtistAdapter adapter = new ArtistAdapter(getContext(), selectedArtists);
            artistsRecyclerView.setAdapter(adapter);
        } else {
            Log.e("TrackViewArtists", "Tracks or artist names are null.");
        }

        return view;
    }
}
