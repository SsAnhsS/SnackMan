<template>
  <div id="language-container">
    <img alt="English" class="language-icon" src="@/assets/uk_flag.svg" @click="changeLocale('en')">
    <img alt="German" class="language-icon" src="@/assets/german_flag.svg" @click="changeLocale('de')">
  </div>
</template>

<script setup lang="ts">
import {i18n} from '@/main';

/**
 * triggered by clicking on flag images
 * changes locale and therefore the language
 *
 * @param locale with according language
 */
const changeLocale = (locale: 'en' | 'de') => {
  i18n.global.locale.value = locale;
}
</script>

<style scoped>

#language-container {
  z-index: 2; /* ignores dark background */
  display: flex;
  gap: 1.2rem;
  position: fixed;
  top: 1.5rem;
  left: 2rem;
}

.language-icon {
  width: 4vw;
  height: auto;
  cursor: pointer;
  transition: transform 0.1s ease;
}

.language-icon:hover {
  transform: scale(1.1);
}

</style>
