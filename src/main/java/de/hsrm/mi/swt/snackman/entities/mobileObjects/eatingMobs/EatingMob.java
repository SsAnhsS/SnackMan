package de.hsrm.mi.swt.snackman.entities.mobileObjects.eatingMobs;

import de.hsrm.mi.swt.snackman.configuration.GameConfig;
import de.hsrm.mi.swt.snackman.entities.map.GameMap;
import de.hsrm.mi.swt.snackman.configuration.GameConfig;
import de.hsrm.mi.swt.snackman.entities.map.Square;
import de.hsrm.mi.swt.snackman.entities.mapObject.snack.Snack;
import de.hsrm.mi.swt.snackman.entities.mapObject.snack.SnackType;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.Mob;

/**
 * A mob which can consume snacks
 */
public abstract class EatingMob extends Mob {
    private int kcal;

    private int MAXKCAL = 0;

    public EatingMob(GameMap gameMap, double speed, double radius) {
        super(gameMap, speed, radius);
        if ((this) instanceof SnackMan) {
            MAXKCAL = GameConfig.SNACKMAN_MAX_CALORIES;
        }
    }

    public EatingMob(GameMap gameMap, double speed, double radius, double posX, double posY, double posZ) {
        super(gameMap, speed, radius, posX, posY, posZ);
    }

    public EatingMob() {
        super();
    }

    public int getKcal() {
        return kcal;
    }

    public void setKcal(int value) {
        kcal = value;
    }

    protected void gainKcal(int addingKcal) throws Exception {
        if ((this.kcal + addingKcal) >= 0) {
            this.kcal += addingKcal;
        } else {
            throw new Exception("Kcal cannot be below zero!");
        }
    }

    public void loseKcal(int loseKcal) throws Exception {
        if ((this.kcal - loseKcal) >= 0) {
            this.kcal -= loseKcal;
        } else {
            throw new Exception("Kcal cannot be below zero!");
        }
    }

    @Override
    public void move(boolean f, boolean b, boolean l, boolean r, double delta, GameMap gameMap) {
        super.move(f, b, l, r, delta, gameMap);

        Square currentSquare = gameMap.getSquareAtIndexXZ(calcMapIndexOfCoordinate(this.getPosX()), calcMapIndexOfCoordinate(this.getPosZ()));
        if (currentSquare.getSnack().getSnackType() != SnackType.EMPTY)
            consumeSnackOnSquare(currentSquare);
    }

    /**
     * Collects the snack on the square if there is one.
     * If there is one than remove it from the square.
     *
     * @param square to eat the snack from
     */
    public void consumeSnackOnSquare(Square square) {
        Snack snackOnSquare = square.getSnack();

        if (snackOnSquare.getSnackType() != SnackType.EMPTY) {
            if ((kcal + snackOnSquare.getCalories()) >= MAXKCAL) {
                setKcal(MAXKCAL);
            } else {
                setKcal(kcal += snackOnSquare.getCalories());
            }

            //set snack to null after consuming it
            square.setSnack(new Snack(SnackType.EMPTY));
        }
    }
}
