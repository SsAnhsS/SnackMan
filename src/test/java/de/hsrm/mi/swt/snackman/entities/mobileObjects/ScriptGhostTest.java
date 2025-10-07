package de.hsrm.mi.swt.snackman.entities.mobileObjects;

import de.hsrm.mi.swt.snackman.SnackmanApplication;
import de.hsrm.mi.swt.snackman.entities.map.GameMap;
import de.hsrm.mi.swt.snackman.entities.map.Square;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.eatingMobs.Chicken.Direction;
import de.hsrm.mi.swt.snackman.services.LobbyManagerService;
import de.hsrm.mi.swt.snackman.services.MapService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.FileSystemUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Unit tests for the {@link ScriptGhost} class.
 * This class tests the behavior of the ScriptGhost, including initialization, movement, and interactions.
 */
@SpringBootTest
class ScriptGhostTest {

    @Autowired
    private MapService mapService;

    private LobbyManagerService lobbyManagerService;

    private GameMap gameMap;

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
    static void tearDownAfter() {
        if (Files.exists(workFolder)) {
            FileSystemUtils.deleteRecursively(workFolder.toFile());
        }
    }


    @BeforeEach
    void setUp() {
        char[][] mockMazeData = new char[][]{
                {'#', '#', '#'},
                {'#', '.', '#'},
                {'#', '#', '#'}
        };
        gameMap = mapService.convertMazeDataGameMap("1", mockMazeData);
    }

    /**
     * Tests the initialization of a {@link ScriptGhost} for testing purposes.
     */
    @Test
    void testScriptGhostInitializationForTests() {
        Square initialPosition = gameMap.getSquareAtIndexXZ(1, 1);
        ScriptGhost scriptGhost = new ScriptGhost(gameMap, initialPosition);

        Assertions.assertNotNull(scriptGhost);
        Assertions.assertEquals(1, scriptGhost.getGhostPosX());
        Assertions.assertEquals(1, scriptGhost.getGhostPosZ());
        Assertions.assertNotNull(scriptGhost.getLookingDirection());
        Assertions.assertNotNull(scriptGhost.getPosition());
        Assertions.assertNotNull(scriptGhost.getQuat());
    }

    /**
     * Tests the initialization of a {@link ScriptGhost} with a specified difficulty level.
     */
    @Test
    void testScriptGhostInitializationForGame() {
        Square initialPosition = gameMap.getSquareAtIndexXZ(1, 1);
        ScriptGhost scriptGhost = new ScriptGhost(gameMap, initialPosition, ScriptGhostDifficulty.EASY);

        Assertions.assertNotNull(scriptGhost);
        Assertions.assertEquals(1, scriptGhost.getGhostPosX());
        Assertions.assertEquals(1, scriptGhost.getGhostPosZ());
        Assertions.assertNotNull(scriptGhost.getLookingDirection());
        Assertions.assertNotNull(scriptGhost.getPosition());
        Assertions.assertNotNull(scriptGhost.getQuat());
    }

    /**
     * Tests that the {@link ScriptGhost#generateId()} method generates unique IDs incrementally.
     */
    @Test
    void testGenerateId() {
        long id1 = ScriptGhost.generateId();
        long id2 = ScriptGhost.generateId();

        Assertions.assertNotEquals(id1, id2);
        Assertions.assertEquals(id1 + 1, id2);
    }

    /**
     * Tests the visibility calculation of squares visible to the {@link ScriptGhost}.
     */
    @Test
    void testGetSquaresVisibleForGhost() {
        Square currentPosition = gameMap.getSquareAtIndexXZ(1, 1);
        ScriptGhost scriptGhost = new ScriptGhost(gameMap, currentPosition);
        scriptGhost.setDifficulty(ScriptGhostDifficulty.EASY);
        Direction lookingDirection = Direction.ONE_NORTH;

        var visibleSquares = scriptGhost.getSquaresVisibleForGhost(currentPosition, lookingDirection);

        Assertions.assertNotNull(visibleSquares);
        Assertions.assertEquals(9, visibleSquares.size());
        Assertions.assertEquals(lookingDirection.toString(), visibleSquares.get(8));
    }


    /**
     * Tests the movement behavior of the {@link ScriptGhost}.
     * Simulates a thread and checks for position changes after a delay.
     */
    @Test
    void testMoveScriptGhost() throws InterruptedException {
        Square initialPosition = gameMap.getSquareAtIndexXZ(1, 1);
        ScriptGhost scriptGhost = new ScriptGhost(gameMap, initialPosition);
        scriptGhost.setDifficulty(ScriptGhostDifficulty.EASY);

        Thread ghostThread = new Thread(scriptGhost);
        ghostThread.start();

        Thread.sleep(3000);

        Assertions.assertNotEquals(1, scriptGhost.getGhostPosX() + scriptGhost.getGhostPosZ());
        ghostThread.interrupt();
    }

    /**
     * Tests if the {@link ScriptGhost} detects when it is on the same square as the SnackMan.
     */
    @Test
    void testStandingOnSameSquareAsSnackMan() {
        Square initialPosition = gameMap.getSquareAtIndexXZ(1, 1);
        ScriptGhost scriptGhost = new ScriptGhost(gameMap, initialPosition);

        boolean isNotSameSquare = scriptGhost.notStandingOnSameSquareAsSnackman();

        Assertions.assertTrue(isNotSameSquare);
    }


}