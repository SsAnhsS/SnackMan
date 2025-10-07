<template>
  <MenuBackground></MenuBackground>

  <div class="outer-box">
    <div id="inner-box">
      <h1 class="title">{{ $t('chooseRole.title') }}</h1>
      <div class="character-grid">
        <div
          v-for="button in buttons"
          :key="button.id"
          :class="{ selected: button.selected }"
          :style="
            button.selected ? { opacity: 0.3, cursor: 'not-allowed' } : {}
          "
          class="character-item"
          @click="selectCharacter(button)"
        >
          <div
            :style="button.selected ? { pointerEvents: 'none' } : {}"
            class="image-container"
          >
            <img
              :alt="button.name"
              :src="button.image"
              class="character-image"
            />
          </div>
          <p class="character-name">{{ $t(button.translation) }}</p>
        </div>
      </div>
      <div id="button-box">
        <SmallNavButton
          v-if="isPlayerAdmin"
          id="start-game-button"
          class="small-nav-buttons"
          @click="startGame"
          :disabled="playersWithoutRole > 0"
        >
          {{ $t('button.startGame') }}
        </SmallNavButton>
      </div>
      <div id="player-count" v-if="playersWithoutRole > 0">
        {{ playersWithoutRole }} {{ playersWithoutRole === 1 ? $t('chooseRole.playerCount.onePlayer') : $t('chooseRole.playerCount.twoPlayers') }}
      </div>
    </div>

    <PopUp v-if="showPopUp" class="popup-box" @hidePopUp="hidePopUp">
      <p class="info-heading">{{ $t('popup.cantStart.heading') }}</p>
      <p class="info-text">{{ infoText }}</p>
    </PopUp>

    <PopUp v-if="showRolePopup" class="popup-box" @hidePopUp="hidePopUp">
      <p class="info-heading">{{ $t('popup.cantStart.heading') }}</p>
      <p class="info-text">{{ infoText }}</p>
    </PopUp>

    <div v-if="darkenBackground" id="darken-background"></div>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, ref, watchEffect} from 'vue'
import MenuBackground from '@/components/MenuBackground.vue'
import SmallNavButton from '@/components/SmallNavButton.vue'
import type {IPlayerClientDTD} from '@/stores/Lobby/IPlayerClientDTD'
import {useRoute, useRouter} from 'vue-router'
import PopUp from '@/components/PopUp.vue'
import type {Button} from '@/stores/Lobby/lobbiesstore'
import {useLobbiesStore} from '@/stores/Lobby/lobbiesstore'
import {useI18n} from 'vue-i18n'

const {t} = useI18n()

const route = useRoute()
const router = useRouter()
const lobbyId = route.params.lobbyId as string
const lobbiesStore = useLobbiesStore()
const buttons = lobbiesStore.buttons

const lobby = computed(() =>
  lobbiesStore.lobbydata.lobbies.find(l => l.lobbyId === lobbyUrl),
)
const members = computed(
  () => lobby.value?.members || ([] as Array<IPlayerClientDTD>),
)
const playersWithoutRole = computed(() => {
  return members.value.filter(member => member.role === 'UNDEFINED').length;
});

const darkenBackground = ref(false)
const showPopUp = ref(false)
const showRolePopup = ref(false)
const infoText = ref()
const hidePopUp = () => {
  showPopUp.value = false
}
const lobbyUrl = route.params.lobbyId

const isPlayerAdmin = computed(() => {
  const currentPlayer = lobbiesStore.lobbydata.currentPlayer
  const lobby = lobbiesStore.lobbydata.lobbies.find(l => l.lobbyId === lobbyId)
  return (
    currentPlayer &&
    lobby &&
    currentPlayer.playerId === lobby.adminClient.playerId
  )
})

/**
 * Starts the game if the player is the admin and there are enough members in the lobby.
 * If the player is not the admin or there are not enough members, a popup will be shown.
 *
 * @async
 * @function startGame
 * @throws {Error} If the player or lobby is not found.
 */

const selectedCharacter = ref<Button | null>(null)

const selectCharacter = async (button: Button) => {
  // already choosen Role
  if (button.selected) {
    infoText.value = t('chooseRole.alreadyChosen')
    showPopUp.value = true
    darkenBackground.value = true
    return
  }
  // sende LobbyId, PlayerId,Rolle, ButtonId, Selected, ButtonId
  const payload = {
    lobbyId: lobbyId,
    playerId: lobbiesStore.lobbydata.currentPlayer.playerId,
    role: button.name.toUpperCase(), // "SNACKMAN" oder "GHOST"
    buttonId: button.id,
    selected: button.selected,
  }
  try {
    // bekommt statuscode Zurück 200 Ok , 409 Conflict , 400 Badrequest
    const response = await fetch('/api/lobbies/lobby/choose/role', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    })

    if (response.ok) {
      selectedCharacter.value = button
      darkenBackground.value = true
    } else if (response.status === 409) {
      infoText.value = t('chooseRole.alreadyChosen')
      showPopUp.value = true
      darkenBackground.value = true
    } else {
      infoText.value = t('chooseRole.error.selection')
      showPopUp.value = true
      darkenBackground.value = true
    }
  } catch (error) {
    console.error('Error selecting character:', error)
    infoText.value = t('chooseRole.error.connectionFailed')
    showPopUp.value = true
    darkenBackground.value = true
  }
}

onMounted(async () => {
  await lobbiesStore.fetchLobbyById(lobbyId)

  await lobbiesStore.startLobbyLiveUpdate()
})

/**
 * Starts the game if the player is the admin and there are enough members in the lobby.
 * If the player is not the admin or there are not enough members, a popup will be shown.
 *
 * @async
 * @function startGame
 * @throws {Error} If the player or lobby is not found.
 */
const startGame = async () => {
  const playerId = lobbiesStore.lobbydata.currentPlayer.playerId
  let snackmanCounter: number = 0
  let memberCounter: number = 0
  const lobby = lobbiesStore.lobbydata.lobbies.find(
    lobby => lobby.lobbyId === lobbyId,
  )

  if (!playerId || !lobby) {
    console.error('Player or Lobby not found')
    return
  }
  if (playerId === lobby.adminClient.playerId) {
    lobby.members.forEach(member => {
      if (member.role === 'UNDEFINED') {
        showPopUp.value = true
        darkenBackground.value = true
        infoText.value = t('chooseRole.error.everyPlayerNeedsRole')
        return
      } else if (member.role === 'SNACKMAN') {
        snackmanCounter++
        memberCounter++
      } else if (member.role === 'GHOST') {
        memberCounter++
      }
    })

    if (snackmanCounter === 1 && memberCounter === lobby.members.length) {
      await lobbiesStore.startGame(lobby.lobbyId)
      buttons.forEach(button => (button.selected = false))
      buttons.forEach(button => (button.selectedBy = ''))
      selectedCharacter.value = null

      await router.push({
        name: 'GameView',
        query: {
          role: lobbiesStore.lobbydata.currentPlayer.role,
          lobbyId: lobbiesStore.lobbydata.currentPlayer.joinedLobbyId,
        },
      })
    } else {
      showPopUp.value = true
      darkenBackground.value = true
      infoText.value = t('chooseRole.error.exactlyOneSnackMan')
    }
  } else {
    showPopUp.value = true
    darkenBackground.value = true
    infoText.value = 'Only Admin can start the game!'
  }
}

watchEffect(() => {
  if (lobbiesStore.lobbydata && lobbiesStore.lobbydata.lobbies) {
    const updatedLobby = lobbiesStore.lobbydata.lobbies.find(
      lobby => lobby.lobbyId === lobbyUrl,
    )
    if (updatedLobby && updatedLobby.gameStarted) {
      const currentPlayerId = lobbiesStore.lobbydata.currentPlayer.playerId
      const currentPlayerInUpdatedLobby = updatedLobby.members.find(
        member => member.playerId == currentPlayerId,
      )

      if (currentPlayerInUpdatedLobby) {
        buttons.forEach(button => (button.selected = false))
        buttons.forEach(button => (button.selectedBy = ''))
        selectedCharacter.value = null
        router.push({
          name: 'GameView',
          query: {
            role: lobbiesStore.lobbydata.currentPlayer.role,
            lobbyId: lobbiesStore.lobbydata.currentPlayer.joinedLobbyId,
          },
        })
      }
    }
  }
})
</script>

<style scoped>
.title {
  width: 100%;
  top: 1rem;
  text-align: center;
  font-weight: bold;
  color: var(--main-text-color);
}

#button-box {
  width: 100%;
  display: flex;
  justify-content: center;
  padding: 10vh 0 1vh 0;
}

#player-count {
  margin-top: 0.5vh;
  font-size: 1.3rem;
  font-weight: bold;
  color: var(--main-text-color);
  text-align: center;
}

.outer-box {
  position: absolute;
  top: 15%;
  left: 50%;
  transform: translateX(-50%);
  width: 90%;
  height: 70%;
  background: rgba(255, 255, 255, 60%);
  border-radius: 0.5rem;
  padding: 0 20px;
}

.character-grid {
  padding-top: 5%;
  display: grid;
  grid-template-columns: repeat(5, 5fr);
  grid-gap: 20px;
}

#inner-box {
  display: flex;
  flex-direction: column;
  align-items: center;
}

:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.character-item {
  height: 100%;
  padding: 10px;
  background: rgba(255, 255, 255, 70%);
  text-align: center;
  cursor: pointer;
  display: flex;
  flex-direction: column;
}

.character-item:hover {
  background-color: var(--primary-highlight-color);
}

.character-item.selected {
  cursor: not-allowed;
  background-color: var(--primary-highlight-color);
}

.image-container {
  flex-grow: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
}

.character-image {
  max-width: 80%;
  object-fit: contain;
}

.character-name {
  font-weight: bold;
  color: var(--primary-text-color);
  margin: 0;
}

#start-game-button {
  position: absolute;
  bottom: 7%;
  left: 50%;
  transform: translate(-50%);
}

@media (max-width: 1300px) {
  .character-grid {
    display: grid;
    grid-template-columns: repeat(4, 5fr);
  }
}

@media (max-width: 1000px) {
  .title {
    top: 0.5rem;
    font-size: 60px;
  }

  .character-grid {
    display: grid;
    grid-template-columns: repeat(3, 5fr);
  }
}

@media (max-width: 800px) {
  .outer-box {
    height: 70%;
    top: 23%;
  }

  .title {
    top: 0.5rem;
    font-size: 40px;
  }

  .character-grid {
    display: grid;
    grid-template-columns: repeat(3, 5fr);
  }
}
</style>
