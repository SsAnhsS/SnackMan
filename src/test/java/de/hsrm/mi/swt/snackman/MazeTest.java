package de.hsrm.mi.swt.snackman;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.FileSystemUtils;

import de.hsrm.mi.swt.snackman.services.ReadMazeService;

@SpringBootTest
class MazeTest {
    
    @Autowired
    ReadMazeService mazeService;

    private static final String MAZE_FILE_PATH = "./extensions/map/Maze.txt";
    private static final Path workFolder = Paths.get("./extensions").toAbsolutePath();

    @BeforeAll
    static void fileSetUp() {
        try{
            tearDownAfter();  
        }catch(Exception e){
            System.out.println("No file to delete");
        }   
        SnackmanApplication.checkAndCopyResources();
        assert Files.exists(workFolder.resolve("maze"));
        assert Files.exists(workFolder.resolve("map"));
    }

    @AfterAll
    static void tearDownAfter() throws IOException {
        if (Files.exists(workFolder)) {
            FileSystemUtils.deleteRecursively(workFolder.toFile());
        }
    }

    @Test
    void mazeExists() {
        mazeService.generateNewMaze();
        Path filePath = Paths.get(MAZE_FILE_PATH);
        Assertions.assertTrue(Files.exists(filePath), "The Maze.txt file does not exist!");
    }

    @Test
    void mazeHasContent() {
        mazeService.generateNewMaze();
        List<String> maze = readMazeFile();

        Assertions.assertFalse(maze.isEmpty(), "The Maze.txt file is empty!");
    }

    @Test 
    void mazeHasDefinedCharacters() {
        mazeService.generateNewMaze();
        List<String> maze = readMazeFile();

        for (int i = 0; i < maze.size(); i++) {
            String line = maze.get(i);
            for (char c : line.toCharArray()) {
                Assertions.assertTrue(
                    c == ' ' || c == '#' || c == 'o' || c == '0' || c == 'S' || c == 'G' || c == 'C',
                    "Invalid character: '" + c + "' in maze at line " + i
                );
            }
        }
    }

    // Helper method to read the Maze.txt file
    private List<String> readMazeFile() {
        try {
            return Files.readAllLines(Paths.get(MAZE_FILE_PATH));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Maze.txt", e);
        }
    }
}