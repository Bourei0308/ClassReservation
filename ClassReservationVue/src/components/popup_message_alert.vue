<template>
  <transition name="fade-slide" appear>
    <div class="modal-overlay" v-if="show">
      <div class="modal-content">
        <div :class="['icon-circle', isSuccess ? 'success' : 'error']">
          <svg v-if="isSuccess" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" class="icon-check">
            <polyline points="20 6 9 17 4 12" />
          </svg>
          <svg v-else xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" class="icon-cross">
            <line x1="18" y1="6" x2="6" y2="18"/>
            <line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </div>
        <p class="message">{{ message }}</p>
        <button class="menu-button" @click="$emit('close')">閉じる</button>
      </div>
    </div>
  </transition>
</template>


<script setup>
const props = defineProps({
  show: Boolean,
  isSuccess: Boolean,
  message: String
});
const emit = defineEmits(['close']);
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(0,0,0,0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}
.modal-content {
  background: white;
  padding: 30px 40px 40px;
  border-radius: 16px;
  width: 320px;
  text-align: center;
  box-shadow: 0 10px 30px rgba(0,0,0,0.25);
}
.icon-circle {
  margin: 0 auto 20px;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
}
.icon-circle.success {
  background-color: #2d2d69;
}
.icon-circle.error {
  background-color: #e74c3c;
  border: 5px solid #e74c3c;
}
.icon-check, .icon-cross {
  width: 60px;
  height: 60px;
}
.message {
  font-size: 1.2rem;
  color: #333;
  margin-bottom: 28px;
  min-height: 48px;
}
.menu-button {
  display: block;
  width: 100%;
  margin: 0 auto;
  padding: 14px;
  font-size: 1.1rem;
  font-weight: bold;
  color: white;
  background-color: #2d2d69eb;
  border: none;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
  cursor: pointer;
}
.menu-button:hover {
  background-color: #0056b3;
  transform: scale(1.02);
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-30px);
}
.fade-slide-enter-to,
.fade-slide-leave-from {
  opacity: 1;
  transform: translateY(0);
}
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

</style>
