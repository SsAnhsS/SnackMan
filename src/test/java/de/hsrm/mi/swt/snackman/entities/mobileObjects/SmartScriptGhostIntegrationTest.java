package de.hsrm.mi.swt.snackman.entities.mobileObjects;

import de.hsrm.mi.swt.snackman.SnackmanApplication;
import de.hsrm.mi.swt.snackman.entities.map.GameMap;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.python.util.PythonInterpreter;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for ScriptGhost movement logic with Jython and Java integration.
 * This class ensures that the 'ScriptGhost' Java class correctly interacts with
 * the 'SmartGhostMovementSkript.py' Python logic.
 */
public class SmartScriptGhostIntegrationTest {

    @Mock
    private GameMap gameMap;
    private ScriptGhost scriptGhost;

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
     * Verifies that the Script Ghost can interact with the Python script directly,
     * using a Jython interpreter and returns a result.
     */
    @Test
    void testGhostMovement() {
        try (PythonInterpreter pyInterp = new PythonInterpreter()) {
            pyInterp.exec("import sys");
            pyInterp.exec("sys.path.append('./extensions/ghost/')");

            String mapAroundGhost = "'L', 'M', 'L', 'G', 'L', 'L', 'L', 'L', '0'";
            pyInterp.exec("from SmartGhostMovementSkript import choose_next_square");
            pyInterp.exec("result = choose_next_square([" + mapAroundGhost + "])");

            int result = Integer.parseInt(pyInterp.get("result").toString());

            assertEquals(0, result,
                    "The Python script should correctly determine the next move (' ').");
        }
    }
}