<template>
     <!-- 合計コマ数：{{ totalCharged }} ｜ 使用済：{{ usedHours }} ｜ 残り：{{ remainingHours }} -->
  <div class="header">コマポン進捗表</div>

  <div class="class-status-box">
    <div class="center">
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

onMounted(async() =>await fetchHours());
watch(() => props.studentID, () => fetchHours());
</script>

<style scoped>
.class-status-box {
  width: 80%;
  margin: 20px auto;
  border: 2px solid #333;
  border-radius: 10px;
  padding: 10px 15px;
  font-family: 'Arial', sans-serif;
  background-color: #fff;
}


.header {
  font-weight: bold;
  font-size: 22px;
  text-align: center;
  margin-top: 40px;
}

.usage-bar {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.remaining-label {
  font-size: 14px;
  color: #444;
}

.usage-details {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  margin-bottom: -10px; 
}


.bar-remaining {
  background-color: #2d2d69;
  height: 100%;
  transition: width 0.3s ease;
  display: flex; 
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  font-size: 14px;
}

.bar-text {
  white-space: nowrap;
}

.bar-background {
  background-color: #e9ecef;
  border-radius: 6px;
  height: 40px; 
  position: relative;
  overflow: hidden;
  margin-bottom: 24px;
}


.bar-remaining {
  background-color: #2d2d69;
  height: 100%;
  transition: width 0.3s ease;
}

.center{
    text-align: center;
    font-size: 20px;
}

.text-info {
  font-size: 12px;
  margin-top: 6px;
  color: #555;
  text-align: center;
}
</style>
