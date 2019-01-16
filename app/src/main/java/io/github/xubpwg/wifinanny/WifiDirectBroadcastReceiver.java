package io.github.xubpwg.wifinanny;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import io.github.xubpwg.wifinanny.child.ChildActivity;
import io.github.xubpwg.wifinanny.child.ChildViewInterface;
import io.github.xubpwg.wifinanny.parent.ParentActivity;
import io.github.xubpwg.wifinanny.parent.ParentViewInterface;

public class WifiDirectBroadcastReceiver extends BroadcastReceiver {

    private static final String WIFI_TAG = "WiFiDirectBR";

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;

    private WifiP2pDeviceList peerList;
    private String hostAddress;

    private ParentViewInterface parentView;
    private ChildViewInterface childView;

    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {

        @Override
        public void onPeersAvailable(WifiP2pDeviceList peers) {
            Log.d(WIFI_TAG, "onPeersAvailable: device list is " + peers.getDeviceList());

            if (!peers.getDeviceList().isEmpty()) {
                if (peerList == null) {
                    peerList = new WifiP2pDeviceList(peers);
                } else if (!peerList.equals(peers)) {
                    peerList = peers;
                }
                Log.d(WIFI_TAG, "onPeersAvailable: saved device list is: " + peerList);

                WifiP2pDevice device = (WifiP2pDevice) peerList.getDeviceList().toArray()[0];
                if (device.status != WifiP2pDevice.CONNECTED && parentView != null) {
                    parentView.setButtonsConnect();
                }
            }
        }
    };

    public WifiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, ParentActivity parentView) {
        this.manager = manager;
        this.channel = channel;
        this.parentView = parentView;
    }

    public WifiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, ChildActivity childView) {
        this.manager = manager;
        this.channel = channel;
        this.childView = childView;
    }

    public WifiP2pManager getManager() {
        return manager;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            // Check to see if Wi-Fi is enabled and notify appropriate activity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi P2P is enabled
                Log.d(WIFI_TAG, "onReceive: WiFi P2P is enabled.");
            } else {
                // Wi-Fi P2P is not enabled
                Log.d(WIFI_TAG, "onReceive: WiFi P2P is disabled.");
            }

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // Call WifiP2pManager.requestPeers() to get a list of current peers
            if (manager != null) {
                manager.requestPeers(channel, peerListListener);
            }

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            Log.d(WIFI_TAG, "onReceive: connection changed action");

            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (childView != null) {
                int intFromService = childView.getIntent().getIntExtra("START_FROM_SERVICE", 0);
                if (networkInfo.isConnected() & intFromService != 100) {
                    childView.setButtonsStartMonitoring();
                }
            } else if (parentView != null) {
                int intFromService = parentView.getIntent().getIntExtra("START_FROM_SERVICE", 0);
                if (networkInfo.isConnected() & intFromService != 200) {
                    parentView.setButtonsStartListening();
                }
            }
            // Respond to new connection or disconnections
            /** if (peerList != null) {
                for (int i = 0; i < peerList.getDeviceList().size(); i++) {
                    WifiP2pDevice device = (WifiP2pDevice) peerList.getDeviceList().toArray()[i];
                    if (device.status == WifiP2pDevice.CONNECTED) {
                        manager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
                            @Override
                            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                                if (info != null) {
                                    hostAddress = info.groupOwnerAddress.getHostAddress();
                                    Log.d(WIFI_TAG, "onConnectionInfoAvailable: host address is " + hostAddress);
                                }
                            }
                        });
                    }
                }
            } */

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public WifiP2pDeviceList getPeerList() {
        return peerList;
    }
}
