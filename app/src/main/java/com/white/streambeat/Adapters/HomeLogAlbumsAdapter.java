package com.white.streambeat.Adapters;

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

public class HomeLogAlbumsAdapter extends RecyclerView.Adapter<HomeLogAlbumsAdapter.ViewHolder> {

    Context context;
    List<Albums> albums;
    FragmentManager fragmentManager;

    public HomeLogAlbumsAdapter(Context context, List<Albums> albums, FragmentManager fragmentManager) {
        this.context = context;
        this.albums = albums;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public HomeLogAlbumsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.home_log_albums_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeLogAlbumsAdapter.ViewHolder holder, int position) {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.small_push);
        Albums album = albums.get(position);
        holder.homeLogAlbumsTV.setText(album.getAlbum_title());
        Glide.with(context).load(album.getCover_image_url()).into(holder.homeLogAlbumsIV);

        holder.homeLogAlbumsLL.setOnClickListener(v -> {
            v.startAnimation(animation);
            new Handler().postDelayed(() -> navigateToAlbumTracksFragment(fragmentManager, album.getAlbum_title(), album.getCover_image_url()), 100);
        });
    }

    @Override
    public int getItemCount() {
        return Math.min(albums.size(), 6);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout homeLogAlbumsLL;
        ImageView homeLogAlbumsIV;
        TextView homeLogAlbumsTV;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            homeLogAlbumsLL = itemView.findViewById(R.id.homeLogAlbumLL);
            homeLogAlbumsTV = itemView.findViewById(R.id.homeLogALbumTV);
            homeLogAlbumsIV = itemView.findViewById(R.id.homeLogAlbumIV);
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
                    .commit();
        }
    }
}
