<template>
  <header class="fixed top-0 left-0 w-full z-50 flex justify-between items-center px-6 py-4 bg-gray-800 text-white shadow-md">
    <!-- Logo / Title -->
    <div class="text-2xl font-semibold">
      ClassReservation
    </div>

    <!-- Right icons -->
    <div class="flex items-center space-x-4">
      <!-- Notification icon -->
      <router-link to="/notice" class="hover:text-blue-300">
        <BellIcon class="w-6 h-6" />
      </router-link>

      <!-- Chat icon -->
      <router-link to="/chat" class="hover:text-blue-300">
        <MessageCircleIcon class="w-6 h-6" />
      </router-link>

      <!-- マイページ button -->
      <router-link to="/account">
        <div class="bg-black text-white rounded-full px-4 py-1 text-sm hover:bg-gray-900">
          マイページ
        </div>
      </router-link>
    </div>
  </header>
</template>

<script setup>
import { BellIcon, MessageCircleIcon } from 'lucide-vue-next'
</script>

<style scoped>
/* 如果页面内容被 header 遮住，记得在 layout 或页面顶加上 padding-top */
</style>
