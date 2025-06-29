<template>
  <div class="chat-container">
    <!-- ✅ 固定的展开按钮，仅在移动端显示 -->
    <button class="toggle-button" @click="showList = true">
      ☰
    </button>

    <!-- ✅ 移动端浮出式聊天列表 -->
    <div class="mobile-sidebar" v-if="showList">
      <div class="overlay" @click="showList = false"></div>
      <div class="sidebar-content">
        <ChatAccountList :user="user" :chats="chats" :users="users" :selectedUser="selectedUser"
          @select="handleSelectUser" />
      </div>
    </div>

    <!-- ✅ PC端的正常展示 -->
    <div class="sidebar-desktop">
      <ChatAccountList :user="user" :chats="chats" :users="users" :selectedUser="selectedUser" @select="selectUser" />
    </div>

    <!-- 聊天内容显示 -->
    <div class="display">
      <ChatDisplay :user="user" :targetUser="selectedUser" :chats="chats" @sent="fetchChats" />
    </div>
  </div>
</template>

<script setup>
import ChatAccountList from '@/components/chat_account_list.vue'
import ChatDisplay from '@/components/chat_display.vue'
import { useAuth } from '@/scripts/useAuth'
import { ref, onMounted, watch, onUnmounted } from 'vue'
import axios from 'axios'

const { user } = useAuth()
const chats = ref([])
const users = ref([])
const selectedUser = ref(null)
const selectedSender = ref(null)

const showList = ref(false)
const handleSelectUser = (u) => {
  selectUser(u)
  showList.value = false  // 移动端选完用户后关闭侧边栏
}

const fetchChats = async () => {
  const res = await axios.get(`/api/chats/user/${user.value.id}`)
  chats.value = res.data
}

const fetchUsers = async () => {
  const res = await axios.get('/api/users')
  if (user.value.role === 1) {
    users.value = res.data.filter(u => (u.role === 0 || u.role === 2) && u.id !== user.value.id)
  } else if (user.value.role === 2) {
    users.value = res.data.filter(u => (u.role === 0 || u.role === 1) && u.id !== user.value.id)
  } else {
    users.value = []
  }

}

const selectUser = (u) => {
  selectedUser.value = u
  selectedSender.value = u
  localStorage.setItem('lastSelectedUserId', u.id)
}

// 自动恢复选中的用户
import { inject } from 'vue'
const hasUnreadMessage = inject('hasUnreadMessage')
onMounted(async () => {
  hasUnreadMessage.value = false
  await fetchUsers()
  await fetchChats()
})

watch(users, (newUsers) => {
  if (!selectedUser.value) {
    const lastId = localStorage.getItem('lastSelectedUserId')
    if (lastId) {
      const u = newUsers.find(user => user.id === lastId)
      if (u) {
        selectedUser.value = u
      }
    }
  }
}, { immediate: true })

watch(selectedUser, (newUser) => {
  if (newUser) {
    fetchChats()
  }
})

import { useWebSocket } from '@/scripts/useWebSocket'

const { connect, disconnect, subscribe, send, isConnected } = useWebSocket()

onMounted(() => {
  subscribe(`/api/topic/chats/${user.value.id}`, (message) => {
    const msg = JSON.parse(message.body)
    chats.value.push(msg)

    if (selectedSender.value && selectedSender.value.id === msg.fromUserId) {
      console.log(selectedSender.value.id)
      markAsRead([msg])
    }
  })
})

onUnmounted(() => {
  selectedSender.value = null
})

const markAsRead = async (messages) => {
  for (const msg of messages) {
    if (!msg.isRead) {
      await axios.post(`/api/chats/mark-read/${msg.id}`, { ...msg, isRead: true })
      hasUnreadMessage.value = false
      msg.isRead = true
    }
  }
}

</script>

<style>
.chat-container {
  display: flex;
  height: 100vh;
  font-family: sans-serif;
}

/* 右侧聊天显示 */
.display {
  width: 60%;
  position: relative;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ✅ 固定展开按钮 */
.toggle-button {
  position: fixed;
  top: 80px;
  left: 10px;
  z-index: 1001;
  background-color: #ffffff;
  border: 1px solid #ccc;
  padding: 6px 10px;
  font-size: 18px;
  border-radius: 5px;
  display: none;
}

/* ✅ 移动端侧边栏和遮罩 */
.mobile-sidebar {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  z-index: 1000;
}

.overlay {
  background: rgba(0, 0, 0, 0.3);
}

.sidebar-content {
  width: 80%;
  max-width: 300px;
  background-color: white;
  overflow-y: auto;
}

/* ✅ PC端正常显示左侧列表 */
.sidebar-desktop {
  width: 40%;
  border-right: 1px solid #ccc;
  overflow-y: auto;
}

/* ✅ 隐藏移动端组件和显示按钮的条件控制 */
@media (max-width: 768px) {
  .display {
    width: 100%;
  }

  .sidebar-desktop {
    display: none;
  }

  .toggle-button {
    display: block;
  }

  /* 覆盖层淡入 */
  .mobile-sidebar .overlay {
    flex: 1;
    background: rgba(0, 0, 0, 0.3);
    animation: fadeIn 0.3s ease;
  }

  /* 内容部分从右滑入 */
  .mobile-sidebar .sidebar-content {
    width: 80%;
    max-width: 300px;
    overflow-y: auto;
    background-color: transparent;
    animation: slideInRight 0.3s ease forwards;
  }

  /* 动画定义 */
  @keyframes slideInRight {
    0% {
      transform: translateX(100%);
      opacity: 0;
    }

    100% {
      transform: translateX(0);
      opacity: 1;
    }
  }

  @keyframes fadeIn {
    0% {
      background: rgba(0, 0, 0, 0);
    }

    100% {
      background: rgba(0, 0, 0, 0.3);
    }
  }
}
</style>
