<template>
  <div class="container">
    <h2>学生授業時間チャージ</h2>
    <label>生徒を選択:</label>
    <select v-model="selectedUserId">
      <option disabled value="">選択してください</option>
      <option v-for="user in students" :key="user.id" :value="user.id">
        {{ user.name }}（{{ user.account }}）
      </option>
    </select>
    <label>チャージ時間（分）:</label>
    <input type="number" v-model.number="chargeAmount" min="1" required />
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
      chargeAmount: 0
    };
  },
  mounted() {
    fetch("http://localhost:8080/api/users")
      .then(res => res.json())
      .then(data => {
        this.students = data.filter(u => u.role === 1); // 生徒だけ表示
      })
      .catch(err => {
        alert("ユーザー一覧取得失敗: " + err.message);
      });
  },
  methods: {
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
  max-width: 600px;
  margin: 30px auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}
label {
  display: block;
  margin-top: 15px;
}
select, input {
  width: 100%;
  padding: 8px;
  margin-top: 5px;
}
button {
  margin-top: 20px;
  padding: 10px 20px;
}
</style>






