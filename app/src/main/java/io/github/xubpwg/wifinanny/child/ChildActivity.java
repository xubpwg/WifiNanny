package io.github.xubpwg.wifinanny.child;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import io.github.xubpwg.wifinanny.R;

public class ChildActivity extends AppCompatActivity implements ChildViewInterface {

    private Button startMonitoringButton;
    private Button stopMonitoringButton;

    private ChildPresenterInterface presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child);

        initializeView();
        presenter.attachView(this);
        presenter.initWifiReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();

        presenter.attachView(this);
        presenter.registerWifiReceiver();
        presenter.startDiscovering();
    }

    @Override
    protected void onPause() {
        super.onPause();

        presenter.stopDiscovering();
        presenter.unregisterWifiReceiver();
        presenter.detachView();
    }

    @Override
    public void showToast(String toastText) {
        Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    private void initializeView() {

        startMonitoringButton = findViewById(R.id.start_monitoring_button);
        startMonitoringButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.startMonitoring();
            }
        });

        stopMonitoringButton = findViewById(R.id.stop_monitoring_button);
        stopMonitoringButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.stopMonitoring();
            }
        });

        presenter = new ChildPresenter();
    }
}
