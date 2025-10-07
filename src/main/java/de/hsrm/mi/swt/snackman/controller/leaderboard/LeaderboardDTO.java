package de.hsrm.mi.swt.snackman.controller.leaderboard;

import de.hsrm.mi.swt.snackman.entities.leaderboard.Leaderboard;

import java.util.List;

/**
 * Data Transfer Object (DTO) for representing a leaderboard.
 * This record encapsulates a list of {@link LeaderboardEntryDTO} objects.
 */
public record LeaderboardDTO(List<LeaderboardEntryDTO> leaderboardEntries) {
    public static LeaderboardDTO fromLeaderboardDTO(Leaderboard leaderboard) {
        return new LeaderboardDTO(leaderboard.getLeaderboard().stream()
                .map(LeaderboardEntryDTO::fromLeaderboardEntry)
                .toList());
    }
}
