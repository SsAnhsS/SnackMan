import * as THREE from 'three'

export interface IChickenDTD {
  id: number,
  chickenPosX: number,
  chickenPosZ: number,
  thickness: ChickenThickness;
  lookingDirection: Direction
  isScared: boolean
}

export interface IChicken {
  id: number,
  chickenPosX: number,
  chickenPosZ: number,
  thickness: ChickenThickness;
  lookingQuaternion: THREE.Quaternion
  isScared: boolean
  meshId: number
}

export enum ChickenThickness {
  THIN = 1, SLIGHTLY_THICK = 1.25, MEDIUM = 1.5, HEAVY = 1.75, VERY_HEAVY = 2
}

export enum Direction {
  ONE_NORTH, ONE_SOUTH, ONE_EAST, ONE_WEST
}
