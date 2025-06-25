<template>
    <div class="chat-display">
        <div v-if="!targetUser" class="chat-placeholder">
            チャット相手を選択してください。
        </div>

        <div v-else class="chat-messages" ref="chatContainer">
            <div v-for="msg in sortedMessages" :key="msg.id"
                :class="['message-box', msg.fromUserId === user.id ? 'self' : 'other']">
                <div class="sender-name">
                    {{ msg.fromUserId === user.id ? user.name : targetUser.name }}
                </div>
                <span v-if="msg.fromUserId === user.id && !msg.isRead" class="unread-label">未読</span>
                <span v-if="msg.fromUserId === user.id && msg.isRead" class="unread-label">既読</span>
                <div class="message-bubble">

                    {{ msg.message }}
                </div>

            </div>
        </div>

        <div class="chat-input">
            <input v-model="text" class="chat-textbox" placeholder="メッセージを入力..." @keyup.enter="send" />
            <button @click="sendMessage" class="send-button">送信</button>
        </div>
    </div>
</template>

<script setup>
import { ref, watch, onUpdated, nextTick, computed } from 'vue'
import axios from 'axios'

import { sendEmail, EmailTemplates } from '@/scripts/emailSender' // ← 追加


const props = defineProps(['user', 'targetUser', 'chats'])
const emit = defineEmits(['sent'])

const text = ref('')
const chatContainer = ref(null)

import { useWebSocket } from '@/scripts/useWebSocket'

const { send,subscribe,disconnect } = useWebSocket()

const sendMessage = async () => {
    if (!text.value.trim()) return
    const payload = {
        fromUserId: props.user.id,
        toUserId: props.targetUser.id,
        message: text.value,
        createdAt: new Date().toISOString(),
        isRead: false
    }
    await axios.post('/api/chats', payload)
    send("/api/app/chat/send", {}, JSON.stringify(payload))

    // // 💌 メール送信処理（ここを追加）
    // // ② メールテンプレート生成と送信（ここが今回の追加）
    // try {
    //     const template = EmailTemplates.teacherBookingNotification(
    //         props.targetUser.name,  // 先生の名前
    //         props.user.name,        // 学生の名前
    //         new Date().toLocaleString() // 授業時間（今回は仮に今の時刻）
    //     )

    //     await sendEmail({
    //         to: props.targetUser.email, // 📧 送信先メールアドレス
    //         subject: template.subject,
    //         body: template.body
    //     })

    //     console.log('メール送信成功')
    // } catch (error) {
    //     console.error('メール送信失敗:', error)
    // }
    text.value = ''
    emit('sent')
}

const sortedMessages = computed(() => {
    return props.chats
        .filter(c =>
            (c.fromUserId === props.user.id && c.toUserId === props.targetUser?.id) ||
            (c.toUserId === props.user.id && c.fromUserId === props.targetUser?.id))
        .sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))
})

watch(() => props.targetUser, async () => {
    if (props.targetUser) {
        const unread = props.chats.filter(c =>
            c.fromUserId === props.targetUser.id &&
            c.toUserId === props.user.id &&
            !c.isRead
        )
        for (const msg of unread) {
            await axios.post(`/api/chats/mark-read/${msg.id}`, { ...msg, isRead: true })
        }
        emit('sent')
    }
})

watch(sortedMessages, () => {
    nextTick(() => {
        chatContainer.value?.scrollTo({
            top: chatContainer.value.scrollHeight,
            behavior: 'smooth'
        })
    })
})

import { onMounted, onUnmounted } from 'vue'

onMounted(() => {
    if (props.user) {
        // ✅ 订阅已读状态
        subscribe(`/api/topic/read-status/${props.user.id}`, (message) => {
            const updatedChat = JSON.parse(message.body)
            const index = props.chats.findIndex(c => c.id === updatedChat.id)
            if (index !== -1) {
                props.chats[index].isRead = true
            }
        })
    }
})

onUnmounted(() => {
  disconnect()
})
</script>

<style scoped>
.chat-display {
    width: 60%;
    position: relative;
    display: flex;
    flex-direction: column;
    height: 100vh;
    box-sizing: border-box;
}

.chat-placeholder {
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: center;
    color: #999;
    font-size: 1.1em;
}

.chat-messages {
    flex: 1;
    overflow-y: auto;
    padding: 12px 16px;
    background-color: #fefefe;
}

.message-box {
    margin-bottom: 16px;
    max-width: 70%;
    word-break: break-word;
}

.message-box.other {
    text-align: left;
    align-self: flex-start;
}

.message-box.self {
    text-align: right;
    align-self: flex-end;
    margin-left: auto;
}

.sender-name {
    font-size: 0.8em;
    color: #777;
    margin-bottom: 4px;
}

.message-bubble {
    display: inline-block;
    padding: 10px 14px;
    border-radius: 20px;
    background-color: #e5e5e5;
    color: #000;
}

.unread-label {
    margin-right: 10px;
    font-size: 12px;
    color: gray;
    user-select: none;
}

.message-box.other .message-bubble {
    background-color: #a0e8a0;
}

.chat-input {
    display: flex;
    gap: 8px;
    padding: 12px;
    border-top: 1px solid #ccc;
    background-color: #fafafa;
}

.chat-textbox {
    flex: 1;
    padding: 8px 10px;
    font-size: 1em;
    border: 1px solid #bbb;
    border-radius: 6px;
    outline: none;
}

.chat-textbox:focus {
    border-color: #007bff;
}

.send-button {
    padding: 8px 16px;
    background-color: #007bff;
    color: white;
    font-weight: bold;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    transition: background-color 0.2s;
}

.send-button:hover {
    background-color: #0056b3;
}
</style>