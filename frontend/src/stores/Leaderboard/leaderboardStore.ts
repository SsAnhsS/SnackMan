import {defineStore} from 'pinia';
import {reactive, readonly} from "vue";
import {Client} from "@stomp/stompjs";
import type {IFrontendLeaderboardEntryMessageEvent} from "@/services/IFrontendMessageEvent";
import type {Leaderboard, LeaderboardDTD} from "@/stores/Leaderboard/LeaderboardEntryDTD";
import {fetchLeaderboardDataFromBackend} from "@/services/LeaderboardInitDataService";
import type {LeaderboardEntry} from "@/stores/Leaderboard/LeaderboardDTD";

/**
 * Defines the pinia store used for saving the leaderboard from
 * the backend. Updates of the leaderboard entries are saved here and
 * updated in the store itself.
 */
export const useLeaderboardStore = defineStore('leaderboard', () => {
  let leaderboardStompClient: Client

  const leaderboard = reactive({
    leaderboardEntries: []
  } as Leaderboard)

  /**
   * Initializes the leaderboard store by fetching leaderboard data from the backend.
   * The fetched leaderboard entries are saved in the store's state.
   *
   * @throws {Error} Throws an error if fetching leaderboard data fails.
   */
  async function initLeaderboardStore() {
    try {
      const response: LeaderboardDTD = await fetchLeaderboardDataFromBackend()

      leaderboard.leaderboardEntries = []
      for (const entry of response.leaderboardEntries) {
        leaderboard.leaderboardEntries.push(entry as LeaderboardEntry)
      }

      console.debug("Initialised leaderboard data {}", leaderboard)
    } catch (reason) {
      throw reason
    }
  }

  /**
   * Starts the leaderboard update process via WebSocket connection.
   * This establishes a connection to the STOMP broker and listens for real-time updates
   * to leaderboard entries, updating the store's state upon receiving new messages.
   *
   * @throws {Error} Throws an error if WebSocket or STOMP client connection fails.
   */
  async function startLeaderboardUpdate() {
    const protocol = window.location.protocol.replace('http', 'ws')
    const wsurl = `${protocol}//${window.location.host}/ws`
    const DEST_SQUARE = '/topic/leaderboard'

    if (!leaderboardStompClient) {
      leaderboardStompClient = new Client({brokerURL: wsurl})

      leaderboardStompClient.onWebSocketError = (event) => {
        throw new Error('Websocket wit message: ' + event)
      }

      leaderboardStompClient.onStompError = (frameElement) => {
        throw new Error('Stompclient with message: ' + frameElement)
      }

      leaderboardStompClient.onConnect = (frameElement) => {
        console.log('Stompclient connected')

        leaderboardStompClient.subscribe(DEST_SQUARE, async (message) => {
          const change: IFrontendLeaderboardEntryMessageEvent = JSON.parse(message.body)
          leaderboard.leaderboardEntries.push(change.leaderboardEntry as LeaderboardEntry)
          sortLeaderboard()
        })
      }

      leaderboardStompClient.onDisconnect = () => {
        console.log('Stompclient disconnected.')
      }

      leaderboardStompClient.activate()
    }
  }

  /**
   * Adds a new leaderboard entry to the backend by sending a POST request.
   *
   * @param {LeaderboardEntry} data - The leaderboard entry data to be added.
   *
   * @throws {Error} Throws an error if the POST request fails.
   */
  async function addNewLeaderboardEntry(data: LeaderboardEntry) {
    const url = 'api/leaderboard/new/entry'

    const requestOptions: RequestInit = {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify(data)
    }

    try {
      const response = await fetch(url, requestOptions);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
    } catch (error) {
      console.error('Error:', error);
    }
  }

  /**
   * Sorts the leaderboard entries based on their duration, release date, and name.
   * The leaderboard is sorted in ascending order of duration first, followed by release date,
   * and finally by name.
   */
  function sortLeaderboard() {
    leaderboard.leaderboardEntries.sort((a, b) => {
      const durationComparison = a.duration.localeCompare(b.duration);
      if (durationComparison !== 0) {
        return durationComparison;
      }
      const releaseComparison = a.releaseDate.localeCompare(b.releaseDate);
      if (releaseComparison !== 0) {
        return releaseComparison;
      }
      return a.name.localeCompare(b.name);
    })
  }

  return {
    leaderboard: readonly(leaderboard as Leaderboard),
    initLeaderboardStore,
    startLeaderboardUpdate,
    addNewLeaderboardEntry
  }
})
