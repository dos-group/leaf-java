package org.leaf.power;

import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.power.PowerMeasurement;
import org.leaf.infrastructure.ComputeNode;
import org.leaf.location.Location;

/**
 * Power model for WiFi network links that takes distance between sender and receiver into account.
 */
public class PowerModelNetworkLinkWifi extends PowerModelNetworkLink {

    // amplifier energy dissipation in free space channel (Joul per bit per square meter : J/bit/m^2)
    private final double amplifierDissipation;

    public PowerModelNetworkLinkWifi(final double energyPerBit,
                                     final double amplifierDissipation) {
        super(energyPerBit);
        this.amplifierDissipation = amplifierDissipation;
    }

    @Override
    public PowerMeasurement getPowerMeasurement() {
        return computePowerUsage(getLink().getUsedBandwidth());
    }

    public PowerMeasurement computePowerUsage(double usedBandwidth) {
        if (usedBandwidth == 0) return new PowerMeasurement();

        Location srcLocation = getEntityLocation(getLink().getSrc());
        Location dstLocation = getEntityLocation(getLink().getDst());
        double distance = Math.sqrt(Math.pow((srcLocation.getX() - dstLocation.getX()), 2)
            + Math.pow((srcLocation.getY() - dstLocation.getY()), 2));

        double dissipationEnergyPerBit = amplifierDissipation * Math.pow(distance, 2);
        return new PowerMeasurement(0, (getEnergyPerBit() + dissipationEnergyPerBit) * usedBandwidth);
    }

    private Location getEntityLocation(SimEntity entity) {
        if (!(entity instanceof Datacenter)) {
            throw new RuntimeException("Cannot initialize " + this + " because " + entity + " source is no data center");
        }
        Location location = ((ComputeNode) entity).getLocation();
        if (location == Location.NULL) {
            throw new RuntimeException("Cannot initialize " + this + " because " + entity + " has no location");
        }
        return location;
    }

}
