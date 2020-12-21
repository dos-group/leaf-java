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

    public void setBound(boolean bound) {
        this.bound = bound;
    }

    public boolean isBound() {
        return bound;
    }
}
