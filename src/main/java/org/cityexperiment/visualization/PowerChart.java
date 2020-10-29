package org.cityexperiment.visualization;

import org.cloudbus.cloudsim.power.PowerMeasurement;
import org.cloudbus.cloudsim.power.PowerMeter;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * Displays the total power usage of all provided PowerMeters over time.
 */
public class PowerChart extends LineChart {

    private List<PowerMeter> powerMeters;
    private double maxWatt = 0;

    public PowerChart(String name, List<PowerMeter> powerMeters, double[] timeSteps) {
        super(timeSteps);
        this.powerMeters = powerMeters;
        initChart(name);
    }

    @Override
    void update(int currentTimeStep) {
        if (currentTimeStep >= timeSteps.length) return;

        double max = 0;
        for (PowerMeter powerMeter : powerMeters) {
            double[] measurements = powerMeter.getPowerMeasurements().stream().mapToDouble(PowerMeasurement::getTotalUsage).toArray();
            double[] t = Arrays.copyOfRange(timeSteps, 0, measurements.length);
            updateSeries(getChart(), powerMeter.getName(), t, measurements, SeriesMarkers.NONE, Color.BLACK);
            for (double measurement : measurements) {
                max = Math.max(max, measurement);
            }
        }

        if (max > maxWatt) {
            maxWatt = max;
            updateHeight(getChart(), maxWatt);
        }
    }

    private void initChart(String name) {
        XYChart powerChart = new XYChartBuilder().height(270).width(450).theme(Styler.ChartTheme.Matlab)
            .title(name).xAxisTitle("Time (s)").yAxisTitle("Power (Watt)").build();
        powerChart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        powerChart.getStyler().setLegendVisible(true);
        powerChart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
        setSize(powerChart, timeSteps.length, 0);
        setChart(powerChart);
    }
}
