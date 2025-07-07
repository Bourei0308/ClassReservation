<template>
  <div class="container lesson-container">
    <h2>授業履歴追加</h2>

    <div class="lesson-box-container">
      <div
        v-for="(lesson, index) in lessons"
        :key="index"
        class="lesson-box"
      >
        <button class="delete-button" @click="removeLesson(index)">×</button>

        <div class="participant-info">
          <button class="select-button" @click="lesson.showTeacherModal = true">
            {{ lesson.teacherName || '先生を選択' }}
          </button>
          <span class="role-label">（先生）</span>
          →
          <button class="select-button" @click="lesson.showStudentModal = true">
            {{ lesson.studentName || '生徒を選択' }}
          </button>
          <span class="role-label">（生徒）</span>
        </div>

        <div class="lesson-info">
          <div class="label-box">
            <div class="time-label">
              <span class="label-tag">日付</span>
              <input type="date" v-model="lesson.date" class="date-input" />
            </div>
            <div class="time-label">
              <span class="label-tag">時間</span>
              <input type="time" v-model="lesson.time" class="date-input" />
            </div>
            <div class="time-label">
              <span class="label-tag">コマ</span>
              <select v-model="lesson.duration" class="select-input">
                <option v-for="val in durations" :key="val" :value="val">{{ val }} 時間</option>
              </select>
            </div>
          </div>
        </div>

        <!-- モーダル -->
        <UserSelectModal
          :show="lesson.showTeacherModal"
          :role="2"
          title="先生を選択"
          @select="user => onSelectTeacher(index, user)"
          @close="lesson.showTeacherModal = false"
        />
        <UserSelectModal
          :show="lesson.showStudentModal"
          :role="1"
          title="生徒を選択"
          @select="user => onSelectStudent(index, user)"
          @close="lesson.showStudentModal = false"
        />
      </div>

      <!-- 右侧固定的加号按钮 -->
      <div class="add-icon" @click="addLesson()">
        ＋
      </div>
    </div>

    <!-- ボタン類 -->
    <div class="button-group">
  <button class="submit-button" @click="uploadLessons" :disabled="!isAllLessonsFilled">アップロード</button>
  <button class="reset-button" @click="resetLessons">リセット</button>
  <button class="submit-button" @click="exportCsv" :disabled="!isAllLessonsFilled">出力</button>
  <input type="file" @change="importCsv" accept=".csv" class="import-file" />
</div>
  </div>
</template>

<script setup>

import { useModalManager } from '@/scripts/useModalManager';
const {
  showAlert, closeAlert, alertProps,
  confirmShow, confirmMessage, openConfirm, openConfirmAsync, onConfirm, onCancel
} = useModalManager();

import { ref, onMounted } from 'vue';
import moment from 'moment';
import axios from 'axios';
import UserSelectModal from '@/components/popup_select_user.vue';

const durations = [0.5, 1, 1.5, 2, 2.5, 3];
const lessons = ref([]);

import { computed } from 'vue';

const isAllLessonsFilled = computed(() => {
  return lessons.value.length > 0 && lessons.value.every(lesson =>
    lesson.teacherId &&
    lesson.studentId &&
    lesson.date &&
    lesson.time &&
    lesson.duration
  );
});

function createLesson() {
  const now = moment();
  return {
    teacherId: '',
    teacherName: '',
    studentId: '',
    studentName: '',
    date: now.format('YYYY-MM-DD'),
    time: now.format('HH:mm'),
    duration: 1,
    showTeacherModal: false,
    showStudentModal: false,
  };
}

// 新增课程，默认追加在末尾
function addLesson() {
  let newLesson;
  if (lessons.value.length > 0) {
    const lastLesson = lessons.value[lessons.value.length - 1];
    newLesson = JSON.parse(JSON.stringify(lastLesson));
  } else {
    newLesson = createLesson();
  }
  lessons.value.push(newLesson);
  saveToLocalStorage();
}

// 删除指定索引课程
// 削除（确认后删除）
function removeLesson(index) {
  if (lessons.value.length <= 1) {
    showAlert('最低1つの授業が必要です。', false);
    return;
  }

  lessons.value.splice(index, 1);
    saveToLocalStorage();
}

function onSelectTeacher(index, user) {
  lessons.value[index].teacherId = user.id;
  lessons.value[index].teacherName = user.name;
  saveToLocalStorage();
}

function onSelectStudent(index, user) {
  lessons.value[index].studentId = user.id;
  lessons.value[index].studentName = user.name;
  saveToLocalStorage();
}

function uploadLessons() {
  const message = `現在 ${lessons.value.length} 件の授業をアップロードしようとしています。\n\n実行してもよろしいですか？`;

  openConfirm(message, async () => {
    try {
      const payload = lessons.value.map((lesson) => {
        const start = moment(`${lesson.date} ${lesson.time}`);
        const end = start.clone().add(lesson.duration, 'hours');
        return {
          teacherId: lesson.teacherId,
          studentId: lesson.studentId,
          startTime: start.toISOString(),
          endTime: end.toISOString(),
          status: 2,
        };
      });

      await axios.post('/api/class-schedules/bulk', payload);
      clearLocalStorage();
      showAlert('アップロード完了！', true);
    } catch (err) {
      showAlert('アップロード失敗: ' + err.message, false);
    }
  });
}

function resetLessons() {
  lessons.value = [createLesson()];
  clearLocalStorage();
}

function saveToLocalStorage() {
  localStorage.setItem('lessonDrafts', JSON.stringify(lessons.value));
}

function clearLocalStorage() {
  localStorage.removeItem('lessonDrafts');
}

function exportCsv() {
  const rows = lessons.value.map(l => [
    l.teacherId,l.teacherName, l.studentId,l.studentName, l.date, l.time, l.duration
  ]);
  const csvContent = ['先生ID,先生,生徒ID,生徒,日付,時間,コマ']
    .concat(rows.map(r => r.join(',')))
    .join('\n');

  const BOM = '\uFEFF'; // UTF-8 BOM
  const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' });

  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  link.download = 'lessons.csv';
  link.click();
}

function importCsv(event) {
  const file = event.target.files[0];
  const reader = new FileReader();
  reader.onload = (e) => {
    const lines = e.target.result.split('\n').slice(1); // Skip header
    lessons.value = lines
      .filter(line => line.trim())
      .map((line) => {
        const [teacherId, teacherName, studentId, studentName, date, time, duration] = line.split(',');
        return {
            teacherId,
          teacherName,
          studentId,
          studentName,
          date,
          time,
          duration: parseFloat(duration),
          showTeacherModal: false,
          showStudentModal: false,
        };
      });
    saveToLocalStorage();
  };
  reader.readAsText(file);
}

onMounted(() => {
  const draft = localStorage.getItem('lessonDrafts');
  if (draft) {
    lessons.value = JSON.parse(draft);
  } else {
    lessons.value.push(createLesson());
  }
});
</script>

<style scoped>
.container {
  padding: 20px;
  max-width: 1100px;
  margin: 30px auto;
  background-color: rgba(255, 255, 255, 0.9);
  border: 1px solid #ccc;
  border-radius: 20px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  
}

/* 标题居中 */
h2 {
  text-align: center;
  margin-bottom: 20px;
}

/* 课程盒子容器，使用flex布局，右侧留给加号 */
.lesson-box-container {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  position: relative;
}

/* 课程盒子 */
.lesson-box {
  position: relative;
  border: 1px solid #ccc;
  border-radius: 12px;
  padding: 12px;
  width: 320px;
  box-shadow: 2px 2px 8px rgba(0, 0, 0, 0.1);
  background: white;
}

/* 删除按钮X，绝对定位右上 */
.delete-button {
  position: absolute;
  top: 6px;
  right: 6px;
  background-color: #f44336;
  color: white;
  border: none;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  font-size: 18px;
  line-height: 20px;
  cursor: pointer;
  transition: background-color 0.3s ease, transform 0.2s ease;
  user-select: none;
  padding: 0;
}

.delete-button:hover {
  background-color: #a7342c;
  transform: scale(1.1);
}

/* 加号按钮，固定在课程盒子容器右侧，垂直居中 */
.add-icon {
  font-size: 40px;
  cursor: pointer;
  color: #007bff;
  align-self: center;
  user-select: none;
  width: 50px;
  height: 50px;
  line-height: 48px;
  border: 2px solid #007bff;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: background-color 0.3s ease, color 0.3s ease;
}

.add-icon:hover {
  background-color: #007bff;
  color: white;
}

/* 按钮组布局 */
.button-group {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 16px;
  justify-content: center;
  margin-top: 20px;
  padding: 0 10px;
  box-sizing: border-box;
}

.submit-button,
.reset-button {
  padding: 14px;
  font-size: 1.05rem;
  font-weight: 700;
  color: white;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  user-select: none;
  box-sizing: border-box;
  transition: all 0.3s ease;
}

.submit-button {
  background-color: #2d2d69eb;
}

.submit-button:hover {
  background-color: #0056b3;
  transform: scale(1.03);
}

.reset-button {
  background-color: #ff6666;
}

.reset-button:hover {
  background-color: #cc3333;
  transform: scale(1.05);
}

/* 参与者信息和样式 */
.participant-info {
  font-size: 16px;
  margin-bottom: 10px;
}

.select-button {
  margin: 0 5px;
  padding: 8px 16px;
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
  font-weight: 600;
}

.time-label {
  padding: 4px 8px;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.date-input,
.select-input {
  min-width: 140px;
  padding: 6px 10px;
  border-radius: 12px;
  border: 1.8px solid #2d2d69;
  font-weight: 600;
  color: #2d2d69;
  background-color: white;
  cursor: pointer;
  transition: border-color 0.3s ease;
}

.date-input:focus,
.select-input:focus {
  outline: none;
  border-color: #0056b3;
}

/* 文件导入按钮样式 */
.import-file {
  display: inline-block;
  cursor: pointer;
  margin-top: 6px;
}
.submit-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
  transform: none;
}

/* 小屏幕下响应式调整 */
@media (max-width: 768px) {
  .lesson-box-container {
    flex-direction: column;
    align-items: center;
  }

  .lesson-box {
    width: 100%;
    max-width: 400px;
  }

  .add-icon {
    margin-top: 12px;
  }
}
</style>
