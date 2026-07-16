package net.elytraautopilot.strategy;

/**
 * Flight phase for the strategy-based autopilot mode.
 *
 * <ul>
 * <li>{@link #CLIMB} — uses the fastest-climb-rate waveform to gain
 * altitude.</li>
 * <li>{@link #CRUISE} — uses the fastest-horizontal-speed-smooth waveform for
 * level, high-speed flight.</li>
 * </ul>
 *
 * <p>
 * Phase transitions are driven by absolute altitude with hysteresis (see
 * {@code cruiseAltitudeMin} / {@code cruiseAltitudeMax} in
 * {@link net.elytraautopilot.config.ModConfig}).
 */
public enum FlightPhase {
    CLIMB, CRUISE;

    /**
     * Returns the translation key for this phase's display label.
     */
    public String translationKey() {
        return switch (this) {
            case CLIMB -> "text.elytraautopilot.phase.climb";
            case CRUISE -> "text.elytraautopilot.phase.cruise";
        };
    }
}
