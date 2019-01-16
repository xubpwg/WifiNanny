package io.github.xubpwg.wifinanny.child;

public interface ChildPresenterInterface {

    void attachView(ChildActivity activity);

    void detachView();

    void startMonitoring();

    void stopMonitoring();

    void initWifiReceiver();

    void registerWifiReceiver();

    void unregisterWifiReceiver();

    void startDiscovering();

    void stopDiscovering();

    void initView();
}
