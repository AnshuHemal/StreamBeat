package com.white.streambeat;

import android.annotation.SuppressLint;
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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.white.streambeat.Activities.DashboardActivity;
import com.white.streambeat.Fragments.AlbumTracksFragment;
import com.white.streambeat.Models.Tracks;

import java.util.Objects;

public class TrackMoreOptionsSheetFragment extends BottomSheetDialogFragment {

    private static final String ARG_TRACK = "track";
    Tracks track;
    TextView sheetTrackName, sheetAlbumName, txtLikeTrackMoreSheet;
    ImageView sheetTrackImage;
    LinearLayout sheetViewAlbumLL, sheetViewArtistsLL, likeTrackBtnMoreSheet;

    private OnTrackUpdateListener trackUpdateListener;

    public TrackMoreOptionsSheetFragment(Tracks track) {
        this.track = track;
    }

    public static TrackMoreOptionsSheetFragment newInstance(Tracks track) {
        TrackMoreOptionsSheetFragment fragment = new TrackMoreOptionsSheetFragment(track);
        Bundle args = new Bundle();
        args.putParcelable(ARG_TRACK, track);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.track_more_options_sheet, container, false);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.small_push);

        sheetTrackImage = view.findViewById(R.id.sheetTrackImage);
        sheetTrackName = view.findViewById(R.id.sheetTrackName);
        sheetAlbumName = view.findViewById(R.id.sheetAlbumName);
        sheetViewAlbumLL = view.findViewById(R.id.sheetViewAlbumLL);
        sheetViewArtistsLL = view.findViewById(R.id.sheetViewArtistsLL);
        likeTrackBtnMoreSheet = view.findViewById(R.id.likeTrackBtnMoreSheet);
        txtLikeTrackMoreSheet = view.findViewById(R.id.txtLikeTrackMoreSheet);

        if (getArguments() != null) {
            track = getArguments().getParcelable(ARG_TRACK);
        }

        if (track != null) {
            updateTextLikedTrack();
            Glide.with(view).load(track.getTrack_image_url()).into(sheetTrackImage);
            sheetTrackName.setText(track.getTrack_name());
            sheetAlbumName.setText(track.getArtist_names().get(0) + " • " + track.getAlbum_title());

            sheetViewAlbumLL.setOnClickListener(v -> {
                v.startAnimation(animation);
                new Handler().postDelayed(() -> {
                    closeAllBottomSheets();
                    navigateToAlbumTracksFragment(getParentFragmentManager(), track.getAlbum_title());
                }, 100);
            });

            sheetViewArtistsLL.setOnClickListener(v -> {
                v.startAnimation(animation);
                new Handler().postDelayed(() -> navigateToArtistSheetFragment(track), 100);
            });

            likeTrackBtnMoreSheet.setOnClickListener(v -> {
                v.startAnimation(animation);
                new Handler().postDelayed(() -> {
                    if (!track.isLikedByUser()) {
                        ((DashboardActivity) requireContext()).addToLikedSongs(track, Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber());
                        track.setLikedByUser(true);
                        updateTextLikedTrack();
                        notifyTrackUpdated();
                    } else {
                        ((DashboardActivity) requireContext()).removeFromLikedSongs(track, Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber());
                        track.setLikedByUser(false);
                        updateTextLikedTrack();
                        notifyTrackUpdated();
                    }
                }, 100);
            });
        }

        return view;
    }

    @SuppressLint("SetTextI18n")
    private void updateTextLikedTrack() {
        if (track.isLikedByUser()) {
            txtLikeTrackMoreSheet.setText("Remove from Liked Songs");
        } else {
            txtLikeTrackMoreSheet.setText("Add to Liked Songs");
        }
    }

    private void navigateToArtistSheetFragment(Tracks track) {
        FragmentManager fragmentManager = getParentFragmentManager();
        for (Fragment fragment : fragmentManager.getFragments()) {
            if (fragment instanceof TrackMoreOptionsSheetFragment) {
                ((BottomSheetDialogFragment) fragment).dismiss();
            }
        }
        TrackViewArtistsSheetFragment artistsSheetFragment = new TrackViewArtistsSheetFragment(track);
        artistsSheetFragment.show(getParentFragmentManager(), "TrackViewArtistsSheetFragment");
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

    public void closeAllBottomSheets() {
        FragmentManager fragmentManager = getParentFragmentManager();
        for (Fragment fragment : fragmentManager.getFragments()) {
            if (fragment instanceof BottomSheetDialogFragment) {
                ((BottomSheetDialogFragment) fragment).dismiss();
            }
        }
    }
    public void setOnTrackUpdateListener(OnTrackUpdateListener listener) {
        this.trackUpdateListener = listener;
    }

    private void notifyTrackUpdated() {
        if (trackUpdateListener != null) {
            trackUpdateListener.onTrackUpdated(track);
        }
    }

    public interface OnTrackUpdateListener {
        void onTrackUpdated(Tracks updatedTrack);
    }
}