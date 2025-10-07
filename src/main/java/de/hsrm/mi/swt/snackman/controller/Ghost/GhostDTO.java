package de.hsrm.mi.swt.snackman.controller.Ghost;

public record GhostDTO(long id, double posX, double posY, double posZ, double radius, int speed) {

    @Override
    public String toString() {
        return "GhostDTO{" +
                "id=" + id +
                ", posX=" + posX +
                ", posY=" + posY +
                ", posZ=" + posZ +
                ", radius=" + radius +
                ", speed=" + speed +
                '}';
    }
}
