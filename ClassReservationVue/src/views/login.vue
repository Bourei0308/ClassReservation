<template>
  <div class="login-container">
    <div class="login-box">
      <h1>ログイン</h1>
      <div class="input-group" :class="{ 'has-error': accountError }">
        <label for="account">アカウント</label>
        <input type="text" id="account" v-model="account" placeholder="例）ほげほげ">
        <p v-if="accountError" class="error-message">{{ accountError }}</p>
      </div>
      <div class="input-group" :class="{ 'has-error': passwordError }">
        <label for="password">パスワード</label>
        <input type="password" id="password" v-model="password" placeholder="例）Password123">
        <p v-if="passwordError" class="error-message">{{ passwordError }}</p>
      </div>
      <button @click="doLogin()" class="login-button primary-login-button">ログイン</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
// restoreLogin, useAuth はこのスニペットでは使用されていないため、明確化のために削除しています。
// 実際のログインロジックで必要であれば、インポートしてください。

const account = ref('');
const password = ref('');
const accountError = ref('');
const passwordError = ref('');

const doLogin = () => {
  // 以前のエラーをリセット
  accountError.value = '';
  passwordError.value = '';

  let isValid = true;

  if (!account.value.trim()) {
    accountError.value = 'アカウント名を入力してください。';
    isValid = false;
  }

  if (!password.value.trim()) {
    passwordError.value = 'パスワードを入力してください。';
    isValid = false;
  }

  if (isValid) {
    console.log('ログイン試行:', account.value, password.value);
    // ここに実際のログインロジックを実装します
    // 例: API呼び出しなど
  } else {
    console.log('ログイン失敗: 入力エラー');
  }
};

onMounted(() => {
  // 初期表示でデフォルトのログインタイプを'1'（生徒）に設定
  // これは元のコードのコメントのままで、ここにロジックは実装されていません。
});
</script>

<style scoped>
/* ページ全体のセンタリングと背景 */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #181818;
  font-family: 'Arial', sans-serif;
}

/* ログインボックスのスタイル */
.login-box {
  background-color: #ffffff;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  text-align: center;
  width: 100%;
  max-width: 400px;
}

.login-box h1 {
  font-size: 2.2em;
  color: #333;
  margin-bottom: 30px;
}

/* 入力グループのスタイル */
.input-group {
  margin-bottom: 20px;
  text-align: left;
}

.input-group label {
  display: block;
  font-size: 0.9em;
  color: #555;
  margin-bottom: 8px;
  font-weight: bold;
}

.input-group input {
  width: calc(100% - 20px);
  padding: 12px 10px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 1em;
  box-sizing: border-box;
}

.input-group input:focus {
  border-color: #007bff;
  outline: none;
  box-shadow: 0 0 5px rgba(0, 123, 255, 0.2);
}

/* エラーメッセージのスタイル */
.error-message {
  color: #dc3545; /* エラー用の赤色 */
  font-size: 0.85em;
  margin-top: 5px;
  text-align: left;
}

/* エラー時の入力フィールドのボーダー */
.input-group.has-error input {
  border-color: #dc3545; /* エラーフィールド用の赤いボーダー */
}


/* ログインボタンの基本スタイル */
.login-button {
  width: 100%;
  padding: 12px 20px;
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 1.1em;
  cursor: pointer;
  transition: background-color 0.3s ease, box-shadow 0.3s ease;
  margin-top: 20px; /* スペースを広げるためマージンを増やす */
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2); /* 控えめな影 */
}

/* メインログインボタンのスタイル (新しいデフォルト) */
.primary-login-button {
  background-color: #007bff; /* 目立つ青色 */
}

.primary-login-button:hover {
  background-color: #0056b3; /* ホバーでより濃い青色 */
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3); /* ホバーで影を強調 */
}

.primary-login-button:active {
  background-color: #004085; /* クリック時にさらに濃く */
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.2); /* クリック時に内側の影 */
}


/* 既存の特定のログインボタンのスタイル (他の場所で使用する場合に保持) */
/* 生徒ログインボタンの色 (デフォルト) */
.login-button.student-login-button {
  background-color: #007bff; /* 青色 */
}
.login-button.student-login-button:hover {
  background-color: #0056b3; /* 濃い青色 */
}

/* 先生ログインボタンの色 */
.login-button.teacher-login-button {
  background-color: #28a745; /* 緑色 */
}
.login-button.teacher-login-button:hover {
  background-color: #1e7e34; /* 濃い緑色 */
}

/* 管理者ログインボタンの色 */
.login-button.admin-login-button {
  background-color: #dc3545; /* 赤色 */
}
.login-button.admin-login-button:hover {
  background-color: #bd2130; /* 濃い赤色 */
}


/* 新規ユーザー登録リンクのコンテナ */
.login-type-links {
  margin-top: 25px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

/* 新規ユーザー登録ボタン（リンクのように見せる）のスタイル */
.new-account-link {
  background: none;
  border: none;
  padding: 0;
  font-size: 0.9em;
  color: #007bff;
  text-decoration: none;
  cursor: pointer;
  transition: text-decoration 0.3s ease;
  display: block;
  margin: 0 auto;
}

.new-account-link:hover {
  text-decoration: underline;
}
</style>