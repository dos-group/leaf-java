package org.examples.smart_city_traffic.visualization;

import org.examples.smart_city_traffic.mobility.MobilityManager;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.awt.*;
import java.util.Arrays;

/**
 * Displays the number of taxis that were on the map over time.
 */
public class TaxiChart extends LineChart {

    private MobilityManager mobilityManager;

    private double maxNCars = 0;

    public TaxiChart(MobilityManager mobilityManager, double[] timeSteps) {
        super(timeSteps);
        this.mobilityManager = mobilityManager;
        initChart();
    }

    @Override
    public void update(int currentTimeStep) {
        int historySize = mobilityManager.getTaxiCountHistory().size();
        if (historySize == 0) return;
        if (historySize >= timeSteps.length) historySize = timeSteps.length;
        double[] c = new double[historySize];  // double array so we can directly pass it to updateSeries()
        for (int i=0; i < historySize; i++) {
            c[i] = (double) mobilityManager.getTaxiCountHistory().get(i);
            if (c[i] > maxNCars) {
                maxNCars = c[i];
                updateHeight(getChart(), maxNCars);
            }
        }
        double[] t = Arrays.copyOfRange(timeSteps, 0, historySize);
        updateSeries(getChart(), "Taxis", t, c, SeriesMarkers.NONE, Color.BLACK);
    }

    private void initChart() {
        XYChart taxisChart = new XYChartBuilder().height(270).width(450).theme(Styler.ChartTheme.Matlab)
            .title("Taxis").xAxisTitle("Time (s)").yAxisTitle("Number of taxis").build();
        taxisChart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        setSize(taxisChart, timeSteps.length, 0);
        setChart(taxisChart);
    }
}
