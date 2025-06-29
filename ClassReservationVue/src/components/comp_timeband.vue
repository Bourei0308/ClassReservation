<template>
    <div class="bandbox">
        <!-- 上方刻度行 -->
        <div class="hour-labels">
            <div v-for="h in hourLabels" :key="h" class="hour-label"
                :style="{ left: ((h - rangeStartHour) / rangeDurationHours) * 100 + '%' }">
                {{ h }}時
            </div>
        </div>

        <!-- 灰底带 + 蓝色时间段 -->
        <div class="time-band">
            <div v-for="(range, index) in blue_time" :key="index" class="blue-segment"
                :style="calculateStyle(range[0], range[1])">
                <div class="time-labels">
                    <div class="start-time">{{ formatTime(range[0]) }}</div>
                    <div class="end-time">{{ formatTime(range[1]) }}</div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { computed } from 'vue'
import moment from 'moment'

const props = defineProps({
  blue_time: {
    type: Array,
    required: true
  },
  hourStep: {
    type: Number,
    default: 1
  },
})

function getTimeRangeFromBlueTimes(blueTimes) {
  if (!blueTimes || blueTimes.length === 0) {
    return { start: '00:00', end: '24:00' }
  }

  const allTimes = blueTimes.flat().map(time => moment(time, 'YYYY-MM-DD HH:mm'))

  const minTime = moment.min(allTimes)
  const maxTime = moment.max(allTimes)

  const adjustedStart = minTime.clone().subtract(1, 'hours')
  const adjustedEnd = maxTime.clone().add(1, 'hours')

  return {
    start: adjustedStart.format('HH:mm') < '00:00' ? '00:00' : adjustedStart.format('HH:mm'),
    end: adjustedEnd.format('HH:mm') > '24:00' ? '24:00' : adjustedEnd.format('HH:mm')
  }
}

const timeRange = computed(() => getTimeRangeFromBlueTimes(props.blue_time))

const rangeStartHour = computed(() => parseInt(timeRange.value.start.split(':')[0]))
const rangeEndHour = computed(() => parseInt(timeRange.value.end.split(':')[0]))

const rangeStartMinutes = computed(() => rangeStartHour.value * 60 + parseInt(timeRange.value.start.split(':')[1]))
const rangeEndMinutes = computed(() => rangeEndHour.value * 60 + parseInt(timeRange.value.end.split(':')[1]))

const rangeDurationMinutes = computed(() => rangeEndMinutes.value - rangeStartMinutes.value)
const rangeDurationHours = computed(() => rangeDurationMinutes.value / 60)

const hourLabels = computed(() => {
  const labels = []
  for (let i = rangeStartHour.value; i <= rangeEndHour.value; i += props.hourStep) {
    labels.push(i)
  }
  return labels
})

const formatTime = (datetime) => {
  return moment(datetime, 'YYYY-MM-DD HH:mm').format('HH:mm')
}

const calculateStyle = (startStr, endStr) => {
  const start = moment(startStr, 'YYYY-MM-DD HH:mm')
  const end = moment(endStr, 'YYYY-MM-DD HH:mm')

  const startMin = Math.max(
    start.hours() * 60 + start.minutes(),
    rangeStartMinutes.value
  )
  const endMin = Math.min(
    end.hours() * 60 + end.minutes(),
    rangeEndMinutes.value
  )

  if (endMin <= rangeStartMinutes.value || startMin >= rangeEndMinutes.value) return { display: 'none' }

  const leftPercent = ((startMin - rangeStartMinutes.value) / rangeDurationMinutes.value) * 100
  const widthPercent = ((endMin - startMin) / rangeDurationMinutes.value) * 100

  return {
    left: `${leftPercent}%`,
    width: `${widthPercent}%`
  }
}
</script>



<style scoped>
.bandbox {
    border: 1px solid rgb(230, 230, 230);
    border-radius: 10px;
    padding: 10px;
    width: 100%;
    box-sizing: border-box;
    background-color: rgb(244, 248, 255);
    box-shadow: 0px 0px 4px rgb(156, 156, 156);
}

.hour-labels {
    position: relative;
    height: 20px;
    width: 95%;
    margin: 0 auto 4px auto;
}

.hour-label {
    position: absolute;
    transform: translateX(-50%);
    font-size: 12px;
    color: #2d2d69;
    white-space: nowrap;
    font-weight: 700;
}

.time-band {
    position: relative;
    height: 40px;
    width: 95%;
    margin: 0 auto;
    background-color: #e9ecef;
    border-radius: 6px;
    overflow: hidden;
}

.blue-segment {
    position: absolute;
    top: 0;
    bottom: 0;
    background-color: #2d2d69eb;
    border-radius: 4px;
    color: white;
    font-size: 10px;
    text-align: center;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    padding: 2px 0;
}

.time-labels {
    display: flex;
    flex-direction: column;
    height: 100%;
    justify-content: space-between;
    margin: 5px 0;
}

.start-time,
.end-time {
    line-height: 1;
}
</style>
