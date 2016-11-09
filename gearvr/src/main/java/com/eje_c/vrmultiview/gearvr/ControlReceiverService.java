package com.eje_c.vrmultiview.gearvr;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.server.AsyncHttpServer;

import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.res.IntegerRes;
import org.androidannotations.annotations.res.StringRes;
import org.json.JSONException;
import org.json.JSONObject;

@EService
public class ControlReceiverService extends Service {
    public static final String ACTION_WEBSOCKET_CONNECTED = "websocket_connected";
    public static final String ACTION_WEBSOCKET_DISCONNECTED = "websocket_disconnected";
    public static final String ACTION_WEBSOCKET_MESSAGE_RECEIVED = "websocket_message_received";
    public static final String EXTRA_WEBSOCKET_MESSAGE = "websocket_message";
    private static final String TAG = "ControlReceiverService";

    private AsyncHttpServer server;
    private WebSocket webSocket;

    @IntegerRes
    int websocketPort;
    @StringRes
    String websocketProtocol;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        server = new AsyncHttpServer();

        // For debug purpose communication testing
        server.get("/", (request, response) -> {
            JSONObject json = new JSONObject();
            try {
                json.put("vrmultiview", "Welcome");
                json.put("websocketProtocol", websocketProtocol);
                json.put("version", BuildConfig.VERSION_NAME);
                response.send(json);
            } catch (JSONException e) {
                e.printStackTrace();
                response.code(500);
                response.send(e.getLocalizedMessage());
            }
        });

        // Accept WebSocket connection
        server.websocket("/control", websocketProtocol, (webSocket, request) -> {

            // Refuse if already this device is connected to controller
            if (this.webSocket != null) {
                webSocket.close();
                return;
            }

            this.webSocket = webSocket;

            broadcastManager.sendBroadcast(new Intent(ACTION_WEBSOCKET_CONNECTED));

            webSocket.setClosedCallback(ex -> {
                this.webSocket = null;

                broadcastManager.sendBroadcast(new Intent(ACTION_WEBSOCKET_DISCONNECTED));

                // Show error details
                if (ex != null) ex.printStackTrace();
            });

            webSocket.setStringCallback(s -> {
//                Log.d(TAG, s); // For debug purpose

                broadcastManager.sendBroadcast(new Intent(ACTION_WEBSOCKET_MESSAGE_RECEIVED)
                        .putExtra(EXTRA_WEBSOCKET_MESSAGE, s));
            });
        });

        server.listen(websocketPort);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        closeWebSocket();
        stopServer();
    }

    private void closeWebSocket() {
        if (webSocket != null) {
            webSocket.close();
            webSocket = null;
        }
    }

    private void stopServer() {
        server.stop();
        AsyncServer.getDefault().stop();
    }
}
