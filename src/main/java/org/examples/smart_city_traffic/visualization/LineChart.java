package org.examples.smart_city_traffic.visualization;

import org.knowm.xchart.XYChart;

/**
 * Base class for line charts.
 */
public abstract class LineChart extends Chart {

    protected double[] timeSteps;

    public LineChart(double[] timeSteps) {
        this.timeSteps = timeSteps;
    }

    protected static void updateHeight(XYChart chart, double height) {
        chart.getStyler().setYAxisMax(height);
    }
}
