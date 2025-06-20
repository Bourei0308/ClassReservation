<template>
  <div class="top-chatbox">
    <div class="chatbox-header">Chatbox</div>
    <div class="chatbox-body">
      <div v-if="senderIds.length === 0" class="no-unread">未読メッセージがありません。</div>

      <div
        v-else
        v-for="userId in senderIds"
        :key="userId"
        class="sender-box"
      >
        <div class="sender-header">
          <div class="sender-name-area">
            <span v-if="unreadCount(userId)" class="unread-badge">
              {{ unreadCount(userId) }}
            </span>
            <span class="sender-name">
              {{ getUserName(userId) }}
            </span>
          </div>
          <div class="message-time">
            {{ latestMsgTime(userId) }}
          </div>
        </div>
        <div class="sender-message">
          {{ latestMsg(userId)?.message }}
        </div>
      </div>
    </div>
  </div>
</template>


<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import { useAuth } from '@/scripts/useAuth'

const { user } = useAuth()
const unreadChats = ref([])
const allUsers = ref([])

onMounted(async () => {
  const [chatRes, userRes] = await Promise.all([
    axios.get(`/api/chats/unread/${user.value.id}`),
    axios.get('/api/users')
  ])
  unreadChats.value = chatRes.data
  allUsers.value = userRes.data
})

const senderIds = computed(() => {
  const ids = unreadChats.value.map(c => c.fromUserId)
  return [...new Set(ids)]
})

const getUserName = (uid) => {
  return allUsers.value.find(u => u.id === uid)?.name || 'Unknown'
}

const unreadCount = (uid) => {
  return unreadChats.value.filter(c => c.fromUserId === uid).length
}
import { latestMessage, latestTime } from '@/scripts/chatUtils.js'
const latestMsg = (uid) => latestMessage(unreadChats.value, uid)
const latestMsgTime = (uid) => latestTime(unreadChats.value, uid)

</script>

<style scoped>
.top-chatbox {
  width: 400px;
  height: 600px;
  border: 1px solid #ccc;
  display: flex;
  flex-direction: column;
  background-color: #fff;
}

.chatbox-header {
  background-color: #f1f1f1;
  padding: 12px;
  font-size: 20px;
  font-weight: bold;
  text-align: center;
  color: #000;
  border-bottom: 1px solid #ccc;
}

.chatbox-body {
  overflow-y: auto;
  flex: 1;
}

.sender-box {
  padding: 10px 12px;
  border-bottom: 1px solid #eee;
  background-color: #fff;
}

.sender-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sender-name-area {
  display: flex;
  align-items: center;
}

.unread-badge {
  background-color: red;
  color: white;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 999px;
  margin-right: 6px;
}

.sender-name {
  font-size: 16px;
  font-weight: bold;
  color: #000;
}

.message-time {
  font-size: 12px;
  color: #888;
}

.sender-message {
  margin-top: 4px;
  font-size: 14px;
  color: #444;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.no-unread {
  text-align: center;
  color: #888;
  font-size: 14px;
  margin-top: 20px;
}

</style>
