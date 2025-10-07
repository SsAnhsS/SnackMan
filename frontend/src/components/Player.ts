import * as THREE from 'three'
import {type WebGLRenderer} from 'three'
import {PointerLockControls} from 'three/addons/controls/PointerLockControls.js'
import {reactive, ref, type UnwrapNestedRefs} from "vue";
import {SoundManager} from "@/services/SoundManager";
import {SoundType} from "@/services/SoundTypes";

export class Player {
  // Booleans for checking movement-input
  private moveForward: boolean;
  private moveBackward: boolean;
  private moveLeft: boolean;
  private moveRight: boolean;
  private sprinting: boolean;
  private camera: THREE.PerspectiveCamera;
  private controls: PointerLockControls;
  private targetPosition: THREE.Vector3
  private isJumping: boolean;
  private lastJumpTime: number;
  private doubleJump: boolean;
  private spacePressed: boolean;
  private calories: number;
  private _message = ref("")

  private _sprintData = reactive({
    sprintTimeLeft: 100, // percentage (0-100)
    isSprinting: false,
    isCooldown: false,
  })

  /**
   * Initialises the Player as a camera
   * @param renderer Renderer for the scene. Needed for PointerLockControls
   * @param posX x-spawn-sosition
   * @param posY y-spawn-position
   * @param posZ z-spawn-position
   */
  constructor(renderer: WebGLRenderer, posX: number, posY: number, posZ: number) {
    this.moveBackward = false;
    this.moveForward = false;
    this.moveLeft = false;
    this.moveRight = false;
    this.sprinting = false;
    this.targetPosition = new THREE.Vector3(posX, posY, posZ) // marks the current position in the backend (used as a target for moving (lerping) the camera position towards)
    this.calories = 0;
    this.isJumping = false;
    this.lastJumpTime = 0;
    this.doubleJump = false;
    this.spacePressed = false;
    this.camera = new THREE.PerspectiveCamera(45, window.innerWidth / window.innerHeight, 0.1, 100)
    this.camera.position.set(posX, posY, posZ)
    this.controls = new PointerLockControls(this.camera, renderer.domElement)
    this.controls.maxPolarAngle = (170 / 180) * Math.PI;
    this.controls.minPolarAngle = (10 / 180) * Math.PI;

    document.addEventListener('keydown', (event) => {
      this.onKeyDown(event)
    })
    document.addEventListener('keyup', (event) => {
      this.onKeyUp(event)
    })
    document.addEventListener('click', () => {
      this.controls.lock()
    })
  }

  public getCamera(): THREE.PerspectiveCamera {
    return this.camera;
  }

  public getControls(): PointerLockControls {
    return this.controls;
  }

  /**
   * unsets released keys
   * @param event keyboard-events
   */
  public onKeyUp(event: any) {
    switch (event.code) {
      case 'ArrowUp':
      case 'KeyW':
        this.moveForward = false
        this.sprinting = false;
        break

      case 'ArrowLeft':
      case 'KeyA':
        this.moveLeft = false
        break

      case 'ArrowDown':
      case 'KeyS':
        this.moveBackward = false
        break

      case 'ArrowRight':
      case 'KeyD':
        this.moveRight = false
        break

      case 'Space':
        this.spacePressed = false;
        break

      case 'ShiftLeft':
        this.sprinting = false;
        break;
    }
  }

  /**
   * sets pressed keys
   * @param event keyboard-events
   */
  public onKeyDown(event: any) {
    switch (event.code) {
      case 'ArrowUp':
      case 'KeyW':
        this.moveForward = true
        if (event.shiftKey) {
          this.sprinting = true;
        }
        break

      case 'ArrowLeft':
      case 'KeyA':
        this.moveLeft = true
        break

      case 'ArrowDown':
      case 'KeyS':
        this.moveBackward = true
        break

      case 'ArrowRight':
      case 'KeyD':
        this.moveRight = true
        break

      case 'Space':
        if (!this.spacePressed) {
          this.spacePressed = true;
          const currentTime = performance.now();
          if (!this.isJumping) {
            //Single Jump
            this.isJumping = true;
            this.lastJumpTime = currentTime;
          } else if (!this.doubleJump && (currentTime - this.lastJumpTime <= 600)) {
            //Double Jump
            this.doubleJump = true;
            this.lastJumpTime = currentTime;
          }
        }
        break;

      case 'ShiftLeft':
        this.sprinting = this.moveForward;
        if (this.moveForward) {
          this.sprinting = true;
        }
        break;
    }
  }

  /**
   * get booleans of held inputs
   * @returns Object with inputs-values
   */
  public getInput(): object {
    return {forward: this.moveForward, backward: this.moveBackward, left: this.moveLeft, right: this.moveRight}
  }

  /**
   * Set target position to lerp towards
   */
  public setPosition(pos: THREE.Vector3) {
    this.targetPosition = pos;
    if (this.targetPosition.y <= 2) {
      this.isJumping = false
      this.doubleJump = false
    }
  }

  public get message() {
    return this._message;
  }

  public getIsJumping() {
    return this.isJumping;
  }

  public getIsDoubleJumping() {
    return this.doubleJump;
  }

  public get isSprinting(): boolean {
    return this.sprinting;
  }

  get sprintData(): UnwrapNestedRefs<{ isSprinting: boolean; sprintTimeLeft: number; isCooldown: boolean }> & {} {
    return this._sprintData;
  }

  /**
   * Moves the camera position towards the target position by t (0.1) ammount
   */
  public lerpPosition() {
    this.camera.position.lerp(this.targetPosition, 0.1)
  }

  public getCalories(): number {
    return this.calories;
  }

  public setCalories(cal: number): void {
    if (cal > this.calories) {
      SoundManager.playSound(SoundType.EAT_SNACK)
    }

    this.calories = cal;
  }
}
