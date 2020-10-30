package org.cityexperiment.infrastructure;

import org.cityexperiment.application.STMApplicationFactory;
import org.cloudbus.cloudsim.power.models.PowerModelHostSimple;
import org.leaf.location.Location;
import org.cityexperiment.mobility.MobilityModelTaxi;
import org.cloudbus.cloudsim.core.Simulation;
import org.cloudbus.cloudsim.core.events.SimEvent;
import org.cloudbus.cloudsim.power.models.PowerModelHost;
import org.leaf.placement.Orchestrator;
import org.leaf.application.Application;
import org.leaf.infrastructure.ComputeNode;
import org.leaf.host.HostFactory;

import java.util.List;

import static org.cityexperiment.Settings.CAR_MIPS;
import static org.cityexperiment.Settings.CAR_WATT_PER_MIPS;
import static org.leaf.LeafTags.START_APPLICATION;
import static org.leaf.LeafTags.STOP_APPLICATION;

/**
 * A taxi which features a mobility model and hosts a STM application.
 */
public class Taxi extends ComputeNode {

    private static int SHUTDOWN = -1;  // Cars are destroyed _after_ their application so we need to send a proper event.

    private double startTime;
    private MobilityModelTaxi mobilityModel;
    private Orchestrator broker;

    Application application = Application.NULL;

    public Taxi(Simulation simulation, MobilityModelTaxi mobilityModel, Orchestrator broker) {
        super(simulation, List.of(HostFactory.createHost(CAR_MIPS, PowerModelHost.NULL)));
        this.startTime = getSimulation().clock();
        this.mobilityModel = mobilityModel;
        this.broker = broker;
    }

    @Override
    protected void startEntity() {
        scheduleNow(this, START_APPLICATION);
        // Don't call super.startEntity(), the DATACENTER_REGISTRATION_REQUEST event will cause a memory leak
    }

    @Override
    public void processEvent(SimEvent evt) {
        if (evt.getTag() == START_APPLICATION) {
            application = STMApplicationFactory.create(this);
            broker.startApplication(application);
        }
        if (evt.getTag() == SHUTDOWN) {
            super.shutdownEntity();
        }
        super.processEvent(evt);
    }

    @Override
    public void shutdownEntity() {
        if (application != null) {
            schedule(application, 0, STOP_APPLICATION);
        }
        schedule(SHUTDOWN);
    }

    @Override
    public Location getLocation() {
        return mobilityModel.getLocation(getSimulation().clock() - this.startTime);
    }

    public Application getApplication() {
        return application;
    }

    public MobilityModelTaxi getMobilityModel() {
        return mobilityModel;
    }
}
