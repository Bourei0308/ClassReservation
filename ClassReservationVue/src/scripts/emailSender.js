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

// ----------------------------
// 📧 メールテンプレート定義
// ----------------------------

export const EmailTemplates = {
  // 学生：予約成功
  studentBookingConfirmed(studentName, classTime) {
    return {
      subject: "【じゅくポン】予約完了のお知らせ",
      body: `${studentName}さん、こんにちは！\n\n授業の予約が正常に完了しました。\n開始時間: ${classTime}\n\n引き続きよろしくお願いいたします。\n\nじゅくポン運営より🐶`,
    };
  },

  // 学生：先生が予約承認
  studentBookingApproved(studentName, classTime) {
    return {
      subject: "【じゅくポン】先生が授業予約を承認しました",
      body: `${studentName}さん、\n\nご予約の授業が先生により承認されました。\n授業日時: ${classTime}\n\n準備してお待ちください！`,
    };
  },

  // 学生：授業前日リマインド
  studentReminder(studentName, classTime) {
    return {
      subject: "【じゅくポン】明日は授業の日です！",
      body: `${studentName}さん、こんにちは！\n\n明日 ${classTime} に授業があります。\nお忘れのないようご準備ください。\n\nじゅくポンより🐶`,
    };
  },

  // 先生：予約通知
  teacherBookingNotification(teacherName, studentName, classTime) {
    return {
      subject: "【じゅくポン】新しい予約があります",
      body: `${teacherName}先生、\n\n${studentName}さんより授業の予約が入りました。\n日時: ${classTime}\nご確認の上、承認をお願いいたします。`,
    };
  },

  // 先生：前日授業リマインド
  teacherReminder(teacherName, classTime) {
    return {
      subject: "【じゅくポン】明日の授業予定",
      body: `${teacherName}先生、\n\n明日 ${classTime} に授業の予定があります。\n準備をお願いいたします。\n\nじゅくポン運営`,
    };
  },
};
