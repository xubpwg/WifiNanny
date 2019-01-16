package io.github.xubpwg.wifinanny.chooselogic;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;

import io.github.xubpwg.wifinanny.child.ChildActivity;
import io.github.xubpwg.wifinanny.parent.ParentActivity;

public class ChooseLogicPresenter implements ChooseLogicPresenterInterface{

    private ChooseLogicViewInterface view;

    @Override
    public void attachView(ChooseLogicViewInterface view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void launchChild() {
        launch(ChildActivity.class);
    }

    @Override
    public void launchParent() {
        launch(ParentActivity.class);
    }

    @Override
    public void forceWifiOn() {
        WifiManager wifiManager = (WifiManager) view.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
    }

    @Override
    public void checkPermissions() {
        ActivityCompat.requestPermissions(view.getActivity(),
                new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.CHANGE_WIFI_STATE,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.CHANGE_NETWORK_STATE,
                        Manifest.permission.INTERNET,
                        Manifest.permission.FOREGROUND_SERVICE,
                        Manifest.permission.RECORD_AUDIO},
                10);
    }

    private void launch(Class activityClass) {
        Intent intent = new Intent(view.getContext(), activityClass);
        view.getContext().startActivity(intent);
    }
}
