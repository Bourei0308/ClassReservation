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
    <AlertModal v-bind="alertProps" @close="closeAlert" />
    <ConfirmDialog :show="confirmShow" :message="confirmMessage" @confirm="onConfirm" @cancel="onCancel" />

</template>

<script setup>
import { ref, watch } from 'vue'
import axios from 'axios'

// 🔸 alert
import AlertModal from '@/components/popup_message_alert.vue';
import ConfirmDialog from '@/components/popup_message_confirm.vue';
import { useModalManager } from '@/scripts/useModalManager';
const {
    showAlert, closeAlert, alertProps,
    confirmShow, confirmMessage, openConfirm, onConfirm, onCancel
} = useModalManager();

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

const save = () => {
    const date = props.lesson.date;
    const startDateTime = `${date}T${localStartTime.value}`;
    const endDateTime = `${date}T${localEndTime.value}`;

    const msg = `この履歴の時間帯を ${localStartTime.value}～${localEndTime.value} に修正します。チェックしてください。`;

    openConfirm(msg, async () => {
        try {
            await axios.put(`/api/class-schedules/${props.lesson.id}`, {
                ...props.lesson,
                startTime: startDateTime,
                endTime: endDateTime,
            });

            showAlert('履歴を更新しました！', true); // 成功提示
            emit('updated');
            emit('close');
        } catch (error) {
            showAlert(`更新失敗: ${error.message}`, false); // 失败提示
        }
    });
};
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
    padding: 1.5rem;
    border-radius: 12px;
    width: 90%;
    max-width: 400px;
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
    
    color: #2d2d69;
}

.modal-content h3 {
    text-align: center;
    margin-bottom: 1.2rem;
    color: #2d2d69;
    font-size: 1.2rem;
    font-weight: bold;
}

.modal-content div {
    margin-bottom: 1rem;
    display: flex;
    flex-direction: column;
    gap: 0.4rem;
}

label {
    font-weight: 600;
    font-size: 0.95rem;
    color: #2d2d69;
}

input[type="time"] {
    padding: 0.4rem 0.6rem;
    border-radius: 6px;
    border: 1px solid #ccc;
    font-size: 1rem;
    color: #2d2d69;
}

.modal-buttons {
    display: flex;
    justify-content: center;
    gap: 1.5rem;
    margin-top: 1.5rem;
}

.save-button,
.cancel-button {
    padding: 0.5rem 1.2rem;
    border: none;
    border-radius: 8px;
    font-weight: bold;
    font-size: 0.95rem;
    cursor: pointer;
    transition: background-color 0.3s ease;
}

.save-button {
    background-color: #2d2d69;
    color: white;
}

.save-button:hover {
    background-color: #1e1e4f;
}

.cancel-button {
    background-color: #e0e0e0;
    color: #2d2d69;
}

.cancel-button:hover {
    background-color: #c5c5c5;
}

@media screen and (max-width: 500px) {
    .modal-content {
        width: 95%;
        padding: 1rem;
    }

    .modal-buttons {
        flex-direction: column;
        gap: 0.8rem;
    }

    .save-button,
    .cancel-button {
        width: 100%;
    }
}
</style>
