import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

import { createI18n } from 'vue-i18n'
import ja from './locales/ja'
import zh from './locales/zh'
const i18n = createI18n({
  legacy: false,
  locale: localStorage.getItem('lang') || 'zh', // 默认语言
  messages: {
    ja,
    zh,
  },
})

const app = createApp(App)

app.use(router)
app.use(i18n)

app.mount('#app')

