package org.leaf.host;

import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.lang.String.format;

/**
 * Host that works with the LEAF infrastructure and application model
 */
public class HostLeaf extends HostSimple {
    private static final Logger LOGGER = LoggerFactory.getLogger(HostLeaf.class.getSimpleName());

    private double usedMips;

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
    }

    /**
     * Will shut down the host if there are no more VMs running
     */
    public void tryToShutDown() {
        if(getVmList().isEmpty()){
            setActive(false);
        } else {
            LOGGER.debug("Cannot power off {}. There are still {} VMs running.", this, getVmList().size());
        }
    }
}
