package de.hsrm.mi.swt.snackman.services;

import de.hsrm.mi.swt.snackman.controller.leaderboard.LeaderboardDTO;
import de.hsrm.mi.swt.snackman.controller.leaderboard.LeaderboardEntryDTO;
import de.hsrm.mi.swt.snackman.entities.leaderboard.LeaderboardEntry;
import de.hsrm.mi.swt.snackman.messaging.FrontendMessageService;
import de.hsrm.mi.swt.snackman.messaging.FrontendLeaderboardEntryMessageEvent;
import de.hsrm.mi.swt.snackman.messaging.EventType;
import de.hsrm.mi.swt.snackman.messaging.ChangeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for {@link LeaderboardService}.
 * This class tests the functionality of the {@link LeaderboardService},
 * including reading leaderboard data, adding new entries, and retrieving leaderboard DTOs.
 */
public class LeaderboardServiceTest {

    private LeaderboardService leaderboardService;
    private FrontendMessageService frontendMessageServiceMock;

    @TempDir
    private Path tempDir;

    /**
     * Sets up the test environment before each test.
     * This includes creating a mock {@link FrontendMessageService}, ensuring a clean temporary file,
     * and initializing the {@link LeaderboardService} with a sample leaderboard file.
     *
     * @throws IOException if there is an error creating or writing to the temporary file.
     */
    @BeforeEach
    void setUp() throws IOException {
        frontendMessageServiceMock = mock(FrontendMessageService.class);
        Path tempFile = tempDir.resolve("leaderboard.txt");
        if (Files.exists(tempFile)) {
            Files.delete(tempFile);
        }
        Files.write(tempFile, List.of(
                "Mulan;02:23;2024-03-04",
                "König der Löwen, Mufasa;01:05;2024-03-02",
                "Biene Maja;00:58;2024-12-10",
                "Livia;00:20;2025-01-01"
        ));

        leaderboardService = new LeaderboardService(frontendMessageServiceMock, tempFile.toAbsolutePath().toString());
    }

    /**
     * Tests the ability of the {@link LeaderboardService} to read leaderboard data from a file.
     * Verifies that the leaderboard is correctly populated and sorted.
     */
    @Test
    void testReadInLeaderboard() {
        LeaderboardDTO dto = leaderboardService.getLeaderboardAsDTO();
        assertEquals(4, dto.leaderboardEntries().size());
        assertEquals("Livia", dto.leaderboardEntries().get(0).name());
        assertEquals("Mulan", dto.leaderboardEntries().get(3).name());
    }

    /**
     * Tests the addition of a new entry to the leaderboard.
     * Verifies that the entry is added to the file, sorted in the leaderboard,
     * and that an event is sent to the frontend.
     *
     * @throws IOException if there is an error reading from the temporary file.
     */
    @Test
    void testAddLeaderboardEntry() throws IOException {
        LeaderboardEntry newEntry = new LeaderboardEntry("Ariel", "00:15", "2025-01-06");
        leaderboardService.addLeaderboardEntry(newEntry);

        // Verify the entry was added and sorted correctly
        LeaderboardDTO dto = leaderboardService.getLeaderboardAsDTO();
        assertEquals(5, dto.leaderboardEntries().size());
        assertEquals("Ariel", dto.leaderboardEntries().getFirst().name());

        // Verify the entry was written to the file
        List<String> lines = Files.readAllLines(tempDir.resolve("leaderboard.txt"));
        assertTrue(lines.stream().anyMatch(line -> line.equals("Ariel;00:15;2025-01-06")));

        // Verify the message was sent to the frontend
        ArgumentCaptor<FrontendLeaderboardEntryMessageEvent> captor = ArgumentCaptor.forClass(FrontendLeaderboardEntryMessageEvent.class);
        verify(frontendMessageServiceMock, times(1)).sendLeaderboardEntryEvent(captor.capture());
        FrontendLeaderboardEntryMessageEvent sentEvent = captor.getValue();
        assertEquals(EventType.LEADERBOARD, sentEvent.eventType());
        assertEquals(ChangeType.UPDATE, sentEvent.changeType());
        assertEquals("Ariel", sentEvent.leaderboardEntry().name());
    }

    /**
     * Tests the conversion of the leaderboard data to a {@link LeaderboardDTO}.
     * Verifies that the DTO contains the correct data and that it matches the leaderboard content.
     */
    @Test
    void testGetLeaderboardAsDTO() {
        LeaderboardDTO dto = leaderboardService.getLeaderboardAsDTO();
        assertNotNull(dto);
        assertEquals(4, dto.leaderboardEntries().size());

        LeaderboardEntryDTO firstEntry = dto.leaderboardEntries().getFirst();
        assertEquals("Livia", firstEntry.name());
        assertEquals("00:20", firstEntry.duration());
        assertEquals("2025-01-01", firstEntry.releaseDate());
    }
}
