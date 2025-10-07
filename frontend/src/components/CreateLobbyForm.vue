<template>
  <div class="overlay"></div>
  <div id="individual-form-box-size" class="form-box">
    <h1 id="title"> {{ $t('createLobby.title') }} </h1>

    <form id="form" @submit.prevent="createLobby">
      <label>
        {{ $t('createLobby.form.label') }}
      </label>
      <input ref="lobbyInput" v-model.trim="lobbyName" placeholder="Lobbyname" type="text">
      <p
        v-if="errorMessage"
        id="error-message">

        {{ errorMessage }}
      </p>
    </form>

    <SmallNavButton
      id="cancel-lobby-creation-button"
      class="small-nav-buttons"
      @click="cancelLobbyCreation"
    >
      {{ $t('button.cancel') }}
    </SmallNavButton>
    <SmallNavButton
      id="create-lobby-button"
      class="small-nav-buttons"
      @click="createLobby"
    >
      {{ $t('button.createLobby') }}
    </SmallNavButton>
  </div>
</template>

<script setup lang="ts">
import SmallNavButton from '@/components/SmallNavButton.vue';
import {useRouter} from 'vue-router';
import {onMounted, ref} from 'vue';
import {useLobbiesStore} from '@/stores/Lobby/lobbiesstore';
import type {IPlayerClientDTD} from '@/stores/Lobby/IPlayerClientDTD';
import {useI18n} from 'vue-i18n';

const router = useRouter();
const lobbiesStore = useLobbiesStore();
const currentPlayer = lobbiesStore.lobbydata.currentPlayer as IPlayerClientDTD;

const lobbyName = ref('');
const lobbyInput = ref(); // needed for autofocus
const errorMessage = ref('');

const {t} = useI18n(); // needed for internationalization
// defines event wich can be triggered by this component
const emit = defineEmits<(event: 'cancelLobbyCreation') => void>()

/**
 * Emits an event to cancel the lobby creation process.
 *
 * @function cancelLobbyCreation
 * @returns {void}
 */
const cancelLobbyCreation = () => {
  emit('cancelLobbyCreation');
}

/**
 * Creates a new lobby with the specified name and admin client.
 * Validates the admin client and lobby name before attempting to create the lobby.
 * Alerts the user if there are any validation errors or if the lobby creation fails.
 * On success, redirects to the newly created lobby view.
 *
 * @async
 * @function createLobby
 * @throws {Error} Throws an alert if the admin client is invalid or the lobby name is empty or already taken.
 * @throws {Error} Shows a popup if there is an error creating the lobby.
 * @returns {void}
 */
const createLobby = async () => {
  const adminClient = currentPlayer;

  if (
    !adminClient ||
    adminClient.playerId === '' ||
    adminClient.playerName === ''
  ) {
    alert('Admin client is not valid!')
    return
  }

  if (!lobbyName.value.trim()) {
    errorMessage.value = t('createLobby.error.lobbyNameEmpty');
    return;
  }

  const isDuplicateName = lobbiesStore.lobbydata.lobbies.some(
    lobby => lobby.name === lobbyName.value.trim(),
  )

  if (isDuplicateName) {
    errorMessage.value =
      t('createLobby.error.lobbyNameDuplicate');
    return
  }

  try {
    const newLobby = await lobbiesStore.createLobby(
      lobbyName.value.trim(),
      adminClient,
    )
    if (newLobby && newLobby.lobbyId) {
      cancelLobbyCreation()
      router.push({name: 'LobbyView', params: {lobbyId: newLobby.lobbyId}})
    } else {
      throw new Error('Lobby creation returned invalid response.')
    }
  } catch (error) {
    console.error('Error:', error)
    alert('Error create Lobby!')
  }
}

onMounted(() => {
  lobbyInput.value.focus();
})

</script>

<style scoped>
#title {
  position: absolute;
  top: 1rem;
  left: 50%;
  transform: translateX(-50%);
  font-size: 2.3rem;
}

input::placeholder {
  color: var(--secondary-text-color);
  font-weight: bold;
}

#individual-form-box-size {
  left: 50%;
  top: 25%;
  transform: translateX(-50%);
  width: 50%;
  height: 30rem;
}

#form {
  position: inherit;
  display: flex;
  flex-direction: column;
  align-items: center;
  top: 35%;
  width: 100%;
  font-size: 1.5rem;
  font-weight: bold;
  color: var(--main-text-color);
}

#form > input {
  font-size: 1.2rem;
  width: auto;
  height: 2rem;
  margin: 1.2rem 0;
  padding: 1.2rem;
}

#error-message {
  font-size: 1.1rem;
  font-style: italic;
  margin-top: -1.8rem;
  color: red;
}

.small-nav-buttons {
  bottom: 7%;
}

#cancel-lobby-creation-button {
  left: 5%;
}

#create-lobby-button {
  right: 5%;
}
</style>
