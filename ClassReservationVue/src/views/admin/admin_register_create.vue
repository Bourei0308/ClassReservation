<template>
  <body>
  <div class="modal-overlay">
    <div class="modal-content">
      <h2 class="form-title">アカウント作成（管理者）</h2>

      <form @submit.prevent="goToRegister" class="form-body">
        <label>氏名:</label>
        <input type="text" v-model="form.name" required />

        <label>メールアドレス:</label>
        <input type="email" v-model="form.email" required />

        <label>パスワード:</label>
        <input type="password" v-model="form.password" required />

        <label>アカウント名:</label>
        <input type="text" v-model="form.account" required />

        <label>ロール:</label>
        <div class="radio-button-group">
          <label v-for="option in roleOptions" :key="option.value"
            :class="['radio-button', { active: form.role === option.value }]">
            <input type="radio" :value="option.value" v-model="form.role" class="hidden-radio" />
            {{ option.label }}
          </label>
        </div>

        <button type="submit" class="submit-button">登録</button>
        <button type="button" class="submit-button" @click="goToAccountList">アカウント一覧</button>
      </form>
    </div>
  </div>
  <AlertModal v-bind="alertProps" @close="closeAlert" />
  <ConfirmDialog :show="confirmShow" :message="confirmMessage" @confirm="onConfirm" @cancel="onCancel" />
</body>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

import AlertModal from '@/components/popup_message_alert.vue';
import ConfirmDialog from '@/components/popup_message_confirm.vue';
import { useModalManager } from '@/scripts/useModalManager';
const {
  showAlert, closeAlert, alertProps,
  confirmShow, confirmMessage, openConfirm, onConfirm, onCancel
} = useModalManager();

const router = useRouter();
const roleOptions = [
  { value: 0, label: '管理者' },
  { value: 2, label: '先生' },
  { value: 1, label: '生徒' },
];

const form = ref({
  name: "",
  email: "",
  password: "",
  role: "",
  account: ""
});

const goToRegister = () => {
  const msg = `以下の情報でアカウントを作成します。\n\n` +
              `氏名: ${form.value.name}\n` +
              `メール: ${form.value.email}\n` +
              `パスワード: ${form.value.password}\n` +
              `アカウント名: ${form.value.account}\n` +
              `ロール: ${form.value.role === '0' ? '管理者' : form.value.role === '1' ? '生徒' : '先生'}\n\n` +
              `確認してください。`;

  openConfirm(msg, async () => {
    try {
      const res = await fetch("/api/users/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(form.value)
      });

      if (!res.ok) {
        const text = await res.text();
        if (text === "account-exists") throw new Error("アカウントがすでに存在しています。");
        if (text === "name-exists") throw new Error("名前がすでに使用されています。");
        throw new Error("登録に失敗しました。");
      }

      await res.json();
      showAlert("登録が完了しました！", true);
      form.value = {
        name: "", email: "", password: "", role: "", account: ""
      };
    } catch (err) {
      showAlert("エラー: " + err.message, false);
    }
  });
};

const goToAccountList = () => {
  router.push('/admin/accountlist');
};
</script>

<style scoped>
body {
  height: 100%;
  margin: 0;
  padding: 0;
  background-image: url('../../assets/img/4.png');
  background-repeat: repeat;
  background-size: 1000px auto;
  font-family: 'Segoe UI', sans-serif;
}

.modal-overlay {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding-top: 60px;
  background-color: rgba(255, 255, 255, 0.6);
}

.modal-content {
  background: white;
  padding: 30px 40px;
  border-radius: 16px;
  width: 600px;
  max-height: 85vh;
  overflow-y: auto;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
  animation: fadeInUp 0.8s ease-out;
}

.form-title {
  margin-bottom: 20px;
  font-size: 1.8rem;
  color: #2d2d69;
  text-align: center;
  font-weight: 700;
  border-bottom: 2px solid #2d2d69;
  padding-bottom: 10px;
}

.form-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-body label {
  font-weight: 600;
  color: #2d2d69;
}

.form-body input[type="text"],
.form-body input[type="email"],
.form-body input[type="password"] {
  padding: 12px;
  border: 1px solid #bbb;
  border-radius: 10px;
  font-size: 1rem;
  background-color: #fefefe;
  transition: border 0.2s ease;
}

.form-body input:focus {
  border-color: #2d2d69;
  outline: none;
}

.radio-button-group {
  display: flex;
  gap: 12px;
  margin-top: 6px;
}

.radio-button {
  background-color: #f0f0f0;
  color: #2d2d69;
  padding: 10px 18px;
  border-radius: 8px;
  cursor: pointer;
  user-select: none;
  font-weight: bold;
  transition: all 0.3s ease;
  border: 1px solid #ccc;
}

.radio-button:hover {
  background-color: #e0e0e0;
}

.radio-button.active {
  background-color: #2d2d69;
  color: white;
  border: 1px solid #2d2d69;
}

.hidden-radio {
  display: none;
}

.submit-button {
  margin-top: 20px;
  padding: 14px;
  font-size: 1.1rem;
  font-weight: bold;
  color: white;
  background-color: #2d2d69;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: background-color 0.3s ease, transform 0.2s ease;
}

.submit-button:hover {
  background-color: #1e1e50;
  transform: scale(1.03);
}

@keyframes fadeInUp {
  0% {
    opacity: 0;
    transform: translateY(40px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>


