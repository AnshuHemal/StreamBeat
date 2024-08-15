package com.white.streambeat.Activities;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import android.os.Handler;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.palette.graphics.Palette;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
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
import com.white.streambeat.LoadingDialog;
import com.white.streambeat.Models.Albums;
import com.white.streambeat.Models.SharedViewModel;
import com.white.streambeat.Models.Tracks;
import com.white.streambeat.NotificationReceiver;
import com.white.streambeat.R;
import com.white.streambeat.TrackPlayerSheetFragment;
import com.white.streambeat.databinding.ActivityDashboardBinding;

import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class DashboardActivity extends AppCompatActivity implements TrackPlayerSheetFragment.OnTrackControlListener, TrackPlayerSheetFragment.OnTrackUpdateListener {

    ActivityDashboardBinding binding;
    FrameLayout frameLayout;
    Fragment currentFragment;
    SharedViewModel sharedViewModel;
    FirebaseUser firebaseUser;

    View miniPlayerView;
    TextView miniPlayerTitle, miniPlayerArtistsNames;
    ImageView miniPlayerPlayPause, miniPlayerShuffle, miniPlayerImage;

    LoadingDialog dialog;

    MediaSessionCompat mediaSession;
    private static final String CHANNEL_ID = "music_channel";

    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    Tracks currentTrack;
    LinearProgressIndicator trackProgressIndicator;
    private Handler handler;
    private Runnable updateProgressRunnable;
    private List<Tracks> tracksList;
    private int currentTrackPosition = -1;

    public void addToLikedSongs(Tracks track, String phoneNumber) {
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                ServerConnector.STORE_LIKED_TRACKS,
                response -> {}/*Toast.makeText(DashboardActivity.this, response, Toast.LENGTH_SHORT).show()*/, error -> Toast.makeText(DashboardActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
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

    public void removeFromLikedSongs(Tracks tracks, String phoneNumber) {
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                ServerConnector.REMOVE_LIKED_TRACKS,
                response -> {}/*Toast.makeText(DashboardActivity.this, response, Toast.LENGTH_SHORT).show()*/, error -> Toast.makeText(DashboardActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> map = new HashMap<>();
                map.put("user_phone", phoneNumber);
                map.put("track_name", tracks.getTrack_name());
                return map;
            }
        };
        Volley.newRequestQueue(getApplicationContext()).add(stringRequest);
    }

    @SuppressLint("MissingPermission")
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    String deviceName = device.getName() != null ? device.getName() : "Unnamed Device";
                    Toast.makeText(context, "Bluetooth connected with " + deviceName, Toast.LENGTH_SHORT).show();
                }
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                Toast.makeText(context, "Bluetooth device disconnected", Toast.LENGTH_SHORT).show();
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

        dialog = new LoadingDialog(this);


        setupMediaSession();

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

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        registerReceiver(bluetoothReceiver, filter);

        miniPlayerView.setOnClickListener(v -> {
            if (currentTrack != null) {
                TrackPlayerSheetFragment trackPlayerSheetFragment = new TrackPlayerSheetFragment(currentTrack);
                trackPlayerSheetFragment.show(getSupportFragmentManager(), "TrackPlayerSheetFragment");
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
            updateIconLikedTracks(currentTrack);
            saveUserLog(currentTrack);
            showNotification();

            if (handler == null) {
                handler = new Handler();
            }

            if (updateProgressRunnable != null) {
                handler.removeCallbacks(updateProgressRunnable);
            }

            updateProgressRunnable = new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        int currentPosition = mediaPlayer.getCurrentPosition();

                        trackProgressIndicator.setProgress(currentPosition);
                        trackProgressIndicator.setMax(mediaPlayer.getDuration());

                        int remainingTime = getRemainingDuration();

                        if (remainingTime <= 1000) {
                            playNextTrack();
                            return;
                        }

                        Fragment fragment = getSupportFragmentManager().findFragmentByTag("TrackPlayerSheetFragment");
                        if (fragment instanceof TrackPlayerSheetFragment) {
                            ((TrackPlayerSheetFragment) fragment).updateProgress(currentPosition, mediaPlayer.getDuration());
                        }
                        showNotification();
                        updateIconLikedTracks(currentTrack);
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
                                int dominantColor = palette.getDarkMutedColor(ContextCompat.getColor(DashboardActivity.this, R.color.lightTheme));
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
        miniPlayerShuffle.setOnClickListener(v -> {
            if (!tracks.isLikedByUser()) {
                addToLikedSongs(tracks, firebaseUser.getPhoneNumber());
                tracks.setLikedByUser(true);
            } else {
                removeFromLikedSongs(tracks, firebaseUser.getPhoneNumber());
                tracks.setLikedByUser(false);
            }
            updateIconLikedTracks(tracks);
        });
    }

    public void updateIconLikedTracks(Tracks tracks) {
        if (tracks.isLikedByUser()) {
            miniPlayerShuffle.setImageResource(R.drawable.added);
            miniPlayerShuffle.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.lightGreen));
        } else {
            miniPlayerShuffle.setImageResource(R.drawable.add_to_liked_songs);
            miniPlayerShuffle.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.white));
        }
    }

    public void togglePlayback() {
        if (mediaPlayer.isPlaying()) {
            updateIconLikedTracks(currentTrack);
            pauseTrack();
        } else {
            updateIconLikedTracks(currentTrack);
            playTrack();
        }
    }

    public void pauseTrack() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
            miniPlayerPlayPause.setImageResource(R.drawable.play_track);
            handler.removeCallbacks(updateProgressRunnable);
            showNotification();
            updateIconLikedTracks(currentTrack);
        }
    }


    public void playTrack() {
        if (mediaPlayer != null && !isPlaying) {
            mediaPlayer.start();
            isPlaying = true;
            miniPlayerPlayPause.setImageResource(R.drawable.pause_track);
            trackProgressIndicator.setProgress(mediaPlayer.getCurrentPosition());
            trackProgressIndicator.setMax(mediaPlayer.getDuration());
            handler.post(updateProgressRunnable);
            showNotification();
            updateIconLikedTracks(currentTrack);
        }
    }

    public void playNextTrack() {
        if (tracksList != null && !tracksList.isEmpty()) {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }

            currentTrackPosition++;
            if (currentTrackPosition >= tracksList.size()) {
                currentTrackPosition = 0; // Loop back to the start
            }
            Tracks nextTrack = tracksList.get(currentTrackPosition);

            playCurrentTrack();
            showMiniPlayer(nextTrack);
            updateIconLikedTracks(nextTrack);
            showNotification();
            updateAlbumTracksAdapter();
            updateSearchAdapter();
        } else {
            Log.e("TrackPlayer", "Track list is null or empty.");
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
            updateIconLikedTracks(tracksList.get(currentTrackPosition));
            showNotification();
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
            return duration - currentDuration;
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

    private void saveUserLog(Tracks track) {
        String album_title = track.getAlbum_title();
        int album_id = 0;
        for (Albums album : Objects.requireNonNull(sharedViewModel.getAllAlbumsList().getValue())) {
            if (album.getAlbum_title().equals(album_title)) {
                album_id = album.getAlbum_id();
                break;
            }
        }

        int finalAlbum_id = album_id;
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                ServerConnector.SAVE_USER_LOGS,
                response -> {
                },
                error -> Toast.makeText(DashboardActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("key_phone", firebaseUser.getPhoneNumber());
                hashMap.put("album_ids", String.valueOf(finalAlbum_id));
                hashMap.put("listen_date", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
                return hashMap;
            }
        };
        Volley.newRequestQueue(getApplicationContext()).add(stringRequest);
    }

    @SuppressLint("MissingPermission")
    private void showNotification() {
        if (currentTrack == null) return;

        Intent playPauseIntent = new Intent(getApplicationContext(), NotificationReceiver.class).setAction("PLAY_PAUSE");
        PendingIntent playPausePendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent nextIntent = new Intent(getApplicationContext(), NotificationReceiver.class).setAction("NEXT");
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent previousIntent = new Intent(getApplicationContext(), NotificationReceiver.class).setAction("PREVIOUS");
        PendingIntent previousPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 2, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Glide.with(getApplicationContext())
                .asBitmap()
                .load(currentTrack.getTrack_image_url())
                .override(800, 800)
                .centerCrop()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable com.bumptech.glide.request.transition.Transition<? super Bitmap> transition) {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                .setContentTitle(currentTrack.getTrack_name())
                                .setContentText(String.join(", ", currentTrack.getArtist_names()))
                                .setLargeIcon(resource)
                                .addAction(R.drawable.previous_track, "Previous", previousPendingIntent)
                                .addAction(isPlaying ? R.drawable.pause_track : R.drawable.play_track, "Play/Pause", playPausePendingIntent)
                                .addAction(R.drawable.next_track, "Next", nextPendingIntent)
                                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                        .setMediaSession(mediaSession.getSessionToken())
                                        .setShowActionsInCompactView(0, 1, 2))
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setOngoing(true)
                                .setSilent(true)
                                .setNotificationSilent()
                                .setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.silent));

                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                        notificationManager.notify(1, builder.build());
                    }
                });
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
        if (mediaSession != null) {
            mediaSession.release();
            mediaSession = null;
        }
        unregisterReceiver(bluetoothReceiver);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(1);
    }

    private final BroadcastReceiver localReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra("action");
            if (action != null) {
                switch (action) {
                    case "PLAY_PAUSE":
                        if (mediaPlayer != null) {
                            if (mediaPlayer.isPlaying()) {
                                pauseTrack();
                            } else {
                                playTrack();
                            }
                        }
                        break;
                    case "NEXT":
                        playNextTrack();
                        break;
                    case "PREVIOUS":
                        playPreviousTrack();
                        break;
                    case "SEEK":
                        int position = intent.getIntExtra("position", 0);
                        seekTo(position);
                        break;
                    default:
                        break;
                }
            }
        }
    };

    private void setupMediaSession() {
        mediaSession = new MediaSessionCompat(this, "MusicPlayerSession");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                playTrack();
            }

            @Override
            public void onPause() {
                pauseTrack();
            }

            @Override
            public void onSkipToNext() {
                playNextTrack();
            }

            @Override
            public void onSkipToPrevious() {
                playPreviousTrack();
            }

            @Override
            public void onSeekTo(long pos) {
                seekTo((int) pos);
            }
        });

        mediaSession.setMediaButtonReceiver(null);
        mediaSession.setActive(true);
    }


    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(localReceiver, new IntentFilter("com.white.streambeat.ACTION"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localReceiver);
    }

    @Override
    public void onTrackUpdated(Tracks updatedTrack) {
        showMiniPlayer(updatedTrack);
    }

}