package de.hsrm.mi.swt.snackman.entities.mobileObjects;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hsrm.mi.swt.snackman.SnackmanApplication;
import de.hsrm.mi.swt.snackman.configuration.GameConfig;
import de.hsrm.mi.swt.snackman.entities.map.GameMap;
import de.hsrm.mi.swt.snackman.entities.map.Square;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.eatingMobs.Chicken.Chicken;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.eatingMobs.Chicken.Direction;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.eatingMobs.SnackMan;

/**
 * The ScriptGhost represents a ghost entity in the game that
 * moves autonomously by executing Python scripts. It extends the Mob class
 * and implements Runnable to handle threaded movement logic.
 * Based on the visible squares around its current position, the script decides
 * the next move for the ghost.
 */
public class ScriptGhost extends Mob implements Runnable {

    private static long idCounter = 0;
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private final Logger log = LoggerFactory.getLogger(ScriptGhost.class);
    private final int WAITING_TIME = 2000;  // in ms
    private long id;
    private Direction lookingDirection;
    private boolean isWalking;
    private int ghostPosX, ghostPosZ;
    // python
    private PythonInterpreter pythonInterpreter = null;
    private ScriptGhostDifficulty difficulty;
    private GameMap gameMap;

    public ScriptGhost() {
        super();
        this.difficulty = ScriptGhostDifficulty.EASY;
        this.lookingDirection = Direction.getRandomDirection();
        initJython();
    }

    public ScriptGhost(GameMap gameMap, ScriptGhostDifficulty difficulty) {
        super();
        this.gameMap = gameMap;
        this.difficulty = difficulty;
        this.lookingDirection = Direction.getRandomDirection();
        initJython();
    }

    public ScriptGhost(GameMap gameMap, Square initialPosition, ScriptGhostDifficulty difficulty) {
        this(gameMap, initialPosition);
        this.difficulty = difficulty;
    }

    public ScriptGhost(GameMap gameMap, Square initialPosition) {
        super();
        this.difficulty = ScriptGhostDifficulty.EASY;
        this.gameMap = gameMap;
        id = generateId();
        this.ghostPosX = initialPosition.getIndexX();
        this.ghostPosZ = initialPosition.getIndexZ();
        initialPosition.addMob(this);
        this.isWalking = true;
        this.lookingDirection = Direction.getRandomDirection();
        initJython();
    }

    /**
     * Method to generate the next id of a new ScriptGhost. It is synchronized because of thread-safety.
     *
     * @return the next incremented id
     */
    protected synchronized static long generateId() {
        return idCounter++;
    }

    /**
     * @param currentPosition  the square the ghost is standing on top of
     * @param lookingDirection
     * @return a list of 8 square which are around the current square + the
     * direction the ghost is looking in the order:
     * northwest_square, north_square, northeast_square, east_square,
     * southeast_square, south_square, southwest_square, west_square,
     * direction
     */
    public synchronized List<String> getSquaresVisibleForGhost(Square currentPosition, Direction lookingDirection) {
        List<String> squares = new ArrayList<>();
        squares.add(Direction.ONE_NORTH_ONE_WEST.get_one_North_one_West_Square(this.gameMap, currentPosition).getPrimaryTypeForGhost());
        squares.add(Direction.ONE_NORTH.get_one_North_Square(this.gameMap, currentPosition).getPrimaryTypeForGhost());
        squares.add(Direction.ONE_NORTH_ONE_EAST.get_one_North_one_East_Square(this.gameMap, currentPosition).getPrimaryTypeForGhost());
        squares.add(Direction.ONE_EAST.get_one_East_Square(this.gameMap, currentPosition).getPrimaryTypeForGhost());
        squares.add(Direction.ONE_SOUTH_TWO_EAST.get_one_South_one_East_Square(this.gameMap, currentPosition).getPrimaryTypeForGhost());
        squares.add(Direction.ONE_SOUTH.get_one_South_Square(this.gameMap, currentPosition).getPrimaryTypeForGhost());
        squares.add(Direction.ONE_SOUTH_ONE_WEST.get_one_South_one_West_Square(this.gameMap, currentPosition).getPrimaryTypeForGhost());
        squares.add(Direction.ONE_WEST.get_one_West_Square(this.gameMap, currentPosition).getPrimaryTypeForGhost());
        squares.add(lookingDirection.toString());
        return squares;
    }

    /**
     * Adds a {@link PropertyChangeListener} to this object.
     * The listener will be notified whenever a bound property changes.
     *
     * @param listener the {@link PropertyChangeListener} to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Contains the movement logic for the ghost. The ghost calculates its next
     * moves and updates its position.
     */
    protected void move() {
        while (isWalking) {
            // get 9 squares
            Square currentPosition = this.gameMap.getSquareAtIndexXZ(this.ghostPosX, this.ghostPosZ);
            List<String> squares = getSquaresVisibleForGhost(currentPosition, lookingDirection);
            log.debug("Squares ghost is seeing: {}", squares);
            log.debug("Current position is x {} z {}", this.ghostPosX, this.ghostPosZ);

            if (notStandingOnSameSquareAsSnackman()) {
                int newMove = 0;
                if (this.difficulty == ScriptGhostDifficulty.EASY) {
                    newMove = executeMovementSkript(squares);
                } else {
                    List<List<String>> pythonList = new ArrayList<>();
                    for (String[] row : getStringMap()) {
                        pythonList.add(Arrays.asList(row));
                    }
                    newMove = executeMovementSkriptDifficult(pythonList);
                }
                setNewPosition(newMove);
                log.debug("New position is x {} z {}", this.ghostPosX, this.ghostPosZ);
            }

        }
    }

    /**
     * @return an array of strings representing the game map
     */
    public String[][] getStringMap() {
        int rows = this.gameMap.getGameMapSquares().length;
        int cols = this.gameMap.getGameMapSquares()[0].length;
        String[][] result = new String[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = this.gameMap.getGameMapSquares()[i][j].getPrimaryTypeForGhostWithHighDifficulty(this.id);
            }
        }
        result[this.ghostPosX][this.ghostPosZ] = "G";

        return result;
    }

    /**
     * @return true if the ghost is not standing on the same square as snackman
     */
    protected boolean notStandingOnSameSquareAsSnackman() {
        return this.gameMap.getSquareAtIndexXZ(this.ghostPosX, this.ghostPosZ).getMobs().stream().noneMatch(mob -> mob instanceof SnackMan);
    }

    /**
     * Executes the ghost's movement script written in Python and determines the
     * next move.
     *
     * @param squares a list of squares visible from the ghost's current position.
     * @return the index of the next move resulting from the Python script's execution.
     */
    public int executeMovementSkript(List<String> squares) {
        try {
            log.debug("Running python ghost script with: {}", squares.toString());
            pythonInterpreter.exec(GameConfig.GHOST_SCRIPT_HARD + ".choose_next_square");
            PyObject result = pythonInterpreter.eval(GameConfig.GHOST_SCRIPT_EASY + ".choose_next_square(" + convertToPyStringList(squares) + ")");
            // PyObject result = func.__call__(new PyList(squares));

            return Integer.parseInt(result.toString());
        } catch (Exception ex) {
            log.error("Error while executing ghost python script: ", ex);
            ex.printStackTrace();
            return 0;
        }
    }

    private PyList convertToPyStringList(List<String> list) {
        PyList pyList = new PyList();
        for (String e : list) {
            pyList.append(new PyString(e));
        }
        return pyList;
    }


    /**
     * Executes the ghost's movement script written in Python and determines the
     * next move.
     *
     * @param pythonList a list of squares visible from the ghost's current position.
     * @return the index of the next move resulting from the Python script's execution.
     */
    public int executeMovementSkriptDifficult(List<List<String>> pythonList) {
        PyList pyListList = new PyList();
        for (List<String> list : pythonList) {
            pyListList.append(convertToPyStringList(list));
        }
        try {
            log.debug("Running python ghost script with: {}", pythonList.toString());
            PyObject result = pythonInterpreter.eval(GameConfig.GHOST_SCRIPT_HARD + ".choose_next_square(" + pyListList + ")");
            // PyObject result = func.__call__(new PyList(pythonList));

            return Integer.parseInt(result.toString());
        } catch (Exception ex) {
            log.error("Error while executing difficult ghost python script: ", ex);
            ex.printStackTrace();
            return 0;
        }
    }

    /**
     * Initializes Jython for executing the chicken's movement script.
     * Sets up the required Python environment and interpreter.
     */
    public void initJython() {
        this.pythonInterpreter = new PythonInterpreter();
        pythonInterpreter.exec("import sys");
        URL path = SnackmanApplication.class.getProtectionDomain().getCodeSource().getLocation();
        String jarClassesPath = path.getPath().replace("nested:", "").replace("!", "");
        pythonInterpreter.exec("if './extensions/chicken' not in sys.path: sys.path.insert(0, './extensions/chicken')");
        pythonInterpreter.exec("if './extensions/ghost' not in sys.path: sys.path.insert(0, './extensions/ghost')");
        pythonInterpreter.exec("if './extensions/maze' not in sys.path: sys.path.insert(0, './extensions/maze')");
        pythonInterpreter.exec("if './extensions' not in sys.path: sys.path.insert(0, './extensions')");
        pythonInterpreter.exec("if '.' not in sys.path: sys.path.insert(0, '.')");
        pythonInterpreter.exec("if '" + jarClassesPath + "/chicken' not in sys.path: sys.path.append('" + jarClassesPath + "/chicken')");
        pythonInterpreter.exec("if '" + jarClassesPath + "/ghost' not in sys.path: sys.path.append('" + jarClassesPath + "/ghost')");
        pythonInterpreter.exec("if '" + jarClassesPath + "/maze' not in sys.path: sys.path.append('" + jarClassesPath + "/maze')");
        pythonInterpreter.exec("if '" + jarClassesPath + "/Lib' not in sys.path: sys.path.append('" + jarClassesPath + "/Lib')");

        this.pythonInterpreter.exec("import " + GameConfig.GHOST_SCRIPT_EASY);
        this.pythonInterpreter.exec("import " + GameConfig.GHOST_SCRIPT_HARD);
    }

    /**
     * Updates the ghost's position based on the chosen move and sets its new
     * direction.
     *
     * @param newMove a list representing the next move for the ghost.
     */
    private void setNewPosition(int newMove) {
        //get positions
        Direction walkingDirection = Direction.getDirection(newMove);
        this.lookingDirection = walkingDirection;
        Square oldPosition = this.gameMap.getSquareAtIndexXZ(this.ghostPosX, this.ghostPosZ);
        Square newPosition = walkingDirection.getNewPosition(this.gameMap, this.ghostPosX, this.ghostPosZ, walkingDirection);

        try {
            Thread.sleep(WAITING_TIME / 2);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }

        propertyChangeSupport.firePropertyChange("scriptGhost", null, this);

        try {
            Thread.sleep(WAITING_TIME / 2);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }

        // set new position
        this.ghostPosX = newPosition.getIndexX();
        this.ghostPosZ = newPosition.getIndexZ();
        this.setPosX(newPosition.getIndexX());
        this.setPosZ(newPosition.getIndexZ());
        oldPosition.removeMob(this);
        newPosition.addMob(this);
        scaresEverythingThatCouldBeEncountered(newPosition, gameMap);
        propertyChangeSupport.firePropertyChange("scriptGhost", null, this);
    }

    /**
     * when moving, the ghost scares everything that gets in its way
     *
     * @param currentPosition current position
     * @param gameMap         gamemap
     */
    private synchronized void scaresEverythingThatCouldBeEncountered(Square currentPosition, GameMap gameMap) {
        for (Mob mob : gameMap.getSquareAtIndexXZ(currentPosition.getIndexX(), currentPosition.getIndexZ()).getMobs()) {
            switch (mob) {
                case Chicken chicken:
                    chicken.isScaredFromGhost(true);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * starts a thread for moving the ghost
     */
    @Override
    public void run() {
        try {
            Thread.sleep(WAITING_TIME);
            move();
            log.debug("Stopping script ghost with id {}", id);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public long getId() {
        return id;
    }

    public int getGhostPosX() {
        return ghostPosX;
    }

    public int getGhostPosZ() {
        return ghostPosZ;
    }

    public Direction getLookingDirection() {
        return lookingDirection;
    }

    public void setDifficulty(ScriptGhostDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public String toString() {
        return "ScriptGhost{" +
                "id=" + id +
                ", lookingDirection=" + lookingDirection +
                ", ghostPosX=" + ghostPosX +
                ", ghostPosZ=" + ghostPosZ +
                ", difficulty=" + difficulty +
                ", id=" + id +
                '}';
    }

    public void setWalking(boolean walking) {
        isWalking = walking;
    }
}
