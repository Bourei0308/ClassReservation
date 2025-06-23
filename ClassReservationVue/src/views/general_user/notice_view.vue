<template>
    <div class="notice-container">
        <h2>お知らせ一覧</h2>

        <div v-for="(notification, index) in notifications" :key="notification.id" class="notification-box">
            <!-- 通知标题区域 -->
            <div class="notification-header" @click="toggleDetail(index, notification)">
                <div class="left-info">
                    <span class="title">{{ notification.title }}</span>
                    <span class="date">{{ formatDate(notification.createdAt) }}</span>
                </div>
                <div class="right-icons">
                    <span v-if="!notification.read" class="unread-icon">!</span>
                    <span class="chevron">&gt;</span>
                </div>
            </div>

            <!-- 展开详情 -->
            <div v-if="expandedIndex === index" class="notification-detail">
                {{ notification.message }}
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useAuth } from '@/scripts/useAuth'

const { user } = useAuth()
const notifications = ref([])
const expandedIndex = ref(null)

const fetchNotifications = async () => {
    try {
        const res = await axios.get(`/api/notifications/user/${user.value.id}`)
        
        // 按 createdAt 倒序排序
        notifications.value = res.data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
    } catch (err) {
        console.error('通知取得失敗', err)
    }
}

const toggleDetail = async (index, notification) => {   
    if (expandedIndex.value === index) {
        expandedIndex.value = null
        return
    }

    expandedIndex.value = index

    if (!notification.isRead) {
        try {
            await axios.put(`/api/notifications/${notification.id}/read`)
            notification.read = true // 修改 read 状态
        } catch (err) {
            console.error('既読更新失敗', err)
        }
    }
}

const formatDate = (isoString) => {
  const utcString = isoString.endsWith('Z') ? isoString : isoString + 'Z'
  const date = new Date(utcString)

  return new Intl.DateTimeFormat('ja-JP', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(date)
}

onMounted(fetchNotifications)
</script>

<style scoped>
.notice-container {
    max-width: 600px;
    margin: 30px auto;
    font-family: sans-serif;
}

.notification-box {
    border: 2px solid black;
    margin-bottom: 10px;
    transition: all 0.3s ease;
}

.notification-header {
    display: flex;
    justify-content: space-between;
    padding: 10px 14px;
    background-color: white;
    cursor: pointer;
    align-items: center;
}

.left-info {
    display: flex;
    align-items: center;
    gap: 20px;
}

.date {
    color: gray;
    font-size: 12px;
    min-width: 100px;
}

.title {
    font-weight: bold;
    font-size: 16px;
}

.right-icons {
    display: flex;
    align-items: center;
}

.unread-icon {
    background-color: red;
    color: white;
    border-radius: 50%;
    padding: 4px 8px;
    font-size: 12px;
    margin-right: 8px;
    font-weight: bold;
}

.chevron {
    font-size: 20px;
    transform: translateY(-1px);
}

.notification-detail {
    background-color: #f7f7f7;
    padding: 10px 14px;
    border-top: 1px solid black;
    font-size: 14px;
}
</style>
