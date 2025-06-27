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
  // 生徒による授業予約
  classReservedByStudent: (date) => ({
    title: '【予約】授業のご案内',
    message: `生徒様より ${date} の授業が予約されました。ご確認ください。`
  }),

  // 先生が予約承認
  classReservationApprovedByTeacher: (date) => ({
    title: '【承認】授業予約のご案内',
    message: `先生が ${date} の授業予約を承認しました。ご確認ください。`
  })
}
