import {Direction} from "@/stores/Chicken/IChickenDTD";
import * as THREE from "three";

export interface IScriptGhostDTD {
  id: number,
  scriptGhostPosX: number,
  scriptGhostPosZ: number,
  lookingDirection: Direction
}

export interface IScriptGhost {
  id: number,
  scriptGhostPosX: number,
  scriptGhostPosZ: number,
  lookingQuaternion: THREE.Quaternion,
  meshId: number
}
