package com.white.streambeat.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.white.streambeat.Activities.DashboardActivity;
import com.white.streambeat.Fragments.AlbumTracksFragment;
import com.white.streambeat.Models.Albums;
import com.white.streambeat.Models.Artists;
import com.white.streambeat.Models.Tracks;
import com.white.streambeat.R;

import java.util.Collections;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ALBUM = 1;
    private static final int TYPE_ARTIST = 2;
    private static final int TYPE_TRACK = 3;

    private final Context context;
    List<Object> searchResults;
    FragmentManager fragmentManager;
    private int currentlyPlayingPosition = -1;

    public SearchAdapter(Context context, FragmentManager fragmentManager, List<Object> searchResults) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.searchResults = searchResults;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<Object> newSearchResults) {
        searchResults.clear();
        searchResults.addAll(newSearchResults);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setCurrentlyPlayingPosition(int position) {
        this.currentlyPlayingPosition = position;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(context);

        switch (viewType) {
            case TYPE_ALBUM:
                view = inflater.inflate(R.layout.search_album_layout, parent, false);
                viewHolder = new AlbumViewHolder(view);
                break;
            case TYPE_ARTIST:
                view = inflater.inflate(R.layout.search_artist_layout, parent, false);
                viewHolder = new ArtistViewHolder(view);
                break;
            case TYPE_TRACK:
                view = inflater.inflate(R.layout.search_track_layout, parent, false);
                viewHolder = new TrackViewHolder(view);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = searchResults.get(position);

        switch (holder.getItemViewType()) {
            case TYPE_ALBUM:
                configureAlbumViewHolder((AlbumViewHolder) holder, (Albums) item);
                break;
            case TYPE_ARTIST:
                configureArtistViewHolder((ArtistViewHolder) holder, (Artists) item);
                break;
            case TYPE_TRACK:
                configureTrackViewHolder((TrackViewHolder) holder, (Tracks) item, position);
                break;
        }
    }

    private void configureTrackViewHolder(TrackViewHolder holder, Tracks track, int position) {
        holder.trackName.setText(track.getTrack_name());

        StringBuilder artistsBuilder = new StringBuilder();
        for (int i=0; i<track.getArtist_names().size(); i++) {
            artistsBuilder.append(track.getArtist_names().get(i));
            if (i < track.getArtist_names().size() - 1) {
                artistsBuilder.append(", ");
            }
        }
        holder.trackArtists.setText(artistsBuilder.toString());
        Picasso.get().load(track.getTrack_image_url()).into(holder.trackImage);

        if (track.isLikedByUser()) {
            holder.trackBtnLike.setImageResource(R.drawable.added);
            holder.trackBtnLike.setColorFilter(ContextCompat.getColor(context, R.color.lightGreen));
        }

        if (position == currentlyPlayingPosition) {
            holder.trackName.setTextColor(ContextCompat.getColor(context, R.color.lightGreen));
        } else {
            holder.trackName.setTextColor(ContextCompat.getColor(context, R.color.lightWhite));
        }

        holder.itemView.setOnClickListener(v -> {
            Object item = searchResults.get(position);
            if (position != currentlyPlayingPosition) {
                if (item instanceof Tracks) {
                    Tracks clickedTrack = (Tracks) item;
                    ((DashboardActivity) context).playTracks(Collections.singletonList(clickedTrack), 0);
                    setCurrentlyPlayingPosition(position);
                } else {
                    Toast.makeText(context, "Invalid track item clicked", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void configureAlbumViewHolder(AlbumViewHolder holder, Albums album) {
        holder.albumName.setText(album.getAlbum_title());
        Picasso.get().load(album.getCover_image_url()).into(holder.albumCover);

        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                Object item = searchResults.get(adapterPosition);
                if (item instanceof Albums) {
                    Albums album1 = (Albums) item;
                    navigateToAlbumTracksFragment(fragmentManager, album1.getAlbum_title());
                }
            }
        });
    }

    private void configureArtistViewHolder(ArtistViewHolder holder, Artists artist) {
        holder.artistName.setText(artist.getArtist_name());
        Picasso.get().load(artist.getImage_url()).into(holder.artistImage);

        // Set click listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Artist", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    @Override
    public int getItemViewType(int position) {
        Object item = searchResults.get(position);
        if (item instanceof Albums) {
            return TYPE_ALBUM;
        } else if (item instanceof Artists) {
            return TYPE_ARTIST;
        } else if (item instanceof Tracks) {
            return TYPE_TRACK;
        }
        return -1;
    }

    public static class TrackViewHolder extends RecyclerView.ViewHolder {
        TextView trackName, trackArtists;
        ImageView trackImage, trackBtnLike;

        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            trackName = itemView.findViewById(R.id.trackSearchName);
            trackArtists = itemView.findViewById(R.id.trackSearchArtistsName);
            trackImage = itemView.findViewById(R.id.trackSearchImage);
            trackBtnLike = itemView.findViewById(R.id.btnLike);
        }
    }

    public static class AlbumViewHolder extends RecyclerView.ViewHolder {
        TextView albumName;
        ImageView albumCover;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            albumName = itemView.findViewById(R.id.albumSearchName);
            albumCover = itemView.findViewById(R.id.albumSearchImage);
        }
    }

    public static class ArtistViewHolder extends RecyclerView.ViewHolder {
        TextView artistName;
        ImageView artistImage;

        public ArtistViewHolder(@NonNull View itemView) {
            super(itemView);
            artistName = itemView.findViewById(R.id.artistSearchName);
            artistImage = itemView.findViewById(R.id.artistSearchImage);
        }
    }

    public void navigateToAlbumTracksFragment(FragmentManager fragmentManager, String album_title) {
        Bundle bundle = new Bundle();
        bundle.putString("album_title", album_title);

        AlbumTracksFragment albumTracksFragment = new AlbumTracksFragment();
        albumTracksFragment.setArguments(bundle);

        if (fragmentManager != null && !fragmentManager.isDestroyed()) {
            fragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, albumTracksFragment)
                    .commit();
        }
    }
}
