package io.github.xubpwg.wifinanny.child;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import io.github.xubpwg.wifinanny.WifiDirectBroadcastReceiver;

public class ChildPresenter implements ChildPresenterInterface{

    private static final String CHILD_TAG = "Child";

    private ChildViewInterface view;

    private WifiDirectBroadcastReceiver wifiReceiver;
    private WifiP2pManager wifiManager;
    private WifiP2pManager.Channel channel;
    private IntentFilter wifiIntentFilter;

    @Override
    public void attachView(ChildActivity view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void startMonitoring() {
        if (wifiReceiver.getHostAddress() != null) {
            Intent intent = new Intent(view.getContext(), SoundDetectionService.class);
            intent.setAction(SoundDetectionService.ACTION_START_SOUND_DETECTION_SERVICE);
            intent.putExtra("HOST ADDRESS", wifiReceiver.getHostAddress());
            view.getContext().startService(intent);
        } else {
            view.showToast("Host address is null.");
        }

    }

    @Override
    public void stopMonitoring() {
        Intent intent = new Intent(view.getContext(), SoundDetectionService.class);
        intent.setAction(SoundDetectionService.ACTION_STOP_SOUND_DETECTION_SERVICE);
        view.getContext().startService(intent);
    }

    @Override
    public void initWifiReceiver() {
        wifiManager = (WifiP2pManager) view.getContext().getSystemService(Context.WIFI_P2P_SERVICE);
        if (wifiManager != null) {
            channel = wifiManager.initialize(view.getContext().getApplicationContext(), view.getContext().getMainLooper(), null);
            wifiReceiver = new WifiDirectBroadcastReceiver(wifiManager, channel);

            wifiIntentFilter = new IntentFilter();
            wifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
            wifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
            wifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
            wifiIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        } else {
            view.showToast("Device doesn\'t support WiFi P2P");
        }
    }

    @Override
    public void registerWifiReceiver() {
        view.getContext().registerReceiver(wifiReceiver, wifiIntentFilter);
    }

    @Override
    public void unregisterWifiReceiver() {
        view.getContext().unregisterReceiver(wifiReceiver);
    }

    @Override
    public void startDiscovering() {
        wifiManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(CHILD_TAG, "onSuccess: peer discovery initiates successfully.");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(CHILD_TAG, "onSuccess: peer discovery initiation failed.");
            }
        });
    }

    @Override
    public void stopDiscovering() {
        wifiManager.stopPeerDiscovery(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(CHILD_TAG, "onSuccess: peer discovery stops successfully.");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(CHILD_TAG, "onSuccess: peer discovery stopping failure.");
            }
        });
    }
}
