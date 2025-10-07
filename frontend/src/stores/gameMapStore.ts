import {defineStore} from 'pinia';
import {reactive, readonly} from "vue";
import type {IGameMap, IGameMapDTD} from './IGameMapDTD';
import {fetchGameMapDataFromBackend} from "../services/GameMapDataService.js";
import {Client} from "@stomp/stompjs";
import type {ISquare} from "@/stores/Square/ISquareDTD";
import * as THREE from "three";
import {Scene} from "three";
import type {IChicken, IChickenDTD} from "@/stores/Chicken/IChickenDTD";
import {ChickenThickness, Direction} from "@/stores/Chicken/IChickenDTD";
import {GameObjectRenderer} from "@/renderer/GameObjectRenderer";
import {useLobbiesStore} from "@/stores/Lobby/lobbiesstore";
import {Player} from '@/components/Player';
import {EventType, type IMessageDTD} from './messaging/IMessageDTD';
import type {ISnackmanUpdateDTD} from './messaging/ISnackmanUpdateDTD';
import type {ISquareUpdateDTD} from './messaging/ISquareUpdateDTD';
import {SnackType} from './Snack/ISnackDTD';
import type {IScriptGhost, IScriptGhostDTD} from "@/stores/Ghost/IScriptGhostDTD";
import type {IGhostUpdateDTD} from "@/stores/messaging/IGhostUpdateDTD";
import {useRouter} from "vue-router";
import type {IGameEndDTD} from "@/stores/GameEnd/IGameEndDTD";
import {SoundManager} from "@/services/SoundManager";
import {SoundType} from "@/services/SoundTypes";
import type {IOtherPlayer} from './IOtherPlayer';

/**
 * Defines the pinia store used for saving the map from
 * the backend. Updates of the snacks are saved here and
 * updated in the store itself. It holds the scene on
 * which the game is build up on.
 */
export const useGameMapStore = defineStore('gameMap', () => {
  const protocol = window.location.protocol.replace('http', 'ws')
  const wsurl = `${protocol}//${window.location.host}/ws`
  let stompclient = new Client({brokerURL: wsurl})
  const scene = new THREE.Scene()
  const gameObjectRenderer = GameObjectRenderer()
  const {lobbydata} = useLobbiesStore()
  let player: Player
  let otherPlayers: Map<String, IOtherPlayer>
  let OFFSET: number
  let DEFAULT_SIDE_LENGTH: number
  const router = useRouter();

  const mapData = reactive({
    DEFAULT_SQUARE_SIDE_LENGTH: 0,
    DEFAULT_WALL_HEIGHT: 0,
    gameMap: new Map<number, ISquare>(),
    chickens: [],
    scriptGhosts: []
  } as IGameMap);

  async function initGameMap() {
    try {
      const response: IGameMapDTD = await fetchGameMapDataFromBackend(lobbydata.currentPlayer.joinedLobbyId!)
      mapData.DEFAULT_SQUARE_SIDE_LENGTH = response.DEFAULT_SQUARE_SIDE_LENGTH
      mapData.DEFAULT_WALL_HEIGHT = response.DEFAULT_WALL_HEIGHT

      OFFSET = mapData.DEFAULT_SQUARE_SIDE_LENGTH / 2
      DEFAULT_SIDE_LENGTH = mapData.DEFAULT_SQUARE_SIDE_LENGTH

      mapData.chickens = []
      mapData.scriptGhosts = []
      mapData.gameMap = new Map<number, ISquare>()

      for (const square of response.gameMap) {
        mapData.gameMap.set(square.id, square as ISquare)
      }

      for (const chicken of response.chickens) {
        const iChicken = {} as IChicken
        iChicken.chickenPosX = chicken.chickenPosX * DEFAULT_SIDE_LENGTH + OFFSET
        iChicken.chickenPosZ = chicken.chickenPosZ * DEFAULT_SIDE_LENGTH + OFFSET
        iChicken.id = chicken.id
        iChicken.isScared = chicken.isScared
        iChicken.thickness = chicken.thickness
        iChicken.lookingQuaternion = new THREE.Quaternion()
        iChicken.lookingQuaternion.setFromAxisAngle(new THREE.Vector3(0, 1, 0), 0)

        mapData.chickens.push(iChicken)
      }

      for (const ghost of response.scriptGhosts) {
        const iGhost = {} as IScriptGhost
        iGhost.scriptGhostPosX = ghost.scriptGhostPosX * DEFAULT_SIDE_LENGTH + OFFSET
        iGhost.scriptGhostPosZ = ghost.scriptGhostPosZ * DEFAULT_SIDE_LENGTH + OFFSET
        iGhost.id = ghost.id
        iGhost.lookingQuaternion = new THREE.Quaternion()
        iGhost.lookingQuaternion.setFromAxisAngle(new THREE.Vector3(0, 1, 0), 0)
        mapData.scriptGhosts.push(iGhost)
      }
    } catch (reason) {
      throw reason //Throw again to pass to execution function
    }
  }

  function startGameMapLiveUpdate() {

    const DEST_UPDATES = `/topic/lobbies/${lobbydata.currentPlayer.joinedLobbyId}/update`
    if (!stompclient.active) {

      stompclient.onWebSocketError = (event) => {
        throw new Error('Websocket wit message: ' + event)
      }

      stompclient.onStompError = (frameElement) => {
        throw new Error('Stompclient with message: ' + frameElement)
      }

      stompclient.onConnect = (frameElement) => {
        console.log('Stompclient connected')

        stompclient.subscribe(DEST_UPDATES, message => {
          const content: Array<IMessageDTD> = JSON.parse(message.body)
          for (const mess of content) {
            switch (mess.event) {
              case EventType.GameEnd:
                const gameEndUpdate: IGameEndDTD = mess.message
                endGame(gameEndUpdate, lobbydata.currentPlayer.joinedLobbyId!)
                break;
              case EventType.SnackManUpdate:
                const mobUpdate: ISnackmanUpdateDTD = mess.message

                //play sound for ghost and snackman
                if (mobUpdate.isScared) {
                  SoundManager.playSound(SoundType.GHOST_SCARES_SNACKMAN)
                }

                if (mobUpdate.playerId === lobbydata.currentPlayer.playerId) {
                  if (player == undefined) {
                    continue;
                  }

                  player.setCalories(mobUpdate.calories)

                  player.sprintData.sprintTimeLeft = mobUpdate.sprintTimeLeft
                  player.sprintData.isSprinting = mobUpdate.isSprinting
                  player.sprintData.isCooldown = mobUpdate.isInCooldown

                  if (mobUpdate.message != null) {
                    player.message.value = mobUpdate.message
                  }

                  player.setPosition(mobUpdate.position);
                } else {
                  if (otherPlayers == undefined || otherPlayers.size == 0) {
                    continue;
                  }
                  otherPlayers.get(mobUpdate.playerId)!.rotation.set(mobUpdate.rotation.x, mobUpdate.rotation.y, mobUpdate.rotation.z, mobUpdate.rotation.w)
                  //TODO adjust player height
                  otherPlayers.get(mobUpdate.playerId)!.targetPosition.set(mobUpdate.position.x, mobUpdate.position.y - 2, mobUpdate.position.z)
                }
                break;

              case EventType.GhostUpdate:
                const ghostUpdate: IGhostUpdateDTD = mess.message
                if (ghostUpdate.playerId === lobbydata.currentPlayer.playerId) {
                  if (player == undefined) {
                    continue;
                  }
                  player.setPosition(ghostUpdate.position);
                  break;
                } else {
                  if (otherPlayers == undefined || otherPlayers.size == 0) {
                    continue;
                  }
                  otherPlayers.get(ghostUpdate.playerId)!.rotation.set(ghostUpdate.rotation.x, ghostUpdate.rotation.y, ghostUpdate.rotation.z, ghostUpdate.rotation.w)
                  otherPlayers.get(ghostUpdate.playerId)!.targetPosition.set(ghostUpdate.position.x, ghostUpdate.position.y - 2, ghostUpdate.position.z)

                }
                break;
              case EventType.SquareUpdate:
                const squareUpdate: ISquareUpdateDTD = mess.message
                if (squareUpdate.square.snack.snackType == SnackType.EMPTY) {
                  const savedMeshId = mapData.gameMap.get(squareUpdate.square.id)!.snack.meshId
                  removeMeshFromScene(scene, savedMeshId)
                  mapData.gameMap.set(squareUpdate.square.id, squareUpdate.square)
                } else {
                  spawnSnack(squareUpdate)
                }
                break;
              case EventType.ChickenUpdate:
                const chickenUpdate: IChickenDTD = mess.message

                updateChicken(chickenUpdate)
                if (chickenUpdate.isScared) {
                  SoundManager.playSound(SoundType.GHOST_SCARES_CHICKEN)
                }
                break;
              case EventType.ScriptGhostUpdate:
                const scriptGhostUpdate: IScriptGhostDTD = mess.message

                updateScriptGhost(scriptGhostUpdate)
                break;
              default:
                console.error(mess.message)
            }
          }
        })
      }

      stompclient.onDisconnect = () => {
        console.log('Stompclient disconnected.')
      }

      stompclient.activate()
    }
  }

  /**
   * Handles the end of a game and navigates to the GameEnd view with relevant details.
   *
   * @param gameEndUpdate - Contains the details about the game end, including the winning role,
   *                        the time played, and the calories collected during the game.
   */
  function endGame(gameEndUpdate: IGameEndDTD, lobbyId: string) {
    router.push({
      name: 'GameEnd',
      query: {
        winningRole: gameEndUpdate.role,
        timePlayed: gameEndUpdate.timePlayed,
        kcalCollected: gameEndUpdate.kcalCollected,
        lobbyId: gameEndUpdate.lobbyId
      }
    }).then(r => {
        stompclient.deactivate()
        mapData.DEFAULT_SQUARE_SIDE_LENGTH = 0
        mapData.DEFAULT_WALL_HEIGHT = 0
        mapData.scriptGhosts = []
        mapData.chickens = []
        mapData.gameMap = new Map<number, ISquare>()
        for (let i = scene.children.length - 1; i >= 0; i--) {
          scene.remove(scene.children[i])
        }
        const lobby = lobbydata.lobbies.find(l => l.lobbyId === lobbyId)
      if (lobby) {
        for (const member of lobby.members) {
          if (member.playerId === lobbydata.currentPlayer.playerId) {
            member.role = 'UNDEFINED'
          }
        }
      }
        lobbydata.currentPlayer.joinedLobbyId = ""

        SoundManager.playSound(SoundType.GAME_END)
      }
    )
  }

  function updateChicken(change: IChickenDTD) {
    const chickenUpdate: IChickenDTD = change
    const currentChicken = mapData.chickens.find(chicken => chicken.id == chickenUpdate.id)
    if (currentChicken == undefined) {
      console.error("A chicken is undefined in pinia")
    } else {
      if (currentChicken.thickness != chickenUpdate.thickness) {
        updateThickness(currentChicken, chickenUpdate)
      }
      updateLookingDirection(currentChicken, chickenUpdate)
      updateWalkingDirection(currentChicken, chickenUpdate, DEFAULT_SIDE_LENGTH, OFFSET)
    }
  }

  function updateScriptGhost(change: IScriptGhostDTD) {
    const scriptGhostUpdate: IScriptGhostDTD = change
    const currentScriptGhost = mapData.scriptGhosts.find(scriptGhost => scriptGhost.id == scriptGhostUpdate.id)
    if (currentScriptGhost == undefined) {
      console.error("A script ghost is undefined in pinia")
    } else {
      updateLookingDirectionScriptGhost(currentScriptGhost, scriptGhostUpdate)
      updateWalkingDirectionScriptGhost(currentScriptGhost, scriptGhostUpdate, DEFAULT_SIDE_LENGTH, OFFSET)
    }
  }

  async function spawnSnack(squareUpdate: ISquareUpdateDTD) {
    const savedMeshId = mapData.gameMap.get(squareUpdate.square.id)!.snack.meshId
    removeMeshFromScene(scene, savedMeshId)
    mapData.gameMap.set(squareUpdate.square.id, squareUpdate.square)

    const snackToAdd = await gameObjectRenderer.createSnackOnFloor(
      squareUpdate.square.indexX * DEFAULT_SIDE_LENGTH + OFFSET,
      squareUpdate.square.indexZ * DEFAULT_SIDE_LENGTH + OFFSET,
      0, // yPosition für Snacks (z. B. Bodenhöhe)
      DEFAULT_SIDE_LENGTH,
      squareUpdate.square.snack?.snackType,
    )
    scene.add(snackToAdd)
    setSnackMeshId(squareUpdate.square.id, snackToAdd.id)
  }

  function setPlayer(p: Player) {
    player = p
  }

  function setOtherPlayers(other: Map<String, IOtherPlayer>) {
    otherPlayers = other
  }

  function getOtherPlayers() {
    return otherPlayers
  }

  function updateThickness(
    currentChicken: IChicken,
    chickenUpdate: IChickenDTD,
  ) {
    const chickenMesh = scene.getObjectById(currentChicken.meshId)
    currentChicken.thickness = chickenUpdate.thickness

    if (!chickenMesh) {
      console.warn('Chicken mesh not found in the scene.')
      return
    }

    const thicknessValue =
      ChickenThickness[chickenUpdate.thickness as unknown as keyof typeof ChickenThickness]

    switch (thicknessValue) {
      case ChickenThickness.THIN:
        chickenMesh!.scale.set(ChickenThickness.THIN, ChickenThickness.THIN, ChickenThickness.THIN)
        break
      case ChickenThickness.SLIGHTLY_THICK:
        chickenMesh!.scale.set(ChickenThickness.SLIGHTLY_THICK * 1.2, ChickenThickness.SLIGHTLY_THICK, ChickenThickness.SLIGHTLY_THICK * 1.2)
        break
      case ChickenThickness.MEDIUM:
        chickenMesh!.scale.set(ChickenThickness.MEDIUM * 1.4, ChickenThickness.MEDIUM, ChickenThickness.MEDIUM * 1.4)
        break
      case ChickenThickness.HEAVY:
        chickenMesh!.scale.set(ChickenThickness.HEAVY * 1.7, ChickenThickness.HEAVY, ChickenThickness.HEAVY * 1.7)
        break
      case ChickenThickness.VERY_HEAVY:
        chickenMesh!.scale.set(ChickenThickness.VERY_HEAVY * 1.9, ChickenThickness.VERY_HEAVY, ChickenThickness.VERY_HEAVY * 1.9)
        break
      default:
        console.warn('ETWAS IST SCHIED GELAUFEN...')
    }
  }

  function updateLookingDirection(
    currentChicken: IChicken,
    chickenUpdate: IChickenDTD,
  ) {
    const lookingDir = Direction[chickenUpdate.lookingDirection as unknown as keyof typeof Direction]
    switch (
      lookingDir // rotates the chicken depending on what its looking direction is
      ) {
      case Direction.ONE_NORTH:
        currentChicken.lookingQuaternion.setFromAxisAngle(new THREE.Vector3(0, 1, 0), Math.PI / 2)
        break;
      case Direction.ONE_SOUTH:
        currentChicken.lookingQuaternion.setFromAxisAngle(new THREE.Vector3(0, 1, 0), (3 * Math.PI) / 2)
        break;
      case Direction.ONE_EAST:
        currentChicken.lookingQuaternion.setFromAxisAngle(new THREE.Vector3(0, 1, 0), Math.PI)
        break
      case Direction.ONE_WEST:
        currentChicken.lookingQuaternion.setFromAxisAngle(new THREE.Vector3(0, 1, 0), 0)
        break
    }
  }

  function updateLookingDirectionScriptGhost(currentScriptGhost: IScriptGhost, scriptGhostUpdate: IScriptGhostDTD) {
    const lookDir = Direction[scriptGhostUpdate.lookingDirection as unknown as keyof typeof Direction]
    switch (lookDir) {
      case Direction.ONE_NORTH:
        currentScriptGhost.lookingQuaternion.setFromAxisAngle(new THREE.Vector3(0, 1, 0), Math.PI / 2)
        break;
      case Direction.ONE_SOUTH:
        currentScriptGhost.lookingQuaternion.setFromAxisAngle(new THREE.Vector3(0, 1, 0), (3 * Math.PI) / 2)
        break;
      case Direction.ONE_EAST:
        currentScriptGhost.lookingQuaternion.setFromAxisAngle(new THREE.Vector3(0, 1, 0), Math.PI)
        break
      case Direction.ONE_WEST:
        currentScriptGhost.lookingQuaternion.setFromAxisAngle(new THREE.Vector3(0, 1, 0), 0)
        break
    }
  }

  function updateWalkingDirection(
    currentChicken: IChicken,
    chickenUpdate: IChickenDTD,
    DEFAULT_SIDE_LENGTH: number,
    OFFSET: number,
  ) {
    currentChicken.chickenPosX = chickenUpdate.chickenPosX * DEFAULT_SIDE_LENGTH + OFFSET
    currentChicken.chickenPosZ = chickenUpdate.chickenPosZ * DEFAULT_SIDE_LENGTH + OFFSET
  }

  function updateWalkingDirectionScriptGhost(currentScriptGhost: IScriptGhost, scriptGhostUpdate: IScriptGhostDTD, DEFAULT_SIDE_LENGTH: number, OFFSET: number) {
    currentScriptGhost.scriptGhostPosX = scriptGhostUpdate.scriptGhostPosX * DEFAULT_SIDE_LENGTH + OFFSET
    currentScriptGhost.scriptGhostPosZ = scriptGhostUpdate.scriptGhostPosZ * DEFAULT_SIDE_LENGTH + OFFSET
  }

  function setSnackMeshId(squareId: number, meshId: number) {
    const square = mapData.gameMap.get(squareId)
    if (square != undefined && square.snack.snackType != SnackType.EMPTY)
      square.snack.meshId = meshId
  }

  function setChickenMeshId(meshId: number, chickenId: number) {
    const chicken = mapData.chickens.find(chicken => chicken.id === chickenId)
    if (chicken != undefined) chicken.meshId = meshId
  }

  function setScriptGhostMeshId(meshId: number, ghostId: number) {
    const ghost = mapData.scriptGhosts.find(ghost => ghost.id === ghostId);
    if (ghost != undefined)
      ghost.meshId = meshId
  }

  function removeMeshFromScene(scene: Scene, meshId: number) {
    const mesh = scene.getObjectById(meshId)
    if (mesh != undefined) {
      scene.remove(mesh!)
    }
  }

  function getScene() {
    return scene
  }

  return {
    mapContent: readonly(mapData as IGameMap),
    initGameMap,
    startGameMapLiveUpdate,
    setSnackMeshId,
    setChickenMeshId,
    getScene,
    setPlayer,
    setOtherPlayers,
    getOtherPlayers,
    stompclient: stompclient,
    setScriptGhostMeshId,
  };
})
