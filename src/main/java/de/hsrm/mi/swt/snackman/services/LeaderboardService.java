package de.hsrm.mi.swt.snackman.services;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.hsrm.mi.swt.snackman.controller.leaderboard.LeaderboardDTO;
import de.hsrm.mi.swt.snackman.controller.leaderboard.LeaderboardEntryDTO;
import de.hsrm.mi.swt.snackman.entities.leaderboard.Leaderboard;
import de.hsrm.mi.swt.snackman.entities.leaderboard.LeaderboardEntry;
import de.hsrm.mi.swt.snackman.messaging.ChangeType;
import de.hsrm.mi.swt.snackman.messaging.EventType;
import de.hsrm.mi.swt.snackman.messaging.FrontendLeaderboardEntryMessageEvent;
import de.hsrm.mi.swt.snackman.messaging.FrontendMessageService;

/**
 * Service class for managing the leaderboard.
 * <p>
 * This class is responsible for reading, updating, and providing access
 * to leaderboard data stored in a CSV-formatted file. It also handles
 * communication with the frontend via messaging
 */
@Service
public class LeaderboardService {
    public static final String CSV_LINE_SPLITTER = ";";
    private final String filePath;
    private final Leaderboard leaderboard = new Leaderboard();
    Logger log = LoggerFactory.getLogger(MapService.class);
    private FrontendMessageService frontendMessageService;

    @Autowired
    public LeaderboardService(FrontendMessageService frontendMessageService) {
        this.frontendMessageService = frontendMessageService;
        this.filePath = "./extensions/leaderboard.txt";

        List<String> lines = readInLeaderboard();
        fillLeaderboard(lines);
        log.info("Leaderboard loaded: {}", leaderboard);
    }

    public LeaderboardService(FrontendMessageService frontendMessageService, String filePath) {
        this.frontendMessageService = frontendMessageService;
        this.filePath = filePath;
        List<String> lines = readInLeaderboard();
        fillLeaderboard(lines);
    }

    /**
     * Reads leaderboard data from the file specified by the filePath.
     *
     * @return a list of strings representing the lines in the file
     * @throws RuntimeException if the file cannot be read or is empty
     */
    private List<String> readInLeaderboard() {
        List<String> lines = new ArrayList<>();
        String line;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read the " + filePath + " file.", e);
        }

        if (lines.isEmpty()) {
            throw new RuntimeException(filePath + " is empty.");
        }
        return lines;
    }

    /**
     * Fills the in-memory leaderboard with data parsed from the provided lines.
     * Each line is expected to be in CSV format: name;duration;releaseDate.
     *
     * @param lines the lines from the leaderboard file
     * @throws RuntimeException if a line is invalid or does not conform to the expected format
     */
    private void fillLeaderboard(List<String> lines) {
        for (String line : lines) {
            String[] parts = line.split(CSV_LINE_SPLITTER);
            if (parts.length != 3)
                throw new RuntimeException("Invalid CSV line: " + line + " at " + filePath + " file.");
            this.leaderboard.addEntry(new LeaderboardEntry(parts[0], parts[1], parts[2]));
        }
        Collections.sort(this.leaderboard.getLeaderboard());
    }

    /**
     * Adds a new entry to the leaderboard and updates the file and frontend.
     * <p>
     * The entry is added to the in-memory leaderboard, saved to the file, and a message
     * is sent to the frontend to notify about the update.
     *
     * @param leaderboardEntry the new leaderboard entry to add
     */
    public void addLeaderboardEntry(LeaderboardEntry leaderboardEntry) {
        // add to list
        this.leaderboard.addEntry(leaderboardEntry);
        Collections.sort(this.leaderboard.getLeaderboard());
        // add to file
        String newLine = leaderboardEntry.getEntryAsFileLine();
        try (FileWriter fileWriter = new FileWriter(this.filePath, true)) {
            fileWriter.write(newLine);
        } catch (IOException e) {
            log.error("Failed to write a new entry to {} file.", this.filePath, e);
        }
        // stomp
        FrontendLeaderboardEntryMessageEvent message = new FrontendLeaderboardEntryMessageEvent(EventType.LEADERBOARD, ChangeType.UPDATE, LeaderboardEntryDTO.fromLeaderboardEntry(leaderboardEntry));
        this.frontendMessageService.sendLeaderboardEntryEvent(message);

        log.info("Leaderboard was updated: {}", leaderboard);
    }

    /**
     * Converts the in-memory leaderboard into a {@link LeaderboardDTO}.
     *
     * @return a {@link LeaderboardDTO} representing the current state of the leaderboard
     */
    public LeaderboardDTO getLeaderboardAsDTO() {
        return LeaderboardDTO.fromLeaderboardDTO(leaderboard);
    }
}
