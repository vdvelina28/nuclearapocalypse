package net.elileo.nuclearapocalypse;

import net.elileo.nuclearapocalypse.CountdownTimer;

public class CountdownTimer {
    private static final int MAX_TICKS = 20 * 60 * 20; // 20 minutes in ticks (20 ticks per second)
    private int ticksRemaining = MAX_TICKS;

    public void tick() {
        if (ticksRemaining > 0) {
            ticksRemaining--;
        }
    }

    public boolean isFinished() {
        return ticksRemaining <= 0;
    }

    public String getTimeString() {
        int totalSeconds = ticksRemaining / 20; // 20 ticks = 1 second
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void reset() {
        this.ticksRemaining = MAX_TICKS;
    }
}
