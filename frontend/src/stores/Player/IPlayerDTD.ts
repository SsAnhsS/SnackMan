export interface IPlayerDTD {
  posX: number
  posY: number
  posZ: number
  qX: number
  qY: number
  qZ: number
  qW: number
  radius: number
  speed: number
  playerId: string
  sprintTimeLeft: number
  isSprinting: boolean
  isInCooldown: boolean
  sprintMultiplier: number
  maxCalories: number
  currentCalories?: number
  message?: string
}
