package de.hsrm.mi.swt.snackman.entities.mobileObjects;

import de.hsrm.mi.swt.snackman.entities.map.GameMap;
import de.hsrm.mi.swt.snackman.services.MapService;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.hsrm.mi.swt.snackman.configuration.GameConfig;
import de.hsrm.mi.swt.snackman.entities.map.Square;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.eatingMobs.SnackMan;
import org.mockito.Mock;

class DefaultMovementTest {
    
    private SnackMan snackman;

    private GameMap gameMap;

    @BeforeEach
    public void setup(){
        Square[][] emptyMap = { {new Square(0,0), new Square(0,1), new Square(0,2)},
                                {new Square(1,0), new Square(1,1), new Square(1,2)}, 
                                {new Square(2,0), new Square(2,1), new Square(2,2)} };
        this.gameMap = new GameMap(emptyMap);

        snackman = new SnackMan(gameMap, 10, 0.3, 1.5 * GameConfig.SQUARE_SIZE,2 ,1.5 * GameConfig.SQUARE_SIZE);
    }


    @Test
    // Test if x/z map square index is correctly calculated
    void calculateMapSquareIndexFromPos(){
        double posX = 2.7813;
        assertEquals(2/GameConfig.SQUARE_SIZE, snackman.calcMapIndexOfCoordinate(posX));
    }



    @Test
    // Test if Snackman is moving forward (no rotation) when forward is true
    void moveForwardStandard(){
        double startPosX = snackman.getPosX();
        double startPosY = snackman.getPosY();
        double startPosZ = snackman.getPosZ();
        snackman.move(true, false, false, false, 0.1, this.gameMap);
        assertTrue(startPosX == snackman.getPosX());
        assertTrue(startPosY == snackman.getPosY());
        assertTrue(startPosZ > snackman.getPosZ());
    }

    @Test
    // Test if Snackman is moving backward (no rotation) when forward is true
    void moveBackwardStandard(){
        double startPosX = snackman.getPosX();
        double startPosY = snackman.getPosY();
        double startPosZ = snackman.getPosZ();
        snackman.move(false, true, false, false, 0.1, this.gameMap);
        assertTrue(startPosX == snackman.getPosX());
        assertTrue(startPosY == snackman.getPosY());
        assertTrue(startPosZ < snackman.getPosZ());
    }

    @Test
    // Test if Snackman is moving left(no rotation) when forward is true
    void moveLeftStandard(){
        double startPosX = snackman.getPosX();
        double startPosY = snackman.getPosY();
        double startPosZ = snackman.getPosZ();
        snackman.move(false, false, true, false, 0.1, this.gameMap);
        assertTrue(startPosX > snackman.getPosX());
        assertTrue(startPosY == snackman.getPosY());
        assertTrue(startPosZ == snackman.getPosZ());
    }

    @Test
    // Test if Snackman is moving right (no rotation) when forward is true
    void moveRightStandard(){
        double startPosX = snackman.getPosX();
        double startPosY = snackman.getPosY();
        double startPosZ = snackman.getPosZ();
        snackman.move(false, false, false, true, 0.1, this.gameMap);
        assertTrue(startPosX < snackman.getPosX());
        assertTrue(startPosY == snackman.getPosY());
        assertTrue(startPosZ == snackman.getPosZ());
    }

    @Test
    // Test if Snackman Forward/Backward movement is correct after rotating
    void rotateThenMoveNewForward(){
        double startPosX = snackman.getPosX();
        double startPosY = snackman.getPosY();
        double startPosZ = snackman.getPosZ();
        Quaterniond rotation = new Quaterniond();
        rotation.rotateAxis(Math.toRadians(90) ,new Vector3d(0,1,0));
        snackman.setQuaternion(rotation.x,rotation.y, rotation.z, rotation.w);
        snackman.move(true, false, false, false, 0.1, this.gameMap);
        assertTrue(startPosX > snackman.getPosX());
        assertTrue(startPosY == snackman.getPosY());
        assertTrue(startPosZ == snackman.getPosZ());
    }

    @Test
    // Test if Snackman Left/Right movement is correct after rotating
    void rotateThenMoveNewLeft(){
        double startPosX = snackman.getPosX();
        double startPosY = snackman.getPosY();
        double startPosZ = snackman.getPosZ();
        Quaterniond rotation = new Quaterniond();
        rotation.rotateAxis(Math.toRadians(90) ,new Vector3d(0,1,0));
        snackman.setQuaternion(rotation.x,rotation.y, rotation.z, rotation.w);
        snackman.move(false, false, true, false, 0.1, this.gameMap);
        assertTrue(startPosX == snackman.getPosX());
        assertTrue(startPosY == snackman.getPosY());
        assertTrue(startPosZ < snackman.getPosZ());
    }

}
