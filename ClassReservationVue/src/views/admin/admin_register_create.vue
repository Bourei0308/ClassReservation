<template>
  <div class="container">
    <h2>アカウント作成（管理者）</h2>
    <form @submit.prevent="submitForm">
      <label>氏名:</label>
      <input type="text" v-model="form.name" required />

      <label>メールアドレス:</label>
      <input type="email" v-model="form.email" required />

      <label>パスワード:</label>
      <input type="password" v-model="form.password" required />

      <label>ロール:</label>
      <div>
        <label>
          <input type="radio" value="teacher" v-model="form.role" required />
          先生
        </label>
        <label>
          <input type="radio" value="student" v-model="form.role" />
          生徒
        </label>
      </div>

      <button type="submit">確認画面へ</button>
    </form>
  </div>
</template>

<script>
export default {
  name: "AdminRegisterCreate",
  data() {
    return {
      form: {
        name: "",
        email: "",
        password: "",
        role: ""
      }
    };
  },
  methods: {
    submitForm() {
      // バリデーションチェック後、APIへPOST送信
      fetch("http://localhost:8080/api/admin/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(this.form)
      })
        .then(response => response.json())
        .then(data => {
          alert("アカウント確認画面へ進みます");
          // routerで次の画面へ遷移する場合
          this.$router.push("/admin/register/confirm");
        })
        .catch(error => {
          console.error("登録エラー:", error);
        });
    }
  }
};
</script>

<style scoped>
.container {
  max-width: 500px;
  margin: 30px auto;
}
input, button {
  display: block;
  margin-top: 10px;
  padding: 8px;
  width: 100%;
}
</style>
