package de.hsrm.mi.swt.snackman.entities.mapObject.snack;

/**
 * Class that defines Snack, that can be eaten by EatingMob's
 */
public class Snack {
    private SnackType snackType;
    private int calories;

    public Snack(SnackType snackType) {
        setSnackType(snackType);
    }

    public SnackType getSnackType() {
        return this.snackType;
    }

    public void setSnackType(SnackType snackType) {
        this.snackType = snackType;
        this.calories = snackType.getCalories();
    }

    public int getCalories() {
        return this.calories;
    }

    public void setCalories(int newCalories) {
        this.calories = newCalories;
    }

    @Override
    public String toString() {
        return "Snack{" +
                "snackType=" + snackType +
                '}';
    }
}
