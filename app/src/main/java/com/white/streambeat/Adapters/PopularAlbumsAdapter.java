package com.white.streambeat.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.white.streambeat.Fragments.AlbumTracksFragment;
import com.white.streambeat.Models.Albums;
import com.white.streambeat.R;

import java.util.List;

public class PopularAlbumsAdapter extends RecyclerView.Adapter<PopularAlbumsAdapter.ViewHolder> {

    Context context;
    List<Albums> albumsList;
    FragmentManager fragmentManager;

    public PopularAlbumsAdapter(Context context, List<Albums> albumsList, FragmentManager fragmentManager) {
        this.context = context;
        this.albumsList = albumsList;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public PopularAlbumsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.home_albums, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PopularAlbumsAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.small_push);
        holder.homeAlbumTextName.setText(albumsList.get(position).getAlbum_title());
        Glide.with(context).load(albumsList.get(position).getCover_image_url()).into(holder.homeAlbumImage);

        holder.homeAlbumLL.setOnClickListener(v -> {
            v.startAnimation(animation);
            new Handler().postDelayed(() -> navigateToAlbumTracksFragment(fragmentManager, albumsList.get(position).getAlbum_title(), albumsList.get(position).getCover_image_url()), 100);
        });
    }

    @Override
    public int getItemCount() {
        return albumsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout homeAlbumLL;
        TextView homeAlbumTextName;
        ImageView homeAlbumImage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            homeAlbumTextName = itemView.findViewById(R.id.homeBestOfArtistsAlbumName);
            homeAlbumImage = itemView.findViewById(R.id.homeBestOfArtistsAlbumImage);
            homeAlbumLL = itemView.findViewById(R.id.homeBestOfArtistsAlbumLL);
        }
    }
    public void navigateToAlbumTracksFragment(FragmentManager fragmentManager, String album_title, String album_image_url) {
        Bundle bundle = new Bundle();
        bundle.putString("album_title", album_title);
        bundle.putString("album_image_url", album_image_url);


        AlbumTracksFragment albumTracksFragment = new AlbumTracksFragment();
        albumTracksFragment.setArguments(bundle);

        if (fragmentManager != null && !fragmentManager.isDestroyed()) {
            fragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, albumTracksFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}
