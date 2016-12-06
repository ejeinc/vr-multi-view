package com.eje_c.vrmultiview.controller;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.eje_c.vrmultiview.common.ControlMessage;
import com.eje_c.vrmultiview.common.GearVRDeviceInfo;
import com.eje_c.vrmultiview.common.JSON;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.menu)
public class MainActivity extends AppCompatActivity {

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            controllerService = ((ControllerService.LocalBinder) service).getService();
            showVideoPathDialog();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            controllerService = null;
        }
    };
    private ControllerService controllerService;

    @ViewById
    RecyclerView deviceList;
    @ViewById
    ImageButton btnPlay;
    @ViewById
    TextView deviceCount;

    @Bean
    DeviceListAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BroadcastReceiverService_.intent(this).start();
        bindService(ControllerService_.intent(this).get(), conn, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        BroadcastReceiverService_.intent(this).stop();
        unbindService(conn);
        super.onDestroy();
    }

    @AfterViews
    void init() {
        deviceList.setAdapter(adapter);
        deviceList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        deviceList.setHasFixedSize(true);
    }

    /**
     * Show video path input dialog
     */
    @OptionsItem(R.id.menu_video_path)
    void showVideoPathDialog() {

        if (controllerService == null) return;

        final String currentPath = controllerService.getPath();

        // Try to get file paths from /sdcard/multiview-files.txt
        List<String> filePaths = getFilePaths();
        if (!filePaths.isEmpty()) {

            // Show path choose dialog
            new MaterialDialog.Builder(this)
                    .title(R.string.set_video_path)
                    .items(filePaths)
                    .itemsCallbackSingleChoice(filePaths.indexOf(currentPath), (dialog, itemView, which, text) -> {
                        if (which >= 0) {
                            setAndSyncPath(text.toString());
                        }
                        return true;
                    })
                    .positiveText(android.R.string.ok)
                    .show();
        } else {

            // Show path input dialog
            new MaterialDialog.Builder(this)
                    .title(R.string.set_video_path)
                    .input(null, currentPath, false, (dialog, input) -> {

                        // Send control message to Gear VR
                        setAndSyncPath(input.toString());
                    })
                    .show();
        }
    }

    private void setAndSyncPath(String path) {
        controllerService.setPath(path);
        controllerService.sendControlMessage();

        toast(getString(R.string.path_changed, path));
    }

    /**
     * Read file paths from /sdcard/multiview-files.txt
     *
     * @return List of file path candidacies.
     */
    private static List<String> getFilePaths() {

        final File file = new File(Environment.getExternalStorageDirectory(), "multiview-files.txt");
        if (!file.exists()) return Collections.emptyList();

        List<String> filePaths = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            while (true) {
                String line = br.readLine();

                // EOF
                if (line == null) break;

                final String filePath = line.trim();
                // Ignore empty row
                if (filePath.isEmpty()) continue;

                filePaths.add(filePath);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return filePaths;
    }

    /**
     * Called when received Gear VR connection info.
     *
     * @param json GearVRDeviceInfo JSON representation.
     */
    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Receiver(actions = BroadcastReceiverService.ACTION_BROADCAST_RECEIVED, local = true)
    void onBroadcastReceive(@Receiver.Extra(BroadcastReceiverService.EXTRA_MESSAGE) String json) {

        GearVRDeviceInfo info = JSON.parse(json, GearVRDeviceInfo.class);
        controllerService.connect(info);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Receiver(actions = ControllerService.ACTION_DEVICE_CONNECTED, local = true)
    void onDeviceConnected(@Receiver.Extra(ControllerService.EXTRA_DEVICE_INFO) GearVRDeviceInfo info) {

        // Show connected WebSockets on list
        adapter.add(info);
        updateDeviceCount();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Receiver(actions = ControllerService.ACTION_DEVICE_DISCONNECTED, local = true)
    void onDeviceDisconnected(@Receiver.Extra(ControllerService.EXTRA_DEVICE_INFO) GearVRDeviceInfo info) {

        // Remove from list
        adapter.remove(info);
        updateDeviceCount();
    }


    /**
     * Show connected devices count.
     */
    @AfterViews
    @UiThread(propagation = UiThread.Propagation.REUSE)
    void updateDeviceCount() {
        deviceCount.setText(getString(R.string.deviceCount, adapter.getItemCount()));
    }

    /**
     * Toggle play and stop state.
     */
    @Click
    void btnPlay() {

        switch (controllerService.getState()) {
            case ControlMessage.STATE_STOP:
                toast(R.string.play);
                controllerService.setState(ControlMessage.STATE_PLAY);
                btnPlay.setImageResource(R.drawable.ic_stop_white_24px);
                break;
            case ControlMessage.STATE_PLAY:
                toast(R.string.stop);
                controllerService.setState(ControlMessage.STATE_STOP);
                btnPlay.setImageResource(R.drawable.ic_play_arrow_white_24px);
                break;
        }

        controllerService.sendControlMessage();
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
