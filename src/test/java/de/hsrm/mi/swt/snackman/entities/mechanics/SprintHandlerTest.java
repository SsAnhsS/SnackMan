package de.hsrm.mi.swt.snackman.entities.mechanics;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

import de.hsrm.mi.swt.snackman.SnackmanApplication;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class SprintHandlerTest {

    private SprintHandler sprintHandler;

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
    void setUp() {
        sprintHandler = new SprintHandler();
    }

    @Test
    void testStartSprint() throws InterruptedException {
        sprintHandler.startSprint();

        assertTrue(sprintHandler.canSprint(), "Sprint should have started");
        assertFalse(sprintHandler.isInCooldown(), "Cooldown should not be active when sprinting");

        // Simulate a short wait to reduce sprint time
        Thread.sleep(2000);

        assertTrue(sprintHandler.getSprintTimeLeft() < 5, "Sprint time should decrease after starting sprint");
        assertTrue(sprintHandler.getSprintTimeLeft() > 0, "Sprint time should still be above zero");
    }

    @Test
    void testStopSprint() throws InterruptedException {
        sprintHandler.startSprint();
        Thread.sleep(2000);

        // Stop sprinting
        sprintHandler.stopSprint();

        assertTrue(sprintHandler.isInCooldown(), "After stopping sprint, cooldown should be active");
        assertFalse(sprintHandler.canSprint(), "Cannot sprint during cooldown");
    }
    
    @Test
    void testCooldownLogic() throws InterruptedException {
        sprintHandler.startSprint();
        sprintHandler.stopSprint();
    
        assertTrue(sprintHandler.isInCooldown(), "Cooldown should be active after stopping sprint");
    
        int retries = 0;
        while (sprintHandler.isInCooldown() && retries < 100) { // Maximum 10 seconds (100 x 100ms)
            Thread.sleep(100);
            retries++;
        }
    
        assertFalse(sprintHandler.isInCooldown(), "Cooldown should be over");
        assertTrue(sprintHandler.canSprint(), "Player should be able to sprint again after cooldown");
    }

    @Test
    void testCooldownDoesNotRestartWhileActive() throws InterruptedException {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        sprintHandler.startSprint();
        executor.schedule(() -> sprintHandler.stopSprint(), 2, TimeUnit.SECONDS);

        // First test: Cooldown started
        Thread.sleep(500);
        int initialCooldown = sprintHandler.getCooldownTimeLeft();

        // Attempt to restart
        sprintHandler.startSprint();

        // Check that cooldown time has not been reset
        assertEquals(initialCooldown, sprintHandler.getCooldownTimeLeft(),
                "Cooldown duration should not reset while cooldown is active");

        executor.shutdown();
    }

    @Test
    void testSprintExhaustionTriggersCooldown() throws InterruptedException {
        sprintHandler.startSprint();
        Thread.sleep(6000); // Exceed maximum sprint time (5 seconds)

        assertTrue(sprintHandler.isInCooldown(), "Cooldown should be triggered when sprint time is exhausted");
        assertFalse(sprintHandler.canSprint(), "Cannot sprint while cooldown is active");
    }
}