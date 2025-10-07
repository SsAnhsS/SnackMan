import type { IPlayerDTD } from "@/stores/Player/IPlayerDTD"

export async function fetchSnackManFromBackend(lobbyId: string, playerId: string): Promise<IPlayerDTD> {
    // rest endpoint from backend
    const response = await fetch(`/api/lobbies/${lobbyId}/player/${playerId}`)
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    return await response.json() as IPlayerDTD
  }
