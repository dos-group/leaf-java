package org.cityexperiment.placement;

import org.cityexperiment.infrastructure.DatacenterFog;
import org.cityexperiment.infrastructure.InfrastructureGraphCity;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.leaf.application.Application;
import org.leaf.application.Task;
import org.leaf.infrastructure.ComputeNode;
import org.leaf.infrastructure.InfrastructureGraph;
import org.leaf.placement.Orchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.cityexperiment.Settings.FOG_SHUTDOWN_DEADLINE;
import static org.cityexperiment.Settings.FOG_UTILIZATION_THRESHOLD;

/**
 * DatacenterBroker for the city experiments.
 */
public class OrchestratorCity extends Orchestrator {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrchestratorCity.class.getSimpleName());

    ComputeNode cloudDc;

    public OrchestratorCity(InfrastructureGraph infrastructureGraph, ComputeNode cloudDc) {
        super(infrastructureGraph);
        this.cloudDc = cloudDc;
    }

    @Override
    public void placeApplication(Application application) {
        for (Task task : application.getTasks()) {
            if (!task.isBound()) {
                task.setComputeNode(determinePlacement(task));
            }
        }
    }

    /**
     * Assigns VMs (Tasks) to data centers (compute nodes).
     *
     * Source and sink tasks get assigned to their bound data center.
     * The strategy for assigning processing tasks depends on whether the shutdown energy-saving mechanism is applied in the experiment.
     * - If FOG_SHUTDOWN_DEADLINE<0 processing tasks are distributed evenly across available fog nodes
     * - If FOG_SHUTDOWN_DEADLINE>=0 processing tasks are consolidated on a minimal number of fog nodes
     *
     * If there are no fog nodes or if all are utilized more than FOG_UTILIZATION_THRESHOLD, processing tasks are placed in the cloud.
     */
    protected ComputeNode determinePlacement(Task task) {
        if (task.isBound()) {
            return task.getComputeNode();
        }

        Comparator<Host> comparator;
        if (FOG_SHUTDOWN_DEADLINE < 0) {
            comparator = findLeastUtilizedFogDc();
        } else {
            comparator = findMaxUtilizedFogDcBelowThreshold();
        }

        InfrastructureGraphCity infrastructureGraph = (InfrastructureGraphCity) getInfrastructureGraph();
        List<DatacenterFog> fogDcs = infrastructureGraph.getFogDcs();
        Optional<Host> optionalHost = fogDcs.stream()
            .map(DatacenterSimple::getHostList)
            .flatMap(List::stream)
            .max(comparator);

        if (optionalHost.isPresent()) {
            Host host = optionalHost.get();
            if (host.getCpuPercentUtilization() < FOG_UTILIZATION_THRESHOLD) {
                return (ComputeNode) optionalHost.get().getDatacenter();
            } else {
                LOGGER.warn("All fog nodes running at >{} capacity. Placing {} in the cloud.", FOG_UTILIZATION_THRESHOLD, task);
            }
        } else {
            LOGGER.info("No fog nodes available. Placing {} in the cloud.", task);
        }
        return cloudDc;
    }

    /**
     * Returns a Comparator that determines the fog data center with the lowest utilization.
     */
    private Comparator<Host> findLeastUtilizedFogDc() {
        return findFogDc(false);
    }

    /**
     * Returns a Comparator that determines the fog data center with the highest utilization below the FOG_UTILIZATION_THRESHOLD.
     */
    private Comparator<Host> findMaxUtilizedFogDcBelowThreshold() {
        return findFogDc(true);
    }

    /**
     * Private helper function for constructing a host Comparator
     */
    private Comparator<Host> findFogDc(boolean max) {
        return Comparator.comparing(Host::getCpuPercentUtilization, (u1, u2) -> {
            if (u1 < FOG_UTILIZATION_THRESHOLD && u2 > FOG_UTILIZATION_THRESHOLD) {
                return 1;
            } else if (u1 > FOG_UTILIZATION_THRESHOLD && u2 < FOG_UTILIZATION_THRESHOLD) {
                return -1;
            } else if (u1 > FOG_UTILIZATION_THRESHOLD && u2 > FOG_UTILIZATION_THRESHOLD) {
                return max ? Double.compare(u2, u1) : Double.compare(u1, u2);
            } else if (u1 < FOG_UTILIZATION_THRESHOLD && u2 < FOG_UTILIZATION_THRESHOLD) {
                return max ? Double.compare(u1, u2) : Double.compare(u2, u1);
            } else {
                return 0;
            }
        });
    }
}
