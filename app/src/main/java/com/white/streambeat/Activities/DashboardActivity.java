package com.white.streambeat.Activities;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;

import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.white.streambeat.Fragments.AlbumTracksFragment;
import com.white.streambeat.Fragments.ExploreFragment;
import com.white.streambeat.Fragments.HomeFragment;
import com.white.streambeat.Fragments.LibraryFragment;
import com.white.streambeat.Fragments.ProfileFragment;
import com.white.streambeat.Models.SharedViewModel;
import com.white.streambeat.Models.Tracks;
import com.white.streambeat.R;
import com.white.streambeat.databinding.ActivityDashboardBinding;

import java.io.IOException;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    ActivityDashboardBinding binding;
    FrameLayout frameLayout;
    Fragment currentFragment;
    SharedViewModel sharedViewModel;

    View miniPlayerView;
    TextView miniPlayerTitle, miniPlayerArtistsNames;
    ImageView miniPlayerPlayPause, miniPlayerShuffle;

    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    Tracks currentTrack;
    LinearProgressIndicator trackProgressIndicator;
    private Handler handler;
    private Runnable updateProgressRunnable;
    private List<Tracks> tracksList;
    private int currentTrackPosition = -1;
    String bluetoothDevice = "";

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    // Device connected, show toast with device name
                    bluetoothDevice = device.getName();
                    if (bluetoothDevice != null) {
                        Toast.makeText(context, "Bluetooth connected with " + bluetoothDevice, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        miniPlayerView = findViewById(R.id.track_mini_player_bar);
        miniPlayerTitle = findViewById(R.id.trackMPName);
        miniPlayerArtistsNames = findViewById(R.id.trackMPArtistName);
        miniPlayerPlayPause = findViewById(R.id.btnPlayPause);
        miniPlayerShuffle = findViewById(R.id.shuffle);
        trackProgressIndicator = findViewById(R.id.trackProgressIndicator);

        miniPlayerView.setVisibility(View.GONE);

        frameLayout = findViewById(R.id.frameLayout);
        currentFragment = new HomeFragment();
        loadFragment(currentFragment, false);

        binding.bottomNavBar.setOnNavigationItemSelectedListener(item -> {
            Fragment fragment;

            if (item.getItemId() == R.id.navHome) {
                fragment = new HomeFragment();
            } else if (item.getItemId() == R.id.navExplore) {
                fragment = new ExploreFragment();
            } else if (item.getItemId() == R.id.navLibrary) {
                fragment = new LibraryFragment();
            } else {
                fragment = new ProfileFragment();
            }
            currentFragment = fragment;
            loadFragment(fragment, false);
            return true;
        });

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        registerReceiver(bluetoothReceiver, filter);
    }

    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();
    }

    public void onBackPressed() {
        if (currentFragment instanceof HomeFragment) {
            super.onBackPressed();
        } else {
            loadFragment(new HomeFragment(), false);
            currentFragment = new HomeFragment();
            binding.bottomNavBar.setSelectedItemId(R.id.navHome);
        }
    }

    public void playTracks(List<Tracks> tracksList, int position) {
        this.tracksList = tracksList;
        this.currentTrackPosition = position;
        playCurrentTrack();
    }

    private void playCurrentTrack() {
        if (currentTrackPosition >= 0 && currentTrackPosition < tracksList.size()) {
            currentTrack = tracksList.get(currentTrackPosition);
            showMiniPlayer(currentTrack);
            handler = new Handler();
            updateProgressRunnable = new Runnable() {
                @Override
                public void run() {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    trackProgressIndicator.setProgress(currentPosition);
                    trackProgressIndicator.setMax(mediaPlayer.getDuration());
                    handler.postDelayed(this, 1000);
                }
            };
            handler.post(updateProgressRunnable);

            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener(mp -> {
                mediaPlayer.start();
                isPlaying = true;
                miniPlayerPlayPause.setImageResource(R.drawable.pause_track);
                handler.post(updateProgressRunnable);
            });

            if (mediaPlayer.isPlaying()) {
                miniPlayerPlayPause.setImageResource(R.drawable.pause_track);
            } else {
                miniPlayerPlayPause.setImageResource(R.drawable.play_track);
            }

            try {
                mediaPlayer.setDataSource("https://onlinetestcase.com/wp-content/uploads/2023/06/500-KB-MP3.mp3");
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void showMiniPlayer(Tracks tracks) {
        miniPlayerTitle.setText(tracks.getTrack_name());
        StringBuilder artistsBuilder = new StringBuilder();
        for (int i = 0; i < tracks.getArtist_names().size(); i++) {
            artistsBuilder.append(tracks.getArtist_names().get(i));
            if (i < tracks.getArtist_names().size() - 1) {
                artistsBuilder.append(", ");
            }
        }
        miniPlayerArtistsNames.setText(artistsBuilder.toString());
        miniPlayerView.setVisibility(View.VISIBLE);

        miniPlayerPlayPause.setOnClickListener(v -> togglePlayback());
        miniPlayerShuffle.setOnClickListener(v -> playNextTrack());
    }

    private void togglePlayback() {
        if (mediaPlayer.isPlaying()) {
            pauseTrack();
        } else {
            playTrack();
        }
    }

    private void pauseTrack() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.pause();
            miniPlayerPlayPause.setImageResource(R.drawable.play_track);
            isPlaying = false;
            handler.removeCallbacks(updateProgressRunnable);
        }
    }

    private void playTrack() {
        if (mediaPlayer != null && !isPlaying) {
            mediaPlayer.start();
            miniPlayerPlayPause.setImageResource(R.drawable.pause_track);
            isPlaying = true;
            handler.post(updateProgressRunnable);
        }
    }

    private void playNextTrack() {
        if (tracksList != null && !tracksList.isEmpty()) {
            currentTrackPosition++;
            if (currentTrackPosition >= tracksList.size()) {
                currentTrackPosition = 0;
            }
            updateAlbumTracksAdapter();
            updateSearchAdapter();
            playCurrentTrack();
            showMiniPlayer(tracksList.get(currentTrackPosition));
        }
    }

    private void updateAlbumTracksAdapter() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);
        if (fragment instanceof AlbumTracksFragment) {
            ((AlbumTracksFragment) fragment).updateCurrentlyPlayingPosition(currentTrackPosition);
        }
    }

    private void updateSearchAdapter() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);
        if (fragment instanceof ExploreFragment) {
            ((ExploreFragment) fragment).updateCurrentlyPlayingPosition(currentTrackPosition);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateProgressRunnable);
    }
}