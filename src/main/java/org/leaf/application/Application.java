package org.leaf.application;

import org.cloudbus.cloudsim.core.CloudSimEntity;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.Simulation;
import org.cloudbus.cloudsim.core.events.SimEvent;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.power.models.*;
import org.cloudbus.cloudsim.vms.Vm;
import org.leaf.host.HostLeaf;
import org.leaf.infrastructure.NetworkLink;
import org.leaf.infrastructure.InfrastructureGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.leaf.power.PowerModelApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static org.cityexperiment.Settings.SIMULATION_TIME;
import static org.cityexperiment.Settings.WIFI_REALLOCATION_INTERVAL;
import static org.leaf.LeafTags.*;

/**
 * LEAF Application
 *
 * A collection of VMs connected by a directed graph.
 */
public class Application extends CloudSimEntity implements PowerAware<PowerModelApplication> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class.getSimpleName());

    public static Application NULL = new Application(Simulation.NULL);

    private DefaultDirectedGraph<Task, DataFlow> graph = new DefaultDirectedGraph<>(DataFlow.class);
    private Task lastAddedOperator;
    private double lastOutgoingBitRate;
    private boolean running = false;

    private PowerModelApplication powerModel = PowerModelApplication.NULL;

    private Map<NetworkLink, Double> reservedNetworkMap = new HashMap<>();
    private Map<HostLeaf, Double> reservedCpuMap = new HashMap<>();


    public Application(Simulation simulation) {
        super(simulation);
        setPowerModel(new PowerModelApplication(this));
    }

    @Override
    protected void startEntity() {}

    @Override
    public void processEvent(SimEvent evt) {
        if (evt.getTag() == START_APPLICATION) {
            if (getSimulation().clock() > SIMULATION_TIME) return;
            checkVmsPlaced();
            reserveNetwork();
            reserveCpu();
            running = true;
        } else if (evt.getTag() == UPDATE_NETWORK) {
            if (getSimulation().clock() > SIMULATION_TIME) return;
            releaseNetwork();
            reserveNetwork();
            if (running) schedule(WIFI_REALLOCATION_INTERVAL, UPDATE_NETWORK);
        } else if (evt.getTag() == STOP_APPLICATION) {
            releaseNetwork();
            releaseCpu();
            running = false;
            shutdownEntity();
        }
    }

    public Application addSourceTask(final Task task, Datacenter boundDatacenter, double outgoingBitRate) {
        if (lastAddedOperator != null) {
            throw new IllegalStateException("Pipeline already has a source. " + graph);
        }
        task.setBoundDatacenter(boundDatacenter);
        graph.addVertex(task);
        lastAddedOperator = task;
        lastOutgoingBitRate = outgoingBitRate;

        return this;
    }


    public Application addProcessingTask(final Task task, double outgoingBitRate) {
        if (lastAddedOperator == null) {
            throw new IllegalStateException("Pipeline has no source task, call setSourceTask() first.");
        }
        graph.addVertex(task);
        graph.addEdge(lastAddedOperator, task, new DataFlow(lastOutgoingBitRate));
        lastAddedOperator = task;
        lastOutgoingBitRate = outgoingBitRate;

        return this;
    }

    public Application addSinkTask(final Task task, Datacenter boundDatacenter) {
        if (lastAddedOperator == null) {
            throw new IllegalStateException("Pipeline has no source task, call setSourceTask() first.");
        }
        task.setBoundDatacenter(boundDatacenter);
        graph.addVertex(task);
        graph.addEdge(lastAddedOperator, task, new DataFlow(lastOutgoingBitRate));
        return this;
    }

    public List<Vm> getVms() {
        return new ArrayList<>(graph.vertexSet());
    }

    /**
     * Checks whether all VMs related to the tasks have been placed on the infrastructure
     */
    private void checkVmsPlaced() {
        List<Vm> unplacedVms = graph.vertexSet().stream().filter(vm -> vm.getHost() == Host.NULL).collect(Collectors.toList());
        if (!unplacedVms.isEmpty()) {
            throw new IllegalStateException("Cannot start pipeline as not all VMs have been placed on a host. The following VMs have not been placed: " + unplacedVms);
        }
    }

    /**
     * Finds the shortest path between two VMs on the infrastructure
     */
    private GraphPath<SimEntity, NetworkLink> findNetworkPath(Vm srcVm, Vm dstVm) {
        InfrastructureGraph network = (InfrastructureGraph) this.getSimulation().getNetworkTopology();
        GraphPath<SimEntity, NetworkLink> path = network.getPath(srcVm.getHost().getDatacenter(), dstVm.getHost().getDatacenter());
        if (path == null) {
            throw new RuntimeException("Could not find a path between " + srcVm + " and " + dstVm);
        }
        return path;
    }

    private void reserveNetwork() {
        for (DataFlow dataFlow : graph.edgeSet()) {
            GraphPath<SimEntity, NetworkLink> path = findNetworkPath(dataFlow.getSourceVm(), dataFlow.getTargetVm());
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
        for (Vm vm : graph.vertexSet()) {
            HostLeaf host = (HostLeaf) vm.getHost();
            double requestedMips = vm.getCurrentRequestedTotalMips();
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

    public DefaultDirectedGraph<Task, DataFlow> getGraph() {
        return graph;
    }

    public Map<NetworkLink, Double> getReservedNetworkMap() {
        return reservedNetworkMap;
    }

    public Map<HostLeaf, Double> getReservedCpuMap() {
        return reservedCpuMap;
    }
}
