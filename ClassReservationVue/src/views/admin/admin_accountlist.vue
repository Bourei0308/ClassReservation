<template>
  <div class="account-list-container">
    <h2>アカウント一覧</h2>

    <div class="account-box-container">
      <div v-for="user in users" :key="user.id" class="account-box" :class="'role-' + roleClass(user.role)">
        <div class="account-header">
          <div class="role-label" :class="'role-label-' + roleClass(user.role)">{{ user.name }}</div>
          <button class="delete-button" @click="deleteUser(user)">削除</button>
        </div>
        <div class="account-info">
          <div><strong>アカウント:</strong> {{ user.account }}</div>
          <div><strong>ロール:</strong> {{ convertRole(user.role) }}</div>
          <div><strong>メール:</strong> {{ user.email }}</div>
        </div>
      </div>
    </div>
  </div>
  <AlertModal v-bind="alertProps" @close="closeAlert" />
  <ConfirmDialog :show="confirmShow" :message="confirmMessage" @confirm="onConfirm" @cancel="onCancel" />
</template>
<script setup>
import { ref, onMounted } from 'vue';

// 🔸 alert
import AlertModal from '@/components/popup_message_alert.vue';
import ConfirmDialog from '@/components/popup_message_confirm.vue';
import { useModalManager } from '@/scripts/useModalManager';
const {
  showAlert, closeAlert, alertProps,
  confirmShow, confirmMessage, openConfirm, onConfirm, onCancel
} = useModalManager();

// ユーザー一覧
const users = ref([]);

const roleClass = (role) => {
  if (role === '0' || role === 0) return 'admin';
  if (role === '1' || role === 1) return 'student';
  if (role === '2' || role === 2) return 'teacher';
  return '';
};
// 🔸 役割を表示用に変換
const convertRole = (role) => {
  if (role === '0' || role === 0) return '管理者';
  if (role === '1' || role === 1) return '生徒';
  if (role === '2' || role === 2) return '先生';
  return '不明';
};

// 🔸 ユーザー一覧取得
const fetchUsers = async () => {
  try {
    const res = await fetch("/api/users");
    const data = await res.json();
    users.value = data;
  } catch (err) {
    alert("ユーザー一覧の取得に失敗しました: " + err.message);
  }
};

const deleteUser = async (user) => {
  // 拼接纯文本确认信息，换行显示
  const confirmMessage =
    `${user.name}\n` +
    `アカウント: ${user.account}\n` +
    `ロール: ${convertRole(user.role)}\n` +
    `メール: ${user.email}\n\n` +
    `本当にこのアカウントを削除しますか？`;

  openConfirm(confirmMessage, async () => {
    try {
      const res = await fetch(`/api/users/${user.id}`, {
        method: "DELETE",
      });

      if (!res.ok) throw new Error("削除に失敗しました");

      await res.text();
      showAlert("アカウントを削除しました。", true);

      users.value = users.value.filter((u) => u.id !== user.id);
    } catch (err) {
      showAlert("エラー: " + err.message, false);
    }
  });
};


// 🔸 初回マウント時に実行
onMounted(() => {
  fetchUsers();
});
</script>

<style scoped>
.container {
  max-width: 900px;
  margin: 30px auto;
  font-family: Arial, sans-serif;
}

.account-list-container {
  max-width: 1100px;
  margin: 30px auto;
  padding: 20px;
  background-color: #fff;
  border: 1px solid #ddd;
  border-radius: 12px;
  font-family: Arial, sans-serif;
}

.account-list-container h2 {
  text-align: center;
  margin-bottom: 20px;
}

.account-box-container {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  justify-content: flex-start;
}

.account-box {
  border-radius: 12px;
  padding: 15px;
  width: 320px;
  box-shadow: 2px 2px 8px rgba(0, 0, 0, 0.1);
  color: #222;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  border: 1px solid #ccc;
}

/* 角色色区分 */
.role-teacher {
  background-color: #e3f2fd;
  /* 浅蓝 */
  border-color: #2196f3;
  /* 蓝色边框 */
  color: #0d47a1;
}

.role-student {
  background-color: #e0ffe6;
  /* 浅绿 */
  border-color: #4caf50;
  /* 绿色边框 */
  color: #1b5e20;
}

.role-admin {
  background-color: #fff9c4;
  /* 浅黄 */
  border-color: #fbc02d;
  /* 黄色边框 */
  color: #f57f17;
}

.account-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.role-label {
  font-weight: bold;
  font-size: 14px;
  padding: 4px 10px;
  border-radius: 8px;
  user-select: none;
  text-align: center;
  min-width: 80px;
}

.role-label-teacher {
  background-color: #0d47a1;
  color: white;
}

.role-label-student {
  background-color: #1b5e20;
  color: white;
}

.role-label-admin {
  background-color: #f57f17;
  color: white;
}

/* 按钮 */
.delete-button {
  background-color: #f44336;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 6px 10px;
  font-size: 13px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.delete-button:hover {
  background-color: #d32f2f;
}

.account-info div {
  margin-bottom: 6px;
  font-size: 14px;
}
</style>