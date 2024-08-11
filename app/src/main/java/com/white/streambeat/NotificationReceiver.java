package com.white.streambeat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.white.streambeat.Activities.DashboardActivity;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            Log.d(TAG, "Received action: " + action);
            switch (action) {
                case "PLAY_PAUSE":
                    handlePlayPause(context);
                    break;
                case "NEXT":
                    handleNext(context);
                    break;
                case "PREVIOUS":
                    handlePrevious(context);
                    break;
                case "SEEK":
                    int position = intent.getIntExtra("position", 0);
                    handleSeek(context, position);
                    break;
                default:
                    break;
            }
        }
    }

    private void handlePlayPause(Context context) {
        // Implement logic to toggle play/pause
        Intent playPauseIntent = new Intent(context, DashboardActivity.class);
        playPauseIntent.setAction("PLAY_PAUSE");
        context.startService(playPauseIntent);
    }

    private void handleNext(Context context) {
        Intent nextIntent = new Intent(context, DashboardActivity.class);
        nextIntent.setAction("NEXT");
        context.startService(nextIntent);
    }

    private void handlePrevious(Context context) {
        Intent previousIntent = new Intent(context, DashboardActivity.class);
        previousIntent.setAction("PREVIOUS");
        context.startService(previousIntent);
    }

    private void handleSeek(Context context, int position) {
        Intent seekIntent = new Intent(context, DashboardActivity.class);
        seekIntent.setAction("SEEK");
        seekIntent.putExtra("position", position);
        context.startService(seekIntent);
    }
}
