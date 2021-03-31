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


    @Activate
    protected void activate() {
        log.info("Mao Device activating.");

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
        deviceProviderService.deviceConnected(deviceId, deviceDescription);
    }

    @Override
    public void removeDevice(DeviceId deviceId) {
        deviceProviderService.deviceDisconnected(deviceId);
    }


    @Override
    public void addPort(DeviceId deviceId, int portId) {
        addPort(deviceId, portId, UNDEFINED_STR);
    }

    @Override
    public void addPort(DeviceId deviceId, int portId, String portName) {

        List<PortDescription> portDescriptionList = new ArrayList<>();

        DefaultAnnotations annotations = DefaultAnnotations.builder()
                .set(AnnotationKeys.PORT_NAME, portName)
                .build();

        PortDescription portDescription = DefaultPortDescription.builder()
                .withPortNumber(PortNumber.portNumber(portId))
                .annotations(annotations)
                .type(Port.Type.FIBER)
                .portSpeed(800000)
                .isEnabled(true)
                .isRemoved(false)
                .build();

        portDescriptionList.add(portDescription);
        log.info("Mao add port\n {}", portDescription);
        deviceProviderService.updatePorts(deviceId, portDescriptionList);


//
//        DeviceId deviceId1 = DeviceId.deviceId("mao:1");
//        DeviceId deviceId2 = DeviceId.deviceId("mao:2");
//
//        List<PortDescription> portDescriptionList = new ArrayList<>();
//
//        PortDescription portDescription = DefaultPortDescription.builder()
//                .withPortNumber(PortNumber.portNumber(1, "ens10086"))
//                .type(Port.Type.FIBER)
//                .portSpeed(800000)
//                .isEnabled(true)
//                .isRemoved(false)
//                .annotations()
//                .build();
//
//        portDescriptionList.add(portDescription);
//        log.info("Mao add port\n {}", portDescription);
//        deviceProviderService.updatePorts(deviceId1, portDescriptionList);
//
//
//        portDescriptionList.clear();
//        portDescription = DefaultPortDescription.builder()
//                .withPortNumber(PortNumber.portNumber(1, "ens10010"))
//                .type(Port.Type.FIBER)
//                .portSpeed(300000)
//                .isEnabled(true)
//                .isRemoved(false)
//                .build();
//
//        portDescriptionList.add(portDescription);
//        log.info("Mao add port\n {}", portDescription);
//        deviceProviderService.updatePorts(deviceId2, portDescriptionList);
    }

    @Override
    public void removePort(DeviceId deviceId, int portId) {

        PortDescription portDescription = DefaultPortDescription.builder()
                .withPortNumber(PortNumber.portNumber(portId))
                .type(Port.Type.FIBER)
                .portSpeed(800000)
                .isEnabled(false)
                .isRemoved(true)
                .build();
        log.info("Mao remove port\n {}", portDescription);
        deviceProviderService.deletePort(deviceId, portDescription);


//        DeviceId deviceId1 = DeviceId.deviceId("mao:1");
//        DeviceId deviceId2 = DeviceId.deviceId("mao:2");
//
//        PortDescription portDescription = DefaultPortDescription.builder()
//                .withPortNumber(PortNumber.portNumber(1, "ens10086"))
//                .type(Port.Type.FIBER)
//                .portSpeed(800000)
//                .isEnabled(false)
//                .isRemoved(true)
//                .build();
//        log.info("Mao remove port\n {}", portDescription);
//        deviceProviderService.deletePort(deviceId1, portDescription);
//
//        portDescription = DefaultPortDescription.builder()
//                .withPortNumber(PortNumber.portNumber(1, "ens10010"))
//                .type(Port.Type.FIBER)
//                .portSpeed(300000)
//                .isEnabled(false)
//                .isRemoved(true)
//                .build();
//        log.info("Mao remove port\n {}", portDescription);
//        deviceProviderService.deletePort(deviceId2, portDescription);
    }

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


//        ConnectPoint src = new ConnectPoint(DeviceId.deviceId("mao:1"), PortNumber.portNumber(1, "ens10086"));
//        ConnectPoint dst = new ConnectPoint(DeviceId.deviceId("mao:2"), PortNumber.portNumber(1, "ens10010"));
//        DefaultAnnotations annotations = DefaultAnnotations.builder()
//                .set(AnnotationKeys.PROTOCOL, "MaoLinkProtocol")
//                .set(AnnotationKeys.LAYER, "MaoLayer")
//                .build();
//        LinkDescription linkDescription = new DefaultLinkDescription(src, dst, Link.Type.DIRECT, DefaultLinkDescription.EXPECTED, annotations);
//        linkProviderService.linkDetected(linkDescription);
//
//        linkDescription = new DefaultLinkDescription(dst, src, Link.Type.DIRECT, DefaultLinkDescription.EXPECTED, annotations);
//        linkProviderService.linkDetected(linkDescription);
    }

    @Override
    public void addLink(DeviceId src, int srcPort, String srcPortName, DeviceId dst, int dstPort, String dstPortName) {
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


//        ConnectPoint src = new ConnectPoint(DeviceId.deviceId("mao:1"), PortNumber.portNumber(1, "ens10086"));
//        ConnectPoint dst = new ConnectPoint(DeviceId.deviceId("mao:2"), PortNumber.portNumber(1, "ens10010"));
//        DefaultAnnotations annotations = DefaultAnnotations.builder()
//                .set(AnnotationKeys.PROTOCOL, "MaoLinkProtocol")
//                .set(AnnotationKeys.LAYER, "MaoLayer")
//                .build();
//        LinkDescription linkDescription = new DefaultLinkDescription(src, dst, Link.Type.DIRECT, DefaultLinkDescription.EXPECTED, annotations);
//        linkProviderService.linkVanished(linkDescription);
//
//        linkDescription = new DefaultLinkDescription(dst, src, Link.Type.DIRECT, DefaultLinkDescription.EXPECTED, annotations);
//        linkProviderService.linkVanished(linkDescription);
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

        }

        @Override
        public boolean isReachable(DeviceId deviceId) {
            return true;
        }

        @Override
        public boolean isAvailable(DeviceId deviceId) {
            return true;
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
