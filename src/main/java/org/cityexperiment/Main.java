package org.cityexperiment;

import org.cityexperiment.city.City;
import org.cityexperiment.infrastructure.InfrastructureGraphCity;
import org.cityexperiment.mobility.MobilityManager;
import org.cityexperiment.infrastructure.Taxi;
import org.cityexperiment.infrastructure.TrafficLightSystem;
import org.cityexperiment.util.CsvExporter;
import org.cityexperiment.visualization.Visualizer;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerMeter;
import org.cloudsimplus.util.Log;
import org.leaf.application.TaskFactory;
import org.leaf.host.HostFactory;

import java.io.File;
import java.util.List;
import java.util.Locale;

import static java.util.stream.Collectors.toList;
import static org.cityexperiment.Settings.*;


/**
 * Smart city experiment
 */
public class Main {

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);  // US number formatting
        Log.setLevel(LOG_LEVEL);
		new Main();
	}

	private Main() {
        long startTime = System.currentTimeMillis();

        String experimentName = determineExperimentName();
        mkdir(RESULTS_PATH);
        mkdir(RESULTS_PATH + "/" + experimentName);

		CloudSim simulation = new CloudSim();
        City city = new City(simulation, Settings.CITY_WIDTH, Settings.CITY_HEIGHT, Settings.STREETS_PER_AXIS);
        MobilityManager mm = new MobilityManager(simulation, city);
        InfrastructureGraphCity nt = city.getNetworkTopology();

        PowerMeter cloud = new PowerMeter(simulation, city.getCloudDc()).setName("cloud");
        PowerMeter fog = new PowerMeter(simulation, nt.getFogDcs()).setName("fog");
        PowerMeter wifi = new PowerMeter(simulation, nt::getWifiLinks).setName("wifi");
        PowerMeter wan = new PowerMeter(simulation, nt::getWanLinks).setName("wan");
        PowerMeter cctvApp = new PowerMeter(simulation, () -> nt.getTraficLightSystems().stream().map(TrafficLightSystem::getApplication).collect(toList())).setName("cctv");
        PowerMeter stmApp = new PowerMeter(simulation, () -> nt.getTaxis().stream().map(Taxi::getApplication).collect(toList())).setName("stm");

        cloud.setMeasurementInterval(POWER_MEASUREMENT_INTERVAL);
        fog.setMeasurementInterval(POWER_MEASUREMENT_INTERVAL);
        wifi.setMeasurementInterval(POWER_MEASUREMENT_INTERVAL);
        wan.setMeasurementInterval(POWER_MEASUREMENT_INTERVAL);
        cctvApp.setMeasurementInterval(POWER_MEASUREMENT_INTERVAL);
        stmApp.setMeasurementInterval(POWER_MEASUREMENT_INTERVAL);

        if (VISUALIZATION_REDRAW_INTERVAL > 0) {
            new Visualizer(experimentName, simulation, city, mm, List.of(cloud, fog, wifi, wan), List.of(cctvApp, stmApp), TIME_STEP_INTERVAL, SIMULATION_TIME);
        }

        simulation.terminateAt(SIMULATION_TIME);
        simulation.start();

        System.out.println("Writing results...");
        CsvExporter.write(RESULTS_PATH + "/" + experimentName + "/infrastructure.csv", mm, List.of(cloud, fog, wifi, wan));
        CsvExporter.write(RESULTS_PATH + "/" + experimentName + "/applications.csv", mm, List.of(cctvApp, stmApp));

        System.out.println("Experiment " + experimentName + " finished!");
        System.out.println();
        System.out.println("Simulated time: " + SIMULATION_TIME + "s");
        System.out.println("Real time:      " + ((System.currentTimeMillis() - startTime) / 1000) + "s");
        System.out.println("PMs created:    " + HostFactory.createdEntities());
        System.out.println("VMs created:    " + TaskFactory.createdEntities());
	}

    private static String determineExperimentName() {
        String experimentName;
        if (FOG_DCS <= 0) {
            experimentName = "cloud_only";
        } else {
            experimentName = "fog_" + FOG_DCS;
            if (FOG_SHUTDOWN_DEADLINE > -1) {
                experimentName += "_shutdown" + FOG_SHUTDOWN_DEADLINE;
            }
        }
        return experimentName;
    }

    private void mkdir(String dir) {
        File file = new File(dir);
        if(file.mkdir()){
            System.out.println("Directory " + dir + " created");
        } else {
            System.out.println("Did not create directory " + dir);
        }
    }
}
