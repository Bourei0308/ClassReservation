// 📁 src/scripts/notificationTemplates.js

export const NotificationTemplates = {
  // 先生からの授業リマインド
  teacherScheduleReminder: (date) => ({
    title: '【リマインド】授業予定のご案内',
    message: `明日 ${date} に授業の予定があります。ご確認のうえ、ご準備をお願いします。`
  }),

  // 先生による予約キャンセル
  classCancelledByTeacher: (date) => ({
    title: '【キャンセル】授業中止のお知らせ',
    message: `誠に申し訳ありませんが、${date} の授業は先生の都合によりキャンセルとなりました。別日をご予約ください。`
  }),

  // 生徒への宿題リマインド
  homeworkReminder: (subject) => ({
    title: '【宿題リマインド】提出をお忘れなく！',
    message: `${subject}の宿題の提出期限が近づいています。忘れずに準備しましょう！`
  }),

  // メンテナンス通知
  systemMaintenance: (date, time) => ({
    title: '【重要】システムメンテナンスのお知らせ',
    message: `以下の時間帯にメンテナンスを実施します。\n\n日時：${date} ${time}\n\nサービスが一時的にご利用いただけなくなる場合があります。ご不便をおかけしますが、ご了承お願いいたします。`
  })
}
