package com.eje_c.vrmultiview.controller;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.eje_c.vrmultiview.common.ControlMessage;
import com.eje_c.vrmultiview.common.GearVRDeviceInfo;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.UiThread;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@EService
public class ControllerService extends Service {

    public static final String ACTION_DEVICE_CONNECTED = "device_connected";
    public static final String ACTION_DEVICE_DISCONNECTED = "device_disconnected";
    public static final String EXTRA_DEVICE_INFO = "device_info";

    public class LocalBinder extends Binder {
        public ControllerService getService() {
            return ControllerService.this;
        }
    }

    private final LocalBinder localBinder = new LocalBinder();
    private final Set<WebSocket> webSockets = new CopyOnWriteArraySet<>();
    private final ControlMessage controlMessage = new ControlMessage();

    @org.androidannotations.annotations.res.StringRes
    String websocketProtocol;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        closeSocket();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    /**
     * Close all WebSockets connecting to Gear VR.
     */
    private void closeSocket() {
        for (WebSocket webSocket : webSockets) {
            webSocket.close();
        }

        webSockets.clear();
        AsyncHttpClient.getDefaultInstance().getServer().stop();
    }

    /**
     * Called when received Gear VR connection info.
     *
     * @param info
     */
    @UiThread
    public void connect(GearVRDeviceInfo info) {

        final String uri = String.format(Locale.US, "ws://%s:%d/control", info.ipAddress, info.port);

        AsyncHttpClient.getDefaultInstance().websocket(uri, websocketProtocol, (ex, webSocket) -> {

            // Connection error
            if (ex != null) {
                ex.printStackTrace();
                return;
            }

            // Keep connected WebSockets
            webSockets.add(webSocket);

            // Send current state
            webSocket.send(controlMessage.toString());

            // Notify to Activity
            LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(new Intent(ACTION_DEVICE_CONNECTED)
                            .putExtra(EXTRA_DEVICE_INFO, info));

            // Clean up
            webSocket.setClosedCallback(ex1 -> {
                // Delete reference
                webSockets.remove(webSocket);

                // Notify to Activity
                LocalBroadcastManager.getInstance(this)
                        .sendBroadcast(new Intent(ACTION_DEVICE_DISCONNECTED)
                                .putExtra(EXTRA_DEVICE_INFO, info));
            });
        });
    }

    /**
     * Send control message to connected Gear VRs
     */
    public void sendControlMessage() {

        String json = controlMessage.toString();

        for (WebSocket webSocket : webSockets) {
            webSocket.send(json);
        }
    }

    public String getPath() {
        return controlMessage.path;
    }

    public void setPath(String path) {
        controlMessage.path = path;
    }

    public int getState() {
        return controlMessage.state;
    }

    public void setState(int state) {
        controlMessage.state = state;
    }
}
