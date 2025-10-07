package de.hsrm.mi.swt.snackman.messaging.MessageLoop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hsrm.mi.swt.snackman.controller.Square.SquareDTO;
import de.hsrm.mi.swt.snackman.entities.lobby.*;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.Ghost;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.ScriptGhost;
import de.hsrm.mi.swt.snackman.services.MapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import de.hsrm.mi.swt.snackman.configuration.GameConfig;
import de.hsrm.mi.swt.snackman.entities.map.Square;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.Mob;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.eatingMobs.Chicken.Chicken;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.eatingMobs.SnackMan;
import de.hsrm.mi.swt.snackman.services.LobbyManagerService;

@Service
public class MessageLoop {

    Logger log = LoggerFactory.getLogger(MessageLoop.class);
    @Autowired
    private LobbyManagerService lobbyService;
    @Autowired
    private MapService mapService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    private Map<String, List<Square>> changedSquares = new HashMap<>();
    // private Map<String, List<KollisionEvent>> kollisions;

    private Map<String, List<Chicken>> changedChicken = new HashMap<>();

    private Map<String, List<ScriptGhost>> changedScriptGhosts = new HashMap<>();

    private Map<String, List<GameEnd>> changedGameEnd = new HashMap<>();

    @Scheduled(fixedRate = 50)
    public void messageLoop() {
        List<Lobby> lobbys = lobbyService.getAllLobbies();
        if (lobbys.isEmpty()) {
            return;
        }
        for (Lobby lobby : lobbys) {
            if (!lobby.isGameStarted()) {
                continue;
            }
            List<Message> messages = new ArrayList<>();

            List<GameEnd> gameEndQueue = changedGameEnd.get(lobby.getLobbyId());
            changedGameEnd.remove(lobby.getLobbyId());

            if (gameEndQueue != null) {
                for (GameEnd gameEnd : gameEndQueue) {
                    log.info("The game {} has been ended.", lobby.getLobbyId());
                    messages.add(new Message<>(EventEnum.GameEnd, GameEndDTO.fromGameEnd(gameEnd)));
                    lobbyService.closeAndDeleteLobby(lobby.getLobbyId());
                    messagingTemplate.convertAndSend("/topic/lobbies/" + lobby.getLobbyId() + "/update", messages);
                    return;
                }
            }

            List<Square> squareQueue = changedSquares.get(lobby.getLobbyId());
            changedSquares.remove(lobby.getLobbyId());

            List<Chicken> chickenQueue = changedChicken.get(lobby.getLobbyId());
            changedChicken.remove(lobby.getLobbyId());

            List<ScriptGhost> scriptGhostQueue = changedScriptGhosts.get(lobby.getLobbyId());
            changedScriptGhosts.remove(lobby.getLobbyId());

            for (String client : lobby.getClientMobMap().keySet()) {
                Mob mob = lobby.getClientMobMap().get(client);

                switch (mob) {
                    case SnackMan snackMan -> {
                        messages.add(new Message<>(EventEnum.SnackManUpdate, new MobUpdateMessage(snackMan.getPosition(),
                                snackMan.getQuat(), snackMan.getRadius(), snackMan.getSpeed(), client, snackMan.getSprintTimeLeft(),
                                snackMan.isSprinting(), snackMan.isInCooldown(), snackMan.getCurrentCalories(),
                                snackMan.getCurrentCalories() >= GameConfig.MAX_KALORIEN ?
                                        GameConfig.MAX_KALORIEN_MESSAGE : null, snackMan.isScared()
                        )));
                    }
                    case Ghost ghost -> {
                        messages.add(new Message<>(EventEnum.GhostUpdate, GhostUpdateMessage.fromGhost(ghost, client)));
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + mob);
                }
            }
            if (squareQueue != null) {
                for (Square square : squareQueue) {
                    messages.add(new Message<>(EventEnum.SquareUpdate, new SquareUpdateMessage(SquareDTO.fromSquare(square))));
                }
            }
            if (chickenQueue != null) {
                for (Chicken chicken : chickenQueue) {
                    messages.add(new Message<>(EventEnum.ChickenUpdate, ChickenUpdateMessage.fromChicken(chicken)));
                }
            }
            if (scriptGhostQueue != null) {
                for (ScriptGhost scriptGhost : scriptGhostQueue) {
                    messages.add(new Message<>(EventEnum.ScriptGhostUpdate, ScriptGhostDTO.fromScriptGhost(scriptGhost)));
                }
            }

            if (messages.isEmpty()) {
                return;
            }
            messagingTemplate.convertAndSend("/topic/lobbies/" + lobby.getLobbyId() + "/update", messages);
            long currentTime = System.currentTimeMillis();
            if ((currentTime - lobby.getTimeSinceLastSnackSpawn()) > GameConfig.TIME_FOR_SNACKS_TO_RESPAWN) {
                this.mapService.respawnSnacks(lobbyService.getGameMapByLobbyId(lobby.getLobbyId()), GameConfig.SNACK_SPAWN_RATE);
                lobby.setTimeSinceLastSnackSpawn(System.currentTimeMillis());
            }
        }
    }

    public void addSquareToQueue(Square square, String lobbyId) {
        if (changedSquares.containsKey(lobbyId)) {
            changedSquares.get(lobbyId).add(square);
        } else {
            List<Square> temp = new ArrayList<>();
            temp.add(square);
            changedSquares.put(lobbyId, temp);
        }
    }

    public void addChickenToQueue(Chicken chicken, String lobbyId) {
        if (changedChicken.containsKey(lobbyId)) {
            changedChicken.get(lobbyId).add(chicken);
        } else {
            List<Chicken> temp = new ArrayList<>();
            temp.add(chicken);
            changedChicken.put(lobbyId, temp);
        }
    }

    public void addScriptGhostToQueue(ScriptGhost scriptGhost, String lobbyId) {
        if (changedScriptGhosts.containsKey(lobbyId)) {
            changedScriptGhosts.get(lobbyId).add(scriptGhost);
        } else {
            List<ScriptGhost> temp = new ArrayList<>();
            temp.add(scriptGhost);
            changedScriptGhosts.put(lobbyId, temp);
        }
    }

    public void addGameEndToQueue(GameEnd gameEnd, String lobbyId) {
        if (changedGameEnd.containsKey(lobbyId)) {
            changedGameEnd.get(lobbyId).add(gameEnd);
            // Lobby Role entfernen
            Lobby l = lobbyService.findLobbyByLobbyId(lobbyId);
            for (PlayerClient p : l.getMembers()) {
                p.setRole(ROLE.UNDEFINED);
            }
        } else {
            List<GameEnd> temp = new ArrayList<>();
            temp.add(gameEnd);
            changedGameEnd.put(lobbyId, temp);
        }
    }
}
