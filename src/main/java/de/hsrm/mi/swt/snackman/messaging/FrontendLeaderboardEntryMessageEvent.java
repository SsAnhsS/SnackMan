package de.hsrm.mi.swt.snackman.messaging;

import de.hsrm.mi.swt.snackman.controller.leaderboard.LeaderboardEntryDTO;

public record FrontendLeaderboardEntryMessageEvent(EventType eventType, ChangeType changeType,
                                                   LeaderboardEntryDTO leaderboardEntry) {

    @Override
    public EventType eventType() {
        return eventType;
    }

    @Override
    public ChangeType changeType() {
        return changeType;
    }

    @Override
    public LeaderboardEntryDTO leaderboardEntry() {
        return leaderboardEntry;
    }
}
