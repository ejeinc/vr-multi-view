package com.eje_c.vrmultiview.gearvr;

import android.content.Intent;
import android.os.Bundle;

import com.eje_c.meganekko.Meganekko;
import com.eje_c.meganekko.MeganekkoApp;
import com.eje_c.meganekko.gearvr.MeganekkoActivity;
import com.eje_c.vrmultiview.common.ControlMessage;
import com.eje_c.vrmultiview.common.JSON;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Receiver;

@EActivity
public class MainActivity extends MeganekkoActivity {

    private App app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ControlReceiverService_.intent(this).start();
        BroadcastService_.intent(this).start();
    }

    @Override
    protected void onDestroy() {
        ControlReceiverService_.intent(this).stop();
        BroadcastService_.intent(this).stop();
        super.onDestroy();
    }

    @Override
    protected void onHmdMounted() {
        super.onHmdMounted();
        app.onHmdMounted();
    }

    @Override
    protected void onHmdUnmounted() {
        super.onHmdUnmounted();
        app.onHmdUnmounted();
    }

    /**
     * Called when connected with Gear VR.
     */
    @Receiver(actions = ControlReceiverService.ACTION_WEBSOCKET_CONNECTED, local = true)
    void websocketConnected() {
        if (app == null) return;

        app.onControllerConnected();
    }

    /**
     * Called when WebSocket connection is disconnected.
     */
    @Receiver(actions = ControlReceiverService.ACTION_WEBSOCKET_DISCONNECTED, local = true)
    void websocketDisconnected() {
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
        if (app == null) return;

        ControlMessage controlMessage = JSON.parse(message, ControlMessage.class);
        app.onControlMessageReceived(controlMessage);
    }

    /**
     * Called when error is occurred in Service.
     *
     * @param msg Error message.
     */
    @Receiver(actions = BroadcastService.ACTION_ERROR, local = true)
    void onError(@Receiver.Extra(Intent.EXTRA_TEXT) String msg) {
        createVrToastOnUiThread(msg);
    }

    @Override
    public MeganekkoApp createMeganekkoApp(Meganekko meganekko) {

        // Set CPU and GPU level to lowest value
        getApp().setCpuLevel(0);
        getApp().setGpuLevel(0);

        return app = new App(meganekko);
    }
}
