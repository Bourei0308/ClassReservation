<template>
  <div class="container lesson-container">
    <h2>ユーザ一括追加</h2>

    <div class="lesson-box-container">
      <div v-for="(user, index) in users" :key="index" class="lesson-box">
        <button class="delete-button" @click="removeUser(index)">×</button>

        <div class="lesson-info">
          <div class="label-box">
            <div class="time-label">
              <span class="label-tag">アカウント</span>
              <input v-model="user.account" class="date-input" />
            </div>
            <div class="time-label">
              <span class="label-tag">ユーザ名</span>
              <input v-model="user.name" class="date-input" />
            </div>
            <div class="time-label">
              <span class="label-tag">パスワード</span>
              <input type="password" v-model="user.password" class="date-input" />
            </div>
            <div class="time-label">
              <span class="label-tag">メール（任意）</span>
              <input v-model="user.email" class="date-input" />
            </div>
            <div class="time-label">
              <span class="label-tag">役割</span>
              <select v-model="user.role" class="select-input">
                <option :value="1">生徒</option>
                <option :value="2">先生</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      <div class="add-icon" @click="addUser">＋</div>
    </div>

    <div class="button-group">
      <button class="submit-button" @click="submitUsers" :disabled="!isValid">アップロード</button>
      <button class="reset-button" @click="resetUsers">リセット</button>
      <button class="submit-button" @click="exportCsv" :disabled="!isValid">出力</button>
      <input type="file" @change="importCsv" accept=".csv" class="import-file" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';
import { useModalManager } from '@/scripts/useModalManager';

const {
  showAlert, openConfirm
} = useModalManager();

const users = ref([]);

function createUser() {
  return {
    account: '',
    name: '',
    password: '',
    email: '',
    role: 1
  };
}

function addUser() {
  users.value.push(createUser());
  saveToLocalStorage();
}

function removeUser(index) {
  if (users.value.length <= 1) {
    showAlert('最低1人のユーザが必要です。', false);
    return;
  }
  users.value.splice(index, 1);
  saveToLocalStorage();
}

const isValid = computed(() =>
  users.value.length > 0 && users.value.every(u => u.account && u.name && u.password && u.role)
);

function submitUsers() {
  openConfirm(`現在 ${users.value.length} 人のユーザを追加しようとしています。\n\n実行してもよろしいですか？`, async () => {
    try {
      await axios.post('/api/users/bulk', users.value);
      clearLocalStorage();
      showAlert('アップロード完了！', true);
    } catch (err) {
      showAlert('アップロード失敗: ' + err.message, false);
    }
  });
}

function resetUsers() {
  users.value = [createUser()];
  clearLocalStorage();
}

function saveToLocalStorage() {
  localStorage.setItem('userDrafts', JSON.stringify(users.value));
}

function clearLocalStorage() {
  localStorage.removeItem('userDrafts');
}

function exportCsv() {
  const rows = users.value.map(u => [u.account, u.name, u.password, u.email, u.role]);
  const csvContent = ['アカウント,ユーザ名,パスワード,メール,役割']
    .concat(rows.map(r => r.join(',')))
    .join('\n');

  const BOM = '\uFEFF';
  const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  link.download = 'users.csv';
  link.click();
}

function importCsv(event) {
  const file = event.target.files[0];
  const reader = new FileReader();
  reader.onload = (e) => {
    const lines = e.target.result.split('\n').slice(1);
    users.value = lines.filter(line => line.trim()).map(line => {
      const [account, name, password, email, role] = line.split(',');
      return {
        account, name, password, email, role: parseInt(role)
      };
    });
    saveToLocalStorage();
  };
  reader.readAsText(file);
}

onMounted(() => {
  const draft = localStorage.getItem('userDrafts');
  if (draft) {
    users.value = JSON.parse(draft);
  } else {
    users.value.push(createUser());
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
