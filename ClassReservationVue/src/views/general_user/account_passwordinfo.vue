<template>
  <div class="wrapper">
    <h2 v-if="!verified">メールに届いた認証コードを入力してください</h2>
    <h2 v-else>新しいパスワードを入力してください</h2>

    <!-- 認証コード入力 -->
    <div v-if="!verified">
      <input v-model="code" placeholder="6桁のコード" maxlength="6" />
      <button @click="verifyCode">認証する</button>
    </div>

    <!-- パスワード変更フォーム -->
    <div v-if="verified">
      <input v-model="newPassword" type="password" placeholder="新しいパスワード" />
      <button @click="changePassword">パスワードを変更</button>
    </div>

    <!-- メッセージ -->
    <p v-if="error" class="error">{{ error }}</p>
    <p v-if="success" class="success">変更が完了しました。ログイン画面に戻ります。</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { sendPasswordChangedMail } from '@/scripts/emailSender'
import { useAuth } from '@/scripts/useAuth'

const { logout } = useAuth()
const router = useRouter()

// 認証ステート
const code = ref('')
const verified = ref(false)
const error = ref('')
const success = ref(false)

// パスワード変更ステート
const newPassword = ref('')
const id = ref('')
const email = ref('')
const name = ref('')

// 認証コードの確認
const verifyCode = async () => {
  error.value = ''
  const res = await fetch('/api/auth/verify-code', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ code: code.value })
  })

  if (res.ok) {
    verified.value = true
    await loadUserInfo()
  } else {
    error.value = '認証コードが一致しません'
  }
}

// ログイン中のユーザー情報を取得
const loadUserInfo = async () => {
  try {
    const res = await fetch('/api/auth/me')
    if (res.ok) {
      const data = await res.json()
      id.value = data.id
      email.value = data.email
      name.value = data.name
    } else {
      error.value = 'ユーザー情報の取得に失敗しました。再ログインしてください。'
    }
  } catch (e) {
    error.value = '通信エラーが発生しました。'
  }
}

// パスワードを変更する
const changePassword = async () => {
  error.value = ''
  if (!newPassword.value) {
    error.value = '新しいパスワードを入力してください'
    return
  }

  const res = await fetch('/api/auth/change-password', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      email: email.value,
      name: name.value,
      newPassword: newPassword.value
    })
  })

  if (res.ok) {
    success.value = true
    sendPasswordChangedMail(id.value)
    logout()
    setTimeout(() => router.push('/'), 3000)
  } else {
    const message = await res.text()
    error.value = message || 'パスワードの変更に失敗しました'
  }
}
</script>

<style scoped>
.wrapper {
  max-width: 400px;
  margin: 4rem auto;
  padding: 2rem 1.5rem;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 20px rgba(45, 45, 105, 0.15);
  text-align: center;
  
  color: #2d2d69;
  box-sizing: border-box;
}

input {
  margin: 1rem 0 1.5rem;
  padding: 0.6rem 0.75rem;
  width: 100%;
  max-width: 360px;
  border: 1.5px solid #ccc;
  border-radius: 10px;
  font-size: 1rem;
  color: #2d2d69;
  outline: none;
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
  box-sizing: border-box;
}

input:focus {
  border-color: #2d2d69;
  box-shadow: 0 0 0 3px rgba(45, 45, 105, 0.2);
}

button {
  background-color: #2d2d69;
  color: white;
  border: none;
  border-radius: 12px;
  padding: 0.7rem 2.5rem;
  font-weight: 700;
  font-size: 1rem;
  cursor: pointer;
  margin-top: 0.8rem;
  transition: background-color 0.3s ease;
  min-width: 140px;
}

button:hover {
  background-color: #1e1e4f;
}

.error {
  margin-top: 1rem;
  color: #d9534f; /* 更统一的红色 */
  font-weight: 600;
}

.success {
  margin-top: 1rem;
  color: #28a745; /* 更统一的绿色 */
  font-weight: 600;
}

</style>
