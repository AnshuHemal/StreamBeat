package com.white.streambeat.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.white.streambeat.Models.Artists;
import com.white.streambeat.R;

import java.util.List;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ViewHolder> {

    Context context;
    List<Artists> artistsList;

    public ArtistAdapter(Context context, List<Artists> artistsList) {
        this.context = context;
        this.artistsList = artistsList;
    }

    @NonNull
    @Override
    public ArtistAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_artist_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistAdapter.ViewHolder holder, int position) {
        Artists artist = artistsList.get(position);
        holder.artistName.setText(artist.getArtist_name());
        Glide.with(context).load(artist.getImage_url()).into(holder.artistImage);
    }

    @Override
    public int getItemCount() {
        return artistsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView artistName;
        ImageView artistImage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            artistName = itemView.findViewById(R.id.artistSearchName);
            artistImage = itemView.findViewById(R.id.artistSearchImage);
        }
    }
}
