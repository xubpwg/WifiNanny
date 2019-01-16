package io.github.xubpwg.wifinanny.parent;

public interface ParentPresenterInterface {

    void attachView(ParentActivity activity);

    void initWifiReceiver();

    void registerWifiReceiver();

    void startDiscovering();

    void stopDiscovering();

    void startListening();

    void stopListening();

    void unregisterWifiReceiver();

    void detachView();

    void startConnectionScenario();

    void startDisconnectionScenario();

    void initView();
}
