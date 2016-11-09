package com.eje_c.vrmultiview.gearvr;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.eje_c.vrmultiview.common.ControlMessage;
import com.eje_c.vrmultiview.common.JSON;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Receiver;
import org.meganekkovr.GearVRActivity;

@EActivity
public class MainActivity extends GearVRActivity {

    private final ServiceConnection controlConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };
    private final ServiceConnection broadcastConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService(ControlReceiverService_.intent(this).get(), controlConnection, BIND_AUTO_CREATE);
        bindService(BroadcastService_.intent(this).get(), broadcastConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        unbindService(controlConnection);
        unbindService(broadcastConnection);
        super.onDestroy();
    }

    /**
     * Called when connected with Gear VR.
     */
    @Receiver(actions = ControlReceiverService.ACTION_WEBSOCKET_CONNECTED, local = true)
    void websocketConnected() {

        App app = (App) getApp();
        if (app == null) return;

        app.onControllerConnected();
    }

    /**
     * Called when WebSocket connection is disconnected.
     */
    @Receiver(actions = ControlReceiverService.ACTION_WEBSOCKET_DISCONNECTED, local = true)
    void websocketDisconnected() {

        App app = (App) getApp();
        if (app == null) return;

        app.onControllerDisconnected();
    }

    /**
     * Called when receive WebSocket string message.
     *
     * @param message ControlMessage JSON representation.
     */
    @Receiver(actions = ControlReceiverService.ACTION_WEBSOCKET_MESSAGE_RECEIVED, local = true)
    void websocketMessageReceived(@Receiver.Extra(ControlReceiverService.EXTRA_WEBSOCKET_MESSAGE) String message) {

        App app = (App) getApp();
        if (app == null) return;

        ControlMessage controlMessage = JSON.parse(message, ControlMessage.class);
        app.onControlMessageReceived(controlMessage);
    }
}
