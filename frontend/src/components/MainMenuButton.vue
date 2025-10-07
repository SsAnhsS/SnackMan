<template>
  <button class="button-mainmenu" @click="handleClick">
    <slot></slot>
  </button>
</template>

<script setup lang="ts">

defineProps({
  type: {
    type: String,
    default: 'button',
  },
})

const emit = defineEmits(['click'])

const handleClick = () => {
  emit('click')
}
</script>

<style>
.button-mainmenu {
  display: inline-flex;
  padding: 1.5rem 3rem;
  justify-content: center;
  align-items: center;
  gap: 0.625rem;

  border-radius: 0.5rem;
  border: 4px solid var(--primary-text-color);
  background: var(--background-for-text-color);
  box-shadow: 8px 8px 0px 0px var(--primary-text-color);

  font-size: 2.5rem;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease-in-out;
}

.button-mainmenu:hover {
  background: var(--primary-highlight-color);
  color: var(--primary-text-color);
}

@media (max-width: 1280px) {
  .button-mainmenu {
    padding: 1.25rem 2.5rem;
    font-size: 2rem;
    box-shadow: 6px 6px 0px 0px var(--primary-text-color);
  }
}
</style>
