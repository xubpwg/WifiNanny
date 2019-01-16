package io.github.xubpwg.wifinanny.chooselogic;

public interface ChooseLogicPresenterInterface {

    void attachView(ChooseLogicViewInterface view);

    void detachView();

    void launchChild();

    void launchParent();

    void forceWifiOn();

    void checkPermissions();
}
