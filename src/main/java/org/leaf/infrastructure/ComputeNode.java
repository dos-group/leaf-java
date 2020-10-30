package org.leaf.infrastructure;

import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.Simulation;
import org.cloudbus.cloudsim.core.events.SimEvent;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.vms.Vm;
import org.leaf.host.HostLeaf;
import org.leaf.location.Location;
import org.leaf.location.LocationAware;

import java.util.ArrayList;
import java.util.List;

import static org.leaf.LeafTags.SHUTDOWN_FOG_NODE;

/**
 * Location-aware data center which can optionally shut down its hosts if they are not utilized any more
 */
public class ComputeNode extends DatacenterSimple implements LocationAware {

    /**
     * A property that implements the Null Object Design Pattern for {@link ComputeNode}
     * objects.
     */
    public static ComputeNode NULL = new ComputeNode(Simulation.NULL, new ArrayList<>()) {};

    private Location location = Location.NULL;

    public ComputeNode(Simulation simulation, List<HostLeaf> hostList, VmAllocationPolicy vmAllocationPolicy) {
        super(simulation, hostList, vmAllocationPolicy);
    }

    public ComputeNode(Simulation simulation, List<HostLeaf> hostList) {
        this(simulation, hostList, new VmAllocationPolicySimple());
    }

    @Override
    public void processEvent(final SimEvent evt) {
        if (evt.getTag() == SHUTDOWN_FOG_NODE) {
            Host host = (HostLeaf) evt.getData();
            if(host.getCpuMipsUtilization() == 0){
                host.setActive(false);
            } else {
                LOGGER.debug("Cannot power off {}. There are still {} MIPS reserved.", this, host.getCpuMipsUtilization());
            }
        }
        super.processEvent(evt);
    }

    /**
     * TODO
     * @param host
     */
    public void tryToShutDown(final Host host) {
        schedule(host.getIdleShutdownDeadline(), SHUTDOWN_FOG_NODE, host);
    }

    @Override
    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

}
