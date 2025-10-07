<template>
  <div class="page-overlay">
    <div class="background"></div>
  </div>
</template>

<script lang="ts" setup></script>

<style scoped>
.page-overlay {
  width: 100vw;
  height: 100vh;
  position: relative;
  overflow: hidden;
}

.background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: var(--primary-highlight-color);
  background-image: url('@/assets/background-overlay-bars.png');
  background-size: cover;
  background-position: center;
  z-index: -10;
}
</style>
