package com.white.streambeat.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.white.streambeat.Models.Albums;
import com.white.streambeat.R;

import java.util.List;
import java.util.Map;

public class UserLogsAdapter extends RecyclerView.Adapter<UserLogsAdapter.ViewHolder> {
    Context context;
    Map<String, List<Albums>> dateWithTracksMap;

    public UserLogsAdapter(Context context, Map<String, List<Albums>> dateWithTracksMap) {
        this.context = context;
        this.dateWithTracksMap = dateWithTracksMap;
    }

    @NonNull
    @Override
    public UserLogsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.listening_history_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserLogsAdapter.ViewHolder holder, int position) {
        String date = (String) dateWithTracksMap.keySet().toArray()[position];
        List<Albums> albums = dateWithTracksMap.get(date);

        holder.dateTextView.setText(date);

        PopularAlbumsAdapter albumsAdapter = new PopularAlbumsAdapter(context, albums, null);
        holder.tracksRecyclerView.setAdapter(albumsAdapter);
        holder.tracksRecyclerView.setLayoutManager(new LinearLayoutManager(context));
    }

    @Override
    public int getItemCount() {
        return dateWithTracksMap.size();
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
}
