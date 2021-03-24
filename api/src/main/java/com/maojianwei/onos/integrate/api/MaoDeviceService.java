package com.maojianwei.onos.integrate.api;

import org.onosproject.net.DeviceId;

public interface MaoDeviceService {

    DeviceId genDeviceId(String deviceIdStr);

    void addDevice(DeviceId deviceId);
    void addDevice(DeviceId deviceId, String name, String version, String managementProtocol);
    void removeDevice(DeviceId deviceId);

    void addPort();
    void removePort();
    void changePortStatus();

    void addLink();
    void removeLink();
    void removeAllLinks(DeviceId deviceId);
}
