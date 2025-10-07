export interface IGameEndDTD {
  role: ROLE
  timePlayed: number;
  kcalCollected: number
  lobbyId: string
}

export enum ROLE {
  SNACKMAN = "SNACKMAN", GHOST = "GHOST"
}
