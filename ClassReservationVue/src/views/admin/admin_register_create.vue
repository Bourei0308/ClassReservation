<template>
  <div class="container">
    <h2>アカウント作成（管理者）</h2>

    <form @submit.prevent="goToConfirm">
      <label>氏名:</label>
      <input type="text" v-model="form.name" required />

      <label>メールアドレス:</label>
      <input type="email" v-model="form.email" required />

      <label>パスワード:</label>
      <input type="password" v-model="form.password" required />

      <label>アカウント名:</label>
      <input type="text" v-model="form.account" required />

      <label>ロール:</label>
      <div class="radio-group">
        <label>
          <input type="radio" value="0" v-model="form.role" required />
          管理者
        </label>
        <label>
          <input type="radio" value="2" v-model="form.role" required />
          先生
        </label>
        <label>
          <input type="radio" value="1" v-model="form.role" />
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
        role: "",
        account: ""
      }
    };
  },
  methods: {
    goToConfirm() {
      // セッションストレージに保存（確認画面へ渡す）
      sessionStorage.setItem("registerForm", JSON.stringify(this.form));
      this.$router.push("/admin/register/confirm");
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
input[type="text"],
input[type="email"],
input[type="password"] {
  width: 100%;
  padding: 8px;
  margin-top: 5px;
}
.radio-group {
  margin-top: 10px;
}
button {
  margin-top: 20px;
  padding: 10px 20px;
}
</style>
