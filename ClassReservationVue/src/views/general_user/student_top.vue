<template>
    <div>
        <Top_Calender account="student" :studentID=userid />
        <div class="student_section">
            <h2 class="center_title">今日の予定</h2>
            <TimeBand :blue_time="blueTimes" :hourStep="2" />
            <!-- <TopStudentAvailableClass :total="10" :remaining="3" /> -->
            <TopStudentAvailableClass :studentID="userid" />



        </div>
    </div>
</template>
<script setup>
import { useRouter } from 'vue-router'
import { ref, computed, onMounted } from 'vue'
import { useAuth } from '@/scripts/useAuth'

import Top_Calender from '@/components/top_calendar.vue'
import Top_ChatBox from '@/components/top_chatbox.vue'
import TopStudentAvailableClass from '@/components/top_student_available_class.vue'
import TimeBand from '@/components/comp_timeband.vue'

const router = useRouter()
const { restoreLogin, user } = useAuth()

const userid = ref("")         // ✅ ref にする
const total = ref(0)
const used = ref(0)
const remaining = computed(() => Math.max(0, total.value - used.value))

const fetchData = async () => {
  if (!userid.value) return   // 念のためチェック
  try {
    const [chargedRes, usedRes] = await Promise.all([
      fetch(`http://localhost:8080/api/charges/users/${userid.value}/total`),
      fetch(`http://localhost:8080/api/class-schedules/student/${userid.value}/total-hours`)
    ])
    total.value = await chargedRes.json()
    used.value = await usedRes.json()
  } catch (err) {
    console.error("データ取得エラー:", err)
  }
}

onMounted(async () => {
  await restoreLogin()
  userid.value = user.value.id      // ✅ ここでセット
  await fetchData()                 // ✅ そのあと fetch 実行
})

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
.center_title {
    text-align: center;
    font-size: 24px;
    margin-bottom: 20px;
    font-weight: bold;
}
</style>