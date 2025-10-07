export interface IMessageDTD {
  event: EventType,
  message: any
}

export enum EventType {
  SnackManUpdate = "SnackManUpdate", SquareUpdate = "SquareUpdate",
  ChickenUpdate = "ChickenUpdate", GhostUpdate = "GhostUpdate",
  ScriptGhostUpdate = "ScriptGhostUpdate", GameEnd = "GameEnd"
}
