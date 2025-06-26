<template>
    <div v-if="show" class="overlay">
        <div class="popup">
            <h2 class="title">今月の予約一覧</h2>

            <!-- フィルター -->
            <div class="filters">
                <!-- 先生or生徒 -->
                <select v-model="selectedUserId">
                    <option value="">全ての先生</option>
                    <option v-for="u in filterUserList" :key="u.id" :value="u.id">
                        {{ u.name }}
                    </option>
                </select>

                <!-- 期間 -->
                <select v-model="selectedPeriod">
                    <option value="month">今月</option>
                    <option value="week">今週</option>
                </select>

                <!-- ステータス -->
                <select v-model="selectedStatus">
                    <option value="">全ての授業</option>
                    <option value="0">承認待ち</option>
                    <option value="1">承認済み</option>
                    <option value="2">完了</option>
                    <option value="3">キャンセル</option>
                </select>
                <button class="btn close" @click="close">閉じる</button>
            </div>

            <!-- ローディング -->
            <div v-if="loading" class="loading">読み込み中...</div>

            <!-- コンテンツ -->
            <div v-else>
                <div v-if="filteredSchedules.length === 0" class="empty">
                    該当する予約はありません
                </div>

                <div v-for="item in filteredSchedules" :key="item.id" class="schedule-box">
                    <!-- 左側 -->
                    <div class="schedule-info">
                        <div class="schedule-header">
                            <div v-if="role == 2" class="student-name">
                                {{ getStudentName(item.studentId) }}
                            </div>
                            <div v-else-if="role == 1" class="student-name">
                                {{ getStudentName(item.teacherId) }}
                            </div>
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
                            <button v-if="role == 2" class="btn approve"
                                @click="handleChangeStatus(item.id, 1,'confirm')">承認</button>
                            <button v-if="role == 2" class="btn cancel" @click="handleChangeStatus(item.id, 3,'cancell')">キャンセル</button>
                            <button v-if="role == 1" class="btn cancel" @click="handleChangeStatus(item.id, 3)">キャンセル</button>
                        </template>
                        <template v-else-if="item.status === 1 && role == 2">
                            <button class="btn approve" @click="handleChangeStatus(item.id, 2)">完了</button>
                            <button class="btn cancel" @click="handleChangeStatus(item.id, 3,'cancell')">キャンセル</button>
                        </template>
                        <template v-else-if="item.status === 3 && role == 2">
                            <button class="btn approve" @click="handleChangeStatus(item.id, 1,'confirm')">承認に変更</button>
                        </template>
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
const role = ref(0)

// フィルター用
const selectedUserId = ref('')
const selectedPeriod = ref('month')
const selectedStatus = ref('')

// API
import { getSchedulesByTeacher, getSchedulesByStudent, getUsers, changeStatus } from '@/scripts/chatUtils.js'
import { sendStudentConfirmMail,sendStudentCancellMail } from '@/scripts/emailSender'

// 開く
const open = async () => {
    show.value = true
    loading.value = true
    if (!user.value?.role) return
    try {
        role.value = user.value.role
        await refreshPopup()
    } finally {
        loading.value = false
    }
}

// 閉じる
const close = () => {
    show.value = false
}

// データ再取得
const refreshPopup = async () => {
    if (role.value === 2) {
        schedules.value = await getSchedulesByTeacher(user.value.id)
    } else if (role.value === 1) {
        schedules.value = await getSchedulesByStudent(user.value.id)
    }
    users.value = await getUsers()
}

// ステータス変更
const handleChangeStatus = async (id, newStatus,mode) => {
    const confirm = window.confirm('ステータスを変更してもよろしいですか？');
    if (!confirm) {
        return;
    }
    await changeStatus(id, newStatus)
    if (mode) {
        if(mode=="cancell") {
            sendStudentCancellMail(id)
        } else if (mode=="confirm") {
            sendStudentConfirmMail(id)
        }
    }
    await refreshPopup()
}

// 名前取得
const getStudentName = (id) => {
    const found = users.value.find(u => u.id === id)
    return found ? found.name : '不明なユーザー'
}

// 時間表示
const formatTime = (time) => moment(time).format('MM/DD HH:mm')

// 時間差
const getDuration = (start, end) => {
    const diff = moment(end).diff(moment(start), 'minutes')
    return (diff / 60).toFixed(1)
}

// ステータスラベル
const statusLabel = (status) => {
    switch (status) {
        case 0: return '承認待ち'
        case 1: return '承認済み'
        case 2: return '完了'
        case 3: return 'キャンセル'
        default: return '不明'
    }
}

// ステータスカラー
const statusColor = (status) => {
    switch (status) {
        case 0: return 'waiting'
        case 1: return 'approved'
        case 2: return 'completed'
        case 3: return 'canceled'
        default: return ''
    }
}

// ---------- ✅ フィルター処理 ----------

// ユーザーリスト
const filterUserList = computed(() => {
    if (role.value === 2) {
        // 先生なら生徒
        const studentIds = [...new Set(schedules.value.map(s => s.studentId))]
        return users.value.filter(u => studentIds.includes(u.id))
    } else if (role.value === 1) {
        // 生徒なら先生
        const teacherIds = [...new Set(schedules.value.map(s => s.teacherId))]
        return users.value.filter(u => teacherIds.includes(u.id))
    }
    return []
})

// フィルターされた予約
const filteredSchedules = computed(() => {
    return schedules.value.filter(item => {
        const date = moment(item.startTime)

        // 期間フィルター
        const isInPeriod = selectedPeriod.value === 'month'
            ? date.isSame(moment(), 'month')
            : date.isSame(moment(), 'week')

        // ユーザーフィルター
        const userMatch = !selectedUserId.value ||
            (role.value === 2 && item.studentId === selectedUserId.value) ||
            (role.value === 1 && item.teacherId === selectedUserId.value)

        // ステータスフィルター
        const statusMatch = selectedStatus.value === '' ||
            String(item.status) === selectedStatus.value

        return isInPeriod && userMatch && statusMatch
    })
})

defineExpose({
    open,
    close
})
</script>

<style scoped>
/* フィルター */
.filters {
    display: flex;
    gap: 12px;
    margin-bottom: 12px;
}

/* その他はそのまま */
.overlay {
    position: fixed;
    inset: 0;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 50;
}

.popup {
    background-color: white;
    border-radius: 8px;
    padding: 24px;
    width: 600px;
    max-height: 80vh;
    overflow-y: auto;
}

.title {
    font-size: 20px;
    font-weight: bold;
    margin-bottom: 16px;
}

.loading {
    text-align: center;
}

.empty {
    text-align: center;
    color: gray;
}

.schedule-box {
    border: 1px solid #ccc;
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 12px;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.schedule-info {
    flex: 1;
}

.schedule-header {
    display: flex;
    align-items: center;
}

.student-name {
    font-size: 18px;
    font-weight: bold;
}

.time-info {
    font-size: 14px;
    color: gray;
    margin-left: 8px;
}

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

.button-group {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

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

.footer {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
}
</style>
