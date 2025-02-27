package org.leaf.host;

import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.leaf.infrastructure.ComputeNode;

import java.util.List;

import static java.lang.String.format;

/**
 * Host that works with the LEAF infrastructure and application model
 */
public class HostLeaf extends HostSimple {

    private double usedMips;

    public HostLeaf(long mips) {
        this(0, 0, 0, List.of(new PeSimple(mips)));
    }

    public HostLeaf(long ram, long bw, long storage, List<Pe> peList) {
        super(ram, bw, storage, peList);
    }

    @Override
    public double getCpuPercentUtilization() {
        return usedMips / getTotalMipsCapacity();
    }

    @Override
    public double getCpuMipsUtilization() {
        return usedMips;
    }

    public boolean reserveMips(double reserveMips) {
        setActive(true);
        double newUsedMips = usedMips + reserveMips;
        if (newUsedMips > getTotalMipsCapacity()) {
            return false;
        } else {
            usedMips = newUsedMips;
            return true;
        }
    }

    public void releaseMips(double releaseMips) {
        double newUsedMips = usedMips - releaseMips;
        if (newUsedMips < 0) {
            throw new RuntimeException(format("Cannot release %f MIPS because only %f are reserved.", releaseMips, usedMips));
        }
        usedMips = newUsedMips;
        if (getIdleShutdownDeadline() >= 0) {
            ((ComputeNode) getDatacenter()).tryToShutDown(this);
        }
    }

}
