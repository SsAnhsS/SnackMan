<template>
  <div id="popup-box">
    <div id="message">
      <slot> PopUp-Message</slot>
    </div>
    <SmallNavButton
      id="ok-button"
      @click="$emit('hidePopUp')">

      {{ $t('button.ok') }}
    </SmallNavButton>
  </div>
</template>

<script lang="ts" setup>
import SmallNavButton from '@/components/SmallNavButton.vue';
</script>

<style scoped>
#popup-box {
  z-index: 10;
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 14rem;

  display: flex;
  flex-direction: row;

  background: var(--accent-color);
  border-radius: 0.3rem;
  text-align: left;
  padding: 1rem;
  color: var(--main-text-color);
}

#message {
  padding: 1rem 0 0 2rem;
}

#ok-button {
  right: 5%;
  top: 30%;
  font-size: 1.2rem;
  margin-top: 1rem;
  padding: 0.7rem;
}
</style>
