package de.hsrm.mi.swt.snackman.entities.lobby;


/**
 * Represents a client or player in the lobby system.
 */
public class PlayerClient {
    private String playerId;
    private String playerName;
    private ROLE role;

    public PlayerClient(String playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.role = ROLE.UNDEFINED;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public ROLE getRole() {
        return role;
    }

    public void setRole(ROLE role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "PlayerClient{" +
                "playerId='" + playerId + '\'' +
                ", playerName='" + playerName + '\'' +
                ", role=" + role +
                '}';
    }
}
