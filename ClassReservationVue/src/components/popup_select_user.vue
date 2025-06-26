<template>
  <div class="modal-overlay" v-if="show">
    <div class="modal-content">
      <h3>{{ title }}</h3>
      <div class="user-grid">
        <div 
          v-for="user in users" 
          :key="user.id" 
          class="user-item" 
          @click="selectUser(user)"
        >
          {{ user.name }}
        </div>
      </div>
      <button @click="$emit('close')">閉じる</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getUsers } from '@/scripts/chatUtils';

const props = defineProps({
  role: Number,    // 2: 先生, 3: 生徒
  show: Boolean,
  title: String
});
const emit = defineEmits(['select', 'close']);

const users = ref([]);

onMounted(async () => {
  const allUsers = await getUsers();
  users.value = allUsers.filter(u => u.role === props.role);
});

function selectUser(user) {
  emit('select', user);
  emit('close');
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(0,0,0,0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
}

.modal-content {
  background: white;
  padding: 20px;
  border-radius: 12px;
  width: 500px;
}

.user-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin: 10px 0;
}

.user-item {
  background: #eee;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  flex: 1 0 30%;
  text-align: center;
}

.user-item:hover {
  background: #ccc;
}
</style>
