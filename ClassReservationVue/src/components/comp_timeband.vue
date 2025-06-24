<template>
  <canvas ref="canvas" :width="width" :height="height" />
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import moment from 'moment'

const props = defineProps({
  blue_time: {
    type: Array,
    required: true
  }
})

const canvas = ref(null)
const width = 800
const height = 40

// 描画
const drawBand = () => {
  const ctx = canvas.value.getContext('2d')

  // クリア
  ctx.clearRect(0, 0, width, height)

  // 灰色バック
  ctx.fillStyle = '#ccc'
  ctx.fillRect(0, 0, width, height)

  // 分け
  const minutesInDay = 24 * 60
  const pxPerMin = width / minutesInDay

  // 青色描画
  ctx.fillStyle = 'blue'
  props.blue_time.forEach(([startStr, endStr]) => {
    const start = moment(startStr, 'YYYY-MM-DD HH:mm')
    const end = moment(endStr, 'YYYY-MM-DD HH:mm')

    const startMinutes = start.hours() * 60 + start.minutes()
    const endMinutes = end.hours() * 60 + end.minutes()

    const x = startMinutes * pxPerMin
    const w = (endMinutes - startMinutes) * pxPerMin
    ctx.fillRect(x, 0, w, height)
  })
}

// 初始绘制
onMounted(() => {
  drawBand()
})

// 输入更新时重绘
watch(() => props.blue_time, drawBand, { deep: true })
</script>
