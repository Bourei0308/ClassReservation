<template>
  <div class="login-container">
    <!-- 左側：背景犬画像 -->
    <div class="left-panel"></div>

    <!-- 右側：フォームエリア -->
    <div class="right-panel">
      <div class="login-box" :class="{ shake: loginFailed }">
        <h1>ログイン</h1>

        <div class="input-group" :class="{ 'has-error': accountError }">
          <label for="account">アカウント</label>
          <input type="text" id="account" v-model="account" placeholder="例）ほげほげ">
          <p v-if="accountError" class="error-message">{{ accountError }}</p>
        </div>

        <div class="input-group" :class="{ 'has-error': passwordError }">
          <label for="password">パスワード</label>
          <input type="password" id="password" v-model="password" placeholder="例）Password123">
          <p v-if="passwordError" class="error-message">{{ passwordError }}</p>
        </div>

        <button
          :class="['login-button', { loading: isLoading }]"
          @click="handleLogin"
        >
          ログイン
        </button>

        <button @click="router.push('/account/reset-request')" class="forgot-password-button">
          パスワードをお忘れの方はこちら
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useAuth } from '@/scripts/useAuth';
import { useRouter } from 'vue-router';

const { user, login, devLoginMockUser, TEST_MODE } = useAuth();
const router = useRouter();

const account = ref('');
const password = ref('');
const accountError = ref('');
const passwordError = ref('');
const loginFailed = ref(false);
const isLoading = ref(false);

const handleLogin = async () => {
  accountError.value = '';
  passwordError.value = '';
  let isValid = true;

  if (!account.value.trim()) {
    accountError.value = 'アカウント名を入力してください。';
    isValid = false;
  }
  if (!password.value.trim()) {
    passwordError.value = 'パスワードを入力してください。';
    isValid = false;
  }

  if (!isValid) return;

  isLoading.value = true;
  try {
    await login(account.value, password.value);
  } catch (e) {
    loginFailed.value = true;
    setTimeout(() => (loginFailed.value = false), 500);
  }
  isLoading.value = false;
};
</script>

<style scoped>
.login-container {
  display: flex;
  height: 100vh;
  background-image: url('@/assets/img/2.png');
  background-repeat: no-repeat;
  background-position: -180px center;
  background-size: auto 85%;
  background-color: #f7cd4a;
  font-family: 'Arial', sans-serif;
}

.left-panel {
  flex: 1;
}

.right-panel {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-box {
  background-color: rgba(255, 255, 255, 0.95);
  padding: 60px 50px;
  border-radius: 20px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
  text-align: center;
  width: 550px;
  animation: fadeInUp 0.8s ease-out;
}

.login-box.shake {
  animation: shake 0.4s ease;
}

.login-box h1 {
  font-size: 2.8rem;
  font-weight: 800;
  color: #333;
  margin-bottom: 40px;
  border-bottom: 3px solid #2b2b69eb;
  padding-bottom: 12px;
}

.input-group {
  margin-bottom: 25px;
  text-align: left;
}

.input-group label {
  display: block;
  font-size: 1.1em;
  color: #555;
  margin-bottom: 10px;
  font-weight: bold;
}

.input-group input {
  width: 100%;
  padding: 14px;
  font-size: 1.1em;
  border: 1px solid #ccc;
  border-radius: 6px;
}

.input-group input:focus {
  border-color: #2d2d69eb;
  outline: none;
  box-shadow: 0 0 6px rgba(0, 123, 255, 0.3);
}

.login-button {
  width: 100%;
  padding: 16px;
  font-size: 1.8em;
  background-color: #2d2d69eb;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  margin-top: 25px;
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.2);
  position: relative;
}

.login-button.loading::after {
  content: "";
  position: absolute;
  top: 50%;
  right: 20px;
  width: 16px;
  height: 16px;
  margin-top: -8px;
  border: 2px solid #fff;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spinner 0.7s linear infinite;
}

.forgot-password-button {
  margin-top: 18px;
  font-size: 1.5em;
  background: none;
  border: none;
  color: #ef6673;
  cursor: pointer;
  text-decoration: underline;
}

.forgot-password-button:hover {
  color: #0056b3;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(40px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25%, 75% { transform: translateX(-10px); }
  50% { transform: translateX(10px); }
}

@keyframes spinner {
  to {
    transform: rotate(360deg);
  }
}
</style>