<template>
  <div class="user-info-container">
    <!-- 左側：背景犬画像 -->
    <div class="left-panel"></div>

    <!-- 右側：编辑用户信息内容 -->
    <div class="right-panel">
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

    <AlertModal v-bind="alertProps" @close="closeAlert" />
    <ConfirmDialog :show="confirmShow" :message="confirmMessage" @confirm="onConfirm" @cancel="onCancel" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { useAuth } from '@/scripts/useAuth'

// 🔸 alert
import AlertModal from '@/components/popup_message_alert.vue';
import ConfirmDialog from '@/components/popup_message_confirm.vue';
import { useModalManager } from '@/scripts/useModalManager';
const {
  showAlert, closeAlert, alertProps,
  confirmShow, confirmMessage, openConfirm, onConfirm, onCancel
} = useModalManager();


const router = useRouter()
const { user } = useAuth()

// ローカル編集用
const editedName = ref(user.value.name)
const editedEmail = ref(user.value.email)

const getConfirmMessage = () => {
  return `
■アカウント: ${user.value.account}
■ユーザ名: ${editedName.value}
■メール: ${editedEmail.value}

これらの内容で更新してよろしいですか？
`
}

// 更新処理
const submitChanges = () => {
  // 打开确认弹窗，确认后执行异步更新
  openConfirm(getConfirmMessage(), async () => {
    try {
      const updatedUser = {
        ...user.value,
        name: editedName.value,
        email: editedEmail.value,
      }

      await axios.put(`/api/users/${user.value.id}`, updatedUser)

      user.value.name = editedName.value
      user.value.email = editedEmail.value
      sessionStorage.setItem('user', JSON.stringify(user.value))

      showAlert('更新が完了しました！', true)

      setTimeout(() => {
        router.push('/account')
      }, 1200)
    } catch (e) {
      if (e.response?.data === 'name-exists') {
        showAlert('同じユーザ名が既に存在します', false)
      } else {
        showAlert('更新に失敗しました', false)
        console.error(e)
      }
    }
  })
}

const cancel = () => {
  router.push('/account')
}
</script>

<style scoped>
.user-info-container {
  display: flex;
  height: 100vh;
  font-family: Arial, sans-serif;
  background-color: #f7cd4a; /* 登录页背景色 */
}

/* 左側：背景犬画像 */
.left-panel {
  flex: 1;
  background-image: url('@/assets/img/2.png'); /* 确认路径 */
  background-repeat: no-repeat;
  background-position: center center;
  background-size: contain;
  background-color: #f7cd4a;
}

/* 右側：白色卡片 */
.right-panel {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}
.user-info-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 2rem;
  background: #f5f7fa;
  min-height: 100vh;
  box-sizing: border-box;
  font-family: Arial, sans-serif;
}

.user-info-box {
  background-color: white;
  padding: 2rem;
  border-radius: 16px;
  box-shadow: 0 8px 20px rgba(45, 45, 105, 0.2);
  width: 100%;
  max-width: 600px;
  color: #2d2d69;
  box-sizing: border-box;
}

.info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 1rem;
  margin-bottom: 1.2rem;
}

.label {
  font-weight: 700;
  font-size: 1rem;
  color: #2d2d69;
  min-width: 120px;
}

.value {
  flex: 1;
  font-size: 1rem;
  color: #333;
  word-break: break-word;
}

.edit-button {
  background-color: #2d2d69;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 0.5rem 1rem;
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.edit-button:hover {
  background-color: #1e1e4f;
}

.divider {
  height: 1px;
  background-color: #e0e0e0;
  margin-bottom: 1.2rem;
}

/* 密码区按钮排版 */
.password-row {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 1rem;
  margin-top: 2rem;
}

.password-button {
  flex: 1;
  background-color: #2d2d69;
  color: white;
  border: none;
  border-radius: 10px;
  padding: 0.6rem 1.2rem;
  font-weight: 700;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.3s ease;
  min-width: 140px;
}

.password-button:hover {
  background-color: #1e1e4f;
}

.input-box {
  flex: 1;
  padding: 0.5rem 0.75rem;
  border: 1px solid #ccc;
  border-radius: 8px;
  font-size: 1rem;
  color: #2d2d69;
  background-color: #fff;
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
  outline: none;
  min-width: 200px;
}

.input-box:focus {
  border-color: #2d2d69;
  box-shadow: 0 0 0 2px rgba(45, 45, 105, 0.2);
}
</style>
