package org.leaf.infrastructure;

import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.network.topologies.NetworkTopology;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.leaf.application.Application;
import org.leaf.placement.Orchestrator;

/**
 * Infrastructure topology connecting compute nodes with network links in a weighted graph.
 *
 * The implementation is a bit messy because it complies with the outdated CloudSim NetworkTopology interface.
 */
public class InfrastructureGraph implements NetworkTopology {

    public static InfrastructureGraph NULL = new InfrastructureGraph() {};

    private DirectedWeightedMultigraph<SimEntity, NetworkLink> graph;

    public InfrastructureGraph() {
        graph = new DirectedWeightedMultigraph<>(NetworkLink.class);
    }

    public void addLink(NetworkLink networkLink) {
        graph.addVertex(networkLink.getSrc());
        graph.addVertex(networkLink.getDst());
        graph.addEdge(networkLink.getSrc(), networkLink.getDst(), networkLink);
        graph.setEdgeWeight(networkLink, networkLink.getLatency());  // in jgrapht all access to the weight of an edge must go through the graph interface
    }

    @Override
    public void addLink(SimEntity src, SimEntity dest, double bw, double lat) {
        // "Old" CloudSim API
        NetworkLink networkLink = new NetworkLink(src, dest);
        networkLink.setBandwidth(bw);
        networkLink.setLatency(lat);
        addLink(networkLink);
    }

    @Override
    public double getDelay(final SimEntity src, final SimEntity dest) {
        // TODO The Network interfaces in CloudSimPlus are not very good, the try catch should not be necessary
        FloydWarshallShortestPaths<SimEntity, NetworkLink> algorithm = new FloydWarshallShortestPaths<>(graph);
        try {
            return algorithm.getPathWeight(src, dest);  // Returns Double.POSITIVE_INFINITY if no path exists
        } catch (IllegalArgumentException e) {
            return Double.POSITIVE_INFINITY;
        }
    }

    public void removeLink(SimEntity src, SimEntity dest) {
        graph.removeEdge(src, dest);
    }

    public GraphPath<SimEntity, NetworkLink> getPath(final SimEntity src, final SimEntity dest) {
        DijkstraShortestPath<SimEntity, NetworkLink> algorithm = new DijkstraShortestPath<>(graph);
        try {
            return algorithm.getPath(src, dest);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Cannot");
        }
    }

    public DirectedWeightedMultigraph<SimEntity, NetworkLink> getGraph() {
        return graph;
    }

}

