export function latestMessage(chats, uid) {
  return [...chats]
    .filter(c => c.fromUserId === uid || c.toUserId === uid)
    .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))[0]
}

export function latestTime(chats, uid) {
  const msg = latestMessage(chats, uid)
  if (!msg) return ''
  const d = new Date(msg.createdAt)
  const now = new Date()
  return d.toDateString() === now.toDateString()
    ? d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    : `${d.getMonth() + 1}月${d.getDate()}日`
}

import axios from 'axios'
import moment from 'moment'

export async function fetchAndProcessBlueTimes(teacherId, dateStr) {
  const date = moment(dateStr, 'YYYY-MM-DD')

  // 1. 获取 API 数据
  const [availableRes, scheduleRes] = await Promise.all([
    axios.get(`/api/available-times/teacher/${teacherId}`),
    axios.get('/api/class-schedules')
  ])

  // 2. 筛选出同一天的可用时间 & 授業
  const availableTimes = availableRes.data
    .filter(item => moment(item.startTime).isSame(date, 'day'))
    .map(item => ({
      start: moment(item.startTime),
      end: moment(item.endTime)
    }))

  const schedules = scheduleRes.data
    .filter(sch => sch.teacherId === teacherId)
    .filter(sch => moment(sch.startTime).isSame(date, 'day'))
    .map(sch => ({
      start: moment(sch.startTime),
      end: moment(sch.endTime)
    }))

  // 3. 从可用时间中扣除授業时间
  const result = subtractTimeRanges(availableTimes, schedules)

  // 4. 转换为 ['YYYY-MM-DD HH:mm', 'YYYY-MM-DD HH:mm']
  return result.map(r => [
    r.start.format('YYYY-MM-DD HH:mm'),
    r.end.format('YYYY-MM-DD HH:mm')
  ])
}

function subtractTimeRanges(availableRanges, classRanges) {
  const result = []

  for (const a of availableRanges) {
    let splits = [a]

    for (const c of classRanges) {
      const newSplits = []
      for (const s of splits) {
        // 无重叠
        if (c.end <= s.start || c.start >= s.end) {
          newSplits.push(s)
        } else {
          // 有重叠，切割成最多两段
          if (c.start > s.start) {
            newSplits.push({ start: s.start, end: c.start })
          }
          if (c.end < s.end) {
            newSplits.push({ start: c.end, end: s.end })
          }
        }
      }
      splits = newSplits
    }

    result.push(...splits)
  }

  return result
}
