package com.white.streambeat.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.white.streambeat.Models.Artists;
import com.white.streambeat.R;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupArtistAdapter extends RecyclerView.Adapter<SetupArtistAdapter.ViewHolder> {
    List<Artists> artistsList;
    Context context;

    public SetupArtistAdapter(List<Artists> artistsList, Context context) {
        this.artistsList = artistsList;
        this.context = context;
    }

    @NonNull
    @Override
    public SetupArtistAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_artists_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SetupArtistAdapter.ViewHolder holder, int position) {
        Artists artist = artistsList.get(position);
        holder.artistName.setText(artist.getArtist_name());
        Glide.with(context).load(artist.getImage_url()).into(holder.artistImage);

        holder.artistContainer.setOnClickListener(v -> {
            boolean isSelected = !artist.isSelected();
            artist.setSelected(isSelected);
            holder.tickImage.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        });

        holder.tickImage.setVisibility(artist.isSelected() ? View.VISIBLE : View.GONE);
    }

    public List<Artists> getSelectedArtists() {
        List<Artists> selectedArtists = new ArrayList<>();
        for (Artists artist : artistsList) {
            if (artist.isSelected()) {
                selectedArtists.add(artist);
            }
        }
        return selectedArtists;
    }

    @Override
    public int getItemCount() {
        return artistsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout artistContainer;
        private final CircleImageView artistImage;
        private final TextView artistName;
        private final ImageView tickImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            artistContainer = itemView.findViewById(R.id.artist_container);
            artistImage = itemView.findViewById(R.id.artist_image);
            artistName = itemView.findViewById(R.id.artist_name);
            tickImage = itemView.findViewById(R.id.tick_image);
        }
    }
}
