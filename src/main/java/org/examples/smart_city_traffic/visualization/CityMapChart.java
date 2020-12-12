package org.examples.smart_city_traffic.visualization;

import org.examples.smart_city_traffic.city.City;
import org.examples.smart_city_traffic.city.Street;
import org.examples.smart_city_traffic.infrastructure.DatacenterFog;
import org.examples.smart_city_traffic.infrastructure.Taxi;
import org.examples.smart_city_traffic.mobility.MobilityManager;
import org.cloudbus.cloudsim.hosts.Host;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.leaf.location.Location;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

import static org.examples.smart_city_traffic.Settings.SIMULATION_TIME;
import static org.examples.smart_city_traffic.Settings.TIME_STEP_INTERVAL;

/**
 * Displays a top-down view of a map of the city.
 *
 * Cars are displayed as red dots.
 * Fog nodes are displayed as blue dots when they are active and blue crosses when they are inactive.
 */
public class CityMapChart extends Chart {

    private MobilityManager mobilityManager;
    private City city;

    public CityMapChart(MobilityManager mobilityManager, City city) {
        this.mobilityManager = mobilityManager;
        this.city = city;
        initChart();
    }

    @Override
    public void update(int currentTimeStep) {
        if (currentTimeStep > SIMULATION_TIME * TIME_STEP_INTERVAL) return;
        List<Location> locations = mobilityManager.getCars().stream().map(Taxi::getLocation).collect(Collectors.toList());
        int nLocations = locations.size();
        if (nLocations > 0) {
            double[] X = new double[nLocations];
            double[] Y = new double[nLocations];
            for (int i = 0; i < nLocations; i++) {
                Location location = locations.get(i);
                X[i] = location.getX();
                Y[i] = location.getY();
            }
            updateActiveFogDcs(getChart());
            updateSeries(getChart(), "taxis", X, Y, SeriesMarkers.CIRCLE, Color.RED);
        }
    }

    private void initChart() {
        XYChart cityMapChart = new XYChartBuilder().height(270).width(450).theme(Styler.ChartTheme.Matlab)
            .title("City Map").xAxisTitle("Width (meters)").yAxisTitle("Length (meters)").build();
        cityMapChart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Scatter);
        cityMapChart.getStyler().setMarkerSize(4);
        cityMapChart.getStyler().setPlotGridLinesVisible(false);
        setSize(cityMapChart, city.getWidth(), city.getHeight());
        drawStreets(cityMapChart);
        // drawTrafficLights(cityMapChart);
        drawFogDcs(cityMapChart);
        setChart(cityMapChart);
    }

    private void drawStreets(XYChart cityMapChart) {
        for (Street street : city.getStreetGraph().edgeSet()) {
            Location source = city.getStreetGraph().getEdgeSource(street);
            Location target = city.getStreetGraph().getEdgeTarget(street);
            double[] X = new double[] {source.getX(), target.getX()};
            double[] Y = new double[] {source.getY(), target.getY()};
            XYSeries streetSeries = cityMapChart.addSeries(street.toString(), X, Y);
            streetSeries.setXYSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
            streetSeries.setMarker(SeriesMarkers.NONE);
            streetSeries.setLineColor(Color.LIGHT_GRAY);
            streetSeries.setLineWidth(1);
            streetSeries.setLineStyle(new BasicStroke());
        }
    }

    private void drawTrafficLights(XYChart cityMapChart) {
        List<Location> trafficLights = city.getTrafficLightLocations();
        double[] X = trafficLights.stream().mapToDouble(Location::getX).toArray();
        double[] Y = trafficLights.stream().mapToDouble(Location::getY).toArray();
        updateSeries(cityMapChart, "Entry Points", X, Y, SeriesMarkers.SQUARE, Color.DARK_GRAY);
    }

    private void drawFogDcs(XYChart cityMapChart) {
        for (DatacenterFog fogDc : city.getInfrastructureGraph().getFogDcs()) {
            updateSeries(cityMapChart, fogDc.getName(), new double[] {fogDc.getLocation().getX()}, new double[] {fogDc.getLocation().getY()}, SeriesMarkers.CROSS, Color.BLUE);
        }
    }

    private void updateActiveFogDcs(XYChart cityMapChart) {
        for (DatacenterFog fogDc : city.getInfrastructureGraph().getFogDcs()) {
            XYSeries series = cityMapChart.getSeriesMap().get(fogDc.getName());
            if (fogDc.getHostList().stream().anyMatch(Host::isActive)) {
                series.setMarker(SeriesMarkers.DIAMOND);
            } else {
                series.setMarker(SeriesMarkers.CROSS);
            }
        }
    }

}
