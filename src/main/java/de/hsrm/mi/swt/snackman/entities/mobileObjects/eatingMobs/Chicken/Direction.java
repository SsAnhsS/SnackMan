package de.hsrm.mi.swt.snackman.entities.mobileObjects.eatingMobs.Chicken;

import de.hsrm.mi.swt.snackman.entities.map.GameMap;
import de.hsrm.mi.swt.snackman.entities.map.Square;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * The direction the chicken could move into
 * North: move up in maze into direction of z
 * South: move down in maze into direction of x
 * this is what the coordinates look like
 ****************************** z *******************************
 *           (-1, -1)          (-1, 0)          (-1, 1)         *
 *         NORTHWEST           NORTH           NORTHEAST        *
 *              \                 |                 /           *
 *               \                |                /            *
 *                \               |               /             *
 *      (-1) ----- WEST ---- (0, 0) ---- EAST ------ (+1)       x
 *                /               |               \             *
 *               /                |                \            *
 *              /                 |                 \           *
 *         SOUTHWEST           SOUTH           SOUTHEAST        *
 *          (1, -1)            (1, 0)            (1, 1)         *
 * 2N2W | 2NW  |  2N  | 2NO  | 2N2O  |
 *  N2W |  NW  |   N  |  NO  |  N2O  |
 *   2W |   W  |      |   O  |   2O  |
 *  S2W |  SW  |   S  |  SO  |  S2O  |
 * 2S2W | 2SW  |  2S  | 2SO  | 2S2O  |
 * --------------------------------------------------------------
 */
public enum Direction {
    TWO_NORTH_TWO_WEST("", -2, -2), TWO_NORTH_ONE_WEST("", -2, -1), TWO_NORTH("ß", -2, 0), TWO_NORTH_ONE_EAST("", -2, 1), TWO_NORTH_TWO_EAST("", -2, 2),
    ONE_NORTH_TWO_WEST("", -1, -2), ONE_NORTH_ONE_WEST("", -1, -1), ONE_NORTH("0", -1, 0), ONE_NORTH_ONE_EAST("", -1, 1), ONE_NORTH_TWO_EAST("", -1, 2),
    TWO_WEST("", 0, -2), ONE_WEST("3", 0, -1), CHICKEN("", 0, 0), ONE_EAST("1", 0, 1), TWO_EAST("", 0, 2),
    ONE_SOUTH_TWO_WEST("", 1, -2), ONE_SOUTH_ONE_WEST("", 1, -1), ONE_SOUTH("2", 1, 0), ONE_SOUTH_ONE_EAST("", 1, 1), ONE_SOUTH_TWO_EAST("", 1, 2),
    TWO_SOUTH_TWO_WEST("", 2, -2), TWO_SOUTH_ONE_WEST("", 2, -1), TWO_SOUTH("", 2, 0), TWO_SOUTH_ONE_EAST("", 2, 1), TWO_SOUTH_TWO_EAST("", 2, 2);


    private static final Logger log = LoggerFactory.getLogger(Direction.class);
    private final String indexOfList;
    private final int deltaX;
    private final int deltaZ;

    Direction(String indexOfList, int deltaX, int deltaZ) {
        this.indexOfList = indexOfList;
        this.deltaX = deltaX;
        this.deltaZ = deltaZ;
    }

    /**
     * Return the Direction as an index used in the chicken movement python script
     *
     * @param indexOfList index of the list where the chicken want to move to
     * @return the direction the chicken wants to move towards
     */
    public static Direction getDirection(int indexOfList) {
        log.debug("Index of List: {}", indexOfList);
        return switch (indexOfList) {
            case 0 -> Direction.ONE_NORTH;
            case 1 -> Direction.ONE_EAST;
            case 2 -> Direction.ONE_SOUTH;
            case 3 -> Direction.ONE_WEST;
            default -> throw new IndexOutOfBoundsException("Chicken is walking into a not defined direction.");
        };
    }

    /**
     * Gets a random direction
     *
     * @return random direction
     */
    public static Direction getRandomDirection() {
        Direction[] mainDirections = {ONE_NORTH, ONE_SOUTH, ONE_EAST, ONE_WEST};
        Random random = new Random();
        int randomIndex = random.nextInt(mainDirections.length);
        return mainDirections[randomIndex];
    }

    /**
     * Gives back the new square-position of the chicken
     *
     * @param x,        z the current position of the chicken
     * @param direction in which the chicken decided to go
     * @return the square which is laying in the direction of the currentPosition
     */
    public synchronized Square getNewPosition(GameMap gameMap, int x, int z, Direction direction) {
        Square currentChickenPosition = gameMap.getSquareAtIndexXZ(x, z);

        return gameMap.getSquareAtIndexXZ(
                currentChickenPosition.getIndexX() + direction.deltaX,
                currentChickenPosition.getIndexZ() + direction.deltaZ
        );
    }

    public Square get_two_North_two_West_Square(GameMap gameMap, Square currentPosition) {
        return gameMap.getSquareAtIndexXZ(currentPosition.getIndexX() + TWO_NORTH_TWO_WEST.deltaX, currentPosition.getIndexZ() + TWO_NORTH_TWO_WEST.deltaZ);
    }

    public Square get_two_North_one_West_Square(GameMap gameMap, Square currentPosition) {
        return gameMap.getSquareAtIndexXZ(currentPosition.getIndexX() + TWO_NORTH_ONE_WEST.deltaX, currentPosition.getIndexZ() + TWO_NORTH_ONE_WEST.deltaZ);
    }

    public Square get_two_North_Square(GameMap gameMap, Square currentPosition) {
        return gameMap.getSquareAtIndexXZ(currentPosition.getIndexX() + TWO_NORTH.deltaX, currentPosition.getIndexZ() + TWO_NORTH.deltaZ);
    }

    public Square get_two_North_one_East_Square(GameMap gameMap, Square currentPosition) {
        return gameMap.getSquareAtIndexXZ(currentPosition.getIndexX() + TWO_NORTH_ONE_EAST.deltaX, currentPosition.getIndexZ() + TWO_NORTH_ONE_EAST.deltaZ);
    }

    public Square get_two_North_two_East_Square(GameMap gameMap, Square currentPosition) {
        return gameMap.getSquareAtIndexXZ(currentPosition.getIndexX() + TWO_NORTH_TWO_EAST.deltaX, currentPosition.getIndexZ() + TWO_NORTH_TWO_EAST.deltaZ);
    }

    public Square get_one_North_two_West_Square(GameMap gameMap, Square currentPosition) {
        return gameMap.getSquareAtIndexXZ(currentPosition.getIndexX() + ONE_NORTH_TWO_WEST.deltaX, currentPosition.getIndexZ() + ONE_NORTH_TWO_WEST.deltaZ);
    }

    public Square get_one_North_one_West_Square(GameMap gameMap, Square currentPosition) {
        return gameMap.getSquareAtIndexXZ(currentPosition.getIndexX() + ONE_NORTH_ONE_WEST.deltaX, currentPosition.getIndexZ() + ONE_NORTH_ONE_WEST.deltaZ);
    }

    public Square get_one_North_Square(GameMap gameMap, Square currentPosition) {
        return gameMap.getSquareAtIndexXZ(currentPosition.getIndexX() + ONE_NORTH.deltaX, currentPosition.getIndexZ() + ONE_NORTH.deltaZ);
    }

    public Square get_one_North_one_East_Square(GameMap gameMap, Square currentPosition) {
        return gameMap.getSquareAtIndexXZ(currentPosition.getIndexX() + ONE_NORTH_ONE_EAST.deltaX, currentPosition.getIndexZ() + ONE_NORTH_ONE_EAST.deltaZ);
    }

    public Square get_one_North_two_East_Square(GameMap gameMap, Square currentPosition) {
        return gameMap.getSquareAtIndexXZ(currentPosition.getIndexX() + ONE_NORTH_TWO_EAST.deltaX, currentPosition.getIndexZ() + ONE_NORTH_TWO_EAST.deltaZ);
    }

    public Square get_two_West_Square(GameMap gameMap, Square currentPosition) {
        return gameMap.getSquareAtIndexXZ(currentPosition.getIndexX() + TWO_WEST.deltaX, currentPosition.getIndexZ() + TWO_WEST.deltaZ);
    }

    public Square get_one_West_Square(GameMap gameMap, Square currentPosition) {
        return gameMap.getSquareAtIndexXZ(currentPosition.getIndexX() + ONE_WEST.deltaX, currentPosition.getIndexZ() + ONE_WEST.deltaZ);
    }

    public Square get_Chicken_Square(GameMap gameMap, Square currentPosition) {
        return gameMap.getSquareAtIndexXZ(currentPosition.getIndexX() + CHICKEN.deltaX, currentPosition.getIndexZ() + CHICKEN.deltaZ);
    }

    public Square get_one_East_Square(GameMap gameMap, Square currentPosition) {
        return gameMap.getSquareAtIndexXZ(currentPosition.getIndexX() + ONE_EAST.deltaX, currentPosition.getIndexZ() + ONE_EAST.deltaZ);
    }

    public Square get_two_East_Square(GameMap gameMap, Square currentPosition) {
        return gameMap.getSquareAtIndexXZ(currentPosition.getIndexX() + TWO_EAST.deltaX, currentPosition.getIndexZ() + TWO_EAST.deltaZ);
    }

    public Square get_one_South_two_West_Square(GameMap gameMap, Square currentPosition) {
        return gameMap.getSquareAtIndexXZ(currentPosition.getIndexX() + ONE_SOUTH_TWO_WEST.deltaX, currentPosition.getIndexZ() + ONE_SOUTH_TWO_WEST.deltaZ);
    }

    public Square get_one_South_one_West_Square(GameMap gameMap, Square currentPosition) {
        return gameMap.getSquareAtIndexXZ(currentPosition.getIndexX() + ONE_SOUTH_ONE_WEST.deltaX, currentPosition.getIndexZ() + ONE_SOUTH_ONE_WEST.deltaZ);
    }

    public Square get_one_South_Square(GameMap gameMap, Square currentPosition) {
        return gameMap.getSquareAtIndexXZ(currentPosition.getIndexX() + ONE_SOUTH.deltaX, currentPosition.getIndexZ() + ONE_SOUTH.deltaZ);
    }

    public Square get_one_South_one_East_Square(GameMap gameMap, Square currentPosition) {
        return gameMap.getSquareAtIndexXZ(currentPosition.getIndexX() + ONE_SOUTH_ONE_EAST.deltaX, currentPosition.getIndexZ() + ONE_SOUTH_ONE_EAST.deltaZ);
    }

    public Square get_one_South_two_East_Square(GameMap gameMap, Square currentPosition) {
        return gameMap.getSquareAtIndexXZ(currentPosition.getIndexX() + ONE_SOUTH_TWO_EAST.deltaX, currentPosition.getIndexZ() + ONE_SOUTH_TWO_EAST.deltaZ);
    }

    public Square get_two_South_two_West_Square(GameMap gameMap, Square currentPosition) {
        return gameMap.getSquareAtIndexXZ(currentPosition.getIndexX() + TWO_SOUTH_TWO_WEST.deltaX, currentPosition.getIndexZ() + TWO_SOUTH_TWO_WEST.deltaZ);
    }

    public Square get_two_South_one_West_Square(GameMap gameMap, Square currentPosition) {
        return gameMap.getSquareAtIndexXZ(currentPosition.getIndexX() + TWO_SOUTH_ONE_WEST.deltaX, currentPosition.getIndexZ() + TWO_SOUTH_ONE_WEST.deltaZ);
    }

    public Square get_two_South_Square(GameMap gameMap, Square currentPosition) {
        return gameMap.getSquareAtIndexXZ(currentPosition.getIndexX() + TWO_SOUTH.deltaX, currentPosition.getIndexZ() + TWO_SOUTH.deltaZ);
    }

    public Square get_two_South_one_East_Square(GameMap gameMap, Square currentPosition) {
        return gameMap.getSquareAtIndexXZ(currentPosition.getIndexX() + TWO_SOUTH_ONE_EAST.deltaX, currentPosition.getIndexZ() + TWO_SOUTH_ONE_EAST.deltaZ);
    }

    public Square get_two_South_two_East_Square(GameMap gameMap, Square currentPosition) {

        return gameMap.getSquareAtIndexXZ(currentPosition.getIndexX() + TWO_SOUTH_TWO_EAST.deltaX, currentPosition.getIndexZ() + TWO_SOUTH_TWO_EAST.deltaZ);
    }

    @Override
    public String toString() {
        return indexOfList;
    }
}
