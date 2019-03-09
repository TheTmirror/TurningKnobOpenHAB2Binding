/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.drehbinding.handler;

import static org.eclipse.smarthome.binding.drehbinding.internal.DrehbindingBindingConstants.*;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.jupnp.UpnpService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DrehbindingHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Tristan - Initial contribution
 */
@Component(configurationPid = "binding.drehbinding", service = ThingHandlerFactory.class)
public class DrehbindingHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(DrehbindingHandlerFactory.class);

    private UpnpService upnpService;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_DREHKNOPF.equals(thingTypeUID)) {
            logger.debug("Building a new Handler for {}", thing.getUID());
            return new DrehbindingHandler(thing, upnpService);
        }

        return null;
    }

    @Reference
    protected void setUpnpIOService(UpnpService upnpService) {
        this.upnpService = upnpService;
    }

    protected void unsetUpnpIOService(UpnpService ioSupnpServiceervice) {
        this.upnpService = upnpService;
    }
}
