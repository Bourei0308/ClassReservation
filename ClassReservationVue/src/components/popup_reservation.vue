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
                    <option value="3months">3ヶ月</option>
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

                <div v-for="item in filteredSchedules" :key="item.id" class="schedule-box"
                    :class="['schedule-box', `status-bg-${item.status}`]">
                    <!-- 左側 -->
                    <div class="schedule-info">
                        <div class="schedule-header">
                            <!-- 判断是否为手机端 -->
                            <template v-if="isMobile">
                                <!-- 手机端：姓名 + 状态同一行 -->
                                <div class="mobile-header-line">
                                    <span v-if="role == 2" class="student-name">
                                        {{ getStudentName(item.studentId) }}
                                    </span>
                                    <span v-else-if="role == 1" class="student-name">
                                        {{ getStudentName(item.teacherId) }}
                                    </span>
                                    <span class="status-inline" :class="statusColor(item.status)">
                                        {{ statusLabel(item.status) }}
                                    </span>
                                </div>
                                <!-- 时间信息单独一行 -->
                                <div class="time-info">
                                    {{ formatTime(item.startTime) }} ~ {{ formatTime(item.endTime) }}
                                    ({{ getDuration(item.startTime, item.endTime) }}時間)
                                </div>
                            </template>

                            <template v-else>
                                <!-- PC端：姓名、时间、状态分开显示 -->
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
                                <div class="status" :class="statusColor(item.status)">
                                    {{ statusLabel(item.status) }}
                                </div>
                            </template>
                        </div>
                    </div>


                    <!-- 右侧按钮 -->
                    <div class="button-group">
                        <template v-if="item.status === 0">
                            <button v-if="role == 2" class="btn approve"
                                @click="handleChangeStatus(item.id, 1, 'confirm')">承認</button>
                            <button v-if="role == 2" class="btn remove"
                                @click="handleChangeStatus(item.id, 3, 'cancell')">取消</button>
                            <button v-if="role == 1" class="btn remove"
                                @click="handleChangeStatus(item.id, 4, 'cancell')">取消</button>
                        </template>
                        <template v-else-if="item.status === 1 && role == 2">
                            <button class="btn approve" @click="handleChangeStatus(item.id, 2)">完了</button>
                            <button class="btn remove" @click="handleChangeStatus(item.id, 3, 'cancell')">取消</button>
                        </template>
                        <template v-else-if="(item.status == 3 || item.status == 2) && role == 2">
                            <button class="btn remove" @click="handleChangeStatus(item.id, 1, 'confirm')">承認に変更</button>
                        </template>
                        <template v-else-if="(item.status == 3) && role == 1">
                            <button class="btn remove" @click="handleChangeStatus(item.id, 4, 'cancell')">削除</button>
                        </template>
                    </div>
                </div>
            </div>

            <div class="footer">
                <button class="btn close" @click="close">閉じる</button>
            </div>
        </div>
    </div>

    <LoadingModal :show="loadingShow" />
    <AlertModal v-bind="alertProps" @close="closeAlert" />
    <ConfirmDialog :show="confirmShow" :message="confirmMessage" @confirm="onConfirm" @cancel="onCancel" />

</template>

<script setup>
const isMobile = ref(window.innerWidth <= 768);
const emit = defineEmits(['list-refreshed'])
import { ref, computed, onMounted } from 'vue'
import { useAuth } from '@/scripts/useAuth'
import moment from 'moment'
const { user } = useAuth()

// 🔸 alert
import AlertModal from '@/components/popup_message_alert.vue';
import LoadingModal from '@/components/popup_message_loading.vue';
import ConfirmDialog from '@/components/popup_message_confirm.vue';
import { useModalManager } from '@/scripts/useModalManager'
const {
    showAlert, closeAlert, alertProps,
    confirmShow, confirmMessage, openConfirm, onConfirm, onCancel, showLoading, closeLoading, loadingShow
} = useModalManager();

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
import { sendStudentConfirmMail, sendStudentCancellMail, sendStudentCancelledBeforeApprovalMail } from '@/scripts/emailSender'

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
    // 按 startTime 从晚到早排序
    schedules.value.sort((a, b) => new Date(b.startTime) - new Date(a.startTime));
    users.value = await getUsers()
}

// ステータス変更
const handleChangeStatus = (id, newStatus, mode) => {
  const statusTextMap = {
    0: '復元',
    1: '承認',
    2: '完了',
    3: '取り消し',
    4: '取り消し',
  };
  const actionText = statusTextMap[newStatus] || '変更';

  openConfirm(`ステータスを「${actionText}」に変更してもよろしいですか？`, async () => {
    try {
      showLoading();

      // 先更新状态
      await changeStatus(id, newStatus);

      // 成功后立即刷新视图等
      await refreshPopup();
      emit('list-refreshed');
      closeLoading();
      showAlert('ステータスを変更しました', true);

      // 然后再“异步发送邮件”，不阻塞主流程
      if (mode === "confirm" && newStatus === 1) {
        sendStudentConfirmMail(id).catch((err) => {
          console.error('メール送信失敗:', err);
          showAlert('確認メールの送信に失敗しました', false);
        });

      } else if (mode === "cancell" && newStatus === 3) {
        sendStudentCancellMail(id).catch((err) => {
          console.error('メール送信失敗:', err);
          showAlert('キャンセルメールの送信に失敗しました', false);
        });

      } else if (mode === "cancell" && newStatus === 4) {
        sendStudentCancelledBeforeApprovalMail(id).catch((err) => {
          console.error('メール送信失敗:', err);
          showAlert('承認前キャンセルメールの送信に失敗しました', false);
        });
      }

    } catch (error) {
      console.error('ステータス変更エラー:', error);
      closeLoading();
      showAlert('ステータスの変更に失敗しました', false);
    }
  });
};


// 名前取得
const getStudentName = (id) => {
    const found = users.value.find(u => u.id === id)
    return found ? found.name : '不明なユーザー'
}

// 時間表示
const formatTime = (time) => moment.utc(time).local().format('MM/DD HH:mm');

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
    return `status status-${status}`;
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

        const isInPeriod = (() => {
            if (selectedPeriod.value === 'month') {
                return date.isSame(moment(), 'month')
            } else if (selectedPeriod.value === 'week') {
                return date.isSame(moment(), 'week')
            } else if (selectedPeriod.value === '3months') {
                const start = moment().startOf('month').subtract(1, 'month')
                const end = moment().endOf('month').add(1, 'month')
                return date.isBetween(start, end, 'day', '[]')
            }
            return true
        })()

        const userMatch = !selectedUserId.value ||
            (role.value === 2 && item.studentId === selectedUserId.value) ||
            (role.value === 1 && item.teacherId === selectedUserId.value)

        const statusMatch = selectedStatus.value === '' ||
            String(item.status) === selectedStatus.value

        return isInPeriod && userMatch && statusMatch
    })
})



onMounted(() => {
    isMobile.value = window.innerWidth <= 768; // 判断屏幕宽度是否为手机端
    window.addEventListener('resize', () => {
        isMobile.value = window.innerWidth <= 768;
    });
});
defineExpose({
    open,
    close
})

</script>

<style scoped>
.overlay {
    position: fixed;
    top: 0;
    left: 0;
    z-index: 2;
    width: 100vw;
    height: 100vh;
    background: rgba(0, 0, 0, 0.4);
    display: flex;
    justify-content: center;
    align-items: center;
}

.popup {
    background-color: white;
    width: 80%;
    max-width: 700px;
    max-height: 60vh;
    overflow-y: auto;
    padding: 24px;
    border-radius: 14px;
    box-shadow: 0 5px 18px rgba(0, 0, 0, 0.2);
}

.title {
    text-align: center;
    font-size: 1.5rem;
    margin-bottom: 20px;
    color: #2d2d69;
    font-weight: bold;
}

.filters {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    margin-bottom: 20px;
    justify-content: center;
}

.filters select {
    padding: 8px 12px;
    border-radius: 12px;
    border: 1.8px solid #2d2d69;
    font-weight: 600;
    color: #2d2d69;
    background-color: white;
    min-width: 160px;
}

.filters .btn.close {
    padding: 10px 20px;
    background-color: #f44336;
    color: white;
    font-weight: bold;
    border: none;
    border-radius: 12px;
    cursor: pointer;
    transition: background-color 0.3s ease;
}

.filters .btn.close:hover {
    background-color: #8b1d1d;
}

.schedule-box {
    border: 1px solid #ccc;
    border-radius: 12px;
    padding: 14px;
    margin-bottom: 16px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    box-shadow: 0 3px 10px rgba(0, 0, 0, 0.05);
    background-color: #fdfdfd;
    transition: transform 0.2s ease;
}

.schedule-box:hover {
    transform: scale(1.01);
}

.schedule-info {
    max-width: 70%;
}

.schedule-header {
    margin-bottom: 8px;
}

.student-name {
    font-weight: bold;
    color: #2d2d69;
    margin-bottom: 4px;
}

.time-info {
    font-size: 0.95rem;
    color: #444;
}

.status {
    margin-top: 4px;
    font-weight: bold;
    font-size: 0.9rem;
    padding: 6px 12px;
    border-radius: 8px;
    display: inline-block;
    width: 120px;
    text-align: center;
    color: white;
}

.status.status-0 {
    background-color: #f1c40f;
}

.status.status-1 {
    background-color: #3498db;
}

.status.status-2 {
    background-color: #27ae60;
}

.status.status-3 {
    background-color: #888;
}

.button-group {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
}

.button-group .btn {
    padding: 8px 16px;
    font-size: 0.95rem;
    font-weight: bold;
    border: none;
    border-radius: 10px;
    cursor: pointer;
    transition: all 0.3s ease;
}

.button-group .approve {
    background-color: #4caf50;
    color: white;
}

.button-group .cancel {
    background-color: #f39c12;
    color: white;
}

.button-group .remove {
    background-color: #7f8c8d;
    color: white;
}

.button-group .btn:hover {
    opacity: 0.85;
    transform: scale(1.03);
}

.loading {
    text-align: center;
    font-size: 1.1rem;
    color: #888;
    margin-top: 20px;
}

.empty {
    text-align: center;
    font-size: 1rem;
    color: #999;
    margin-top: 40px;
}

.footer {
    text-align: center;
    margin-top: 20px;
}

.footer .btn.close {
    padding: 10px 30px;
    background-color: #f44336;

    color: white;
    border: none;
    border-radius: 12px;
    font-weight: bold;
    cursor: pointer;
    transition: all 0.3s ease;
}

.footer .btn.close:hover {
    background-color: #8b1d1d;
}

.status-bg-0 {
    background-color: #fffbea;
    /* 承認待ち：淡黄色 */
}

.status-bg-1 {
    background-color: #e3f2fd;
    /* 承認済み：淡蓝色 */
}

.status-bg-2 {
    background-color: #e0f7ea;
    /* 完了：淡绿色 */
}

.status-bg-3 {
    background-color: #f3f3f3;
    /* キャンセル：灰色 */
}


/* 响应式 */
@media (max-width: 768px) {
    .popup {
        width: 95%;
        max-height: 80vh;
        padding: 16px;
    }

    .schedule-box {
        flex-direction: column;
        align-items: flex-start;
    }

    .schedule-info {
        max-width: 100%;
    }

    .button-group {
        width: 100%;
        justify-content: flex-start;
    }

    .button-group .btn {
        flex: 1;
        min-width: 120px;
    }

    .status {
        margin-top: 4px;
        font-size: 0.7rem;
        padding: 3px 3px;
        border-radius: 8px;
        font-weight: 300;
        width: 60px;

    }

    .student-name {
        width: 120px;
        display: inline-block;
    }

    .button-group .btn {
        padding: 3px 3px;
        font-size: 1rem;
        font-weight: 300;
    }
}
</style>
