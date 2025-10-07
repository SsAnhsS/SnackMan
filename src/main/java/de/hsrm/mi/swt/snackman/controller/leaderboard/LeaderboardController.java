package de.hsrm.mi.swt.snackman.controller.leaderboard;

import de.hsrm.mi.swt.snackman.entities.leaderboard.LeaderboardEntry;
import de.hsrm.mi.swt.snackman.services.LeaderboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * REST controller for handling leaderboard-related operations.
 * Provides endpoints to retrieve and update leaderboard data.
 */
@Controller
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final Logger logger = LoggerFactory.getLogger(LeaderboardController.class);
    @Autowired
    private LeaderboardService leaderboardService;

    /**
     * Retrieves the current leaderboard as a DTO.
     *
     * @return a {@link ResponseEntity} containing the leaderboard data
     */
    @GetMapping("")
    public ResponseEntity<LeaderboardDTO> getLeaderboard() {
        logger.info("Retrieving the leaderboard: {}", leaderboardService.getLeaderboardAsDTO());

        return ResponseEntity.ok(leaderboardService.getLeaderboardAsDTO());
    }

    /**
     * Creates a new leaderboard entry based on the provided request body.
     *
     * @param requestBody a map containing the "name", "duration", and "releaseDate" of the new entry
     * @return a {@link ResponseEntity} indicating the operation's success
     */
    @PostMapping("/new/entry")
    public ResponseEntity<Void> createNewLeaderboardEntry(@RequestBody Map<String, String> requestBody) {
        String name = requestBody.get("name");
        String duration = requestBody.get("duration");
        String releaseDate = requestBody.get("releaseDate");

        LeaderboardEntry newEntry = new LeaderboardEntry(name, duration, releaseDate);
        this.leaderboardService.addLeaderboardEntry(newEntry);
        logger.info("Creating new a leaderboard entry: {}, leaderboard now {}", newEntry, leaderboardService.getLeaderboardAsDTO());

        return ResponseEntity.ok().build();
    }
}
