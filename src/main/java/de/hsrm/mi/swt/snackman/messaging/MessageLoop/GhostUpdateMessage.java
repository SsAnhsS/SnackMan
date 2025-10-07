package de.hsrm.mi.swt.snackman.messaging.MessageLoop;

import de.hsrm.mi.swt.snackman.entities.mobileObjects.Ghost;
import org.joml.Quaterniond;
import org.joml.Vector3d;

public record GhostUpdateMessage(Vector3d position, Quaterniond rotation, double radius, double speed,
                                 String playerId) {

    public static GhostUpdateMessage fromGhost(Ghost ghost, String playerId) {
        return new GhostUpdateMessage(ghost.getPosition(), ghost.getRotationQuaternion(), ghost.getRadius(), ghost.getSpeed(), playerId);
    }
}