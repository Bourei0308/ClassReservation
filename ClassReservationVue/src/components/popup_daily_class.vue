<template>
    <div v-if="show" class="modal-overlay">
        <div class="modal-content">
            <div class="header">
                <button @click="changeMonth(-1)">&lt;先月</button>
                <h3>{{ roleName }} {{ currentYear }}年{{ currentMonth }}月の授業記録</h3>
                <button @click="changeMonth(1)">次月&gt;</button>
            </div>

            <input type="month" v-model="monthPicker" @change="onMonthPickerChange" />

            <div class="calendar-grid">
                <div class="calendar-header" v-for="(d, i) in 7" :key="i">
                    {{ ['日', '月', '火', '水', '木', '金', '土'][i] }}
                </div>
                <div v-for="(cell, index) in calendarCells" :key="index"
                    :class="['calendar-cell', cell.isCurrentMonth ? '' : 'inactive']">
                    <div class="date-number">{{ cell.day }}</div>
                    <div v-for="item in summaryData" :key="item.id">
                        <button v-if="cell.isCurrentMonth && item.dailyHours[cell.day]"
                            @click="selectDay(item, cell.day)">
                            {{ item.name }}: {{ item.dailyHours[cell.day] }}h
                        </button>
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
import { ref, computed } from 'vue';

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
        if (
            date.getFullYear() !== currentYear.value ||
            date.getMonth() + 1 !== currentMonth.value
        ) continue;

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

    // 前の月の空白
    for (let i = 0; i < firstDayOfWeek; i++) {
        cells.push({ day: prevMonthDays - firstDayOfWeek + i + 1, isCurrentMonth: false });
    }
    // 今月の本体
    for (let i = 1; i <= totalDays; i++) {
        cells.push({ day: i, isCurrentMonth: true });
    }
    // 次の月の空白
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
</script>

<style scoped>
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
}

.modal-content {
    background: white;
    padding: 1rem;
    max-height: 90vh;
    overflow: auto;
    width: 95vw;
    max-width: 1000px;
}

.header {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 1rem;
    margin-bottom: 1rem;
}

.calendar-grid {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    gap: 0.5rem;
    margin-top: 1rem;
}

.calendar-header {
    font-weight: bold;
    text-align: center;
}

.calendar-cell {
    border: 1px solid #ccc;
    padding: 0.5rem;
    min-height: 60px;
    background: #f9f9f9;
}

.calendar-cell.inactive {
    background: #eee;
    color: #aaa;
}

.date-number {
    font-weight: bold;
    margin-bottom: 0.3rem;
}

button {
    cursor: pointer;
    background: none;
    border: none;
    color: blue;
    text-decoration: underline;
    font-size: 0.8rem;
}

.monthly-summary {
  margin-top: 2rem;
}
.monthly-summary table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 0.5rem;
}
.monthly-summary th,
.monthly-summary td {
  border: 1px solid #ccc;
  padding: 0.5rem;
  text-align: center;
}
.monthly-summary h4 {
  margin-bottom: 0.5rem;
  font-size: 1rem;
  font-weight: bold;
}
</style>