package de.hsrm.mi.swt.snackman.entities.mobileObjects.eatingMobs.Chicken;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

import de.hsrm.mi.swt.snackman.SnackmanApplication;

//TODO Talachicken cannot be tested, because not the right script is loaded. To fix that, we need to copy the
// talachicken script into extension folder
public class TalaChickenTest {

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
    static void tearDownAfter() {
        if (Files.exists(workFolder)) {
            FileSystemUtils.deleteRecursively(workFolder.toFile());
        }
    }

    @Test
    public void talaChickenGoesToEmptySpace(){
        Chicken chicken = new Chicken("TalaChickenMovementSkript");

        List<String> visibleEnvironment = List.of("W", "W", "W", "L", "W",
                                                    "L", "L", "L", "L", "L",
                                                    "L", "W", "H", "W", "L",
                                                    "L", "W", "W", "W", "L",
                                                    "L", "W", "L", "W", "L");

        int result = chicken.executeMovementSkript(visibleEnvironment);

        assertEquals( 0 , result,
                "The Chicken should move to the empty space (' ') matching its new direction.");
    }

    @Test
    public void talaChickenGoesToEmptySpace1(){
        Chicken chicken = new Chicken("TalaChickenMovementSkript");

        List<String> visibleEnvironment = List.of("W", "W", "W", "L", "W",
                                                    "L", "L", "W", "L", "L",
                                                    "L", "W", "H", "L", "L",
                                                    "L", "W", "W", "W", "L",
                                                    "L", "W", "L", "W", "L");

        int result = chicken.executeMovementSkript(visibleEnvironment);

        assertEquals( 1 , result,
                "The Chicken should move to the empty space (' ') matching its new direction.");
    }

    @Test
    public void talaChickenGoesToEmptySpace2(){
        Chicken chicken = new Chicken("TalaChickenMovementSkript");

        List<String> visibleEnvironment = List.of("W", "W", "W", "L", "W",
                                                    "L", "L", "W", "L", "L",
                                                    "L", "W", "H", "W", "L",
                                                    "L", "W", "L", "W", "L",
                                                    "L", "W", "L", "W", "L");

        int result = chicken.executeMovementSkript(visibleEnvironment);

        assertEquals( 2 , result,
                "The Chicken should move to the empty space (' ') matching its new direction.");
    }

    @Test
    public void talaChickenGoesToEmptySpace3(){
        Chicken chicken = new Chicken("TalaChickenMovementSkript");

        List<String> visibleEnvironment = List.of("W", "W", "W", "L", "W",
                                                    "L", "L", "W", "L", "L",
                                                    "L", "L", "H", "W", "L",
                                                    "L", "W", "W", "W", "L",
                                                    "L", "W", "L", "W", "L");

        int result = chicken.executeMovementSkript(visibleEnvironment);

        assertEquals( 3 , result,
                "The Chicken should move to the empty space (' ') matching its new direction.");
    }

    @Test
    public void talaChickenGoesToSnackMan(){
        Chicken chicken = new Chicken("TalaChickenMovementSkript");

        List<String> visibleEnvironment = List.of("W", "W", "W", "L", "W",
                                                    "L", "L", "SM", "L", "L",
                                                    "L", "L", "H", "L", "L",
                                                    "L", "L", "L", "L", "L",
                                                    "L", "W", "L", "W", "L");

        int result = chicken.executeMovementSkript(visibleEnvironment);

        assertEquals( 0 , result,
                "The Chicken should move to the empty space (' ') matching its new direction.");
    }

    @Test
    public void talaChickenGoesToSnackMan2(){
        Chicken chicken = new Chicken("TalaChickenMovementSkript");

        List<String> visibleEnvironment = List.of("W", "W", "W", "L", "W",
                                                    "L", "L", "L", "L", "L",
                                                    "L", "L", "H", "SM", "L",
                                                    "L", "L", "L", "L", "L",
                                                    "L", "W", "L", "W", "L");

        int result = chicken.executeMovementSkript(visibleEnvironment);

        assertEquals( 1 , result,
                "The Chicken should move to the empty space (' ') matching its new direction.");
    }

    @Test
    public void talaChickenGoesToSnackMan3(){
        Chicken chicken = new Chicken("TalaChickenMovementSkript");

        List<String> visibleEnvironment = List.of("W", "W", "W", "L", "W",
                                                    "L", "L", "L", "L", "L",
                                                    "L", "L", "H", "L", "L",
                                                    "L", "L", "SM", "L", "L",
                                                    "L", "W", "L", "W", "L");

        int result = chicken.executeMovementSkript(visibleEnvironment);

        assertEquals( 2 , result,
                "The Chicken should move to the empty space (' ') matching its new direction.");
    }

    @Test
    public void talaChickenGoesToSnackMan4(){
        Chicken chicken = new Chicken("TalaChickenMovementSkript");

        List<String> visibleEnvironment = List.of("W", "W", "W", "L", "W",
                                                    "L", "L", "L", "L", "L",
                                                    "L", "SM", "H", "L", "L",
                                                    "L", "L", "L", "L", "L",
                                                    "L", "W", "L", "W", "L");

        int result = chicken.executeMovementSkript(visibleEnvironment);

        assertEquals( 3 , result,
                "The Chicken should move to the empty space (' ') matching its new direction.");
    }

    @Test
    public void talaChickenAvoidsGhost(){
        Chicken chicken = new Chicken("TalaChickenMovementSkript");

        List<String> visibleEnvironment = List.of("W", "W", "W", "L", "W",
                                                    "L", "L", "G", "L", "L",
                                                    "L", "L", "H", "L", "L",
                                                    "L", "L", "L", "L", "L",
                                                    "L", "W", "L", "W", "L");

        int result = chicken.executeMovementSkript(visibleEnvironment);

        assertEquals( 2 , result,
                "The Chicken should move to the empty space (' ') matching its new direction.");
    }

    @Test
    public void talaChickenAvoidsGhost2(){
        Chicken chicken = new Chicken("TalaChickenMovementSkript");

        List<String> visibleEnvironment = List.of("W", "W", "W", "L", "W",
                                                    "L", "L", "L", "L", "L",
                                                    "L", "L", "H", "G", "L",
                                                    "L", "L", "L", "L", "L",
                                                    "L", "W", "L", "W", "L");

        int result = chicken.executeMovementSkript(visibleEnvironment);

        assertEquals( 3 , result,
                "The Chicken should move to the empty space (' ') matching its new direction.");
    }

    @Test
    public void talaChickenAvoidsGhost3(){
        Chicken chicken = new Chicken("TalaChickenMovementSkript");

        List<String> visibleEnvironment = List.of("W", "W", "W", "L", "W",
                                                    "L", "L", "L", "L", "L",
                                                    "L", "L", "H", "L", "L",
                                                    "L", "L", "G", "L", "L",
                                                    "L", "W", "L", "W", "L");

        int result = chicken.executeMovementSkript(visibleEnvironment);

        assertEquals( 0 , result,
                "The Chicken should move to the empty space (' ') matching its new direction.");
    }

    @Test
    public void talaChickenAvoidsGhost4(){
        Chicken chicken = new Chicken("TalaChickenMovementSkript");

        List<String> visibleEnvironment = List.of("W", "W", "W", "L", "W",
                                                    "L", "L", "L", "L", "L",
                                                    "L", "G", "H", "L", "L",
                                                    "L", "L", "L", "L", "L",
                                                    "L", "W", "L", "W", "L");

        int result = chicken.executeMovementSkript(visibleEnvironment);

        assertEquals( 1 , result,
                "The Chicken should move to the empty space (' ') matching its new direction.");
    }
 
}
