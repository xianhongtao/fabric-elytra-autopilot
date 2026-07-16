package net.elytraautopilot.strategy;

import net.elytraautopilot.ElytraAutoPilot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Immutable holder for a precomputed per-tick pitch waveform loaded from a CSV
 * resource file under {@code assets/elytraautopilot/strategies/}.
 *
 * <p>
 * The CSV must have a header row containing an {@code angle} column. Each
 * subsequent row provides one tick's strategy angle in degrees (positive =
 * nose-up). The Minecraft pitch sign convention is applied at application time:
 * {@code minecraft_pitch = -strategy_angle}.
 */
public final class FlightStrategy {

    private final double[] angles;

    private FlightStrategy(double[] angles) {
        this.angles = angles;
    }

    /**
     * Loads a strategy CSV from the mod's classpath resources.
     *
     * @param resourceName
     *            the CSV file name (e.g. {@code "climb.csv"})
     * @param expectedTicks
     *            the expected number of data rows (period length)
     * @return the loaded strategy, or {@code null} if the resource is missing or
     *         malformed (an error is logged)
     */
    public static FlightStrategy loadResource(String resourceName, int expectedTicks) {
        String resourcePath = "/assets/elytraautopilot/strategies/" + resourceName;
        try (InputStream stream = FlightStrategy.class.getResourceAsStream(resourcePath);
                BufferedReader reader = stream == null
                        ? null
                        : new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            if (reader == null) {
                ElytraAutoPilot.LOGGER.error("Missing strategy resource: {}", resourcePath);
                return null;
            }

            String header = reader.readLine();
            int angleColumn = findAngleColumn(header, resourcePath);
            List<Double> angleList = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = splitCsvLine(line);
                if (angleColumn >= parts.length) {
                    ElytraAutoPilot.LOGGER.error("Malformed strategy row in {}: {}", resourcePath, line);
                    return null;
                }
                angleList.add(Double.parseDouble(parts[angleColumn]));
            }

            if (angleList.size() != expectedTicks) {
                ElytraAutoPilot.LOGGER.error("{} has {} ticks, expected {}", resourcePath, angleList.size(),
                        expectedTicks);
                return null;
            }

            double[] out = new double[angleList.size()];
            for (int i = 0; i < out.length; i++) {
                out[i] = angleList.get(i);
            }
            return new FlightStrategy(out);
        } catch (IOException | NumberFormatException e) {
            ElytraAutoPilot.LOGGER.error("Failed to load strategy resource: {}", resourcePath, e);
            return null;
        }
    }

    private static int findAngleColumn(String header, String resourcePath) {
        if (header == null) {
            ElytraAutoPilot.LOGGER.error("Empty strategy resource: {}", resourcePath);
            return -1;
        }
        String[] columns = splitCsvLine(header);
        for (int i = 0; i < columns.length; i++) {
            String column = columns[i];
            if ("angle".equals(column) || "angleDeg_pass_to_stepElytra2D".equals(column)) {
                return i;
            }
        }
        ElytraAutoPilot.LOGGER.error("No angle column in strategy resource: {}", resourcePath);
        return -1;
    }

    private static String[] splitCsvLine(String line) {
        String[] parts = line.split(",", -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim().replace("\"", "");
        }
        return parts;
    }

    /**
     * Returns the strategy angle (degrees, positive = nose-up) at the given tick,
     * wrapping periodically.
     */
    public double angleAt(int tick) {
        return angles[Math.floorMod(tick, angles.length)];
    }

    /**
     * Advances the tick index by one, wrapping periodically.
     */
    public int nextTick(int tick) {
        return (tick + 1) % angles.length;
    }

    /**
     * Returns the period length in ticks (one full waveform cycle).
     */
    public int period() {
        return angles.length;
    }
}
