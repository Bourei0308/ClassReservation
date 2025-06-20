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
