package com.example.digitalwellbeinganalyzer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ScreenStateReceiver extends BroadcastReceiver {

    private static final String TAG = "ScreenStateReceiver";

    public interface PickupListener {
        void onPhonePickup(long timestamp);
    }

    private static PickupListener listener;

    public static void setListener(PickupListener l) {
        listener = l;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
            long now = System.currentTimeMillis();
            Log.i(TAG, ">>> PHONE PICKUP DETECTED at " + now);
            if (listener != null) {
                listener.onPhonePickup(now);
            }
        }
    }
}