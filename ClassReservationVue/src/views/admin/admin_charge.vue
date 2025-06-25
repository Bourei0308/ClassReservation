<template>
  <div class="container">
    <h2>学生授業時間チャージ</h2>

    <div class="search-bar">
      <input type="text" v-model="searchKeyword" placeholder="Search" />
      <i class="fas fa-search search-icon"></i>
    </div>

    <label>生徒を選択:</label>
    <select v-model="selectedUserId" @change="updateCurrentHours">
      <option disabled value="">選択してください</option>
      <option v-for="user in filteredStudents" :key="user.id" :value="user.id">
        {{ user.name }}（{{ user.account }}）
      </option>
    </select>

    <p v-if="currentHours !== null" class="current-hours">
      現在のコマ数：{{ currentHours }}
    </p>

    <label>チャージ時間（h）:</label>
    <input
      type="number"
      v-model.number="chargeAmount"
      min="0"
      step="0.5"
      required
    />

    <button @click="charge">チャージする</button>
  </div>
</template>

<script>
export default {
  name: "AdminCharge",
  data() {
    return {
      students: [],
      selectedUserId: "",
      chargeAmount: 0,
      searchKeyword: "",
      currentHours: null
    };
  },
  computed: {
    filteredStudents() {
      const keyword = this.searchKeyword.toLowerCase();
      return this.students.filter(u =>
        u.name.toLowerCase().includes(keyword) || u.account.toLowerCase().includes(keyword)
      );
    }
  },
  mounted() {
    fetch("http://localhost:8080/api/users")
      .then(res => res.json())
      .then(data => {
        this.students = data.filter(u => u.role === 1);
      })
      .catch(err => {
        alert("ユーザー一覧取得失敗: " + err.message);
      });
  },
  methods: {
    updateCurrentHours() {
      const user = this.students.find(u => u.id === this.selectedUserId);
      this.currentHours = user?.remainingHours || 0; // サーバーのuserに `remainingHours` がある前提
    },
    charge() {
      if (!this.selectedUserId || this.chargeAmount <= 0) {
        alert("生徒とチャージ時間を正しく入力してください。");
        return;
      }
      fetch(`http://localhost:8080/api/charges/users/${this.selectedUserId}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ chargeHours: this.chargeAmount })
      })
        .then(res => {
          if (!res.ok) throw new Error("チャージ失敗");
          return res.text();
        })
        .then(() => {
          alert("チャージが完了しました！");
          this.chargeAmount = 0;
          this.updateCurrentHours(); // 最新値に更新
          this.$router.push("/top/0");
        })
        .catch(err => {
          alert("エラー: " + err.message);
        });
    }
  }
};

</script>
<style scoped>
.container {
  max-width: 400px;
  margin: 30px auto;
  padding: 20px;
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

select, input[type="number"] {
  width: 100%;
  padding: 10px;
  margin-top: 5px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 14px;
}

input[type="number"]::-webkit-inner-spin-button,
input[type="number"]::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

button {
  margin-top: 20px;
  padding: 10px 20px;
  font-size: 14px;
  background-color: #a9d3b8;
  color: #000;
  border: 1px solid #aaa;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

button:hover {
  background-color: #88c3a9;
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

</style>






