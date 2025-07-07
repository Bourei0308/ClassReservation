<template>
  <div class="admin-container">
    <div class="content-box">
      <h1>管理者トップページ</h1>
      <router-link to="/admin/register/create" class="menu-button">アカウント作成</router-link>
      <router-link to="/admin/accountlist" class="menu-button">アカウント一覧</router-link>
      <router-link to="/admin/charge" class="menu-button">授業時間チャージ</router-link>
      <router-link to="/admin/lesson" class="menu-button">授業管理</router-link>
      <router-link to="/admin/lesson-add" class="menu-button">授業入力</router-link>
      <router-link to="/admin/user-add" class="menu-button">ユーザ入力</router-link>
    </div>
  </div>
</template>


<script setup>
</script>

<style scoped>
.admin-container {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  height: 100vh;
  background-image: url('@/assets/img/2.png'); /* 犬画像 */
  background-repeat: no-repeat;
  background-position: -150px center; /* ← さらに左へずらす */
  background-size: auto 85%; /* 少し小さくして調整 */
  background-color: #f7cd4a; /* 背景の黄色 */
  padding-right: 140px; /* 右にゆとり */
  font-family: 'Arial', sans-serif;
}

.right-menu {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.menu-box {
  margin-left: auto;
  padding: 40px 32px;
  background-color: rgba(255, 255, 255, 0.85);
  border-radius: 20px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
  text-align: center;
  min-width: 320px;
}

.menu-box h1 {
  font-size: 2.4rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 30px;
  border-bottom: 2px solid #007bff;
  padding-bottom: 10px;
}

.menu-button {
  display: block;
  width: 100%;
  margin: 16px 0;
  padding: 14px;
  font-size: 1.1rem;
  font-weight: bold;
  color: white;
  background-color: #2d2d69eb;
  border: none;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
  cursor: pointer;
}

.menu-button:hover {
  background-color: #0056b3;
  transform: scale(1.02);
}
</style>
