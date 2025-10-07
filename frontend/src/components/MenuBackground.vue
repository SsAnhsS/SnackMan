<template>
  <div class="menu-background">
    <div v-if="isLobbyView" class="color-overlay"></div>
    <slot></slot>
  </div>
</template>

<script setup lang="ts">

// Prop to control the color&blur overlay
defineProps({
  isLobbyView: {
    type: Boolean,
    default: false,
  },
});
</script>

<style>
.menu-background {
  background: url('@/assets/background-design.png') no-repeat center center;
  background-size: cover;
  height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  position: relative;
}

.color-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(5px) brightness(0.8);
  pointer-events: none;
}

@media (max-width: 1280px) {
  .menu-background {
    background: url('@/assets/background-design-S.png') no-repeat center center;
    background-size: cover;
  }
}
</style>
