package com.maojianwei.onos.integrate.api;

import org.onosproject.net.DeviceId;

public interface MaoDeviceService {

    DeviceId genDeviceId(String deviceIdStr);

    void addDevice(DeviceId deviceId);
    void addDevice(DeviceId deviceId, String name, String version, String managementProtocol);
    void removeDevice(DeviceId deviceId);

    void addPort(DeviceId deviceId, int portId);
    void addPort(DeviceId deviceId, int portId, String portName);
    void removePort(DeviceId deviceId, int portId);
    void changePortStatus();

    /**
     * For tag:
     * "" means "no tag, no color"
     * " " means "no tag, only color"
     *
     * @param deviceId .
     * @param portId .
     * @param tagLevel .
     * @param tag .
     */
    void updatePortTag(DeviceId deviceId, long portId, String tagLevel, String tag);
    void reportPortStatistics(DeviceId deviceId, long portId, long sendTotalBytes, long sendTotalPackets);

    void addLink(DeviceId src, int srcPort, DeviceId dst, int dstPort);
    void addLink(DeviceId src, int srcPort, String srcPortName, DeviceId dst, int dstPort, String dstPortName);
    void removeLink(DeviceId src, int srcPort, DeviceId dst, int dstPort);
    void removeAllLinks(DeviceId deviceId);
}
