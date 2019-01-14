package io.github.xubpwg.wifinanny.parent;

interface PeersListPresenterInterface {

    void onBindDeviceViewAtPosition(int i, DeviceViewHolder viewHolder);

    int getPeersCount();

    void onItemClickedAtPosition(int adapterPosition);
}
