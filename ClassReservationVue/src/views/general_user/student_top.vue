<template>
  <div class="student_section">
    <div class="left-dog-fixed"></div>
    <!-- 右侧：日程内容 -->
    <div class="right-panel">
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

      <button class="fixed-circle-button" @click="openPopup">
        今月の予約一覧
      </button>

      <SchedulePopup @list-refreshed="handleListRefreshed" ref="popup" />
      <TopStudentAvailableClass v-if="userid" :studentID="userid" ref="classStatus" />
      <Top_Calender v-if="userid" account="student" :studentID="userid"
        @reservation-refreshed="handleReservationDeleted" ref="calendar" />
    </div>
  </div>
</template>

<style scoped>
.left-dog-fixed {
  position: fixed;
  top: 0;
  left: 0;
  width: 600px;
  /* 狗图宽度 */
  height: 100vh;
  /* 高度撑满屏幕 */
  background-image: url('@/assets/img/2.png');
  background-repeat: no-repeat;
  background-position: center center;
  background-size: contain;
  background-color: #f7cd4a;
  /* 和整体背景保持一致 */
  z-index: 0;
  /* 保证在内容下方或你想要的层级 */
}

.student_section {
  margin-left: 400px;
  /* 留出狗图宽度，避免遮挡 */
  min-height: 100vh;
  font-family: Arial, sans-serif;
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
  /* ✅ 关键点：激活 z-index */
  z-index: 1;
}

.fixed-circle-button {
  position: fixed;
  right: 20px;
  /* 距离左边20px */
  top: 70%;
  /* 垂直居中 */
  transform: translateY(-50%);
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background-color: #2d2d69;
  /* 主题色 */
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
  /* 允许换行 */
  z-index: 1000;
}

.fixed-circle-button:hover {
  background-color: #1e1e4f;
}

.schedule-header {
  background-color: #f4f8ff;
  /* 淡蓝背景，与之前保持一致 */
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
  /* TimeBand 本身自带宽度，外围可留些内边距 */
  padding: 0 6px;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 手机端宽度调整 */
@media (max-width: 768px) {
  .left-dog-fixed {
    display: none;
    /* 手机端隐藏狗图 */
  }

  .student_section {
    margin-left: 0;
    /* 去掉 margin，让内容贴屏幕左边 */
  }

  .right-panel {
    width: 100%;
    padding: 20px 16px;
    align-items: stretch;
    /* 让子内容更贴边 */
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
import { useRouter } from 'vue-router'
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useAuth } from '@/scripts/useAuth'
import axios from 'axios'
import moment from 'moment'

import Top_Calender from '@/components/top_calendar.vue'
import SchedulePopup from '@/components/popup_reservation.vue'
import TopStudentAvailableClass from '@/components/top_student_available_class.vue'
import TimeBand from '@/components/comp_timeband.vue'

const router = useRouter()
const { restoreLogin, user } = useAuth()
const popup = ref(null)

const userid = ref("")         // ✅ ref にする
const total = ref(0)
const used = ref(0)
const remaining = computed(() => Math.max(0, total.value - used.value))

const screenWidth = ref(window.innerWidth)
const updateWidth = () => {
  screenWidth.value = window.innerWidth
}
onMounted(() => {
  window.addEventListener('resize', updateWidth)
})
onUnmounted(() => {
  window.removeEventListener('resize', updateWidth)
})

function openPopup() {
  popup.value.open()
}

//ログイン
onMounted(async () => {
  await restoreLogin()
  userid.value = user.value.id      // ✅ ここでセット
  await fetchTodayBlueTimes()  // 今日の授業時間帯を取得
})

const blueTimes = ref([])
const isBlueTimesLoaded = ref(false)
const hourStep = computed(() => {
  return screenWidth.value <= 768 ? 6 : 2
})

const fetchTodayBlueTimes = async () => {
  const studentId = user.value.id
  if (!studentId) {
    console.warn('studentId が取得できません')
    return []
  }

  try {
    isBlueTimesLoaded.value = false
    const response = await axios.get(`/api/class-schedules/student/${studentId}`)
    const classList = response.data // ClassSchedule[] 型

    const today = moment().format('YYYY-MM-DD')

    const todayClasses = classList.filter(cls => {
      // 先用 utc 解析，再转本地时间
      const start = moment.utc(cls.startTime).local()
      return start.isSame(today, 'day')
    })

    blueTimes.value = todayClasses.map(cls => {
      const start = moment.utc(cls.startTime).local().format('YYYY-MM-DD HH:mm')
      const end = moment.utc(cls.endTime).local().format('YYYY-MM-DD HH:mm')
      return [start, end]
    })
  } catch (err) {
    console.error('授業取得エラー:', err)
    return []
  } finally {
    isBlueTimesLoaded.value = true
  }
}



// 予約削除時に子コンポーネントのメソッドを呼び出すための ref
const classStatus = ref(null)
const handleReservationDeleted = () => {
  // 直接调用子组件的 fetchHours 方法
  if (classStatus.value?.fetchHours) {
    classStatus.value.fetchHours()
  }
}

const calendar = ref(null)
const handleListRefreshed = async () => {
  // 直接调用子组件的 fetchHours 方法
  if (calendar.value?.onChange) {
    await calendar.value.onChange()
  }
}

</script>