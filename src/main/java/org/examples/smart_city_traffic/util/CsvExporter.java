package org.examples.smart_city_traffic.util;

import de.siegmar.fastcsv.writer.CsvWriter;
import org.examples.smart_city_traffic.mobility.MobilityManager;
import org.cloudbus.cloudsim.power.PowerMeasurement;
import org.cloudbus.cloudsim.power.PowerMeter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.examples.smart_city_traffic.Settings.POWER_MEASUREMENT_INTERVAL;
import static org.examples.smart_city_traffic.Settings.SIMULATION_TIME;

/**
 * Exports a CSV file containing taxi count and PowerMeter measurements.
 *
 * It is expected that all power and taxi count measurements were conducted with the same frequency!
 *
 * The first column "time" describes the simulation time of the measurement
 * The second column "taxis" describes the number of taxis that was on the map at the simulation time
 * For every provided PowerMeter two more columns are added:
 * - "<name> static" for the static energy consumption part of the power measurement
 * - "<name> dynamic" for the dynamic energy consumption part of the power measurement
 */
public class CsvExporter {

    public static void write(String fileName, MobilityManager mm, List<PowerMeter> powerMeters) {
        List<Integer> taxiCountHistory = mm.getTaxiCountHistory();

        String[] types = new String[] {"static", "dynamic"};
        Collection<String[]> csvData = new ArrayList<>();

        String[] columnNames = new String[powerMeters.size() * types.length + 2];  // +2 is for time and taxis columns
        columnNames[0] = "time";
        columnNames[1] = "taxis";
        for (int i = 0; i < powerMeters.size(); i++) {
            for (int j = 0; j < types.length; j++) {
                columnNames[i * types.length + j + 2] = powerMeters.get(i).getName() + " " + types[j];
            }
        }
        csvData.add(columnNames);

        List<List<PowerMeasurement>> measurementsList = powerMeters.stream().map(PowerMeter::getPowerMeasurements).collect(toList());

        for (int line_index = 0; line_index < SIMULATION_TIME / POWER_MEASUREMENT_INTERVAL; line_index++) {
            String[] line = new String[columnNames.length];
            line[0] = Integer.toString(line_index);
            line[1] = Integer.toString(taxiCountHistory.get(line_index));
            for (int i = 0; i < powerMeters.size(); i++) {
                PowerMeasurement measurement = measurementsList.get(i).get(line_index);
                line[i * types.length + 2] = Double.toString(measurement.getStaticPower());
                line[i * types.length + 3] = Double.toString(measurement.getDynamicPower());
            }
            csvData.add(line);
        }

        File file = new File(fileName);
        CsvWriter csvWriter = new CsvWriter();
        try {
            csvWriter.write(file, StandardCharsets.UTF_8, csvData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
