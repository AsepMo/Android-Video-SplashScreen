package com.video.evolution.application.player.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Network {
    public static boolean IsConnected(Context c) {
        ConnectivityManager manager = (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (manager == null) return false;

        NetworkInfo network = manager.getActiveNetworkInfo();
        if (network == null) return false;

        return network.isConnected();
    }
}

