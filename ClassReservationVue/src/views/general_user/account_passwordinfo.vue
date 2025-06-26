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
  margin: 40px auto;
  text-align: center;
}
input {
  margin: 10px 0;
  padding: 8px;
  width: 80%;
}
button {
  padding: 10px 20px;
  margin-top: 10px;
}
.error {
  color: red;
}
.success {
  color: green;
}
</style>
