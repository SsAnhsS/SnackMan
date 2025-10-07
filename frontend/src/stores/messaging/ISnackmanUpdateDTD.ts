import * as THREE from "three";

export interface ISnackmanUpdateDTD {
  calories: number,
  position: THREE.Vector3,
  rotation: THREE.Quaternion,
  radius: number,
  speed: number,
  playerId: string,
  sprintTimeLeft: number,
  isSprinting: boolean,
  isInCooldown: boolean,
  message: string,
  isScared: boolean
}
