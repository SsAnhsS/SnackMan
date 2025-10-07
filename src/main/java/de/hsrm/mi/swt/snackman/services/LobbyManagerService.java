package de.hsrm.mi.swt.snackman.services;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.*;

import de.hsrm.mi.swt.snackman.entities.map.GameMap;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.ScriptGhost;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.ScriptGhostDifficulty;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.eatingMobs.Chicken.Chicken;
import de.hsrm.mi.swt.snackman.messaging.MessageLoop.MessageLoop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import de.hsrm.mi.swt.snackman.entities.lobby.Lobby;
import de.hsrm.mi.swt.snackman.entities.lobby.PlayerClient;
import de.hsrm.mi.swt.snackman.entities.lobby.ROLE;

/**
 * Service for managing lobbies and clients in the application.
 */
@Service
public class LobbyManagerService {

    private final MapService mapService;
    private final Map<String, Lobby> lobbies = new HashMap<>();
    private final Map<String, PlayerClient> clients = new HashMap<>();
    private final Logger log = LoggerFactory.getLogger(LobbyManagerService.class);
    private final MessageLoop messageLoop;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    public LobbyManagerService(MapService mapService, @Lazy MessageLoop messageLoop) {
        this.mapService = mapService;
        this.messageLoop = messageLoop;
    }

    /**
     * Create a new client
     *
     * @param name the name of the client
     * @return the client
     */
    public PlayerClient createNewClient(String name) {
        String uuid = UUID.randomUUID().toString();
        PlayerClient newClient = new PlayerClient(uuid, name);
        this.clients.put(uuid, newClient);

        return newClient;
    }

    /**
     * Creates a new lobby and adds it to the list. Initilizes the GameMap without Mobs.
     *
     * @param name Name of the lobby
     * @return The lobby created
     * @throws LobbyAlreadyExistsException
     */
    public Lobby createLobby(String name, PlayerClient admin, MessageLoop messageLoop, String difficulty) throws LobbyAlreadyExistsException {
        if (lobbies.values().stream().anyMatch(lobby -> lobby.getName().equals(name))) {
            throw new LobbyAlreadyExistsException("Lobby name already exists");
        }

        //TODO change to SessionId
        var uuid = UUID.randomUUID().toString();
        GameMap gameMap = this.mapService.createNewGameMap(uuid);

        ScriptGhostDifficulty scriptGhostDifficulty = ScriptGhostDifficulty.getScriptGhostDifficulty(difficulty);
        Lobby lobby = new Lobby(uuid, name, admin, gameMap, messageLoop, scriptGhostDifficulty);
        admin.setRole(ROLE.UNDEFINED);

        lobbies.put(lobby.getLobbyId(), lobby);
        return lobby;
    }

    /**
     * Returns the list of all lobbies.
     *
     * @return list of lobbies
     */
    public List<Lobby> getAllLobbies() {
        return lobbies.values().stream().toList();
    }

    /**
     * Adds a player to a lobby.
     *
     * @param lobbyId  ID of the lobby
     * @param playerId ID of the player
     * @return The updated lobby
     * @throws GameAlreadyStartedException if the game has already been started
     */
    public Lobby joinLobby(String lobbyId, String playerId) throws GameAlreadyStartedException {
        Lobby lobby = findLobbyByLobbyId(lobbyId);

        if (lobby.isGameStarted() || lobby.isChooseRole()) {
            throw new GameAlreadyStartedException("Game already started");
        }

        Optional<PlayerClient> newJoiningClient = findClientByClientId(playerId);
        if (!lobby.getAdminClientId().equals(playerId) && newJoiningClient.isPresent()) {
            newJoiningClient.get().setRole(ROLE.UNDEFINED);
        }

        lobby.getMembers().add(newJoiningClient.get());

        return lobby;
    }

    /**
     * Removes a player from a lobby.
     *
     * @param lobbyId  ID of the lobby
     * @param playerId ID of the player
     */
    public synchronized void leaveLobby(String lobbyId, String playerId) {
        Lobby lobby = findLobbyByLobbyId(lobbyId);
        lobby.removeMember(playerId);

        if (lobby.getAdminClientId().equals(playerId) || lobby.getMembers().isEmpty()) {
            lobbies.remove(lobby.getLobbyId());
        }
    }

    /**
     * Stops all threads of chickens and script ghosts.
     * Removes all players from the lobby and deletes the
     * lobby afterward.
     *
     * @param lobbyId the id of the lobby to be closed
     */
    public synchronized void closeAndDeleteLobby(String lobbyId) {
        Lobby lobby = findLobbyByLobbyId(lobbyId);

        stopAllScriptThreads(lobby);
        removeAllPlayersFromLobby(lobby);

        messagingTemplate.convertAndSend("/topic/lobbies", getAllLobbies());
    }

    /**
     * Stops all threads of chickens and script ghosts.
     *
     * @param lobby the lobby where to stop the scripts
     */
    private void stopAllScriptThreads(Lobby lobby) {
        for (Chicken chicken : lobby.getChickens()) {
            chicken.setWalking(false);
        }
        for (ScriptGhost scriptGhost : lobby.getScriptGhosts()) {
            scriptGhost.setWalking(false);
        }

    }

    /**
     * Removes all players from the lobby and deletes the
     * lobby afterward.
     *
     * @param lobby the lobby where to stop the scripts
     */
    private synchronized void removeAllPlayersFromLobby(Lobby lobby) {
        List<PlayerClient> membersToRemove = new ArrayList<>(lobby.getMembers());
        PlayerClient admin = null;

        for (PlayerClient player : membersToRemove) {
            if (!lobby.getAdminClient().equals(player)) {
                leaveLobby(lobby.getLobbyId(), player.getPlayerId());
            } else {
                admin = player;
            }
        }

        assert admin != null;
        leaveLobby(lobby.getLobbyId(), admin.getPlayerId());
    }

    /**
     * Starts the game in the specified lobby.
     *
     * @param lobbyId ID of the lobby
     */
    public void startGame(String lobbyId) {
        Lobby lobby = findLobbyByLobbyId(lobbyId);

        if (lobby.getMembers().size() < 2) {
            throw new IllegalStateException("Not enough players to start the game");
        }

        // If Admin want to play with custom map
        if (lobby.getUsedCustomMap()) {
            String customMapName = String.format("SnackManMap_%s.txt", lobbyId);

            Path customMapPath = Paths.get("./extensions/map/" + customMapName).toAbsolutePath();

            if (!Files.exists(customMapPath)) {
                throw new IllegalStateException("Custom map file not found: " + customMapPath);
            }

            GameMap newGameMap = mapService.createNewGameMap(lobbyId, customMapPath.toString());

            lobby.setGameMap(newGameMap);
        }

        if (lobby.getGameMap() == null) {
            throw new IllegalStateException("Game map is not set. Unable to start the game.");
        }

        log.info("Starting lobby {}", lobby);
        lobby.startGame();
        mapService.spawnMobs(lobby.getGameMap(), lobby);
    }

    /**
     * Starts a singleplayer game in the specified lobby.
     *
     * @param lobbyId ID of the lobby
     */
    public void startSingleplayer(String lobbyId) {
        Lobby lobby = findLobbyByLobbyId(lobbyId);
        lobby.startGame();

        mapService.spawnMobs(lobby.getGameMap(), lobby);
    }

    /**
     * Searches the lobby for its UUID
     *
     * @param lobbyID UUID of the lobby
     * @return the lobby
     */
    public Lobby findLobbyByLobbyId(String lobbyID) {
        Lobby lobby = lobbies.get(lobbyID);
        if (lobby == null) {
            throw new NoSuchElementException("There is not lobby with the id " + lobbyID);
        } else {
            return lobby;
        }
    }

    /**
     * Checks weather the snackman-role has already been selected in the lobby
     *
     * @param lobby the lobby to search in
     * @return true if the role snackman is already owned by someone
     */
    public boolean snackmanAlreadySelected(Lobby lobby) {
        return lobby.getMembers().stream().anyMatch(playerClient -> playerClient.getRole() == ROLE.SNACKMAN);
    }

    /**
     * Searches the client for their UUID
     *
     * @param clientID UUID of the client
     * @return the client
     */
    public Optional<PlayerClient> findClientByClientId(String clientID) {
        PlayerClient client = clients.get(clientID);
        if (client == null) {
            return Optional.of(null);
        } else {
            return Optional.of(client);
        }
    }

    public GameMap getGameMapByLobbyId(String lobbyId) {
        return lobbies.get(lobbyId).getGameMap();
    }

    public MessageLoop getMessageLoop() {
        return messageLoop;
    }


    public void chooseRoleTrue(String lobbyId) {
        Lobby lobby = findLobbyByLobbyId(lobbyId);
        lobby.setChooseRole();
    }

}
