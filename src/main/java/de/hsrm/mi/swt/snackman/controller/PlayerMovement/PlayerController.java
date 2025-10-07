package de.hsrm.mi.swt.snackman.controller.PlayerMovement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import de.hsrm.mi.swt.snackman.configuration.GameConfig;
import de.hsrm.mi.swt.snackman.services.LobbyManagerService;


@Controller
@RequestMapping("/api")
public class PlayerController {
    @Autowired
    private LobbyManagerService lobbyService;

    // Zum Registrieren eines neuen Spielers

    @GetMapping("/lobbies/{lobbyId}/player/{playerId}")
    public ResponseEntity<PlayerToFrontendDTO> initSnackman(@PathVariable("lobbyId") String lobbyId, @PathVariable("playerId") String playerId) {
        var playerMob = lobbyService.findLobbyByLobbyId(lobbyId).getClientMobMap().get(playerId);

        return ResponseEntity.ok(new PlayerToFrontendDTO(playerMob.getPosX(), playerMob.getPosY(), playerMob.getPosZ(),
                playerMob.getRotationQuaternion().x, playerMob.getRotationQuaternion().y,
                playerMob.getRotationQuaternion().z, playerMob.getRotationQuaternion().w, playerMob.getRadius(),
                playerMob.getSpeed(), playerId, GameConfig.SNACKMAN_SPRINT_MULTIPLIER, GameConfig.SNACKMAN_MAX_CALORIES));
    }
}
