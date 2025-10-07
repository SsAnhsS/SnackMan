package de.hsrm.mi.swt.snackman.messaging.MessageLoop;

import org.joml.Quaterniond;
import org.joml.Vector3d;

public record MobUpdateMessage(Vector3d position, Quaterniond rotation, double radius, double speed, String playerId,
                               int sprintTimeLeft, boolean isSprinting, boolean isInCooldown, int calories,
                               String message, boolean isScared) {
}