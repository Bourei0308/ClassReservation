<template>
  <transition name="slide-fade" appear>
    <div class="modal-overlay" v-if="show">

      <div class="modal-content">
        <h3>{{ title || $t('popup_charge_history.title') }}</h3>

        <!-- ▼ 生徒選択ボタン，占满一整行 -->
        <button class="full-width-button" @click="showUserSelect = true">
          {{ $t('popup_charge_history.selectStudent') }}
        </button>

        <!-- ▼ 选中状态居中显示 -->
        <div class="selected-student-wrapper" v-if="selectedStudent">
          <span>{{ $t('popup_charge_history.selected') }}：{{ selectedStudent.name }}</span>
          <button class="clear-button" @click="clearStudent">{{ $t('popup_charge_history.clear') }}</button>
        </div>

        <!-- ▼ 履歴表 -->
        <table class="history-table">
          <thead>
            <tr>
              <th>{{ $t('popup_charge_history.student') }}</th>
              <th>{{ $t('popup_charge_history.charge') }}</th>
              <th>{{ $t('popup_charge_history.datetime') }}</th>
              <th>{{ $t('popup_charge_history.actions') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in histories" :key="item.id">
              <td>{{ usersMap.get(item.studentId) || '不明' }}</td>
              <td>{{ item.chargeHours }} H</td>
              <td>{{ formatDate(item.createdAt) }}</td>
              <td>
                <button class="delete-button" @click="confirmDelete(item)">
                  {{ $t('popup_charge_history.delete') }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>

        <button class="close-button" @click="$emit('close')">
          {{ $t('popup_charge_history.close') }}
        </button>
      </div>


    </div>
  </transition>
  <!-- ▼ 生徒選択モーダル -->
  <UserSelectModal v-if="showUserSelect" :show="showUserSelect" :role="1"
    :title="$t('popup_charge_history.selectStudent')" @select="onUserSelect" @close="showUserSelect = false" />
  <AlertModal v-bind="alertProps" @close="closeAlert" />
  <ConfirmDialog :show="confirmShow" :message="confirmMessage" @confirm="onConfirm" @cancel="onCancel" />
</template>

<script setup>
import { ref, watchEffect } from 'vue';
import axios from 'axios';
import UserSelectModal from '@/components/popup_select_user.vue';

const props = defineProps({
  show: Boolean,
  student: Object,
  title: String
});
const emit = defineEmits(['close', 'deleted']);

// 🔸 alert
import AlertModal from '@/components/popup_message_alert.vue';
import ConfirmDialog from '@/components/popup_message_confirm.vue';
import { useModalManager } from '@/scripts/useModalManager';
const {
  showAlert, closeAlert, alertProps,
  confirmShow, confirmMessage, openConfirm, onConfirm, onCancel
} = useModalManager();

const histories = ref([]);
const usersMap = ref(new Map()); // ← ID→名前のMap
const showUserSelect = ref(false);
const selectedStudent = ref(props.student || null);

// 日付フォーマット
const formatDate = (dateStr) => {
  const d = new Date(dateStr);
  return d.toLocaleString('ja-JP');
};

// 🔸 ユーザー情報取得（id→name マップ用）
const loadUsers = async () => {
  try {
    const res = await axios.get('/api/users');
    const users = res.data;
    usersMap.value = new Map(users.map(user => [user.id, user.name]));
  } catch (err) {
    alert('ユーザー情報の取得に失敗: ' + err.message);
  }
};

// 履歴取得
const loadHistories = async () => {
  try {
    const res = selectedStudent.value
      ? await axios.get(`/api/charges/users/${selectedStudent.value.id}`)
      : await axios.get('/api/charges');

    histories.value = res.data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
  } catch (err) {
    alert('チャージ履歴の取得に失敗: ' + err.message);
  }
};

// 削除

import { useI18n } from 'vue-i18n'
const { t } = useI18n()

const confirmDelete = async (item) => {
  const confirmed = await openConfirm({
    title: t('popup_charge_history.confirmDeleteTitle'),
    message: t('popup_charge_history.confirmDeleteMessage'),
  })

  if (!confirmed) return

  try {
    await axios.delete(`/api/charge/${item.id}`)
    showAlert(t('popup_charge_history.deleteSuccess'))
    fetchChargeHistory()
  } catch {
    showAlert(t('popup_charge_history.deleteFail'))
  }
}

// 生徒選択
const onUserSelect = (user) => {
  selectedStudent.value = user;
  loadHistories();
};

// 選択解除
const clearStudent = () => {
  selectedStudent.value = null;
  emit('deleted');
  loadHistories();
};

// モーダル表示中に履歴とユーザー取得
watchEffect(() => {
  if (props.show) {
    selectedStudent.value = props.student || selectedStudent.value;
    loadUsers();
    loadHistories();
  }
});
</script>



<style scoped>
.modal-content {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  width: 90%;
  max-width: 700px;
  box-shadow: 0 8px 20px rgba(45, 45, 105, 0.3);
  
  color: #2d2d69;
  box-sizing: border-box;
}

h3 {
  text-align: center;
  margin-bottom: 1.5rem;
  font-weight: 700;
  font-size: 1.5rem;
  color: #2d2d69;
}

/* 生徒選択按钮，宽度100% */
.full-width-button {
  display: block;
  width: 100%;
  background-color: #2d2d69;
  color: white;
  font-weight: 700;
  font-size: 1rem;
  padding: 0.75rem 0;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 1rem;
  transition: background-color 0.3s ease;
}

.full-width-button:hover {
  background-color: #1e1e4f;
}

/* 选中学生信息与解除按钮居中 */
.selected-student-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
  font-weight: 600;
  font-size: 1rem;
  color: #2d2d69;
}

.clear-button {
  background-color: #e0e0e0;
  border: none;
  border-radius: 8px;
  padding: 0.3rem 0.8rem;
  cursor: pointer;
  font-weight: 600;
  color: #2d2d69;
  transition: background-color 0.3s ease;
}

.clear-button:hover {
  background-color: #c5c5c5;
}

/* 表格样式 */
.history-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 1.5rem;
  font-size: 0.95rem;
  color: #2d2d69;
}

.history-table th,
.history-table td {
  border: 1px solid #ccc;
  padding: 0.6rem 0.8rem;
  text-align: center;
}

.history-table thead th {
  background-color: #f7f9ff;
  font-weight: 700;
  color: #2d2d69;
}

/* 删除按钮 */
.delete-button {
  background-color: #d9534f;
  color: white;
  border: none;
  padding: 0.4rem 0.8rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: background-color 0.3s ease;
}

.delete-button:hover {
  background-color: #b52b27;
}

/* 关闭按钮居中 */
.close-button {
  display: block;
  margin: 0 auto;
  background-color: #2d2d69;
  color: white;
  border: none;
  padding: 0.6rem 1.5rem;
  border-radius: 10px;
  font-weight: 700;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.close-button:hover {
  background-color: #1e1e4f;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-content.slide-fade-enter-active,
.modal-content.slide-fade-leave-active {
  transition: opacity 0.35s ease, transform 0.35s ease;
}

.modal-content.slide-fade-enter-from,
.modal-content.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(-30px);
}

.modal-content.slide-fade-enter-to,
.modal-content.slide-fade-leave-from {
  opacity: 1;
  transform: translateY(0);
}
</style>