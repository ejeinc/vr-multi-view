package com.eje_c.vrmultiview.gearvr;

import android.app.Service;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;

import com.eje_c.vrmultiview.common.GearVRDeviceInfo;

import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.res.IntegerRes;
import org.androidannotations.annotations.res.StringRes;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@EService
public class BroadcastService extends Service {

    public static final String ACTION_ERROR = "error";
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private Future<?> future;

    @IntegerRes
    int broadcastPort;
    @IntegerRes
    int websocketPort;
    @StringRes
    String websocketProtocol;

    @SystemService
    WifiManager wifiManager;
    @SystemService
    TelephonyManager telephonyManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createBroadcastData();
        start();
    }

    /**
     * @return Broadcast address for current network.
     */
    private String getBroadcastAddress() {

        final DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        int netmask = dhcpInfo.netmask;
        int ipAddr = dhcpInfo.ipAddress;

        return Formatter.formatIpAddress(~netmask | ipAddr);
    }

    private byte[] createBroadcastData() {
        GearVRDeviceInfo info = new GearVRDeviceInfo();
        info.ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        info.port = websocketPort;
        info.imei = telephonyManager.getDeviceId();
        return info.toString().getBytes();
    }

    /**
     * Send broadcast message. Resumes if WebSocket connection is closed.
     */
    @Receiver(actions = ControlReceiverService.ACTION_WEBSOCKET_DISCONNECTED, local = true)
    void start() {
        if (future == null) {
            final int delay = 3000 + new Random().nextInt(1000); // Random delay time in 3-4 seconds

            future = executorService.scheduleWithFixedDelay((Runnable) () -> {

                String broadcastAddress = getBroadcastAddress();
                byte[] broadcastData = createBroadcastData();

                if (broadcastAddress == null) return; // Failed to get broadcast address

                try (DatagramSocket clientSocket = new DatagramSocket()) {
                    InetAddress targetAddress = InetAddress.getByName(broadcastAddress);
                    DatagramPacket sendPacket = new DatagramPacket(broadcastData, broadcastData.length, targetAddress, broadcastPort);
                    clientSocket.send(sendPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }, delay, delay, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Stop broadcasting. Called when established WebSocket connection.
     */
    @Receiver(actions = ControlReceiverService.ACTION_WEBSOCKET_CONNECTED, local = true)
    void stop() {
        if (future != null) {
            future.cancel(true);
            future = null;
        }
    }

    @Override
    public void onDestroy() {
        stop();
        executorService.shutdown();
        super.onDestroy();
    }
}
