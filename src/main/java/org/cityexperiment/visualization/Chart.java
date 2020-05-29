package org.cityexperiment.visualization;

import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.Marker;

import java.awt.*;

public abstract class Chart {

    private XYChart chart;

    abstract void update(int currentTimeStep);

    protected static void setSize(XYChart chart, double width, double updateHeight) {
        chart.getStyler().setXAxisMin(0.);
        chart.getStyler().setXAxisMax(width);
        chart.getStyler().setYAxisMin(0.);
        chart.getStyler().setYAxisMax(updateHeight);
    }

    protected static void updateSeries(XYChart chart, String name, double[] X, double[] Y, Marker marker, Color color) {
        if (chart.getSeriesMap().containsKey(name)) {
            chart.updateXYSeries(name, X, Y, null);
        } else {
            XYSeries series = chart.addSeries(name, X, Y, null);
            series.setMarker(marker); // Marker type: circle,rectangle, diamond..
            series.setMarkerColor(color); // The color: blue, red, green, yellow, gray..
            series.setLineStyle(new BasicStroke());
        }
    }

    public XYChart getChart() {
        return chart;
    }

    public void setChart(XYChart chart) {
        this.chart = chart;
    }

}
