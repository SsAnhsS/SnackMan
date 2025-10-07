package de.hsrm.mi.swt.snackman.entities.mapObject.snack;

import java.util.Random;

public enum SnackType {
    CHERRY(100),
    STRAWBERRY(300),
    ORANGE(500),
    APPLE(700),
    EMPTY(0),
    EGG(0);

    private final int calories;

    SnackType(int calories) {
        this.calories = calories;
    }

    /**
     * Gets a random snack
     *
     * @return random snack
     */
    public static SnackType getRandomSnack() {
        SnackType[] snacks = {CHERRY, STRAWBERRY, ORANGE, APPLE};
        Random random = new Random();
        int randomIndex = random.nextInt(snacks.length);
        SnackType returnSnack = snacks[randomIndex];

        if (returnSnack != SnackType.EGG) {
            return snacks[randomIndex];
        } else {
            return getRandomSnack();
        }
    }

    public int getCalories() {
        return calories;
    }
}
