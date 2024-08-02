package com.white.streambeat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        Intent actionIntent = new Intent("com.white.streambeat.ACTION");
        switch (action) {
            case "PLAY_PAUSE":
                actionIntent.putExtra("action", "PLAY_PAUSE");
                break;
            case "NEXT":
                actionIntent.putExtra("action", "NEXT");
                break;
            case "PREVIOUS":
                actionIntent.putExtra("action", "PREVIOUS");
                break;
            case "SEEK":
                int position = intent.getIntExtra("position", 0);
                actionIntent.putExtra("action", "SEEK");
                actionIntent.putExtra("position", position);
                break;
        }

        LocalBroadcastManager.getInstance(context).sendBroadcast(actionIntent);
    }
}
