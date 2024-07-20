package com.white.streambeat.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.white.streambeat.Models.Tracks;
import com.white.streambeat.R;

import java.util.List;

public class TracksAdapter extends RecyclerView.Adapter<TracksAdapter.ViewHolder> {

    Context context;
    List<Tracks> tracksList;

    public TracksAdapter(Context context, List<Tracks> tracksList) {
        this.context = context;
        this.tracksList = tracksList;
    }

    @NonNull
    @Override
    public TracksAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_track_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TracksAdapter.ViewHolder holder, int position) {
        holder.trackName.setText(tracksList.get(position).getTrack_name());

        StringBuilder artistsBuilder = new StringBuilder();
        for (int i=0; i < tracksList.get(position).getArtist_names().size(); i++) {
            artistsBuilder.append(tracksList.get(position).getArtist_names().get(i));
            if (i < tracksList.get(position).getArtist_names().size() - 1) {
                artistsBuilder.append(", ");
            }
        }
        holder.trackArtists.setText(artistsBuilder.toString());
        Picasso.get().load(tracksList.get(position).getTrack_image_url()).into(holder.trackImage);
    }

    @Override
    public int getItemCount() {
        return tracksList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView trackName, trackArtists;
        ImageView trackImage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            trackName = itemView.findViewById(R.id.trackSearchName);
            trackArtists = itemView.findViewById(R.id.trackSearchArtistsName);
            trackImage = itemView.findViewById(R.id.trackSearchImage);
        }
    }
}
