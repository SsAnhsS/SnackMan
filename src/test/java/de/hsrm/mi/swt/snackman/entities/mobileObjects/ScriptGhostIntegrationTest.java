package de.hsrm.mi.swt.snackman.entities.mobileObjects;

import de.hsrm.mi.swt.snackman.SnackmanApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.python.util.PythonInterpreter;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration tests for Ghost movement logic with Jython and Java integration.
 * This class ensures that the 'ScriptGhost' Java class correctly interacts with
 * the 'GhostMovementSkript.py' Python logic.
 */
public class ScriptGhostIntegrationTest {

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
     * using a Jython interpreter.
     */
    @Test
    void testScriptGhostMovement() {
        try (PythonInterpreter pyInterp = new PythonInterpreter()) {
            pyInterp.exec("import sys");
            pyInterp.exec("sys.path.append('./extensions/ghost/')");

            String mapAroundScriptGhost = "'L', 'L', 'W', 'W', 'W', 'W', 'W', 'W', '0'";
            pyInterp.exec("from GhostMovementSkript import choose_next_square");
            pyInterp.exec("result = choose_next_square([" + mapAroundScriptGhost + "])");

            String result = pyInterp.get("result").toString();

            assertEquals("0", result, "The Python script should correctly determine the next move (' ').");
        }
    }
}