package de.hsrm.mi.swt.snackman.entities.mobileObjects;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.joml.Vector3d;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

import de.hsrm.mi.swt.snackman.SnackmanApplication;
import de.hsrm.mi.swt.snackman.configuration.GameConfig;
import de.hsrm.mi.swt.snackman.entities.map.GameMap;
import de.hsrm.mi.swt.snackman.entities.map.Square;
import de.hsrm.mi.swt.snackman.entities.mapObject.MapObjectType;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.eatingMobs.SnackMan;

class MobPushMethodsTest {
    private GameMap gameMap;

    private SnackMan snackman;

    private static final Path workFolder = Paths.get("./extensions").toAbsolutePath();

    @BeforeAll
    static void fileSetUp() {
        try {
            tearDownAfter();
        } catch (Exception e) {
            System.out.println("No file to delete");
        }
        SnackmanApplication.checkAndCopyResources();
    }

    @AfterAll
    static void tearDownAfter() throws IOException {
        if (Files.exists(workFolder)) {
            FileSystemUtils.deleteRecursively(workFolder.toFile());
        }
    }
    
    @BeforeEach
    public void setup(){
        Square[][] testMap1 = { {new Square(0,0), new Square(0,1), new Square(0,2)},
                                {new Square(MapObjectType.WALL, 1,0), new Square(1,1), new Square(MapObjectType.WALL, 1,2)}, 
                                {new Square(2,0), new Square(2,1), new Square(2,2)} };
        this.gameMap = new GameMap(testMap1);
    }

    @Test
    void testPushback(){
        snackman = new SnackMan(gameMap, 10, 0.3, 1.5 * GameConfig.SQUARE_SIZE, GameConfig.SQUARE_HEIGHT, 2.5 * GameConfig.SQUARE_SIZE);
        snackman.setForwardForTest(new Vector3d(1,0,1));
        snackman.pushback();
        assertTrue(!snackman.squareUnderneathIsWall(snackman.getPosition()));
    }

    @Test
    void testRespawn(){
        snackman = new SnackMan(gameMap, 10, 0.3, 1.5 * GameConfig.SQUARE_SIZE, GameConfig.SNACKMAN_GROUND_LEVEL, 1.5 * GameConfig.SQUARE_SIZE);

        snackman.setPosY(GameConfig.SQUARE_HEIGHT);
        snackman.setPosZ(snackman.getPosition().z + GameConfig.SQUARE_SIZE);

        snackman.respawn();

        assertTrue(snackman.getPosition().x == 1.5 * GameConfig.SQUARE_SIZE);
        assertTrue(snackman.getPosition().y == GameConfig.SNACKMAN_GROUND_LEVEL);
        assertTrue(snackman.getPosition().z == 1.5 * GameConfig.SQUARE_SIZE);
    }

    @Test
    void testPushForward() {
        snackman = new SnackMan(gameMap, 10, 0.3, 1.5 * GameConfig.SQUARE_SIZE, GameConfig.SQUARE_HEIGHT, 0.5 * GameConfig.SQUARE_SIZE);
        assertTrue(snackman.squareUnderneathIsWall(snackman.getPosition()));

        snackman.push_forward();

        assertTrue(!snackman.squareUnderneathIsWall(snackman.getPosition()));
        assertTrue(snackman.getPosition().x == 1.5 * GameConfig.SQUARE_SIZE);
        assertTrue(snackman.getPosition().y == GameConfig.SNACKMAN_GROUND_LEVEL);
        assertTrue(snackman.getPosition().z > GameConfig.SQUARE_SIZE && snackman.getPosition().z < 2 * GameConfig.SQUARE_SIZE);
    }

    @Test
    void testPushBackward() {
        snackman = new SnackMan(gameMap, 10, 0.3, 1.5 * GameConfig.SQUARE_SIZE, GameConfig.SQUARE_HEIGHT, 2.5 * GameConfig.SQUARE_SIZE);
        assertTrue(snackman.squareUnderneathIsWall(snackman.getPosition()));

        snackman.push_backward();

        assertTrue(!snackman.squareUnderneathIsWall(snackman.getPosition()));
        assertTrue(snackman.getPosition().x == 1.5 * GameConfig.SQUARE_SIZE);
        assertTrue(snackman.getPosition().y == GameConfig.SNACKMAN_GROUND_LEVEL);
        assertTrue(snackman.getPosition().z > GameConfig.SQUARE_SIZE && snackman.getPosition().z < 2 * GameConfig.SQUARE_SIZE);
    }

    @Test
    void testPushLeft() {
        snackman = new SnackMan(gameMap, 10, 0.3, 1.5 * GameConfig.SQUARE_SIZE, GameConfig.SQUARE_HEIGHT, 2.5 * GameConfig.SQUARE_SIZE);
        assertTrue(snackman.squareUnderneathIsWall(snackman.getPosition()));

        snackman.push_left();

        assertTrue(!snackman.squareUnderneathIsWall(snackman.getPosition()));
        assertTrue(snackman.getPosition().x > 0 && snackman.getPosition().x < GameConfig.SQUARE_SIZE);
        assertTrue(snackman.getPosition().y == GameConfig.SNACKMAN_GROUND_LEVEL);
        assertTrue(snackman.getPosition().z == 2.5 * GameConfig.SQUARE_SIZE);
    }

    @Test
    void testPushRight() {
        snackman = new SnackMan(gameMap, 10, 0.3, 1.5 * GameConfig.SQUARE_SIZE, GameConfig.SQUARE_HEIGHT, 2.5 * GameConfig.SQUARE_SIZE);
        assertTrue(snackman.squareUnderneathIsWall(snackman.getPosition()));

        snackman.push_right();

        assertTrue(!snackman.squareUnderneathIsWall(snackman.getPosition()));
        assertTrue(snackman.getPosition().x > 2 * GameConfig.SQUARE_SIZE && snackman.getPosition().x < 3 * GameConfig.SQUARE_SIZE);
        assertTrue(snackman.getPosition().y == GameConfig.SNACKMAN_GROUND_LEVEL);
        assertTrue(snackman.getPosition().z == 2.5 * GameConfig.SQUARE_SIZE);
    }

    @Test
    void testOffMaze(){
        snackman = new SnackMan(gameMap, 10, 0.3, 1.5 * GameConfig.SQUARE_SIZE, GameConfig.SQUARE_HEIGHT, 1.5 * GameConfig.SQUARE_SIZE);

        snackman.setPosX(1.5 * GameConfig.SQUARE_SIZE);
        snackman.setPosY(0.5 * GameConfig.SNACKMAN_GROUND_LEVEL);
        snackman.setPosZ(3.5 * GameConfig.SQUARE_SIZE);

        snackman.setIsJumping(true);
        double deltaTime = 0.016;
        snackman.updateJumpPosition(deltaTime);

        assertTrue(snackman.squareUnderneathIsFloor(snackman.getPosition()));
        assertTrue(snackman.getPosition().x == 1.5 * GameConfig.SQUARE_SIZE);
        assertTrue(snackman.getPosition().y == GameConfig.SNACKMAN_GROUND_LEVEL);
        assertTrue(snackman.getPosition().z == 1.5 * GameConfig.SQUARE_SIZE);
    }
}
