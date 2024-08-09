package com.white.streambeat.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.white.streambeat.Models.Artists;
import com.white.streambeat.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FavArtistsAdapter extends RecyclerView.Adapter<FavArtistsAdapter.ViewHolder> {
    Context context;
    List<Artists> artistsList;

    public FavArtistsAdapter(Context context, List<Artists> artistsList) {
        this.context = context;
        this.artistsList = artistsList;
    }

    @NonNull
    @Override
    public FavArtistsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.home_fav_artists, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavArtistsAdapter.ViewHolder holder, int position) {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.small_push);
        holder.homeArtistName.setText(artistsList.get(position).getArtist_name());
        Glide.with(context).load(artistsList.get(position).getImage_url()).into(holder.homeArtistImage);
        holder.homeArtistLL.setOnClickListener(v -> v.startAnimation(animation));
    }

    @Override
    public int getItemCount() {
        return artistsList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateArtistsList(List<Artists> newArtistsList) {
        this.artistsList = newArtistsList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView homeArtistName;
        CircleImageView homeArtistImage;
        LinearLayout homeArtistLL;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            homeArtistName = itemView.findViewById(R.id.homeArtistName);
            homeArtistImage = itemView.findViewById(R.id.homeArtistImage);
            homeArtistLL = itemView.findViewById(R.id.homeArtistLL);
        }
    }
}
