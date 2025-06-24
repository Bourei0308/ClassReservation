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

    // 提取所有时间为 moment 对象
    const allTimes = blueTimes.flat().map(time => moment(time, 'YYYY-MM-DD HH:mm'))

    // 获取最早和最晚
    const minTime = moment.min(allTimes)
    const maxTime = moment.max(allTimes)

    // 向前/向后调整 1 小时
    const adjustedStart = minTime.clone().subtract(1, 'hours')
    const adjustedEnd = maxTime.clone().add(1, 'hours')

    // 格式化为 HH:mm 输出
    return {
        start: adjustedStart.format('HH:mm') < '00:00' ? '00:00' : adjustedStart.format('HH:mm'),
        end: adjustedEnd.format('HH:mm') > '24:00' ? '24:00' : adjustedEnd.format('HH:mm')
    }
}
const timeRange = getTimeRangeFromBlueTimes(props.blue_time)

// 起始小时和持续小时数
const rangeStartHour = parseInt(timeRange.start.split(':')[0])
const rangeEndHour = parseInt(timeRange.end.split(':')[0])
const rangeStartMinutes = rangeStartHour * 60 + parseInt(timeRange.start.split(':')[1])
const rangeEndMinutes = rangeEndHour * 60 + parseInt(timeRange.end.split(':')[1])
const rangeDurationMinutes = rangeEndMinutes - rangeStartMinutes
const rangeDurationHours = rangeDurationMinutes / 60

// 时刻标签数组
const hourLabels = []
for (let i = rangeStartHour; i <= rangeEndHour; i += props.hourStep) {
    hourLabels.push(i)
}

// 格式化为 HH:mm
const formatTime = (datetime) => {
    return moment(datetime, 'YYYY-MM-DD HH:mm').format('HH:mm')
}

// 蓝色段样式计算
const calculateStyle = (startStr, endStr) => {
    const start = moment(startStr, 'YYYY-MM-DD HH:mm')
    const end = moment(endStr, 'YYYY-MM-DD HH:mm')

    const startMin = Math.max(
        start.hours() * 60 + start.minutes(),
        rangeStartMinutes
    )
    const endMin = Math.min(
        end.hours() * 60 + end.minutes(),
        rangeEndMinutes
    )

    if (endMin <= rangeStartMinutes || startMin >= rangeEndMinutes) return { display: 'none' }

    const leftPercent = ((startMin - rangeStartMinutes) / rangeDurationMinutes) * 100
    const widthPercent = ((endMin - startMin) / rangeDurationMinutes) * 100

    return {
        left: `${leftPercent}%`,
        width: `${widthPercent}%`
    }
}
</script>


<style scoped>
.bandbox {
    border: 2px solid black;
    border-radius: 10px;
    padding: 10px;
    width: 80%;
    box-sizing: border-box;
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
    color: #333;
    white-space: nowrap;
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
    background-color: #0085fc;
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
