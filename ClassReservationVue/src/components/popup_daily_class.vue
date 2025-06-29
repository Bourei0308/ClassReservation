<template>
    <div v-if="show" class="modal-overlay">
        <div class="modal-content">
            <div class="header">
                <button @click="changeMonth(-1)">&lt;先月</button>
                <h3>{{ roleName }} {{ currentYear }}年{{ currentMonth }}月の授業記録</h3>
                <button @click="changeMonth(1)">次月&gt;</button>
            </div>

            <input type="month" v-model="monthPicker" @change="onMonthPickerChange" />

            <!-- PC端日历网格 -->
            <div class="calendar-grid desktop-only">
                <div class="calendar-header" v-for="(d, i) in 7" :key="i">
                    {{ ['日', '月', '火', '水', '木', '金', '土'][i] }}
                </div>
                <div v-for="(cell, index) in calendarCells" :key="index"
                    :class="['calendar-cell', cell.isCurrentMonth ? '' : 'inactive']">
                    <div class="date-number">{{ cell.day }}</div>
                    <div v-for="item in summaryData" :key="item.id + '-' + cell.day">
                        <button v-if="cell.isCurrentMonth && item.dailyHours[cell.day]"
                            @click="selectDay(item, cell.day)">
                            {{ item.name }}: {{ item.dailyHours[cell.day] }}h
                        </button>
                    </div>
                    <div v-if="cell.isCurrentMonth" class="daily-total">
                        計{{ getDailyTotal(cell.day) }}h
                    </div>
                </div>
            </div>

            <!-- 手机端折叠面板 -->
            <div class="mobile-only">
                <div v-for="week in weeks" :key="week.weekIndex" class="week-panel">
                    <!-- 周折叠标题 -->
                    <div class="week-header" @click="toggleWeek(week.weekIndex)">
                        <span>第{{ week.weekIndex }}週</span>
                        <span>{{ foldedWeeks.includes(week.weekIndex) ? '▶︎' : '▼' }}</span>
                    </div>

                    <div v-show="!foldedWeeks.includes(week.weekIndex)" class="week-content">
                        <!-- 纵向列表 每天也是折叠 -->
                        <div v-for="day in week.days" :key="day" class="day-panel">
                            <div class="day-header" @click="toggleDay(day)">
                                <span>{{ day }}日</span>
                                <span>{{ foldedDays.includes(day) ? '▶︎' : '▼' }}</span>
                            </div>
                            <div v-show="!foldedDays.includes(day)" class="day-content">
                                <div v-for="item in summaryData" :key="item.id + '-' + day" class="item-row">
                                    <button v-if="item.dailyHours[day]" @click="selectDay(item, day)">
                                        {{ item.name }}: {{ item.dailyHours[day] }}h
                                    </button>
                                    <span v-else class="no-data">-</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 月度合计列表 -->
            <div class="monthly-summary">
                <h4>{{ roleName }}ごとの月間合計時間</h4>
                <table>
                    <thead>
                        <tr>
                            <th>{{ roleName }}名</th>
                            <th>合計時間</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="item in summaryData" :key="item.id">
                            <td>{{ item.name }}</td>
                            <td>{{ item.total.toFixed(1) }} 時間</td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <button @click="$emit('close')">閉じる</button>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';

const props = defineProps({
    show: Boolean,
    role: Number,
    lessons: Array,
});
const emit = defineEmits(['close', 'select']);

const roleName = props.role === 2 ? '先生' : '生徒';

const today = new Date();
const current = ref(new Date(today.getFullYear(), today.getMonth(), 1));
const monthPicker = ref(`${current.value.getFullYear()}-${String(current.value.getMonth() + 1).padStart(2, '0')}`);

const currentYear = computed(() => current.value.getFullYear());
const currentMonth = computed(() => current.value.getMonth() + 1);

function changeMonth(offset) {
    current.value.setMonth(current.value.getMonth() + offset);
    current.value = new Date(current.value);
    updateMonthPicker();
}

function updateMonthPicker() {
    monthPicker.value = `${current.value.getFullYear()}-${String(current.value.getMonth() + 1).padStart(2, '0')}`;
}

function onMonthPickerChange() {
    const [y, m] = monthPicker.value.split('-');
    current.value = new Date(Number(y), Number(m) - 1, 1);
}

function getDurationHours(timeStr) {
    const [start, end] = timeStr.split('〜');
    const [startH, startM] = start.split(':').map(Number);
    const [endH, endM] = end.split(':').map(Number);
    let diff = (endH - startH) + (endM - startM) / 60;
    if (diff < 0) diff += 24;
    return diff;
}

const summaryData = computed(() => {
    const map = new Map();
    const groupKey = props.role === 2 ? 'teacherName' : 'studentName';
    const idKey = props.role === 2 ? 'teacherId' : 'studentId';
    const otherIdKey = props.role === 2 ? 'studentId' : 'teacherId';

    for (const lesson of props.lessons) {
        if (lesson.status !== 2) continue;
        const date = new Date(lesson.date);
        if (date.getFullYear() !== currentYear.value || date.getMonth() + 1 !== currentMonth.value) continue;

        const day = date.getDate();
        const id = lesson[idKey];
        const name = lesson[groupKey];
        const otherId = lesson[otherIdKey];
        if (!id || !name) continue;

        if (!map.has(id)) {
            map.set(id, {
                id,
                name,
                dailyHours: {},
                total: 0,
                teacherId: props.role === 2 ? id : otherId,
                studentId: props.role === 2 ? otherId : id,
            });
        }

        const item = map.get(id);
        const duration = getDurationHours(lesson.time);
        item.dailyHours[day] = (item.dailyHours[day] || 0) + duration;
        item.total += duration;
    }

    for (const item of map.values()) {
        for (const d in item.dailyHours) {
            item.dailyHours[d] = item.dailyHours[d].toFixed(1);
        }
    }

    return Array.from(map.values()).sort((a, b) => a.name.localeCompare(b.name));
});

const calendarCells = computed(() => {
    const year = currentYear.value;
    const month = currentMonth.value;
    const startDate = new Date(year, month - 1, 1);
    const endDate = new Date(year, month, 0);
    const firstDayOfWeek = startDate.getDay();
    const totalDays = endDate.getDate();

    const cells = [];
    const prevMonthDays = new Date(year, month - 1, 0).getDate();

    for (let i = 0; i < firstDayOfWeek; i++) {
        cells.push({ day: prevMonthDays - firstDayOfWeek + i + 1, isCurrentMonth: false });
    }
    for (let i = 1; i <= totalDays; i++) {
        cells.push({ day: i, isCurrentMonth: true });
    }
    while (cells.length % 7 !== 0) {
        cells.push({ day: cells.length % 7, isCurrentMonth: false });
    }

    return cells;
});

function selectDay(item, day) {
    emit('select', {
        id: item.id,
        name: item.name,
        date: `${currentYear.value}-${String(currentMonth.value).padStart(2, '0')}-${String(day).padStart(2, '0')}`,
    });
    emit('close');
}

const foldedWeeks = ref([]);
const foldedDays = ref([]);
function toggleWeek(weekIndex) {
    if (foldedWeeks.value.includes(weekIndex)) {
        foldedWeeks.value = foldedWeeks.value.filter(w => w !== weekIndex);
    } else {
        foldedWeeks.value.push(weekIndex);
    }
}

function toggleDay(day) {
    if (foldedDays.value.includes(day)) {
        foldedDays.value = foldedDays.value.filter(d => d !== day);
    } else {
        foldedDays.value.push(day);
    }
}
const weeks = computed(() => {
    const year = currentYear.value;
    const month = currentMonth.value;

    // 先获取当月天数
    const daysInMonth = new Date(year, month, 0).getDate();

    // 按周分组，周日开始为一周起点
    const map = new Map();

    for (let day = 1; day <= daysInMonth; day++) {
        const date = new Date(year, month - 1, day);
        const dayOfWeek = date.getDay(); // 0(日)~6(土)

        // 计算周数：用当前日期和当月第一天日的weekday偏移计算周数
        // 也可以用Math.floor((day + firstDayOfMonthWeekday -1) / 7) + 1

        const firstDayWeekday = new Date(year, month - 1, 1).getDay();

        // 计算weekIndex
        const weekIndex = Math.floor((day + firstDayWeekday - 2) / 7) + 1; // 减2是为了从0开始

        if (!map.has(weekIndex)) {
            map.set(weekIndex, []);
        }
        map.get(weekIndex).push(day);
    }

    // 转成数组并排序
    const result = [];
    for (const [weekIndex, days] of map.entries()) {
        result.push({ weekIndex, days });
    }
    result.sort((a, b) => a.weekIndex - b.weekIndex);

    return result;
});

function getDailyTotal(day) {
  let total = 0;
  for (const item of summaryData.value) {
    if (item.dailyHours[day]) {
      total += Number(item.dailyHours[day]);
    }
  }
  return total.toFixed(1);
}

watch(weeks, (newWeeks) => {
    foldedWeeks.value = newWeeks.map(w => w.weekIndex);
}, { immediate: true });

</script>

<style scoped>
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(45, 45, 105, 0.7);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
}

.modal-content {
    background: white;
    padding: 1.5rem;
    max-height: 90vh;
    overflow-y: auto;
    width: 95vw;
    max-width: 1000px;
    border-radius: 16px;
    box-shadow: 0 8px 20px rgba(45, 45, 105, 0.3);
    font-family: Arial, sans-serif;
    box-sizing: border-box;
    color: #2d2d69;
}

.header {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 1rem;
    margin-bottom: 1rem;
}

.header button {
    background-color: #2d2d69;
    border: none;
    border-radius: 8px;
    color: white;
    font-weight: 700;
    font-size: 10px;
    cursor: pointer;
    padding: 0.5rem 1rem;
    transition: background-color 0.3s ease;
}

.header button:hover {
    background-color: #1e1e4f;
}

h3 {
    margin: 0;
    font-weight: 700;
    font-size: 16px;
    color: #2d2d69;
}

.month-picker {
    width: 160px;
    display: block;
    margin: 0 auto 1rem;
    padding: 0.3rem;
    border-radius: 8px;
    border: 2px solid #2d2d69;
    font-weight: 700;
    color: #2d2d69;
    cursor: pointer;
    transition: border-color 0.3s ease;
}

.month-picker:focus {
    outline: none;
    border-color: #1e1e4f;
}

.calendar-grid.desktop-only {
    display: grid;
    grid-template-columns: repeat(7, 100px);
    grid-template-rows: 28px repeat(auto-fill, minmax(90px, auto)); /* 第一行 header 变矮 */
    gap: 4px;
    justify-content: center;
}

.calendar-header {
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 0.85rem;
    font-weight: 700;
    color: #2d2d69;
    border-bottom: 2px solid #2d2d69;
}

.calendar-cell {
    border: 1px solid #ccc;
    width: 100px;
    /* 固定宽 */
    min-height: 90px;
    background: #f7f9ff;
    color: #2d2d69;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 4px;
    position: relative;
}

.calendar-cell.inactive {
    background: #eee;
    color: #aaa;
}

.date-number {
    font-weight: 700;
    margin-bottom: 4px;
    user-select: none;
    font-size: 0.85rem;
    line-height: 1;
}

/* 日历按钮 */
.calendar-cell button {
    width: 80px;
    /* 撑满格子宽度 */
    height: 24px;
    /* 固定按钮高度 */
    font-size: 0.7rem;
    padding: 0;
    border-radius: 6px;
    background-color: #2d2d69;
    color: white;
    border: none;
    cursor: pointer;
    white-space: nowrap;
    text-overflow: ellipsis;
    transition: background-color 0.2s ease;
    user-select: none;
}

.calendar-cell button:hover {
    background-color: #4343e6;
}

/* 月度合计表 */
.monthly-summary {
    margin-top: 2rem;
}

.monthly-summary h4 {
    font-size: 1.1rem;
    font-weight: 700;
    color: #2d2d69;
    margin-bottom: 0.5rem;
}

.monthly-summary table {
    width: 100%;
    border-collapse: collapse;
}

.monthly-summary th,
.monthly-summary td {
    border: 1px solid #ccc;
    padding: 0.5rem;
    text-align: center;
    color: #2d2d69;
    font-weight: 600;
}

.monthly-summary th {
    background-color: #f7f9ff;
}

/* 关闭按钮 */
.modal-content>button {
    margin-top: 1.5rem;
    background-color: #2d2d69;
    color: white;
    padding: 10px 24px;
    border: none;
    border-radius: 12px;
    cursor: pointer;
    font-weight: 700;
    display: block;
    margin-left: auto;
    margin-right: auto;
    transition: background-color 0.3s ease;
}

.modal-content>button:hover {
    background-color: #1e1e4f;
}

/* 手机端折叠日历样式 */
.mobile-only {
    display: none;
}

/* 折叠面板样式 */
/* 周面板整体样式 */
.week-panel {
    margin-bottom: 1rem;
    border: 1px solid #ccc;
    border-radius: 12px;
    background-color: #f7f9ff;
    overflow: hidden;
    box-sizing: border-box;
}

.week-header {
    background-color: #2d2d69;
    color: white;
    font-weight: 700;
    padding: 0.6rem 1rem;
    display: flex;
    justify-content: space-between;
    align-items: center;
    cursor: pointer;
    user-select: none;
    font-size: 1rem;
    border-radius: 12px 12px 0 0;
}

.week-content {
    padding: 0.4rem 1rem 1rem 1rem;
    background-color: white;
    display: flex;
    flex-direction: column;
    gap: 0.8rem;
}

.day-panel {
    border: 1px solid #ccc;
    border-radius: 10px;
    overflow: hidden;
}

.day-header {
    background-color: #6c6cd2;
    color: white;
    font-weight: 700;
    padding: 0.5rem 1rem;
    display: flex;
    justify-content: space-between;
    align-items: center;
    cursor: pointer;
    user-select: none;
    font-size: 0.95rem;
    border-radius: 10px 10px 0 0;
}

.day-content {
    padding: 0.6rem 1rem;
    background-color: #f7f9ff;
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.daily-total {
  margin-top: auto; /* 推到容器底部 */
  font-weight: 700;
  color: #9696d0;
  font-size: 0.8rem;
  text-align: center;
  padding-top: 4px;
  border-top: 1px solid #ccc;
  width: 100%;
}

.item-row button {
    background-color: #2d2d69;
    border: none;
    border-radius: 8px;
    color: white;
    padding: 6px 10px;
    font-size: 0.85rem;
    cursor: pointer;
    white-space: nowrap;
    text-align: left;
    transition: background-color 0.3s ease;
}

.item-row button:hover {
    background-color: #1e1e4f;
}

.no-data {
    color: #aaa;
    font-size: 0.85rem;
    user-select: none;
    padding: 6px 10px;
}




/* 响应式：小屏幕显示折叠日历，隐藏桌面日历 */
@media screen and (max-width: 768px) {
    .desktop-only {
        display: none !important;
    }

    .mobile-only {
        display: block !important;
    }

    .modal-content {
        padding: 15px 5px;
    }
}
</style>
