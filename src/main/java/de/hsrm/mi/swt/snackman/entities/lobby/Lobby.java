package de.hsrm.mi.swt.snackman.entities.lobby;

import java.util.*;

import de.hsrm.mi.swt.snackman.configuration.GameConfig;
import de.hsrm.mi.swt.snackman.entities.map.GameMap;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.Mob;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.ScriptGhost;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.ScriptGhostDifficulty;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.eatingMobs.Chicken.Chicken;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.eatingMobs.SnackMan;
import de.hsrm.mi.swt.snackman.messaging.MessageLoop.MessageLoop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a lobby where players can gather to play a game together.
 */
public class Lobby {
    private final Logger log = LoggerFactory.getLogger(Lobby.class);
    private String lobbyId;
    private String name;
    private PlayerClient adminClient;
    private boolean isGameStarted;
    private boolean isChooseRole;
    private List<PlayerClient> members;
    private GameMap gameMap;
    private SortedMap<String, Mob> clientMobMap;
    private List<Chicken> chickens = new ArrayList<>();
    private List<ScriptGhost> scriptGhosts = new ArrayList<>();
    private long timeSinceLastSnackSpawn;
    private Timer gameTimer;
    private long timePlayed = 0;
    private boolean isGameFinished = false;
    private ROLE winningRole;
    private long gameStartTime;
    private long endTime;
    private MessageLoop messageLoop;
    private boolean usedCustomMap;
    private ScriptGhostDifficulty scriptGhostDifficulty;

    public Lobby(String lobbyId, String name, PlayerClient adminClient, GameMap gameMap, MessageLoop messageLoop, ScriptGhostDifficulty scriptGhostDifficulty) {
        this.lobbyId = lobbyId;
        this.gameMap = gameMap;
        this.name = name;
        this.adminClient = adminClient;
        this.isGameStarted = false;
        this.isChooseRole = false;
        this.members = new ArrayList<>();
        this.members.add(adminClient);
        this.clientMobMap = new TreeMap<>();
        this.messageLoop = messageLoop;
        this.scriptGhostDifficulty = scriptGhostDifficulty;
        initTimer();
        this.usedCustomMap = false;
    }

    /**
     * initializes the timer responsible for making sure
     * a match only lasts 5 minutes
     */
    private void initTimer() {
        this.gameTimer = new Timer();
    }

    /**
     * Removes a player from the lobby
     * @param playerId the id of the player to be removed
     */
    public synchronized void removeMember(String playerId) {
        members.removeIf(client -> client.getPlayerId().equals(playerId));
    }

    /**
     * Starts a new timer for playing the game.
     * After 5 minutes, the game is automatically stopped.
     */
    private void startNewGameTimer() {
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        gameTimer = new Timer();

        TimerTask task = new TimerTask() {
            public void run() {
                endGame(ROLE.GHOST);
            }
        };

        gameTimer.schedule(task, GameConfig.PLAYING_TIME);
    }

    /**
     * Starts the game by starting the playing timer
     * and saving when the game started.
     */
    public void startGame() {
        setGameStarted();
        startNewGameTimer();
        this.gameStartTime = System.currentTimeMillis();
    }

    /**
     * Ends the game by ending the playing time timer
     * and determining who won the game.
     *
     * @param winningRole the role winning the game
     */
    public void endGame(ROLE winningRole) {
        log.info("The role {} has won the game.", winningRole);
        this.endTime = System.currentTimeMillis();
        this.timePlayed = (endTime - this.gameStartTime) / 1000;
        if (this.timePlayed > (GameConfig.PLAYING_TIME / 1000))
            this.timePlayed = GameConfig.PLAYING_TIME / 1000;
        this.winningRole = winningRole;
        SnackMan snackMan = getSnackman();

        if (snackMan != null) {
            this.gameTimer.cancel();
            GameEnd gameEnd = new GameEnd(winningRole, this.timePlayed, snackMan.getKcal(), this.lobbyId);
            setGameFinished(true, gameEnd);
        }
    }

    /**
     * Retrieves the first {@link SnackMan} instance found on the game map.
     * The method searches through all squares of the game map, checking each square's mobs for a {@link SnackMan}.
     *
     * @return the first {@link SnackMan} instance found, or null if no SnackMan is present
     */
    public SnackMan getSnackman() {
        return Arrays.stream(this.gameMap.getGameMapSquares())
                .flatMap(Arrays::stream)
                .flatMap(square -> square.getMobs().stream())
                .filter(mob -> mob instanceof SnackMan)
                .map(mob -> (SnackMan) mob)
                .findFirst()
                .orElse(null);
    }

    public SortedMap<String, Mob> getClientMobMap() {
        return clientMobMap;
    }

    public String getName() {
        return name;
    }

    public String getAdminClientId() {
        return adminClient.getPlayerId();
    }

    public boolean isGameStarted() {
        return isGameStarted;
    }

    /**
     * Marks the game as started by setting the {@code isGameStarted} flag to {@code true}.
     * Also sets the time of the last snack spawn to the current system time.
     */
    public void setGameStarted() {
        this.isGameStarted = true;
        setTimeSinceLastSnackSpawn(System.currentTimeMillis());
    }

    /**
     * Marks the game as finished and processes the end state of the game.
     * Updates the {@code isGameFinished} flag and adds the provided {@link GameEnd} object
     * to the message queue for processing.
     *
     * @param gameFinished {@code true} to mark the game as finished, {@code false} otherwise
     * @param gameEnd      the {@link GameEnd} object representing the final state of the game
     */
    public void setGameFinished(boolean gameFinished, GameEnd gameEnd) {
        this.isGameFinished = gameFinished;
        messageLoop.addGameEndToQueue(gameEnd, lobbyId);
    }

    public List<PlayerClient> getMembers() {
        return members;
    }

    public PlayerClient getAdminClient() {
        return adminClient;
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    public void setGameMap(GameMap newGameMap) {
        this.gameMap = newGameMap;
    }

    public String getLobbyId() {
        return lobbyId;
    }

    public long getTimeSinceLastSnackSpawn() {
        return timeSinceLastSnackSpawn;
    }

    public void setTimeSinceLastSnackSpawn(long time) {
        timeSinceLastSnackSpawn = time;
    }

    public void setChooseRole() {
        this.isChooseRole = true;
    }

    public void setChooseRoleFinsih() {
        this.isChooseRole = false;
    }

    public boolean isChooseRole() {
        return isChooseRole;
    }

    @Override
    public String toString() {
        return "Lobby{" +
                "lobbyId='" + lobbyId + '\'' +
                ", name='" + name + '\'' +
                ", adminClient=" + adminClient +
                ", isGameStarted=" + isGameStarted +
                ", isChooseRole=" + isChooseRole +
                ", members=" + members +
                ", gameMap=" + gameMap +
                ", clientMobMap=" + clientMobMap +
                ", timeSinceLastSnackSpawn=" + timeSinceLastSnackSpawn +
                '}';
    }

    public void addChicken(Chicken chicken) {
        this.chickens.add(chicken);
    }

    public void addScriptGhost(ScriptGhost scriptGhost) {
        this.scriptGhosts.add(scriptGhost);
    }

    public List<Chicken> getChickens() {
        return chickens;
    }

    public List<ScriptGhost> getScriptGhosts() {
        return scriptGhosts;
    }

    public boolean getUsedCustomMap() {
        return usedCustomMap;
    }

    public void setUsedCustomMap(boolean value) {
        this.usedCustomMap = value;
    }

    public long getGameStartTime() {
        return this.gameStartTime;
    }

    public ScriptGhostDifficulty getScriptGhostDifficulty() {
        return scriptGhostDifficulty;
    }
}
