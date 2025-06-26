<template>
    <div v-if="show" class="overlay">
        <div class="popup">
            <h2 class="title">今月の予約一覧</h2>

            <div v-if="loading" class="loading">読み込み中...</div>

            <div v-else>
                <div v-if="monthlySchedules.length === 0" class="empty">
                    今月の予約はありません
                </div>

                <div v-for="item in monthlySchedules" :key="item.id" class="schedule-box">
                    <!-- 左側 -->
                    <div class="schedule-info">
                        <div class="schedule-header">
                            <div class="student-name">{{ getStudentName(item.studentId) }}</div>
                            <div class="time-info">
                                {{ formatTime(item.startTime) }} ~ {{ formatTime(item.endTime) }}
                                ({{ getDuration(item.startTime, item.endTime) }}時間)
                            </div>
                        </div>
                        <div class="status" :class="statusColor(item.status)">
                            {{ statusLabel(item.status) }}
                        </div>
                    </div>

                    <!-- 右侧按钮 -->
                    <div class="button-group">
                        <template v-if="item.status === 0">
                            <button class="btn approve" @click="handleChangeStatus(item.id, 1)">承認</button>
                            <button class="btn cancel" @click="handleChangeStatus(item.id, 3)">キャンセル</button>
                        </template>
                        <template v-else-if="item.status === 1">
                            <button class="btn approve" @click="handleChangeStatus(item.id, 2)">完了</button>
                            <button class="btn cancel" @click="handleChangeStatus(item.id, 3)">キャンセル</button>
                        </template>
                        <template v-else-if="item.status === 3">
                            <button class="btn approve" @click="handleChangeStatus(item.id, 1)">承認に変更</button>
                        </template>
                        <!-- status === 2 無ボタン -->
                    </div>
                </div>
            </div>

            <div class="footer">
                <button class="btn close" @click="close">閉じる</button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useAuth } from '@/scripts/useAuth'
import moment from 'moment'

const { user } = useAuth()

const show = ref(false)
const schedules = ref([])
const users = ref([])
const loading = ref(false)


import { getSchedulesByTeacher, getUsers, changeStatus } from '@/scripts/chatUtils.js'

const open = async () => {
    show.value = true
    loading.value = true
    try {
        refreshPopup()
    } finally {
        loading.value = false
    }
}

const refreshPopup = async () => {
    schedules.value = await getSchedulesByTeacher(user.value.id)
    users.value = await getUsers()
}

const handleChangeStatus = async (id, newStatus) => {
    await changeStatus(id, newStatus)
    await refreshPopup()
}

const close = () => {
    show.value = false
}

const getStudentName = (studentId) => {
    const found = users.value.find(u => u.id === studentId)
    return found ? found.name : '不明なユーザー'
}

const formatTime = (time) => {
    return moment(time).format('MM/DD HH:mm')
}

const getDuration = (start, end) => {
    const diff = moment(end).diff(moment(start), 'minutes')
    return (diff / 60).toFixed(1)
}

const statusLabel = (status) => {
    switch (status) {
        case 0: return '承認待ち'
        case 1: return '承認済み'
        case 2: return '完了'
        case 3: return 'キャンセル'
        default: return '不明'
    }
}

const statusColor = (status) => {
    switch (status) {
        case 0: return 'waiting'
        case 1: return 'approved'
        case 2: return 'completed'
        case 3: return 'canceled'
        default: return ''
    }
}

const currentMonth = moment().month() // 0 ~ 11
const currentYear = moment().year()

const monthlySchedules = computed(() => {
    return schedules.value.filter(item => {
        const date = moment(item.startTime)
        return date.year() === currentYear && date.month() === currentMonth
    })
})



defineExpose({
    open,
    close
})
</script>
<style scoped>
/* 弹窗背景 */
.overlay {
    position: fixed;
    inset: 0;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 50;
}

/* 弹窗框体 */
.popup {
    background-color: white;
    border-radius: 8px;
    padding: 24px;
    width: 600px;
    max-height: 80vh;
    overflow-y: auto;
}

/* 标题 */
.title {
    font-size: 20px;
    font-weight: bold;
    margin-bottom: 16px;
}

/* 加载中 */
.loading {
    text-align: center;
}

/* 无数据提示 */
.empty {
    text-align: center;
    color: gray;
}

/* 每个预约box */
.schedule-box {
    border: 1px solid #ccc;
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 12px;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

/* 左侧信息 */
.schedule-info {
    flex: 1;
}

/* 学生名+时间 */
.schedule-header {
    display: flex;
    align-items: center;
}

/* 学生名 */
.student-name {
    font-size: 18px;
    font-weight: bold;
}

/* 时间 */
.time-info {
    font-size: 14px;
    color: gray;
    margin-left: 8px;
}

/* 状态文字 */
.status {
    margin-top: 8px;
    font-size: 14px;
}

.status.waiting {
    color: orange;
}

.status.approved {
    color: green;
}

.status.completed {
    color: blue;
}

.status.canceled {
    color: red;
}

/* 右侧按钮 */
.button-group {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

/* 通用按钮 */
.btn {
    padding: 6px 12px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    color: white;
}

.btn.approve {
    background-color: #22c55e;
}

.btn.cancel {
    background-color: #ef4444;
}

.btn.close {
    background-color: #6b7280;
}

/* 底部按钮 */
.footer {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
}
</style>
