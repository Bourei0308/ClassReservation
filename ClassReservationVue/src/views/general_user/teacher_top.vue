<template>
  <div class="teacher_section">
    <!-- 左侧：狗图 -->
    <div class="left-dog-fixed"></div>

    <!-- 右侧内容 -->
    <div class="right-panel">
      <!-- 今日の予定 -->
      <section class="schedule-header">
        <h2 class="schedule-title">今日の予定</h2>
        <div class="timeband-wrapper">
          <template v-if="blueTimes.length > 0">
            <TimeBand :blue_time="blueTimes" :hour-step="hourStep" />
          </template>
          <template v-else>
            <div style="color: blue; font-weight: bold; text-align: center;">
              今日に授業がありません。
            </div>
          </template>
        </div>
      </section>

      <!-- 今月の予約一覧 -->
      <button class="fixed-circle-button" @click="openPopup">
        今月の予約一覧
      </button>

      <!-- カレンダーとポップアップ -->
      <Top_Calender account="teacher" :teacherID="userid" ref="calendar" />
      <SchedulePopup ref="popup" @list-refreshed="handleListRefreshed" />
    </div>
  </div>
</template>

<style scoped>
.left-dog-fixed {
  position: fixed;
  top: 0;
  left: 0;
  width: 600px;
  height: 100vh;
  background-image: url('@/assets/img/2.png');
  background-repeat: no-repeat;
  background-position: center center;
  background-size: contain;
  background-color: #f7cd4a;
  z-index: 0;
}

.teacher_section {
  margin-left: 400px;
  min-height: 100vh;
  
  background-color: #f7cd4a;
}

.right-panel {
  width: 90%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 30px;
  background: #f7cd4a;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: relative;
  z-index: 1;
}

.fixed-circle-button {
  position: fixed;
  right: 20px;
  top: 70%;
  transform: translateY(-50%);
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background-color: #2d2d69;
  color: white;
  font-weight: 700;
  font-size: 14px;
  border: none;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(45, 45, 105, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 10px;
  text-align: center;
  white-space: normal;
  z-index: 1000;
}

.fixed-circle-button:hover {
  background-color: #1e1e4f;
}

.schedule-header {
  background-color: #f4f8ff;
  border: 1px solid #e6e6e6;
  border-radius: 12px;
  padding: 20px 24px;
  box-shadow: 0 3px 12px rgba(45, 45, 105, 0.12);
  width: 100%;
  max-width: 900px;
  margin: 20px auto;
  box-sizing: border-box;
}

.schedule-title {
  font-weight: 800;
  font-size: 1.8rem;
  color: #2d2d69;
  margin: 0 0 16px 0;
  user-select: none;
  border-bottom: 3px solid #2d2d69eb;
  padding-bottom: 8px;
}

.timeband-wrapper {
  padding: 0 6px;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 手机端样式 */
@media (max-width: 768px) {
  .left-dog-fixed {
    display: none;
  }

  .teacher_section {
    margin-left: 0;
  }

  .right-panel {
    width: 100%;
    padding: 20px 16px;
    align-items: stretch;
  }

  .schedule-header {
    width: 100%;
    margin: 0 auto 20px;
  }

  .fixed-circle-button {
    right: 12px;
    bottom: 150px;
    top: auto;
    transform: none;
    width: 64px;
    height: 64px;
    font-size: 12px;
    padding: 8px;
  }
}

</style>

<script setup>
import { useRouter } from 'vue-router'; // Vue RouterのuseRouterフックをインポート
import { ref, onMounted,computed } from 'vue'; // VueのrefをインポートClassReservationVue\src\components\top_calendar.vue
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

const screenWidth = ref(window.innerWidth)
const updateWidth = () => {
  screenWidth.value = window.innerWidth
}
const hourStep = computed(() => {
  return screenWidth.value <= 768 ? 6 : 2
})

async function fetchTodayTeacherSchedules() {
  const { user } = useAuth()
  if (!user.value?.id) {
    console.error('user id is not available')
    return
  }

  try {
    // 获取老师所有课程（时间为 UTC0）
    const res = await axios.get(`/api/class-schedules/teacher/${user.value.id}`)
    const schedules = res.data

    // 获取本地“今天”的开始与结束
    const localToday = moment().startOf('day')
    const localTomorrow = moment(localToday).add(1, 'day')

    // 用 .utc() 再 .local() 将 UTC 转换为本地
    const todaySchedules = schedules.filter(s => {
      const localStart = moment.utc(s.startTime).local()
      return localStart.isSameOrAfter(localToday) && localStart.isBefore(localTomorrow)
    })

    // 转换为本地格式
    blueTimes.value = todaySchedules.map(s => {
      const startLocal = moment.utc(s.startTime).local().format('YYYY-MM-DD HH:mm')
      const endLocal = moment.utc(s.endTime).local().format('YYYY-MM-DD HH:mm')
      return [startLocal, endLocal]
    })

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

