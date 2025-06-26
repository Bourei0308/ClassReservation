<template>
  <div class="password-reset-wrapper">
    <h2>新しいパスワードを入力してください</h2>
    <input v-model="newPassword" type="password" placeholder="新しいパスワード" />
    <button @click="changePassword">パスワードを変更</button>
    <p v-if="error" class="error">{{ error }}</p>
    <p v-if="success" class="success">変更が完了しました。ログイン画面に戻ります。</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

import { sendPasswordChangedMail } from '@/scripts/emailSender' // ✅ 追加

import { useAuth } from '@/scripts/useAuth'
const { logout  } = useAuth()

const router = useRouter()

const id = ref('')
const email = ref('')
const name = ref('')
const newPassword = ref('')
const error = ref('')
const success = ref(false)

// ✅ ログイン済みユーザーの情報を取得
onMounted(async () => {
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
})

const changePassword = async () => {
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
    // ✅ パスワード変更完了メール送信
    sendPasswordChangedMail(id.value)
    logout()
    setTimeout(() => router.push('/'), 3000)
  } else {
    const message = await res.text()
    error.value = message || 'パスワードの変更に失敗しました'
  }
}

</script>
