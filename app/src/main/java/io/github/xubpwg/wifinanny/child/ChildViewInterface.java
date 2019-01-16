package io.github.xubpwg.wifinanny.child;

import android.content.Context;
import android.content.Intent;

public interface ChildViewInterface {

    void showToast(String toastText);

    Context getContext();

    void initializeView();

    Intent getIntent();

    void setButtonsInitialState();

    void setButtonsStartMonitoring();

    void setButtonsStopMonitoring();

    ChildActivity getActivity();
}
