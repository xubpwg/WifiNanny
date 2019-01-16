package io.github.xubpwg.wifinanny.parent;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import io.github.xubpwg.wifinanny.R;

public class ParentActivity extends AppCompatActivity implements ParentViewInterface{

    private static final String PARENT_V_TAG = "ParentV";

    private ParentPresenterInterface presenter;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private PeersListAdapter adapter;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;

    private Button connectButton;
    // private Button disconnectButton;
    private Button stopListeningButton;
    private Button startListeningButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent);

        presenter = new ParentPresenter();
        presenter.attachView(this);
        presenter.initView();
        if (this.getIntent().getIntExtra("START_FROM_SERVICE", 0) != 200) {
            presenter.initWifiReceiver();
            Log.d(PARENT_V_TAG, "onCreate: intent int is " + this.getIntent().getIntExtra("START_FROM_SERVICE", 0));
        } else {
            setButtonsStopListening();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        presenter.attachView(this);
        if (getIntent().getIntExtra("START_FROM_SERVICE", 0) != 200) {
            presenter.registerWifiReceiver();
            presenter.startDiscovering();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (getIntent().getIntExtra("START_FROM_SERVICE", 0) != 200) {
            presenter.stopDiscovering();
            presenter.unregisterWifiReceiver();
        }
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
    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    @Override
    public void startShowProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void stopShowProgress() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showConnectionDialog() {
        dialog = builder.show();
    }

    @Override
    public void closeConnectionDialog() {
        dialog.cancel();
    }

    @Override
    public void refresh() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void initializeView() {

        connectButton = findViewById(R.id.connect_button);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.startConnectionScenario();
            }
        });

        /**disconnectButton = findViewById(R.id.disconnect_button);
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.startDisconnectionScenario();
            }
        }); */

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

        progressBar = findViewById(R.id.progress_bar);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_availabledevices, (ViewGroup) findViewById(android.R.id.content) , false);
        recyclerView = dialogView.findViewById(R.id.peers_list_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PeersListAdapter((ParentPresenter) presenter);
        recyclerView.setAdapter(adapter);

        builder = new AlertDialog.Builder(this)
                .setView(dialogView);
    }

    @Override
    public void setButtonsInitialState() {
        connectButton.setEnabled(false);
        // disconnectButton.setEnabled(false);
        startListeningButton.setEnabled(false);
        stopListeningButton.setEnabled(false);
    }

    @Override
    public void setButtonsConnect() {
        connectButton.setEnabled(true);
        // disconnectButton.setEnabled(false);
        startListeningButton.setEnabled(false);
        stopListeningButton.setEnabled(false);
    }

    @Override
    public void setButtonsStartListening() {
        connectButton.setEnabled(false);
        // disconnectButton.setEnabled(true);
        startListeningButton.setEnabled(true);
        stopListeningButton.setEnabled(false);
    }

    @Override
    public void setButtonsStopListening() {
        connectButton.setEnabled(false);
        // disconnectButton.setEnabled(false);
        startListeningButton.setEnabled(false);
        stopListeningButton.setEnabled(true);
    }

    @Override
    public ParentActivity getActivity() {
        return this;
    }
}
