<template>
  <div id="individual-outer-box-size" class="outer-box">
    <div class="inner-box">
      <h1 class="title"> {{ $t('difficulty.title') }} </h1>
      <div id="button-pair">
        <MainMenuButton id="easy-button"
                        :class="{ 'selected': difficulty === ScriptGhostDifficulty.EASY }"
                        @click="setDifficulty(ScriptGhostDifficulty.EASY)">
          {{ $t('difficulty.easy') }}
        </MainMenuButton>
        <MainMenuButton id="difficult-button"
                        :class="{ 'selected': difficulty === ScriptGhostDifficulty.DIFFICULT }"
                        @click="setDifficulty(ScriptGhostDifficulty.DIFFICULT)">
          {{ $t('difficulty.difficult') }}
        </MainMenuButton>
      </div>
      <p id="description">
        {{ $t('difficulty.description1') }}
        <span id="highlight-difficulty-easy">'{{ $t('difficulty.easy') }}'</span>
        {{ $t('difficulty.description2') }}
        <span id="highlight-difficulty-difficult">'{{ $t('difficulty.difficult') }}'</span>
        {{ $t('difficulty.description3') }}
      </p>
    </div>
    <SmallNavButton id="menu-back-button" class="small-nav-buttons" @click="backToMainMenu">
      {{ $t('button.back') }}
    </SmallNavButton>
    <SmallNavButton id="start-game-button" class="small-nav-buttons" @click="startSingleplayer">
      {{ $t('button.startGame') }}
    </SmallNavButton>
  </div>

</template>

<script lang="ts" setup>
import SmallNavButton from "@/components/SmallNavButton.vue";
import {ref} from "vue";
import {useRouter} from 'vue-router'
import {ScriptGhostDifficulty} from "@/stores/Player/IDifficulty";
import type {ILobbyDTD} from "@/stores/Lobby/ILobbyDTD";
import {useLobbiesStore} from "@/stores/Lobby/lobbiesstore";
import MainMenuButton from "@/components/MainMenuButton.vue";

const router = useRouter()
const difficulty = ref(ScriptGhostDifficulty.EASY)    // standardmäßig ist easy ausgewählt -> das kann man dann ändern
const lobbiesStore = useLobbiesStore()

const emit = defineEmits<{
  (e: 'hideChooseDifficultySingleplayer'): void;
}>();

const setDifficulty = (newDifficulty: ScriptGhostDifficulty) => {
  difficulty.value = newDifficulty
}

const backToMainMenu = () => {
  emit('hideChooseDifficultySingleplayer');
}

const startSingleplayer = async () => {

  const player = lobbiesStore.lobbydata.currentPlayer
  const lobby = await lobbiesStore.startSingleplayerGame(player, difficulty.value.toString()) as ILobbyDTD

  if (!player.playerId || !lobby) {
    console.error('Player or Lobby not found')
    return
  }

  if (player.playerId === lobby.adminClient.playerId) {
    await router.push({
      name: 'GameView',
      query: {
        role: lobbiesStore.lobbydata.currentPlayer.role,
        lobbyId: lobbiesStore.lobbydata.currentPlayer.joinedLobbyId,
      },
    })
  }
}
</script>

<style scoped>
#individual-outer-box-size {
  width: 80%;
  height: 60%;
  padding: 2%;
  top: 10%;
}

.title {
  color: var(--primary-highlight-color);
  font-size: 60px;
  text-align: center;
  margin-bottom: 2rem;
  font-weight: bold;
}

#description {
  color: var(--main-text-color);
  text-align: center;
  font-size: 28px;
}

#button-pair {
  width: 100%;
  display: flex;
  flex-direction: row;
  justify-content: center;
  gap: 20px;
  padding: 3em 0;
}

#menu-back-button {
  position: absolute;
  left: 5%;
  bottom: 8%;
}

#start-game-button {
  position: absolute;
  right: 5%;
  bottom: 8%;
}

#highlight-difficulty-easy {
  color: var(--primary-sprint-bar-color);
}

#highlight-difficulty-difficult {
  color: var(--accent-color);
}

#easy-button:hover,
#easy-button.selected {
  background-color: var(--primary-sprint-bar-color);
}

#difficult-button:hover,
#difficult-button.selected {
  background-color: var(--accent-color);
}

@media (min-width: 2500px) {
  #individual-outer-box-size {
    top: 20%;
    height: 50%;
    width: 60%;
  }
}

@media (min-width: 2300px) {
  .title {
    font-size: 80px;
  }
}

@media (min-width: 1000px) and (min-height: 1500px) {
  #description {
    font-size: 35px;
  }
}

@media (min-width: 1900px) and (max-width: 2499px) {
  #individual-outer-box-size {
    top: 18%;
    width: 70%;
    height: 60%;
  }
}

@media (min-width: 1500px) and (max-width: 1899px) {
  #individual-outer-box-size {
    top: 18%;
    width: 80%;
    height: 70%;
  }
}

@media (min-width: 500px) and (max-width: 1499px) and (max-height: 1000px) {
  #individual-outer-box-size {
    top: 10%;
    width: 90%;
    height: 80%;
  }
}
</style>
