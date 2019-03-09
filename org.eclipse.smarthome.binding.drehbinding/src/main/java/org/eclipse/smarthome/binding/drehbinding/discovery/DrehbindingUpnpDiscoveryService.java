package org.eclipse.smarthome.binding.drehbinding.discovery;

import static org.eclipse.smarthome.binding.drehbinding.internal.DrehbindingBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.UpnpService;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DrehbindingUpnpDiscoveryService} is responsible processing the
 * results of searches for UPNP devices
 *
 * @author Karel Goderis - Initial contribution
 */
@Component(immediate = true)
public class DrehbindingUpnpDiscoveryService implements UpnpDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(DrehbindingUpnpDiscoveryService.class);
    UpnpService service;

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    public DiscoveryResult createResult(RemoteDevice device) {
        ThingUID uid = getThingUID(device);

        if (uid == null) {
            return null;
        }

        // logDeviceInformations(device);

        Map<String, Object> properties = new HashMap<>(2);

        // Device properties
        properties.put(HOST, device.getDetails().getBaseURL());
        properties.put(SERIAL, device.getDetails().getSerialNumber());
        properties.put(UDN, device.getIdentity().getUdn().getIdentifierString());

        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                .withLabel(device.getDetails().getFriendlyName()).withRepresentationProperty(SERIAL).build();

        return result;
    }

    private void logDeviceInformations(RemoteDevice device) {
        logger.trace(
                "=======================================INFORMATIONS BEGIN=======================================\n");
        logger.trace("==============INFORMATIONS DESCRIPTIONURL==============");
        logger.trace("" + device.getIdentity().getDescriptorURL() + "\n");
        logger.trace("==============INFORMATIONS DETAILS==============");
        logger.trace("" + device.getDetails() + "\n");
        logger.trace("FriendlyName: " + device.getDetails().getFriendlyName());
        logger.trace("SerialNumber: " + device.getDetails().getSerialNumber());
        logger.trace("Upc: " + device.getDetails().getUpc());
        logger.trace("==============INFORMATIONS DISPLAYSTRING==============");
        logger.trace("" + device.getDisplayString() + "\n");
        logger.trace("==============INFORMATIONS EMBEDDEDDEVICES==============");
        logger.trace("" + device.getEmbeddedDevices() + "\n");
        logger.trace("#Embeddeddevices: " + device.getEmbeddedDevices().length);
        logger.trace("==============INFORMATIONS ICONS==============");
        logger.trace("" + device.getIcons() + "\n");
        logger.trace("#Icon: " + device.getIcons().length);
        logger.trace("==============INFORMATIONS IDENTITY==============");
        logger.trace("" + device.getIdentity() + "\n");
        logger.trace("" + device.getIdentity().getDescriptorURL());
        logger.trace("" + device.getIdentity().getDiscoveredOnLocalAddress());
        logger.trace("" + device.getIdentity().getUdn());
        logger.trace("" + device.getIdentity().validate().size());
        logger.trace("==============INFORMATIONS PARENT==============");
        logger.trace("" + device.getParentDevice() + "\n");
        logger.trace("==============INFORMATIONS ROOT==============");
        logger.trace("" + device.getRoot() + "\n");
        logger.trace("" + ((device.getRoot() == device) && device.isRoot()));
        logger.trace("==============INFORMATIONS SERVICES==============");
        logger.trace("" + device.getServices() + "\n");
        logger.trace("" + device.getServices().length);
        logger.trace("" + device.hasServices());
        logger.trace("==============INFORMATIONS TYPE==============");
        logger.trace("" + device.getType() + "\n");
        logger.trace("==============INFORMATIONS VERSION==============");
        logger.trace("" + device.getVersion() + "\n");
        logger.trace("" + device.getVersion().validate().size());
        logger.trace("" + device.getVersion().getMajor());
        logger.trace("" + device.getVersion().getMinor());

        logger.trace("Registery contains my device: "
                + (service.getRegistry().getDevice(device.getIdentity().getUdn(), true) == device));

        logger.trace("=======================================INFORMATIONS END=======================================");
    }

    @Reference
    protected void setUpnpService(UpnpService upnpService) {
        this.service = upnpService;
    }

    protected void unsetUpnpService(UpnpService upnpService) {
        this.service = null;
    }

    @Override
    public ThingUID getThingUID(RemoteDevice device) {
        try {

            DeviceDetails details = device.getDetails();

            logger.trace("Man: " + details.getManufacturerDetails().getManufacturer());
            logger.trace("Modell: " + details.getModelDetails().getModelName());

            if (details.getManufacturerDetails().getManufacturer().toLowerCase().equals("tristan")
                    && details.getModelDetails().getModelName().toLowerCase().equals("drehknopfprototype")) {
                return new ThingUID(THING_TYPE_DREHKNOPF, details.getSerialNumber());
            }

            return null;

        } catch (NullPointerException ex) {
            return null;
        }
    }

}