import * as THREE from 'three'
import {SnackType} from '@/stores/Snack/ISnackDTD'
import {ChickenThickness} from '@/stores/Chicken/IChickenDTD'
import {GLTFLoader} from 'three/examples/jsm/loaders/GLTFLoader.js'

/**
 * for creating the objects in the map
 * objects are rendered by the GameMapRenderer.ts
 */
export const GameObjectRenderer = () => {
  const GROUNDSIZE = 1000
  const loader = new GLTFLoader()
  const ghostGLB = "/ghost.glb"
  const chickenGLB = "/chicken.glb"
  let ghostModel: THREE.Group | null = null
  let chickenModel: THREE.Group | null = null

  const snackModels = {
    [SnackType.STRAWBERRY]: "/strawberry.glb",
    [SnackType.ORANGE]: "/orange.glb",
    [SnackType.CHERRY]: "/cherry.glb",
    [SnackType.APPLE]: "/apple.glb",
    [SnackType.EGG]: "/yoshiegg.glb",
    [SnackType.EMPTY]: null,
  };

  const snackModelCache: { [key in SnackType]?: THREE.Group } = {};

  async function createSnackOnFloor(
    xPosition: number,
    zPosition: number,
    yPosition: number,
    sideLength: number,
    type: SnackType
  ): Promise<THREE.Object3D> {
    const modelPath = snackModels[type];
    if (!modelPath) {
      console.error(`No model found for SnackType: ${type}`);
      return new THREE.Mesh(
        new THREE.BoxGeometry(sideLength / 3, 1, sideLength / 3),
        new THREE.MeshStandardMaterial({color: "gray", opacity: 0.5, transparent: true})
      );
    }

    if (!snackModelCache[type]) {
      try {
        const gltf = await loader.loadAsync(modelPath);
        const snackModel = gltf.scene;

        // Calculate scaling
        const box = new THREE.Box3().setFromObject(snackModel);
        const size = new THREE.Vector3();
        box.getSize(size);

        const maxDimension = Math.max(size.x, size.y, size.z);
        const scale = (sideLength / 3) / maxDimension; // Standardised scaling based on `sideLength`.
        snackModel.scale.set(scale, scale, scale);

        const yOffset = box.min.y * scale; // Bottom edge of the model after scaling
        snackModel.position.y -= yOffset;

        snackModel.traverse((child: any) => {
          if (child.isMesh) {
            child.castShadow = true;
            child.receiveShadow = true;
          }
        });

        snackModelCache[type] = snackModel;
      } catch (error) {
        console.error(`Error loading snack model for ${type}:`, error);
        return new THREE.Mesh(
          new THREE.BoxGeometry(sideLength / 3, 1, sideLength / 3),
          new THREE.MeshStandardMaterial({color: 'gray', opacity: 0.5, transparent: true})
        );
      }
    }

    const clonedSnack = snackModelCache[type]!.clone();
    clonedSnack.position.set(xPosition, yPosition, zPosition);
    return clonedSnack;
  }

  async function createChickenOnFloor(
    xPosition: number,
    zPosition: number,
    yPosition: number,
    thickness: ChickenThickness,
  ): Promise<THREE.Group> {
    const scale = ChickenThickness[thickness as unknown as keyof typeof ChickenThickness]
    // If the model is already loaded, clone it
    if (chickenModel) {
      const clonedChicken = chickenModel.clone()
      clonedChicken.position.set(xPosition, yPosition, zPosition)
      clonedChicken.scale.set(scale, scale, scale)
      clonedChicken.castShadow = true
      clonedChicken.receiveShadow = true
      return clonedChicken
    }

    // Load model asynchronously and replace placeholder
    return new Promise((resolve, reject) => {
      loader.load(
        chickenGLB,
        (gltf) => {
          chickenModel = gltf.scene
          chickenModel.position.set(xPosition, yPosition, zPosition)
          chickenModel.scale.set(scale, scale, scale)
          chickenModel.castShadow = true
          chickenModel.receiveShadow = true
          resolve(chickenModel.clone())
        },
        undefined,
      )
    })
  }

  async function createGhostOnFloor(
    xPosition: number,
    zPosition: number,
    yPosition: number,
    sideLength: number
  ): Promise<THREE.Group> {
    // If the model is already loaded, clone it
    if (ghostModel) {
      const clonedGhost = ghostModel.clone()
      clonedGhost.position.set(xPosition, yPosition, zPosition)
      clonedGhost.scale.set(0.5, 0.5, 0.5)
      clonedGhost.castShadow = true
      clonedGhost.receiveShadow = true
      return clonedGhost
    }

    // Load model asynchronously and replace placeholder
    return new Promise((resolve, reject) => {
      loader.load(
        ghostGLB,
        (gltf) => {
          ghostModel = gltf.scene
          ghostModel.position.set(xPosition, yPosition, zPosition)
          ghostModel.scale.set(0.5, 0.5, 0.5)
          ghostModel.castShadow = true
          ghostModel.receiveShadow = true
          resolve(ghostModel.clone())
        },
        undefined,
      )
    })
  }

  const createFloorSquare = (
    xPosition: number,
    zPosition: number,
    sideLength: number,
  ) => {
    // TODO squareHeight is set for seeing it actually in game
    const squareHeight = 0.1
    const squareMaterial = new THREE.MeshStandardMaterial({color: 'green'})
    const squareGeometry = new THREE.BoxGeometry(
      sideLength,
      squareHeight,
      sideLength,
    )
    const square = new THREE.Mesh(squareGeometry, squareMaterial)

    // Position the square
    square.position.set(xPosition, 0, zPosition)

    return square
  }

  const createGround = () => {
    // ground setup
    const groundTexture = new THREE.TextureLoader().load('./textures/green-grass-512x512.jpg')
    groundTexture.wrapS = THREE.RepeatWrapping;
    groundTexture.wrapT = THREE.RepeatWrapping;
    groundTexture.repeat.set(1000, 1000);
    const groundGeometry = new THREE.PlaneGeometry(GROUNDSIZE, GROUNDSIZE)
    const groundMaterial = new THREE.MeshStandardMaterial({
      map: groundTexture,
      roughness: 0.7,
      metalness: 0.1,
    })
    const ground = new THREE.Mesh(groundGeometry, groundMaterial)
    ground.castShadow = true
    ground.receiveShadow = true
    ground.rotateX(-Math.PI / 2)
    ground.position.set(0, 0, 0)

    return ground
  }

  /**
   * create a single wall with given x-, y-, z-position, height and sidelengthm
   */
  const createWall = (
    xPosition: number,
    yPosition: number,
    zPosition: number,
    height: number,
    sideLength: number,
  ) => {
    //frontend/public/textures/pngtree_cartoon_style_seamless_textured_surface_square.jpg
    const wallTexture = new THREE.TextureLoader().load('./textures/pngtree_cartoon_style_seamless_textured_surface_square.jpg')
    wallTexture.wrapS = THREE.RepeatWrapping;
    wallTexture.wrapT = THREE.RepeatWrapping;
    wallTexture.repeat.set(1, 1.87);

    const wallMaterial = new THREE.MeshStandardMaterial({
      map: wallTexture,
      color: 0xffd133,
      emissive: 0x000000,
      emissiveIntensity: 0.7,
      roughness: 0.9,
    })
    const wallGeometry = new THREE.BoxGeometry(sideLength, height, sideLength)
    const wall = new THREE.Mesh(wallGeometry, wallMaterial)

    // Position the wall
    wall.position.set(xPosition, yPosition, zPosition)

    return wall
  }

  return {
    createSnackOnFloor,
    createChickenOnFloor,
    createFloorSquare,
    createGround,
    createWall,
    createGhostOnFloor
  }
}
