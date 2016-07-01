package com.eje_c.vrmultiview.controller;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.res.IntegerRes;
import org.androidannotations.annotations.res.StringRes;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

@EService
public class BroadcastReceiverService extends Service {

    public static final String ACTION_BROADCAST_RECEIVED = "broadcast_received";
    public static final String EXTRA_MESSAGE = "message";
    public static final int NOTIFICATION_ID = 1;

    private volatile boolean running;
    private DatagramSocket socket;
    private NotificationManagerCompat notificationManager;

    @StringRes
    String appName;
    @IntegerRes
    int broadcastPort;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        start();

        // Show notification
        notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_remote_controller)
                .setContentTitle(appName)
                .setContentText(getString(R.string.notification_text, appName))
                .setContentIntent(PendingIntent.getActivity(this, 1, MainActivity_.intent(this).get(), 0))
                .setOngoing(true)
                .build());
    }

    @Override
    public void onDestroy() {
        stop();
        notificationManager.cancel(NOTIFICATION_ID);
        super.onDestroy();
    }

    @Background
    void start() {
        running = true;

        try {
            socket = new DatagramSocket(broadcastPort);
            byte[] buffer = new byte[4096];

            while (running) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(buffer, packet.getOffset(), packet.getLength());

                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_BROADCAST_RECEIVED).putExtra(EXTRA_MESSAGE, message));
            }

        } catch (IOException e) {
            // On socket closed
//            e.printStackTrace();
        }
    }


    private void stop() {
        running = false;

        if (socket != null) {
            socket.close();
            socket = null;
        }
    }
}
