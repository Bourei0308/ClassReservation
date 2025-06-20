<template>
    <div class="chat-account-list">
        <div v-for="u in users" :key="u.id" @click="selectUser(u)" :class="[
            'user-chat-entrance',
            isSelected(u) ? 'selected' : ''
        ]">
            <div>
                SelectedId: {{ props.selectedUser?.id }}, CurrentId: {{ u.id }}
            </div>
            <div class="user-chat-header">
                <div class="user-name-area">
                    <span v-if="unreadCount(u.id)" class="unread-badge">
                        {{ unreadCount(u.id) }}
                    </span>
                    <span class="user-name">{{ u.name }}</span>
                </div>
                <span class="chat-time">{{ latestMsgTime(u.id) }}</span>
            </div>
            <div class="chat-preview" v-if="latestMsg(u.id)">
                {{ latestMsg(u.id).message }}
            </div>
        </div>
    </div>
</template>

<script setup>
import { latestMessage, latestTime } from '@/scripts/chatUtils.js'
const emit = defineEmits(['select'])
const props = defineProps(['users', 'chats', 'user', 'selectedUser'])  // 👈 增加 selectedUser
const latestMsg = (uid) => latestMessage(props.chats, uid)
const latestMsgTime = (uid) => latestTime(props.chats, uid)

const unreadCount = (uid) => {
    return props.chats.filter(
        c => c.fromUserId === uid && c.toUserId === props.user.id && !c.isRead
    ).length
}
//前回選択ユーザ記憶

const selectUser = (user) => {
    emit('select', user)
}

import { watch } from 'vue'

watch(() => props.selectedUser, (newVal) => {
    console.log('selectedUser changed:', newVal?.id)
})

const isSelected = (u) => {
    return props.selectedUser?.id === u.id
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

.user-chat-entrance.selected {
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
