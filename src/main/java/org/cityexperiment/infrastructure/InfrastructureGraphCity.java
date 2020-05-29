package org.cityexperiment.infrastructure;

import org.cityexperiment.infrastructure.*;
import org.cityexperiment.infrastructure.DatacenterCloud;
import org.cityexperiment.infrastructure.DatacenterFog;
import org.cloudbus.cloudsim.core.CloudSimEntity;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cityexperiment.infrastructure.Taxi;
import org.cityexperiment.infrastructure.TrafficLightSystem;
import org.leaf.infrastructure.ComputeNode;
import org.leaf.infrastructure.NetworkLink;
import org.leaf.infrastructure.InfrastructureGraph;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.cityexperiment.Settings.*;

/**
 * Infrastructure graph that allows adding cloud and fog data centers, traffic light systems and taxis
 * and directly links them together with the correct NetworkLinks.
 */
public class InfrastructureGraphCity extends InfrastructureGraph {

    /**
     * Cloud data centers are connected to other data centers
     */
    public void addCloudDc(DatacenterCloud cloudDc) {
        getGraph().addVertex(cloudDc);
        for (DatacenterCloud dc : getCloudDcs()) {
            if (cloudDc == dc) continue;
            addLink(new NetworkLinkWan(cloudDc, dc));
        }
    }

    /**
     * Fog data centers are connected to a traffic light system via Ethernet (no power usage)
     */
    public void addFogDc(DatacenterFog fogDc) {
        getGraph().addVertex(fogDc);
        for (TrafficLightSystem _tls : getTlsInRange(fogDc)) {
            if (fogDc.getLocation().equals(_tls.getLocation())) {
                addLink(new NetworkLinkEthernet(fogDc, _tls));
            }
        }
    }

    /**
     * Cars are connected to all traffic light systems in range via WiFi.
     * This initial allocation may change during simulation since taxis are mobile
     *
     * @see #update()
     */
    public void addCar(Taxi taxi) {
        getGraph().addVertex(taxi);
        for (TrafficLightSystem _tls : getTlsInRange(taxi)) {
            addLink(new NetworkLinkWifiTaxiToAp(taxi, _tls));
        }
    }

    /**
     * Traffic light systems are connected to the cloud via WAN and to other traffic light systems in range via WiFi.
     */
    public void addTrafficLightSystem(TrafficLightSystem tls) {
        getGraph().addVertex(tls);
        for (DatacenterCloud dc : getCloudDcs()) {
            addLink(new NetworkLinkWan(tls, dc));
        }
        for (TrafficLightSystem _tls : getTlsInRange(tls)) {
            addLink(new NetworkLinkWifiApToAp(tls, _tls));
        }
    }

    public void removeCar(Taxi taxi) {
        getGraph().removeVertex(taxi);
    }

    /**
     * Recalculates the traffic light systems in range for all taxis.
     */
    public void update() {
        for (Taxi taxi : getTaxis()) {
            Set<TrafficLightSystem> edgesToRemove = getCurrentlyLinkedTls(taxi);
            Set<TrafficLightSystem> edgesToAdd = getTlsInRange(taxi);

            Set<TrafficLightSystem> union = new HashSet<>(edgesToRemove);
            union.retainAll(edgesToAdd);

            edgesToRemove.removeAll(union);
            edgesToAdd.removeAll(union);

            for (TrafficLightSystem tls : edgesToRemove) {
                removeLink(taxi, tls);
            }
            for (TrafficLightSystem tls : edgesToAdd) {
                addLink(new NetworkLinkWifiTaxiToAp(taxi, tls));
            }
        }
    }

    public List<DatacenterCloud> getCloudDcs() {
        return getVertexByType(DatacenterCloud.class);
    }

    public List<DatacenterFog> getFogDcs() {
        return getVertexByType(DatacenterFog.class);
    }

    public List<Taxi> getTaxis() {
        return filterStarted(getVertexByType(Taxi.class));
    }

    public List<TrafficLightSystem> getTraficLightSystems() {
        return getVertexByType(TrafficLightSystem.class);
    }

    public List<NetworkLinkWifi> getWifiLinks() {
        return getEdgeByType(NetworkLinkWifi.class);
    }

    public List<NetworkLinkWan> getWanLinks() {
        return getEdgeByType(NetworkLinkWan.class);
    }

    private <T extends SimEntity> List<T> getVertexByType(Class<T> x) {
        return getGraph().vertexSet().stream()
            .filter(x::isInstance)
            .map(x::cast)
            .collect(Collectors.toList());
    }

    private <T extends NetworkLink> List<T> getEdgeByType(Class<T> x) {
        return getGraph().edgeSet().stream()
            .filter(x::isInstance)
            .map(x::cast)
            .collect(Collectors.toList());
    }

    private <T extends CloudSimEntity> List<T> filterStarted(List<T> entity) {
        return entity.stream().filter(CloudSimEntity::isStarted).collect(Collectors.toList());
    }

    private Set<TrafficLightSystem> getCurrentlyLinkedTls(ComputeNode dc) {
        return getGraph().outgoingEdgesOf(dc).stream()
            .map(NetworkLink::getDst)
            .map(TrafficLightSystem.class::cast)
            .collect(Collectors.toSet());
    }

    private Set<TrafficLightSystem> getTlsInRange(ComputeNode dc) {
        return getTraficLightSystems().stream()
            .filter(tls -> tls.getLocation().distance(dc.getLocation()) <= WIFI_RANGE)
            .filter(tls -> tls != dc)
            .collect(Collectors.toSet());
    }
}

