<template>
  <ViewBackground></ViewBackground>
  <div id="individual-outer-box-size" class="outer-box">
    <h1>{{ $t('leaderBoard.title') }}</h1>
    <div class="table-container">
      <table>
        <thead>
        <tr>
          <td><!-- Should be empty because the placement does not require a headline --></td>
          <td>Name</td>
          <td> {{ $t('leaderBoard.date') }}</td>
          <td> {{ $t('leaderBoard.duration') }}</td>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(entry, index) in leaderboardEntries">
          <td>{{ index + 1 }}</td>
          <td>{{ entry.name }}</td>
          <td>{{ entry.releaseDate }}</td>
          <td>{{ entry.duration }}</td>
        </tr>
        </tbody>
      </table>
    </div>
    <SmallNavButton
      id="menu-back-button"
      class="small-nav-buttons"
      @click="backToMainMenu"
    >
      {{ $t('button.back') }}
    </SmallNavButton>
  </div>
  <img
    id="snackman"
    :src="
      gameResult === 'SNACKMAN'
        ? '/characters/kirby.png'
        : '/characters/kirby-monochrome.png'
    "
    alt="representation of snackman"
    class="character-image"
  />
  <img
    id="ghost"
    :src="
      gameResult === 'GHOST'
        ? '/characters/ghost.png'
        : '/characters/ghost-monochrome.png'
    "
    alt="representation of ghost"
    class="character-image"
  />
</template>

<script lang="ts" setup>
import {useLeaderboardStore} from '@/stores/Leaderboard/leaderboardStore'
import {computed, onMounted, ref} from 'vue'
import SmallNavButton from '@/components/SmallNavButton.vue'
import {useRoute, useRouter} from 'vue-router'
import ViewBackground from "@/components/ViewBackground.vue";

const leaderboardStore = useLeaderboardStore()
const router = useRouter()
const route = useRoute()
const gameResult = ref<'SNACKMAN' | 'GHOST' | null>(null)

const backToMainMenu = () => {
  router.push({name: 'MainMenu'})
}

/**
 * Load the leaderboard data, initialize stomp message updates and look for winner
 */
onMounted(async () => {
  await leaderboardStore.initLeaderboardStore()
  await leaderboardStore.startLeaderboardUpdate()

  const winningRole = route.query.winningRole as string | undefined

  switch (winningRole) {
    case 'SNACKMAN':
      gameResult.value = 'SNACKMAN'
      break
    case 'GHOST':
      gameResult.value = 'GHOST'
      break
    default:
      gameResult.value = null
      break
  }
})

const leaderboardEntries = computed(
  () => leaderboardStore.leaderboard.leaderboardEntries,
)
</script>

<style scoped>
#individual-outer-box-size {
  top: 10% !important;
  width: 50%;
  height: 70%;
  max-height: 50rem;
  padding: 30px;
}

table {
  table-layout: fixed;
  width: 100%;
  border-collapse: separate;
  border-spacing: 0 15px;
  overflow-x: auto;
}

tr {
  font-size: 1.2rem;
}

thead tr td {
  background: transparent;
  color: var(--main-text-color);
  font-weight: bold;
  border: none;
  padding: 0 10px;
}

thead tr td {
  background: transparent;
  vertical-align: bottom;
  color: var(--main-text-color);
  font-weight: bold;
  border: none;
  padding: 5px 10px;
  box-shadow: none;
}

thead tr td:first-child,
thead tr td:last-child {
  border: none;
}

thead tr td:not(:first-child):not(:last-child) {
  border: none;
}

tbody tr td {
  margin: 5px 0;
  padding: 5px 10px;
  background: var(--background-for-text-color);
  box-shadow: 10px 8px 0 var(--primary-text-color);
  color: var(--primary-text-color);
}

tr td:first-child,
td:last-child {
  border: 4px solid var(--primary-text-color);
}

tr td:first-child {
  width: 7%;
  text-align: right;
  border-right: none;
  border-top-left-radius: 0.1rem;
  border-bottom-left-radius: 0.1rem;
}

tr td:last-child {
  width: 15%;
  text-align: center;
  border-left: none;
  border-top-right-radius: 0.1rem;
  border-bottom-right-radius: 0.1rem;
}

tr td:nth-child(3) {
  width: 20%;
  text-align: center;
}

tr td:not(:first-child):not(:last-child) {
  border-top: 4px solid var(--primary-text-color);
  border-bottom: 4px solid var(--primary-text-color);
}

.table-container {
  max-height: 400px;
  overflow-y: auto;
}

#menu-back-button {
  position: absolute;
  bottom: 3%;
  left: 45%;
}

.character-image {
  position: absolute;
  bottom: 3%;
  width: 280px;
  height: auto;
  z-index: 10;
}

#snackman {
  left: 15%;
}

#ghost {
  right: 15%;
}

@media (min-width: 3000px) {
  #individual-outer-box-size {
    top: 20% !important;
    width: 40%;
  }

  .character-image {
    width: 400px;
    bottom: 20% !important;
  }

  #snackman {
    left: 25% !important;
  }

  #ghost {
    right: 25% !important;
  }
}

@media (min-width: 3000px) and (min-height: 1400px) {
  .character-image {
    width: 350px;
    bottom: 30%;
  }

  #snackman {
    left: 10%;
  }

  #ghost {
    right: 10%;
  }
}

@media (min-width: 1900px) and (max-width: 2999px) {
  #individual-outer-box-size {
    top: 15% !important;
  }

  .character-image {
    width: 350px;
    bottom: 7%;
  }

  #snackman {
    left: 12%;
  }

  #ghost {
    right: 12%;
  }
}

@media (min-width: 1900px) and (min-height: 1400px) {
  .character-image {
    width: 350px;
    bottom: 30%;
  }

  #snackman {
    left: 11%;
  }

  #ghost {
    right: 11%;
  }
}

@media (min-width: 1500px) and (max-width: 1899px) {
  #individual-outer-box-size {
    top: 12% !important;
    height: 75%;
  }

  #snackman {
    left: 9%;
  }

  #ghost {
    right: 9%;
  }
}
</style>
