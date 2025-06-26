<template>
  <div class="lesson-container">
    <h2>先生授業明細（完了状態）</h2>

    <!-- 🔍 検索フォーム -->
    <div class="search-bar">
      <button @click="showTeacherModal = true">
        {{ selectedTeacher ? selectedTeacher.name : '先生を選択' }}
      </button>

      <button @click="showStudentModal = true">
        {{ selectedStudent ? selectedStudent.name : '生徒を選択' }}
      </button>

      <select v-model="filter.status">
        <option value="">ステータス</option>
        <option v-for="(label, value) in statusOptions" :key="value" :value="value">
          {{ label }}
        </option>
      </select>

      <input type="date" v-model="filter.startDate" placeholder="開始日" />
      <input type="date" v-model="filter.endDate" placeholder="終了日" />
      <select v-model="filter.period">
        <option value="">すべての期間</option>
        <option value="week">今週</option>
        <option value="month">今月</option>
        <option value="3months">3ヶ月以内</option>
        <option value="year">1年以内</option>
      </select>
      <button @click="resetFilters">リセット</button>
    </div>

    <!-- 🔢 集計表示 -->
    <div v-if="selectedTeacher" class="summary">
      {{ selectedTeacher.name }}先生　
      {{ periodStart }}~{{ periodEnd }}　
      授業時間数：{{ teacherHours }}コマ
    </div>

    <div v-if="selectedStudent" class="summary">
      {{ selectedStudent.name }}さん　
      {{ periodStart }}~{{ periodEnd }}　
      授業時間数：{{ studentHours }}コマ　
      承認待ち数：{{ pendingHours }}コマ　
      残りコマ数：{{ remainingHours }}コマ
    </div>

    <button @click="openMonthlySummary(2)">先生の月別授業一覧</button>
    <button @click="openMonthlySummary(1)">生徒の月別授業一覧</button>

    <!-- 📋 授業テーブル -->
    <table class="lesson-table">
      <thead>
        <tr>
          <th>先生名</th>
          <th>授業日</th>
          <th>時間</th>
          <th>生徒名</th>
          <th>ステータス</th>
          <th>備考</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(lesson, index) in filteredLessons" :key="index" :class="statusClass(lesson.status)">
          <td>{{ lesson.teacherName }}</td>
          <td>{{ lesson.date }}</td>
          <td>{{ lesson.time }}（{{ getDurationHours(lesson.time) }}時間）</td>
          <td>{{ lesson.studentName }}</td>
          <td>{{ statusText(lesson.status) }}</td>
          <td>{{ lesson.comment }}</td>
        </tr>
      </tbody>
    </table>

    <!-- 弹窗 -->
    <UserSelectModal :show="showTeacherModal" :role="2" title="先生を選択" @select="onSelectTeacher"
      @close="showTeacherModal = false" />
    <UserSelectModal :show="showStudentModal" :role="1" title="生徒を選択" @select="onSelectStudent"
      @close="showStudentModal = false" />
    <MonthlySummaryModal :show="showMonthlySummary" :role="monthlySummaryRole" :lessons="lessons"
      @close="showMonthlySummary = false" @select="onSelectMonthlySummary" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import axios from "axios";
import UserSelectModal from '@/components/popup_select_user.vue';
import MonthlySummaryModal from '@/components/popup_monthly_class.vue';
import { getUsers, getSchedulesByTeacher, getSchedulesByStudent } from '@/scripts/chatUtils';

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

onMounted(async () => {
  try {
    const { data } = await axios.get("/api/lessons/completed");
    lessons.value = data;
  } catch (error) {
    console.error("データ取得エラー:", error);
  }
});

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

  return lessons.value.filter(l => {
    const matchTeacher = !filter.value.teacher || l.teacherName === filter.value.teacher;
    const matchStudent = !filter.value.student || l.studentName === filter.value.student;
    const matchStatus = !filter.value.status || l.status === Number(filter.value.status);

    // 用startDate和endDate判断
    const matchDate = (!start || !end) || (l.date >= start && l.date <= end);

    return matchTeacher && matchStudent && matchStatus && matchDate;
  }).sort((a, b) => {
    const statusDiff = statusOrder[a.status] - statusOrder[b.status];
    if (statusDiff !== 0) return statusDiff;

    if (a.date > b.date) return -1;
    if (a.date < b.date) return 1;
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
const monthlySummaryRole = ref(2);  // 2=先生, 1=生徒

function openMonthlySummary(role) {
  monthlySummaryRole.value = role;
  showMonthlySummary.value = true;
}

function onSelectMonthlySummary({ id, name, month }) {
  showMonthlySummary.value = false;
  resetFilters();

  if (monthlySummaryRole.value === 2) {
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


</script>


<style scoped>
.lesson-container {
  max-width: 1100px;
  margin: 30px auto;
  padding: 20px;
  background-color: #fff;
  border: 1px solid #ddd;
  border-radius: 12px;
  font-family: Arial, sans-serif;
}

h2 {
  text-align: center;
  margin-bottom: 20px;
}

.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
  justify-content: center;
}


.search-bar input,
.search-bar select {
  padding: 8px;
  border-radius: 5px;
  border: 1px solid #ccc;
}

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

/* ステータス別 行の背景色 */
.status-pending {
  background-color: #ffe6e6;
  /* 赤系（承認待ち） */
}

.status-confirmed {
  background-color: #e0f7ff;
  /* 水色（承認済み） */
}

.status-completed {
  background-color: #e0ffe6;
  /* 緑系（完了） */
}

.status-canceled {
  background-color: #f0f0f0;
  /* グレー（キャンセル） */
}
</style>
