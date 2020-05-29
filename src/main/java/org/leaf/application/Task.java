package org.leaf.application;

import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletScheduler;
import org.cloudbus.cloudsim.vms.VmSimple;

/**
 * Task of an Application
 */
public class Task extends VmSimple {

    Datacenter boundDatacenter = Datacenter.NULL;

    public Task(long id, long mipsCapacity, long numberOfPes) {
        super(id, mipsCapacity, numberOfPes);
    }

    public Datacenter getBoundDatacenter() {
        return boundDatacenter;
    }

    public void setBoundDatacenter(Datacenter boundDatacenter) {
        this.boundDatacenter = boundDatacenter;
    }
}
