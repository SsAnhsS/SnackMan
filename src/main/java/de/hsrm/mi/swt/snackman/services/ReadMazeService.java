package de.hsrm.mi.swt.snackman.services;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.python.util.PythonInterpreter;
import org.springframework.stereotype.Service;

import de.hsrm.mi.swt.snackman.SnackmanApplication;

@Service
public class ReadMazeService {

    private PythonInterpreter interpreter;

    public ReadMazeService() {
        interpreter = new PythonInterpreter();
        interpreter.exec("import sys");
        URL path = SnackmanApplication.class.getProtectionDomain().getCodeSource().getLocation();
        String jarClassesPath = path.getPath().replace("nested:", "").replace("!", "");
        interpreter.exec("if './extensions/chicken' not in sys.path: sys.path.insert(0, './extensions/chicken')");
        interpreter.exec("if './extensions/ghost' not in sys.path: sys.path.insert(0, './extensions/ghost')");
        interpreter.exec("if './extensions/maze' not in sys.path: sys.path.insert(0, './extensions/maze')");
        interpreter.exec("if './extensions' not in sys.path: sys.path.insert(0, './extensions')");
        interpreter.exec("if '.' not in sys.path: sys.path.insert(0, '.')");
        interpreter.exec("if '" + jarClassesPath + "/chicken' not in sys.path: sys.path.append('" + jarClassesPath + "/chicken')");
        interpreter.exec("if '" + jarClassesPath + "/ghost' not in sys.path: sys.path.append('" + jarClassesPath + "/ghost')");
        interpreter.exec("if '" + jarClassesPath + "/maze' not in sys.path: sys.path.append('" + jarClassesPath + "/maze')");
        interpreter.exec("if '" + jarClassesPath + "/Lib' not in sys.path: sys.path.append('" + jarClassesPath + "/Lib')");
    }

    /**
     * Reads maze data from a file and converts it into a char array with [x][z]-coordinates
     *
     * @param filePath the path to the file containing the maze data
     * @return a char array representing the maze
     * @throws RuntimeException if there's an error reading the file
     */
    public char[][] readMazeFromFile(String filePath) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read the maze file.", e);
        }

        if (lines.isEmpty()) {
            throw new RuntimeException("Maze file is empty.");
        }

        int rows = lines.size();
        int cols = lines.getFirst().length();
        char[][] mazeAsCharArray = new char[rows][cols];

        for (int i = 0; i < rows; i++) {
            mazeAsCharArray[i] = lines.get(i).toCharArray();
        }

        return mazeAsCharArray;
    }

    public void generateNewMaze() {
        interpreter.exec("import Maze");
        interpreter.exec("Maze.main()");
    }

}
