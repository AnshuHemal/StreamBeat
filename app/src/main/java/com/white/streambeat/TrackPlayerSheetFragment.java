package com.white.streambeat;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSeekBar;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.squareup.picasso.Picasso;
import com.white.streambeat.Models.Tracks;

public class TrackPlayerSheetFragment extends BottomSheetDialogFragment {

    ImageView trackImage, playPauseButton, playNextTrack, playPreviousTrack;
    TextView trackName, artistNames, currentTime, totalTime;
    AppCompatSeekBar seekBar;

    Tracks track;
    Handler handler = new Handler();
    Runnable updateProgressRunnable;

    OnTrackControlListener trackControlListener;

    public interface OnTrackControlListener {
        void playNextTrack();

        void playPreviousTrack();

        void playTrack();

        void pauseTrack();

        int getCurrentPosition();

        int getDuration();

        boolean isPlaying();

        void seekTo(int position);

        Tracks getCurrentTrack();

        int getRemainingDuration();
    }

    public TrackPlayerSheetFragment(Tracks track) {
        this.track = track;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.track_player_sheet, container, false);

        trackImage = view.findViewById(R.id.trackImage);
        playPauseButton = view.findViewById(R.id.playPauseButton);
        playNextTrack = view.findViewById(R.id.playNextTrack);
        playPreviousTrack = view.findViewById(R.id.playPreviousTrack);
        trackName = view.findViewById(R.id.trackTSName);
        artistNames = view.findViewById(R.id.trackTSArtistName);
        currentTime = view.findViewById(R.id.currentTimeTxt);
        totalTime = view.findViewById(R.id.totalTimeTxt);
        seekBar = view.findViewById(R.id.seekBar);

        updateTrackDetails();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && trackControlListener != null) {
                    trackControlListener.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(updateProgressRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                handler.post(updateProgressRunnable);
            }
        });

        playNextTrack.setOnClickListener(v -> {
            if (trackControlListener != null) {
                trackControlListener.playNextTrack();
                updateTrackDetails();
            }
        });

        playPreviousTrack.setOnClickListener(v -> {
            if (trackControlListener != null) {
                trackControlListener.playPreviousTrack();
                updateTrackDetails();
            }
        });

        updateUI();

        playPauseButton.setOnClickListener(v -> {
            if (trackControlListener != null) {
                if (trackControlListener.isPlaying()) {
                    trackControlListener.pauseTrack();
                } else {
                    trackControlListener.playTrack();
                    seekBar.setProgress(trackControlListener.getCurrentPosition());
                }
                updateUI();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        View parent = (View) requireView().getParent();
        if (parent != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(parent);
            behavior.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO);
            behavior.setHideable(true);
            behavior.setDraggable(true);

            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    if (newState == BottomSheetBehavior.STATE_SETTLING) {
                        dismiss();
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    // Optional: Handle sliding if needed
                }
            });
            if (updateProgressRunnable != null) {
                handler.post(updateProgressRunnable);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (updateProgressRunnable != null) {
            handler.removeCallbacks(updateProgressRunnable);
        }
    }

    public void updateProgress(int currentPos, int duration) {
        if (seekBar != null) {
            seekBar.setMax(duration);
            seekBar.setProgress(currentPos);
            currentTime.setText(formatTime(currentPos));
            totalTime.setText(formatTime(duration));
        }
    }

    private String formatTime(int milliseconds) {
        int minutes = (milliseconds / 1000) / 60;
        int seconds = (milliseconds / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void setUpdateProgressRunnable() {
        this.updateProgressRunnable = new Runnable() {
            @Override
            public void run() {
                if (trackControlListener != null) {
                    int currentPosition = trackControlListener.getCurrentPosition();
                    int duration = trackControlListener.getDuration();
                    int remainingTime = trackControlListener.getRemainingDuration();
                    updateProgress(currentPosition, duration);
                    if (remainingTime <= 1000) {
                        trackControlListener.playNextTrack();
                        return;
                    }
                    handler.postDelayed(this, 1000);
                }
            }
        };
        if (handler != null) {
            handler.post(updateProgressRunnable);
        }
    }

    private void updatePlayPauseButton() {
        if (trackControlListener.isPlaying()) {
            playPauseButton.setImageResource(R.drawable.pause_track);
        } else {
            playPauseButton.setImageResource(R.drawable.play_track);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null && updateProgressRunnable != null) {
            handler.removeCallbacks(updateProgressRunnable);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnTrackControlListener) {
            trackControlListener = (OnTrackControlListener) context;
        } else {
            throw new ClassCastException(context + " must implement OnTrackControlListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        trackControlListener = null;
    }

    private void updateUI() {
        updatePlayPauseButton();
        if (trackControlListener != null) {
            int currentPosition = trackControlListener.getCurrentPosition();
            int duration = trackControlListener.getDuration();
            updateProgress(currentPosition, duration);
        }
    }

    private void updateTrackDetails() {
        if (trackControlListener != null) {
            Tracks track = trackControlListener.getCurrentTrack();
            if (track != null) {
                trackName.setText(track.getTrack_name());
                StringBuilder artistsBuilder = new StringBuilder();
                for (int i = 0; i < track.getArtist_names().size(); i++) {
                    artistsBuilder.append(track.getArtist_names().get(i));
                    if (i < track.getArtist_names().size() - 1) {
                        artistsBuilder.append(", ");
                    }
                }
                artistNames.setText(artistsBuilder.toString());
                Picasso.get().load(track.getTrack_image_url()).into(trackImage);
            }
        }
    }
}