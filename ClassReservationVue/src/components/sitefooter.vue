<template>
  <link href="https://fonts.googleapis.com/css2?family=Pacifico&display=swap" rel="stylesheet">
  <footer class="site_footer">
    <div class="center">
      <div class="catchphrase">
        学びをもっと楽しく、happyに。
      </div>
    </div>



    <div class="dawn">
      <span>@じゅくぽん運営</span>
    </div>

    <div class="footer_links">
      <a href="/disclaimer" target="_blank">免責事項</a>
      <span class="divider">|</span>
      <RouterLink to="/contact">お問い合わせ</RouterLink>
    </div>

  </footer>
</template>

<script setup>
import { useRouter, RouterLink } from 'vue-router'
</script>

<style>
  .site_footer {
    background-color: #2d2d69eb;
    background-image: radial-gradient(white 1px, transparent 1px);
    background-size: 40px 40px;
    color: white;
    font-size: 12px;
    text-align: center;
    padding: 32px 0 24px;
    position: relative;
    overflow: hidden;
  }

  /* キラキラのアニメーション */
  .site_footer::before {
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    width: 200%;
    height: 200%;
    background-image: radial-gradient(white 1px, transparent 1px);
    background-size: 60px 60px;
    opacity: 0.2;
    animation: sparkleMove 20s linear infinite;
    z-index: 0;
  }

  @keyframes sparkleMove {
    from {
      transform: translate(0, 0);
    }

    to {
      transform: translate(-30px, -30px);
    }
  }

  /* 本文やテキストは上に表示 */
  .site_footer>* {
    position: relative;
    z-index: 1;
  }


  .center {
    text-align: center;
    margin-top: 10px;
  }

  .catchphrase {
    font-family: 'Hiragino Kaku Gothic ProN', 'Yu Gothic', sans-serif;
    font-size: 18px;
    color: #ffffff;
    margin-bottom: -100px;
    display: inline-block;
    transform: rotate(-1deg) skewY(-2deg) translateY(-4px);
    background: linear-gradient(to right, #ffffff, #8cdce1);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    font-weight: bold;
    animation: riseUp 1.2s ease-in-out;
  }

  @keyframes riseUp {
    from {
      opacity: 0;
      transform: translateY(10px) scale(0.95);
    }

    to {
      opacity: 1;
      transform: translateY(0) scale(1);
    }
  }

  .dawn {
    margin-top: 30px;
  }


  .footer_links {
    margin-top: 20px;
    font-size: 11px;
    color: #ccc;
  }

  .footer_links a {
    color: #ccc;
    text-decoration: none;
    margin: 0 8px;
    transition: color 0.3s ease;
  }

  .footer_links a:hover {
    color: #ffffff;
    text-decoration: none;
  }

  .footer_links .divider {
    color: #888;
  }

  .footer-image {
    position: absolute;
    bottom: 10px;
    width: 300px;
    /* サイズを大きく（例: 80px → 100px） */
    height: auto;
    z-index: 1;
    opacity: 0.8;
  }

  .footer-image.left {
    left: 100px;
    /* 左端からより内側に（例: 20px → 40px） */
  }

  .footer-image.right {
    right: 100px;
    /* 右端からより内側に（例: 20px → 40px） */
  }
</style>