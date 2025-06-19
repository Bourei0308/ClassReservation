<template>
  <div class="login-container">
    <div class="login-box">
      <h1>ログイン</h1>
      <div class="input-group">
        <label for="account">アカウント</label>
        <input type="text" id="account" v-model="account" placeholder="例）ほげほげ">
      </div>
      <div class="input-group">
        <label for="password">パスワード</label>
        <input type="password" id="password" v-model="password" placeholder="例）Password123">
      </div>
      <button @click="doLogin()" class="login-button">ログイン</button>
      <router-link :to="{name:'NewAccountrView'}" class="new-account-link">新規ユーザー登録画面へ</router-link>
      <router-link :to="{name:'NewAccountrView'}" class="new-account-link">新規ユーザー登録画面へ</router-link>
      <router-link :to="{name:'NewAccountrView'}" class="new-account-link">新規ユーザー登録画面へ</router-link>
      <router-link :to="{name:'NewAccountrView'}" class="new-account-link">新規ユーザー登録画面へ</router-link>
    </div>
  </div>
</template>


<style scoped>
/* ページ全体のセンタリングと背景 */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh; /* ビューポートの高さ全体を使う */
  background-color: #181818; /* 薄いグレーの背景 */
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
  max-width: 400px; /* ボックスの最大幅 */
}

.login-box h1 {
  font-size: 2.2em;
  color: #333;
  margin-bottom: 30px;
}

/* 入力グループのスタイル */
.input-group {
  margin-bottom: 20px;
  text-align: left; /* ラベルと入力フィールドを左揃え */
}

.input-group label {
  display: block; /* ラベルを独立したブロックにする */
  font-size: 0.9em;
  color: #555;
  margin-bottom: 8px;
  font-weight: bold;
}

.input-group input {
  width: calc(100% - 20px); /* パディング分を考慮 */
  padding: 12px 10px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 1em;
  box-sizing: border-box; /* paddingとborderをwidthに含める */
}

.input-group input:focus {
  border-color: #007bff; /* フォーカス時のボーダー色 */
  outline: none; /* デフォルトのアウトラインを消す */
  box-shadow: 0 0 5px rgba(0, 123, 255, 0.2);
}

/* ログインボタンのスタイル */
.login-button {
  width: 100%;
  padding: 12px 20px;
  background-color: #007bff; /* 青色のボタン */
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 1.1em;
  cursor: pointer;
  transition: background-color 0.3s ease;
  margin-top: 10px;
}

.login-button:hover {
  background-color: #0056b3; /* ホバー時の色 */
}

/* 新規ユーザー登録リンクのスタイル */
.new-account-link {
  display: block; /* リンクをブロック要素にして中央揃え可能にする */
  margin-top: 25px;
  font-size: 0.9em;
  color: #007bff;
  text-decoration: none; /* 下線を削除 */
}

.new-account-link:hover {
  text-decoration: underline; /* ホバー時に下線を表示 */
}
</style>