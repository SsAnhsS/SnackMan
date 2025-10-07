<template>
  <ViewBackground></ViewBackground>
  <div id="individual-outer-box-size" class="outer-box">
    <h1 class="result-title">{{ gameResult }}</h1>
    <p class="end-reason">{{ gameReason }}</p>
    <p class="end-reason">
      {{ $t('gameEnd.playingTime', { time: formatedPlayedTime }) }}
    </p>
    <div id="button-pair">
      <SmallNavButton id="menu-back-button" @click="backToMainMenu">
        {{ $t('button.backToMainMenu') }}
      </SmallNavButton>
      <SmallNavButton id="export-map-button" @click="downloadMap">
        {{ $t('button.exportMap') }}
      </SmallNavButton>
      <SmallNavButton
        v-if="
          !alreadyEntered &&
          lobbydata.currentPlayer.role == 'SNACKMAN' &&
          winningRole == 'SNACKMAN'
        "
        id="create-leaderboard-entry-button"
        @click="showCreateNewLeaderboardEntryForm"
      >
        {{ $t('button.createNewLeaderBoardEntry') }}
      </SmallNavButton>
    </div>
    <CreateNewLeaderboardEntryForm
      v-if="showCreateNewLeaderboardEntry"
      :playedTime="formatTime(playedTime)"
      @cancelNewLeaderboardEntryCreation="cancelNewLeaderboardEntryCreation"
      @entryCreated="hideCreateNewLeaderboardEntryForm"
    >
    </CreateNewLeaderboardEntryForm>
  </div>
  <img
    id="snackman"
    :src="
      winningRole === 'SNACKMAN'
        ? '/characters/kirby.png'
        : '/characters/kirby-monochrome.png'
    "
    alt="representation of snackman"
    class="character-image"
  />
  <div v-if="feedbackMessage" :class="['feedback-message', feedbackClass]">
    {{ feedbackMessage }}
  </div>
  <img
    id="ghost"
    :src="
      winningRole === 'GHOST'
        ? '/characters/ghost.png'
        : '/characters/ghost-monochrome.png'
    "
    alt="representation of ghost"
    class="character-image"
  />
</template>

<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import CreateNewLeaderboardEntryForm from '@/components/CreateNewLeaderboardEntryForm.vue'
import { useLobbiesStore } from '@/stores/Lobby/lobbiesstore'
import ViewBackground from '@/components/ViewBackground.vue'
import SmallNavButton from '@/components/SmallNavButton.vue'
import { SoundManager } from '@/services/SoundManager'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const route = useRoute()
const router = useRouter()
const showCreateNewLeaderboardEntry = ref(false)
const alreadyEntered = ref(false)
const { lobbydata } = useLobbiesStore()

// Read player role and game result from query parameters
const lobbyId = (route.query.lobbyId as string) || '-'
const winningRole = (route.query.winningRole as string) || '-'
const playedTime = (route.query.timePlayed as unknown as number) || 0
const formatedPlayedTime = formatTime(playedTime)
const kcalCollected = (route.query.kcalCollected as unknown as number) || 0
const gameResult = ref<string>('')

if (winningRole == 'SNACKMAN') {
  gameResult.value = t('gameEnd.gameResult.winner.snackman')
} else {
  gameResult.value = t('gameEnd.gameResult.winner.ghosts')
}

/**
 * Computes the reason for the game's result based on the winning role and game conditions.
 */

// Compute the game reason dynamically or display '-' if no data is available
const gameReason = computed(() => {
  if (winningRole === 'SNACKMAN') {
    return t('gameEnd.gameReason.calorieTarget')
  } else if (winningRole === 'GHOST' && kcalCollected < 0) {
    return t('gameEnd.gameReason.noCaloriesLeft')
  }
  return t('gameEnd.gameReason.timeIsUp')
})

const showLeaderboard = () => {
  if (winningRole && winningRole !== '-') {
    router.push({
      name: 'Leaderboard',
      query: { winningRole: winningRole },
    })
  } else {
    console.debug('no winning role')
    router.push({ name: 'Leaderboard' })
  }
}

const cancelNewLeaderboardEntryCreation = () => {
  showCreateNewLeaderboardEntry.value = false
}

const showCreateNewLeaderboardEntryForm = () => {
  showCreateNewLeaderboardEntry.value = true
}

const hideCreateNewLeaderboardEntryForm = () => {
  showCreateNewLeaderboardEntry.value = false
  alreadyEntered.value = true
  showLeaderboard() // show leaderboard after creating a new entry in it
}

const backToMainMenu = () => {
  router.push({ name: 'MainMenu' })
}

/**
 * Formats the time in seconds into a string in the format `MM:SS`.
 * @param {number} seconds - The time in seconds to be formatted.
 * @returns {string} The formatted time string.
 */
function formatTime(seconds: number): string {
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`
}

const feedbackMessage = ref('')
const feedbackClass = ref('')

const downloadMap = async () => {
  try {
    const response = await fetch(`/api/download?lobbyId=${lobbyId}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    })

    if (!response.ok) throw new Error('Failed to download map.')

    const blob = await response.blob()
    const link = document.createElement('a') // Create an anchor element to trigger the download
    const url = URL.createObjectURL(blob)
    link.href = url
    link.download = 'SnackManMap.txt'
    document.body.appendChild(link) // Append the link to the DOM
    link.click() // Simulate a click to start the download
    document.body.removeChild(link) // Remove the link after the download

    // Revoke the object URL to free up memory
    URL.revokeObjectURL(url)

    // Success feedback
    feedbackMessage.value = t('gameEnd.feedback.mapSaved')
    feedbackClass.value = 'success'
  } catch (error: any) {
    // Failure feedback
    feedbackMessage.value = t('gameEnd.feedback.mapNotSaved')
    feedbackClass.value = 'error'
  }
  // Clear feedback after 3 seconds
  setTimeout(() => {
    feedbackMessage.value = ''
    feedbackClass.value = ''
  }, 3000)
}

onMounted(() => {
  SoundManager.stopAllInGameSounds()
})
</script>

<style scoped>
#individual-outer-box-size {
  top: 10%;
  width: 80%;
  text-align: center;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 4rem;
}

.result-title {
  color: var(--primary-highlight-color);
  line-height: 1.1;
  margin-bottom: 3rem;
  font-weight: bold;
}

.end-reason {
  color: var(--main-text-color);
  font-size: 2rem;
  margin-bottom: 2rem;
}

#button-pair {
  width: 100%;
  justify-content: space-around;
  display: flex;
  flex-direction: row;
  padding-top: 2em;
}

.character-image {
  position: absolute;
  bottom: 6%;
  width: 20%;
  height: auto;
  z-index: 10;
}

#snackman {
  left: 6%;
}

#ghost {
  right: 6%;
}

.feedback-message {
  position: absolute;
  bottom: 0;
  width: 100%;
  height: 22%;
  font-size: 2rem;
  margin-top: 4rem;
  font-weight: bold;
  padding: 10px 20px;
  border-radius: 5px;
  text-align: center;
  align-content: center;
  animation: fadeIn 0.5s;
  z-index: 5;
}

.feedback-message.success {
  color: #fff;
  background-color: #50c878;
}

.feedback-message.error {
  color: #fff;
  background-color: #c70039;
}

/* Fade-in animation */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@media (min-width: 2500px) {
  #individual-outer-box-size {
    top: 30%;
    width: 50%;
  }

  .character-image {
    bottom: 10%;
  }

  #snackman {
    left: 10%;
  }

  #ghost {
    right: 10%;
  }
}

@media (min-width: 1900px) and (max-width: 2499px) {
  #individual-outer-box-size {
    top: 20%;
    width: 60%;
  }
}

@media (max-width: 1000px) {
  .result-title {
    font-size: 60px;
  }
}
</style>
