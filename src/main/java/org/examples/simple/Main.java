package org.examples.simple;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.Simulation;
import org.cloudbus.cloudsim.power.PowerMeter;
import org.cloudbus.cloudsim.power.models.PowerModelHostSimple;
import org.cloudsimplus.util.Log;
import org.leaf.LeafSim;
import org.leaf.application.Application;
import org.leaf.application.Task;
import org.leaf.infrastructure.ComputeNode;
import org.leaf.infrastructure.InfrastructureGraph;
import org.leaf.infrastructure.NetworkLink;
import org.leaf.placement.Orchestrator;
import org.leaf.power.PowerModelHostShared;
import org.leaf.power.PowerModelNetworkLink;

import java.util.List;
import java.util.Locale;

import static org.examples.smart_city_traffic.Settings.*;


public class Main {

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);  // US number formatting
        Log.setLevel(LOG_LEVEL);
		new Main();
	}

	private Main() {
        runExperiment();
    }

    private void runExperiment() {
        LeafSim simulation = new LeafSim();

        ComputeNode sensorNode = new ComputeNode(simulation, 5000, new PowerModelHostSimple(20, 8), -1);
        ComputeNode fogNode = new ComputeNode(simulation, 70000, new PowerModelHostSimple(200, 30), -1);
        ComputeNode cloudNode = new ComputeNode(simulation, Long.MAX_VALUE, new PowerModelHostShared(700e-6), -1);

        NetworkLink wifiLink = new NetworkLink(sensorNode, fogNode).setBandwidth(1.6e9);
        wifiLink.setPowerModel(new PowerModelNetworkLink(300e-9));
        simulation.getInfrastructure().addLink(wifiLink);
        NetworkLink wanLink = new NetworkLink(fogNode, cloudNode).setBandwidth(1.6e9);
        wanLink.setPowerModel(new PowerModelNetworkLink(7000e-9));
        simulation.getInfrastructure().addLink(wanLink);

        // This determines the placement of the processing task
        Orchestrator orchestrator = new OrchestratorSimple(simulation.getInfrastructure(), fogNode);

        Application application = new Application(simulation, orchestrator);
        Task sourceTask = new Task(100);
        Task processingTask = new Task(8000);
        Task sinkTask = new Task(200);
        application.addSourceTask(sourceTask, 100e3, sensorNode);
        application.addProcessingTask(processingTask, 50e3);
        application.addSinkTask(sinkTask, cloudNode);

        PowerMeter wifiLinkPowerMeter = new PowerMeter(simulation, wifiLink);
        PowerMeter sensorNodePowerMeter = new PowerMeter(simulation, sensorNode);
        PowerMeter applicationPowerMeter = new PowerMeter(simulation, application);
        PowerMeter infrastructurePowerMeter = new PowerMeter(simulation,
                List.of(sensorNode, fogNode, cloudNode, wifiLink, wanLink));

        simulation.terminateAt(1);
        simulation.start();

        System.out.println("Experiment finished!");
        System.out.println("Wifi: " + wifiLinkPowerMeter.getPowerMeasurements().get(0).getTotalPower() + " W");
        System.out.println("Sensor Node: " + sensorNodePowerMeter.getPowerMeasurements().get(0).getTotalPower() + " W");
        System.out.println("Application: " + applicationPowerMeter.getPowerMeasurements().get(0).getTotalPower() + " W");
        System.out.println("Infrastructure: " + infrastructurePowerMeter.getPowerMeasurements().get(0).getTotalPower() + " W");
	}

}
