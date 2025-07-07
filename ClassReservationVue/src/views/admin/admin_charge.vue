<template>
  <body>
  <div class="container">

    <h2>学生授業時間チャージ</h2>

    <!-- 👤 生徒選択 -->
    <label>生徒を選択:</label>
    <div class="user-select-box">
      <div class="selected-user" @click="showUserSelect = true">
        {{ selectedUser ? selectedUser.name : '生徒を選択' }}
      </div>
      <button @click="showUserSelect = true">選択</button>
    </div>

    <!-- 💳 チャージ操作 -->
    <div class="charge-row">

      <div v-if="currentHours !== null" class="current-hours">
        現在のコマ数：{{ currentHours }}
      </div>
      <input class="charge-input" type="number" v-model.number="chargeAmount" min="0" step="0.5" required />

      <button @click="confirmCharge">追加</button>
    </div>

    <!-- 📄 一覧表示 -->
    <h2 style="margin-top: 30px;">全生徒の現在のコマ数</h2>
    <div class="student-list">
      <div v-for="student in studentHoursList" :key="student.id" class="student-box">
        <span class="student-name">{{ student.name }}</span><span class="student-hour">残り <span
            class="student-hour-number">{{ student.hours }}</span> 時間</span><span class="student-hour">チャージ <span
            class="student-hour-number">{{ student.charge }}</span> 時間</span>
        <button @click="editPopup(student)" class="edit-button">編集</button>
      </div>
    </div>
    <button @click="showHistoryModal = true">チャージ履歴を見る</button>

    <!-- 🪟 モーダル -->
    <UserSelectModal :show="showUserSelect" :role="1" title="生徒を選択" @select="handleUserSelect"
      @close="showUserSelect = false" />
    <ChargeHistoryModal v-if="showHistoryModal" :show="showHistoryModal" :student="selectedUser" title="チャージ履歴"
      @close="showHistoryModal = false" @deleted="refresh" />
    <AlertModal v-bind="alertProps" @close="closeAlert" />
    <ConfirmDialog :show="confirmShow" :message="confirmMessage" @confirm="onConfirm" @cancel="onCancel" />
  </div>
  </body>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from "axios";
import moment from 'moment';
import UserSelectModal from "@/components/popup_select_user.vue";
import ChargeHistoryModal from "@/components/popup_edit_charge.vue";

// 🔸 alert
import AlertModal from '@/components/popup_message_alert.vue';
import ConfirmDialog from '@/components/popup_message_confirm.vue';
import { useModalManager } from '@/scripts/useModalManager';
const {
  showAlert, closeAlert, alertProps,
  confirmShow, confirmMessage, openConfirm, onConfirm, onCancel
} = useModalManager();

// 🔸 refs
const students = ref([]);
const selectedUser = ref(null);
const chargeAmount = ref(0);
const currentHours = ref(null);
const totalCharged = ref(null);
const studentHoursList = ref([]);
const showHistoryModal = ref(false);
const showUserSelect = ref(false);

// 🔸 computed
const canCharge = computed(() => {
  return selectedUser.value && chargeAmount.value > 0;
});

// 🔸 学生一覧取得
const loadStudents = async () => {
  try {
    const res = await axios.get("/api/users");
    students.value = res.data.filter((user) => user.role === 1);
  } catch (err) {
    showAlert("ユーザー一覧取得失敗: " + err.message, false);
  }
};

// 🔸 特定生徒の現在のコマ数
const loadCurrentHours = async (userId) => {
  try {
    const chargeUrl = `/api/charges/users/${userId}/total`;
    const usageUrl = `/api/class-schedules/student/${userId}/total-hours`;

    const [charged, used] = await Promise.all([
      axios.get(chargeUrl),
      axios.get(usageUrl)
    ]);

    const remaining = Math.max(0, charged.data - used.data);
    return [remaining.toFixed(1), charged.data];
  } catch (err) {
    showAlert("ユーザーコマ数取得失敗: " + err.message, false);
  }
};

// 🔸 ユーザー選択時
const handleUserSelect = async (user) => {
  selectedUser.value = user;
  const [remain, charge] = await loadCurrentHours(user.id);
  currentHours.value = remain;
  totalCharged.value = charge;
};

const confirmCharge = () => {
  if (!canCharge.value) {
    showAlert("生徒とチャージ時間を正しく入力してください。", false);
    return;
  }
  const msg = `${selectedUser.value.name}さんに ${chargeAmount.value} 時間をチャージします。\nチャージ情報をチェックしてください。`;
  openConfirm(msg, doCharge);
};

// 🔸 チャージ処理
const doCharge = async () => {
  try {
    await axios.post(
      `/api/charges/users/${selectedUser.value.id}`,
      {
        chargeHours: chargeAmount.value,
        createdAt: moment().format('YYYY-MM-DDTHH:mm:ss')
      }
    );
    showAlert("チャージが完了しました！", true);
    chargeAmount.value = 0;

    const [remain, charge] = await loadCurrentHours(selectedUser.value.id);
    currentHours.value = remain;
    totalCharged.value = charge;

    await loadAllStudentHours();
  } catch (err) {
    showAlert("チャージ失敗: " + err.message, false);
  }
};

// 🔸 全生徒の現在のコマ数取得
const loadAllStudentHours = async () => {
  const results = await Promise.all(
    students.value.map(async (user) => {
      const [hours, charge] = await loadCurrentHours(user.id);
      return {
        id: user.id,
        name: user.name,
        hours,
        charge
      };
    })
  );
  studentHoursList.value = results;
};

const editPopup = (student) => {
  selectedUser.value = {
    id: student.id,
    name: student.name,
  };
  showHistoryModal.value = true;
};

const refresh = async () => {
  selectedUser.value=null;
  await loadAllStudentHours();
};

// 🔸 初期化
const initialize = async () => {
  await loadStudents();
  await loadAllStudentHours();
};

onMounted(initialize);



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

.user-select-box {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 5px;
}

.selected-user {
  flex: 1;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
  background-color: #f9f9f9;
  cursor: pointer;
  text-align: left;
}

.selected-user:hover {
  background-color: #ececec;
}

.container {
  max-width: 700px;
  margin: 30px auto;
  padding: 20px 70px;
  background-color: white;
  border: 2px solid #ccc;
  border-radius: 12px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  text-align: center;
}

h2 {
  font-size: 18px;
  margin-bottom: 10px;
  color: #333;
}

label {
  display: block;
  margin-top: 15px;
  font-weight: bold;
  text-align: left;
}

select,
input[type="number"] {
  width: 100%;
  padding: 10px;
  margin-top: 5px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 14px;
}

button {
  padding: 10px 20px;
  font-size: 14px;
  background-color: #2d2d69eb;
  color: #fff;
  border: 0px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

button:hover {
  background-color: #0056b3;
}

.search-bar {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  position: relative;
}

.search-bar input {
  width: 100%;
  padding: 8px 30px 8px 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
}

.search-icon {
  position: absolute;
  right: 10px;
  color: #888;
}

.current-hours {
  margin-top: 10px;
  font-size: 14px;
  color: #444;
  text-align: left;
}

.charge-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 15px;
  justify-content: space-between;
}

.charge-input {
  flex: 1;
  min-width: 80px;
}

button {
  white-space: nowrap;
}

.student-name {
  width: 100px;
  display: inline-block;
  color: rgb(0, 145, 255);
  font-weight: 700;
  border-left: 3px solid rgb(0, 145, 255);
  border-radius: 3px;
  padding-left: 10px;
}

.student-hour-number {
  font-weight: 700;
  color: rgb(0, 145, 255);
  font-size: 1.2rem;
}

.student-box {
  text-align: left;
  margin-bottom: 5px;
  border-bottom: 1px solid rgba(0, 145, 255, 0.2);
  padding-bottom: 2px;
}

.student-hour {
  width: 180px;
  display: inline-block;
}
</style>
