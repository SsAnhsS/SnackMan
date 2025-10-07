import type {ISnack, ISnackDTD} from "@/stores/Snack/ISnackDTD";
import {MapObjectType} from "@/stores/IGameMapDTD";

export interface ISquareDTD {
  id: number,
  indexX: number,
  indexZ: number,
  type: MapObjectType,
  snack: ISnackDTD,
}

export interface ISquare {
  id: number,
  indexX: number,
  indexZ: number,
  type: MapObjectType,
  snack: ISnack,
}


