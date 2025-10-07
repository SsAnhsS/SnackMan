<template>
  <button class="small-nav-button">
    <slot>Nav</slot>
    <!-- slot = default, will be overwritten -->
  </button>
</template>

<script lang="ts" setup></script>

<style scoped>
.small-nav-button {
  background-color: var(--background-for-text-color);
  position: inherit;
  font-size: 1.3rem;
  font-weight: bold;
  width: auto;
  padding: 0.8rem 1.2rem;
  height: auto;
  text-align: center;
  cursor: pointer;
  border: var(--primary-text-color) solid 4px;
  border-radius: 0.2rem;
  box-shadow: 4px 3px 0 var(--primary-text-color);
  font-family: var(--font-main);
}

.small-nav-button:hover {
  background-color: var(--primary-highlight-color);
}

.small-nav-button:disabled:hover {
  background-color: var(--background-for-text-color); /* Keine Hover-Farbe */
}
</style>
