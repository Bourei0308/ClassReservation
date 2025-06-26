<template>
    <div>
        <Top_Calender account="teacher" :teacherID=userid ref="calendar" />
        <button class="open-button" @click="openPopup">
            今月の予約一覧
        </button>

        <SchedulePopup ref="popup"   @list-refreshed="handleListRefreshed" />
        <div class="timeband_section">
            <h2 class="center_title">今日の予定</h2>
            <TimeBand :blue_time="blueTimes" :hourStep="2" />
        </div>
    </div>
</template>

<script setup>
import { useRouter } from 'vue-router'; // Vue RouterのuseRouterフックをインポート
import { ref, onMounted } from 'vue'; // VueのrefをインポートClassReservationVue\src\components\top_calendar.vue
import Top_Calender from '@/components/top_calendar.vue'; // 子コンポーネントをインポート
const router = useRouter(); // useRouterフックを使用してルーターインスタンスを取得
import SchedulePopup from '@/components/popup_reservation.vue'
const popup = ref(null)
import { useAuth } from '@/scripts/useAuth'
const { restoreLogin, user } = useAuth()
const userid = user.value.id

import axios from 'axios'
import moment from 'moment'
import TimeBand from '@/components/comp_timeband.vue'
const blueTimes = ref([])

onMounted(() => {
  fetchTodayTeacherSchedules()
})



async function fetchTodayTeacherSchedules() {
  const { user } = useAuth()
  if (!user.value?.id) {
    console.error('user id is not available')
    return
  }

  try {
    // 获取老师所有课程
    const res = await axios.get(`/api/class-schedules/teacher/${user.value.id}`)
    const schedules = res.data

    // 过滤出“今天”的课程
    const today = moment().startOf('day')
    const tomorrow = moment(today).add(1, 'day')

    const todaySchedules = schedules.filter(s => {
      const start = moment(s.startTime)
      return start.isSameOrAfter(today) && start.isBefore(tomorrow)
    })

    // 转成你需要的格式 [ [开始, 结束], ... ]
    blueTimes.value = todaySchedules.map(s => [
      moment(s.startTime).format('YYYY-MM-DD HH:mm'),
      moment(s.endTime).format('YYYY-MM-DD HH:mm')
    ])

  } catch (error) {
    console.error('Failed to fetch schedules:', error)
  }
}

function openPopup() {
  popup.value.open()
}

const calendar = ref(null)
const handleListRefreshed = async () => {
  // 直接调用子组件的 fetchHours 方法
  if (calendar.value?.onChange) {
    await calendar.value.onChange()
  }
}


</script>

<style scoped>
.timeband_section {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    margin: 50px 0;
}

.open-button {
    background-color: #3b82f6;
    /* 蓝色 */
    color: white;
    padding: 8px 16px;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-size: 14px;
    transition: background-color 0.2s;
}

.open-button:hover {
    background-color: #2563eb;
    /* hover 深蓝 */
}
</style>