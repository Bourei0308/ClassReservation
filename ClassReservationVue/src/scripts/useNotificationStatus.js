import { ref } from 'vue'

export const hasUnreadNotification = ref(false)

export const checkUnreadNotifications = async (userId) => {
  try {
    const res = await fetch(`/api/notifications/user/${userId}`)
    const notifications = await res.json()
    hasUnreadNotification.value = notifications.some(n => !n.read)
  } catch (error) {
    console.error("通知の取得エラー:", error)
  }
}
