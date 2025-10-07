package de.hsrm.mi.swt.snackman.messaging;

import de.hsrm.mi.swt.snackman.controller.leaderboard.LeaderboardDTO;
import de.hsrm.mi.swt.snackman.controller.leaderboard.LeaderboardEntryDTO;

import java.util.List;

public record FrontendLeaderboardMessageEvent(EventType eventType, ChangeType changeType,
                                              LeaderboardDTO leaderboardDTO) {

    @Override
    public EventType eventType() {
        return eventType;
    }

    @Override
    public ChangeType changeType() {
        return changeType;
    }

    @Override
    public LeaderboardDTO leaderboardDTO() {
        return leaderboardDTO;
    }
}
