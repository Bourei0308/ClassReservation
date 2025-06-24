<template>
    <div>
        <Top_Calender account="teacher" :teacherID=userid />
        <Top_ChatBox />

        <div>
            <TimeBand :blue_time="blueTimes" />
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