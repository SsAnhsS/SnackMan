package de.hsrm.mi.swt.snackman.messaging;

import de.hsrm.mi.swt.snackman.entities.lobby.Lobby;

public record FrontendChooseRoleEvent(Lobby lobby) {

    public Lobby lobby() {
        return lobby;
    }

    @Override
    public String toString() {
        return "FrontendLobbyMessageEvent{" +
                ", lobby=" + lobby +
                '}';
    }

}
