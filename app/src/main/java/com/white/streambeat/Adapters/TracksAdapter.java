package com.white.streambeat.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;
import com.white.streambeat.Activities.DashboardActivity;
import com.white.streambeat.Models.Tracks;
import com.white.streambeat.R;
import com.white.streambeat.TrackMoreOptionsSheetFragment;

import java.util.ArrayList;
import java.util.List;

public class TracksAdapter extends RecyclerView.Adapter<TracksAdapter.ViewHolder> {

    Context context;
    List<Tracks> tracksList;
    private int currentlyPlayingPosition = -1;
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    public TracksAdapter(Context context) {
        this.context = context;
        this.tracksList = new ArrayList<>();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setTracksList(List<Tracks> tracksList) {
        this.tracksList = tracksList;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setCurrentlyPlayingPosition(int position) {
        this.currentlyPlayingPosition = position;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TracksAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_track_layout, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull TracksAdapter.ViewHolder holder, int position) {
        holder.trackName.setText(tracksList.get(position).getTrack_name());

        StringBuilder artistsBuilder = new StringBuilder();
        for (int i = 0; i < tracksList.get(position).getArtist_names().size(); i++) {
            artistsBuilder.append(tracksList.get(position).getArtist_names().get(i));
            if (i < tracksList.get(position).getArtist_names().size() - 1) {
                artistsBuilder.append(", ");
            }
        }
        holder.trackArtists.setText(artistsBuilder.toString());
        Picasso.get().load(tracksList.get(position).getTrack_image_url()).into(holder.trackImage);

        if (tracksList.get(position).isLikedByUser()) {
            holder.trackLikeBtn.setImageResource(R.drawable.added);
            holder.trackLikeBtn.setColorFilter(ContextCompat.getColor(context, R.color.lightGreen));
        } else {
            holder.trackLikeBtn.setImageResource(R.drawable.add_to_liked_songs);
            holder.trackLikeBtn.setColorFilter(ContextCompat.getColor(context, R.color.lightWhite));
        }

        if (position == currentlyPlayingPosition) {
            holder.trackName.setTextColor(ContextCompat.getColor(context, R.color.lightGreen));
        } else {
            holder.trackName.setTextColor(ContextCompat.getColor(context, R.color.lightWhite));
        }

        holder.llSearchItem.setOnClickListener(v -> {
            if (position != currentlyPlayingPosition) {
                ((DashboardActivity) context).playTracks(tracksList, position);
                setCurrentlyPlayingPosition(position);
            }
        });

        holder.trackLikeBtn.setOnClickListener(v -> {
            if (!tracksList.get(position).isLikedByUser()) {
                ((DashboardActivity) context).addToLikedSongs(tracksList.get(position), firebaseUser.getPhoneNumber());
                notifyDataSetChanged();
            } else {
                ((DashboardActivity) context).removeFromLikedSongs(tracksList.get(position), firebaseUser.getPhoneNumber());
                notifyDataSetChanged();
            }
        });

        holder.btnMoreOptions.setOnClickListener(v -> {
            TrackMoreOptionsSheetFragment optionsFragment = TrackMoreOptionsSheetFragment.newInstance(tracksList.get(position));
            if (context instanceof AppCompatActivity) {
                AppCompatActivity activity = (AppCompatActivity) context;
                FragmentManager fragmentManager = activity.getSupportFragmentManager();
                optionsFragment.show(fragmentManager, optionsFragment.getTag());
            } else {
                throw new IllegalStateException("Context must be an instance of AppCompatActivity");
            }
        });
    }

    @Override
    public int getItemCount() {
        return tracksList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView trackName, trackArtists;
        ImageView trackImage, trackLikeBtn, btnMoreOptions;
        LinearLayout llSearchItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            llSearchItem = itemView.findViewById(R.id.llSearchItem);
            trackName = itemView.findViewById(R.id.trackSearchName);
            trackArtists = itemView.findViewById(R.id.trackSearchArtistsName);
            trackImage = itemView.findViewById(R.id.trackSearchImage);
            trackLikeBtn = itemView.findViewById(R.id.btnLike);
            btnMoreOptions = itemView.findViewById(R.id.btnMoreOptions);
        }
    }
}