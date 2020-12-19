package org.examples.smart_city_traffic.mobility;

import org.apache.commons.math3.distribution.PoissonDistribution;
import org.examples.smart_city_traffic.Settings;
import org.examples.smart_city_traffic.city.City;
import org.examples.smart_city_traffic.city.Street;
import org.examples.smart_city_traffic.infrastructure.InfrastructureGraphCity;
import org.examples.smart_city_traffic.infrastructure.Taxi;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimEntity;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.events.SimEvent;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.leaf.location.Location;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.examples.smart_city_traffic.CityTags.*;
import static org.examples.smart_city_traffic.Settings.*;
import static org.leaf.LeafTags.UPDATE_NETWORK_TOPOLOGY;

public class MobilityManager extends CloudSimEntity {

    private City city;
    private TaxiDistribution taxiGenerationDistribution;
    private DijkstraShortestPath<Location, Street> streetPathAlgorithm;

    private List<Integer> taxiCountHistory = new ArrayList<>();
    private long lastReportingTime = System.currentTimeMillis();

    public MobilityManager(CloudSim simulation, City city) {
        super(simulation);
        this.city = city;
        this.taxiGenerationDistribution = new TaxiDistribution();
        this.streetPathAlgorithm = new DijkstraShortestPath<>(city.getStreetGraph());
    }

    public List<Taxi> getCars() {
        InfrastructureGraphCity topology = (InfrastructureGraphCity) getSimulation().getNetworkTopology();
        return topology.getTaxis();
    }

    @Override
    protected void startInternal() {
        schedule(TIME_STEP_INTERVAL, CREATE_CARS);
        schedule(WIFI_REALLOCATION_INTERVAL, UPDATE_NETWORK_TOPOLOGY);
        schedule(POWER_MEASUREMENT_INTERVAL, COUNT_CARS);
    }

    @Override
    public void processEvent(SimEvent evt) {
        InfrastructureGraphCity topology = (InfrastructureGraphCity) getSimulation().getNetworkTopology();
        if (evt.getTag() == CREATE_CARS) {
            for (MobilityModelTaxi mobilityModel : pickNewCarRoutes()) {
                Taxi taxi = new Taxi(getSimulation(), mobilityModel, city.getStmApplicationGenerator());
                topology.addCar(taxi);
                schedule(mobilityModel.getEndTime(), DESTROY_CAR, taxi);
            }
            schedule(TIME_STEP_INTERVAL, CREATE_CARS);
        } else if (evt.getTag() == DESTROY_CAR) {
            Taxi taxi = (Taxi) evt.getData();
            topology.removeCar(taxi);
            taxi.shutdown();
        } else if (evt.getTag() == UPDATE_NETWORK_TOPOLOGY) {
            InfrastructureGraphCity network = (InfrastructureGraphCity) this.getSimulation().getNetworkTopology();
            network.update();
            schedule(WIFI_REALLOCATION_INTERVAL, UPDATE_NETWORK_TOPOLOGY);
        } else if (evt.getTag() == COUNT_CARS) {
            taxiCountHistory.add(getCars().size());
            schedule(POWER_MEASUREMENT_INTERVAL, COUNT_CARS);
        } else if (evt.getTag() == CloudSimTags.END_OF_SIMULATION) {
            shutdown();
            topology.getTaxis().forEach(Taxi::shutdown);
        }

        // Printing simulation progress: This should go to a separate module
        long now = System.currentTimeMillis();
        if (now - lastReportingTime > PRINT_PROGRESS_INTERVAL * 1000) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println(simpleDateFormat.format(new Date(now)) + String.format(":\t %,.2f%% (%ds)", getSimulation().clock() / SIMULATION_TIME * 100, (int) getSimulation().clock()));
            lastReportingTime = now;
        }
    }

    public List<Integer> getTaxiCountHistory() {
        return taxiCountHistory;
    }

    private List<MobilityModelTaxi> pickNewCarRoutes() {
        double mean = taxiGenerationDistribution.getCount(getSimulation().clock());
        double speed = taxiGenerationDistribution.getSpeed(getSimulation().clock());

        List<MobilityModelTaxi> result = new ArrayList<>();
        if (mean == 0) {
            return result;
        }
        PoissonDistribution distribution = new PoissonDistribution(mean);
        distribution.reseedRandomGenerator(Settings.RANDOM.nextInt());
        int nNewCars = distribution.sample();
        if (nNewCars > 0) {
            for (int i = 0; i < nNewCars; i++) {
                Location start = getRandomGateLocation();
                Location destination;
                do {
                    destination = getRandomGateLocation();
                } while (start.distance(destination) < CITY_WIDTH * 0.5);  // Make sure start and end are not too close for more realistic routes
                result.add(new MobilityModelTaxi(streetPathAlgorithm.getPath(start, destination), speed, TIME_STEP_INTERVAL));
            }
        }
        return result;
    }

    private Location getRandomGateLocation() {
        List<Location> entryPoints = city.getGateLocations();
        return entryPoints.get(Settings.RANDOM.nextInt(entryPoints.size()));
    }
}
