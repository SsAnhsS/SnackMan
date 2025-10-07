package de.hsrm.mi.swt.snackman.controller.PlayerMovement;

public record PlayerToFrontendDTO(double posX, double posY, double posZ, double qX, double qY, double qZ, double qW,
                                  double radius, double speed, String playerId, double sprintMultiplier,
                                  int maxCalories) {
}
