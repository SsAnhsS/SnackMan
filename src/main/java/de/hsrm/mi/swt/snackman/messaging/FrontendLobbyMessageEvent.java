package de.hsrm.mi.swt.snackman.messaging;

import de.hsrm.mi.swt.snackman.entities.lobby.Lobby;

import java.util.List;

public record FrontendLobbyMessageEvent(List<Lobby> lobbies) {

    @Override
    public List<Lobby> lobbies() {
        return lobbies;
    }

    @Override
    public String toString() {
        return "FrontendLobbyMessageEvent{" +
                ", lobbies=" + lobbies +
                '}';
    }
}
