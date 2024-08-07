package com.white.streambeat.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.white.streambeat.Models.Artists;
import com.white.streambeat.R;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupArtistAdapter extends RecyclerView.Adapter<SetupArtistAdapter.ViewHolder> {
    List<Artists> artistsList;
    Context context;
    private final List<Artists> selectedArtists = new ArrayList<>();

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

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<Artists> newSearchResults) {
        artistsList.clear();
        artistsList.addAll(newSearchResults);
        notifyDataSetChanged();
    }

    public List<Artists> getSelectedArtists() {
        return selectedArtists;
    }

    @Override
    public void onBindViewHolder(@NonNull SetupArtistAdapter.ViewHolder holder, int position) {
        Artists artist = artistsList.get(position);
        holder.bind(artist);
    }

    @Override
    public int getItemCount() {
        return artistsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout artistContainer;
        private final CircleImageView artistImage;
        private final TextView artistName;
        private final CheckBox checkboxArtist;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            artistContainer = itemView.findViewById(R.id.artist_container);
            artistImage = itemView.findViewById(R.id.artist_image);
            artistName = itemView.findViewById(R.id.artist_name);
            checkboxArtist = itemView.findViewById(R.id.checkbox_artist);
        }

        public void bind(Artists artist) {
            artistName.setText(artist.getArtist_name());

            Picasso.get().load(artist.getImage_url()).into(artistImage);
            checkboxArtist.setChecked(artist.isSelected());

            // Set a listener to update the artist's selection state
            checkboxArtist.setOnCheckedChangeListener((buttonView, isChecked) -> {
                artist.setSelected(isChecked);
                updateSelectedArtists(artist, isChecked);
            });
        }

        private void updateSelectedArtists(Artists artist, boolean isChecked) {
            if (isChecked && !selectedArtists.contains(artist)) {
                selectedArtists.add(artist);
            } else {
                selectedArtists.remove(artist);
            }
        }
    }
}
