<template>
  <div class="container">
    <h2>アカウント一覧</h2>
    <table>
      <thead>
        <tr>
          <th>ID</th>
          <th>アカウント</th>
          <th>氏名</th>
          <th>メール</th>
          <th>ロール</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="user in users" :key="user.id">
          <td>{{ user.id }}</td>
          <td>{{ user.account }}</td>
          <td>{{ user.name }}</td>
          <td>{{ user.email }}</td>
          <td>{{ convertRole(user.role) }}</td>
          <td>
            <button @click="deleteUser(user.id)">削除</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
<script>
export default {
  name: "AdminAccountList",
  data() {
    return {
      users: []
    };
  },
  mounted() {
    this.fetchUsers();
  },
  methods: {
    fetchUsers() {
      fetch("http://localhost:8080/api/users")
        .then(res => res.json())
        .then(data => {
          this.users = data;
        })
        .catch(err => {
          alert("ユーザー一覧の取得に失敗しました: " + err.message);
        });
    },
    convertRole(role) {
      switch (role) {
        case 0: return "管理者";
        case 1: return "生徒";
        case 2: return "先生";
        default: return "不明";
      }
    },
    deleteUser(userId) {
      if (!confirm("本当にこのアカウントを削除しますか？")) return;
      fetch(`http://localhost:8080/api/users/${userId}`, {
        method: "DELETE"
      })
        .then(res => {
          if (!res.ok) throw new Error("削除に失敗しました");
          return res.text();
        })
        .then(() => {
          alert("アカウントを削除しました。");
          // ユーザー一覧から即時削除
          this.users = this.users.filter(user => user.id !== userId);
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
  max-width: 900px;
  margin: 30px auto;
  font-family: Arial, sans-serif;
}
table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 20px;
}
th, td {
  padding: 12px;
  border: 1px solid #ccc;
  text-align: left;
}
thead {
  background-color: #F4F4F4;
}
button {
  padding: 5px 10px;
  color: white;
  background-color: #E74C3C;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}
button:hover {
  background-color: #C0392B;
}
</style>