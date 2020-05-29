package org.leaf.placement;

import org.cloudbus.cloudsim.brokers.DatacenterBrokerAbstract;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.events.SimEvent;
import org.cloudbus.cloudsim.vms.Vm;
import org.leaf.application.Application;

import java.util.List;

import static org.leaf.LeafTags.*;

/**
 * DatacenterBroker for the LEAF infrastructure and application model.
 */
public abstract class DatacenterBrokerLeaf extends DatacenterBrokerAbstract {

    public DatacenterBrokerLeaf(CloudSim simulation, String name) {
        super(simulation, name);
    }

    public void startApplication(Application application) {
        submitVmList(application.getVms());
        schedule(application, 0, START_APPLICATION);
    }

    public void stopApplication(Application application) {
        destroyVmList(application.getVms());
        schedule(application, 0, STOP_APPLICATION);
    }

    @Override
    protected Vm defaultVmMapper(Cloudlet cloudlet) {
        throw new RuntimeException(this.getClass().getName() + " cannot place cloudlets.");
    }

    @Override
    public void processEvent(final SimEvent evt) {
        if (evt.getTag() == SCHEDULE_APPLICATION_START) {
            Application application = (Application) evt.getData();
            submitVmList(application.getVms());
            scheduleNow(application, START_APPLICATION);
        } else if (evt.getTag() == SCHEDULE_APPLICATION_END) {
            Application application = (Application) evt.getData();
            destroyVmList(application.getVms());
            scheduleNow(application, STOP_APPLICATION);
        }
        super.processEvent(evt);
    }

    private void destroyVmList(List<Vm> vms) {
        for (Vm vm : vms) {
            sendNow(getDatacenter(vm), CloudSimTags.VM_DESTROY, vm);
            vm.getHost().getIdleShutdownDeadline();
        }
    }

}
