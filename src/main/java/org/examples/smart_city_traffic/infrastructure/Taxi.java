package org.examples.smart_city_traffic.infrastructure;

import org.examples.smart_city_traffic.application.StmApplicationGenerator;
import org.examples.smart_city_traffic.mobility.MobilityModelTaxi;
import org.cloudbus.cloudsim.core.Simulation;
import org.cloudbus.cloudsim.power.models.PowerModelHost;
import org.leaf.application.Application;
import org.leaf.host.HostFactory;
import org.leaf.infrastructure.ComputeNode;
import org.leaf.location.Location;

import java.util.List;

import static org.examples.smart_city_traffic.Settings.CAR_MIPS;

/**
 * A taxi which features a mobility model and hosts a STM application.
 */
public class Taxi extends ComputeNode {

    private double startTime;
    private MobilityModelTaxi mobilityModel;
    private StmApplicationGenerator stmApplicationGenerator;

    Application application = Application.NULL;

    public Taxi(Simulation simulation, MobilityModelTaxi mobilityModel, StmApplicationGenerator stmApplicationGenerator) {
        super(simulation, List.of(HostFactory.createHost(CAR_MIPS, PowerModelHost.NULL)));
        this.startTime = getSimulation().clock();
        this.mobilityModel = mobilityModel;
        this.stmApplicationGenerator = stmApplicationGenerator;
    }

    @Override
    protected void startInternal() {
        application = stmApplicationGenerator.create(this);
        // Don't call super.startEntity(), the DATACENTER_REGISTRATION_REQUEST event will cause a memory leak
    }

    @Override
    public void shutdown() {
        application.shutdown();
        super.shutdown();
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
