package org.leaf.application;

import org.cloudbus.cloudsim.core.CloudSimEntity;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.Simulation;
import org.cloudbus.cloudsim.core.events.SimEvent;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.power.PowerAware;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.leaf.host.HostLeaf;
import org.leaf.infrastructure.InfrastructureGraph;
import org.leaf.infrastructure.NetworkLink;
import org.leaf.placement.Orchestrator;
import org.leaf.power.PowerModelApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.examples.smart_city_traffic.Settings.SIMULATION_TIME;
import static org.examples.smart_city_traffic.Settings.WIFI_REALLOCATION_INTERVAL;
import static org.leaf.LeafTags.*;

/**
 * LEAF Application
 *
 * A collection of tasks connected by a directed graph.
 */
public class Application extends CloudSimEntity implements PowerAware<PowerModelApplication> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class.getSimpleName());

    public static Application NULL = new Application(Simulation.NULL, Orchestrator.NULL);

    private DirectedAcyclicGraph<Task, DataFlow> graph = new DirectedAcyclicGraph<>(DataFlow.class);
    private Task lastAddedTask;
    private double lastOutgoingBitRate;
    private boolean running = false;

    private Orchestrator orchestrator = Orchestrator.NULL;
    private double reallocationInterval = -1;
    private PowerModelApplication powerModel = PowerModelApplication.NULL;

    /** Keeps track of how many resources are allocated by the application on compute nodes and network links */
    private Map<NetworkLink, Double> reservedNetworkMap = new HashMap<>();
    private Map<HostLeaf, Double> reservedCpuMap = new HashMap<>();

    public Application(Simulation simulation, Orchestrator orchestrator) {
        this(simulation, orchestrator, -1);
    }

    public Application(Simulation simulation, Orchestrator orchestrator, double reallocationInterval) {
        super(simulation);
        this.orchestrator = orchestrator;
        this.reallocationInterval = reallocationInterval;
        setPowerModel(new PowerModelApplication(this));
    }

    @Override
    protected void startEntity() {}

    @Override
    public void processEvent(SimEvent evt) {
        if (evt.getTag() == START_APPLICATION) {
            if (getSimulation().clock() > SIMULATION_TIME) return;
            orchestrator.placeApplication(this);
            checkTasksPlaced();
            reserveNetwork();
            reserveCpu();
            running = true;
            if (this.reallocationInterval > 0) {
                schedule(this.reallocationInterval, UPDATE_NETWORK_TOPOLOGY);
            }
        } else if (evt.getTag() == UPDATE_NETWORK_TOPOLOGY) {
            if (getSimulation().clock() > SIMULATION_TIME) return;
            checkTasksPlaced();
            releaseNetwork();
            reserveNetwork();
            // if (running) schedule(this.reallocationInterval, UPDATE_NETWORK_TOPOLOGY);
        } else if (evt.getTag() == STOP_APPLICATION) {
            releaseNetwork();
            releaseCpu();
            running = false;
            shutdownEntity();
        }
    }

    public Application addSourceTask(final Task task, double outgoingBitRate) {
        if (lastAddedTask != null) {
            throw new IllegalStateException("Pipeline already has a source. " + graph);
        }
        graph.addVertex(task);
        lastAddedTask = task;
        lastOutgoingBitRate = outgoingBitRate;

        return this;
    }

    public Application addProcessingTask(final Task task, double outgoingBitRate) {
        if (lastAddedTask == null) {
            throw new IllegalStateException("Pipeline has no source task, call setSourceTask() first.");
        }
        graph.addVertex(task);
        graph.addEdge(lastAddedTask, task, new DataFlow(lastOutgoingBitRate));
        lastAddedTask = task;
        lastOutgoingBitRate = outgoingBitRate;

        return this;
    }

    public Application addSinkTask(final Task task) {
        if (lastAddedTask == null) {
            throw new IllegalStateException("Pipeline has no source task, call setSourceTask() first.");
        }
        graph.addVertex(task);
        graph.addEdge(lastAddedTask, task, new DataFlow(lastOutgoingBitRate));
        return this;
    }

    public List<Task> getTasks() {
        return new ArrayList<>(graph.vertexSet());
    }

    /**
     * Checks whether all VMs related to the tasks have been placed on the infrastructure
     */
    private void checkTasksPlaced() {
        List<Task> unplacedTasks = graph.vertexSet().stream().filter(task -> task.getHost() == Host.NULL).collect(Collectors.toList());
        if (!unplacedTasks.isEmpty()) {
            throw new IllegalStateException("Cannot start pipeline as not all tasks have been placed on a host. The following tasks have not been placed: " + unplacedTasks);
        }
    }

    /**
     * Finds the shortest path between two tasks on the infrastructure
     */
    private GraphPath<SimEntity, NetworkLink> findNetworkPath(Task srcTask, Task dstTask) {
        InfrastructureGraph network = (InfrastructureGraph) this.getSimulation().getNetworkTopology();
        GraphPath<SimEntity, NetworkLink> path = network.getPath(srcTask.getHost().getDatacenter(), dstTask.getHost().getDatacenter());
        if (path == null) {
            throw new RuntimeException("Could not find a path between " + srcTask + " and " + dstTask);
        }
        return path;
    }

    private void reserveNetwork() {
        for (DataFlow dataFlow : graph.edgeSet()) {
            GraphPath<SimEntity, NetworkLink> path = findNetworkPath(dataFlow.getSourceTask(), dataFlow.getTargetTask());
            for (NetworkLink networkLink : path.getEdgeList()) {
                if (dataFlow.getBitRate() == 0) continue;
                if (networkLink.reserveBandwidth(dataFlow.getBitRate())) {
                    reservedNetworkMap.computeIfPresent(networkLink, (k, v) -> v + dataFlow.getBitRate());
                    reservedNetworkMap.putIfAbsent(networkLink, dataFlow.getBitRate());
                    LOGGER.debug("{}: {}: Reserved {} kbit/s on {}.", getSimulation().clockStr(), getClass().getSimpleName(), dataFlow.getBitRate() / 1000, networkLink);
                } else {
                    throw new IllegalStateException("Cannot allocate " + dataFlow.getBitRate() + " bandwidth at shortest path link " + networkLink +
                        ", which connects the pipeline link " + dataFlow +
                        ". Finding alternative routing paths is not implemented yet.");
                }
            }
        }
    }

    private void releaseNetwork() {
        for (Map.Entry<NetworkLink, Double> entry : reservedNetworkMap.entrySet()) {
            NetworkLink link = entry.getKey();
            double releaseBandwidth = entry.getValue();
            link.releaseBandwidth(releaseBandwidth);
            LOGGER.debug("{}: {}: Released {} kbit/s on {}.", getSimulation().clockStr(), getClass().getSimpleName(), releaseBandwidth / 1000, link);
        }
        reservedNetworkMap = new HashMap<>();
    }

    private void reserveCpu() {
        for (Task task : graph.vertexSet()) {
            HostLeaf host = (HostLeaf) task.getHost();
            double requestedMips = task.getRequestedMips();
            if (requestedMips == 0) continue;
            if (host.reserveMips(requestedMips)) {
                reservedCpuMap.computeIfPresent(host, (k, v) -> v + requestedMips);
                reservedCpuMap.putIfAbsent(host, requestedMips);
                LOGGER.debug("{}: {}: Reserved {} MIPS on {}.", getSimulation().clockStr(), getClass().getSimpleName(), requestedMips, host);
            } else {
                throw new IllegalStateException("Cannot allocate " + requestedMips + " MIPS on " + host +
                    ". Missing resources error handling not implemented yet.");
            }
        }
    }

    private void releaseCpu() {
        for (Map.Entry<HostLeaf, Double> entry : reservedCpuMap.entrySet()) {
            HostLeaf host = entry.getKey();
            double releaseMips = entry.getValue();
            entry.getKey().releaseMips(entry.getValue());
            LOGGER.debug("{}: {}: Released {} MIPS on {}.", getSimulation().clockStr(), getClass().getSimpleName(), releaseMips, host);
        }
        reservedCpuMap = new HashMap<>();
    }

    @Override
    public PowerModelApplication getPowerModel() {
        return powerModel;
    }

    @Override
    public void setPowerModel(PowerModelApplication powerModel) {
        this.powerModel = powerModel;
    }

    public boolean isRunning() {
        return running;
    }

    public DirectedAcyclicGraph<Task, DataFlow> getGraph() {
        return graph;
    }

    public Map<NetworkLink, Double> getReservedNetworkMap() {
        return reservedNetworkMap;
    }

    public Map<HostLeaf, Double> getReservedCpuMap() {
        return reservedCpuMap;
    }
}
