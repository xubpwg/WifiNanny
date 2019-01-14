package io.github.xubpwg.wifinanny.parent;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import io.github.xubpwg.wifinanny.R;

public class PeersListAdapter extends RecyclerView.Adapter<DeviceViewHolder> {

    private final PeersListPresenterInterface presenter;

    public PeersListAdapter(ParentPresenter presenter) {
        this.presenter = presenter;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new DeviceViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.itemview_device, viewGroup, false), (ParentPresenter) presenter);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder viewHolder, int i) {
        presenter.onBindDeviceViewAtPosition(i, viewHolder);
    }

    @Override
    public int getItemCount() {
        return presenter.getPeersCount();
    }
}
