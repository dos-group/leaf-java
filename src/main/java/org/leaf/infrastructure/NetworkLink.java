package org.leaf.infrastructure;

import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.power.PowerAware;
import org.leaf.power.PowerModelNetworkLink;
import org.jgrapht.graph.DefaultWeightedEdge;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Link between two compute nodes in the infrastructure graph
 */
public class NetworkLink extends DefaultWeightedEdge implements PowerAware<PowerModelNetworkLink> {

    private double latency = 0;
    private double bandwidth = 0;
    private double usedBandwidth = 0;
    private PowerModelNetworkLink powerModel = PowerModelNetworkLink.NULL;

    private SimEntity src;
    private SimEntity dst;

    /**
     * A property that implements the Null Object Design Pattern for {@link NetworkLink}
     * objects.
     */
    public static NetworkLink NULL = new NetworkLink(SimEntity.NULL, SimEntity.NULL) {
        @Override public double getLatency() { return 0; }
    };

    public NetworkLink(SimEntity src, SimEntity dst) {
        this.src = src;
        this.dst = dst;
    }

    public double getLatency() {
        return latency;
    }

    public NetworkLink setLatency(double latency) {
        this.latency = latency;
        return this;
    }

    public double getBandwidth() {
        return bandwidth;
    }

    public NetworkLink setBandwidth(double bandwidth) {
        this.bandwidth = bandwidth;
        return this;
    }

    public boolean reserveBandwidth(double reserveBw) {
        double newUsedBandwidth = usedBandwidth + reserveBw;
        if (newUsedBandwidth > bandwidth) {
            return false;
        } else {
            usedBandwidth = newUsedBandwidth;
            return true;
        }
    }

    public void releaseBandwidth(double releaseBw) {
        double newUsedBandwidth = usedBandwidth - releaseBw;
        if (newUsedBandwidth < 0) {
            throw new RuntimeException(format("Cannot release %f bandwidth because only %f is reserved.", releaseBw, usedBandwidth));
        }
        usedBandwidth = newUsedBandwidth;
    }

    public double getUsedBandwidth() {
        return usedBandwidth;
    }

    public void setPowerModel(PowerModelNetworkLink powerModel) {
        requireNonNull(powerModel);
        if(powerModel.getLink() != null && powerModel.getLink() != NetworkLink.NULL && !powerModel.getLink().equals(this)){
            throw new IllegalStateException("The given EnergyModelNetwork is already assigned to another NetworkLink. " +
                "Each NetworkLink must have its own EnergyModelNetwork instance.");
        }

        this.powerModel = powerModel;
        powerModel.setLink(this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + String.format("{bandwidth=%6.2e/%6.2e}" , usedBandwidth, bandwidth);
    }

    public PowerModelNetworkLink getPowerModel() {
        return powerModel;
    }

    public SimEntity getSrc() {
        return src;
    }

    public void setSrc(SimEntity src) {
        this.src = src;
    }

    public SimEntity getDst() {
        return dst;
    }

    public void setDst(SimEntity dst) {
        this.dst = dst;
    }
}
