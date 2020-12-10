package org.cityexperiment;

import org.cityexperiment.city.City;
import org.cityexperiment.infrastructure.InfrastructureGraphCity;
import org.cityexperiment.infrastructure.Taxi;
import org.cityexperiment.infrastructure.TrafficLightSystem;
import org.cityexperiment.mobility.MobilityManager;
import org.cityexperiment.util.CsvExporter;
import org.cityexperiment.visualization.Visualizer;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerMeter;
import org.cloudsimplus.util.Log;
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
        /*runExperiment(0);
        runExperiment(1);
        runExperiment(2);
        runExperiment(3);
        runExperiment(4);
        runExperiment(5);*/
        runExperiment(6);
    }

    private void runExperiment(int numFogDcs) {
        long startTime = System.currentTimeMillis();
        String experimentName = determineExperimentName(numFogDcs);

        if (RESULTS_PATH != null) {
            mkdir(RESULTS_PATH);
            mkdir(RESULTS_PATH + "/" + experimentName);
        }

		CloudSim simulation = new CloudSim();
        City city = new City(simulation, CITY_WIDTH, CITY_HEIGHT, STREETS_PER_AXIS, numFogDcs);
        MobilityManager mm = new MobilityManager(simulation, city);
        InfrastructureGraphCity nt = city.getInfrastructureGraph();

        PowerMeter cloud = new PowerMeter(simulation, city.getCloudDc()).setName("cloud");
        PowerMeter fog = new PowerMeter(simulation, nt.getFogDcs()).setName("fog");
        PowerMeter wifi = new PowerMeter(simulation, nt::getWifiLinks).setName("wifi");
        PowerMeter wanUp = new PowerMeter(simulation, nt::getWanUpLinks).setName("wanUp");
        PowerMeter wanDown = new PowerMeter(simulation, nt::getWanDownLinks).setName("wanDown");
        PowerMeter cctvApp = new PowerMeter(simulation, () -> nt.getTraficLightSystems().stream().map(TrafficLightSystem::getApplication).collect(toList())).setName("cctv");
        PowerMeter stmApp = new PowerMeter(simulation, () -> nt.getTaxis().stream().map(Taxi::getApplication).collect(toList())).setName("stm");

        cloud.setMeasurementInterval(POWER_MEASUREMENT_INTERVAL);
        fog.setMeasurementInterval(POWER_MEASUREMENT_INTERVAL);
        wifi.setMeasurementInterval(POWER_MEASUREMENT_INTERVAL);
        wanUp.setMeasurementInterval(POWER_MEASUREMENT_INTERVAL);
        wanDown.setMeasurementInterval(POWER_MEASUREMENT_INTERVAL);
        cctvApp.setMeasurementInterval(POWER_MEASUREMENT_INTERVAL);
        stmApp.setMeasurementInterval(POWER_MEASUREMENT_INTERVAL);

        if (VISUALIZATION_REDRAW_INTERVAL > 0) {
            new Visualizer(experimentName, simulation, city, mm, List.of(cloud, fog, wifi, wanUp, wanDown), List.of(cctvApp, stmApp), TIME_STEP_INTERVAL, SIMULATION_TIME);
        }

        simulation.terminateAt(SIMULATION_TIME);
        simulation.start();


        if (RESULTS_PATH != null) {
            System.out.println("Writing results...");
            CsvExporter.write(RESULTS_PATH + "/" + experimentName + "/infrastructure.csv", mm, List.of(cloud, fog, wifi, wanUp, wanDown));
            CsvExporter.write(RESULTS_PATH + "/" + experimentName + "/applications.csv", mm, List.of(cctvApp, stmApp));
        }

        System.out.println("Experiment " + experimentName + " finished!");
        System.out.println();
        System.out.println("Simulated time: " + SIMULATION_TIME + "s");
        System.out.println("Real time:      " + ((System.currentTimeMillis() - startTime) / 1000) + "s");
        System.out.println("PMs created:    " + HostFactory.createdEntities());
	}

    private static String determineExperimentName(int numFogDcs) {
        String experimentName;
        if (numFogDcs <= 0) {
            experimentName = "cloud_only";
        } else {
            experimentName = "fog_" + numFogDcs;
            if (FOG_SHUTDOWN_DEADLINE > -1) {
                experimentName += "_shutdown" + FOG_SHUTDOWN_DEADLINE;
            }
        }
        return experimentName + "_" + SEED;
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
