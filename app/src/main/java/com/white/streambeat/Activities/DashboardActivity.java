package com.white.streambeat.Activities;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;

import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.palette.graphics.Palette;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.white.streambeat.Connections.ServerConnector;
import com.white.streambeat.Fragments.AlbumTracksFragment;
import com.white.streambeat.Fragments.ExploreFragment;
import com.white.streambeat.Fragments.HomeFragment;
import com.white.streambeat.Fragments.LibraryFragment;
import com.white.streambeat.Fragments.ProfileFragment;
import com.white.streambeat.Models.SharedViewModel;
import com.white.streambeat.Models.Tracks;
import com.white.streambeat.R;
import com.white.streambeat.TrackPlayerSheetFragment;
import com.white.streambeat.databinding.ActivityDashboardBinding;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DashboardActivity extends AppCompatActivity implements TrackPlayerSheetFragment.OnTrackControlListener{

    ActivityDashboardBinding binding;
    FrameLayout frameLayout;
    Fragment currentFragment;
    SharedViewModel sharedViewModel;
    FirebaseUser firebaseUser;

    View miniPlayerView;
    TextView miniPlayerTitle, miniPlayerArtistsNames;
    ImageView miniPlayerPlayPause, miniPlayerShuffle, miniPlayerImage;

    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    Tracks currentTrack;
    LinearProgressIndicator trackProgressIndicator;
    private Handler handler;
    private Runnable updateProgressRunnable;
    private List<Tracks> tracksList;
    private int currentTrackPosition = -1;
    String bluetoothDevice = "";

    public void onLikeButtonClick(Tracks track, String phoneNumber) {
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                ServerConnector.STORE_LIKED_TRACKS,
                response -> Toast.makeText(DashboardActivity.this, response, Toast.LENGTH_SHORT).show(), error -> Toast.makeText(DashboardActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> map = new HashMap<>();
                map.put("user_phone", phoneNumber);
                map.put("track_name", track.getTrack_name());
                return map;
            }
        };
        Volley.newRequestQueue(getApplicationContext()).add(stringRequest);
    }

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
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
        Objects.requireNonNull(getSupportActionBar()).hide();

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        miniPlayerView = findViewById(R.id.track_mini_player_bar);
        miniPlayerTitle = findViewById(R.id.trackMPName);
        miniPlayerArtistsNames = findViewById(R.id.trackMPArtistName);
        miniPlayerPlayPause = findViewById(R.id.btnPlayPause);
        miniPlayerShuffle = findViewById(R.id.shuffle);
        miniPlayerImage = findViewById(R.id.trackMPImage);
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

        miniPlayerView.setOnClickListener(v -> {
            if (currentTrack != null) {
                TrackPlayerSheetFragment trackPlayerSheetFragment = new TrackPlayerSheetFragment(currentTrack);
                trackPlayerSheetFragment.show(getSupportFragmentManager(), trackPlayerSheetFragment.getTag());
                trackPlayerSheetFragment.setUpdateProgressRunnable();
            }
        });
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
            if (handler == null) {
                handler = new Handler();
            }

            if (updateProgressRunnable != null) {
                handler.removeCallbacks(updateProgressRunnable);
            }

            updateProgressRunnable = new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int currentPosition = mediaPlayer.getCurrentPosition();
                        trackProgressIndicator.setProgress(currentPosition);
                        trackProgressIndicator.setMax(mediaPlayer.getDuration());

                        Fragment fragment = getSupportFragmentManager().findFragmentByTag("TrackPlayerSheetFragment");
                        if (fragment instanceof TrackPlayerSheetFragment) {
                            ((TrackPlayerSheetFragment) fragment).updateProgress(currentPosition, mediaPlayer.getDuration());
                        }

                        handler.postDelayed(this, 1000);
                    }
                }
            };
            handler.post(updateProgressRunnable);

            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
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
                if (currentTrack.getFile_url() != null && !currentTrack.getFile_url().isEmpty()) {
                    mediaPlayer.setDataSource(currentTrack.getFile_url());
                    mediaPlayer.prepareAsync();
                } else {
                    throw new IllegalArgumentException("Track URL is null or empty");
                }
            } catch (IOException e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void showMiniPlayer(Tracks tracks) {
        miniPlayerTitle.setText(tracks.getTrack_name());

        Picasso.get().load(tracks.getTrack_image_url())
                .into(miniPlayerImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        Bitmap bitmap = ((BitmapDrawable) miniPlayerImage.getDrawable()).getBitmap();
                        Palette.from(bitmap).generate(palette -> {
                            if (palette != null) {
                                int dominantColor = palette.getDominantColor(ContextCompat.getColor(DashboardActivity.this, R.color.lightGreen));
                                ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), miniPlayerView.getSolidColor(), dominantColor);
                                colorAnimation.setDuration(300); // milliseconds
                                colorAnimation.addUpdateListener(animator -> miniPlayerView.setBackgroundColor((int) animator.getAnimatedValue()));
                                colorAnimation.start();
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        miniPlayerView.setBackgroundColor(ContextCompat.getColor(DashboardActivity.this, R.color.darkTheme));
                    }
                });

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

    public void togglePlayback() {
        if (mediaPlayer.isPlaying()) {
            pauseTrack();
        } else {
            playTrack();
        }
    }

    public void pauseTrack() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.pause();
            miniPlayerPlayPause.setImageResource(R.drawable.play_track);
            isPlaying = false;
            handler.removeCallbacks(updateProgressRunnable);
        }
    }

    public void playTrack() {
        if (mediaPlayer != null && !isPlaying) {
            mediaPlayer.start();
            miniPlayerPlayPause.setImageResource(R.drawable.pause_track);
            isPlaying = true;
            handler.post(updateProgressRunnable);
        }
    }

    public void playNextTrack() {
        if (tracksList != null && !tracksList.isEmpty()) {
            if (tracksList.size() > 1) {
                currentTrackPosition++;
                if (currentTrackPosition >= tracksList.size()) {
                    currentTrackPosition = 0;
                }
            }
            updateAlbumTracksAdapter();
            updateSearchAdapter();
            playCurrentTrack();
            showMiniPlayer(tracksList.get(currentTrackPosition));
        }
    }

    public void playPreviousTrack() {
        if (tracksList != null && !tracksList.isEmpty()) {
            if (tracksList.size() > 1) {
                currentTrackPosition--;
                if (currentTrackPosition < 0) {
                    currentTrackPosition = tracksList.size() - 1;
                }
            }
            updateAlbumTracksAdapter();
            updateSearchAdapter();
            playCurrentTrack();
            showMiniPlayer(tracksList.get(currentTrackPosition));
        }
    }

    public int getCurrentPosition() {
        if (mediaPlayer != null && isPlaying) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    public int getRemainingDuration() {
        if (mediaPlayer != null) {
            int duration = mediaPlayer.getDuration();
            int currentDuration = mediaPlayer.getCurrentPosition();
            return  duration - currentDuration;
        }
        return 0;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    public Tracks getCurrentTrack() {
        return currentTrack;
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
        if (handler != null) {
            handler.removeCallbacks(updateProgressRunnable);
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}