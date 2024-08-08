package com.white.streambeat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.white.streambeat.Models.Tracks;

public class TrackMoreOptionsSheetFragment extends BottomSheetDialogFragment {

    Tracks track;
    TextView sheetTrackName, sheetAlbumName;
    ImageView sheetTrackImage;

    public TrackMoreOptionsSheetFragment(Tracks track) {
        this.track = track;
    }

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.track_more_options_sheet, container, false);

        sheetTrackImage = view.findViewById(R.id.sheetTrackImage);
        sheetTrackName = view.findViewById(R.id.sheetTrackName);
        sheetAlbumName = view.findViewById(R.id.sheetAlbumName);

        if (track != null) {
            Glide.with(view).load(track.getTrack_image_url()).into(sheetTrackImage);
            sheetTrackName.setText(track.getTrack_name());
            sheetAlbumName.setText(track.getArtist_names().get(0) + " • " + track.getAlbum_title());
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View bottomSheet = (View) view.getParent();
        if (bottomSheet != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO);
            behavior.setDraggable(true);
        }
    }
}
