package io.github.xubpwg.wifinanny.chooselogic;

import android.content.Intent;

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

    private void launch(Class activityClass) {
        Intent intent = new Intent(view.getContext(), activityClass);
        view.getContext().startActivity(intent);
    }
}
