import type {LeaderboardEntryDTD} from "@/stores/Leaderboard/LeaderboardDTD";
import {EventType} from "@/stores/messaging/IMessageDTD";

export interface IFrontendLeaderboardEntryMessageEvent {
  eventType: EventType,
  leaderboardEntry: LeaderboardEntryDTD,
}
