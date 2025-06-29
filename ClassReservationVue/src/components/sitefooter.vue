<template>
  <footer class="site_footer">
    @じゅくぽん運営
    <br>
    amemiya・butusaka・mizushima・riku・you
    <br>
    <RouterLink to="/contact">お問い合わせ</RouterLink>
  </footer>
</template>

<script setup>
import { useRouter, RouterLink } from 'vue-router'
// 无需脚本
</script>

<style>
.site_footer {
  background-color: #2d2d69eb;
  color: white;
  font-size: 12px;
  text-align: center;
  padding: 24px 0;
  z-index: 1000;
}
</style>
