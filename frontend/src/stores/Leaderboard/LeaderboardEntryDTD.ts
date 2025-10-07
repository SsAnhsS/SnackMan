import type {LeaderboardEntry, LeaderboardEntryDTD} from "@/stores/Leaderboard/LeaderboardDTD";

export interface LeaderboardDTD {
  leaderboardEntries: LeaderboardEntryDTD[]
}

export interface Leaderboard {
  leaderboardEntries: LeaderboardEntry[]
}
