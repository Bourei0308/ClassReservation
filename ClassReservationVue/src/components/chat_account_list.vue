<template>
  <div class="chat-account-list">
    <div
      v-for="u in users"
      :key="u.id"
      @click="$emit('select', u)"
      class="user-chat-entrance"
    >
      <div class="user-chat-header">
        <div class="user-name-area">
          <span
            v-if="unreadCount(u.id)"
            class="unread-badge"
          >
            {{ unreadCount(u.id) }}
          </span>
          <span class="user-name">{{ u.name }}</span>
        </div>
        <span class="chat-time">{{ latestTime(u.id) }}</span>
      </div>
      <div class="chat-preview" v-if="latestMessage(u.id)">
        {{ latestMessage(u.id).message }}
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps(['users', 'chats', 'user'])

const latestMessage = (uid) => {
  return [...props.chats]
    .filter(c => c.fromUserId === uid || c.toUserId === uid)
    .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))[0]
}

const latestTime = (uid) => {
  const msg = latestMessage(uid)
  if (!msg) return ''
  const d = new Date(msg.createdAt)
  const now = new Date()
  return d.toDateString() === now.toDateString()
    ? d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    : `${d.getMonth() + 1}月${d.getDate()}日`
}

const unreadCount = (uid) => {
  return props.chats.filter(
    c => c.fromUserId === uid && c.toUserId === props.user.id && !c.isRead
  ).length
}
</script>

<style>
.chat-account-list {
  width: 40%;
  border-right: 1px solid #ccc;
  padding: 16px;
  overflow-y: auto;
  box-sizing: border-box;
  height: 100vh;
  background-color: #f9f9f9;
}

.user-chat-entrance {
  background-color: #f1f1f1;
  padding: 12px;
  margin-bottom: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.user-chat-entrance:hover {
  background-color: #e0e0e0;
}

.user-chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-name-area {
  display: flex;
  align-items: center;
}

.user-name {
  font-size: 1.2em;
  font-weight: bold;
}

.chat-time {
  font-size: 0.8em;
  color: #777;
}

.unread-badge {
  background-color: red;
  color: white;
  font-size: 0.7em;
  padding: 2px 6px;
  border-radius: 999px;
  margin-right: 8px;
}

.chat-preview {
  font-size: 0.9em;
  color: #666;
  margin-top: 4px;
}
</style>
