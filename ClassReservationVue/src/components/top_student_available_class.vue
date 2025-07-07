<template>
  <div class="progress-box">
    <div class="header">コマポン進捗表</div>

    <div class="meter-text center">
      がんばりコマメーター：{{ formatHoursToHM(usedHours) }}
    </div>

    <div class="usage-bar">
      <div class="remaining-label">残り</div>
      <div class="usage-details">
        <div class="bar-background">
          <div class="bar-remaining" :style="{ width: `${remainingPercent}%` }">
            <span class="bar-text">{{ remainingHours }} コマ</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>


<script setup>
import { ref, onMounted, watch } from 'vue';

const props = defineProps({
  studentID: {
    type: String,
    required: true
  }
});

const totalCharged = ref(0);
const usedHours = ref(0);
const remainingHours = ref(0);
const remainingPercent = ref(0);

// ⬇️ 追加：小数時間を「○時間○分」に変換する関数
const formatHoursToHM = (hours) => {
  const h = Math.floor(hours);
  const m = Math.round((hours - h) * 60);
  return `${h}時間${m}分`;
};

const fetchHours = async () => {
  if (!props.studentID) {
    return;
  }
  try {
    const [charged, used] = await Promise.all([
      fetch(`/api/charges/users/${props.studentID}/total`).then(res => res.json()),
      fetch(`/api/class-schedules/student/${props.studentID}/total-hours`).then(res => res.json())
    ]);
    totalCharged.value = charged;
    usedHours.value = used;
    remainingHours.value = Math.max(0, (charged - used)).toFixed(1);
    remainingPercent.value = charged > 0 ? Math.max(0, (charged - used) / charged * 100) : 0;
  } catch (err) {
    console.error("利用状況取得エラー:", err);
    totalCharged.value = usedHours.value = remainingHours.value = remainingPercent.value = 0;
  }
};
defineExpose({ fetchHours })

import { useWebSocket } from '@/scripts/useWebSocket'
const { subscribe,} = useWebSocket()
onMounted(async() =>{await fetchHours();
  subscribe(`/api/topic/calendar/`, async () => {
        await fetchHours();
    });
}
);
watch(() => props.studentID, () => fetchHours());
</script>

<style scoped>
.progress-box {
  background-color: #f4f8ff; /* 淡淡的蓝白底，和 bandbox 一致 */
  border: 1px solid #e6e6e6;
  border-radius: 12px;
  box-shadow: 0 3px 12px rgba(45, 45, 105, 0.12);
  padding: 20px 24px;
  width: 100%;
  max-width: 900px;
  margin: 0 auto;
  color: #2d2d69;
  
  box-sizing: border-box;
}

.header {
  font-weight: 800;
  font-size: 1.8rem;
  margin-bottom: 18px;
  border-bottom: 3px solid #2d2d69eb;
  padding-bottom: 10px;
  color: #2d2d69;

  user-select: none;
}

.meter-text {
  font-size: 1.1rem;
  font-weight: 700;
  margin-bottom: 18px;
  text-align: center;
  color: #1e1e4f;
}

.usage-bar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.remaining-label {
  font-weight: 700;
  font-size: 1.1rem;
  min-width: 50px;
  color: #2d2d69;
  user-select: none;
}

.usage-details {
  flex: 1;
}

.bar-background {
  width: 100%;
  height: 28px;
  background-color: #e9ecef;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: inset 0 1px 3px rgba(0,0,0,0.1);
}

.bar-remaining {
  height: 100%;
  background-color: #2d2d69eb;
  border-radius: 14px 0 0 14px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding-right: 12px;
  color: white;
  font-weight: 700;
  user-select: none;
  transition: width 0.6s ease;
}

.bar-text {
  font-size: 1rem;
  white-space: nowrap;
  pointer-events: none;
}
</style>
