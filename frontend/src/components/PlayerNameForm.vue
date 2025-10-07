<template>
  <div class="overlay"></div>
  <div id="individual-form-box-size" class="form-box">
    <form id="form" @submit.prevent="savePlayerName">
      <label id="label-text">
        {{ $t('savePlayerName.label') }}
      </label>
      <input
        v-model.trim="playerName"
        autofocus
        maxlength="16"
        :placeholder="$t('savePlayerName.placeholder')"
        type="text">
      <p
        v-if="errorMessage"
        id="error-message">
        {{ errorMessage }}
      </p>
    </form>

    <SmallNavButton
      id="save-name-button"
      class="small-nav-button"
      @click="savePlayerName">
      {{ $t('button.save') }}
    </SmallNavButton>
  </div>
</template>

<script setup lang="ts">
import SmallNavButton from '@/components/SmallNavButton.vue';
import {ref} from 'vue';
import {useLobbiesStore} from '@/stores/Lobby/lobbiesstore';
import {SoundManager} from "@/services/SoundManager";
import {SoundType} from "@/services/SoundTypes";
import {useI18n} from 'vue-i18n';

const lobbiesStore = useLobbiesStore();

const playerName = ref('');
const errorMessage = ref('');

const {t} = useI18n();

const emit = defineEmits<{
  (e: 'hidePlayerNameForm'): void;
  (e: 'playerNameSaved', value: string): void;
}>();


/**
 * Saves the name of a player.
 * Validates the admin client and playerName before attempting to save the playerName.
 * Alerts the user if there are any validation errors or if the save fails.
 * On success, hides the popup and shows the main menu.
 *
 * @async
 * @function savePlayerName
 * @throws {Error} Shows a popup if there is an error while saving the playerName.
 * @returns {void}
 */
const savePlayerName = async () => {
  if (!playerName.value.trim()) {
    errorMessage.value = t('savePlayerName.error.playerNameEmpty');
    return;
  }

  try {
    lobbiesStore.createPlayer(playerName.value);
    errorMessage.value = "";

    emit('hidePlayerNameForm');
    emit('playerNameSaved', playerName.value);

  } catch (error) {
    alert("Error saving playername");
    console.error(error);
  }

  try {
    await SoundManager.initBackgroundMusicManager()
    SoundManager.stopAllInGameSounds()
    SoundManager.playSound(SoundType.LOBBY_MUSIC)
  } catch (error) {
    console.error(error);
  }
}

</script>

<style scoped>
input::placeholder {
  color: var(--secondary-text-color);
  font-weight: bold;
}

#individual-form-box-size {
  left: 50%;
  top: 25%;
  transform: translateX(-50%);
  width: 30%;
  height: 30rem;
}

#form {
  top: 30%;
  width: 100%;
  font-size: 1.5rem;
  font-weight: bold;
  color: var(--main-text-color);
  display: flex;
  flex-direction: column;
  align-items: center;
}

#form > input {
  font-size: 1.2rem;
  width: 70%;
  height: 2rem;
  margin-top: 0.7rem;
  margin-bottom: 2rem;
  padding: 1.2rem;
}

#label-text {
  font-size: 1.8rem;
  display: block;
  margin-bottom: 1rem;
  text-align: center;
}

#form > label > input {
  font-size: 1.2rem;
  width: 90%;
  height: 2rem;
  padding: 1.2rem;
}

#error-message {
  font-size: 1.1rem;
  font-style: italic;
  margin-top: -1.6rem;
  color: var(--accent-color);
}

#save-name-button {
  bottom: 20%;
  left: 50%;
  transform: translate(-50%);
}

@media (max-width: 1900px) {
  #individual-form-box-size {
    width: 50%;
  }
}

@media (max-width: 1440px) {
  #individual-form-box-size {
    width: 70%;
  }
}
</style>
