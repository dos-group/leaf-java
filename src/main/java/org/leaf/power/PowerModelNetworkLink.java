package org.leaf.power;

import org.cloudbus.cloudsim.power.models.PowerMeasurement;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.leaf.infrastructure.NetworkLink;

/**
 * Linear power model for network links
 */
public class PowerModelNetworkLink implements PowerModel {

    public static PowerModelNetworkLink NULL = new PowerModelNetworkLink(0) {
        @Override public PowerMeasurement measure() { return new PowerMeasurement(); }
    };

    private double energyPerBit;

    public PowerModelNetworkLink(final double energyPerBit) {
        this.energyPerBit = energyPerBit;
    }

    private NetworkLink link;

    public NetworkLink getLink() {
        return link;
    }

    public void setLink(NetworkLink link) {
        this.link = link;
    }

    @Override
    public PowerMeasurement measure() {
        return new PowerMeasurement(0, energyPerBit * getLink().getUsedBandwidth());
    }

    public double getEnergyPerBit() {
        return energyPerBit;
    }

}
