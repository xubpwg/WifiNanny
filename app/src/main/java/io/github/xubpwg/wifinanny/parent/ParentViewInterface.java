package io.github.xubpwg.wifinanny.parent;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

public interface ParentViewInterface {

    Context getContext();

    void showToast(String s);

    RecyclerView getRecyclerView();

    void startShowProgress();

    void stopShowProgress();

    void showConnectionDialog();

    void closeConnectionDialog();

    void refresh();
}
