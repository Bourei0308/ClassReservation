<template>
    <div>
        <Top_Calender account="student" :studentID=userid />
        <div class="student_section">
            <h2 class="center_title">今日の予定</h2>
            <TimeBand :blue_time="blueTimes" :hourStep="2" />
        </div>
    </div>
</template>

<script setup>
import { useRouter } from 'vue-router'; // Vue RouterのuseRouterフックをインポート
import { ref, onMounted } from 'vue'; // VueのrefをインポートClassReservationVue\src\components\top_calendar.vue
import Top_Calender from '@/components/top_calendar.vue'; // 子コンポーネントをインポート
import Top_ChatBox from '@/components/top_chatbox.vue'; // 子コンポーネントをインポート
const router = useRouter(); // useRouterフックを使用してルーターインスタンスを取得

import { useAuth } from '@/scripts/useAuth'
const { restoreLogin, user } = useAuth()
const userid = user.value.id

import TimeBand from '@/components/comp_timeband.vue'

const blueTimes = [
    ['2025-06-20 08:00', '2025-06-20 09:30'],
    ['2025-06-20 14:15', '2025-06-20 15:00']
]
</script>

<style scoped>
.student_section {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    margin: 50px 0;
}
</style>