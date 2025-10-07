<template>
  <button class="map-button" @click="handleClick">
    <slot></slot>
  </button>
</template>

<script setup lang="ts">

const handleClick = () => {
};

</script>

<style scoped>
.map-button {
  position: relative;
  padding: 1rem 2rem;
  border: none;
  border-radius: 0.3125rem;
  background: var(--background-for-text-color);
  color: var(--primary-text-color);
  font-size: 1rem;
  font-weight: bold;
  cursor: pointer;
  transition: box-shadow 0.3s ease, transform 0.3s ease;
}

.map-button:hover {
  box-shadow: 0px 0px 40px 5px rgba(255, 255, 255, 0.7);
}
</style>
