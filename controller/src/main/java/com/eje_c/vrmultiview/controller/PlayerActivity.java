package com.eje_c.vrmultiview.controller;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.eje_c.vrmultiview.common.ControlMessage;
import com.google.vr.sdk.widgets.video.VrVideoEventListener;
import com.google.vr.sdk.widgets.video.VrVideoView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.io.IOException;

@EActivity(R.layout.activity_player)
public class PlayerActivity extends AppCompatActivity {

    private final ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            controllerService = ((ControllerService.LocalBinder) service).getService();
            vrVideoView.playVideo();
            controllerService.setState(ControlMessage.STATE_PLAY);
            controllerService.sendControlMessage();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            controllerService = null;
        }
    };
    private ControllerService controllerService;

    @Extra
    Uri uri;
    @Extra
    int format = VrVideoView.Options.FORMAT_DEFAULT;
    @Extra
    int type = VrVideoView.Options.TYPE_MONO;

    @ViewById
    VrVideoView vrVideoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService(ControllerService_.intent(this).get(), conn, BIND_AUTO_CREATE);
    }

    @AfterViews
    void init() {

        // Take from extras
        VrVideoView.Options options = new VrVideoView.Options();
        options.inputFormat = format;
        options.inputType = type;

        // Hide controls
        vrVideoView.setFullscreenButtonEnabled(false);
        vrVideoView.setInfoButtonEnabled(false);
        vrVideoView.setStereoModeButtonEnabled(false);

        // Load video
        try {
            vrVideoView.loadVideo(uri, options);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        vrVideoView.setEventListener(new VrVideoEventListener() {
            @Override
            public void onCompletion() {
                super.onCompletion();

                // Exit when video ends
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onLoadError(String errorMessage) {
                super.onLoadError(errorMessage);

                // Show load error message and exit
                Toast.makeText(PlayerActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        vrVideoView.resumeRendering();
    }

    @Override
    protected void onPause() {
        vrVideoView.pauseRendering();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        vrVideoView.shutdown();
        controllerService.setState(ControlMessage.STATE_STOP);
        controllerService.sendControlMessage();
        unbindService(conn);
        super.onDestroy();
    }
}
