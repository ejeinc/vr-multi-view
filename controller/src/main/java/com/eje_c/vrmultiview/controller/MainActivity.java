package com.eje_c.vrmultiview.controller;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.eje_c.vrmultiview.common.ControlMessage;
import com.eje_c.vrmultiview.common.GearVRDeviceInfo;
import com.eje_c.vrmultiview.common.JSON;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.menu)
public class MainActivity extends AppCompatActivity {

    private final Set<WebSocket> webSockets = new CopyOnWriteArraySet<>();
    private final ControlMessage controlMessage = new ControlMessage();

    @ViewById
    RecyclerView deviceList;
    @ViewById
    ImageButton btnPlay;
    @ViewById
    TextView deviceCount;

    @Bean
    DeviceListAdapter adapter;

    @org.androidannotations.annotations.res.StringRes
    String websocketProtocol;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BroadcastReceiverService_.intent(this).start();
    }

    @Override
    protected void onDestroy() {
        BroadcastReceiverService_.intent(this).stop();
        closeSocket();
        super.onDestroy();
    }

    /**
     * Close all WebSockets connecting to Gear VR.
     */
    private void closeSocket() {
        for (WebSocket webSocket : webSockets) {
            webSocket.close();
        }

        webSockets.clear();
    }

    @AfterViews
    void init() {
        deviceList.setAdapter(adapter);
        deviceList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    /**
     * Show video path input dialog
     */
    @OptionsItem(R.id.menu_video_path)
    @AfterViews
    void showVideoPathDialog() {

        EditText editText = new EditText(this);
        editText.setText(controlMessage.path);

        new AlertDialog.Builder(this, R.style.Dialog)
                .setTitle(R.string.set_video_path)
                .setView(editText)
                .setPositiveButton(android.R.string.ok, (dialogInterface, which) -> {

                    // Send control message to Gear VR
                    final String path = editText.getText().toString();
                    controlMessage.path = path;
                    sendControlMessage();

                    toast(getString(R.string.path_changed, path));
                })
                .show();
    }

    /**
     * Called when received Gear VR connection info.
     *
     * @param json GearVRDeviceInfo JSON representation.
     */
    @UiThread
    @Receiver(actions = BroadcastReceiverService.ACTION_BROADCAST_RECEIVED, local = true)
    void onBroadcastReceive(@Receiver.Extra(BroadcastReceiverService.EXTRA_MESSAGE) String json) {

        GearVRDeviceInfo info = JSON.parse(json, GearVRDeviceInfo.class);
        final String uri = String.format(Locale.US, "ws://%s:%d/control", info.ipAddress, info.port);

        AsyncHttpClient.getDefaultInstance().websocket(uri, websocketProtocol, (ex, webSocket) -> {

            // Connection error
            if (ex != null) {
                ex.printStackTrace();
                return;
            }

            // Keep connected WebSockets
            webSockets.add(webSocket);

            // Show connected WebSockets on list
            adapter.add(info);

            updateDeviceCount();

            // Send current state
            webSocket.send(controlMessage.toString());

            // Clean up
            webSocket.setClosedCallback(ex1 -> {
                // Delete reference
                webSockets.remove(webSocket);

                // Remove from list
                adapter.remove(info);

                updateDeviceCount();
            });

        });
    }

    /**
     * Show connected devices count.
     */
    @AfterViews
    @UiThread(propagation = UiThread.Propagation.REUSE)
    void updateDeviceCount() {
        deviceCount.setText(getString(R.string.deviceCount, webSockets.size()));
    }

    /**
     * Toggle play and stop state.
     */
    @Click
    void btnPlay() {

        switch (controlMessage.state) {
            case ControlMessage.STATE_STOP:
                toast(R.string.play);
                controlMessage.state = ControlMessage.STATE_PLAY;
                btnPlay.setImageResource(R.drawable.ic_stop_white_24px);
                break;
            case ControlMessage.STATE_PLAY:
                toast(R.string.stop);
                controlMessage.state = ControlMessage.STATE_STOP;
                btnPlay.setImageResource(R.drawable.ic_play_arrow_white_24px);
                break;
        }

        sendControlMessage();
    }

    /**
     * Send control message to connected Gear VRs
     */
    private void sendControlMessage() {

        String json = controlMessage.toString();

        for (WebSocket webSocket : webSockets) {
            webSocket.send(json);
        }
    }

    @UiThread
    void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @UiThread
    void toast(@StringRes int msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
