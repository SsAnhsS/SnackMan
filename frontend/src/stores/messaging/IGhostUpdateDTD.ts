import * as THREE from "three";

export interface IGhostUpdateDTD {
  position: THREE.Vector3,
  rotation: THREE.Quaternion,
  radius: number,
  speed: number,
  playerId: string
}
