<template>
  <div class="modal-overlay" v-if="show">
    <div class="modal-content">
      <h3>{{ title || 'チャージ履歴一覧' }}</h3>

      <div class="history-list">
        <div
          class="history-item"
          v-for="item in histories"
          :key="item.id"
        >
          <div>
            <strong>生徒ID:</strong> {{ item.studentId }}
          </div>
          <div>
            <strong>チャージ:</strong> {{ item.chargeHours }} 時間
          </div>
          <div>
            <strong>日時:</strong> {{ formatDate(item.createdAt) }}
          </div>
          <button class="delete-button" @click="deleteHistory(item.id)">
            削除
          </button>
        </div>
      </div>

      <button @click="$emit('close')">閉じる</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import axios from 'axios'

const props = defineProps({
  show: Boolean,
  student: Object,  // nullの場合は全体
  title: String
})
const emit = defineEmits(['close'])

const histories = ref([])

// 日付フォーマット
const formatDate = (dateStr) => {
  const d = new Date(dateStr)
  return d.toLocaleString('ja-JP')
}

// 履歴取得
const loadHistories = async () => {
  try {
    if (props.student) {
        console.log(res)
      const res = await axios.get(`/api/charges/users/${props.student.id}`)
      histories.value = res.data.sort(
        (a, b) => new Date(b.createdAt) - new Date(a.createdAt)
      )
    } else {
        console.log(res)
      const res = await axios.get('/api/charges')
      histories.value = res.data.sort(
        (a, b) => new Date(b.createdAt) - new Date(a.createdAt)
      )
    }
  } catch (err) {
    alert('チャージ履歴の取得に失敗: ' + err.message)
  }
}

// 削除
const deleteHistory = async (id) => {
  if (!confirm('本当に削除しますか？')) return

  try {
    await axios.delete(`/api/charges/${id}`)
    histories.value = histories.value.filter((item) => item.id !== id)
    alert('削除しました')
  } catch (err) {
    alert('削除に失敗: ' + err.message)
  }
}

// show または student が変わったら再取得
watch(
  () => props.show,
  (newVal) => {
    if (newVal) {
      loadHistories()
    }
  }
)

watch(
  () => props.student,
  () => {
    if (props.show) {
      loadHistories()
    }
  }
)
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
  width: 600px;
  max-height: 80vh;
  overflow-y: auto;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin: 10px 0;
}

.history-item {
  background: #f9f9f9;
  padding: 10px 15px;
  border-radius: 8px;
  border: 1px solid #ccc;
  position: relative;
}

.delete-button {
  position: absolute;
  right: 10px;
  top: 10px;
  background: #e74c3c;
  color: white;
  border: none;
  padding: 5px 8px;
  border-radius: 6px;
  cursor: pointer;
}

.delete-button:hover {
  background: #c0392b;
}
</style>
