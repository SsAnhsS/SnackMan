import * as THREE from "three";
import {Object3D} from "three";
import {SoundType} from "@/services/SoundTypes";

/**
 * Manages the sounds for the game, including background music,
 * positional audio, and sound effects for various game events.
 */
export class SoundManager {
    private static ingameListener: THREE.AudioListener;
    private static lobbyListener: THREE.AudioListener;
    private static characterSounds = new Map<SoundType, THREE.PositionalAudio>();
    private static backgroundSounds = new Map<SoundType, THREE.Audio>();
    private static chickenSoundsToChooseFrom = new Array<THREE.PositionalAudio>();
    private static chickenSounds = new Array<THREE.PositionalAudio>();
    private static ghostSounds = new Array<THREE.PositionalAudio>();

    /**
     * Initializes the sound manager with the given camera.
     * Attaches an audio listener to the camera.
     *
     * @param camera - The camera to which the audio listener is attached.
     */
    public static async initSoundmanager(camera: THREE.Camera): Promise<void> {
      console.debug("InitSoundmanager");
        this.ingameListener = new THREE.AudioListener();
        camera.add(this.ingameListener);

        await this.initSounds();
    }

    /**
     * Initializes the background music manager, setting up a lobby listener.
     */
    public static async initBackgroundMusicManager(): Promise<void> {
      console.debug("InitBackgroundMusicManager")
        this.lobbyListener = new THREE.AudioListener();
        await this.initBackgroundSounds();
    }

    /**
     * Loads an audio buffer asynchronously from the specified path.
     *
     * @param bufferPath - The path to the audio file.
     * @returns A promise that resolves with the loaded audio buffer.
     */
    private static async loadAudioAsync(bufferPath: string): Promise<AudioBuffer> {
        return new Promise((resolve, reject) => {
            const audioLoader = new THREE.AudioLoader();
            audioLoader.load(
                bufferPath,
                (buffer) => resolve(buffer),
                undefined,
                (error) => reject(`Error loading audio file '${bufferPath}': ${error}`)
            );
        });
    }

    /**
     * Attaches a sound to a 3D model or mesh based on the sound type.
     *
     * @param meshOrModel - The 3D object to which the sound will be attached.
     * @param soundType - The type of sound to attach.
     */
    public static attachSoundToModelOrMesh(
        meshOrModel: THREE.Group<THREE.Object3DEventMap> | THREE.Mesh | Object3D,
        soundType: SoundType
    ) {

      if (soundType === SoundType.CHICKEN && this.chickenSoundsToChooseFrom.length > 0) {
            console.debug("Chicken atteched to Modeel")
            const randomIndex = Math.floor(Math.random() * this.chickenSoundsToChooseFrom.length);
            const selectedSound = this.chickenSoundsToChooseFrom[randomIndex];

            const soundClone = new THREE.PositionalAudio(selectedSound.listener);
            if (selectedSound.buffer) {
              soundClone.setBuffer(selectedSound.buffer);
            } else {
              console.error("The selected sound's buffer is null and cannot be set.");
            }
            soundClone.setRefDistance(3); // maximum volume at x units of distance
            soundClone.setMaxDistance(12);
            soundClone.setRolloffFactor(1); // how quickly the volume decreases with increasing distance
            soundClone.setDistanceModel('linear'); // decrease in volume (linear is a good choice for games)
            soundClone.setLoop(true);
            soundClone.setVolume(0.4);

            meshOrModel.add(soundClone);

            this.chickenSounds.push(soundClone);
        } else if (soundType === SoundType.GHOST) {
            const selectedSound = this.characterSounds.get(SoundType.GHOST);

            if (selectedSound) {
                const soundClone = new THREE.PositionalAudio(selectedSound.listener);
                if (selectedSound.buffer) {
                    soundClone.setBuffer(selectedSound.buffer);
                } else {
                    console.error("The selected sound's buffer is null and cannot be set.");
                }
                soundClone.setRefDistance(4); // maximum volume at x units of distance
                soundClone.setMaxDistance(15);
                soundClone.setRolloffFactor(0.7); // how quickly the volume decreases with increasing distance
                soundClone.setDistanceModel('linear'); // decrease in volume (linear is a good choice for games)
                soundClone.setLoop(true);
                soundClone.setVolume(0.1);

                this.ghostSounds.push(soundClone);
            }
        } else {
            console.warn("No sounds available or invalid sound type.");
        }
    }

    /**
     * Initializes all game sounds, including background music and effects.
     */
    public static async initSounds(): Promise<void> {
        await Promise.all([
            this.initGameEndSound(),
            this.initEatSnackSound(),
            this.initChickenSounds(),
            this.initGhostHitsSnackmanSound(),
            this.initGhostSounds()
        ]);
      console.debug("All sounds initialized.");
    }

    /**
     * Stops all in-game sounds.
     */
    public static stopAllInGameSounds() {
        this.chickenSounds.forEach((chickenSound) => {
            chickenSound.stop();
        });

        this.ghostSounds.forEach((ghostSound) => {
            ghostSound.stop();
        });

        this.backgroundSounds.get(SoundType.INGAME_BACKGROUND)?.stop();
    }

    /**
     * Stops the lobby background sound.
     */
    public static stopLobbySound() {
        this.backgroundSounds.get(SoundType.LOBBY_MUSIC)?.stop();
    }

    /**
     * Plays the sound associated with the given sound type.
     *
     * @param soundType - The type of sound to play.
     */
    public static playSound(soundType: SoundType) {
        if (soundType === SoundType.CHICKEN) {
          console.debug("chicken sound is playing");
            this.chickenSounds.forEach((chickenSound) => {
              if (!chickenSound.isPlaying) {
                chickenSound.play();
              }
            });
        } else if (soundType === SoundType.GHOST) {
            this.ghostSounds.forEach((ghostSound) => {
              console.debug("ghost sound is playing");
              if (!ghostSound.isPlaying) {
                ghostSound.play();
              }
            });
        } else {
            const sound = this.characterSounds.get(soundType) || this.backgroundSounds.get(soundType);

            if (sound) {
                if (!sound.isPlaying) {
                    try {
                        sound.play();
                    } catch (error) {
                        console.error(`Error playing sound '${soundType}':`, error);
                    }
                }
            } else {
                console.warn(`Sound with key '${soundType}' not found.`);
            }
        }
    }

    /**
     * Initializes the endgame sound.
     */
    private static async initGameEndSound(): Promise<void> {
        const sound = new THREE.Audio(this.ingameListener);
        const buffer = await this.loadAudioAsync('sounds/backgroundMusic/Game_End_Sound.mp3');
        sound.setBuffer(buffer);
        sound.setVolume(0.1);

        this.backgroundSounds.set(SoundType.GAME_END, sound);
      console.debug("Endgame sound initialized.");
    }

    /**
     * Initializes all background sounds, including in-game and lobby music.
     */
    private static async initBackgroundSounds(): Promise<void> {
        await this.initIngameBackgroundMusic();
        await this.initLobbyBackgroundMusic();
    }

    /**
     * Initializes the in-game background music.
     */
    private static async initIngameBackgroundMusic(): Promise<void> {
        const ingameMusic = new THREE.Audio(this.lobbyListener);
        const buffer = await this.loadAudioAsync('sounds/backgroundMusic/ingameBackgroundMusic.mp3');
        ingameMusic.setBuffer(buffer);
        ingameMusic.setVolume(0.2);
        ingameMusic.setLoop(true);

        this.backgroundSounds.set(SoundType.INGAME_BACKGROUND, ingameMusic);
      console.debug("Ingame music initialized.");
    }

    /**
     * Initializes the lobby background music.
     */
    private static async initLobbyBackgroundMusic(): Promise<void> {
        const lobbyMusic = new THREE.Audio(this.lobbyListener);
        const buffer = await this.loadAudioAsync('sounds/backgroundMusic/lobbyMusic.mp3');
        lobbyMusic.setBuffer(buffer);
        lobbyMusic.setVolume(0.3);
        lobbyMusic.setLoop(true);

        this.backgroundSounds.set(SoundType.LOBBY_MUSIC, lobbyMusic);
      console.debug("Lobby music initialized.");
    }

    /**
     * Initializes the sound for eating a snack.
     */
    private static async initEatSnackSound(): Promise<void> {
        const sound = new THREE.Audio(this.ingameListener);
        const buffer = await this.loadAudioAsync('sounds/snackman/collect_snack_sound.mp3');
        sound.setBuffer(buffer);

        this.backgroundSounds.set(SoundType.EAT_SNACK, sound);
    }

    /**
     * Initializes the ghost sounds.
     */
    private static async initGhostSounds(): Promise<void> {
        const sound = new THREE.PositionalAudio(this.ingameListener);
        const buffer = await this.loadAudioAsync('sounds/ghost/Ghost_Sounds.mp3');
        sound.setBuffer(buffer);

        this.characterSounds.set(SoundType.GHOST, sound);
      console.debug("Ghost sound initialized.");
    }

    /**
     * Initializes the sound for when a ghost hits the Snackman.
     */
    private static async initGhostHitsSnackmanSound(): Promise<void> {
        const sound = new THREE.PositionalAudio(this.ingameListener);
        const buffer = await this.loadAudioAsync('sounds/snackman/hitByGhostSound.mp3');
        sound.setBuffer(buffer);
        sound.setRefDistance(20);

        this.characterSounds.set(SoundType.GHOST_SCARES_SNACKMAN, sound);
      console.debug("Ghost hits snackman sound initialized.");
    }

    /**
     * Initializes chicken sounds from predefined paths.
     */
    private static async initChickenSounds(): Promise<void> {
        const chickenPaths = [
            "sounds/chicken/chicken-clucking.ogg",
            "sounds/chicken/chicken_noises.ogg",
        ];

        const promises = chickenPaths.map(async (path) => {
            try {
                const buffer = await this.loadAudioAsync(path);
                const sound = new THREE.PositionalAudio(this.ingameListener);
                sound.setBuffer(buffer);
                this.chickenSoundsToChooseFrom.push(sound);
            } catch (error) {
                console.error(`Error loading chicken sound '${path}':`, error);
            }
        });

        await Promise.all(promises);

        // Add scared sound for chicken
        await this.initScaredChickenSound();

      console.debug("All chicken sounds initialized.");
    }

    /**
     * Initializes the scared chicken sound.
     */
    private static async initScaredChickenSound() {
        const chickenScaredSoundPath = "sounds/chicken/chicken-single-alarm-call.mp3";

        const promiseChickenScaredSound = await this.loadAudioAsync(chickenScaredSoundPath);
        const scaredChickenSound = new THREE.PositionalAudio(this.ingameListener);
        scaredChickenSound.setBuffer(promiseChickenScaredSound);
        scaredChickenSound.setRefDistance(15);
        scaredChickenSound.setDistanceModel('linear');
        scaredChickenSound.setVolume(0.8);

      console.debug("Scare chicken sound initialized.");
        this.characterSounds.set(SoundType.GHOST_SCARES_CHICKEN, scaredChickenSound);
    }
}
