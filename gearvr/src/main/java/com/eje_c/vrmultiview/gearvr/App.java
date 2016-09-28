package com.eje_c.vrmultiview.gearvr;

import android.os.Environment;

import com.eje_c.vrmultiview.common.ControlMessage;

import org.meganekkovr.MeganekkoApp;

import java.io.File;
import java.io.IOException;

public class App extends MeganekkoApp {

    private PlayerScene scene;

    @Override
    public void init() {
        super.init();
        setSceneFromXml(R.xml.scene);
        scene = (PlayerScene) getScene();
    }

    /**
     * Called when receive WebSocket string message.
     *
     * @param controlMessage Message from controller.
     */
    public void onControlMessageReceived(ControlMessage controlMessage) {

        final String videoPath = new File(Environment.getExternalStorageDirectory(), controlMessage.path).getAbsolutePath();

        try {
            scene.load(videoPath);
            scene.seekTo(controlMessage.seek);

            switch (controlMessage.state) {
                case ControlMessage.STATE_PLAY:
                    scene.start();
                    break;
                case ControlMessage.STATE_STOP:
                    scene.pause();
                    break;
            }

            runOnGlThread(() -> scene.setText(R.string.waiting_for_controller_operation));

        } catch (IOException e) {
            e.printStackTrace();
            runOnGlThread(() -> scene.setText(e.getLocalizedMessage()));
        }
    }

    /**
     * Called when connected with Gear VR.
     */
    public void onControllerConnected() {
        runOnGlThread(() -> scene.setText(R.string.waiting_for_controller_operation));
    }

    /**
     * Called when WebSocket connection is disconnected.
     */
    public void onControllerDisconnected() {
        runOnGlThread(() -> scene.setText(R.string.waiting_for_controller_connection));
    }

    @Override
    public void onHmdMounted() {
        super.onHmdMounted();
        scene.setVolume(1);
    }

    @Override
    public void onHmdUnmounted() {
        super.onHmdUnmounted();
        scene.setVolume(0);
    }
}
