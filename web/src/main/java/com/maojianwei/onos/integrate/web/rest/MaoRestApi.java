package com.maojianwei.onos.integrate.web.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.maojianwei.onos.integrate.api.MaoDeviceService;
import org.onosproject.net.DeviceId;
import org.onosproject.rest.AbstractWebResource;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

import static org.onlab.util.Tools.readTreeFromStream;

@Path("MaoIntegration")
public class MaoRestApi extends AbstractWebResource {


    private static final String JSON_KEY_DEVICE_ID = "deviceId";
    private static final String JSON_KEY_NAME = "deviceName";
    private static final String JSON_KEY_VERSION = "swVersion";
    private static final String JSON_KEY_MGR_PROTOCOL = "manageProtocol";

    private static final String JSON_KEY_ERR = "err";
    private static final String JSON_KEY_MSG = "msg";

    private static final int JSON_ERR_OK = 0;
    private static final int JSON_ERR_INPUT_PARAM_LACK = -1;
    private static final int JSON_ERR_INTERNAL = -500;


    /**
     * Add one device.
     *
     * @param stream json.
     * @return
     */
    @POST
    @Path("addDevice")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addDevice(InputStream stream) {
        MaoDeviceService maoDeviceService = getService(MaoDeviceService.class);

        ObjectNode root = mapper().createObjectNode();

        ObjectNode jsonTree;
        try {
            jsonTree = readTreeFromStream(mapper(), stream);
        } catch (IOException e) {
            root.put(JSON_KEY_ERR, JSON_ERR_INTERNAL);
            return ok(root).build();
        }
        JsonNode nodeDeviceId = jsonTree.get(JSON_KEY_DEVICE_ID);
        JsonNode nodeName = jsonTree.get(JSON_KEY_NAME);
        JsonNode nodeVersion = jsonTree.get(JSON_KEY_VERSION);
        JsonNode nodeProtocol = jsonTree.get(JSON_KEY_MGR_PROTOCOL);

        if (nodeDeviceId == null) {
            root.put(JSON_KEY_ERR, JSON_ERR_INPUT_PARAM_LACK);
            return ok(root).build();
        }
        DeviceId deviceId = maoDeviceService.genDeviceId(nodeDeviceId.asText());

        if (nodeName == null || nodeVersion == null || nodeProtocol == null) {
            maoDeviceService.addDevice(deviceId);
        } else {
            String name = nodeName.asText();
            String version = nodeVersion.asText();
            String manageProtocol = nodeProtocol.asText();

            maoDeviceService.addDevice(deviceId, name, version, manageProtocol);
        }
        root.put(JSON_KEY_ERR, JSON_ERR_OK);
        return ok(root).build();
    }

    /**
     * Remove one device.
     *
     * @param stream json.
     * @return
     */
    @POST
    @Path("removeDevice")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeDevice(InputStream stream) {
        MaoDeviceService maoDeviceService = getService(MaoDeviceService.class);

        ObjectNode root = mapper().createObjectNode();

        ObjectNode jsonTree;
        try {
            jsonTree = readTreeFromStream(mapper(), stream);
        } catch (IOException e) {
            root.put(JSON_KEY_ERR, JSON_ERR_INTERNAL);
            return ok(root).build();
        }
        JsonNode nodeDeviceId = jsonTree.get(JSON_KEY_DEVICE_ID);

        if (nodeDeviceId == null) {
            root.put(JSON_KEY_ERR, JSON_ERR_INPUT_PARAM_LACK);
            return ok(root).build();
        }

        DeviceId deviceId = maoDeviceService.genDeviceId(nodeDeviceId.asText());
        maoDeviceService.removeDevice(deviceId);

        root.put(JSON_KEY_ERR, JSON_ERR_OK);
        return ok(root).build();
    }
}
