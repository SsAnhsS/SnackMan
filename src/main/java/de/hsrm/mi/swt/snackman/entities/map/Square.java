package de.hsrm.mi.swt.snackman.entities.map;

import de.hsrm.mi.swt.snackman.entities.mapObject.MapObjectType;
import de.hsrm.mi.swt.snackman.entities.mapObject.snack.Snack;
import de.hsrm.mi.swt.snackman.entities.mapObject.snack.SnackType;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.Ghost;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.Mob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.eatingMobs.Chicken.Chicken;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.eatingMobs.SnackMan;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.ScriptGhost;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that represents a Square. A Square is part of the game map. Multiple squares representing a game map.
 */
public class Square {
    //It's static because the idCounter is the same for all Squares.
    private static long idCounter = 0;
    private final Logger log = LoggerFactory.getLogger(Square.class);
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private long id;

    private int indexX, indexZ;

    private MapObjectType type;

    private Snack snack;

    private List<Mob> mobs = new ArrayList<Mob>();

    private Spawnpoint spawnpoint;

    public Square(int indexX, int indexZ) {
        id = generateId();
        type = MapObjectType.FLOOR;
        this.indexX = indexX;
        this.indexZ = indexZ;
        this.snack = new Snack(SnackType.EMPTY);
    }

    public Square(int indexX, int indexZ, Spawnpoint spawnpoint) {
        this(indexX, indexZ);
        this.spawnpoint = spawnpoint;
        this.snack = new Snack(SnackType.EMPTY);
    }

    public Square(MapObjectType type, int indexX, int indexZ) {
        this(indexX, indexZ);
        this.type = type;
        this.snack = new Snack(SnackType.EMPTY);
    }

    public Square(Snack snack, int indexX, int indexZ) {
        this(indexX, indexZ);
        this.snack = snack;
        type = MapObjectType.FLOOR;
    }

    /**
     * Method to generate the next id of a new Square. It is synchronized because of thread-safety.
     *
     * @return the next incremented id
     */
    private synchronized static long generateId() {
        return idCounter++;
    }

    /**
     * Adds a {@link PropertyChangeListener} to this object.
     * The listener will be notified whenever a bound property changes.
     *
     * @param listener the {@link PropertyChangeListener} to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public int getIndexX() {
        return indexX;
    }

    public int getIndexZ() {
        return indexZ;
    }

    public MapObjectType getType() {
        return type;
    }

    public void setType(MapObjectType type) {
        this.type = type;
    }

    public Snack getSnack() {
        return snack;
    }

    public void setSnack(Snack snack) {
        // Only add Snack when it's actually a floor
        if (type == MapObjectType.FLOOR) {
            if (this.snack != null && snack != null) {
                log.debug("Square id {} with snack set to {}", id, snack.getSnackType().name());
            } else if (this.snack != null && snack == null) {
                log.debug("Removing snack from square id {}", id);
            }
            this.snack = snack;
            propertyChangeSupport.firePropertyChange("square", null, this);
        }
    }


    public long getId() {
        return id;
    }

    /**
     * @return the dominant type of MapObject
     */
    public synchronized String getPrimaryType() {
        if (type == MapObjectType.WALL) {
            return "W";
        } else if (type == MapObjectType.FLOOR) {
            List<Mob> mobsCopy = new ArrayList<>(mobs);
            if (mobsCopy.stream().anyMatch(mob -> mob instanceof Ghost || mob instanceof ScriptGhost)) return "G";
            else if (mobsCopy.stream().anyMatch(mob -> mob instanceof SnackMan)) return "SM";
            else if (mobsCopy.stream().anyMatch(mob -> mob instanceof Chicken)) return "C";
            else if (this.snack != null && !this.snack.getSnackType().equals(SnackType.EGG))
                return "S";     // eats all snacks except for eggs
        }
        return "L";
    }

    /**
     * @return the dominant type of MapObject for the ghost
     */
    public synchronized String getPrimaryTypeForGhost() {
        if (type == MapObjectType.WALL) {
            return "W";
        } else if (type == MapObjectType.FLOOR) {
            List<Mob> mobsCopy = new ArrayList<>(mobs);
            if (mobsCopy.stream().anyMatch(mob -> mob instanceof SnackMan)) return "M";
            if (mobsCopy.stream().anyMatch(mob -> mob instanceof Chicken)) return "C";
            if (mobsCopy.stream().anyMatch(mob -> mob instanceof Ghost)) return "G";
            if (mobsCopy.stream().anyMatch(mob -> mob instanceof ScriptGhost)) return "G";
            else if (this.snack != null) return "S";
        }
        return "L";
    }

    /**
     * @return the dominant type of MapObject for the ghost with the high difficulty
     */
    public String getPrimaryTypeForGhostWithHighDifficulty(long ghostId) {
        if (type == MapObjectType.WALL) {
            return "W";
        } else if (type == MapObjectType.FLOOR) {
            if (this.mobs.stream().anyMatch(mob -> mob instanceof SnackMan)) return "M";
        }
        return "L";
    }

    public List<Mob> getMobs() {
        return mobs;
    }

    public void addMob(Mob mob) {
        this.mobs.add(mob);
    }

    public void removeMob(Mob mob) {
        this.mobs.remove(mob);
    }

    @Override
    public String toString() {
        return "Square{" +
                "indexX=" + indexX +
                ", indexZ=" + indexZ +
                ", type=" + type +
                ", snack=" + snack +
                ", mobs=" + mobs +
                '}';
    }

    public Spawnpoint getSpawnpoint() {
        return spawnpoint;
    }
}
