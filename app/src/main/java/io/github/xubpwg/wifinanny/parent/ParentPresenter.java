package io.github.xubpwg.wifinanny.parent;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import io.github.xubpwg.wifinanny.WifiDirectBroadcastReceiver;

public class ParentPresenter implements ParentPresenterInterface, PeersListPresenterInterface{

    private static final String PARENT_TAG = "Parent";

    private ParentViewInterface view;

    private WifiP2pManager wifiManager;
    private WifiP2pManager.Channel channel;
    private WifiDirectBroadcastReceiver wifiReceiver;
    private IntentFilter wifiIntentFilter;

    @Override
    public void attachView(ParentActivity activity) {
        this.view = activity;
    }

    @Override
    public void detachView() {
        this.view = null;
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
                Log.d(PARENT_TAG, "onSuccess: peer discovery initiates successfully.");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(PARENT_TAG, "onSuccess: peer discovery initiation failed.");
            }
        });
    }

    @Override
    public void stopDiscovering() {
        wifiManager.stopPeerDiscovery(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(PARENT_TAG, "onSuccess: peer discovery stops successfully.");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(PARENT_TAG, "onSuccess: peer discovery stopping failure.");
            }
        });
    }

    @Override
    public void startListening() {
        Intent intent = new Intent(view.getContext(), AlertHandlingService.class);
        intent.setAction(AlertHandlingService.ACTION_START_ALERT_HANDLING_SERVICE);
        view.getContext().startService(intent);
    }

    @Override
    public void stopListening() {
        Intent intent = new Intent(view.getContext(), AlertHandlingService.class);
        intent.setAction(AlertHandlingService.ACTION_STOP_ALERT_HANDLING_SERVICE);
        view.getContext().startService(intent);
    }

    @Override
    public void startConnectionScenario() {
        view.showConnectionDialog();

    }

    @Override
    public void startDisconnectionScenario() {

    }

    @Override
    public void onBindDeviceViewAtPosition(int i, DeviceViewHolder viewHolder) {
        WifiP2pDevice device = (WifiP2pDevice) wifiReceiver.getPeerList().getDeviceList().toArray()[i];
        viewHolder.setDeviceName(device.deviceName);
        viewHolder.setDeviceAddress(device.deviceAddress);
    }

    @Override
    public int getPeersCount() {
        return wifiReceiver.getPeerList().getDeviceList().size();
    }

    @Override
    public void onItemClickedAtPosition(int adapterPosition) {
        view.startShowProgress();

        WifiP2pConfig config = new WifiP2pConfig();
        final WifiP2pDevice device = (WifiP2pDevice) wifiReceiver.getPeerList().getDeviceList().toArray()[adapterPosition];
        config.deviceAddress = device.deviceAddress;

        wifiManager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(PARENT_TAG, "onSuccess: successfully connected.");
                view.showToast("Successfully connected to " + device.deviceName);
                view.stopShowProgress();
                view.closeConnectionDialog();
            }

            @Override
            public void onFailure(int reason) {
                Log.d(PARENT_TAG, "onSuccess: successfully connected.");
                view.showToast("Connection failure");
                view.stopShowProgress();
            }
        });
    }
}
