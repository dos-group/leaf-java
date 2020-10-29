package org.leaf.power;

import org.cloudbus.cloudsim.power.PowerMeasurement;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.leaf.application.Application;
import org.leaf.application.Task;
import org.leaf.host.HostLeaf;
import org.leaf.infrastructure.NetworkLink;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Power model that computes the relative power-requirements of an application.
 */
public class PowerModelApplication implements PowerModel {

    protected Application application;

    public static PowerModelApplication NULL = new PowerModelApplication(null) {
        @Override public PowerMeasurement getPowerMeasurement() { return new PowerMeasurement(); }
    };

    public PowerModelApplication(final Application application) {
        this.application = application;
    }

    @Override
    public PowerMeasurement getPowerMeasurement() {
        if (!application.isRunning()) {
            return new PowerMeasurement();
        }
        List<PowerMeasurement> measurements = new ArrayList<>();
        for (Map.Entry<NetworkLink, Double> entry : application.getReservedNetworkMap().entrySet()) {
            NetworkLink link = entry.getKey();
            double usedBandwidthLink = link.getUsedBandwidth();
            if (usedBandwidthLink == 0) continue;
            double usageFraction = entry.getValue() / usedBandwidthLink;
            measurements.add(link.getPowerModel().getPowerMeasurement().multiply(usageFraction));
        }
        for (Task task : application.getGraph().vertexSet()) {
            HostLeaf host = (HostLeaf) task.getHost();
            double usedMipsHost = host.getCpuMipsUtilization();
            if (usedMipsHost == 0) continue;
            double usageFraction = task.getRequestedMips() / usedMipsHost;
            measurements.add(host.getPowerModel().getPowerMeasurement().multiply(usageFraction));
        }
        return measurements.stream().reduce(PowerMeasurement::add).orElse(new PowerMeasurement());
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }
}
