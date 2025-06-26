<template>
    <Top_Calender v-if="userid" account="student" :studentID=userid @reservation-refreshed="handleReservationDeleted" ref="calendar" />
    <div class="student_section">
      <h2 class="center_title">今日の予定</h2>
      <TimeBand v-if="isBlueTimesLoaded" :blue_time="blueTimes" :hourStep="2" />
      <button class="open-button" @click="openPopup">
            今月の予約一覧
        </button>
      <SchedulePopup  @list-refreshed="handleListRefreshed" ref="popup" />
      <TopStudentAvailableClass v-if="userid" :studentID="userid" ref="classStatus" />
    </div>
</template>
<script setup>
import { useRouter } from 'vue-router'
import { ref, computed, onMounted } from 'vue'
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

onMounted(async () => {
  await restoreLogin()
  userid.value = user.value.id      // ✅ ここでセット
  await fetchTodayBlueTimes()  // 今日の授業時間帯を取得
})

const blueTimes = ref([])
const isBlueTimesLoaded = ref(false)

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
      const start = moment(cls.startTime)
      return start.isSame(today, 'day')
    })

    blueTimes.value = todayClasses.map(cls => {
      const start = moment(cls.startTime).format('YYYY-MM-DD HH:mm')
      const end = moment(cls.endTime).format('YYYY-MM-DD HH:mm')
      return [start, end]
    })
  } catch (err) {
    console.error('授業取得エラー:', err)
    return []
  } finally {
    isBlueTimesLoaded.value = true
  }
}

function openPopup() {
  popup.value.open()
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

<style scoped>
.student_section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin: 50px 0;
}

.center_title {
  text-align: center;
  font-size: 24px;
  margin-bottom: 20px;
  font-weight: bold;
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