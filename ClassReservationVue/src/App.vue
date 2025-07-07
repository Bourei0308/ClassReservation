<script setup>
import { RouterLink, RouterView } from 'vue-router'
import siteheader from './components/siteheader.vue'
import sitefooter from './components/sitefooter.vue'

import { ref, provide } from 'vue'
const hasUnreadMessage = ref(false)
provide('hasUnreadMessage', hasUnreadMessage)

import { useWebSocket } from '@/scripts/useWebSocket'
import { onMounted, onUnmounted } from 'vue'
const { connect, disconnect } = useWebSocket()

import { useAuth } from '@/scripts/useAuth'
const { user, restoreLogin,language } = useAuth()

onMounted(async () => {
  restoreLogin()
  try {
    const res = await fetch('/api/auth/me', { credentials: 'include' })
    if (res.status === 401) return  // 未ログインならスキップ
    const data = await res.json()
    user.value = data

    // ✔️ 登录成功之后连接WebSocket
    connect(() => {

      // ✔️ 订阅未读消息
      subscribe(`/api/topic/unread/${user.value.id}`, () => {
        hasUnreadMessage.value = true
      })

      // ✔️ 订阅通知
      subscribe(`/api/topic/notice/${user.value.id}`, () => {
        hasUnreadNotification.value = true
      })
      console.log("WebSocket Connected")
    })

  } catch (e) {
    console.log("auth/me skipped or failed", e)
  }
})


onUnmounted(() => {
  disconnect()
})

// 🔸 alert
import AlertModal from '@/components/popup_message_alert.vue';
import ConfirmDialog from '@/components/popup_message_confirm.vue';
import { useModalManager } from '@/scripts/useModalManager';
const {
  showAlert, closeAlert, alertProps,
  confirmShow, confirmMessage, openConfirm, onConfirm, onCancel
} = useModalManager();

</script>

<template>
  <siteheader />
  <div class="wrapper" :class="`lang-${language}`">
    <div>
      <RouterView />
    </div>
  </div>

  <AlertModal v-bind="alertProps" @close="closeAlert" />
  <ConfirmDialog :show="confirmShow" :message="confirmMessage" @confirm="onConfirm" @cancel="onCancel" />

  <sitefooter />
</template>

<style scoped>

.lang-ja {
  font-family: "Kosugi Maru", "Yu Gothic", sans-serif;
}
.lang-zh {
  font-family: "Noto Sans SC", "Microsoft YaHei", sans-serif;
}

.wrapper {
  flex: 1;
  /* 主体内容撑满剩余高度 */
  margin-top: 60px;
  width: 100%;
}


header {
  line-height: 1.5;
  max-height: 100vh;
}

.logo {
  display: block;
  margin: 0 auto 2rem;
}

nav {
  width: 100%;
  font-size: 12px;
  text-align: center;
  margin-top: 2rem;
}

nav a.router-link-exact-active {
  color: var(--color-text);
}

nav a.router-link-exact-active:hover {
  background-color: transparent;
}

nav a {
  display: inline-block;
  padding: 0 1rem;
  border-left: 1px solid var(--color-border);
}

nav a:first-of-type {
  border: 0;
}

@media (min-width: 1024px) {
  header {
    display: flex;
    place-items: center;
    padding-right: calc(var(--section-gap) / 2);
  }

  .logo {
    margin: 0 2rem 0 0;
  }

  header .wrapper {
    display: flex;
    place-items: flex-start;
    flex-wrap: wrap;
  }

  nav {
    text-align: left;
    margin-left: -1rem;
    font-size: 1rem;

    padding: 1rem 0;
    margin-top: 1rem;
  }
}
</style>
