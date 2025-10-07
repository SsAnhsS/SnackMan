package de.hsrm.mi.swt.snackman.entities.mobileObjects;

import de.hsrm.mi.swt.snackman.SnackmanApplication;
import de.hsrm.mi.swt.snackman.entities.map.GameMap;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the 'GhostMovementSkript.py' logic accessed by Jython.
 * This class validates specific movement scenarios for the ScriptGhost.
 */
public class ScriptGhostMovementTest {

    @Mock
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
    static void tearDownAfter() throws IOException {
        if (Files.exists(workFolder)) {
            FileSystemUtils.deleteRecursively(workFolder.toFile());
        }
    }

    /**
     * Tests whether the ghost moves to an empty field (' ’) if one is available.
     */
    @Test
    public void testGhostMovesToEmptySquare() {
        ScriptGhost ghost = new ScriptGhost();
        ghost.initJython();

        List<String> visibleEnvironment = List.of("L", "W", "L", "W", "L", "L", "L", "L", "0");

        int result = ghost.executeMovementSkript(visibleEnvironment);

        assertTrue(List.of(2, 3).contains(result));
    }

    /**
     * Tests whether the ghost moves to the square where the SnackMan (‘M’) is located.
     * The ghost should prioritise the SnackMan and direct its movement accordingly.
     */
    @Test
    public void testGhostMovesToSnackMan() {
        ScriptGhost ghost = new ScriptGhost();
        ghost.initJython();

        List<String> visibleEnvironment = List.of("L", "L", "L", "M", "L", "L", "L", "L", "0");
        int result = ghost.executeMovementSkript(visibleEnvironment);

        assertEquals(1, result);
    }

    /**
     * Tests whether the ghost is moved in the direction of a chicken (‘C’) when it sees it in its environment.
     * In this case, the script should prioritise movement in the direction of the chicken,
     * since no SnackMan is visible.
     */
    @Test
    public void testGhostMovesTowardsChicken() {
        ScriptGhost ghost = new ScriptGhost();
        ghost.initJython();

        List<String> visibleEnvironment = List.of("L", "C", "L", "W", "L", "L", "W", "L", "0");
        int result = ghost.executeMovementSkript(visibleEnvironment);

        assertEquals(0, result);
    }

    /**
     * Tests whether the ghost continues in its previous direction of movement, if this is free, as it sees neither a
     * SnackMan or a chicken.
     * The ghost moves in the direction of the last saved direction of movement.
     */
    @Test
    public void testGhostMovesIntoWalkingDirection() {
        ScriptGhost ghost = new ScriptGhost();
        ghost.initJython();

        List<String> visibleEnvironment = List.of("L", "L", "L", "W", "L", "L", "W", "L", "3");
        int result = ghost.executeMovementSkript(visibleEnvironment);

        assertEquals(3, result);
    }

    /**
     * Tests whether the ghost randomly chooses a direction if several directions are available.
     * The script should randomly select one of the available movements if there are several options.
     */
    @Test
    public void testGhostMovesIntoRandomDirection() {
        ScriptGhost ghost = new ScriptGhost();
        ghost.initJython();

        List<String> visibleEnvironment = List.of("L", "L", "L", "W", "L", "L", "W", "L", "1");
        int result = ghost.executeMovementSkript(visibleEnvironment);

        assertTrue(List.of(0, 2, 3).contains(result));
    }

}