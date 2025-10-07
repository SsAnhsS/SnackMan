import './assets/base.css'

import {createApp} from 'vue'
import {createPinia} from 'pinia'
import {createI18n} from 'vue-i18n'

import App from './App.vue'
import router from './router'

import enMessages from './assets/locales/en.json';
import deMessages from './assets/locales/de.json';

export const i18n = createI18n({
  legacy: false,
  locale: 'en' as 'en' | 'de',
  fallbackLocale: 'en' as 'en' | 'de',
  messages: {
    en: enMessages,
    de: deMessages
  }
})

createApp(App)
  .use(router)
  .use(createPinia())
  .use(i18n)
  .mount('#app');
