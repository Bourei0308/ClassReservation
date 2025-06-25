<template>
  <div class="chat-container">
    <ChatAccountList :user="user" :chats="chats" :users="users" :selectedUser="selectedUser" @select="selectUser" />
    <ChatDisplay :user="user" :targetUser="selectedUser" :chats="chats" @sent="fetchChats" />
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
  localStorage.setItem('lastSelectedUserId', u.id)
}

// 自动恢复选中的用户

onMounted(async () => {
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
  connect()

  subscribe(`/api/topic/chats/${user.value.id}`, (message) => {
    const msg = JSON.parse(message.body)
    console.log('Received:', msg)
    chats.value.push(msg)

    if (selectedUser.value && selectedUser.value.id === msg.fromUserId) {
      markAsRead([msg])
    }
  })
})

onUnmounted(() => {
  disconnect()
})

const markAsRead = async (messages) => {
  for (const msg of messages) {
    if (!msg.isRead) {
      await axios.post('/api/chats', { ...msg, isRead: true })
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

/* 左侧用户列表 */
.chat-container>*:first-child {
  width: 40%;
  border-right: 1px solid #ccc;
  overflow-y: auto;
}

/* 右侧聊天显示 */
.chat-container>*:last-child {
  width: 60%;
  position: relative;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
</style>
