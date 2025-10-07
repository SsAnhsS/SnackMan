package de.hsrm.mi.swt.snackman.controller.leaderboard;

import de.hsrm.mi.swt.snackman.entities.leaderboard.LeaderboardEntry;

/**
 * Data Transfer Object (DTO) representing an individual leaderboard entry.
 * Encapsulates the name, duration, and release date of a leaderboard entry.
 */
public record LeaderboardEntryDTO(String name, String duration, String releaseDate) {
    public static LeaderboardEntryDTO fromLeaderboardEntry(LeaderboardEntry leaderboardEntry) {
        return new LeaderboardEntryDTO(leaderboardEntry.getName(), leaderboardEntry.getDuration().toString(), leaderboardEntry.getReleaseDate().toString());
    }
}
