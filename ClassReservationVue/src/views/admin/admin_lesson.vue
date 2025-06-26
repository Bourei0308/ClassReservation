<template>
  <div class="lesson-container">
    <h2>先生授業明細（完了状態）</h2>

    <!-- 🔍 検索フォーム -->
    <div class="search-bar">
      <input v-model="filter.teacher" placeholder="先生名" />
      <input v-model="filter.student" placeholder="生徒名" />
      <select v-model="filter.status">
        <option value="">ステータス</option>
        <option v-for="(label, value) in statusOptions" :key="value" :value="value">
          {{ label }}
        </option>
      </select>

      <input type="date" v-model="filter.date" />
      <select v-model="filter.period">
        <option value="">すべての期間</option>
        <option value="week">今週</option>
        <option value="month">今月</option>
        <option value="3months">3ヶ月以内</option>
        <option value="year">1年以内</option>
      </select>



    </div>

    <!-- 📋 授業テーブル -->
    <table class="lesson-table">
      <thead>
        <tr>
          <th>先生名</th>
          <th>授業日</th>
          <th>時間</th>
          <th>生徒名</th>
          <th>ステータス</th>
          <th>備考</th>
        </tr>
      </thead>
      <tbody>
       <tr v-for="(lesson, index) in filteredLessons":key="index":class="statusClass(lesson.status)">
          <td>{{ lesson.teacherName }}</td>
          <td>{{ lesson.date }}</td>
          <td>{{ lesson.time }}</td>
          <td>{{ lesson.studentName }}</td>
          <td>{{ statusText(lesson.status) }}</td>
          <td>{{ lesson.comment }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
export default {
  name: "AdminLesson",
  data() {
    return {
      lessons: [],
      filter: {
        teacher: "",
        student: "",
        status: "",
        date: "",
        period: ""
      },
      statusOptions: {
      0: "承認待ち",
      1: "承認済み",
      2: "完了",
      3: "キャンセル"
    }
    };
  },
 computed: {
  filteredLessons() {
    return this.lessons.filter(l => {
      const matchTeacher = this.filter.teacher === "" || l.teacherName.includes(this.filter.teacher);
      const matchStudent = this.filter.student === "" || l.studentName.includes(this.filter.student);
      const matchStatus = this.filter.status === "" || l.status === Number(this.filter.status);

      const lessonDate = new Date(l.date);
      let matchDate = true;

      if (this.filter.date && this.filter.period) {
        const base = new Date(this.filter.date);
        let start, end;

        if (this.filter.period === "week") {
          // ✅ 月曜始まりの週に変更
          const day = base.getDay(); // 0:日, 1:月, ..., 6:土
          const offsetToMonday = (day === 0 ? -6 : 1 - day); // 日曜なら前の月曜へ
          start = new Date(base);
          start.setDate(base.getDate() + offsetToMonday);
          end = new Date(start);
          end.setDate(start.getDate() + 6); // 月曜から6日後＝日曜
        } else if (this.filter.period === "month") {
          start = new Date(base.getFullYear(), base.getMonth(), 1);
          end = new Date(base.getFullYear(), base.getMonth() + 1, 0);
        } else if (this.filter.period === "3months") {
          start = new Date(base);
          start.setMonth(base.getMonth() - 3);
          end = new Date(base);
        } else if (this.filter.period === "year") {
          start = new Date(base);
          start.setFullYear(base.getFullYear() - 1);
          end = new Date(base);
        }

        matchDate = lessonDate >= start && lessonDate <= end;
      } else if (this.filter.date) {
        matchDate = l.date === this.filter.date;
      }

      return matchTeacher && matchStudent && matchStatus && matchDate;
    }).sort((a, b) => a.status - b.status);
  }
},
  mounted() {
    fetch("http://localhost:8080/api/lessons/completed")

      .then(res => res.json())
      .then(data => {
        // レスポンスのdateを yyyy-MM-dd に整形（←ここ重要）
        this.lessons = data.map(l => {
          return {
            ...l,
            date: l.date.split("T")[0]  // T付き日付に対応
          };
        });
      })
      .catch(err => {
        alert("データ取得失敗：" + err.message);
      });
  },
  methods: {
  statusText(status) {
    switch (status) {
      case 0: return "承認待ち";
      case 1: return "承認済み";
      case 2: return "完了";
      case 3: return "キャンセル";
      default: return "不明";
    }
  },
  statusClass(status) {
    switch (status) {
      case 0: return "status-pending";     // 承認待ち → 赤
      case 1: return "status-confirmed";   // 承認済み → 水色
      case 2: return "status-completed";   // 完了 → 緑
      case 3: return "status-canceled";    // キャンセル → グレー
      default: return "";
    }
  }
}
};
</script>

<style scoped>
.lesson-container {
  max-width: 1100px;
  margin: 30px auto;
  padding: 20px;
  background-color: #fff;
  border: 1px solid #ddd;
  border-radius: 12px;
  font-family: Arial, sans-serif;
}

h2 {
  text-align: center;
  margin-bottom: 20px;
}

.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
  justify-content: center;
}


.search-bar input,
.search-bar select {
  padding: 8px;
  border-radius: 5px;
  border: 1px solid #ccc;
}

.lesson-table {
  width: 100%;
  border-collapse: collapse;
}

.lesson-table th,
.lesson-table td {
  border: 1px solid #ccc;
  padding: 10px;
  text-align: center;
}

.lesson-table th {
  background-color: #f2f8ff;
}

/* ステータス別 行の背景色 */
.status-pending {
  background-color: #ffe6e6; /* 赤系（承認待ち） */
}

.status-confirmed {
  background-color: #e0f7ff; /* 水色（承認済み） */
}

.status-completed {
  background-color: #e0ffe6; /* 緑系（完了） */
}

.status-canceled {
  background-color: #f0f0f0; /* グレー（キャンセル） */
}


</style>
