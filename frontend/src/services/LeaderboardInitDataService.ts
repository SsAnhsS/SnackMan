import type {LeaderboardDTD} from "@/stores/Leaderboard/LeaderboardEntryDTD";

/**
 * fetches the leaderboard data from backend, sends a GET request to '/api/leaderboard'
 * and returns the parsed JSON data as a LeaderboardDTD object
 */
export async function fetchLeaderboardDataFromBackend(): Promise<LeaderboardDTD> {
  const response = await fetch('/api/leaderboard')
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`)
  }
  return await response.json()
}
