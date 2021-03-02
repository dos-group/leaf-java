package org.examples.smart_city_traffic.infrastructure;

import org.examples.smart_city_traffic.application.V2iApplicationGenerator;
import org.examples.smart_city_traffic.mobility.MobilityModelTaxi;
import org.cloudbus.cloudsim.core.Simulation;
import org.cloudbus.cloudsim.power.models.PowerModelHost;
import org.leaf.application.Application;
import org.leaf.infrastructure.ComputeNode;
import org.leaf.location.Location;

import static org.examples.smart_city_traffic.Settings.CAR_MIPS;

/**
 * A taxi which features a mobility model and hosts a V2I application.
 */
public class Taxi extends ComputeNode {

    private double startTime;
    private MobilityModelTaxi mobilityModel;
    private V2iApplicationGenerator v2iApplicationGenerator;

    Application application = Application.NULL;

    public Taxi(Simulation simulation, MobilityModelTaxi mobilityModel, V2iApplicationGenerator v2iApplicationGenerator) {
        super(simulation, CAR_MIPS, PowerModelHost.NULL, -1);
        this.startTime = getSimulation().clock();
        this.mobilityModel = mobilityModel;
        this.v2iApplicationGenerator = v2iApplicationGenerator;
    }

    @Override
    protected void startInternal() {
        application = v2iApplicationGenerator.create(this);
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
