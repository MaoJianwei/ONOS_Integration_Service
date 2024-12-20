package com.maojianwei.onos.integrate.web.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.maojianwei.onos.integrate.api.MaoDeviceService;
import org.onosproject.net.DeviceId;
import org.onosproject.rest.AbstractWebResource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import static org.onlab.util.Tools.readTreeFromStream;
import static org.onosproject.net.device.PortStatistics.mao_tag_level;

@Path("MaoIntegration")
public class MaoRestApi extends AbstractWebResource {


    //    add / remove device

    private static final String JSON_KEY_DEVICE_ID = "deviceId";
    private static final String JSON_KEY_NAME = "deviceName";
    private static final String JSON_KEY_VERSION = "swVersion";
    private static final String JSON_KEY_MGR_PROTOCOL = "manageProtocol";

    //    add / remove link

    private static final String JSON_KEY_SRC_DEVICE_ID = "srcDeviceId";
    private static final String JSON_KEY_SRC_PORT_ID = "srcPortId";
    private static final String JSON_KEY_SRC_PORT_NAME = "srcPortName";

    private static final String JSON_KEY_DST_DEVICE_ID = "dstDeviceId";
    private static final String JSON_KEY_DST_PORT_ID = "dstPortId";
    private static final String JSON_KEY_DST_PORT_NAME = "dstPortName";

    //    update custom tag

    private static final String JSON_KEY_PORT_ID = "portId";
    private static final String JSON_KEY_TAG_STRING = "tag";
    private static final String JSON_KEY_TAG_LEVEL = "tagLevel";


    private static final String JSON_KEY_ERR = "err";
    private static final String JSON_KEY_MSG = "msg";

    private static final int JSON_ERR_OK = 0;
    private static final int JSON_ERR_INPUT_PARAM_LACK = -1;
    private static final int JSON_ERR_INPUT_PARAM_ERROR = -2;
    private static final int JSON_ERR_INTERNAL = -500;


    /**
     * Add one device.
     *
     * {
     *      "deviceId":"<device-id>",
     *      "deviceName":"<device-name>",
     *      "swVersion":"<sw-version>",
     *      "manageProtocol":"<manage-protocol>"
     * }
     *
     * @param stream json.
     * @return .
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
     * {
     *      "deviceId":"<device-id>"
     * }
     *
     * @param stream json.
     * @return .
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

    /**
     * Add one bidirectional link.
     *
     * {
     *     "srcDeviceId": "<device-id>",
     *     "srcPortId": <port-number>,
     *     "srcPortName": "<port-name>",
     *     "dstDeviceId": "<device-id>",
     *     "dstPortId": <port-number>,
     *     "dstPortName": "<port-name>"
     * }
     *
     * @param stream json.
     * @return .
     */
    @POST
    @Path("addBiLink")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addBiLink(InputStream stream) {

        MaoDeviceService maoDeviceService = getService(MaoDeviceService.class);

        ObjectNode root = mapper().createObjectNode();
        ObjectNode jsonTree;
        try {
            jsonTree = readTreeFromStream(mapper(), stream);
        } catch (IOException e) {
            root.put(JSON_KEY_ERR, JSON_ERR_INTERNAL);
            return ok(root).build();
        }

        JsonNode nodeSrcDeviceId = jsonTree.get(JSON_KEY_SRC_DEVICE_ID);
        JsonNode nodeSrcPortId = jsonTree.get(JSON_KEY_SRC_PORT_ID);
        JsonNode nodeSrcPortName = jsonTree.get(JSON_KEY_SRC_PORT_NAME);
        JsonNode nodeDstDeviceId = jsonTree.get(JSON_KEY_DST_DEVICE_ID);
        JsonNode nodeDstPortId = jsonTree.get(JSON_KEY_DST_PORT_ID);
        JsonNode nodeDstPortName = jsonTree.get(JSON_KEY_DST_PORT_NAME);

        if (nodeSrcDeviceId == null || nodeSrcPortId == null || nodeDstDeviceId == null || nodeDstPortId == null) {
            root.put(JSON_KEY_ERR, JSON_ERR_INPUT_PARAM_LACK);
            return ok(root).build();
        }

        DeviceId srcDeviceId = maoDeviceService.genDeviceId(nodeSrcDeviceId.asText());
        int srcPortId = nodeSrcPortId.asInt();

        DeviceId dstDeviceId = maoDeviceService.genDeviceId(nodeDstDeviceId.asText());
        int dstPortId = nodeDstPortId.asInt();


        if (nodeSrcPortName == null || nodeDstPortName == null) {
            maoDeviceService.addLink(srcDeviceId, srcPortId, dstDeviceId, dstPortId);
        } else {
            String srcPortName = nodeSrcPortName.asText();
            String dstPortName = nodeDstPortName.asText();
            maoDeviceService.addLink(srcDeviceId, srcPortId, srcPortName, dstDeviceId, dstPortId, dstPortName);
        }
        root.put(JSON_KEY_ERR, JSON_ERR_OK);
        return ok(root).build();
    }



    /**
     * Update the custom tag for one link.
     *
     * For tag:
     *
     * "" means "no tag, no color"
     *
     * " " means "no tag, only color"
     *
     * {
     *      "deviceId":"<device-id>",
     *      "portId":"<port-number>",
     *      "tag":"<tag-string>",
     *      "tagLevel":"<tag-level>"
     * }
     *
     * @param stream json.
     * @return .
     */
    @POST
    @Path("updateCustomTag")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCustomTag(InputStream stream) {
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
        JsonNode nodePortId = jsonTree.get(JSON_KEY_PORT_ID);
        JsonNode nodeTag = jsonTree.get(JSON_KEY_TAG_STRING);
        JsonNode nodeTagLevel = jsonTree.get(JSON_KEY_TAG_LEVEL);

        if (nodeDeviceId == null || nodePortId == null || nodeTag == null || nodeTagLevel == null) {
            root.put(JSON_KEY_ERR, JSON_ERR_INPUT_PARAM_LACK);
            return ok(root).build();
        }

        if (!mao_tag_level.contains(nodeTagLevel.asText())) {
            root.put(JSON_KEY_ERR, JSON_ERR_INPUT_PARAM_ERROR);
            return ok(root).build();
        }

        DeviceId deviceId = maoDeviceService.genDeviceId(nodeDeviceId.asText());
        maoDeviceService.updatePortTag(deviceId, nodePortId.asInt(), nodeTagLevel.asText(), nodeTag.asText());

        root.put(JSON_KEY_ERR, JSON_ERR_OK);
        return ok(root).build();
    }


    /**
     * Remove one bidirectional link.
     *
     * {
     *     "srcDeviceId": "<device-id>",
     *     "srcPortId": <port-number>,
     *     "dstDeviceId": "<device-id>",
     *     "dstPortId": <port-number>
     * }
     *
     * @param stream json.
     * @return .
     */
    @POST
    @Path("removeBiLink")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeBiLink(InputStream stream) {

        MaoDeviceService maoDeviceService = getService(MaoDeviceService.class);

        ObjectNode root = mapper().createObjectNode();
        ObjectNode jsonTree;
        try {
            jsonTree = readTreeFromStream(mapper(), stream);
        } catch (IOException e) {
            root.put(JSON_KEY_ERR, JSON_ERR_INTERNAL);
            return ok(root).build();
        }

        JsonNode nodeSrcDeviceId = jsonTree.get(JSON_KEY_SRC_DEVICE_ID);
        JsonNode nodeSrcPortId = jsonTree.get(JSON_KEY_SRC_PORT_ID);
        JsonNode nodeDstDeviceId = jsonTree.get(JSON_KEY_DST_DEVICE_ID);
        JsonNode nodeDstPortId = jsonTree.get(JSON_KEY_DST_PORT_ID);

        if (nodeSrcDeviceId == null || nodeSrcPortId == null || nodeDstDeviceId == null || nodeDstPortId == null) {
            root.put(JSON_KEY_ERR, JSON_ERR_INPUT_PARAM_LACK);
            return ok(root).build();
        }

        DeviceId srcDeviceId = maoDeviceService.genDeviceId(nodeSrcDeviceId.asText());
        int srcPortId = nodeSrcPortId.asInt();

        DeviceId dstDeviceId = maoDeviceService.genDeviceId(nodeDstDeviceId.asText());
        int dstPortId = nodeDstPortId.asInt();

        maoDeviceService.removeLink(srcDeviceId, srcPortId, dstDeviceId, dstPortId);

        root.put(JSON_KEY_ERR, JSON_ERR_OK);
        return ok(root).build();
    }


    /**
     * .
     *
     * @return .
     */
    @Deprecated
    @GET
    @Path("/netconf/biKnownLinks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response biKnownLinks() {
//        MaoDeviceService maoDeviceService = getService(MaoDeviceService.class);
//
//        ObjectNode root = mapper().createObjectNode();
//
//        ObjectNode jsonTree;
//        try {
//            jsonTree = readTreeFromStream(mapper(), stream);
//        } catch (IOException e) {
//            root.put(JSON_KEY_ERR, JSON_ERR_INTERNAL);
//            return ok(root).build();
//        }
//        JsonNode nodeDeviceId = jsonTree.get(JSON_KEY_DEVICE_ID);
//
//        if (nodeDeviceId == null) {
//            root.put(JSON_KEY_ERR, JSON_ERR_INPUT_PARAM_LACK);
//            return ok(root).build();
//        }
//
//        DeviceId deviceId = maoDeviceService.genDeviceId(nodeDeviceId.asText());
//        maoDeviceService.removeDevice(deviceId);
//
//        root.put(JSON_KEY_ERR, JSON_ERR_OK);
//
        String ret = "[{\"localDeviceName\":\"R3_PE1\",\"localPortName\":\"GigabitEthernet0/3/12\",\"remoteDeviceName\":\"R5_PE3\",\"remotePortName\":\"GigabitEthernet0/3/12\",\"remoteLink\":\"R5_PE3.GigabitEthernet0/3/12\",\"localLink\":\"R3_PE1.GigabitEthernet0/3/12\"},{\"localDeviceName\":\"R3_PE1\",\"localPortName\":\"GigabitEthernet0/3/18\",\"remoteDeviceName\":\"R4_PE2\",\"remotePortName\":\"GigabitEthernet0/3/18\",\"remoteLink\":\"R4_PE2.GigabitEthernet0/3/18\",\"localLink\":\"R3_PE1.GigabitEthernet0/3/18\"},{\"localDeviceName\":\"R4_PE2\",\"localPortName\":\"GigabitEthernet0/3/14\",\"remoteDeviceName\":\"R5_PE3\",\"remotePortName\":\"GigabitEthernet0/3/4\",\"remoteLink\":\"R5_PE3.GigabitEthernet0/3/4\",\"localLink\":\"R4_PE2.GigabitEthernet0/3/14\"}]";
        return ok(ret).build();
    }

    /**
     * .
     *
     * @return .
     */
    @Deprecated
    @GET
    @Path("/netconf/biLinks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response biLinks() {
//        MaoDeviceService maoDeviceService = getService(MaoDeviceService.class);
//
//        ObjectNode root = mapper().createObjectNode();
//
//        ObjectNode jsonTree;
//        try {
//            jsonTree = readTreeFromStream(mapper(), stream);
//        } catch (IOException e) {
//            root.put(JSON_KEY_ERR, JSON_ERR_INTERNAL);
//            return ok(root).build();
//        }
//        JsonNode nodeDeviceId = jsonTree.get(JSON_KEY_DEVICE_ID);
//
//        if (nodeDeviceId == null) {
//            root.put(JSON_KEY_ERR, JSON_ERR_INPUT_PARAM_LACK);
//            return ok(root).build();
//        }
//
//        DeviceId deviceId = maoDeviceService.genDeviceId(nodeDeviceId.asText());
//        maoDeviceService.removeDevice(deviceId);
//
//        root.put(JSON_KEY_ERR, JSON_ERR_OK);
//
        String ret = "[{\"localDeviceName\":\"R3_PE1\",\"localPortName\":\"GigabitEthernet0/3/8\",\"remoteDeviceName\":null,\"remotePortName\":\"5825-7586-fe22\",\"remoteLink\":\"null.5825-7586-fe22\",\"localLink\":\"R3_PE1.GigabitEthernet0/3/8\"},{\"localDeviceName\":\"R3_PE1\",\"localPortName\":\"GigabitEthernet0/3/10\",\"remoteDeviceName\":null,\"remotePortName\":\"5825-7586-fe14\",\"remoteLink\":\"null.5825-7586-fe14\",\"localLink\":\"R3_PE1.GigabitEthernet0/3/10\"},{\"localDeviceName\":\"R3_PE1\",\"localPortName\":\"GigabitEthernet0/3/12\",\"remoteDeviceName\":\"R5_PE3\",\"remotePortName\":\"GigabitEthernet0/3/12\",\"remoteLink\":\"R5_PE3.GigabitEthernet0/3/12\",\"localLink\":\"R3_PE1.GigabitEthernet0/3/12\"},{\"localDeviceName\":\"R3_PE1\",\"localPortName\":\"GigabitEthernet0/3/18\",\"remoteDeviceName\":\"R4_PE2\",\"remotePortName\":\"GigabitEthernet0/3/18\",\"remoteLink\":\"R4_PE2.GigabitEthernet0/3/18\",\"localLink\":\"R3_PE1.GigabitEthernet0/3/18\"},{\"localDeviceName\":\"R4_PE2\",\"localPortName\":\"GigabitEthernet0/3/8\",\"remoteDeviceName\":null,\"remotePortName\":\"48dc-2d02-e6ee\",\"remoteLink\":\"null.48dc-2d02-e6ee\",\"localLink\":\"R4_PE2.GigabitEthernet0/3/8\"},{\"localDeviceName\":\"R4_PE2\",\"localPortName\":\"GigabitEthernet0/3/10\",\"remoteDeviceName\":null,\"remotePortName\":\"48dc-2d02-e6ef\",\"remoteLink\":\"null.48dc-2d02-e6ef\",\"localLink\":\"R4_PE2.GigabitEthernet0/3/10\"},{\"localDeviceName\":\"R4_PE2\",\"localPortName\":\"GigabitEthernet0/3/14\",\"remoteDeviceName\":\"R5_PE3\",\"remotePortName\":\"GigabitEthernet0/3/4\",\"remoteLink\":\"R5_PE3.GigabitEthernet0/3/4\",\"localLink\":\"R4_PE2.GigabitEthernet0/3/14\"}]";
        return ok(ret).build();
    }


    /**
     * .
     *
     * <a href="http://127.0.0.1:8181/netconf/uniLinks"></a>.
     *
     * @return .
     */
    @Deprecated
    @GET
    @Path("/netconf/uniLinks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response uniLinks() {
//        MaoDeviceService maoDeviceService = getService(MaoDeviceService.class);
//
//        ObjectNode root = mapper().createObjectNode();
//
//        ObjectNode jsonTree;
//        try {
//            jsonTree = readTreeFromStream(mapper(), stream);
//        } catch (IOException e) {
//            root.put(JSON_KEY_ERR, JSON_ERR_INTERNAL);
//            return ok(root).build();
//        }
//        JsonNode nodeDeviceId = jsonTree.get(JSON_KEY_DEVICE_ID);
//
//        if (nodeDeviceId == null) {
//            root.put(JSON_KEY_ERR, JSON_ERR_INPUT_PARAM_LACK);
//            return ok(root).build();
//        }
//
//        DeviceId deviceId = maoDeviceService.genDeviceId(nodeDeviceId.asText());
//        maoDeviceService.removeDevice(deviceId);
//
//        root.put(JSON_KEY_ERR, JSON_ERR_OK);
//
        String ret = "[{\"localDeviceName\":\"R3_PE1\",\"localPortName\":\"GigabitEthernet0/3/8\",\"remoteDeviceName\":null,\"remotePortName\":\"5825-7586-fe22\",\"remoteLink\":\"null.5825-7586-fe22\",\"localLink\":\"R3_PE1.GigabitEthernet0/3/8\"},{\"localDeviceName\":\"R3_PE1\",\"localPortName\":\"GigabitEthernet0/3/10\",\"remoteDeviceName\":null,\"remotePortName\":\"5825-7586-fe14\",\"remoteLink\":\"null.5825-7586-fe14\",\"localLink\":\"R3_PE1.GigabitEthernet0/3/10\"},{\"localDeviceName\":\"R3_PE1\",\"localPortName\":\"GigabitEthernet0/3/12\",\"remoteDeviceName\":\"R5_PE3\",\"remotePortName\":\"GigabitEthernet0/3/12\",\"remoteLink\":\"R5_PE3.GigabitEthernet0/3/12\",\"localLink\":\"R3_PE1.GigabitEthernet0/3/12\"},{\"localDeviceName\":\"R3_PE1\",\"localPortName\":\"GigabitEthernet0/3/18\",\"remoteDeviceName\":\"R4_PE2\",\"remotePortName\":\"GigabitEthernet0/3/18\",\"remoteLink\":\"R4_PE2.GigabitEthernet0/3/18\",\"localLink\":\"R3_PE1.GigabitEthernet0/3/18\"},{\"localDeviceName\":\"R4_PE2\",\"localPortName\":\"GigabitEthernet0/3/8\",\"remoteDeviceName\":null,\"remotePortName\":\"48dc-2d02-e6ee\",\"remoteLink\":\"null.48dc-2d02-e6ee\",\"localLink\":\"R4_PE2.GigabitEthernet0/3/8\"},{\"localDeviceName\":\"R4_PE2\",\"localPortName\":\"GigabitEthernet0/3/10\",\"remoteDeviceName\":null,\"remotePortName\":\"48dc-2d02-e6ef\",\"remoteLink\":\"null.48dc-2d02-e6ef\",\"localLink\":\"R4_PE2.GigabitEthernet0/3/10\"},{\"localDeviceName\":\"R4_PE2\",\"localPortName\":\"GigabitEthernet0/3/14\",\"remoteDeviceName\":\"R5_PE3\",\"remotePortName\":\"GigabitEthernet0/3/4\",\"remoteLink\":\"R5_PE3.GigabitEthernet0/3/4\",\"localLink\":\"R4_PE2.GigabitEthernet0/3/14\"},{\"localDeviceName\":\"R4_PE2\",\"localPortName\":\"GigabitEthernet0/3/18\",\"remoteDeviceName\":\"R3_PE1\",\"remotePortName\":\"GigabitEthernet0/3/18\",\"remoteLink\":\"R3_PE1.GigabitEthernet0/3/18\",\"localLink\":\"R4_PE2.GigabitEthernet0/3/18\"},{\"localDeviceName\":\"R5_PE3\",\"localPortName\":\"GigabitEthernet0/3/4\",\"remoteDeviceName\":\"R4_PE2\",\"remotePortName\":\"GigabitEthernet0/3/14\",\"remoteLink\":\"R4_PE2.GigabitEthernet0/3/14\",\"localLink\":\"R5_PE3.GigabitEthernet0/3/4\"},{\"localDeviceName\":\"R5_PE3\",\"localPortName\":\"GigabitEthernet0/3/12\",\"remoteDeviceName\":\"R3_PE1\",\"remotePortName\":\"GigabitEthernet0/3/12\",\"remoteLink\":\"R3_PE1.GigabitEthernet0/3/12\",\"localLink\":\"R5_PE3.GigabitEthernet0/3/12\"}]";
        return ok(ret).build();
    }


    /**
     * Demo1: Three inter-connected routers, while two of them are connected with three links like the LAG.
     *
     * @return .
     */
    @GET
    @Path("/demo1")
    @Produces(MediaType.APPLICATION_JSON)
    public Response demo1() {

        MaoDeviceService mds = getService(MaoDeviceService.class);

        DeviceId beijing = mds.genDeviceId("7181");
        DeviceId shanghai = mds.genDeviceId("2015");
        DeviceId guangzhou = mds.genDeviceId("0810");


        mds.addDevice(beijing, "Beijing", "VRP V8R13C10", "Mao-NETCONF");
        mds.addDevice(shanghai, "Shanghai", "Linux 5.11.12", "OpenSSH");
        mds.addDevice(guangzhou, "Guangzhou", "MaoCloud 2021.5.1", "MaoCloud");


        mds.addLink(beijing, 3, "GE 3/0/3", shanghai, 81, "ens81");

        mds.addLink(beijing, 61, "GE 3/0/61", guangzhou, 31, "MaoLink-31");
        mds.addLink(beijing, 62, "GE 3/0/62", guangzhou, 32, "MaoLink-32");
        mds.addLink(beijing, 63, "GE 3/0/63", guangzhou, 33, "MaoLink-33");

        mds.addLink(guangzhou, 36, "MaoLink-36", shanghai, 96, "ens96");


        ObjectNode root = mapper().createObjectNode()
                .put(JSON_KEY_ERR, JSON_ERR_OK)
                .put(JSON_KEY_MSG, "Three inter-connected routers, while two of them are connected with three links like the LAG.");
        return ok(root).build();
    }


    private static long portStatisticTemp_sendTotalBytes1 = 0L;
    private static long portStatisticTemp_sendTotalBytes2 = 0L;
    private static long portStatisticTemp_sendTotalPackets1 = 0L;
    private static long portStatisticTemp_sendTotalPackets2 = 0L;


    private static final Random random = new Random();



    /**
     * .
     *
     * @return .
     */
    @GET
    @Path("/demo2")
    @Produces(MediaType.APPLICATION_JSON)
    public Response demo2() {

        MaoDeviceService maoDeviceService = getService(MaoDeviceService.class);

        DeviceId deviceIdQingdao = maoDeviceService.genDeviceId("QingdaoRadar");
        maoDeviceService.addDevice(deviceIdQingdao);

        DeviceId deviceIdBeijing = maoDeviceService.genDeviceId("BeijingTower");
        maoDeviceService.addDevice(deviceIdBeijing);

        maoDeviceService.addLink(deviceIdQingdao, 3, deviceIdBeijing, 6);

//        for (int i = 0; i < 10000; i++) {
        portStatisticTemp_sendTotalBytes1 += random.nextInt(1000000);
        maoDeviceService.reportPortStatistics(deviceIdQingdao, 3, portStatisticTemp_sendTotalBytes1, portStatisticTemp_sendTotalPackets1);
        portStatisticTemp_sendTotalBytes2 += random.nextInt(1000000);
        maoDeviceService.reportPortStatistics(deviceIdBeijing, 6, portStatisticTemp_sendTotalBytes2, portStatisticTemp_sendTotalPackets2);
//
//            try {
//                Thread.sleep(800);
//            } catch (Exception e) {
//                ObjectNode root = mapper().createObjectNode()
//                        .put(JSON_KEY_ERR, JSON_ERR_OK)
//                        .put(JSON_KEY_MSG, e.getMessage());
//                return ok(root).build();
//            }
//        }

        ObjectNode root = mapper().createObjectNode()
                .put(JSON_KEY_ERR, JSON_ERR_OK)
                .put(JSON_KEY_MSG, "Demo2.");
        return ok(root).build();
    }


    /**
     * .
     *
     * @return .
     */
    @GET
    @Path("/demo2.5")
    @Produces(MediaType.APPLICATION_JSON)
    public Response demo2_5() {

        MaoDeviceService maoDeviceService = getService(MaoDeviceService.class);

        DeviceId deviceIdQingdao = maoDeviceService.genDeviceId("QingdaoRadar");
//        maoDeviceService.addDevice(deviceIdQingdao);

//        DeviceId deviceIdBeijing = maoDeviceService.genDeviceId("BeijingTower");
//        maoDeviceService.addDevice(deviceIdBeijing);

//        maoDeviceService.addLink(deviceIdQingdao, 3, deviceIdBeijing, 6);

//        for (int i = 0; i < 10000; i++) {
        portStatisticTemp_sendTotalBytes1 += random.nextInt(2000000000);
        portStatisticTemp_sendTotalPackets1 += 1000;
        maoDeviceService.reportPortStatistics(deviceIdQingdao, 3, portStatisticTemp_sendTotalBytes1, portStatisticTemp_sendTotalPackets1);
//            portStatisticTemp_sendTotalBytes2 += 1000000; // random.nextInt(10000000);
        portStatisticTemp_sendTotalPackets2 += 3000;
//            maoDeviceService.reportPortStatistics(deviceIdBeijing, 6, portStatisticTemp_sendTotalBytes2, portStatisticTemp_sendTotalPackets2);

//            try {
//                Thread.sleep(1000);
//            } catch (Exception e) {
//                ObjectNode root = mapper().createObjectNode()
//                        .put(JSON_KEY_ERR, JSON_ERR_OK)
//                        .put(JSON_KEY_MSG, e.getMessage());
//                return ok(root).build();
//            }
//        }

        ObjectNode root = mapper().createObjectNode()
                .put(JSON_KEY_ERR, JSON_ERR_OK)
                .put(JSON_KEY_MSG, "Demo2.5 : " + portStatisticTemp_sendTotalBytes1 + " : " + portStatisticTemp_sendTotalPackets1);
        return ok(root).build();
    }

    private static int levelIndex = 0;

    /**
     * .
     *
     * @return .
     */
    @GET
    @Path("/demo2.6")
    @Produces(MediaType.APPLICATION_JSON)
    public Response demo2_6() {

        MaoDeviceService maoDeviceService = getService(MaoDeviceService.class);


        DeviceId deviceIdQingdao = maoDeviceService.genDeviceId("QingdaoRadar");
//        DeviceId deviceIdBeijing = maoDeviceService.genDeviceId("BeijingTower");

        maoDeviceService.updatePortTag(deviceIdQingdao, 3, mao_tag_level.get(levelIndex), "QINGDAO-RADAR-" + System.currentTimeMillis());

        ObjectNode root = mapper().createObjectNode()
                .put(JSON_KEY_ERR, JSON_ERR_OK)
                .put(JSON_KEY_MSG, "Demo2.6 : " + mao_tag_level.get(levelIndex) + " : QINGDAO-RADAR" + System.currentTimeMillis());

        levelIndex = (levelIndex == 3) ? 0 : (levelIndex + 1) ;

        return ok(root).build();
    }
}
