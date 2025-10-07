package de.hsrm.mi.swt.snackman.entities.lobby;

/**
 * Represents the end state of a game session, including role, time played,
 * and the calories the snackman collected during the game.
 */
public class GameEnd {
    private ROLE role;
    private long timePlayed;
    private int kcalCollected;
    private String lobbyId;

    public GameEnd(ROLE role, long timePlayed, int kcalCollected, String lobbyId) {
        this.role = role;
        this.timePlayed = timePlayed;
        this.kcalCollected = kcalCollected;
        this.lobbyId = lobbyId;
    }

    public ROLE getRole() {
        return role;
    }

    public long getTimePlayed() {
        return timePlayed;
    }

    public int getKcalCollected() {
        return kcalCollected;
    }

    public String getLobbyId() {
        return lobbyId;
    }
}
