<template>
  <MenuBackground :isLobbyView="true">
    <LanguageSwitch></LanguageSwitch>

    <div id="individual-outer-box-size" class="outer-box">
      <h1 class="title">{{ $t('lobbyList.title') }}</h1>
      <SmallNavButton
        id="menu-back-button"
        class="small-nav-buttons"
        @click="backToMainMenu"
      >
        {{ $t('button.back') }}
      </SmallNavButton>
      <SmallNavButton
        id="show-lobby-creation-button"
        class="small-nav-buttons"
        @click="showCreateLobbyForm"
      >
        {{ $t('button.createLobby') }}
      </SmallNavButton>

      <div class="inner-box">
        <ul>
          <li
            v-for="lobby in filteredLobbies"
            :key="lobby.lobbyId"
            class="lobby-list-items"
            @click="joinLobby(lobby)"
          >
            <div class="lobby-name">
              {{ lobby.name }}
            </div>

            <div class="playercount">
              {{ lobby.members.length }} / {{ MAX_PLAYER_COUNT }}
              {{ $t('lobbyList.playerCount.player') }}
            </div>
          </li>
        </ul>
      </div>
    </div>

    <div v-if="darkenBackground" id="darken-background"></div>

    <PopUp v-if="showPopUp" class="popup-box" @hidePopUp="hidePopUp">
      <p class="info-heading">{{ $t('popup.lobbyFull.heading') }}</p>
      <p class="info-text">{{ $t('popup.lobbyFull.text') }}</p>
    </PopUp>

    <CreateLobbyForm
      v-if="showLobbyForm"
      @cancelLobbyCreation="cancelLobbyCreation"
    />
  </MenuBackground>
</template>

<script lang="ts" setup>
import MenuBackground from '@/components/MenuBackground.vue'
import SmallNavButton from '@/components/SmallNavButton.vue'
import CreateLobbyForm from '@/components/CreateLobbyForm.vue'
import PopUp from '@/components/PopUp.vue'
import LanguageSwitch from '@/components/LanguageSwitch.vue'

import { useRouter } from 'vue-router'
import { computed, onMounted, ref } from 'vue'
import { useLobbiesStore } from '@/stores/Lobby/lobbiesstore'
import type { ILobbyDTD } from '@/stores/Lobby/ILobbyDTD'
import type { IPlayerClientDTD } from '@/stores/Lobby/IPlayerClientDTD'

const router = useRouter()
const lobbiesStore = useLobbiesStore()

const lobbies = computed(() => lobbiesStore.lobbydata.lobbies)
const currentPlayer = lobbiesStore.lobbydata.currentPlayer as IPlayerClientDTD

const MAX_PLAYER_COUNT = 5

const filteredLobbies = computed(() => {
  return lobbies.value.filter(lobby => !lobby.gameStarted)
})

const darkenBackground = ref(false)
const showPopUp = ref(false)
const showLobbyForm = ref(false)

const hidePopUp = () => {
  showPopUp.value = false
  darkenBackground.value = false
}

const backToMainMenu = () => {
  router.push({ name: 'MainMenu' })
}

const showCreateLobbyForm = () => {
  showLobbyForm.value = true
  darkenBackground.value = true
}

const cancelLobbyCreation = () => {
  showLobbyForm.value = false
  darkenBackground.value = false
}

/**
 * Joins a specified lobby if it is not full and the game has not started.
 * Alerts the user if the lobby is full or if the game has already started.
 * On successful join, redirects to the lobby view.
 *
 * @async
 * @function joinLobby
 * @param {ILobbyDTD} lobby - The lobby object that the player wants to join.
 * @throws {Error} Throws an alert if the lobby is full or the game has already started.
 * @throws {Error} Throws an alert if there is an error joining the lobby.
 */
const joinLobby = async (lobby: ILobbyDTD) => {
  if (lobby.members.length >= MAX_PLAYER_COUNT) {
    showPopUp.value = true
    darkenBackground.value = true
    return
  }

  try {
    const joinedLobby = await lobbiesStore.joinLobby(
      lobby.lobbyId,
      currentPlayer.playerId,
    )

    if (joinedLobby) {
      router.push({ name: 'LobbyView', params: { lobbyId: lobby.lobbyId } })
    }
  } catch (error: any) {
    console.error('Error:', error)
    alert('Error join Lobby!')
  }
}

onMounted(async () => {
  await lobbiesStore.fetchLobbyList()

  lobbiesStore.startLobbyLiveUpdate()
})
</script>

<style scoped>
:root {
  --button-bottom-spacing: ;
}

.title {
  font-size: 3rem;
  font-weight: bold;
  color: var(--main-text-color);
  text-align: center;
}

#individual-outer-box-size {
  width: 60%;
  max-width: 80%;
  height: 60%;
  padding: 2%;
}

.inner-box {
  position: relative;
  left: 50%;
  transform: translateX(-50%);
  height: 65%;
  border-radius: 0.3rem;
  color: var(--primary-text-color);
  overflow-y: auto;
}

.inner-box > ul {
  list-style: none;
  left: 50%;
  transform: translateX(-50%);
  margin: 0;
  padding: 0;
  vertical-align: middle;
}

.lobby-list-items {
  display: flex;
  justify-content: space-between;
  background: var(--background-for-text-color);
  border: 4px solid var(--primary-text-color);
  border-radius: 0.1rem;
  box-shadow: 4px 3px 0 var(--primary-text-color);
  font-size: 1.2rem;
  padding: 0.5rem 0.8rem;
  margin: 0.7rem 0;
}

.lobby-list-items:hover {
  cursor: pointer;
}

.small-nav-buttons {
  bottom: 4%;
  font-weight: bold;
}

#menu-back-button {
  left: 3%;
}

#show-lobby-creation-button {
  right: 3%;
}

.info-heading {
  font-size: 3rem;
  font-weight: bold;
}

.info-text {
  font-size: 1.8rem;
  padding: 1.2rem;
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
