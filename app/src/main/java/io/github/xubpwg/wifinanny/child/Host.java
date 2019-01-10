package io.github.xubpwg.wifinanny.child;

public class Host implements HostInterface{

    private String hostAddress;

    public Host(String address) {
        this.hostAddress = address;
    }

    @Override
    public String getHostAddress() {
        return hostAddress;
    }
}
