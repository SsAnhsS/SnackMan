import type {IGameMapDTD} from "@/stores/IGameMapDTD";

/**
 * fetches game map data from backend, sends a GET request to '/api/game-map'
 * and returns the parsed JSON data as an IGameMapDTD object
 */
export async function fetchGameMapDataFromBackend(lobbyId: string): Promise<IGameMapDTD> {
  // rest endpoint from backend
  const response = await fetch(`/api/lobby/${lobbyId}/game-map`)
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`)
  }
  return await response.json() as IGameMapDTD
}
