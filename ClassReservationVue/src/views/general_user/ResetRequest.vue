<template>
  <div class="wrapper">
    <h2 v-if="step === 1">登録済みのメールアドレスを入力してください</h2>
    <h2 v-if="step === 2">メールに届いた認証コードを入力してください</h2>
    <h2 v-if="step === 3">新しいパスワードを入力してください</h2>

    <!-- ステップ1: メール入力 -->
    <div v-if="step === 1">
      <input v-model="email" placeholder="例）test@example.com" />
      <button @click="sendCode">認証コードを送信</button>
    </div>

    <!-- ステップ2: 認証コード入力 -->
    <div v-if="step === 2">
      <input v-model="code" placeholder="6桁のコード" maxlength="6" />
      <button @click="verifyCode">認証する</button>
    </div>

    <!-- ステップ3: パスワード入力 -->
    <div v-if="step === 3">
      <input v-model="newPassword" type="password" placeholder="新しいパスワード" />
      <button @click="changePassword">パスワードを変更</button>
    </div>

    <!-- メッセージ -->
    <p v-if="error" class="error">{{ error }}</p>
    <p v-if="success" class="success">変更が完了しました。ログイン画面に戻ります。</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { sendPasswordChangedMail } from '@/scripts/emailSender'
import { useAuth } from '@/scripts/useAuth'

const { logout } = useAuth()
const router = useRouter()

const step = ref(1)
const email = ref('')
const code = ref('')
const newPassword = ref('')
const error = ref('')
const success = ref(false)
const id = ref('')
const name = ref('')

// ステップ1: 認証コード送信
const sendCode = async () => {
  error.value = ''
  if (!email.value.trim()) {
    error.value = 'メールアドレスを入力してください'
    return
  }
  try {
    const res = await fetch('/api/auth/send-code', {
      method: 'POST',
      credentials: 'include', // ✅ 追加済み
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: email.value })
    })
    if (!res.ok) throw new Error('送信に失敗しました')
    step.value = 2
  } catch (err) {
    error.value = err.message
  }
}

// ステップ2: 認証コード検証
const verifyCode = async () => {
  error.value = ''
  try {
    const res = await fetch('/api/auth/verify-code', {
      method: 'POST',
      credentials: 'include', // ✅ 追加済み
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ code: code.value })
    })
    if (!res.ok) throw new Error('認証コードが一致しません')

    // ユーザー情報の取得（emailから取得）
    const userRes = await fetch(`/api/users/email/${encodeURIComponent(email.value)}`, {
      credentials: 'include' // ✅ ここにも追加！
    })
    if (!userRes.ok) throw new Error('ユーザー情報の取得に失敗しました')

    const data = await userRes.json()
    id.value = data.id
    name.value = data.name
    step.value = 3
  } catch (err) {
    error.value = err.message
  }
}

// ステップ3: パスワード変更
const changePassword = async () => {
  error.value = ''
  if (!newPassword.value) {
    error.value = '新しいパスワードを入力してください'
    return
  }
  try {
    const res = await fetch('/api/auth/change-password', {
      method: 'POST',
      credentials: 'include', // ✅ ここにも追加！
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: email.value, name: name.value, newPassword: newPassword.value })
    })
    if (!res.ok) throw new Error(await res.text())

    success.value = true
    sendPasswordChangedMail(id.value)
    logout()
    setTimeout(() => router.push('/'), 3000)
  } catch (err) {
    error.value = err.message || 'パスワードの変更に失敗しました'
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
