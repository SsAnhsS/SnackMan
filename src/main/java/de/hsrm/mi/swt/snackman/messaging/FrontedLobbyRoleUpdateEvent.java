package de.hsrm.mi.swt.snackman.messaging;

import de.hsrm.mi.swt.snackman.entities.lobby.Lobby;

// Idee lobby, SELECTED?, ButtonId,
public record FrontedLobbyRoleUpdateEvent(Lobby lobby, String selectedBy, boolean selected, String buttonId) {

    @Override
    public Lobby lobby() {
        return lobby;
    }

    public boolean selected() {
        return selected;
    }

    @Override
    public String toString() {
        return "FrontedLobbyRoleUpdateEvent{" +
                "lobby=" + lobby +
                ", selectedBy='" + selectedBy + '\'' +
                ", selected=" + selected +
                ", buttonId='" + buttonId + '\'' +
                '}';
    }
}

