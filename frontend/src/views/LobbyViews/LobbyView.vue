<template>
  <MenuBackground></MenuBackground>
  <LanguageSwitch></LanguageSwitch>

  <div id="individual-outer-box-size" class="outer-box">
    <div class="item-row">
      <h1 class="title">{{ lobby?.name || 'Lobby Name' }}</h1>

      <div id="player-count">
        {{ playerCount }} / {{ MAX_PLAYER_COUNT }}
        {{ $t('lobby.playerCount.player') }}
      </div>
    </div>

    <div class="inner-box">
      <ul>
        <li v-for="member in members" class="player-list-items">
          <div class="player-name">
            {{ member.playerName.replace(/"/g, '') }}
            <!-- replace all " in String using RegEx modifier /g (find all) -->
          </div>
        </li>
      </ul>
    </div>

    <div class="item-row">
      <ul class="map-list" v-if="playerId == adminClientId">
        <li
          v-for="map in mapList"
          v-if="mapList.length > 1"
          :key="map.mapName"
          class="map-list-item"
        >
          <input
            :checked="selectedMap === map.mapName"
            :value="map.mapName"
            class="map-choose"
            type="radio"
            @change="selectMap(map.mapName)"
          />
          <span>{{ $t(map.translation) }}</span>
        </li>
      </ul>
    </div>

    <div class="bottom-item-row">
      <div id="button-pair">
        <SmallNavButton
          id="menu-back-button"
          class="small-nav-buttons"
          @click="leaveLobby"
        >
          {{ $t('button.leaveLobby') }}
        </SmallNavButton>
        <SmallNavButton
          id="menu-map-importieren"
          class="small-nav-button"
          v-if="playerId == adminClientId"
          @click="triggerFileInput"
        >
          {{ $t('button.importMap') }}
        </SmallNavButton>
        <input
          ref="fileInput"
          accept=".txt"
          class="input-feld"
          type="file"
          @change="handleFileImport"
        />
      </div>

      <div id="button-pair">
        <SmallNavButton
          id="copyToClip"
          class="small-nav-buttons"
          @click="copyToClip()"
        >
          {{ $t('button.copyLink') }}
        </SmallNavButton>

        <SmallNavButton
          id="start-game-button"
          class="small-nav-buttons"
          @click="chooseRole(lobby)"
        >
          {{ $t('button.startGame') }}
        </SmallNavButton>
      </div>
    </div>
  </div>

  <div v-if="darkenBackground" id="darken-background"></div>

  <PlayerNameForm
    v-if="showPlayerNameForm && !playerNameSaved"
    @hidePlayerNameForm="hidePlayerNameForm"
    @playerNameSaved="savePlayerName"
  >
  </PlayerNameForm>

  <PopUp
    v-if="errorBox && lobbiesStore.lobbydata.currentPlayer.playerName"
    class="popup-box"
    @hidePopUp="hidePopUpAndRedirect"
  >
    <p class="info-heading">{{ infoHeading }}</p>
    <p class="info-text">{{ infoText }}</p>
  </PopUp>

  <PopUp v-if="showPopUp" class="popup-box" @hidePopUp="hidePopUp">
    <p class="info-heading">{{ infoHeading }}</p>
    <p class="info-text">{{ infoText }}</p>
  </PopUp>

  <PopUp v-if="showRolePopup" class="popup-box" @hidePopUp="hidePopUp">
    <p class="info-heading">{{ $t('popup.cantStart.heading') }}</p>
    <p class="info-text">{{ infoText }}</p>
  </PopUp>

  <div v-show="showInfo" id="infoBox">{{ infoText }}</div>
</template>

<script lang="ts" setup>
import MenuBackground from '@/components/MenuBackground.vue'
import SmallNavButton from '@/components/SmallNavButton.vue'
import PlayerNameForm from '@/components/PlayerNameForm.vue'
import PopUp from '@/components/PopUp.vue'
import LanguageSwitch from '@/components/LanguageSwitch.vue'

import {useRoute, useRouter} from 'vue-router'
import {computed, onMounted, ref, watchEffect} from 'vue'
import {useLobbiesStore} from '@/stores/Lobby/lobbiesstore'
import type {IPlayerClientDTD} from '@/stores/Lobby/IPlayerClientDTD'
import type {ILobbyDTD} from '@/stores/Lobby/ILobbyDTD'
import {useI18n} from 'vue-i18n'

const {t} = useI18n()

const router = useRouter()
const route = useRoute()
const lobbiesStore = useLobbiesStore()

const playerId = lobbiesStore.lobbydata.currentPlayer.playerId
const lobbyId = route.params.lobbyId as string

const lobbyUrl = route.params.lobbyId
let lobbyLoaded = false
const lobby = computed(() =>
  lobbiesStore.lobbydata.lobbies.find(l => l.lobbyId === lobbyUrl),
)
const adminClientId = lobby.value?.adminClient.playerId
const members = computed(
  () => lobby.value?.members || ([] as Array<IPlayerClientDTD>),
)
const playerCount = computed(() => members.value.length)
const playerNameSaved = lobbiesStore.lobbydata.currentPlayer.playerName
const showPlayerNameForm = ref(false)

const darkenBackground = ref(false)
const showPopUp = ref(false)
const showRolePopup = ref(false)
const errorBox = ref(false)
const infoText = ref()
const infoHeading = ref()
const showInfo = ref(false)

const mouseX = ref(0)
const mouseY = ref(0)

const mouseInfoBox = ref(document.getElementById('infoBox'))

const MAX_PLAYER_COUNT = 5

const TIP_TOP_DIST = 30
const TIP_SIDE_DIST = 20

const hidePlayerNameForm = () => {
  showPlayerNameForm.value = false

  if (!errorBox.value) {
    darkenBackground.value = false
  }
}

const hidePopUp = () => {
  showPopUp.value = false
  darkenBackground.value = false
}

// needed for errorBox which shows up when lobby does not exist
function hidePopUpAndRedirect() {
  hidePopUp()
  router.push({name: 'LobbyListView'})
}

const mapList = ref<
  { mapName: string; fileName: string; translation: string }[]
>([
  {
    mapName: 'Generated Map',
    fileName: `Maze.txt`,
    translation: 'lobby.mapName.generated',
  },
])

const usedCustomMap = ref(false)
const selectedMap = ref<string | null>(null)

const selectMap = async (mapName: string) => {
  selectedMap.value = mapName

  if (selectedMap.value === 'Generated Map') {
    usedCustomMap.value = false
  } else if (selectedMap.value === 'Uploaded Map') {
    usedCustomMap.value = true
  }

  const status = await changeUsedMapStatus(lobbyId, usedCustomMap.value)
  if (status !== 'done') {
    showPopUp.value = true
    darkenBackground.value = true
    infoHeading.value = 'Map Status Error'
    infoText.value = 'Failed to update the map status.'
  }
}

const triggerFileInput = () => {
  fileInput.value?.click()
}

const fileInput = ref<HTMLInputElement | null>(null)

/**
 * This function processes the file selected by the user in an input field.
 * It ensures the file is a `.txt` file.
 * If the file is valid, it triggers an upload to the server.
 * Otherwise, it displays a popup with error information.
 *
 * @param event - The event triggered by the file input change.
 */
const handleFileImport = (event: Event) => {
  const input = event.target as HTMLInputElement

  if (input.files && input.files.length > 0) {
    const file = input.files[0]
    if (file.name.endsWith('.txt')) {
      uploadFileToServer(file, lobbyId)
    } else {
      showPopUp.value = true
      darkenBackground.value = true
      infoHeading.value = t('popup.mapNotValid.heading')
      infoText.value = t('popup.mapNotValid.text')
    }
  }
}

/**
 * Upload file to server
 * @param file - new custom map in file .txt
 * @param lobbyId - The unique identifier of the lobby.
 */
const uploadFileToServer = async (file: File, lobbyId: string) => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('lobbyId', lobbyId)

  try {
    const response = await fetch('/api/upload', {
      method: 'POST',
      body: formData,
    })

    if (response.ok) {
      const mapName = 'Uploaded Map'
      const fileName = `SnackManMap_${lobbyId}.txt`
      const translation = 'lobby.mapName.uploaded'

      if (mapList.value.length > 1) {
        mapList.value[1] = {mapName, fileName, translation}
      } else {
        mapList.value.push({mapName, fileName, translation})
      }

      selectMap(mapName)
    } else {
      const errorMessage = await response.text()
      const translatedErrorMessage = t(errorMessage)
      showPopUp.value = true
      darkenBackground.value = true
      infoHeading.value = t('popup.mapNotValid.heading')
      infoText.value = translatedErrorMessage
    }
  } catch (error) {
    console.error('Error uploading file:', error)
    showPopUp.value = true
    darkenBackground.value = true
    infoHeading.value = t('popup.uploadingFile.heading')
    infoText.value = error
  }
}

/**
 * Delete uploaded File, when the lobby doesn't exist anymore.
 * @param lobbyId - The unique identifier of the lobby.
 */
const deleteUploadedFile = async (lobbyId: string) => {
  const formData = new FormData()
  formData.append('lobbyId', lobbyId)

  fetch('/api/deleteMap', {
    method: 'DELETE',
    body: formData,
  })
    .then(response => {
      if (!response.ok) {
        console.error('Error deleting file:', response.text())
      }
    })
    .catch(error => {
      console.error('Error deleting file:', error)
    })
}

/**
 * Sends a request to update the used map status for a specific lobby.
 *
 * @param {string} lobbyId - The unique identifier of the lobby.
 * @param {boolean} usedCustomMap - Indicates whether a custom map is used (true) or not (false).
 */
const changeUsedMapStatus = async (
  lobbyId: string,
  usedCustomMap: boolean,
): Promise<string> => {
  try {
    const response = await fetch('/api/change-used-map-status', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({lobbyId, usedCustomMap}),
    })

    if (!response.ok) {
      const errorText = await response.text()
      console.error('Error changing the used map status:', errorText)
      throw new Error(`Failed to change map status: ${errorText}`)
    }

    return 'done'
  } catch (error) {
    console.error('Error changing the used map status:', error)
    throw error
  }
}

watchEffect(() => {
  if (lobbiesStore.lobbydata && lobbiesStore.lobbydata.lobbies) {
    const updatedLobby = lobbiesStore.lobbydata.lobbies.find(
      l => l.lobbyId === lobbyUrl,
    )
    if (updatedLobby) {
      lobbyLoaded = true
    } else if (lobbyLoaded) {
      deleteUploadedFile(lobbyId)
      router.push({name: 'LobbyListView'})
    }
  }
})

onMounted(async () => {
  await lobbiesStore.fetchLobbyList()

  if (!lobby.value) {
    infoHeading.value = t('popup.notExisting.heading')
    infoText.value = t('popup.notExisting.text')
    errorBox.value = true
    darkenBackground.value = true
  }
  await lobbiesStore.startLobbyLiveUpdate()

  // player joined via link
  if (
    !lobbiesStore.lobbydata.currentPlayer ||
    lobbiesStore.lobbydata.currentPlayer.playerId === '' ||
    lobbiesStore.lobbydata.currentPlayer.playerName === ''
  ) {
    // save name to create player, no matter if lobby full or not
    showPlayerNameForm.value = true
    darkenBackground.value = true

    if (lobby.value!.members.length >= MAX_PLAYER_COUNT) {
      infoHeading.value = t('popup.lobbyFull.heading')
      infoText.value = t('popup.lobbyFull.text')
      errorBox.value = true
      darkenBackground.value = true
    }
  }
})

const savePlayerName = async (newName: string) => {
  try {
    await lobbiesStore.createPlayer(newName)
    await joinLobby(lobby.value!)
  } catch (error) {
    console.error('Error saving playerName:', error)
    alert('Error saving playerName!')
  }
}

const joinLobby = async (lobby: ILobbyDTD) => {
  try {
    const joinedLobby = await lobbiesStore.joinLobby(
      lobby.lobbyId,
      lobbiesStore.lobbydata.currentPlayer.playerId,
    )

    if (joinedLobby) {
      router.push({name: 'LobbyView', params: {lobbyId: lobby.lobbyId}})
    }
  } catch (error: any) {
    console.error('Error:', error)
  }
}

/**
 * Leaves the current lobby. If the player is the admin, it will remove other members from the lobby first.
 * After leaving the lobby, the user is redirected to the Lobby List View.
 *
 * @async
 * @function leaveLobby
 * @throws {Error} If the player or lobby is not found.
 */
const leaveLobby = async () => {
  const playerId = lobbiesStore.lobbydata.currentPlayer.playerId
  if (!playerId || !lobby.value) {
    console.error('Player or Lobby not found')
    return
  }

  if (playerId === lobby.value.adminClient.playerId) {
    for (const member of lobby.value.members) {
      if (member.playerId !== playerId) {
        await lobbiesStore.leaveLobby(lobby.value.lobbyId, member.playerId)
      }
    }
    // If Admin-Player leave Lobby, delete the uploaded map
    deleteUploadedFile(lobbyId)
  }

  await lobbiesStore.leaveLobby(lobby.value.lobbyId, playerId)
  router.push({name: 'LobbyListView'})
}

const chooseRole = async (lobby: ILobbyDTD | undefined) => {
  const playerId = lobbiesStore.lobbydata.currentPlayer.playerId

  if (!lobby) {
    return
  }

  if (!playerId || !lobby.members) {
    console.error('Player or Lobbymembers not found')
    return
  }

  if (lobby.members.length < 2) {
    showPopUp.value = true
    darkenBackground.value = true
    infoText.value = t('popup.cantStart.notEnoughPlayers')
    return
  }

  if (playerId === lobby.adminClient.playerId) {
    await lobbiesStore.chooseRole(lobby.lobbyId)
  } else {
    showPopUp.value = true
    darkenBackground.value = true
    infoText.value = t('popup.cantStart.onlyAdminCanInit')
  }
}

function copyToClip() {
  navigator.clipboard.writeText(document.URL)
  infoText.value = t('lobby.linkCopied')
  showInfo.value = true
  mouseInfoBox.value = document.getElementById('infoBox')
  moveToMouse(mouseInfoBox.value!)
  setTimeout(() => {
    showInfo.value = false
  }, 1000)
}

window.onmousemove = function (e) {
  mouseX.value = e.clientX
  mouseY.value = e.clientY
  if (showInfo.value) {
    moveToMouse(mouseInfoBox.value!)
  }
}

function moveToMouse(element: HTMLElement) {
  const offset = mouseInfoBox.value!.parentElement!.getBoundingClientRect()
  element.style.top = mouseY.value - offset.top - TIP_TOP_DIST + 'px'
  element.style.left = mouseX.value - offset.left + TIP_SIDE_DIST + 'px'
}
</script>

<style scoped>
.title {
  font-size: 3rem;
  font-weight: bold;
  color: var(--main-text-color);
  text-align: left;
}

#individual-outer-box-size {
  width: 60%;
  height: 70%;
  padding: 2%;
  top: 15%;
}

#infoBox {
  position: absolute;
  border-radius: 0.5rem;
  background: rgba(255, 255, 255, 60%);
  color: var(--primary-text-color);
}

#player-count {
  font-size: 3rem;
  font-weight: bold;
  color: var(--main-text-color);
  text-align: right;
}

.inner-box {
  position: relative;
  margin-top: 1vh;
  margin-bottom: 1vh;
  left: 50%;
  transform: translateX(-50%);
  height: auto;
  border-radius: 0.3rem;
  color: var(--primary-text-color);
}

.inner-box > ul {
  list-style: none;
  display: flex;
  flex-direction: column;
  left: 50%;
  transform: translateX(-50%);
  margin: 0;
  padding: 0;
  width: 100%;
  min-height: 30vh;
}

.player-list-items {
  display: flex;
  justify-content: space-between;
  background: var(--background-for-text-color);
  border: 4px solid var(--primary-text-color);
  border-radius: 0.1rem;
  box-shadow: 4px 3px 0 var(--primary-text-color);
  font-size: 1.2rem;
  padding: 0.5rem 0.8rem;
  margin: 0.4rem 0;
}

.player-list-items:hover {
  cursor: pointer;
}

.info-heading {
  font-size: 3rem;
  font-weight: bold;
}

.info-text {
  font-size: 1.8rem;
}

.item-row {
  display: flex;
  justify-content: space-between;
}

.bottom-item-row {
  position: absolute;
  bottom: 5%;
  left: 2%;
  padding: 0 5%;
  width: 100%;
  display: flex;
  justify-content: space-around;
}

#button-pair {
  display: flex;
  gap: 20px;
}

.map-list {
  margin-top: 1vh;
  margin-bottom: 1vh;
  display: flex;
  list-style: none;
  padding: 0;
}

.map-list-item {
  margin-right: 40px;
  display: flex;
  align-items: center;
  gap: 20px;
  font-size: 1.5rem;
  color: white;
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

.map-choose {
  width: 20px;
  height: 20px;
  transform: scale(1.5);
}

.input-feld {
  display: none;
}
</style>
