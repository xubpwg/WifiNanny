package io.github.xubpwg.wifinanny.parent;

import android.content.Context;

public interface ParentViewInterface {

    Context getContext();

    void showToast(String s);

    void openAvailableDevicesDialog();
}
