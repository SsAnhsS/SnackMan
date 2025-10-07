import * as THREE from "three";

export interface IOtherPlayer {
  model: THREE.Group,
  rotation: THREE.Quaternion,
  targetPosition: THREE.Vector3
}
