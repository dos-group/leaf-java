package org.leaf.host;

import org.cloudbus.cloudsim.power.models.PowerModelHost;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;

import java.util.List;

/**
 * Factory class for creating Hosts.
 *
 * It takes care that each Host has a unique ID, which is required by CloudSim.
 */
public class HostFactory {

    private static final int RAM = 0;
    private static final int BW = 0;
    private static final int STORAGE = 0;

    private static HostFactory factory;
    private int nextHostId;

    public static HostLeaf createHost(long mips, PowerModelHost powerModel) {
        return createHost(mips, powerModel, -1);
    }

    public static HostLeaf createHost(long mips, PowerModelHost powerModel, int shutdownDeadline) {
        if(factory == null)
            factory = new HostFactory();
        HostLeaf host = new HostLeaf(RAM, BW, STORAGE, List.of(new PeSimple(mips)));
        host.setId(factory.getNextHostId());
        host.setVmScheduler(new VmSchedulerTimeShared());  // Allows the allocation of multiple VMs to a single host
        host.setPowerModel(powerModel);
        if (shutdownDeadline >= 0) {
            host.setActive(false);
            host.setIdleShutdownDeadline(shutdownDeadline);
        }
        return host;
    }

    public int getNextHostId() {
        return nextHostId++;
    }

    public static int createdEntities() {
        if(factory == null) return 0;
        return factory.getNextHostId() -1;
    }
}
