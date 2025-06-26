

// 📁 scripts/emailSender.js

// 共通メール送信関数（Gmail API経由で使うことを想定）
export async function sendEmail({ to, subject, body }) {
  try {
    const response = await fetch("/api/mail/send", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ to, subject, body }),
    });

    if (!response.ok) {
      throw new Error("メール送信失敗: " + response.statusText);
    }

    return await response.json();
  } catch (error) {
    console.error("送信エラー:", error);
    throw error;
  }
}

// メールを送信する関数

/* =========================
📧 先生：予約通知
========================= */
export const sendTeacherNotifyMail = async (scheduleId) => {
    try {
        await axios.post("/api/mail/notify/teacher", {
            classScheduleId: scheduleId
        });
    } catch (error) {
        console.error('sendTeacherNotifyMain:', error);
    }
}

/* =========================
📧 学生：先生が予約承認
========================= */
export const sendStudentConfirmMail = async (scheduleId) => {
    try {
        await axios.post("/api/mail/notify/student/confirmed", {
            classScheduleId: scheduleId
        });
    } catch (error) {
        console.error('sendStudentConfirmMail:', error);
    }
}

/* =========================
📧 学生：先生が予約キャンセル
========================= */
export const sendStudentCancellMail = async (scheduleId) => {
    try {
        await axios.post("/api/mail/notify/student/cancelled", {
            classScheduleId: scheduleId
        });
    } catch (error) {
        console.error('sendStudentCancellMail:', error);
    }
}

/* =========================
📧 パスワード変更メール
========================= */
export const sendPasswordChangedMail = async (userId) => {
    try {
        await axios.post("/api/mail/notify/passwordchange", {
            userId: userId
        });
    } catch (error) {
        console.error('sendPasswordChangedMail:', error);
    }
}

// ----------------------------
// 📧 メールテンプレート定義
// ----------------------------

/* =========================
📧 学生：授業前日リマインド
========================= */
export async function sendStudentReminderEmail(to, studentName, classTime) {
  const subject = '【じゅくポン】明日は授業の日です！'
  const body = `${studentName}さん、こんにちは！\n\n明日 ${classTime} に授業があります。\nお忘れのないようご準備ください。\n\nじゅくポンより🐶`

  await sendEmail({ to, subject, body })
}

/* =========================
📧 先生：前日授業リマインド
========================= */
export async function sendTeacherReminderEmail(to, teacherName, classTime) {
  const subject = '【じゅくポン】明日の授業予定'
  const body = `${teacherName}先生、\n\n明日 ${classTime} に授業の予定があります。\n準備をお願いいたします。\n\nじゅくポン運営`
  await sendEmail({ to, subject, body })
}
