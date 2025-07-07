<template>
  <link href="https://fonts.googleapis.com/css2?family=Kosugi+Maru&display=swap" rel="stylesheet">

  <header class="site_header">
    <!-- Logo / Title -->
    <div v-if="isLoggedIn && hasEmail" class="site_title_login" @click="goTo(`/top/${role}`)">
      本場じゅくぽん
    </div>

    <div v-if="!isLoggedIn || !hasEmail" class="site_title">
      本場じゅくぽん
    </div>

    <!-- Right icons -->
    <div v-if="isLoggedIn && hasEmail" class="header_icon_group">
      <!-- Notification icon -->
      <div class="icon_link" @click="goTo('/notice')">
        <div class="icon_box">
          <BellIcon class="header_icon" />
          <div v-if="hasUnreadNotification" class="unread_dot"></div>
        </div>
      </div>

      <!-- Chat icon -->
      <div class="icon_link" @click="goTo('/chat')">
        <div class="icon_box">
          <MessageCircleIcon class="header_icon" />
          <div v-if="hasUnreadMessage" class="unread_dot"></div>
        </div>

      </div>

      <!-- Language Switch -->
      <div class="icon_link" @click="showLangMenu = !showLangMenu" style="position: relative;">
        <div class="icon_box">
          <GlobeIcon class="header_icon" />
        </div>
        <div v-if="showLangMenu" class="lang_menu">
          <div :class="{ selected: language === 'ja' }" @click="setLanguage('ja')">日本語</div>
          <div :class="{ selected: language === 'zh' }" @click="setLanguage('zh')">中文</div>
        </div>
      </div>


      <!-- マイページ button -->
      <div class="mypage_button" @click="goTo('/account')">
        マイページ
      </div>
    </div>
  </header>
</template>

<script setup>

const role = ref(1)
import { useRouter } from 'vue-router'
import { BellIcon, MessageCircleIcon, GlobeIcon } from 'lucide-vue-next'

const router = useRouter()
const goTo = (path) => {
  router.push(path)
}
import { ref, onMounted, watch, onBeforeUnmount } from 'vue'
import { useAuth } from '@/scripts/useAuth'
const { user, restoreLogin, isLoggedIn, hasEmail, language } = useAuth()

import { inject } from 'vue'
const hasUnreadMessage = inject('hasUnreadMessage')
import { useWebSocket } from '@/scripts/useWebSocket'
const { subscribe } = useWebSocket()


import { hasUnreadNotification, checkUnreadNotifications } from '@/scripts/useNotificationStatus'
import { useModalManager } from '@/scripts/useModalManager';
const {
  showAlert, closeAlert, alertProps,
  confirmShow, confirmMessage, openConfirm, onConfirm, onCancel
} = useModalManager();


onMounted(async () => {
  await restoreLogin()
  if (user.value) {
    await checkUnreadNotifications(user.value.id)
  }
})

const showLangMenu = ref(false)
import { useI18n } from 'vue-i18n'
const { locale } = useI18n()
const setLanguage = (lang) => {
  if (language.value === lang) {
    showLangMenu.value = false
    return
  }
  language.value = lang
  locale.value = lang
  showLangMenu.value = false
  // 语言提示
  const message = lang === 'ja' ? '言語が日本語に変更されました。' : '语言已切换为中文。'
  showAlert(message, true)
}


watch(() => user.value, (newUser) => {
  if (newUser) {
    role.value = newUser.role
    subscribe(`/api/topic/unread/${newUser.id}`, () => {
      console.log("message")
      hasUnreadMessage.value = true
    })
  }
})

onBeforeUnmount(() => {
  if (user.value) {
  }
})


</script>


<style scoped>
.lang_menu {
  position: absolute;
  top: 40px;
  right: 0;
  background: white;
  border: 1px solid #ccc;
  border-radius: 10px;
  box-shadow: 0px 2px 6px rgba(0, 0, 0, 0.2);
  z-index: 100;
  width: 100px;
}

.lang_menu div {
  padding: 8px 16px;
  cursor: pointer;
  font-family: 'Kosugi Maru', sans-serif;
  color: #90caf9;
  transition: background-color 0.2s;
}

.lang_menu div:hover {
  background-color: #f0f0f0;
}

.lang_menu div.selected {
  background-color: #90caf9;
  color: white;
}


.site_header {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  background-color: #2d2d69eb;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
  z-index: 1000;
}

.site_title_login {
  font-size: 24px;
  font-weight: bold;
  cursor: pointer;
}

.site_title {
  font-size: 24px;
  font-weight: bold;
}

.header_icon_group {
  display: flex;
  align-items: center;
  gap: 16px;
}

.icon_link {
  color: white;
  text-decoration: none;
  transition: color 0.2s;
}

.icon_link:hover {
  color: #90caf9;
  /* 浅蓝 */
}

.header_icon {
  width: 24px;
  height: 24px;

}

.icon_box {
  position: relative;
}

.mypage_button {
  background-color: white;
  color: black;
  border-radius: 999px;
  padding: 6px 16px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.mypage_button:hover {
  background-color: #333;
}

.message_icon_wrapper {
  position: relative;
}

.unread_dot {
  position: absolute;
  top: -4px;
  right: -4px;
  width: 10px;
  height: 10px;
  background-color: red;
  border-radius: 50%;
}

.site_title_login,
.site_title {
  font-family: 'Kosugi Maru', sans-serif;
}
</style>
