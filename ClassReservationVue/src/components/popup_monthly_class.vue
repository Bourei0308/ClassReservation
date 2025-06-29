<template>
  <div v-if="show" class="modal-overlay">
    <div class="modal-content">
      <h3>{{ roleName }}今年の授業一覧</h3>

      <!-- ✅ 桌面显示用表格 -->
      <div class="table-wrapper desktop-only">
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
                <button v-if="item.monthlyHours[m]" @click="selectMonth(item, m)">
                  {{ item.monthlyHours[m] }}時間
                </button>
                <span v-else>-</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- ✅ 手机端：每位老师/学生一个卡片 -->
      <div class="mobile-only card-list">
    <div
      class="card"
      v-for="item in summaryData"
      :key="item.id"
    >
      <div class="card-header" @click="toggleFold(item.id)">
        <span class="card-name">{{ item.name }}</span>
        <span class="fold-toggle">
          {{ foldedIds.includes(item.id) ? '▶︎' : '▼' }}
        </span>
      </div>

      <!-- 折り畳み対象内容 -->
      <div v-if="!foldedIds.includes(item.id)" class="month-grid">
        <div class="month" v-for="m in 12" :key="m">
          <div class="label">{{ m }}月</div>
          <div class="value">
            <button v-if="item.monthlyHours[m]" @click="selectMonth(item, m)">
              {{ item.monthlyHours[m] }}時間
            </button>
            <span v-else>-</span>
          </div>
        </div>
      </div>
    </div>
  </div>

      <button @click="$emit('close')">閉じる</button>
    </div>
  </div>
</template>



<style scoped>
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: #fff;
  padding: 1.5rem;
  max-height: 90vh;
  overflow-y: auto;
  width: 95vw;
  max-width: 960px;
  border-radius: 16px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.25);
  font-family: Arial, sans-serif;
  box-sizing: border-box;
}

.modal-content h3 {
  text-align: center;
  color: #2d2d69;
  font-size: 1.4rem;
  font-weight: bold;
  margin-bottom: 16px;
}

/* PC端表格样式 */
.table-wrapper {
  overflow-x: auto;
  margin-bottom: 16px;
}
table {
  border-collapse: collapse;
  width: 100%;
  min-width: 900px;
}
th, td {
  border: 1px solid #ccc;
  text-align: center;
  padding: 0.6rem;
  white-space: nowrap;
  min-width: 80px;
  max-width: 100px;
}
thead th {
  background-color: #f0f3ff;
  color: #2d2d69;
  font-weight: 700;
}
td button {
  background-color: #2d2d69;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 6px 10px;
  width: 100%;
  max-width: 90px;
  font-size: 0.85rem;
  cursor: pointer;
}
td button:hover {
  background-color: #1e1e4f;
}

/* 关闭按钮 */
.modal-content > button {
  margin-top: 12px;
  background-color: #2d2d69;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-weight: bold;
  display: block;
  margin-left: auto;
  margin-right: auto;
  transition: background-color 0.3s ease;
}
.modal-content > button:hover {
  background-color: #1e1e4f;
}

/* 📱 手机端卡片式显示 */
.card-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 20px;
}
/* 每位学生的卡片样式增强 */
.card {
  border: 1px solid #ccc;
  border-radius: 14px;
  padding: 16px;
  background-color: #f7f9ff;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px; /* ✅ 学生之间加大间隔 */
}

/* 学生名样式更醒目 */
.card-name {
  font-weight: bold;
  color: #2d2d69;
  margin-bottom: 12px;
  font-size: 1.1rem;
  text-align: center;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 1.1rem;
  color: #2d2d69;
  padding: 8px 4px;
  cursor: pointer;
  border-bottom: 1px dashed #bbb;
  margin-bottom: 8px;
  user-select: none;
}

.fold-toggle {
  font-size: 1.2rem;
  color: #888;
}

/* 月份网格 */
.month-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px 16px; /* ✅ 行间距更大、列间距适中 */
}

/* 每个月盒子 */
.month {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px;
  border-radius: 12px;
  background-color: #eef2ff; /* ✅ 月份块背景色 */
  box-shadow: inset 0 0 0 1px #d0d5ff;
}

/* 月份 label 加背景 */
.label {
  font-size: 0.85rem;
  font-weight: bold;
  color: #2d2d69;
  background-color: #d7def9;
  padding: 2px 8px;
  border-radius: 8px;
  margin-bottom: 6px;
}

/* 有数据按钮样式（维持统一） */
.value button {
  background-color: #2d2d69;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 4px 10px;
  font-size: 0.85rem;
  width: 100%;
  max-width: 120px;
  text-align: center;
}

/* 无数据的 - 样式 */
.value span {
  color: #aaa;
  font-size: 0.9rem;
}


/* ✅ 响应式控制显示哪一块 */
.desktop-only {
  display: block;
}
.mobile-only {
  display: none;
}

@media (max-width: 768px) {
  .desktop-only {
    display: none;
  }
  .mobile-only {
    display: block;
  }
}
</style>


<script setup>
import { watch, computed,ref } from 'vue';

// 传入参数
const props = defineProps({
  show: Boolean,
  role: Number, // 2=先生, 1=生徒
  lessons: Array, // 传入主页面的 lessons.value
});

const emit = defineEmits(['close', 'select']);

const roleName = props.role === 2 ? '先生' : '生徒';

const year = new Date().getFullYear();

const foldedIds = ref([])
const toggleFold = (id) => {
  if (foldedIds.value.includes(id)) {
    foldedIds.value = foldedIds.value.filter(f => f !== id)
  } else {
    foldedIds.value.push(id)
  }
}

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
