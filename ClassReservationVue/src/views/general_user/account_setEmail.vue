<template>
  <div class="wrapper">
    <h2>メールアドレスを設定してください</h2>

    <input
      v-model="email"
      type="email"
      placeholder="メールアドレスを入力してください"
      @input="clearError"
      :disabled="codeSent"
    />

    <div v-if="!codeSent">
      <button @click="sendVerificationCode" :disabled="loading">
        {{ loading ? '送信中...' : '認証コードを送信' }}
      </button>
    </div>

    <div v-else>
      <input
        v-model="verificationCode"
        type="text"
        maxlength="6"
        placeholder="認証コードを入力してください"
        @input="clearError"
      />
      <button @click="verifyCode" :disabled="loading">
        {{ loading ? '検証中...' : '認証コードを確認' }}
      </button>
    </div>

    <p v-if="emailError" class="error">{{ emailError }}</p>
    <p v-if="success" class="success">
      メールアドレスの設定が完了しました。トップページに移動します。
    </p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { useAuth } from '@/scripts/useAuth'

const router = useRouter()
const { user } = useAuth()

const email = ref(user.value?.email || '')
const verificationCode = ref('')
const emailError = ref('')
const success = ref(false)
const loading = ref(false)
const codeSent = ref(false)

// 简单邮箱格式校验
const validateEmail = (emailStr) => {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return re.test(emailStr)
}

const clearError = () => {
  emailError.value = ''
}

const sendVerificationCode = async () => {
  if (!email.value.trim()) {
    emailError.value = 'メールアドレスを入力してください。'
    return
  }
  if (!validateEmail(email.value)) {
    emailError.value = '有効なメールアドレスを入力してください。'
    return
  }

  loading.value = true
  emailError.value = ''

  try {
    await axios.post('/api/auth/send-code', { email: email.value })
    codeSent.value = true
  } catch (err) {
    emailError.value = '認証コードの送信に失敗しました。再度お試しください。'
    console.error(err)
  } finally {
    loading.value = false
  }
}

const verifyCode = async () => {
  if (!verificationCode.value.trim()) {
    emailError.value = '認証コードを入力してください。'
    return
  }
  if (!/^\d{6}$/.test(verificationCode.value)) {
    emailError.value = '6桁の認証コードを入力してください。'
    return
  }

  loading.value = true
  emailError.value = ''

  try {
    // 認証コード検証
    await axios.post('/api/auth/verify-code', { code: verificationCode.value })

    // 認証成功後更新メールアドレス
    await axios.put(`/api/users/${user.value.id}/email`, { email: email.value })

    // 更新全局 user 对象
    user.value.email = email.value
    success.value = true

    setTimeout(() => {
      router.push(`/top/${user.value.role}`)
    }, 1500)
  } catch (err) {
    if (err.response?.data) {
      emailError.value = err.response.data
    } else {
      emailError.value = '認証コードの確認に失敗しました。再度お試しください。'
    }
    console.error(err)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.wrapper {
  max-width: 400px;
  margin: 30px auto;
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 12px;
  background-color: #fafafa;
  font-family: 'Segoe UI', sans-serif;
  text-align: center;
}

input[type="email"],
input[type="text"] {
  width: 100%;
  padding: 8px 12px;
  margin: 12px 0;
  border: 1.5px solid #2d2d69;
  border-radius: 8px;
  font-size: 16px;
  box-sizing: border-box;
}

button {
  background-color: #2d2d69;
  color: white;
  padding: 12px 24px;
  font-weight: 700;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s ease;
  user-select: none;
}

button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

button:hover:not(:disabled) {
  background-color: #0056b3;
}

.error {
  color: #cc3333;
  margin-top: 12px;
}

.success {
  color: #007b33;
  margin-top: 12px;
  font-weight: 700;
}
</style>
