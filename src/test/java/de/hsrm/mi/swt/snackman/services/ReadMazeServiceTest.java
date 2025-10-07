package de.hsrm.mi.swt.snackman.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

import de.hsrm.mi.swt.snackman.SnackmanApplication;

class ReadMazeServiceTest {

    private ReadMazeService readMazeService;
    private static final Path workFolder = Paths.get("./extensions").toAbsolutePath();

    @BeforeAll
    static void fileSetUp() {
        try{
            tearDownAfter();  
        }catch(Exception e){
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
    void setUp() {
        readMazeService = new ReadMazeService();
    }

    @Test
    void testReadMazeFromFile_success() throws IOException {
        File mazeFile = new File("test_maze.txt");
        FileWriter writer = new FileWriter(mazeFile);
        writer.write("####\n");
        writer.write("#  #\n");
        writer.write("####\n");
        writer.close();

        ReadMazeService readMazeService = new ReadMazeService();

        char[][] maze = readMazeService.readMazeFromFile("test_maze.txt");

        //Check if file is read right
        assertNotNull(maze, "Maze should not be null");
        assertEquals(3, maze.length, "Maze should have three rows");
        assertArrayEquals(new char[]{'#', '#', '#', '#'}, maze[0], "The first row should be correct");
        assertArrayEquals(new char[]{'#', ' ', ' ', '#'}, maze[1], "The second row should be correct");
        assertArrayEquals(new char[]{'#', '#', '#', '#'}, maze[2], "The third row should be correct");

        // Delete file after testing
        mazeFile.delete();
    }

    @Test
    void testReadMazeFromFile_fileNotFound() {
        Exception exception = assertThrows(RuntimeException.class,
                () -> readMazeService.readMazeFromFile("does_not_exist.txt"));

        assertTrue(exception.getMessage().contains("Failed to read the maze file"),
                "The error message should indicate that the file could not be read");
    }

    @Test
    void testReadMazeFromFile_emptyFile() throws IOException {
        File mazeFile = new File("test_maze.txt");
        FileWriter writer = new FileWriter(mazeFile);
        writer.close();

        ReadMazeService readMazeService = new ReadMazeService();

        Exception exception = assertThrows(RuntimeException.class,
                () -> readMazeService.readMazeFromFile("test_maze.txt"));

        assertTrue(exception.getMessage().contains("Maze file is empty."),
                "The error message should indicate that the file has no content");

        // Delete file after testing
        mazeFile.delete();
    }
}
