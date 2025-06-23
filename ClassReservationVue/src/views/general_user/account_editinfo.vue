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
        <input class="input-box" v-model="editedName" />
      </div>
      <div class="divider"></div>

      <div class="info-row">
        <div class="label">■メール</div>
        <input class="input-box" v-model="editedEmail" />
      </div>
      <div class="divider"></div>

      <div class="password-row">
        <button class="password-button" @click="submitChanges">変更</button>
        <button class="password-button" @click="cancel">キャンセル</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { useAuth } from '@/scripts/useAuth'

const router = useRouter()
const { user } = useAuth()

// ローカル編集用
const editedName = ref(user.value.name)
const editedEmail = ref(user.value.email)

// 更新処理
const submitChanges = async () => {
  try {
    const updatedUser = {
      ...user.value,
      name: editedName.value,
      email: editedEmail.value,
    }

    const res = await axios.put(`/api/users/${user.value.id}`, updatedUser)

    // 更新成功: ローカル状態更新 & ページ遷移
    user.value.name = editedName.value
    user.value.email = editedEmail.value
    sessionStorage.setItem('user', JSON.stringify(user.value))

    alert('更新が完了しました')
    router.push('/account')
  } catch (e) {
    if (e.response?.data === 'name-exists') {
      alert('同じユーザ名が既に存在します')
    } else {
      alert('更新に失敗しました')
      console.error(e)
    }
  }
}

const cancel = () => {
  router.push('/account')
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

.input-box {
  flex: 2;
  padding: 6px;
  border: 1px solid #ccc;
  border-radius: 4px;
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
