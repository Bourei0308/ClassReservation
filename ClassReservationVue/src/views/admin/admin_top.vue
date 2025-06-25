<template>
  <div>
    <h1>管理者トップページ</h1>
    <div class="menu">
      <router-link to="/admin/register/create">アカウント作成</router-link>
      <router-link to="/admin/accountlist">アカウント一覧</router-link>
      <router-link to="/admin/charge">授業時間チャージ</router-link>
      <router-link to="/notice">お知らせ管理</router-link>
      <router-link to="/admin/lesson">授業管理</router-link>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AdminTopPage'
}
</script>

<style scoped>
.menu {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-top: 20px;
}
.menu a {
  display: inline-block;
  padding: 10px 20px;
  background-color: #007BFF;
  color: white;
  text-decoration: none;
  border-radius: 8px;
  width: fit-content;
}
.menu a:hover {
  background-color: #0056b3;
}
</style>