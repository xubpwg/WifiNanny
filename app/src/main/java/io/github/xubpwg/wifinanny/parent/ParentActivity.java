package io.github.xubpwg.wifinanny.parent;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import io.github.xubpwg.wifinanny.R;

public class ParentActivity extends AppCompatActivity implements ParentViewInterface{

    private Button connectButton;
    private Button disconnectButton;
    private Button startListeningButton;
    private Button stopListeningButton;

    private ParentPresenterInterface presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent);

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
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    public void showToast(String toastMessage) {
        Toast.makeText(getContext(), toastMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void openAvailableDevicesDialog() {
        AlertDialog.Builder availableDevicesDialogBuilder = new AlertDialog.Builder(getApplicationContext())
                .setView(R.layout.dialog_availabledevices);
        availableDevicesDialogBuilder.show();

    }

    private void initializeView() {
        connectButton = findViewById(R.id.connect_button);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.startConnectionScenario();
            }
        });

        disconnectButton = findViewById(R.id.disconnect_button);
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.startDisconnectionScenario();
            }
        });

        startListeningButton = findViewById(R.id.start_listening_button);
        startListeningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.startListening();
            }
        });

        stopListeningButton = findViewById(R.id.stop_listening_button);
        stopListeningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.stopListening();
            }
        });

        presenter = new ParentPresenter();
    }
}
