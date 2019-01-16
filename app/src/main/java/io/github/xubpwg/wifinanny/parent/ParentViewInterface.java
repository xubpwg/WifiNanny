package io.github.xubpwg.wifinanny.parent;

import android.content.Context;
import android.content.Intent;
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

    Intent getIntent();

    void initializeView();

    void setButtonsInitialState();

    void setButtonsConnect();

    void setButtonsStartListening();

    void setButtonsStopListening();

    ParentActivity getActivity();
}
