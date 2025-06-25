<template>
  <div class="lesson-container">
    <h2>先生授業明細（完了状態）</h2>
    <div class="table-wrapper">
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
          <tr v-for="lesson in lessons" :key="lesson.id">
            <td>{{ lesson.teacherName }}</td>
            <td>{{ lesson.date }}</td>
            <td>{{ lesson.startTime }}〜{{ lesson.endTime }}</td>
            <td>{{ lesson.studentName }}</td>
            <td>{{ lesson.status }}</td>
            <td>{{ lesson.comment || 'なし' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
export default {
  name: "AdminLesson",
  data() {
    return {
      lessons: []
    };
  },
  mounted() {
    fetch("http://localhost:8080/api/lessons/completed")
      .then(res => res.json())
      .then(data => {
        this.lessons = data;
      })
      .catch(err => {
        alert("授業情報の取得に失敗しました: " + err.message);
      });
  }
};
</script>
<style scoped>
.lesson-container {
  max-width: 1000px;
  margin: 40px auto;
  padding: 20px;
  background-color: #ffffff;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.05);
  font-family: 'Arial', sans-serif;
}

h2 {
  text-align: center;
  margin-bottom: 20px;
  color: #333;
}

.table-wrapper {
  overflow-x: auto;
}

.lesson-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 800px;
}

.lesson-table th,
.lesson-table td {
  border: 1px solid #ccc;
  padding: 12px;
  text-align: center;
  white-space: nowrap;
}

.lesson-table th {
  background-color: #f0f8ff;
  color: #333;
  font-weight: bold;
}

.lesson-table tr:hover {
  background-color: #f9f9f9;
}


</style>
