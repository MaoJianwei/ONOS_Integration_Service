/*
 * Copyright 2021-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.maojianwei.onos.integrate.cli;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.onosproject.cli.AbstractShellCommand;
import com.maojianwei.onos.integrate.api.MaoDeviceService;
import org.onosproject.net.DeviceId;

/**
 * Sample reactive forwarding application.
 */
@Service
@Command(scope = "onos", name = "mao-add-link",
        description = "Mao Device Test")
public class MaoDeviceAddLinkCommand extends AbstractShellCommand {

//    @Argument(index = 0, name = "mac", description = "One Mac Address",
//            required = false, multiValued = false)
//    @Completion(MacAddressCompleter.class)
//    String mac = null;

    @Override
    protected void doExecute() {
        MaoDeviceService maoDeviceService = get(MaoDeviceService.class);
        maoDeviceService.addLink(
                maoDeviceService.genDeviceId("1"), 1, "GE 3/0/3",
                maoDeviceService.genDeviceId("2"), 3, "GE 3/0/16"
        );


//        MacAddress macAddress = null;
//        if (mac != null) {
//            macAddress = MacAddress.valueOf(mac);
//        }
//        reactiveForwardingService.printMetric(macAddress);
    }
}