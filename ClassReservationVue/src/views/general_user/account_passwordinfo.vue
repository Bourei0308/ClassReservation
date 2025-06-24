
<template>
  <div class="verify-wrapper">
    <h2>メールに届いた認証コードを入力してください</h2>
    <input v-model="code" placeholder="6桁のコード" maxlength="6" />
    <button @click="verifyCode">認証する</button>
    <p v-if="error" class="error">{{ error }}</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const code = ref('')
const error = ref('')
const router = useRouter()

const verifyCode = async () => {
  const res = await fetch('/api/auth/verify-code', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ code: code.value })
  })

  if (res.ok) {
    router.push('/account/newpassword')  // 認証成功で遷移
  } else {
    error.value = '認証コードが一致しません'
  }
}
</script>