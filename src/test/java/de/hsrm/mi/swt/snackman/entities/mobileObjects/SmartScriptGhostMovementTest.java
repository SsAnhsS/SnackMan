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
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration tests for Ghost movement logic with Jython and Java integration.
 * This class ensures that the 'Ghost' Java class correctly interacts with
 * the 'SmartGhostMovementSkript.py' Python logic.
 */
public class SmartScriptGhostMovementTest {

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
     * Tests if the ghost moves north as determined by the Python script.
     *
     * Simulates a maze configuration where moving north is the optimal move.
     */
    @Test
    public void testGhostWalkNorth() {
        ScriptGhost scriptGhost = new ScriptGhost(null, ScriptGhostDifficulty.DIFFICULT);

        List<List<String>> lab = Arrays.asList(
                Arrays.asList("W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W"),
                Arrays.asList("W", "M", "L", "L", "W", "L", "L", "L", "L", "L", "L", "L", "L", "L", "W"),
                Arrays.asList("W", "G", "W", "L", "W", "W", "W", "W", "W", "W", "W", "W", "L", "W", "W"),
                Arrays.asList("W", "L", "L", "L", "W", "L", "L", "L", "W", "L", "L", "L", "L", "L", "W"),
                Arrays.asList("W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W")
        );

        int result = scriptGhost.executeMovementSkriptDifficult(lab);

        assertEquals(0, result,
                "The Python script should correctly determine the next move which is north.");
    }

    /**
     * Tests if the ghost moves east as determined by the Python script.
     *
     * Simulates a maze configuration where moving east is the optimal move.
     */
    @Test
    public void testGhostWalkEast() {
        ScriptGhost scriptGhost = new ScriptGhost(null, ScriptGhostDifficulty.DIFFICULT);

        List<List<String>> lab = Arrays.asList(
                Arrays.asList("W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W"),
                Arrays.asList("W", "G", "M", "L", "W", "L", "L", "L", "L", "L", "L", "L", "L", "L", "W"),
                Arrays.asList("W", "L", "W", "L", "W", "W", "W", "W", "W", "W", "W", "W", "L", "W", "W"),
                Arrays.asList("W", "L", "L", "L", "W", "L", "L", "L", "W", "L", "L", "L", "L", "L", "W"),
                Arrays.asList("W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W")
        );

        int result = scriptGhost.executeMovementSkriptDifficult(lab);

        assertEquals(1, result,
                "The Python script should correctly determine the next move which is east.");
    }

    /**
     * Tests if the ghost moves south as determined by the Python script.
     *
     * Simulates a maze configuration where moving south is the optimal move.
     */
    @Test
    public void testGhostWalkSouth() {
        ScriptGhost scriptGhost = new ScriptGhost(null, ScriptGhostDifficulty.DIFFICULT);

        List<List<String>> lab = Arrays.asList(
                Arrays.asList("W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W"),
                Arrays.asList("W", "G", "L", "L", "W", "L", "L", "L", "L", "L", "L", "L", "L", "L", "W"),
                Arrays.asList("W", "M", "W", "L", "W", "W", "W", "W", "W", "W", "W", "W", "L", "W", "W"),
                Arrays.asList("W", "L", "L", "L", "W", "L", "L", "L", "W", "L", "L", "L", "L", "L", "W"),
                Arrays.asList("W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W")
        );

        int result = scriptGhost.executeMovementSkriptDifficult(lab);

        assertEquals(2, result,
                "The Python script should correctly determine the next move which is south.");
    }

    /**
     * Tests if the ghost moves west as determined by the Python script.
     *
     * Simulates a maze configuration where moving west is the optimal move.
     */
    @Test
    public void testGhostWalkWest() {
        ScriptGhost scriptGhost = new ScriptGhost(null, ScriptGhostDifficulty.DIFFICULT);

        List<List<String>> lab = Arrays.asList(
                Arrays.asList("W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W"),
                Arrays.asList("W", "M", "G", "L", "W", "L", "L", "L", "L", "L", "L", "L", "L", "L", "W"),
                Arrays.asList("W", "L", "W", "L", "W", "W", "W", "W", "W", "W", "W", "W", "L", "W", "W"),
                Arrays.asList("W", "L", "L", "L", "W", "L", "L", "L", "W", "L", "L", "L", "L", "L", "W"),
                Arrays.asList("W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W", "W")
        );

        int result = scriptGhost.executeMovementSkriptDifficult(lab);

        assertEquals(3, result,
                "The Python script should correctly determine the next move which is west.");
    }

}