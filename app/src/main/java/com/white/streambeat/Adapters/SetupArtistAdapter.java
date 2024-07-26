package com.white.streambeat.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
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
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return (int) Math.ceil((double) artistsList.size() / 3);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout artist1Container, artist2Container, artist3Container;
        CircleImageView artistImage1, artistImage2, artistImage3;
        TextView artistName1, artistName2, artistName3;
        CheckBox checkboxArtist1, checkboxArtist2, checkboxArtist3;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            artist1Container = itemView.findViewById(R.id.artist1_container);
            artist2Container = itemView.findViewById(R.id.artist2_container);
            artist3Container = itemView.findViewById(R.id.artist3_container);
            artistImage1 = itemView.findViewById(R.id.artist_image1);
            artistImage2 = itemView.findViewById(R.id.artist_image2);
            artistImage3 = itemView.findViewById(R.id.artist_image3);

            artistName1 = itemView.findViewById(R.id.artist_name1);
            artistName2 = itemView.findViewById(R.id.artist_name2);
            artistName3 = itemView.findViewById(R.id.artist_name3);

            checkboxArtist1 = itemView.findViewById(R.id.checkbox_artist1);
            checkboxArtist2 = itemView.findViewById(R.id.checkbox_artist2);
            checkboxArtist3 = itemView.findViewById(R.id.checkbox_artist3);

            // Set the tag to identify the ViewHolder in onClickListener
            itemView.setTag(this);
        }
        public void bind(int position) {
            int startIndex = position * 3;

            bindArtist(artist1Container, artistName1, artistImage1, checkboxArtist1, startIndex);
            bindArtist(artist2Container, artistName2, artistImage2, checkboxArtist2, startIndex + 1);
            bindArtist(artist3Container, artistName3, artistImage3, checkboxArtist3, startIndex + 2);
        }

        private void bindArtist(LinearLayout artistContainer, TextView artistName, CircleImageView artistImage,
                                CheckBox checkboxArtist, int index) {
            if (index < artistsList.size()) {
                Artists artist = artistsList.get(index);
                artistName.setText(artist.getArtist_name());

//                byte[] imageBytes = Base64.decode(artist.getImage_url(), Base64.DEFAULT);
//                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
//                artistImage.setImageBitmap(bitmap);
                Picasso.get().load(artistsList.get(index).getImage_url()).into(artistImage);

                checkboxArtist.setChecked(artist.isSelected());
                artistContainer.setVisibility(View.VISIBLE);

                // Set click listener to handle checkbox selection
                checkboxArtist.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    artist.setSelected(isChecked);
                    updateSelectedArtists(artist, isChecked);
                });
            } else {
                // Hide the container if no artist exists at this index
                artistContainer.setVisibility(View.INVISIBLE);
            }
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
