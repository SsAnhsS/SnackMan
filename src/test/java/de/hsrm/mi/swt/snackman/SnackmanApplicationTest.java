package de.hsrm.mi.swt.snackman;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.*;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.springframework.util.FileSystemUtils;

class SnackmanApplicationTest {
    private Path workFolder = Paths.get("./extensions").toAbsolutePath();
    
    @BeforeEach
    void tearDown() throws IOException {
        // Clean up the work folder if it exists
        if (Files.exists(workFolder)) {
            FileSystemUtils.deleteRecursively(workFolder.toFile());
        }
    }

    @AfterEach
    void tearDownAfter() throws IOException {
        // Clean up the work folder if it exists
        if (Files.exists(workFolder)) {
            FileSystemUtils.deleteRecursively(workFolder.toFile());
        }
    }

    @Test
    void testCheckAndCopyResourcesWhenWorkFolderDoesNotExist() {
        assertFalse(Files.exists(workFolder)); 
        
        SnackmanApplication.checkAndCopyResources();

        // Verify the work folder and subfolders are created
        assertTrue(Files.exists(workFolder.resolve("maze")));
        assertTrue(Files.exists(workFolder.resolve("ghost")));
        assertTrue(Files.exists(workFolder.resolve("chicken")));
        assertTrue(Files.exists(workFolder.resolve("map")));

        assertTrue(Files.exists(workFolder.resolve("ghost/GhostMovementSkript.py")));
        assertTrue(Files.exists(workFolder.resolve("ghost/SmartGhostMovementSkript.py")));
        assertTrue(Files.exists(workFolder.resolve("chicken/ChickenMovementSkript.py")));
        assertTrue(Files.exists(workFolder.resolve("maze/Maze.py")));
    }

    @Test
    void testCheckAndCopyResourcesWhenWorkFolderExists() throws IOException {
        Files.createDirectories(workFolder);
        Files.createDirectories(workFolder.resolve("maze"));
        Files.createDirectories(workFolder.resolve("chicken"));
        Files.createFile(workFolder.resolve("maze/file1.txt")); // Simulate pre-existing file
        Files.createFile(workFolder.resolve("chicken/file1.txt")); // Simulate pre-existing file

        SnackmanApplication.checkAndCopyResources();

        // Verify subfolders are created
        assertTrue(Files.exists(workFolder.resolve("ghost")));
        assertTrue(Files.exists(workFolder.resolve("chicken")));
        assertTrue(Files.exists(workFolder.resolve("map")));

        // Verify new files are copied without overwriting existing ones
        assertTrue(Files.exists(workFolder.resolve("ghost/GhostMovementSkript.py")));
        assertTrue(Files.exists(workFolder.resolve("ghost/SmartGhostMovementSkript.py")));
        assertTrue(Files.exists(workFolder.resolve("chicken/ChickenMovementSkript.py")));
        assertTrue(Files.exists(workFolder.resolve("chicken/file1.txt")));
        assertTrue(Files.exists(workFolder.resolve("maze/Maze.py")));
        assertTrue(Files.exists(workFolder.resolve("maze/file1.txt")));
    }
}