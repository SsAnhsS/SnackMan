package de.hsrm.mi.swt.snackman.entities.lobby;

/**
 * Data Transfer Object (DTO) representing the end state of a game session.
 */
public record GameEndDTO(ROLE role, long timePlayed, int kcalCollected, String lobbyId) {
    public static GameEndDTO fromGameEnd(GameEnd gameEnd) {
        return new GameEndDTO(gameEnd.getRole(), gameEnd.getTimePlayed(), gameEnd.getKcalCollected(), gameEnd.getLobbyId());
    }
}
