<template>
  <div class="user-info-wrapper">
    <div class="user-info-box">
      <div class="info-row">
        <div class="label">■アカウント</div>
        <div class="value">{{ user.account }}</div>
      </div>
      <div class="divider"></div>

      <div class="info-row">
        <div class="label">■ユーザ名</div>
        <div class="value">{{ user.name }}</div>
        <button class="edit-button" @click="goToEdit">変更</button>
      </div>
      <div class="divider"></div>

      <div class="info-row">
        <div class="label">■メール</div>
        <div class="value">{{ user.email }}</div>
        <button class="edit-button" @click="goToEdit">変更</button>
      </div>
      <div class="divider"></div>

      <div class="password-row">
        <button class="password-button" @click="goToPasswordChange">パスワード変更</button>
        <button class="password-button" @click="logout">ログアウト</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useAuth } from '@/scripts/useAuth'
import { useRouter } from 'vue-router'

const { user,logout } = useAuth()
const router = useRouter()

const goToEdit = () => {
  router.push('/account/edit')
}

const goToPasswordChange = () => {
  router.push('/account/newpassword')
}
</script>

<style scoped>
.user-info-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  background-color: #f0f0f0;
}

.user-info-box {
  background-color: #fff;
  border: 1px solid #ccc;
  padding: 24px;
  width: 400px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

.info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
}

.label {
  font-weight: bold;
  color: #333;
  flex: 1;
}

.value {
  flex: 2;
  color: #555;
}

.edit-button {
  margin-left: 10px;
  padding: 4px 12px;
  background-color: #ddd;
  border: none;
  cursor: pointer;
}

.edit-button:hover {
  background-color: #ccc;
}

.divider {
  height: 1px;
  background-color: #999;
  margin: 4px 0;
}

.password-row {
  margin-top: 16px;
  text-align: right;
  display: flex;
  gap: 20px;
}

.password-button {
  padding: 6px 14px;
  background-color: #bbb;
  border: none;
  cursor: pointer;
}

.password-button:hover {
  background-color: #999;
}
</style>
