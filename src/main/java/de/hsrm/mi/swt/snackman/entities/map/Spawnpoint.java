package de.hsrm.mi.swt.snackman.entities.map;

public record Spawnpoint(SpawnpointMobType spawnpointMobType) {

    @Override
    public SpawnpointMobType spawnpointMobType() {
        return spawnpointMobType;
    }
}
