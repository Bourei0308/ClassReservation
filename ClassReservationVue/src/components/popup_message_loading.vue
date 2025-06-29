<!-- 加载弹窗示例组件 LoadingModal.vue -->
<template>
  <div v-if="show" class="loading-overlay">
    <div class="loading-spinner"></div>
    <p>読み込み中...</p>
  </div>
</template>
<script setup>
import { defineProps } from 'vue'
const props = defineProps({ show: Boolean })
</script>
<style scoped>
.loading-overlay {
  position: fixed;
  top:0; left:0; right:0; bottom:0;
  background: rgba(0,0,0,0.3);
  display:flex;
  justify-content:center;
  align-items:center;
  flex-direction: column;
  z-index: 9999;
}
.loading-spinner {
  border: 5px solid #ccc;
  border-top-color: #2d2d69;
  border-radius: 50%;
  width: 50px;
  height: 50px;
  animation: spin 1s linear infinite;
  margin-bottom: 10px;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
