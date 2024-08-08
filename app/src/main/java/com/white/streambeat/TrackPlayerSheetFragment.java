package com.white.streambeat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.palette.graphics.Palette;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.white.streambeat.Models.Tracks;

public class TrackPlayerSheetFragment extends BottomSheetDialogFragment {

    ImageView trackImage, playNextTrack, playPreviousTrack, arrowDownButton, btnMoreOptions;
    TextView trackName, artistNames, currentTime, totalTime;
    AppCompatSeekBar seekBar;
    FloatingActionButton playPauseButton;
    RelativeLayout sheetDialogMain;

    Tracks track;
    Handler handler = new Handler();
    Runnable updateProgressRunnable;

    int savedSeekBarPosition = -1;

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
        btnMoreOptions = view.findViewById(R.id.btnMoreOption);
        trackName = view.findViewById(R.id.trackTSName);
        artistNames = view.findViewById(R.id.trackTSArtistName);
        currentTime = view.findViewById(R.id.currentTimeTxt);
        totalTime = view.findViewById(R.id.totalTimeTxt);
        seekBar = view.findViewById(R.id.seekBar);
        sheetDialogMain = view.findViewById(R.id.sheetDialogMain);
        arrowDownButton = view.findViewById(R.id.arrowDownBtn);

        arrowDownButton.setOnClickListener(v -> dismiss());

        btnMoreOptions.setOnClickListener(v -> {
//            dismissAllBottomSheets();
            TrackMoreOptionsSheetFragment moreOptionsSheetFragment = new TrackMoreOptionsSheetFragment(track);
            moreOptionsSheetFragment.show(getParentFragmentManager(), "TrackMoreOptionsSheetFragment");
        });

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
                if (updateProgressRunnable != null) {
                    handler.removeCallbacks(updateProgressRunnable);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setUpdateProgressRunnable();
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

        updatePlayPauseButton();
        updateProgress(trackControlListener.getCurrentPosition(), trackControlListener.getDuration());

        playPauseButton.setOnClickListener(v -> {
            if (trackControlListener != null) {
                if (trackControlListener.isPlaying()) {
                    trackControlListener.pauseTrack();
                    savedSeekBarPosition = seekBar.getProgress();
                    Log.d("TrackPlayer", "Paused at position: " + savedSeekBarPosition);
                } else {
                    trackControlListener.playTrack();
                    if (savedSeekBarPosition != -1) {
                        seekBar.setProgress(savedSeekBarPosition);
                        Log.d("TrackPlayer", "Resumed at position: " + savedSeekBarPosition);
                    }
                }
                updatePlayPauseButton();
                updateProgress(trackControlListener.getCurrentPosition(), trackControlListener.getDuration());
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

                }
            });
            setUpdateProgressRunnable();
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
                    updateProgress(currentPosition, duration);
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(updateProgressRunnable);
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

    private void updateTrackDetails() {
        if (trackControlListener != null) {
            Tracks track = trackControlListener.getCurrentTrack();
            if (track != null) {
                trackName.setText(track.getTrack_name());
                artistNames.setText(TextUtils.join(", ", track.getArtist_names()));
                Picasso.get().load(track.getTrack_image_url())
                        .into(trackImage, new Callback() {
                            @Override
                            public void onSuccess() {
                                Bitmap bitmap = ((BitmapDrawable) trackImage.getDrawable()).getBitmap();
                                Palette.from(bitmap).generate(palette -> {
                                    if (palette != null) {
                                        int[] colors = new int[]{
                                                palette.getDarkVibrantColor(ContextCompat.getColor(requireContext(), R.color.white)),
                                                palette.getDarkMutedColor(ContextCompat.getColor(requireContext(), R.color.darkTheme))
                                        };
                                        GradientDrawable gradientDrawable = new GradientDrawable(
                                                GradientDrawable.Orientation.TL_BR,
                                                colors
                                        );
                                        sheetDialogMain.setBackground(gradientDrawable);
                                    }
                                });
                            }

                            @Override
                            public void onError(Exception e) {
                                sheetDialogMain.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.darkTheme));
                            }
                        });
                int currentPosition = trackControlListener.getCurrentPosition();
                int duration = trackControlListener.getDuration();
                seekBar.setMax(duration);
                if (savedSeekBarPosition != -1) {
                    seekBar.setProgress(savedSeekBarPosition);
                } else {
                    seekBar.setProgress(currentPosition);
                }
                updateProgress(currentPosition, duration);
            }
        }
    }
    public void dismissAllBottomSheets() {
        FragmentManager fragmentManager = getParentFragmentManager(); // Ensure using the correct FragmentManager
        if (fragmentManager != null) {
            for (Fragment fragment : fragmentManager.getFragments()) {
                if (fragment instanceof BottomSheetDialogFragment) {
                    ((BottomSheetDialogFragment) fragment).dismiss();
                }
            }
        }
    }

}