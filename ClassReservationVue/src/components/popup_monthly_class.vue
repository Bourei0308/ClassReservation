<template>
  <div v-if="show" class="modal-overlay">
    <div class="modal-content">
      <h3>{{ roleName }}今年の授業一覧</h3>
      <table>
        <thead>
          <tr>
            <th>{{ roleName }}名</th>
            <th v-for="m in 12" :key="m">{{ m }}月</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in summaryData" :key="item.id">
            <td>{{ item.name }}</td>
            <td v-for="m in 12" :key="m">
              <button
                v-if="item.monthlyHours[m]"
                @click="selectMonth(item, m)"
              >
                {{ item.monthlyHours[m] }}時間
              </button>
              <span v-else>-</span>
            </td>
          </tr>
        </tbody>
      </table>
      <button @click="$emit('close')">閉じる</button>
    </div>
  </div>
</template>

<script setup>
import { watch, computed } from 'vue';

// 传入参数
const props = defineProps({
  show: Boolean,
  role: Number, // 2=先生, 1=生徒
  lessons: Array, // 传入主页面的 lessons.value
});

const emit = defineEmits(['close', 'select']);

const roleName = props.role === 2 ? '先生' : '生徒';

const year = new Date().getFullYear();

// 计算每位老师/学生每月的状态为2课时小时数汇总
const summaryData = computed(() => {
  if (!props.lessons || props.lessons.length === 0) return [];

  const groupKey = props.role === 2 ? 'teacherName' : 'studentName';
  const idKey = props.role === 2 ? 'teacherId' : 'studentId';

  // 另外的ID key，比如角色是老师，则另一个是学生的ID，反之亦然
  const otherIdKey = props.role === 2 ? 'studentId' : 'teacherId';

  const map = new Map();

  function getDurationHours(timeStr) {
    const [start, end] = timeStr.split('〜');
    const [startH, startM] = start.split(':').map(Number);
    const [endH, endM] = end.split(':').map(Number);
    let diff = (endH - startH) + (endM - startM) / 60;
    if (diff < 0) diff += 24;
    return diff;
  }

  for (const lesson of props.lessons) {
    if (lesson.status !== 2) continue;

    const lessonYear = lesson.date.split('-')[0];
    if (Number(lessonYear) !== year) continue;

    const name = lesson[groupKey];
    const id = lesson[idKey];
    const otherId = lesson[otherIdKey];

    if (!name || !id) continue;

    if (!map.has(id)) {
      map.set(id, { 
        id, 
        name, 
        monthlyHours: {}, 
        teacherId: props.role === 2 ? id : otherId,
        studentId: props.role === 2 ? otherId : id,
      });
    }
    const item = map.get(id);

    const month = Number(lesson.date.split('-')[1]);
    const duration = getDurationHours(lesson.time);

    item.monthlyHours[month] = (item.monthlyHours[month] || 0) + duration;
  }

  for (const item of map.values()) {
    for (const m in item.monthlyHours) {
      item.monthlyHours[m] = item.monthlyHours[m].toFixed(1);
    }
  }

  return Array.from(map.values()).sort((a, b) => a.name.localeCompare(b.name));
});


function selectMonth(item, month) {
    console.log(item.id, item.name, month)
  emit('select', { id: item.id, name: item.name, month });
  emit('close');
}
</script>

<style scoped>
.modal-overlay {
  position: fixed; top: 0; left: 0; right:0; bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex; justify-content: center; align-items: center;
  z-index: 1000;
}
.modal-content {
  background: white; padding: 1rem; max-height: 80vh; overflow: auto;
  width: 90vw; max-width: 900px;
}
table {
  border-collapse: collapse; width: 100%;
}
th, td {
  border: 1px solid #ccc; padding: 0.5rem; text-align: center;
}
button {
  cursor: pointer;
  background: none;
  border: none;
  color: blue;
  text-decoration: underline;
}
</style>
