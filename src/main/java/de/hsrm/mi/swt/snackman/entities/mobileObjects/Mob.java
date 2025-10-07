package de.hsrm.mi.swt.snackman.entities.mobileObjects;

import de.hsrm.mi.swt.snackman.entities.map.GameMap;

import org.joml.Quaterniond;
import org.joml.Vector3d;

import de.hsrm.mi.swt.snackman.configuration.GameConfig;
import de.hsrm.mi.swt.snackman.entities.map.GameMap;
import de.hsrm.mi.swt.snackman.entities.map.Square;
import de.hsrm.mi.swt.snackman.entities.map.enums.WallAlignmentStatus;
import de.hsrm.mi.swt.snackman.entities.map.enums.WallSectionStatus;
import de.hsrm.mi.swt.snackman.entities.mapObject.MapObjectType;

/**
 * A mobile object with the ability to move its position
 */
public abstract class Mob {
    private static long idCounter = 0;
    protected long id;
    private Vector3d position;
    private double radius;
    private Quaterniond quat;
    private double speed;
    private Vector3d spawn;
    private Vector3d forward = new Vector3d(0, 0, -1);
    private GameMap gameMap;
    private Vector3d tempPosition  = new Vector3d(0, 0, -1);
    private boolean squareUnderneathIsWall = false;

    /**
     * Base constructor for Map with spawn-location at center of Map
     *
     * @param gameMap GameMap
     * @param speed   speed of the mob
     * @param radius  size of the mob
     */
    public Mob(GameMap gameMap, double speed, double radius) {
        this.gameMap = gameMap;
        this.speed = speed;
        this.radius = radius;
        spawn = new Vector3d((gameMap.getGameMapSquares()[0].length / 2.0) * GameConfig.SQUARE_SIZE, GameConfig.SNACKMAN_GROUND_LEVEL,
                (gameMap.getGameMapSquares()[0].length / 2.0) * GameConfig.SQUARE_SIZE);
        position = new Vector3d(spawn);
        quat = new Quaterniond();
        setCurrentSquareWithIndex(position.x, position.z);
        setPositionWithIndexXZ(position.x, position.z);
        id = generateId();
    }

    public Mob() {
        position = new Vector3d();
        quat = new Quaterniond();
    }

    /**
     * Constructor for Mob with custom spawn point
     *
     * @param gameMap MapService of the map the mob is located on
     * @param speed   speed of the mob
     * @param radius  size of the mob
     * @param posX    x-spawn-position
     * @param posY    y-spawn-positon
     * @param posZ    z-spawn-position
     */
    public Mob(GameMap gameMap, double speed, double radius, double posX, double posY, double posZ) {
        this.gameMap = gameMap;
        this.speed = speed;
        this.radius = radius;

        spawn = new Vector3d(posX, posY, posZ);
        position = new Vector3d(spawn);
        quat = new Quaterniond();
        id = generateId();
    }

    protected synchronized static long generateId() {
        return idCounter++;
    }

    public double getPosX() {
        return position.x;
    }

    public void setPosX(double posX) {
        position.x = posX;
    }

    public double getPosY() {
        return position.y;
    }

    public void setPosY(double posY) {
        this.position.y = posY;
    }

    public double getPosZ() {
        return position.z;
    }

    public void setPosZ(double posZ) {
        this.position.z = posZ;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public Quaterniond getRotationQuaternion() {
        return this.quat;
    }

    /**
     * Calculates the square-indices to set the currentSquare
     *
     * @param x x-position
     * @param z z-position
     */
    public void setCurrentSquareWithIndex(double x, double z) {
        setPositionWithIndexXZ(calcMapIndexOfCoordinate(x), calcMapIndexOfCoordinate(z));
    }

    public void setPositionWithIndexXZ(double x, double z) {
        this.position.x = x;
        this.position.z = z;
    }

    /**
     * Moves the player based on inputs and passed time since last update (delta). Forward is relative to the rotation of the player and handled by a quaternion.
     * Result:
     * 0 = no blocked movement direction
     * 1 = blocked x movement direction
     * 2 = blocked z movement direction
     * 3 = blocked x and z movement directions
     *
     * @param f     input forward pressed?
     * @param b     input backward pressed?
     * @param l     input left pressed?
     * @param r     input right pressed?
     * @param delta time since last update
     */
    public void move(boolean f, boolean b, boolean l, boolean r, double delta, GameMap gameMap) {
        int result = 3;
        int moveDirZ = (f ? 1 : 0) - (b ? 1 : 0);
        int moveDirX = (r ? 1 : 0) - (l ? 1 : 0);

        Vector3d move = new Vector3d();

        if (f || b) {
            move.z -= moveDirZ;
        }
        if (l || r) {
            move.x += moveDirX;
        }

        move.rotate(quat);
        move.y = 0;
        if (!(move.x == 0 && move.z == 0))
            move.normalize();
        move.x = move.x * delta * speed;
        move.z = move.z * delta * speed;
        double xNew = position.x + move.x;
        double zNew = position.z + move.z;

        //RESPAWN AUSSERHALB DES LABYRINTHS
        if (gameMap.getGameMapSquares() != null) {
            if((position.y < GameConfig.SNACKMAN_GROUND_LEVEL) || (calcMapIndexOfCoordinate(xNew) < 0 || calcMapIndexOfCoordinate(xNew) >= gameMap.getGameMapSquares().length) ||  calcMapIndexOfCoordinate(zNew) < 0 || calcMapIndexOfCoordinate(zNew) >= gameMap.getGameMapSquares()[0].length){
                respawn();
                return;
            }
        }
        try {
            result = checkWallCollision(xNew, zNew, gameMap);
        } catch (IndexOutOfBoundsException e) {
            respawn();
            return;
        }
        switch (result) {
            case 0:
                position.x += move.x;
                position.z += move.z;
                break;
            case 1:
                position.z += move.z;
                position.x += move.x;
                if (Math.round(position.x) < position.x) {
                    position.x = Math.round(position.x) + this.radius;
                } else {
                    position.x = Math.round(position.x) - this.radius;
                }
                break;
            case 2:
                position.x += move.x;
                position.z += move.z;
                if (Math.round(position.z) < position.z) {
                    position.z = Math.round(position.z) + this.radius;
                } else {
                    position.z = Math.round(position.z) - this.radius;
                }
                break;
            case 3:
                break;
            default:
                break;
        }
        setPositionWithIndexXZ(position.x, position.z);
    }

    public Quaterniond getQuat() {
        return quat;
    }

    /**
     * respawns the mob at his spawn-location
     */
    public void respawn() {
        this.position.x = spawn.x;
        this.position.z = spawn.z;
        this.position.y = GameConfig.SNACKMAN_GROUND_LEVEL;
        //setCurrentSquareWithIndex(position.x, position.z);

    /*
    TODO unterschied zwischen snackman und geistern beachten in konstructor ändern!!
    für geister

    this.position.x = (mapService.getGameMap().getGameMap().length / 2) * GameConfig.SQUARE_SIZE;
        this.position.z = (mapService.getGameMap().getGameMap()[0].length / 2) * GameConfig.SQUARE_SIZE;
        setPositionWithIndexXZ(position.x, position.z);

     */

    }

    /**
     * firstly determines which walls are close and need to be checked for collision
     * secondly checks for collision with the walls (checks if target location is a wall and if player circle overlaps any walls)
     *
     * @param x target x-position
     * @param z target z-position
     * @returns the type of collision represented as an int
     * 0 = no collision
     * 1 = horizontal collision
     * 2 = vertical collision
     * 3 = both / diagonal collision / corner
     */
    public int checkWallCollision(double x, double z, GameMap gameMap) throws IndexOutOfBoundsException {
        if (gameMap.getSquareAtIndexXZ(calcMapIndexOfCoordinate(x), calcMapIndexOfCoordinate(z)).getType() == MapObjectType.WALL) {
            if (getPosY() >= GameConfig.SQUARE_HEIGHT) {
                return 0;
            } else {
                return 3;
            }
        }

        int collisionCase = 0;
        Square currentSquare = gameMap.getSquareAtIndexXZ(calcMapIndexOfCoordinate(x), calcMapIndexOfCoordinate(z));

        double squareCenterX = currentSquare.getIndexX() * GameConfig.SQUARE_SIZE + GameConfig.SQUARE_SIZE / 2;
        double squareCenterZ = currentSquare.getIndexZ() * GameConfig.SQUARE_SIZE + GameConfig.SQUARE_SIZE / 2;

        int horizontalRelativeToCenter = (x - squareCenterX <= 0) ? -1 : 1;
        int verticalRelativeToCenter = (z - squareCenterZ <= 0) ? -1 : 1;

        Square squareLeftRight = gameMap.getSquareAtIndexXZ(currentSquare.getIndexX() + horizontalRelativeToCenter,
                currentSquare.getIndexZ());
        Square squareTopBottom = gameMap.getSquareAtIndexXZ(currentSquare.getIndexX(),
                currentSquare.getIndexZ() + verticalRelativeToCenter);
        Square squareDiagonal = gameMap.getSquareAtIndexXZ(currentSquare.getIndexX() + horizontalRelativeToCenter,
                currentSquare.getIndexZ() + verticalRelativeToCenter);

        if (squareLeftRight.getType() == MapObjectType.WALL) {
            Vector3d origin = new Vector3d(
                    horizontalRelativeToCenter > 0 ? (currentSquare.getIndexX() + 1) * GameConfig.SQUARE_SIZE
                            : currentSquare.getIndexX() * GameConfig.SQUARE_SIZE,
                    0, 1);
            Vector3d line = new Vector3d(0, 1, 0);
            if (calcIntersectionWithLine(x, z, origin, line)) {
                if (getPosY() >= GameConfig.SQUARE_HEIGHT) {
                    collisionCase = 0;
                } else {
                    collisionCase += 1;
                }
            }
        }

        if (squareTopBottom.getType() == MapObjectType.WALL) {
            Vector3d origin = new Vector3d(0,
                    verticalRelativeToCenter > 0 ? (currentSquare.getIndexZ() + 1) * GameConfig.SQUARE_SIZE
                            : currentSquare.getIndexZ() * GameConfig.SQUARE_SIZE,
                    1);
            Vector3d line = new Vector3d(1, 0, 0);
            if (calcIntersectionWithLine(x, z, origin, line)) {
                if (getPosY() >= GameConfig.SQUARE_HEIGHT) {
                    collisionCase = 0;
                } else {
                    collisionCase += 2;
                }
            }
        }

        if (squareDiagonal.getType() == MapObjectType.WALL && collisionCase == 0) {
            double diagX = horizontalRelativeToCenter > 0 ? (currentSquare.getIndexX() + 1) * GameConfig.SQUARE_SIZE
                    : currentSquare.getIndexX() * GameConfig.SQUARE_SIZE;
            double diagZ = verticalRelativeToCenter > 0 ? (currentSquare.getIndexZ() + 1) * GameConfig.SQUARE_SIZE
                    : currentSquare.getIndexZ() * GameConfig.SQUARE_SIZE;
            double dist = Math.sqrt((diagX - x) * (diagX - x) + (diagZ - z) * (diagZ - z));
            if (dist <= this.radius)
                if (getPosY() >= GameConfig.SQUARE_HEIGHT) {
                    collisionCase = 0;
                } else {
                    collisionCase = 3;
                }
        }

        return collisionCase;
    }

    /**
     * calculates whether the player-cirlce intersects with a line
     *
     * @param xNew      target x-position
     * @param zNew      target z-position
     * @param origin
     * @param direction
     * @returns
     */
    public boolean calcIntersectionWithLine(double xNew, double zNew, Vector3d origin, Vector3d direction) {
        Vector3d line = origin.cross(direction);
        double dist = Math.abs(line.x * xNew + line.y * zNew + line.z) / Math.sqrt(line.x * line.x + line.y * line.y);
        return dist <= this.radius;
    }

    public void setQuaternion(double qX, double qY, double qZ, double qW) {
        quat.x = qX;
        quat.y = qY;
        quat.z = qZ;
        quat.w = qW;

        forward.rotate(quat);
    }

    public void setPosition(Vector3d position) {
        this.position = position;
    }

    public int calcMapIndexOfCoordinate(double a) {
        return (int) (a / GameConfig.SQUARE_SIZE);
    }

    public Vector3d getSpawn() {
        return spawn;
    }

    public void setSpawn(Vector3d spawn) {
        this.spawn = spawn;
    }

    public Vector3d getPosition() {
        return position;
    }

    public long getId() {
        return id;
    }

    public boolean squareUnderneathIsWall(Vector3d positionImport) {
        if (gameMap.getSquareAtIndexXZ(calcMapIndexOfCoordinate(positionImport.x), calcMapIndexOfCoordinate(positionImport.z)).getType() == MapObjectType.WALL) {
            return true;
        } else {
            return false;
        }
    }

    public boolean squareUnderneathIsWall() {
        if (gameMap.getSquareAtIndexXZ(calcMapIndexOfCoordinate(position.x), calcMapIndexOfCoordinate(position.z)).getType() == MapObjectType.WALL) {
            return true;
        } else {
            return false;
        }
    }

    public boolean squareUnderneathIsFloor(Vector3d positionImport) {
        if (gameMap.getSquareAtIndexXZ(calcMapIndexOfCoordinate(positionImport.x), calcMapIndexOfCoordinate(positionImport.z)).getType() == MapObjectType.FLOOR) {
            return true;
        } else {
            return false;
        }
    }

    public boolean squareUnderneathIsFloor() {
        if (gameMap.getSquareAtIndexXZ(calcMapIndexOfCoordinate(position.x), calcMapIndexOfCoordinate(position.z)).getType() == MapObjectType.FLOOR) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Moves the player in the opposite direction of the view direction step by step if blocked by a wall, finding a valid floor position or triggering a respawn if none is found.
     */
    public void pushback() {
        tempPosition = position;
        double stepDistance = 0.1;
        Vector3d backward = new Vector3d(forward);
        backward.normalize().negate();
        squareUnderneathIsWall = squareUnderneathIsWall(position);
        while (squareUnderneathIsWall) {
            Vector3d displacement = new Vector3d(backward);
            displacement.mul(stepDistance);
            displacement.y = 0;
            tempPosition.add(displacement);
            squareUnderneathIsWall = squareUnderneathIsWall(tempPosition);
        }
        tempPosition.y = GameConfig.SNACKMAN_GROUND_LEVEL;
        Vector3d additionalDisplacement = new Vector3d(backward).mul(radius);
        additionalDisplacement.y = 0;
        tempPosition.add(additionalDisplacement);
        if (squareUnderneathIsFloor(tempPosition)) {
            position = tempPosition;
        } else {
            respawn();
        }
    }

    /**
     * Moves the player forward step by step if blocked by a wall, finding a valid floor position or triggering a respawn if none is found.
     */
    public void push_forward() {
        tempPosition = position;
        double stepDistance = 0.1;
        Vector3d pushForwardVector = new Vector3d(0, 0, 1);
        boolean squareUnderneathIsWall = squareUnderneathIsWall(position);
        while (squareUnderneathIsWall) {
            Vector3d displacement = new Vector3d(pushForwardVector).mul(stepDistance);
            displacement.y = 0;
            tempPosition.add(displacement);
            squareUnderneathIsWall = squareUnderneathIsWall(tempPosition);
        }
        tempPosition.y = GameConfig.SNACKMAN_GROUND_LEVEL;
        Vector3d additionalDisplacement = new Vector3d(pushForwardVector).mul(radius);
        additionalDisplacement.y = 0;
        tempPosition.add(additionalDisplacement);
        if (squareUnderneathIsFloor(tempPosition)) {
            position = tempPosition;
        } else {
            respawn();
        }
    }

    /**
     * Moves the player backward step by step if blocked by a wall, finding a valid floor position or triggering a respawn if none is found.
     */
    public void push_backward() {
        tempPosition = position;
        double stepDistance = 0.1;
        Vector3d pushBackwardVector = new Vector3d(0, 0, -1);
        boolean squareUnderneathIsWall = squareUnderneathIsWall(position);
        while (squareUnderneathIsWall) {
            Vector3d displacement = new Vector3d(pushBackwardVector).mul(stepDistance);
            displacement.y = 0;
            tempPosition.add(displacement);
            squareUnderneathIsWall = squareUnderneathIsWall(tempPosition);
        }
        tempPosition.y = GameConfig.SNACKMAN_GROUND_LEVEL;
        Vector3d additionalDisplacement = new Vector3d(pushBackwardVector).mul(radius);
        additionalDisplacement.y = 0;
        tempPosition.add(additionalDisplacement);
        if (squareUnderneathIsFloor(tempPosition)) {
            position = tempPosition;
        } else {
            respawn();
        }
    }

    /**
     * Moves the player left step by step if blocked by a wall, finding a valid floor position or triggering a respawn if none is found.
     */
    public void push_left() {
        tempPosition = position;
        double stepDistance = 0.1;
        Vector3d pushLeftVector = new Vector3d(-1, 0, 0);
        boolean squareUnderneathIsWall = squareUnderneathIsWall(position);
        while (squareUnderneathIsWall) {
            Vector3d displacement = new Vector3d(pushLeftVector).mul(stepDistance);
            displacement.y = 0;
            tempPosition.add(displacement);
            squareUnderneathIsWall = squareUnderneathIsWall(tempPosition);
        }
        tempPosition.y = GameConfig.SNACKMAN_GROUND_LEVEL;
        Vector3d additionalDisplacement = new Vector3d(pushLeftVector).mul(radius);
        additionalDisplacement.y = 0;
        tempPosition.add(additionalDisplacement);
        if (squareUnderneathIsFloor(tempPosition)) {
            position = tempPosition;
        } else {
            respawn();
        }
    }

    /**
     * Moves the player right step by step if blocked by a wall, finding a valid floor position or triggering a respawn if none is found.
     */
    public void push_right() {
        tempPosition = position;
        double stepDistance = 0.1;
        Vector3d pushRightVector = new Vector3d(1, 0, 0);
        boolean squareUnderneathIsWall = squareUnderneathIsWall(position);
        while (squareUnderneathIsWall) {
            Vector3d displacement = new Vector3d(pushRightVector).mul(stepDistance);
            displacement.y = 0;
            tempPosition.add(displacement);
            squareUnderneathIsWall = squareUnderneathIsWall(tempPosition);
        }
        tempPosition.y = GameConfig.SNACKMAN_GROUND_LEVEL;
        Vector3d additionalDisplacement = new Vector3d(pushRightVector).mul(radius);
        additionalDisplacement.y = 0;
        tempPosition.add(additionalDisplacement);
        if (squareUnderneathIsFloor(tempPosition)) {
            position = tempPosition;
        } else {
            respawn();
        }
    }

    /**
     * Determines the alignment of walls surrounding the player's current position.
     * This method checks adjacent squares in the game map to evaluate the presence of walls and identifies a specific wall alignment case based on predefined configurations.
     * @return WallAlignmentStatus Enum value representing the alignment of walls relative to the player's position.
     */
    public WallAlignmentStatus checkWallAlignment() {
        int mobX = calcMapIndexOfCoordinate(position.x);
        int mobZ = calcMapIndexOfCoordinate(position.z);
        //Check for wall elements attached to given square
        boolean leftWall = gameMap.getSquareAtIndexXZ(mobX - 1, mobZ).getType() == MapObjectType.WALL;
        boolean rightWall = gameMap.getSquareAtIndexXZ(mobX + 1, mobZ).getType() == MapObjectType.WALL;
        boolean topWall = gameMap.getSquareAtIndexXZ(mobX, mobZ - 1).getType() == MapObjectType.WALL;
        boolean bottomWall = gameMap.getSquareAtIndexXZ(mobX, mobZ + 1).getType() == MapObjectType.WALL;
        // Case 1: LEFT & RIGHT
        if (!topWall && !bottomWall && leftWall && rightWall) {
            return WallAlignmentStatus.CASE1_LEFT_RIGHT;
        }
        // Case 2: TOP & BOTTOM
        if (topWall && bottomWall && !leftWall && !rightWall) {
            return WallAlignmentStatus.CASE2_TOP_BOTTOM;
        }
        // Case 3: BOTTOM & LEFT
        if (!topWall && bottomWall && leftWall && !rightWall) {
            return WallAlignmentStatus.CASE3_BOTTOM_LEFT;
        }
        // Case 4: TOP & LEFT
        if (topWall && !bottomWall && leftWall && !rightWall) {
            return WallAlignmentStatus.CASE4_TOP_LEFT;
        }
        // Case 5: TOP & RIGHT
        if (topWall && !bottomWall && !leftWall && rightWall) {
            return WallAlignmentStatus.CASE5_TOP_RIGHT;
        }
        // Case 6: BOTTOM & RIGHT
        if (!topWall && bottomWall && !leftWall && rightWall) {
            return WallAlignmentStatus.CASE6_BOTTOM_RIGHT;
        }
        // Case 7: BOTTOM & LEFT & RIGHT
        if (!topWall && bottomWall && leftWall && rightWall) {
            return WallAlignmentStatus.CASE7_BOTTOM_LEFT_RIGHT;
        }
        // Case 8: TOP & BOTTOM & LEFT
        if (topWall && bottomWall && leftWall && !rightWall) {
            return WallAlignmentStatus.CASE8_TOP_BOTTOM_LEFT;
        }
        // Case 9: TOP & LEFT & RIGHT
        if (topWall && !bottomWall && leftWall && rightWall) {
            return WallAlignmentStatus.CASE9_TOP_LEFT_RIGHT;
        }
        // Case 10: TOP & BOTTOM & RIGHT
        if (topWall && bottomWall && !leftWall && rightWall) {
            return WallAlignmentStatus.CASE10_TOP_BOTTOM_RIGHT;
        }
        // Case 11: BOTTOM
        if (!topWall && bottomWall && !leftWall && !rightWall) {
            return WallAlignmentStatus.CASE11_BOTTOM;
        }
        // Case 12: LEFT
        if (!topWall && !bottomWall && leftWall && !rightWall) {
            return WallAlignmentStatus.CASE12_LEFT;
        }
        // Case 13: TOP
        if (topWall && !bottomWall && !leftWall && !rightWall) {
            return WallAlignmentStatus.CASE13_TOP;
        }
        // Case 14: RIGHT
        if (!topWall && !bottomWall && !leftWall && rightWall) {
            return WallAlignmentStatus.CASE14_RIGHT;
        }
        // Case 0: NONE
        return WallAlignmentStatus.CASE0_NONE;
    }

    /**
     * Determines the specific section of a wall relative to the player's current position.
     * This method calculates the center of the wall square based on its boundaries and compares the player's position to the center to identify the wall section.
     * @return WallSectionStatus Enum value representing the section of the wall the player is currently in relative to the wall's center.
     */
    public WallSectionStatus getWallSection() {
        if (gameMap != null) {
            int mobX = calcMapIndexOfCoordinate(position.x);
            int mobZ = calcMapIndexOfCoordinate(position.z);
            Square square = gameMap.getSquareAtIndexXZ(mobX, mobZ);
            long idOfSquare = square.getId();
            double tempX = position.x;
            double tempZ = position.z;

            while (gameMap.getSquareAtIndexXZ(calcMapIndexOfCoordinate(tempX), calcMapIndexOfCoordinate(tempZ)).getId() == idOfSquare) {
                tempX = tempX - 1;
            }
            while (gameMap.getSquareAtIndexXZ(calcMapIndexOfCoordinate(tempX), calcMapIndexOfCoordinate(tempZ)).getId() == idOfSquare) {
                tempZ = tempZ - 1;
            }

            double wallCenterX = tempX + (GameConfig.SQUARE_SIZE / 2);
            double wallCenterZ = tempZ + (GameConfig.SQUARE_SIZE / 2);

            boolean isAboveCenter = position.z < wallCenterZ;
            boolean isLeftOfCenter = position.x < wallCenterX;

            if (isAboveCenter && isLeftOfCenter) {
                return WallSectionStatus.CASE1_TOP_LEFT;
            } else if (isAboveCenter && !isLeftOfCenter) {
                return WallSectionStatus.CASE2_TOP_RIGHT;
            } else if (!isAboveCenter && isLeftOfCenter) {
                return WallSectionStatus.CASE3_BOTTOM_LEFT;
            } else if (!isAboveCenter && !isLeftOfCenter) {
                return WallSectionStatus.CASE4_BOTTOM_RIGHT;
            }
        }

        return WallSectionStatus.CASE0_NONE;
    }

    public void setGameMapForTest(GameMap gameMap) {
        this.gameMap = gameMap;
    }

    public void setForwardForTest(Vector3d forward) {
        this.forward = forward;
    }

    @Override
    public String toString() {
        return "Mob{" +
                "id=" + id +
                ", position=" + position +
                ", radius=" + radius +
                ", quat=" + quat +
                ", speed=" + speed +
                ", spawn=" + spawn +
                '}';
    }
}
