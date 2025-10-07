<template>
  <MenuBackground>
    <LanguageSwitch></LanguageSwitch>
    <div v-if="darkenBackground" id="darken-background"></div>

    <PlayerNameForm
      v-if="showPlayerNameForm && !playerNameSaved"
      @hidePlayerNameForm="hidePlayerNameForm"
    >
    </PlayerNameForm>

    <ChooseDifficultySingleplayer
      v-if="showChooseSingleplayerDifficulty"
      @hideChooseDifficultySingleplayer="hideChooseDifficultySingleplayer">
    </ChooseDifficultySingleplayer>

    <div class="button-container">
      <MainMenuButton v-if="!showChooseSingleplayerDifficulty" @click="startSingleplayer">
        {{ $t('button.singlePlayer') }}
      </MainMenuButton>
      <MainMenuButton v-if="!showChooseSingleplayerDifficulty" @click="showLobbies">{{ $t('button.multiPlayer') }}
      </MainMenuButton>
      <MainMenuButton v-if="!showChooseSingleplayerDifficulty" @click="showLeaderboard">{{ $t('button.leaderBoard') }}
      </MainMenuButton>
    </div>
  </MenuBackground>
</template>

<script setup lang="ts">
import {useLobbiesStore} from '@/stores/Lobby/lobbiesstore'
import MainMenuButton from '@/components/MainMenuButton.vue'
import MenuBackground from '@/components/MenuBackground.vue'
import PlayerNameForm from '@/components/PlayerNameForm.vue';
import {useRouter} from 'vue-router'
import {onMounted, ref} from 'vue';
import {SoundManager} from "@/services/SoundManager";
import {SoundType} from "@/services/SoundTypes";
import ChooseDifficultySingleplayer from "@/views/ChooseDifficultySingleplayer.vue";
import LanguageSwitch from '@/components/LanguageSwitch.vue';

const router = useRouter()
const lobbiesStore = useLobbiesStore()

const playerNameSaved = lobbiesStore.lobbydata.currentPlayer.playerName;
const darkenBackground = ref(false);
const showPlayerNameForm = ref(false);
const showChooseSingleplayerDifficulty = ref(false);


const hidePlayerNameForm = () => {
  showPlayerNameForm.value = false;
  darkenBackground.value = false;
}

const hideChooseDifficultySingleplayer = () => {
  showChooseSingleplayerDifficulty.value = false
}

const showLobbies = () => {
  router.push({name: 'LobbyListView'})
}

const showLeaderboard = () => {
  router.push({name: 'Leaderboard'})
}

const startSingleplayer = () => {
  showChooseSingleplayerDifficulty.value = true
}

onMounted(() => {
  if (!playerNameSaved) {
    darkenBackground.value = true;
    showPlayerNameForm.value = true;
  }

  SoundManager.playSound(SoundType.LOBBY_MUSIC)
})
</script>

<style scoped>
.button-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.5rem;
}

.button-container > * {
  width: 100%;
  text-align: center;
}

#darken-background {
  z-index: 1;
  position: fixed;
  top: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 50%);
  transition: background 0.3s ease;
}
</style>
