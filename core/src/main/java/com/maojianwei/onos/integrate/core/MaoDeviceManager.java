package com.maojianwei.onos.integrate.core;

import org.onlab.packet.ChassisId;
import com.maojianwei.onos.integrate.api.MaoDeviceService;
import org.onosproject.net.*;
import org.onosproject.net.device.*;
import org.onosproject.net.link.*;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component(
        immediate = true,
        service = MaoDeviceService.class
)
public class MaoDeviceManager implements MaoDeviceService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String UNDEFINED_STR = "-";
    private static final ChassisId UNDEFINED_CHASSIS_ID = new ChassisId();

    private static final String SCHEME_PREFIX = "mao:";


    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceProviderRegistry deviceProviderRegistry;

//    @Reference(cardinality = ReferenceCardinality.MANDATORY)
//    protected DeviceAdminService deviceAdminService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected LinkProviderRegistry linkProviderRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected LinkService linkService;

    private MaoDeviceProvider maoDeviceProvider;
    private DeviceProviderService deviceProviderService;

    private MaoLinkProvider maoLinkProvider;
    private LinkProviderService linkProviderService;


    private Map<DeviceId, Boolean> deviceOnlineStatus;

    private Map<DeviceId, List<PortDescription>> devicePortDescription;

    private Map<DeviceId, ReentrantLock> deviceLocks;


    private final Object addDeviceLock = new Object();

    @Activate
    protected void activate() {
        log.info("Mao Device activating.");

        devicePortDescription = new ConcurrentHashMap<>();
        deviceOnlineStatus = new ConcurrentHashMap<>();
        deviceLocks = new ConcurrentHashMap<>();

        maoDeviceProvider = new MaoDeviceProvider();
        deviceProviderService = deviceProviderRegistry.register(maoDeviceProvider);

        maoLinkProvider = new MaoLinkProvider();
        linkProviderService = linkProviderRegistry.register(maoLinkProvider);

        log.info("Mao Device activated.");
    }

    @Deactivate
    protected void deactivate() {
        log.info("Mao Device deactivating.");

        deviceProviderRegistry.unregister(maoDeviceProvider);
        deviceProviderService = null;
        maoDeviceProvider = null;

        linkProviderRegistry.unregister(maoLinkProvider);
        linkProviderService = null;
        maoLinkProvider = null;

        devicePortDescription.clear();
        devicePortDescription = null;

        deviceOnlineStatus.clear();
        deviceOnlineStatus = null;

        deviceLocks.clear();
        deviceLocks = null;

        log.info("Mao Device deactivated.");
    }


    /**
     * Generate DeviceId object from the ID part (not include Scheme part).
     *
     * @param deviceIdStr Just the ID part, not include Scheme part.
     * @return
     */
    @Override
    public DeviceId genDeviceId(String deviceIdStr) {
        return DeviceId.deviceId(SCHEME_PREFIX + deviceIdStr);
    }

    @Override
    public void addDevice(DeviceId deviceId) {
        addDevice(deviceId, UNDEFINED_STR, UNDEFINED_STR, UNDEFINED_STR);
    }

    /**
     * @param name
     * @param deviceId           Just the ID part, not include Scheme part.
     * @param version
     * @param managementProtocol
     */
    @Override
    public void addDevice(DeviceId deviceId, String name, String version, String managementProtocol) {

//        log.info("Mao: addDevice invoked by {}", deviceId);

        Boolean isOnline = deviceOnlineStatus.getOrDefault(deviceId, false);
        if (!isOnline) {
            synchronized (addDeviceLock) {
                isOnline = deviceOnlineStatus.getOrDefault(deviceId, false);
                if (!isOnline) {

                    SparseAnnotations sparseAnnotations = DefaultAnnotations.builder()
                            .set(AnnotationKeys.PROTOCOL, managementProtocol)
                            .set(AnnotationKeys.NAME, name) // 	FRIENDLY NAME
                            .build();
                    DeviceDescription deviceDescription = new DefaultDeviceDescription(
                            deviceId.uri(),
                            Device.Type.ROUTER,
                            UNDEFINED_STR,
                            UNDEFINED_STR,
                            version,
                            UNDEFINED_STR,
                            UNDEFINED_CHASSIS_ID,
                            sparseAnnotations);

                    deviceLocks.putIfAbsent(deviceId, new ReentrantLock());
                    deviceOnlineStatus.put(deviceId, true);
                    deviceProviderService.deviceConnected(deviceId, deviceDescription);
                }
            }
        }
    }

    @Override
    public void removeDevice(DeviceId deviceId) {

//        log.info("Mao: removeDevice invoked by {}", deviceId);

        Boolean isOnline = deviceOnlineStatus.getOrDefault(deviceId, false);
        if (isOnline) {
            deviceOnlineStatus.put(deviceId, false);
            deviceProviderService.deviceDisconnected(deviceId);
            deviceLocks.remove(deviceId);
        }
    }


    @Override
    public void addPort(DeviceId deviceId, int portId) {
        addPort(deviceId, portId, UNDEFINED_STR);
    }

    @Override
    public void addPort(DeviceId deviceId, int portId, String portName) {

        // Avoid dirty data.
        // If we remove a port while the device is disconnected, the port will not be removed and become dirty data.
        if (!deviceService.isAvailable(deviceId)) {
            log.warn("Mao: device is unavailable, port is not added - {}, {}, {}", deviceId, portId, portName);
            return;
        }

        ReentrantLock lock = deviceLocks.getOrDefault(deviceId, null);
        if (lock == null) {
            log.warn("Mao: device is not ready for addPort, lock doesn't exist - {}, {}, {}", deviceId, portId, portName);
            return;
        }

        lock.lock();
        try {
            List<PortDescription> portDescriptionList = devicePortDescription.getOrDefault(deviceId, new ArrayList<>());

            PortDescription oldStatus = null;
            for (int i = 0; i < portDescriptionList.size(); i++) {
                if (portDescriptionList.get(i).portNumber().toLong() == portId) {
                    oldStatus = portDescriptionList.remove(i);
                }
            }
            PortDescription newStatus;
            if (oldStatus != null) {
                DefaultAnnotations annotations = DefaultAnnotations.builder()
                        .putAll(oldStatus.annotations())
                        .set(AnnotationKeys.PORT_NAME, portName)
                        .build();

                newStatus = DefaultPortDescription.builder()
                        .withPortNumber(oldStatus.portNumber())
                        .type(oldStatus.type())
                        .portSpeed(oldStatus.portSpeed())
                        .isEnabled(oldStatus.isEnabled())
                        .isRemoved(oldStatus.isRemoved())
                        .annotations(annotations)
                        .build();
            } else {
                DefaultAnnotations annotations = DefaultAnnotations.builder()
                        .set(AnnotationKeys.PORT_NAME, portName)
                        .build();

                newStatus = DefaultPortDescription.builder()
                        .withPortNumber(PortNumber.portNumber(portId))
                        .annotations(annotations)
                        .type(Port.Type.FIBER)
                        .portSpeed(800000)
                        .isEnabled(true)
                        .isRemoved(false)
                        .build();
            }

            portDescriptionList.add(newStatus);
            log.info("Mao add port\n {}", newStatus);
            devicePortDescription.put(deviceId, portDescriptionList);
            deviceProviderService.updatePorts(deviceId, portDescriptionList);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void removePort(DeviceId deviceId, int portId) {

        // Avoid dirty data.
        // If we remove a port while the device is disconnected, the port will not be removed and become dirty data.
        if (!deviceService.isAvailable(deviceId)) {
            log.warn("Mao: device is unavailable, port is not removed - {}, {}", deviceId, portId);
            return;
        }


        ReentrantLock lock = deviceLocks.getOrDefault(deviceId, null);
        if (lock == null) {
            log.warn("Mao: device is not ready for removePort, lock doesn't exist - {}, {}", deviceId, portId);
            return;
        }

        lock.lock();
        try {
            List<PortDescription> portDescriptionList = devicePortDescription.get(deviceId);

            for (int i = 0; i < portDescriptionList.size(); i++) {
                if (portDescriptionList.get(i).portNumber().toLong() == portId) {
                    PortDescription portDescription = portDescriptionList.remove(i);
                    log.info("Mao remove port with description\n {}", portDescription);
                    deviceProviderService.deletePort(deviceId, portDescription);
                    return;
                }
            }
            log.info("Mao remove port: portDescription not found: {} - {}\n", deviceId, portId);
        } finally {
            lock.unlock();
        }
    }

    @Deprecated
    @Override
    public void changePortStatus() {
        DeviceId deviceId = DeviceId.deviceId("mao:1");
        PortDescription portDescription = DefaultPortDescription.builder()
                .withPortNumber(PortNumber.portNumber(1, "ens10086"))
                .type(Port.Type.FIBER)
                .portSpeed(400000)
                .isEnabled(true)
                .isRemoved(true)
                .build();
        deviceProviderService.portStatusChanged(deviceId, portDescription);
    }

    @Override
    public void addLink(DeviceId src, int srcPort, DeviceId dst, int dstPort) {

        addLink(src, srcPort, UNDEFINED_STR, dst, dstPort, UNDEFINED_STR);
    }

    @Override
    public void addLink(DeviceId src, int srcPort, String srcPortName, DeviceId dst, int dstPort, String dstPortName) {

//        log.info("Mao: addLink invoked by {} -> {}", src, dst);



        ConnectPoint srcCP = new ConnectPoint(src, PortNumber.portNumber(srcPort));
        ConnectPoint dstCP = new ConnectPoint(dst, PortNumber.portNumber(dstPort));

        if (deviceService.getPort(srcCP) == null) {
            addPort(src, srcPort, srcPortName);
        }
        if (deviceService.getPort(dstCP) == null) {
            addPort(dst, dstPort, dstPortName);
        }


        DefaultAnnotations annotations = DefaultAnnotations.builder()
                .set(AnnotationKeys.PROTOCOL, "MaoLinkProtocol")
                .set(AnnotationKeys.LAYER, "MaoLayer")
                .build();
        LinkDescription linkDescription = new DefaultLinkDescription(srcCP, dstCP, Link.Type.DIRECT, DefaultLinkDescription.EXPECTED, annotations);
        linkProviderService.linkDetected(linkDescription);
        linkDescription = new DefaultLinkDescription(dstCP, srcCP, Link.Type.DIRECT, DefaultLinkDescription.EXPECTED, annotations);
        linkProviderService.linkDetected(linkDescription);
    }


    @Override
    public void removeLink(DeviceId src, int srcPort, DeviceId dst, int dstPort) {

//        log.info("Mao: removeLink invoked by {} -> {}", src, dst);


        ConnectPoint srcCP = new ConnectPoint(src, PortNumber.portNumber(srcPort));
        ConnectPoint dstCP = new ConnectPoint(dst, PortNumber.portNumber(dstPort));
        DefaultAnnotations annotations = DefaultAnnotations.builder()
                .set(AnnotationKeys.PROTOCOL, "MaoLinkProtocol")
                .set(AnnotationKeys.LAYER, "MaoLayer")
                .build();
        LinkDescription linkDescription = new DefaultLinkDescription(srcCP, dstCP, Link.Type.DIRECT, DefaultLinkDescription.EXPECTED, annotations);
        linkProviderService.linkVanished(linkDescription);

        linkDescription = new DefaultLinkDescription(dstCP, srcCP, Link.Type.DIRECT, DefaultLinkDescription.EXPECTED, annotations);
        linkProviderService.linkVanished(linkDescription);

        if (deviceService.getPort(srcCP) != null) {
            removePort(src, srcPort);
        }
        if (deviceService.getPort(dstCP) != null) {
            removePort(dst, dstPort);
        }
    }

    public void removeAllLinks(DeviceId deviceId) {
        linkProviderService.linksVanished(deviceId);
    }


    private class MaoDeviceProvider extends AbstractProvider implements DeviceProvider {

        /**
         * Creates a provider with the supplied identifier.
         */
        protected MaoDeviceProvider() {
            // Mao: caution!
            // 1. Provider Scheme must be lower-case
            // 2. DeviceId must be lower-case, and it must obey the Provider Scheme.
            // 3. DeviceId must be "<provider-scheme>" + ":" + "<device-id>" format.
            super(new ProviderId("mao", "Mao-Device-Provider"));
        }

        @Override
        public void triggerProbe(DeviceId deviceId) {

        }

        @Override
        public void roleChanged(DeviceId deviceId, MastershipRole newRole) {
            if (deviceOnlineStatus.getOrDefault(deviceId, false)) {
                log.info("Mao: the role for device {} changed to {}", deviceId, newRole);
                deviceProviderService.receivedRoleReply(deviceId, newRole);
            }
        }

        @Override
        public boolean isReachable(DeviceId deviceId) {
            return deviceOnlineStatus.getOrDefault(deviceId, false);
        }

        @Override
        public boolean isAvailable(DeviceId deviceId) {
            return isReachable(deviceId);
        }

        @Override
        public void changePortState(DeviceId deviceId, PortNumber portNumber, boolean enable) {

        }

        @Override
        public void triggerDisconnect(DeviceId deviceId) {

        }
    }

    private class MaoLinkProvider extends AbstractProvider implements LinkProvider {
        protected MaoLinkProvider() {
            super(new ProviderId("maolink", "Mao-Link-Provider"));
        }
    }
}
