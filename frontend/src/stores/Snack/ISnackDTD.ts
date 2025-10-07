export interface ISnackDTD {
  snackType: SnackType
}

export interface ISnack {
  snackType: SnackType
  meshId: number
}

export enum SnackType {
  CHERRY = 'CHERRY',
  STRAWBERRY = 'STRAWBERRY',
  ORANGE = 'ORANGE',
  APPLE = 'APPLE',
  EGG = 'EGG',
  EMPTY = 'EMPTY'
}
