<template>
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
      <p v-if="currentHours !== null" class="current-hours">
        現在のコマ数：{{ currentHours }}
      </p>

      <input class="charge-input" type="number" v-model.number="chargeAmount" min="0" step="0.5" required />

      <button @click="charge">追加</button>
    </div>

    <!-- 📄 一覧表示 -->
    <h2 style="margin-top: 30px;">全生徒の現在のコマ数</h2>
    <div class="student-list">
      <div v-for="student in studentHoursList" :key="student.id" class="student-box">
        <span class="student-name">{{ student.name }}</span><span class="student-hour">残り <span
            class="student-hour-number">{{ student.hours }}</span> 時間</span><span>チャージ <span
            class="student-hour-number">{{ student.charge }}</span> 時間</span>
      </div>
    </div>

    <!-- 🪟 モーダル -->
    <UserSelectModal :show="showUserSelect" :role="1" title="生徒を選択" @select="handleUserSelect"
      @close="showUserSelect = false" />
  </div>
</template>


<script>
import axios from "axios";
import UserSelectModal from "@/components/popup_select_user.vue";

// ✅ data
const data = () => ({
  students: [],
  selectedUser: null,      // ← user对象
  chargeAmount: 0,
  currentHours: null,
  studentHoursList: [],
  showUserSelect: false    // ← 弹窗控制
});

// ✅ computed
const canCharge = function () {
  return this.selectedUser && this.chargeAmount > 0;
};

// ✅ methods

// 🔸 学生リスト
const loadStudents = async function () {
  try {
    const res = await axios.get("/api/users");
    this.students = res.data.filter((user) => user.role === 1);
  } catch (err) {
    alert("ユーザー一覧取得失敗: " + err.message);
  }
};

// 🔸 特定生徒の現在のコマ数
const loadCurrentHours = async function (userId) {
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
    console.error("現在のコマ数取得失敗", err);
    return "取得失敗";
  }
};

// 🔸 ユーザー選択時
const handleUserSelect = async function (user) {
  this.selectedUser = user;
  [this.currentHours, this.totalCharged] = await this.loadCurrentHours(user.id);
};

// 🔸 チャージ処理
const charge = async function () {
  if (!this.canCharge) {
    alert("生徒とチャージ時間を正しく入力してください。");
    return;
  }

  try {
    await axios.post(
      `/api/charges/users/${this.selectedUser.id}`,
      { chargeHours: this.chargeAmount }
    );

    alert("チャージが完了しました！");
    this.chargeAmount = 0;
    [this.currentHours, this.totalCharged] = await this.loadCurrentHours(this.selectedUser.id);
    await this.loadAllStudentHours();
  } catch (err) {
    alert("チャージ失敗: " + err.message);
  }
};

// 🔸 全生徒の現在のコマ数
const loadAllStudentHours = async function () {
  const results = await Promise.all(
    this.students.map(async (user) => {
      const [hours, charge] = await this.loadCurrentHours(user.id);
      return {
        id: user.id,
        name: user.name,
        hours, charge
      };
    })
  );
  this.studentHoursList = results;
};

// 🔸 初期化
const initialize = async function () {
  await this.loadStudents();
  await this.loadAllStudentHours();
};

const app = {
  name: "AdminCharge",
  components: { UserSelectModal },
  data,
  computed: { canCharge },
  mounted: initialize,
  methods: {
    loadStudents,
    loadCurrentHours,
    loadAllStudentHours,
    handleUserSelect,
    charge,
    initialize
  }
};

export default app;
</script>



<style scoped>
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
  font-family: "Arial", sans-serif;
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
  background-color: #aee3bf;
  color: #1b1b1b;
  border: 0px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

button:hover {
  background-color: #c5eceb;
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
