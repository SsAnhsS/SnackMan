package de.hsrm.mi.swt.snackman.entities.mobileObjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.hsrm.mi.swt.snackman.entities.map.GameMap;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

import de.hsrm.mi.swt.snackman.SnackmanApplication;
import de.hsrm.mi.swt.snackman.configuration.GameConfig;
import de.hsrm.mi.swt.snackman.entities.map.Square;
import de.hsrm.mi.swt.snackman.entities.mapObject.MapObjectType;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.eatingMobs.SnackMan;

class MobWallCollisionTest {

    private SnackMan snackman;

    private GameMap gameMap;

    private static final Path workFolder = Paths.get("./extensions").toAbsolutePath();

    @BeforeAll
    static void fileSetUp() {
        try{
            tearDownAfter();
        }catch(Exception e){
            System.out.println("No file to delete");
        }
        SnackmanApplication.checkAndCopyResources();
    }

    @AfterAll
    static void tearDownAfter() throws IOException {
        if (Files.exists(workFolder)) {
            FileSystemUtils.deleteRecursively(workFolder.toFile());
        }
    }

    @BeforeEach
    public void setup(){
        Square[][] testMap1 = { {new Square(0,0), new Square(0,1), new Square(MapObjectType.WALL,0,2)},
                                {new Square(MapObjectType.WALL,1,0), new Square(1,1), new Square(1,2)}, 
                                {new Square(2,0), new Square(MapObjectType.WALL,2,1), new Square(2,2)} };
        this.gameMap = new GameMap(testMap1);

        snackman = new SnackMan(gameMap, 10, 0.3, 1.5 * GameConfig.SQUARE_SIZE,1,1.5 * GameConfig.SQUARE_SIZE);
    }

    @Test
    void checkCollisionNoWallLeft(){
        double xNew = snackman.getPosX() - (GameConfig.SQUARE_SIZE/2) + (snackman.getRadius()/2);
        double zNew = snackman.getPosZ();
        assertEquals(0, snackman.checkWallCollision(xNew, zNew, this.gameMap));
    }

    @Test
    void checkCollisionWallVertical(){
        double xNew = snackman.getPosX();
        double zNew = snackman.getPosZ() - (GameConfig.SQUARE_SIZE/2) + (snackman.getRadius()/2);
        assertEquals(2, snackman.checkWallCollision(xNew, zNew, this.gameMap));
    }

    @Test
    void checkCollisionWallHorizontal(){
        double xNew = snackman.getPosX() + (GameConfig.SQUARE_SIZE/2) - (snackman.getRadius()/2);
        double zNew = snackman.getPosZ();
        assertEquals(1, snackman.checkWallCollision(xNew, zNew, this.gameMap));
    }

    @Test
    void checkCollisionWallDiagonal(){
        double xNew = snackman.getPosX() - (GameConfig.SQUARE_SIZE/2) + (snackman.getRadius()/2);
        double zNew = snackman.getPosZ() + (GameConfig.SQUARE_SIZE/2) - (snackman.getRadius()/2);
        assertEquals(3, snackman.checkWallCollision(xNew, zNew, this.gameMap));
    }

    @Test
    void checkCollisionWallRightTop(){
        double xNew = snackman.getPosX() + (GameConfig.SQUARE_SIZE/2) - (snackman.getRadius()/2);
        double zNew = snackman.getPosZ() - (GameConfig.SQUARE_SIZE/2) + (snackman.getRadius()/2);
        assertEquals(3, snackman.checkWallCollision(xNew, zNew, this.gameMap));
    }
}
