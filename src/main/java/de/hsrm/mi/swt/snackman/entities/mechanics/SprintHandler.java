package de.hsrm.mi.swt.snackman.entities.mechanics;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Handles sprinting mechanics, including sprint duration, cooldown management,
 * and timers for both states. This class ensures that sprinting cannot occur
 * indefinitely and introduces a cooldown period after sprinting is stopped or
 * the sprint time is exhausted.
 */
public class SprintHandler {
    private static final int MAX_SPRINT_TIME_SEC = 5;

    private int sprintTimeLeft = MAX_SPRINT_TIME_SEC;
    private int cooldownTimeLeft = 0;

    private boolean isSprinting = false;
    private boolean isCooldown = false;

    private Timer sprintTimer = new Timer(true);
    private Timer cooldownTimer = new Timer(true);

    /**
     * Starts sprinting if no cooldown is active and the player is not already sprinting.
     * Initializes a timer to decrement the remaining sprint time.
     */
    public void startSprint() {
        if (isCooldown) {
            return;
        }

        if (isSprinting) {
            return;
        }

        isSprinting = true;

        cooldownTimer.cancel();
        cooldownTimer = new Timer(true);

        sprintTimer.cancel();
        sprintTimer = new Timer(true);
        sprintTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (sprintTimeLeft > 0) {
                    sprintTimeLeft--;
                } else {
                    stopSprint();
                    startCooldown(MAX_SPRINT_TIME_SEC * 2); // Cooldown of 2x sprint time
                }
            }
        }, 0, 1000); // Execute every second
    }

    /**
     * Stops sprinting, calculates the cooldown duration based on the elapsed sprint time,
     * and starts the cooldown timer.
     */
    public void stopSprint() {
        if (!isSprinting) {
            return;
        }

        int cooldownDuration = (MAX_SPRINT_TIME_SEC - sprintTimeLeft) * 2;

        isSprinting = false;
        sprintTimer.cancel();
        startCooldown(cooldownDuration);
    }

    /**
     * Starts the cooldown period after sprinting. Prevents sprinting during the cooldown.
     *
     * @param cooldownDuration Duration of the cooldown in seconds.
     */
    public void startCooldown(int cooldownDuration) {
        if (isCooldown) {
            return;
        }

        isCooldown = true;
        cooldownTimeLeft = cooldownDuration;

        cooldownTimer.cancel();
        cooldownTimer = new Timer(true);
        cooldownTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (cooldownTimeLeft > 0) {
                    cooldownTimeLeft--;
                } else {
                    isCooldown = false;
                    sprintTimeLeft = MAX_SPRINT_TIME_SEC;
                    cooldownTimer.cancel();
                }
            }
        }, 0, 1000);
    }

    public boolean canSprint() {
        return !isCooldown && sprintTimeLeft > 0;
    }

    public boolean isInCooldown() {
        return isCooldown;
    }

    public int getSprintTimeLeft() {
        return sprintTimeLeft;
    }

    public int getCooldownTimeLeft() {
        return cooldownTimeLeft;
    }
}