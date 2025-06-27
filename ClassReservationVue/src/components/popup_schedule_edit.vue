<template>
    <div v-if="show" class="modal-overlay">
        <div class="modal-content">
            <h3>授業編集</h3>
            <div>
                <label>開始時間</label>
                <input type="time" v-model="localStartTime" />
            </div>
            <div>
                <label>終了時間</label>
                <input type="time" v-model="localEndTime" />
            </div>

            <div class="modal-buttons">
                <button class="save-button" @click="save">保存</button>
                <button class="cancel-button" @click="$emit('close')">キャンセル</button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import axios from 'axios'

const props = defineProps({
    show: Boolean,
    startTime: String,
    endTime: String,
    lesson: Object, // lesson 里包含 date 字段，比如 "2025-06-27"
})

const emit = defineEmits(['close', 'updated'])

const localStartTime = ref('')
const localEndTime = ref('')

// 监听父组件传来的时间
watch(
    () => props.startTime,
    (newVal) => {
        localStartTime.value = newVal
    },
    { immediate: true }
)

watch(
    () => props.endTime,
    (newVal) => {
        localEndTime.value = newVal
    },
    { immediate: true }
)

const save = async () => {
    try {
        const date = props.lesson.date // 获取日期，比如 "2025-06-27"

        const startDateTime = `${date}T${localStartTime.value}`
        const endDateTime = `${date}T${localEndTime.value}`

        await axios.put(`/api/class-schedules/${props.lesson.id}`, {
            ...props.lesson,
            startTime: startDateTime,
            endTime: endDateTime,
        })

        alert('更新成功')
        emit('updated')
        emit('close')
    } catch (error) {
        alert('更新失敗: ' + error.message)
    }
}
</script>


<style scoped>
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
}

.modal-content {
    background: white;
    padding: 20px;
    border-radius: 8px;
}

.modal-buttons {
    margin-top: 20px;
    display: flex;
    justify-content: space-between;
}

.save-button {
    background-color: #3b82f6;
    color: white;
    border: none;
    padding: 8px 16px;
    border-radius: 6px;
    cursor: pointer;
}

.cancel-button {
    background-color: #ef4444;
    color: white;
    border: none;
    padding: 8px 16px;
    border-radius: 6px;
    cursor: pointer;
}
</style>
