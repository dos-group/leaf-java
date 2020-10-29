package org.leaf.power;

import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.power.PowerMeasurement;
import org.cloudbus.cloudsim.power.models.PowerModelHost;

/**
 * Power model for data center hosts.
 */
public class PowerModelHostShared extends PowerModelHost {

    private Host host;
    private double wattPerMips;

    /**
     * Instantiates a shared {@link PowerModelHostShared} by specifying its watt per MIPS.
     *
     * @param wattPerMips Incremental watt per MIPS the host consumes under load.
     */
    public PowerModelHostShared(final double wattPerMips) {
        this.wattPerMips = wattPerMips;
    }

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    @Override
    public PowerMeasurement getPowerMeasurement() {
        if(!getHost().isActive()){
            return new PowerMeasurement();
        }
        return new PowerMeasurement(0, host.getCpuMipsUtilization() * wattPerMips);
    }

    /**
     * Computes the hosts power usage in Watts (W) at a certain degree of utilization.
     * Mainly for backwards compatibility.
     *
     * @param utilizationFraction the utilization percentage (between [0 and 1]) of
     * the host.
     * @return the power supply in Watts (W)
     * @throws IllegalArgumentException if utilizationFraction is not between [0 and 1]
     */
    public double getPower(double utilizationFraction) throws IllegalArgumentException {
        return host.getTotalMipsCapacity() * utilizationFraction * wattPerMips;
    }

}