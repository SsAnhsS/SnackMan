package de.hsrm.mi.swt.snackman.controller.PlayerMovement;

// Identifikation welche Figur/Charakter über die UUID
public record PlayerToBackendDTO(boolean forward, boolean backward, boolean left, boolean right, boolean jump,
                                 boolean doubleJump, double qX, double qY, double qZ, double qW, double delta,
                                 String playerId, boolean sprinting) {
}
