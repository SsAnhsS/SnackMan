package de.hsrm.mi.swt.snackman.entities.mobileObjects.eatingMobs;

import de.hsrm.mi.swt.snackman.SnackmanApplication;
import de.hsrm.mi.swt.snackman.entities.map.GameMap;
import de.hsrm.mi.swt.snackman.entities.map.Square;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.Ghost;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.eatingMobs.Chicken.Chicken;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.FileSystemUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link Ghost} class.
 * This test ensures that the Ghost behaves as expected during movement and interaction with other mobs.
 */
@SpringBootTest
class GhostTest {

    private Ghost ghost;

    private Square mockSquare;

    private GameMap mockGameMap;

    private static final Path workFolder = Paths.get("./extensions").toAbsolutePath();

    @BeforeAll
    static void fileSetUp() {
        try {
            tearDownAfter();
        } catch (Exception e) {
            System.out.println("No file to delete");
        }
        SnackmanApplication.checkAndCopyResources();
    }

    @AfterAll
    static void tearDownAfter() {
        if (Files.exists(workFolder)) {
            FileSystemUtils.deleteRecursively(workFolder.toFile());
        }
    }


    @BeforeEach
    void setup() {
        mockGameMap = mock(GameMap.class);
        mockSquare = mock(Square.class);

        when(mockGameMap.getSquareAtIndexXZ(anyInt(), anyInt())).thenReturn(mockSquare);

        ghost = new Ghost(mockSquare, 1.0, 1.0, mockGameMap);
    }

    /**
     * Tests that the Ghost correctly scares all mobs (all chicken and snackman) present in its current square.
     */
    @Test
    void testScaresEverythingThatCouldBeEncountered() {
        SnackMan mockSnackMan = mock(SnackMan.class);
        Chicken mockChicken = mock(Chicken.class);
        when(mockSquare.getMobs()).thenReturn(List.of(mockSnackMan, mockChicken));

        ghost.scaresEverythingThatCouldBeEncountered(mockSquare, mockGameMap);

        verify(mockChicken).isScaredFromGhost(true);
    }

    /**
     * Tests that the Ghost moves to a new square and is correctly removed from the old square
     * and added to the new square.
     */
    @Test
    void testMoveAndSwitchSquares() {
        Square mockOldSquare = mock(Square.class);
        Square mockNewSquare = mock(Square.class);
        when(mockGameMap.getSquareAtIndexXZ(anyInt(), anyInt())).thenReturn(mockOldSquare, mockNewSquare);

        ghost.move(true, false, false, false, 0.1, mockGameMap);

        verify(mockOldSquare).removeMob(ghost);
        verify(mockNewSquare).addMob(ghost);
    }

}
