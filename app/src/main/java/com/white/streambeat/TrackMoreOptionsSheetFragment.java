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
import com.white.streambeat.Fragments.AlbumTracksFragment;
import com.white.streambeat.Models.Tracks;

public class TrackMoreOptionsSheetFragment extends BottomSheetDialogFragment {

    private static final String ARG_TRACK = "track";
    Tracks track;
    TextView sheetTrackName, sheetAlbumName;
    ImageView sheetTrackImage;
    LinearLayout sheetViewAlbumLL, sheetViewArtistsLL;

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

        if (getArguments() != null) {
            track = getArguments().getParcelable(ARG_TRACK);  // Use Parcelable if possible
        }

        if (track != null) {
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
        }

        return view;
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
}