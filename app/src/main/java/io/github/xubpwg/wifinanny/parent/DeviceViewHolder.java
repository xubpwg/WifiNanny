package io.github.xubpwg.wifinanny.parent;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import io.github.xubpwg.wifinanny.R;

public class DeviceViewHolder extends RecyclerView.ViewHolder implements DeviceViewInterface{

    private TextView deviceNameTextView;
    private TextView deviceAddressTextView;

    public DeviceViewHolder(@NonNull final View itemView, final ParentPresenter presenter) {
        super(itemView);
        deviceNameTextView = itemView.findViewById(R.id.device_name_textview);
        deviceAddressTextView = itemView.findViewById(R.id.device_address_textview);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onItemClickedAtPosition(getAdapterPosition());
            }
        });
    }

    @Override
    public void setDeviceName(String deviceName) {
        deviceNameTextView.setText(deviceName);
    }

    @Override
    public void setDeviceAddress(String deviceAddress) {
        deviceAddressTextView.setText(deviceAddress);
    }
}
