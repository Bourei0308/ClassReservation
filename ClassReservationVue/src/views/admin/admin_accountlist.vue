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
        </tr>
      </thead>
      <tbody>
        <tr v-for="user in users" :key="user.id">
          <td>{{ user.id }}</td>
          <td>{{ user.account }}</td>
          <td>{{ user.name }}</td>
          <td>{{ user.email }}</td>
          <td>{{ convertRole(user.role) }}</td>
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
    fetch("http://localhost:8080/api/users")
      .then(res => res.json())
      .then(data => {
        this.users = data;
      })
      .catch(err => {
        alert("ユーザー一覧の取得に失敗しました: " + err.message);
      });
  },
  methods: {
    convertRole(role) {
      switch (role) {
        case 0: return "管理者";
        case 1: return "生徒";
        case 2: return "先生";
        default: return "不明";
      }
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
</style>