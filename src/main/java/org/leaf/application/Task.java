package org.leaf.application;

import org.cloudbus.cloudsim.hosts.Host;
import org.leaf.infrastructure.ComputeNode;

/**
 * Task of an Application
 */
public class Task {

    long requestedMips = 0;
    ComputeNode computeNode = ComputeNode.NULL;
    boolean bound = false;

    public Task(long requestedMips) {
        this.requestedMips = requestedMips;
    }

    public Task(long requestedMips, ComputeNode boundComputeNode) {
        this.requestedMips = requestedMips;
        this.computeNode = boundComputeNode;
        this.bound = true;
    }

    public long getRequestedMips() {
        return requestedMips;
    }

    public void setComputeNode(ComputeNode computeNode) {
        this.computeNode = computeNode;
    }

    public ComputeNode getComputeNode() {
        return computeNode;
    }

    public Host getHost() {
        return computeNode.getHostList().stream().findFirst().orElse(Host.NULL);
    }

    public boolean isBound() {
        return bound;
    }
}
