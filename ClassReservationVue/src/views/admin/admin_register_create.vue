<template>
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


// 🔸 登録フォーム
const form = ref({
  name: "",
  email: "",
  password: "",
  role: "",
  account: ""
});

// 🔸 確認画面へ遷移
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
.modal-overlay {

  inset: 0;

  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
  margin-top: 100px;
}

.modal-content {
  background: white;
  padding: 30px 40px;
  border-radius: 12px;
  width: 600px;
  max-height: 85vh;
  overflow-y: auto;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.25);
}

.form-title {
  margin-bottom: 20px;
  font-size: 1.5rem;
  color: #2d2d69;
  text-align: center;
}

.form-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.form-body label {
  font-weight: bold;
  color: #333;
}

.form-body input[type="text"],
.form-body input[type="email"],
.form-body input[type="password"] {
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 8px;
  font-size: 1rem;
}

.radio-button-group {
  display: flex;
  gap: 12px;
  margin-top: 10px;
}

.radio-button {
  background-color: #eee;
  color: #333;
  padding: 10px 16px;
  border-radius: 8px;
  cursor: pointer;
  user-select: none;
  font-weight: bold;
  transition: all 0.3s ease;
  border: 1px solid #ccc;
}

.radio-button:hover {
  background-color: #ddd;
}

.radio-button.active {
  background-color: #2d2d69eb;
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
  background-color: #2d2d69eb;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.submit-button:hover {
  background-color: #0056b3;
  transform: scale(1.02);
}
</style>
