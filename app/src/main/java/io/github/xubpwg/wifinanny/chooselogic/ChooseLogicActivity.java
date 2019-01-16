package io.github.xubpwg.wifinanny.chooselogic;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import io.github.xubpwg.wifinanny.R;

public class ChooseLogicActivity extends AppCompatActivity implements ChooseLogicViewInterface{

    private Button childButton;
    private Button parentButton;

    private ChooseLogicPresenterInterface presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chooselogic);
        initializeView();

        presenter.attachView(this);
        presenter.checkPermissions();
        presenter.forceWifiOn();
    }

    @Override
    protected void onResume() {
        super.onResume();

        presenter.attachView(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        presenter.detachView();
    }

    private void initializeView() {

        childButton = findViewById(R.id.child_button);
        childButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.launchChild();
            }
        });

        parentButton = findViewById(R.id.parent_button);
        parentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.launchParent();
            }
        });

        presenter = new ChooseLogicPresenter();
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    public Activity getActivity() {
        return this;
    }
}
