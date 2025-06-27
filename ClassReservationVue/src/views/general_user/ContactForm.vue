<template>
    <div class="contact-form">
        <h2>お問い合わせフォーム</h2>
        <form @submit.prevent="submitForm">
            <div class="form-group">
                <label for="name">お名前</label>
                <input type="text" id="name" v-model="form.name" required />
            </div>
            <div class="form-group">
                <label for="email">メールアドレス</label>
                <input type="email" id="email" v-model="form.email" required />
            </div>
            <div class="form-group">
                <label for="message">お問い合わせ内容</label>
                <textarea id="message" v-model="form.message" required></textarea>
            </div>
            <button type="submit" :disabled="sending">送信</button>
            <p v-if="submitted" class="success-message">送信が完了しました。ありがとうございます！</p>
            <p v-if="errorMsg" class="error-message">{{ errorMsg }}</p>
        </form>
    </div>
</template>

<script setup>
import { ref } from 'vue';
import { sendContactFormMail } from '@/scripts/emailSender';

const form = ref({
  name: '',
  email: '',
  message: ''
});
const submitted = ref(false);
const sending = ref(false);
const errorMsg = ref('');

async function submitForm() {
  sending.value = true;
  errorMsg.value = '';
  try {
    await sendContactFormMail({
      name: form.value.name,
      email: form.value.email,
      message: form.value.message
    });
    submitted.value = true;
    form.value = { name: '', email: '', message: '' };
  } catch (e) {
    errorMsg.value = '送信中にエラーが発生しました。時間をおいて再度お試しください。';
  } finally {
    sending.value = false;
  }
}
</script>

<style scoped>
.contact-form {
    max-width: 400px;
    margin: 40px auto;
    padding: 24px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.form-group {
    margin-bottom: 16px;
}

label {
    display: block;
    margin-bottom: 6px;
    font-weight: bold;
}

input,
textarea {
    width: 100%;
    padding: 8px;
    border: 1px solid #ccc;
    border-radius: 4px;
    font-size: 1em;
    resize: vertical; /* 縦方向のみリサイズ可、横は不可 */
}

button {
    background: #007bff;
    color: #fff;
    border: none;
    padding: 10px 20px;
    border-radius: 4px;
    cursor: pointer;
    font-size: 1em;
}

button:hover {
    background: #0056b3;
}

.success-message {
    color: #28a745;
    margin-top: 12px;
}

.error-message {
    color: #dc3545;
    margin-top: 12px;
}
</style>
