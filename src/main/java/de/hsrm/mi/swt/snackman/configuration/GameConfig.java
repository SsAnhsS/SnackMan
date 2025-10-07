package de.hsrm.mi.swt.snackman.configuration;

public class GameConfig {

    //MAP
    public static final int SQUARE_SIZE = 2;
    public static final int SQUARE_HEIGHT = 5;

    // SNACKMAN
    public static final int SNACKMAN_GROUND_LEVEL = 2;
    public static final double SNACKMAN_RADIUS = 0.3;
    public static final double SNACKMAN_SPEED = 6.5;
    public static final double SNACKMAN_SPRINT_MULTIPLIER = 1.5;
    public static final int SNACKMAN_MAX_CALORIES = 20000;
    public static final int SNACKMAN_START_CALORIES = 2500;
    public static final int INVINCIBILITY_TIME = 1000;

    //SNACKMAN JUMPING PARAM
    public static final double JUMP_STRENGTH = 8.5;
    public static final double DOUBLEJUMP_STRENGTH = JUMP_STRENGTH * 0.15;
    public static final double GRAVITY = -27;
    public static final double GRAVITY_AFTER_TIMEOUT = -100;
    public static final int SINGLE_JUMP_CALORIE_COSTS = 500;
    public static final int DOUBLE_JUMP_CALORIE_COSTS = 500;

    //SNACK SPAWN
    public static final int TIME_FOR_SNACKS_TO_RESPAWN = 10000;
    public static final double SNACK_SPAWN_RATE = 0.1;

    //Chicken
    public static final String[] DEFAULT_CHICKEN_SCRIPTS = {"ChickenMovementSkript", "SonicChickenMovementSkript", "TalaChickenMovementSkript"};
    public static final int MAX_KALORIEN = 3000;
    public static final String MAX_KALORIEN_MESSAGE = "Maximum calories reached!";

    //GHOST
    public static final String GHOST_SCRIPT_EASY = "GhostMovementSkript";
    public static final String GHOST_SCRIPT_HARD = "SmartGhostMovementSkript";
    public static final double GHOST_RADIUS = 0.3;
    public static final int GHOST_SPEED = 7;
    public static final int GHOST_DAMAGE = 2000;
    public static final int AMOUNT_PLAYERS = 5;
    public static final String SCRIPT_GHOST_DIFFICULTY_MULTIPLAYER = "EASY";

    //GAME
    public static final long PLAYING_TIME = 300000; // 5 mins = 300.000 milisecs
}
