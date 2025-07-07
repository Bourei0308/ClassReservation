<template>
  <div class="container">
    <h2>先生の生徒管理</h2>

    <!-- 🔸 教师选择 -->
    <div style="margin-bottom: 10px;">
      <button class="select-button" @click="showTeacherModal = true">
        {{ selectedTeacher ? selectedTeacher.name + ' さん' : '先生を選択' }}
      </button>
    </div>

    <!-- 🔸 警告：未选择教师 -->
    <div v-if="!selectedTeacher" style="color: red; margin-bottom: 20px;">
      先生を選択してください。
    </div>

    <!-- 🔸 教师已选择时 -->
    <div v-else>
      <!-- ✅ isStudent -->
      <div class="student-section">
        <h3>この先生の生徒：</h3>
        <div class="lesson-box-container">
          <div v-for="student in isStudents" :key="student.id" class="lesson-box">
            <div class="lesson-info">
              <div class="label-box">
                <div class="time-label">
                  <span class="label-tag">名前</span>
                  {{ student.name }}
                </div>
                <div class="time-label">
                  <span class="label-tag">メール</span>
                  {{ student.email || '未登録' }}
                </div>
                <div class="time-label">
                  <span class="label-tag">担当先生</span>
                  {{ getTeacherNames(student.id).join(' / ') }}
                </div>
              </div>
              <button class="delete-button" @click="confirmRemove(student)">−</button>
            </div>
          </div>
        </div>
      </div>

      <!-- ✅ notStudent -->
      <div class="student-section" style="margin-top: 30px;">
        <h3>未担当の生徒：</h3>
        <div class="lesson-box-container">
          <div v-for="student in notStudents" :key="student.id" class="lesson-box">
            <div class="lesson-info">
              <div class="label-box">
                <div class="time-label">
                  <span class="label-tag">名前</span>
                  {{ student.name }}
                </div>
                <div class="time-label">
                  <span class="label-tag">メール</span>
                  {{ student.email || '未登録' }}
                </div>
              </div>
              <button class="add-button" @click="confirmAdd(student)">＋</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 🔸 教师选择模态 -->
    <UserSelectModal
      :show="showTeacherModal"
      :role="2"
      title="先生を選択"
      @select="onSelectTeacher"
      @close="showTeacherModal = false"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';

import UserSelectModal from '@/components/popup_select_user.vue';
import { useModalManager } from '@/scripts/useModalManager';

const showTeacherModal = ref(false);
const selectedTeacher = ref(null);
const allStudents = ref([]);
const allRelations = ref([]);

const {
  showAlert, closeAlert, alertProps,
  confirmShow, confirmMessage, openConfirm, onConfirm, onCancel
} = useModalManager();

// 🔹 选择老师后触发
const onSelectTeacher = (teacher) => {
  selectedTeacher.value = teacher;
  showTeacherModal.value = false;
  fetchData();
};

// 🔹 获取关系和学生
const fetchData = async () => {
  try {
    const [studentsRes, relationsRes] = await Promise.all([
      axios.get('/api/users/students'),
      axios.get('/api/relations'),
    ]);
    allStudents.value = studentsRes.data;
    allRelations.value = relationsRes.data;
  } catch (err) {
    showAlert('データ取得失敗: ' + err.message, false);
  }
};

// 🔹 该老师的学生列表
const isStudents = computed(() => {
  if (!selectedTeacher.value) return [];
  const teacherId = selectedTeacher.value.id;
  const studentIds = allRelations.value
    .filter(r => r.teacherId === teacherId)
    .map(r => r.studentId);
  return allStudents.value.filter(s => studentIds.includes(s.id));
});

// 🔹 非该老师的学生列表
const notStudents = computed(() => {
  const selected = new Set(isStudents.value.map(s => s.id));
  return allStudents.value.filter(s => !selected.has(s.id));
});

// 🔹 某学生的所有教师名字
const getTeacherNames = (studentId) => {
  const teacherIds = allRelations.value
    .filter(r => r.studentId === studentId)
    .map(r => r.teacherId);
  const names = teacherIds
    .map(id => id === selectedTeacher.value.id
      ? selectedTeacher.value.name
      : null);
  return names.filter(Boolean);
};

// 🔹 删除关系确认
const confirmRemove = (student) => {
  const msg = `${student.name}さんとの関係を削除しますか？`;
  openConfirm(msg, () => removeRelation(student.id));
};

// 🔹 添加关系确认
const confirmAdd = (student) => {
  const msg = `${student.name}さんをこの先生に追加しますか？`;
  openConfirm(msg, () => addRelation(student.id));
};

// 🔹 删除关系（使用 id）
const removeRelation = async (studentId) => {
  const id = `${selectedTeacher.value.id}-${studentId}`;
  try {
    await axios.delete(`/api/relations/${id}`);
    showAlert("削除が完了しました！", true);
    await fetchData();
  } catch (err) {
    showAlert("削除に失敗: " + err.message, false);
  }
};

// 🔹 添加关系（仍通过 POST，后端会自行生成 id）
const addRelation = async (studentId) => {
  try {
    await axios.post('/api/relations', {
      teacherId: selectedTeacher.value.id,
      studentId: studentId
    });
    showAlert("追加が完了しました！", true);
    await fetchData();
  } catch (err) {
    showAlert("追加に失敗: " + err.response?.data || err.message, false);
  }
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.container {
  padding: 20px;
  max-width: 1100px;
  margin: 30px auto;
  background-color: rgba(255, 255, 255, 0.95);
  border: 1px solid #ccc;
  border-radius: 20px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  
}

h2, h3 {
  text-align: center;
  margin-bottom: 16px;
}

.lesson-box-container {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  justify-content: center;
}

.lesson-box {
  border: 1px solid #ccc;
  border-radius: 12px;
  padding: 12px;
  width: 300px;
  position: relative;
  background: white;
}

.label-tag {
  background-color: #009dff;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: bold;
  min-width: 60px;
  display: inline-block;
}

.time-label {
  display: flex;
  gap: 10px;
  padding: 4px 0;
  align-items: center;
}

.lesson-info {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.delete-button {
  background-color: red;
  color: white;
  border: none;
  border-radius: 50%;
  width: 28px;
  height: 28px;
  font-size: 18px;
  cursor: pointer;
}

.add-button {
  background-color: green;
  color: white;
  border: none;
  border-radius: 50%;
  width: 28px;
  height: 28px;
  font-size: 18px;
  cursor: pointer;
}

.select-button {
  padding: 8px 16px;
  font-weight: bold;
  background-color: #2d2d69;
  color: white;
  border: none;
  border-radius: 12px;
  cursor: pointer;
}
</style>
