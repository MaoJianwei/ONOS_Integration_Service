package org.onosproject.mao.device;

import org.onlab.packet.ChassisId;
import org.onosproject.app.ApplicationService;
import org.onosproject.core.CoreService;
import org.onosproject.mao.gui.MaoDeviceService;
import org.onosproject.net.*;
import org.onosproject.net.device.*;
import org.onosproject.net.link.LinkProviderRegistry;
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

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceProviderRegistry deviceProviderRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceAdminService deviceAdminService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceService deviceService;


    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected LinkProviderRegistry linkProviderRegistry;


    private MaoDeviceProvider maoDeviceProvider;
    private DeviceProviderService deviceProviderService;

    @Activate
    protected void activate() {
        log.info("Mao Device activating.");

        maoDeviceProvider = new MaoDeviceProvider();
        deviceProviderService = deviceProviderRegistry.register(maoDeviceProvider);

        log.info("Mao Device activated.");
    }

    @Deactivate
    protected void deactivate() {
        log.info("Mao Device deactivating.");

        removeDevice();
        deviceProviderRegistry.unregister(maoDeviceProvider);
        deviceProviderService = null;
        maoDeviceProvider = null;

        log.info("Mao Device deactivated.");
    }

    @Override
    public void addDevice() {

        SparseAnnotations sparseAnnotations = DefaultAnnotations.builder()
                .set(AnnotationKeys.PROTOCOL, "Mao protocol")
                .build();
        DeviceDescription deviceDescription = new DefaultDeviceDescription(
                URI.create("mao:1"),
                Device.Type.ROUTER,
                "Mao Manufacturer",
                "Mao Hardware v1.0",
                "Mao Software v3.0",
                "Mao Serial 0-0-0-2-3-6",
                new ChassisId("7181"),
                sparseAnnotations);
        deviceProviderService.deviceConnected(DeviceId.deviceId("mao:1"), deviceDescription);
    }

    @Override
    public void removeDevice() {
        deviceProviderService.deviceDisconnected(DeviceId.deviceId("mao:1"));
    }

    @Override
    public void addPort() {
        DeviceId deviceId = DeviceId.deviceId("mao:1");
        List<PortDescription> portDescriptionList = new ArrayList<>();

        PortDescription portDescription = DefaultPortDescription.builder()
                .withPortNumber(PortNumber.portNumber(1, "ens10086"))
                .type(Port.Type.FIBER)
                .portSpeed(800000)
                .isEnabled(true)
                .isRemoved(true)
                .build();

        portDescriptionList.add(portDescription);
        log.info("Mao add port\n {}", portDescription);
        deviceProviderService.updatePorts(deviceId, portDescriptionList);
    }

    @Override
    public void removePort() {
        DeviceId deviceId = DeviceId.deviceId("mao:1");
        PortDescription portDescription = DefaultPortDescription.builder()
                .withPortNumber(PortNumber.portNumber(1, "ens10086"))
                .type(Port.Type.FIBER)
                .portSpeed(800000)
                .isEnabled(true)
                .isRemoved(true)
                .build();
        log.info("Mao remove port\n {}", portDescription);
        deviceProviderService.deletePort(deviceId, portDescription);
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
}
