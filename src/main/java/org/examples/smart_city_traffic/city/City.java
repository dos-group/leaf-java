package org.examples.smart_city_traffic.city;

import org.examples.smart_city_traffic.application.CctvApplicationGenerator;
import org.examples.smart_city_traffic.application.V2iApplicationGenerator;
import org.examples.smart_city_traffic.infrastructure.DatacenterCloud;
import org.examples.smart_city_traffic.infrastructure.DatacenterFog;
import org.examples.smart_city_traffic.infrastructure.InfrastructureGraphCity;
import org.examples.smart_city_traffic.infrastructure.TrafficLightSystem;
import org.examples.smart_city_traffic.placement.OrchestratorCity;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleGraph;
import org.leaf.location.Location;
import org.leaf.placement.Orchestrator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.examples.smart_city_traffic.Settings.*;

/**
 * Representing a city center with streets where all crossings are equipped with traffic light systems.
 */
public class City {

    private double width;
    private double height;
    private int streetsPerAxis;
    private Graph<Location, Street> streetGraph;

    private DatacenterCloud cloudDc;
    private InfrastructureGraphCity infrastructureGraph;
    private Orchestrator orchestrator;
    private CctvApplicationGenerator cctvApplicationGenerator;
    private V2iApplicationGenerator v2iApplicationGenerator;

    private List<Location> gateLocations = new ArrayList<>();   // Gates are entry/exit points for taxis
    private List<Location> trafficLightLocations = new ArrayList<>();

    public City(CloudSim simulation, double width, double height, int streetsPerAxis, int numFogDcs) {
        this.width = width;
        this.height = height;
        this.streetsPerAxis = streetsPerAxis;
        this.streetGraph = createStreetGraph();

        this.cloudDc = new DatacenterCloud(simulation);
        this.infrastructureGraph = new InfrastructureGraphCity();
        this.orchestrator = new OrchestratorCity(this.infrastructureGraph, this.cloudDc);
        this.cctvApplicationGenerator = new CctvApplicationGenerator(this.orchestrator);
        this.v2iApplicationGenerator = new V2iApplicationGenerator(this.orchestrator);

        initInfrastructureGraph(simulation, numFogDcs);
    }

    public InfrastructureGraphCity getInfrastructureGraph() {
        return infrastructureGraph;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public Graph<Location, Street> getStreetGraph() {
        return streetGraph;
    }

    public List<Location> getGateLocations() {
        return gateLocations;
    }

    public List<Location> getTrafficLightLocations() {
        return trafficLightLocations;
    }

    public Datacenter getCloudDc() {
        return cloudDc;
    }

    /**
     * Streets are implemented as graphs.
     */
    private Graph<Location, Street> createStreetGraph() {
        Graph<Location, Street> graph = new SimpleGraph<>(Street.class);
        final int N_POINTS = streetsPerAxis + 2;
        final double STEP_SIZE_X = width / (N_POINTS - 1);
        final double STEP_SIZE_Y = height / (N_POINTS - 1);

        Location[][] locations = new Location[N_POINTS][N_POINTS];
        for (int x = 0; x < N_POINTS; x++) {
            for (int y = 0; y < N_POINTS; y++) {
                Location location = new Location(x * STEP_SIZE_X, y * STEP_SIZE_Y);
                if (x == 0 || x == N_POINTS - 1 || y == 0 || y == N_POINTS - 1) {
                    gateLocations.add(location);
                } else {
                    trafficLightLocations.add(location);
                }
                locations[x][y] = location;
                graph.addVertex(location);
                if (x > 0 && y > 0) {
                    if (y < N_POINTS - 1) graph.addEdge(location, locations[x - 1][y]);
                    if (x < N_POINTS - 1) graph.addEdge(location, locations[x][y - 1]);
                }
            }
        }

        Location[] removeLocations = {
            locations[0][0],
            locations[N_POINTS - 1][0],
            locations[0][N_POINTS - 1],
            locations[N_POINTS - 1][N_POINTS - 1],
        };
        for (Location location : removeLocations) {
            graph.removeVertex(location);
            gateLocations.remove(location);
        }

        return graph;
    }

    /**
     * Initializes the network topology with a cloud and a number of traffic light systems and fog nodes.
     */
    private void initInfrastructureGraph(CloudSim simulation, int numFogDcs) {
        simulation.setNetworkTopology(infrastructureGraph);
        infrastructureGraph.addCloudDc(cloudDc);

        for (Location trafficLightLocation : trafficLightLocations) {
            TrafficLightSystem tls = new TrafficLightSystem(simulation, trafficLightLocation, cctvApplicationGenerator);
            infrastructureGraph.addTrafficLightSystem(tls);
        }

        // On initialization Fog nodes are attached to random traffic lights
        for (Location fogDcLocation : pickNRandomElements(trafficLightLocations, numFogDcs)) {
            DatacenterFog fogDc = new DatacenterFog(simulation, fogDcLocation, FOG_SHUTDOWN_DEADLINE);
            infrastructureGraph.addFogDc(fogDc);
        }
    }

    public Orchestrator getOrchestrator() {
        return orchestrator;
    }

    public CctvApplicationGenerator getCctvApplicationGenerator() {
        return cctvApplicationGenerator;
    }

    public V2iApplicationGenerator getV2iApplicationGenerator() {
        return v2iApplicationGenerator;
    }

    /**
     * Implements the Durstenfeld's algorithm for picking n random elements in a list.
     */
    private static <E> List<E> pickNRandomElements(List<E> list, int n) {
        int length = list.size();
        for (int i = length - 1; i >= length - n; --i) {
            Collections.swap(list, i , new Random(SEED).nextInt(i + 1));
        }
        return list.subList(length - n, length);
    }
}
