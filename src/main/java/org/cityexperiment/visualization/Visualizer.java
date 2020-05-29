package org.cityexperiment.visualization;

import org.cloudbus.cloudsim.core.CloudSimEntity;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.Simulation;
import org.cloudbus.cloudsim.core.events.SimEvent;
import org.cloudbus.cloudsim.power.PowerMeter;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.cityexperiment.city.City;
import org.cityexperiment.mobility.MobilityManager;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import static org.cityexperiment.CityTags.REPAINT_CHARTS;
import static org.cityexperiment.Settings.*;


/**
 * Live visualization of the progress and state of a simulation.
 *
 * The visualization shows for charts:
 * 1. A map of the city and the moving taxis
 * 2. The number of taxis that were present on the map at a given time
 * 3. The infrastructure power usage over time
 * 4. The application power usage over time
 */
public class Visualizer extends CloudSimEntity {

    private String name;
    private double interval;
    private double totalTime;
    private JFrame simulationResultsFrame;
    private List<Chart> charts = new ArrayList<>();
    private int currentTimeStep = 0;
    private long lastUpdate = 0;  // The visualization is only redrawn every VISUALIZATION_REDRAW_INTERVAL seconds

    public Visualizer(String name,
                      Simulation simulation,
                      City city,
                      MobilityManager mobilityManager,
                      List<PowerMeter> infrastructurePowerMeters,
                      List<PowerMeter> applicationPowerMeters,
                      double interval,
                      double totalTime) {
        super(simulation);
        this.name = name;
        this.interval = interval;
        this.totalTime = totalTime;

        double[] timeSteps = DoubleStream
            .iterate(0, n -> n + interval)
            .limit((long) (totalTime / TIME_STEP_INTERVAL))
            .toArray();

        charts.add(new CityMapChart(mobilityManager, city));
        charts.add(new TaxiChart(mobilityManager, timeSteps));
        charts.add(new PowerChart("Infrastructure Power", infrastructurePowerMeters, timeSteps));
        charts.add(new PowerChart("Application Power", applicationPowerMeters, timeSteps));
    }

    @Override
    protected void startEntity() {
        SwingWrapper<XYChart> swingWrapper = new SwingWrapper<>(charts.stream().map(Chart::getChart).collect(Collectors.toList()));
        simulationResultsFrame = swingWrapper.displayChartMatrix(); // Display charts
        simulationResultsFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        schedule(REPAINT_CHARTS);
    }

    @Override
    public void processEvent(SimEvent evt) {
        if (evt.getTag() == REPAINT_CHARTS) {

            // The visualization is only redrawn every VISUALIZATION_REDRAW_INTERVAL seconds
            long now = System.currentTimeMillis();
            if (now - lastUpdate > 1000 * VISUALIZATION_REDRAW_INTERVAL) {
                repaint();
                lastUpdate = now;
            }

            currentTimeStep++;
            if (getSimulation().clock() <= totalTime) {
                schedule(interval, REPAINT_CHARTS);
            }
        } else if (evt.getTag() == CloudSimTags.END_OF_SIMULATION) {
            // One last repaint when the simulation is over
            repaint();
        } else {
            throw new RuntimeException("Unknown tag: " + evt.getTag());
        }
    }

    private void repaint() {
        charts.forEach(chart -> chart.update(currentTimeStep));
        updateFrameTime();
        simulationResultsFrame.repaint();
    }

    private void updateFrameTime() {
        double clock = this.getSimulation().clock();
        simulationResultsFrame.setTitle(String.format("%s | Simulation time: %ds (%,.2f%%)", name, (int) clock, clock / SIMULATION_TIME * 100));
    }
}
