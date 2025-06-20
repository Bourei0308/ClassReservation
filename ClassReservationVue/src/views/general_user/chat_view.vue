<template>
  <div class="chat-container">
    <ChatAccountList
      :user="user"
      :chats="chats"
      :users="users"
      @select="selectUser"
    />
    <ChatDisplay
      :user="user"
      :targetUser="selectedUser"
      :chats="chats"
      @sent="fetchChats"
    />
  </div>
</template>

<script setup>
import ChatAccountList from '@/components/chat_account_list.vue'
import ChatDisplay from '@/components/chat_display.vue'
import { useAuth } from '@/scripts/useAuth'
import { ref, onMounted } from 'vue'
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
  users.value = res.data.filter(u => u.role === 1 && u.id !== user.value.id)
}

const selectUser = (u) => {
  selectedUser.value = u
}

onMounted(() => {
  fetchChats()
  fetchUsers()
})
</script>

<style>
.chat-container {
  display: flex;
  height: 100vh;
  font-family: sans-serif;
}

/* 左侧用户列表 */
.chat-container > *:first-child {
  width: 40%;
  border-right: 1px solid #ccc;
  overflow-y: auto;
}

/* 右侧聊天显示 */
.chat-container > *:last-child {
  width: 60%;
  position: relative;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
</style>
