package com.white.streambeat.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.white.streambeat.Fragments.AlbumTracksFragment;
import com.white.streambeat.Models.Albums;
import com.white.streambeat.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UserLogsAdapter extends RecyclerView.Adapter<UserLogsAdapter.ViewHolder> {
    private final Context context;
    private final Map<String, List<Albums>> dateWithAlbumsMap;
    FragmentManager fragmentManager;

    public UserLogsAdapter(Context context, Map<String, List<Albums>> dateWithAlbumsMap, FragmentManager fragmentManager) {
        this.context = context;
        this.dateWithAlbumsMap = dateWithAlbumsMap;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public UserLogsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.listening_history_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserLogsAdapter.ViewHolder holder, int position) {

        String date = new ArrayList<>(dateWithAlbumsMap.keySet()).get(position);
        List<Albums> albums = dateWithAlbumsMap.get(date);

        holder.dateTextView.setText(date);

        SearchAdapter searchAdapter = new SearchAdapter(context, null, Arrays.asList(Objects.requireNonNull(albums).toArray()));
        holder.tracksRecyclerView.setAdapter(searchAdapter);
        holder.tracksRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        searchAdapter.setOnItemClickListener(album -> navigateToAlbumTracksFragment(fragmentManager, album.getAlbum_title()));
    }

    @Override
    public int getItemCount() {
        return dateWithAlbumsMap.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        RecyclerView tracksRecyclerView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            dateTextView = itemView.findViewById(R.id.userLogsDate);
            tracksRecyclerView = itemView.findViewById(R.id.userLogRV);
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
