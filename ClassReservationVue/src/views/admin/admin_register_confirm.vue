<template>
  <div class="container">
    <h2>アカウント作成確認</h2>

    <div class="field"><strong>氏名:</strong> {{ form.name }}</div>
    <div class="field"><strong>メール:</strong> {{ form.email }}</div>
    <div class="field"><strong>パスワード:</strong> {{ form.password }}</div>
    <div class="field">
      <strong>ロール:</strong> {{ form.role === '0' ? '管理者' : form.role === '1' ? '生徒' : '先生' }}
    </div>
    <div class="field"><strong>アカウント名:</strong> {{ form.account }}</div>

    <div class="buttons">
      <button @click="goBack">戻る</button>
      <button @click="registerUser">登録する</button>
    </div>
  </div>
</template>

<script>
export default {
  name: "AdminRegisterConfirm",
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
  mounted() {
    const saved = sessionStorage.getItem("registerForm");
    if (saved) {
      this.form = JSON.parse(saved);
    } else {
      alert("入力情報が見つかりません。入力画面へ戻ります。");
      this.$router.push("/admin/register/create");
    }
  },
  methods: {
    goBack() {
      this.$router.push("/admin/register/create");
    },
    registerUser() {
      fetch("http://localhost:8080/api/users/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(this.form)
      })
        .then(async (res) => {
          if (!res.ok) {
            const text = await res.text();
            if (text === "account-exists") throw new Error("アカウントがすでに存在しています。");
            if (text === "name-exists") throw new Error("名前がすでに使用されています。");
            throw new Error("登録に失敗しました。");
          }
          return res.json();
        })
        .then(() => {
          alert("登録が完了しました！");
          sessionStorage.removeItem("registerForm");
          this.$router.push("/admin/accountlist"); // 一覧ページに遷移
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
.field {
  margin-bottom: 10px;
}
.buttons {
  margin-top: 20px;
}
button {
  padding: 10px 20px;
  margin-right: 10px;
}
</style>
