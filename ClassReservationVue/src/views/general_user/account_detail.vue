<template>
  <div class="user-info-container">
    <!-- 左側：背景犬画像 -->
    <div class="left-panel"></div>

    <!-- 右側：用户信息内容 -->
    <div class="right-panel">
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
  </div>
  <LoadingModal :show="loadingShow" />
  <AlertModal v-bind="alertProps" @close="closeAlert" />

</template>

<script setup>
import { useAuth } from '@/scripts/useAuth'
import { useRouter } from 'vue-router'

// 🔸 alert
import AlertModal from '@/components/popup_message_alert.vue';
import LoadingModal from '@/components/popup_message_loading.vue';
import { useModalManager } from '@/scripts/useModalManager'
const {
  showAlert, closeAlert, alertProps,
  confirmShow, confirmMessage, openConfirm, onConfirm, onCancel, showLoading, closeLoading,loadingShow
} = useModalManager();

const { user, logout } = useAuth()
const router = useRouter()

const goToEdit = () => {
  router.push('/account/edit')
}

const goToPasswordChange = async () => {
  try {
    showLoading()
    // 認証コード送信API（6桁生成＋メール送信）
    await fetch('/api/auth/send-code', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: user.value.email })
    })

    // 認証コード入力画面へ
    router.push('/account/passwordinfo')
  } catch (err) {
    closeLoading()
    showAlert("認証メールの送信に失敗しました", false);
  }
}

</script>


<style scoped>
.user-info-container {
  display: flex;
  height: 100vh;
  font-family: Arial, sans-serif;
  background-color: #f7cd4a;
  /* 登录界面黄色背景 */
}

/* 左側：背景犬画像 */
.left-panel {
  flex: 1;
  background-image: url('@/assets/img/2.png');
  /* 请确认路径正确 */
  background-repeat: no-repeat;
  background-position: center center;
  background-size: contain;
  background-color: #f7cd4a;
}

/* 右側：用户信息容器 */
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

@media screen and (max-width: 768px) {
  /* 手机等窄屏时隐藏左侧背景图 */
  .left-panel {
    display: none;
  }
  
  /* 右侧部分宽度撑满 */
  .right-panel {
    flex: none;
    width: 100%;
    padding: 20px;
  }
  
  /* 调整用户信息盒子宽度适配手机 */
  .user-info-box {
    max-width: 100%;
    padding: 1.5rem;
    border-radius: 12px;
  }
}
</style>
