<template>
  <transition name="fade-slide" appear>
    <div v-if="show" class="modal-overlay">
      <div class="modal-content">
        <p style="white-space: pre-line;">{{ message }}</p>
        <button class="menu-button" @click="$emit('confirm')">{{ t('popup_confirm.confirm') }}</button>
        <button class="menu-button cancel-btn" @click="$emit('cancel')">{{ t('popup_confirm.cancel') }}</button>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
const { t } = useI18n()

const props = defineProps({
  show: Boolean,
  message: String,
});

const emit = defineEmits(['confirm', 'cancel']);
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
.menu-button {
  display: block;
  width: 100%;
  margin: 12px auto 0;
  padding: 14px;
  font-size: 1.1rem;
  font-weight: bold;
  color: white;
  background-color: #2d2d69eb;
  border: none;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  cursor: pointer;
  transition: all 0.3s ease;
}
.menu-button:hover {
  background-color: #0056b3;
  transform: scale(1.02);
}
.cancel-btn {
  background-color: #ccc;
  color: #333;
}
.cancel-btn:hover {
  background-color: #aaa;
  transform: scale(1.02);
}

/* 这里用上面给你的渐隐滑动效果 */
.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}
</style>
