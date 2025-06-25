<template>
  <header class="site_header">
    <!-- Logo / Title -->
    <div v-if="isLoggedIn" class="site_title_login" @click="goTo(`/top/${role}`)">
      ClassReservation
    </div>

    <div v-if="!isLoggedIn" class="site_title">
      ClassReservation
    </div>

    <!-- Right icons -->
    <div v-if="isLoggedIn" class="header_icon_group">
      <!-- Notification icon -->
      <div class="icon_link" @click="goTo('/notice')">
        <BellIcon class="header_icon" />
      </div>

      <!-- Chat icon -->
      <div class="icon_link" @click="goTo('/chat')">
        <MessageCircleIcon class="header_icon" />
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
import { BellIcon, MessageCircleIcon } from 'lucide-vue-next'

const router = useRouter()
const goTo = (path) => {
  router.push(path)
}
import { ref, onMounted,watch,onBeforeUnmount  } from 'vue'
import { useAuth } from '@/scripts/useAuth'
const { user, restoreLogin,isLoggedIn  } = useAuth()
onMounted(async () => {
  await restoreLogin()
})

watch(() => user.value, (newUser) => {
  if (newUser) {
    role.value = newUser.role
  }
})


const hasUnreadMessage = ref(false)
const { subscribe, disconnect } = useWebSocket()

</script>


<style scoped>
.site_header {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  background-color: #2d2d2d;
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

.mypage_button {
  background-color: black;
  color: white;
  border-radius: 999px;
  padding: 6px 16px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.mypage_button:hover {
  background-color: #333;
}
</style>
