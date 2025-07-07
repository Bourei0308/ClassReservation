<template>
  <body>
  <div class="lesson-container">
    <h2>先生授業明細（完了状態）</h2>

    <!-- 🔍 検索フォーム -->
    <div class="search-bar">
      <button class="select-button" @click="showTeacherModal = true">
        {{ selectedTeacher ? selectedTeacher.name : '先生を選択' }}
      </button>

      <button class="select-button" @click="showStudentModal = true">
        {{ selectedStudent ? selectedStudent.name : '生徒を選択' }}
      </button>

      <select v-model="filter.status" class="select-input">
        <option value="">ステータス</option>
        <option v-for="(label, value) in statusOptions" :key="value" :value="value">
          {{ label }}
        </option>
      </select>

      <input type="date" v-model="filter.startDate" class="date-input" placeholder="開始日" />
      <input type="date" v-model="filter.endDate" class="date-input" placeholder="終了日" />

      <select v-model="filter.period" class="select-input">
        <option value="">すべての期間</option>
        <option value="week">今週</option>
        <option value="month">今月</option>
        <option value="3months">3ヶ月以内</option>
        <option value="year">1年以内</option>
      </select>

      <button class="reset-button" @click="resetFilters">リセット</button>
    </div>

    <!-- 🔢 集計表示 -->
    <!-- ✅ 先生 -->
    <div v-if="selectedTeacher" class="summary">
      <div><span class="highlight-name">{{ selectedTeacher.name }}</span>先生</div>
      <div>
        <span class="highlight-period">{{ periodStart }}</span>~
        <span class="highlight-period">{{ periodEnd }}</span>
      </div>
      <div class="summary-row">
        授業時間数：<span class="highlight-number teacher-hours">{{ teacherHours }}</span>コマ
      </div>
    </div>

    <!-- ✅ 生徒 -->
    <div v-if="selectedStudent" class="summary">
      <div><span class="highlight-name">{{ selectedStudent.name }}</span>さん</div>
      <div>
        <span class="highlight-period">{{ periodStart }}</span>~
        <span class="highlight-period">{{ periodEnd }}</span>
      </div>
      <div class="summary-row">
        授業時間数：<span class="highlight-number student-hours">{{ studentHours }}</span>コマ
      </div>
      <div class="summary-row">
        承認待ち数：<span class="highlight-number pending">{{ pendingHours }}</span>コマ
      </div>
      <div class="summary-row">
        残りコマ数：<span class="highlight-number remaining">{{ remainingHours }}</span>コマ
      </div>
    </div>


    <div class="button-group">
      <button class="submit-button" @click="openMonthlySummary(2)">先生の月別授業一覧</button>
      <button class="submit-button" @click="openMonthlySummary(1)">生徒の月別授業一覧</button>
      <button class="submit-button" @click="openMonthlySummary(2, 'calendar')">先生の日別授業一覧</button>
      <button class="submit-button" @click="openMonthlySummary(1, 'calendar')">生徒の日別授業一覧</button>
    </div>

    <!-- 📋 授業テーブル -->
    <!-- 🔢 授業ボックス表示 -->
    <div class="lesson-box-container">
      <div v-for="lesson in filteredLessons" :key="lesson.id" class="lesson-box" :style="{
        backgroundColor:
          lesson.status === 0 ? 'hsl(60, 100%, 91%)' :
            lesson.status === 1 ? '#e3f2fd' :
              lesson.status === 2 ? 'hsl(129, 100%, 94%)' :
                lesson.status === 3 ? 'hsl(0, 80%, 96%)' :
                  '#888'
      }">
        <!-- 🔸 1行目 -->
        <div class="lesson-header">
          <div v-if="lesson.status !== undefined" class="status-label" :style="{
            backgroundColor:
              lesson.status === 0 ? 'hsl(52, 100%, 34%)' :
                lesson.status === 1 ? 'hsl(211, 100%, 50%)' :
                  lesson.status === 2 ? 'hsl(130, 100%, 24%)' :
                    lesson.status === 3 ? 'hsl(0, 100%, 50%)' :
                      '#888'
          }">
            {{
              lesson.status === 0 ? '承認待ち' :
                lesson.status === 1 ? '承認済み' :
                  lesson.status === 2 ? '完了' :
                    lesson.status === 3 ? 'キャンセル' :
                      '不明'
            }}
          </div>
          <div class="">
            <span class="teacher">{{ lesson.teacherName }}</span>
            <span class="role-label">（先生）</span>
            →
            <span class="student">{{ lesson.studentName }}</span>
            <span class="role-label">（生徒）</span>
          </div>

        </div>

        <!-- 🔹 2行目 -->
        <div class="lesson-info">
          <div class="label-box">
            <div class="time-label">
              <span class="label-tag">日付</span><span class="time-label-content">{{ lesson.date }}</span>
            </div>
            <div class="time-label">
              <span class="label-tag">時間</span><span class="time-label-content">{{ lesson.time }}</span>
            </div>
            <div class="time-label">
              <span class="label-tag">コマ</span><span class="time-label-content">{{ getDurationHours(lesson.time)
              }} 時間</span>
            </div>
          </div>

          <div class="lesson-actions">
            <button class="edit-button" @click="openEditModal(lesson)">編集</button>
            <button class="delete-button" @click="deleteLesson(lesson)">削除</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 弹窗 -->
    
  </div>
  </body>
  <UserSelectModal :show="showTeacherModal" :role="2" title="先生を選択" @select="onSelectTeacher"
      @close="showTeacherModal = false" />
    <UserSelectModal :show="showStudentModal" :role="1" title="生徒を選択" @select="onSelectStudent"
      @close="showStudentModal = false" />
    <MonthlySummaryModal :show="showMonthlySummary" :role="selectedRole" :lessons="lessons"
      @close="showMonthlySummary = false" @select="onSelectMonthlySummary" />
    <MonthlyLessonCalendar :show="showMonthlyCalendar" :role="selectedRole" :lessons="lessons"
      @close="showMonthlyCalendar = false" @select="onSelectDailySummary" />
    <EditLessonModal :show="showEditModal" :start-time="startTime" :end-time="endTime" :lesson="editingLesson"
      @close="showEditModal = false" @updated="init" />
    <AlertModal v-bind="alertProps" @close="closeAlert" />
    <ConfirmDialog :show="confirmShow" :message="confirmMessage" @confirm="onConfirm" @cancel="onCancel" />

</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import axios from "axios";
import UserSelectModal from '@/components/popup_select_user.vue';
import MonthlySummaryModal from '@/components/popup_monthly_class.vue';
import MonthlyLessonCalendar from '@/components/popup_daily_class.vue';
import EditLessonModal from "@/components/popup_schedule_edit.vue";
import { getUsers, getSchedulesByTeacher, getSchedulesByStudent } from '@/scripts/chatUtils';
import moment from 'moment-timezone';

import { useWebSocket } from '@/scripts/useWebSocket'
const { subscribe } = useWebSocket()

// 🔸 alert
import AlertModal from '@/components/popup_message_alert.vue';
import ConfirmDialog from '@/components/popup_message_confirm.vue';
import { useModalManager } from '@/scripts/useModalManager';
const {
  showAlert, closeAlert, alertProps,
  confirmShow, confirmMessage, openConfirm, onConfirm, onCancel
} = useModalManager();

const lessons = ref([]);


const filter = ref({
  teacher: '',
  student: '',
  status: '',
  startDate: '',  // 改为开始日期
  endDate: '',    // 改为结束日期
  period: ''
});

const statusOptions = {
  0: "承認待ち",
  1: "承認済み",
  2: "完了",
  3: "キャンセル"
};

const showTeacherModal = ref(false);
const showStudentModal = ref(false);
const selectedTeacher = ref(null);
const selectedStudent = ref(null);

const periodStart = ref(null);
const periodEnd = ref(null);

const teacherHours = ref(0);
const studentHours = ref(0);
const remainingHours = ref(0);
const pendingHours = ref(0);
const localTimezone = Intl.DateTimeFormat().resolvedOptions().timeZone;

onMounted(async () => {
  init();

  subscribe(`/api/topic/calendar/`, async () => {
        console.log("カレンダーの更新を受信しました");
        await init();
    });
});

const init = async () => {
  try {
    const { data } = await axios.get("/api/lessons/completed");
    lessons.value = data.map(l => {
      const localStart = moment.utc(l.startTime).local(); // 从UTC转换成本地时间
      console.log("dateTimeStr",l.startTime)
      console.log("localStart",localStart)

      return {
        ...l,
        localStart,
      };
    });
  } catch (error) {
    console.error("データ取得エラー:", error);
  }
};

function resetFilters() {
  selectedTeacher.value = null;
  selectedStudent.value = null;
  filter.value = {
    teacher: '',
    student: '',
    status: '',
    date: '',
    period: ''
  };
  teacherHours.value = 0;
  studentHours.value = 0;
  pendingHours.value = 0;
  remainingHours.value = 0;
}

watch([selectedTeacher, filter], async () => {
  if (selectedTeacher.value) {
    const schedules = await getSchedulesByTeacher(selectedTeacher.value.id);
    const [start, end] = getPeriod(schedules);
    if (start && end) {
      teacherHours.value = calculateTotalHours(schedules, start, end, 2);
    } else {
      teacherHours.value = 0;
    }
  } else {
    teacherHours.value = 0;
  }
}, { deep: true });

watch([selectedStudent, filter], async () => {
  if (selectedStudent.value) {
    const schedules = await getSchedulesByStudent(selectedStudent.value.id);

    const [start, end] = getPeriod(schedules);
    if (start && end) {
      studentHours.value = calculateTotalHours(schedules, start, end, 2);  // 只计算status为2的课时数
      pendingHours.value = calculateTotalHours(schedules, start, end, 0);  // 只计算status为0的课时数
    } else {
      studentHours.value = 0;
      pendingHours.value = 0;
    }

    const [charged, used] = await Promise.all([
      fetch(`/api/charges/users/${selectedStudent.value.id}/total`).then(res => res.json()),
      fetch(`/api/class-schedules/student/${selectedStudent.value.id}/total-hours`).then(res => res.json())
    ]);
    remainingHours.value = Math.max(0, (charged - used)).toFixed(1);
  } else {
    studentHours.value = 0;
    pendingHours.value = 0;
    remainingHours.value = 0;
  }
}, { deep: true });

function getPeriod(schedules = []) {
  if (filter.value.startDate && filter.value.endDate) {
    periodStart.value = filter.value.startDate;
    periodEnd.value = filter.value.endDate;
    return [filter.value.startDate, filter.value.endDate];
  }

  // 如果没有明确选择区间，走原来的逻辑
  if (!filter.value.period) {
    if (schedules.length > 0) {
      const dates = schedules
        .map(s => s.startTime.split('T')[0])
        .filter(d => d);

      const minDate = dates.reduce((a, b) => (a < b ? a : b));
      const maxDate = dates.reduce((a, b) => (a > b ? a : b));
      periodStart.value = minDate;
      periodEnd.value = maxDate;
      return [minDate, maxDate];
    }
    return [null, null];
  }

  // 如果filter.period被选中（保留原逻辑）
  const base = filter.value.startDate ? new Date(filter.value.startDate) : new Date();
  let start = new Date(base), end = new Date(base);

  if (filter.value.period === "week") {
    const day = base.getDay();
    const offset = (day === 0 ? -6 : 1 - day);
    start.setDate(base.getDate() + offset);
    end = new Date(start);
    end.setDate(start.getDate() + 6);
  } else if (filter.value.period === "month") {
    start = new Date(base.getFullYear(), base.getMonth(), 1);
    end = new Date(base.getFullYear(), base.getMonth() + 1, 0);
  } else if (filter.value.period === "3months") {
    start.setMonth(base.getMonth() - 3);
  } else if (filter.value.period === "year") {
    start.setFullYear(base.getFullYear() - 1);
  }

  const format = d => d.toISOString().split('T')[0];
  periodStart.value = format(start);
  periodEnd.value = format(end);
  return [format(start), format(end)];
}

watch(() => filter.value.period, (newPeriod) => {
  const baseDate = new Date();

  let start, end;

  if (!newPeriod) {
    // 空周期，清空开始结束日期
    filter.value.startDate = '';
    filter.value.endDate = '';
    return;
  }

  switch (newPeriod) {
    case 'week': {
      const day = baseDate.getDay();
      const diffToMonday = day === 0 ? -6 : 1 - day;
      start = new Date(baseDate);
      start.setDate(baseDate.getDate() + diffToMonday);
      end = new Date(start);
      end.setDate(start.getDate() + 6);
      break;
    }
    case 'month': {
      start = new Date(baseDate.getFullYear(), baseDate.getMonth(), 1);
      end = new Date(baseDate.getFullYear(), baseDate.getMonth() + 1, 0);
      break;
    }
    case '3months': {
      start = new Date(baseDate);
      start.setMonth(baseDate.getMonth() - 3);
      end = new Date(baseDate);
      break;
    }
    case 'year': {
      start = new Date(baseDate);
      start.setFullYear(baseDate.getFullYear() - 1);
      end = new Date(baseDate);
      break;
    }
  }

  const formatDate = (d) => d.toISOString().slice(0, 10);

  filter.value.startDate = formatDate(start);
  filter.value.endDate = formatDate(end);
});

function calculateTotalHours(schedules, start, end, statusFilter = null) {
  return schedules
    .filter(s => {
      const date = s.startTime.split('T')[0];
      return date >= start && date <= end && (statusFilter === null || s.status === statusFilter);
    })
    .reduce((total, s) => {
      const timeStr = s.time || `${s.startTime.slice(11, 16)}〜${s.endTime.slice(11, 16)}`;
      const [startTime, endTime] = timeStr.split('〜');
      const [startHour, startMin] = startTime.split(':').map(Number);
      const [endHour, endMin] = endTime.split(':').map(Number);

      let diffHours = (endHour - startHour) + (endMin - startMin) / 60;
      if (diffHours < 0) diffHours += 24;
      return total + diffHours;
    }, 0)
    .toFixed(1);
}

function getDurationHours(timeStr) {
  // timeStr格式是 "HH:mm〜HH:mm"
  const [start, end] = timeStr.split('〜');
  const [startH, startM] = start.split(':').map(Number);
  const [endH, endM] = end.split(':').map(Number);

  let diff = (endH - startH) + (endM - startM) / 60;
  if (diff < 0) diff += 24; // 跨午夜处理

  return diff.toFixed(1);
}

const statusOrder = { 2: 0, 1: 1, 0: 2, 3: 3 };

const filteredLessons = computed(() => {
  const [start, end] = getPeriod() || [null, null];

  return lessons.value
    .map(l => {
      const localStart = moment.tz(l.startTime, localTimezone);
      console.log("l.startTime",l.startTime)
      return {
        ...l,
        localStart,
      };
    })
    .filter(l => {
      const matchTeacher = !filter.value.teacher || l.teacherName === filter.value.teacher;
      const matchStudent = !filter.value.student || l.studentName === filter.value.student;
      const matchStatus = !filter.value.status || l.status === Number(filter.value.status);

      // 用localStart的日期部分比较
      const localDateStr = l.localStart.format('YYYY-MM-DD');
      const matchDate = (!start || !end) || (localDateStr >= start && localDateStr <= end);

      return matchTeacher && matchStudent && matchStatus && matchDate;
    })
    .sort((a, b) => {
      const statusDiff = statusOrder[a.status] - statusOrder[b.status];
      if (statusDiff !== 0) return statusDiff;

      // localStart逆序（最新时间在前）
      if (a.localStart.isAfter(b.localStart)) return -1;
      if (a.localStart.isBefore(b.localStart)) return 1;
      return 0;
    });
});


function onSelectTeacher(user) {
  selectedTeacher.value = user;
  filter.value.teacher = user.name;
}

function onSelectStudent(user) {
  selectedStudent.value = user;
  filter.value.student = user.name;
}

function statusText(status) {
  return statusOptions[status] || "不明";
}

function statusClass(status) {
  return {
    0: 'status-pending',
    1: 'status-confirmed',
    2: 'status-completed',
    3: 'status-canceled'
  }[status] || '';
}

const showMonthlySummary = ref(false);
const showMonthlyCalendar = ref(false);
const selectedRole = ref(2);  // 2=先生, 1=生徒

function openMonthlySummary(role, mode) {
  selectedRole.value = role;
  if (mode === 'calendar') {
    showMonthlyCalendar.value = true;
    return;
  } else {
    showMonthlySummary.value = true;
  }
}

function onSelectMonthlySummary({ id, name, month }) {
  showMonthlySummary.value = false;
  resetFilters();

  if (selectedRole.value === 2) {
    selectedTeacher.value = { id, name };
    filter.value.teacher = name;
  } else {
    selectedStudent.value = { id, name };
    filter.value.student = name;
  }
  filter.value.status = 2;

  // 选中月份的起始和结束日期
  const year = new Date().getFullYear();
  filter.value.startDate = `${year}-${month.toString().padStart(2, '0')}-01`;
  filter.value.endDate = `${year}-${month.toString().padStart(2, '0')}-${new Date(year, month, 0).getDate()}`;

  console.log({ id, name, month });
}

function onSelectDailySummary({ id, name, date }) {
  showMonthlyCalendar.value = false;  // 关闭弹窗（如有）
  resetFilters();                  // 清空原有筛选条件

  if (selectedRole.value === 2) {
    selectedTeacher.value = { id, name };
    filter.value.teacher = name;
  } else {
    selectedStudent.value = { id, name };
    filter.value.student = name;
  }

  filter.value.status = 2;

  // 设置过滤用的日期范围：当天
  filter.value.startDate = date;
  filter.value.endDate = date;
}

// ✔️ 削除
const deleteLesson = (lesson) => {
  const confirmText = 
    `${lesson.teacherName}（先生） → ${lesson.studentName}（生徒）\n` +
    `日付: ${lesson.date}\n` +
    `時間: ${lesson.time}（${getDurationHours(lesson.time)} 時間）\n\n` +
    `この履歴を本当に削除しますか？`;

  openConfirm(confirmText, async () => {
    try {
      await axios.delete(`/api/class-schedules/${lesson.id}`);
      showAlert('履歴を削除しました！', true);
      await init();
    } catch (e) {
      showAlert('削除失敗: ' + e.message, false);
    }
  });
};


// ✔️ モーダル関連
const startTime = ref('');
const endTime = ref('');
const showEditModal = ref(false);
const editingLesson = ref(null);
// ✔️ 編集モーダルを開く
const openEditModal = (lesson) => {
  editingLesson.value = { ...lesson }

  if (lesson.startTime && lesson.endTime) {
    // 提取时间部分，比如从 "2025-06-27T09:00" 提取 "09:00"
    startTime.value = lesson.startTime.slice(11, 16)
    endTime.value = lesson.endTime.slice(11, 16)
  } else if (lesson.time) {
    const [start, end] = lesson.time.split('〜')
    startTime.value = start
    endTime.value = end
  } else {
    startTime.value = ''
    endTime.value = ''
  }

  showEditModal.value = true
}




</script>


<style scoped>
body {
  height: 100%;
  margin: 0;
  padding: 0;
  background-image: url('../../assets/img/4.png');
  background-repeat: repeat;
  background-size: 1000px auto;
  
}

.lesson-container {
  max-width: 1100px;
  margin: 30px auto;
  padding: 20px;
  background-color: rgba(255, 255, 255, 0.9);
  border: 1px solid #ccc;
  border-radius: 20px;
  
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(6px);
}

/* 统一标题 */
h2 {
  text-align: center;
  margin-bottom: 20px;
}

/* 搜索栏整体 */
.search-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
  padding: 16px 12px;
  background-color: #f7f9ff;
  border-radius: 14px;
  box-shadow: 0 3px 8px rgb(45 45 105 / 0.2);
  margin-bottom: 20px;
  user-select: none;
}

/* 选择按钮：老师/学生 */
.select-button {
  padding: 10px 20px;
  background-color: #2d2d69eb;
  color: white;
  font-weight: 600;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  min-width: 140px;
  transition: background-color 0.3s ease, transform 0.2s ease;
}

.select-button:hover {
  background-color: #0056b3;
  transform: scale(1.05);
}

/* 下拉选择框和日期输入框 统一样式 */
.select-input,
.date-input {
  min-width: 140px;
  padding: 8px 12px;
  border-radius: 12px;
  border: 1.8px solid #2d2d69;
  font-weight: 600;
  color: #2d2d69;
  background-color: white;
  cursor: pointer;
  transition: border-color 0.3s ease;
}

.select-input:focus,
.date-input:focus {
  outline: none;
  border-color: #0056b3;
}



/* 汇总信息 */
.summary {
  text-align: center;
  font-size: 1rem;
  margin-bottom: 20px;
  color: #2d2d69;
  user-select: none;
  line-height: 1.6;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

/* 名称高亮 */
.highlight-name {
  font-weight: 700;
  padding: 4px 10px;
  background-color: #d7def9;
  border-radius: 12px;
  color: #2d2d69;
  margin-right: 6px;
  display: inline-block;
}

/* 时间段高亮 */
.highlight-period {
  font-weight: 600;
  color: #4455aa;
  margin: 0 6px;
  display: inline-block;
  min-width: 110px;
}

/* 数字加粗带背景 */
.highlight-number {
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 12px;
  display: inline-block;
  min-width: 50px;
  text-align: center;
  color: white;
  background-color: #2d2d69eb;
  margin-left: 6px;
}

/* 数字特殊状态颜色 */
.highlight-number.pending {
  background-color: #e74c3c;
  /* 红色 */
}

.highlight-number.remaining {
  background-color: #27ae60;
  /* 绿色 */
}

.highlight-number.student-hours {
  background-color: #2980b9;
  /* 学生蓝 */
}

.highlight-number.teacher-hours {
  background-color: #6c5ce7;
  /* 先生紫 */
}

/* ✅ 按钮容器（.button-group）优化 */
.button-group {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  /* 🔸 从 200px 改成 160px */
  gap: 16px;
  justify-content: center;
  margin-top: 10px;
  padding: 0 10px;
  /* 🔸 手机端左右加 padding，防止贴边 */
  box-sizing: border-box;
  margin-bottom: 20px;
}

/* ✅ 按钮本身调整，移除 min-width 并适配父容器 */
.submit-button {
  width: 100%;
  /* 🔸 宽度随 grid 自适应 */
  padding: 14px;
  font-size: 1.05rem;
  font-weight: 700;
  color: white;
  background-color: #2d2d69eb;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  user-select: none;
  box-sizing: border-box;
}

.submit-button:hover {
  background-color: #0056b3;
  transform: scale(1.03);
}


/* 课程列表相关基础样式，保持原有 */
.lesson-table {
  width: 100%;
  border-collapse: collapse;
}

.lesson-table th,
.lesson-table td {
  border: 1px solid #ccc;
  padding: 10px;
  text-align: center;
}

.lesson-table th {
  background-color: #f2f8ff;
}

/* 状态背景色 */
.status-pending {
  background-color: #ffe6e6;
  /* 赤系 */
}

.status-confirmed {
  background-color: #e0f7ff;
  /* 水色 */
}

.status-completed {
  background-color: #e0ffe6;
  /* 緑系 */
}

.status-canceled {
  background-color: #f0f0f0;
  /* グレー */
}

/* 课程盒子布局 */
.lesson-box-container {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.lesson-box {
  border: 1px solid #ccc;
  border-radius: 12px;
  padding: 12px;
  width: 320px;
  box-shadow: 2px 2px 8px rgba(0, 0, 0, 0.1);
}

.lesson-header {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 6px;
}

.teacher {
  color: red;
}

.student {
  color: green;
}

.role-label {
  font-size: 12px;
  color: #888;
}

.lesson-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.label-box {
  display: flex;
  flex-direction: column;
}

.label-tag {
  background-color: rgb(0, 157, 255);
  color: white;
  padding: 3px 6px;
  border-radius: 4px;
  margin-right: 10px;
}

.time-label {
  padding: 4px 8px;
  font-size: 13px;
}

.time-label-content {
  width: 100px;
  display: inline-block;
  border-bottom: 1px solid rgb(0, 157, 255);
}

.lesson-actions button {
  margin-left: 4px;
}

.edit-button {
  background-color: #4caf50;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 4px 8px;
  transition: background-color 0.3s ease, transform 0.2s ease;
}

.edit-button:hover {
  background-color: #366c37;
  transform: scale(1.05);
}

.delete-button {
  background-color: #f44336;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 4px 8px;
  transition: background-color 0.3s ease, transform 0.2s ease;
}

.delete-button:hover {
  background-color: #a7342c;
  transform: scale(1.05);
}

/* 重置按钮 */
.reset-button {
  padding: 10px 24px;
  background-color: #ff6666;
  color: white;
  font-weight: 700;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: background-color 0.3s ease, transform 0.2s ease;
}

.reset-button:hover {
  background-color: #cc3333;
  transform: scale(1.05);
}

.lesson-comment {
  font-size: 13px;
  color: #555;
}

.status-label {
  color: white;
  margin-bottom: 2px;
  font-size: 12px;
  padding: 6px 0px;
  border-radius: 4px;
  text-align: center;
  display: inline-block;
  width: 100%;
}

@media (max-width: 768px) {

  /* 容器内边距调整 */
  .lesson-container {
    padding: 16px 12px;
  }

  /* 搜索栏改为纵向堆叠 */
  .search-bar {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }

  /* 按钮、下拉框、日期输入框宽度调整为100% */
  .select-button,
  .select-input,
  .date-input,
  .reset-button {
    width: 100%;
    min-width: unset;
    font-size: 0.95rem;
    padding: 10px;
  }

  /* 汇总信息字体缩小，间距优化 */
  .summary {
    font-size: 0.95rem;
    line-height: 1.8;
    gap: 6px;
  }

  .summary .highlight-name,
  .summary .highlight-period,
  .summary .highlight-number {
    display: inline-block;
    margin: 4px 2px;
  }

  .summary-row {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 6px;
    margin-bottom: 4px;
  }

  .highlight-name,
  .highlight-period,
  .highlight-number {
    font-size: 0.9rem;
    padding: 3px 8px;
    margin: 3px 4px;
  }

  /* 主按钮组改为纵向排列 */
  .button-group {
    grid-template-columns: repeat(2, 1fr);
    margin-bottom: 20px;
  }

  .submit-button {
    font-size: 0.8rem;
    padding: 12px;
  }
}
</style>
