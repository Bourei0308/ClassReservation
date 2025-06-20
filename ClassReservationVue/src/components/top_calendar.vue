<template>
    <div class="calendar-view">
        <div class="teacher-select-box">
            <label for="teacher-select" class="teacher-label">先生を選択！</label>
            <select id="teacher-select" v-if="account" v-model="selectedTeacher" @change="onTeacherChange"
                class="teacher-dropdown">
                <option disabled value="">先生を選択してください</option>
                <option v-for="teacher in tusers" :key="teacher.id" :value="teacher">
                    {{ teacher.name }}
                </option>
            </select>
        </div>
        <div class="calendar-header">
            <button @click="prevMonth">&lt; 前月</button>
            <h2>{{ currentMonthYear }}</h2>
            <button @click="nextMonth">次月 &gt;</button>
        </div>

        <div class="calendar-grid">
            <div class="weekday-names">
                <span v-for="dayName in weekdayNames" :key="dayName" class="weekday-name">{{ dayName }}</span>
            </div>
            <div v-for="(week, weekIndex) in calendarGrid" :key="weekIndex" class="calendar-week">
                <div v-for="(dayObj, dayIndex) in week" :key="dayIndex" :class="['calendar-day', {
                    'is-prev-next-month': dayObj.isPrev || dayObj.isNext,
                    'is-today': dayObj.isToday,
                    'is-selected': (() => {
                        const isSelected = selectedDay && moment(selectedDay).isSame(dayObj.date, 'day');
                        return isSelected;
                    })()
                }]" @click="handleDayClick(dayObj)">
                    {{ dayObj.day }}<div v-if="dayObj.eventList && dayObj.eventList.length > 0" class="event-count">
                        イベント: {{ dayObj.eventList.length }}件
                        <ul class="event-list">
                            <li v-for="event in dayObj.eventList" :key="event.id" class="event-title">
                                {{ event.title }}
                            </li>
                        </ul>
                    </div>
                    
                </div>
            </div>
        </div>
        <div v-if="selectedDayEvents" class="selected-day-info">
            <h3>{{ selectedDayEvents.date.getFullYear() }}年{{ selectedDayEvents.month }}月{{selectedDayEvents.day }}日</h3>
            <div v-if="selectedDayEvents.eventList && selectedDayEvents.eventList.length > 0">
                <h4>この日のイベント:</h4>
                <ul>
                    <li v-for="event in selectedDayEvents.eventList" :key="event.id">
                        {{ event.title }} ({{ moment(event.startTime).format('HH:mm') }} - {{
                            moment(event.endTime).format('HH:mm') }})
                    </li>
                </ul>
            </div>
            <div v-else>
                この日にはイベントがありません。
            </div>
            <div class="new">
                新しい予約を入れる
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import moment from "moment";
import _ from "lodash";
import axios from 'axios';

// リアクティブな状態
const currentDate = ref(moment()); // 現在の月を基準にするmomentオブジェクト
const offset = ref(0); // 曜日開始オフセット (0: 日曜日, 1: 月曜日)
const weekdayNames = ["日", "月", "火", "水", "木", "金", "土"]; // 曜日表示
const calendarGrid = ref([]); // カレンダーの2次元配列データ
const calendarEvent = ref([]); // カレンダーのイベントを格納する配列 (変数名を修正しました)
const selectedDayEvents = ref(null); // 選択された日のイベント情報を保持
const selectedDay = ref(null); // 選択された日付（Dateオブジェクト）を保持
const users = ref([]); //全ユーザーを取得して保持
const tusers = ref([]); //全先生ユーザーを保持
const susers = ref([]); //全生徒ユーザーを保持
const selectedTeacher = ref(null); // 選択された先生
const account = ref("user");

// 算出プロパティ
const currentMonthYear = computed(() => {
    return currentDate.value.format('YYYY年MM月');
});

const currentYear = computed(() => {
    return currentDate.value.year();
});

const currentMonth = computed(() => {
    return currentDate.value.month();
});

// メソッド
const prevMonth = () => {
    currentDate.value = currentDate.value.clone().subtract(1, 'month');
    generateCalendar();
    getEvents();//先生の空き時間を取得
};

const nextMonth = () => {
    currentDate.value = currentDate.value.clone().add(1, 'month');
    generateCalendar();
    getEvents();//先生の空き時間を取得
};

const getUsers = async () => {
    try {
        const res = await axios.get(`/api/users`);//全ユーザを取得
        if (res.data) {
            console.log(res.data);
            users.value = res.data; // 取得したデータを users.value に格納
            // 先生と生徒を分けて格納
            tusers.value = res.data.filter(user => user.role === 2);
            susers.value = res.data.filter(user => user.role === 1);
        } else {
            alert('ユーザの情報を取得できませんでした。');
            users.value = [];
            tusers.value = [];
            susers.value = [];
        }
    } catch (error) {
        console.error("データ取得エラー:", error);
        alert('イベントの情報を取得中にエラーが起きました。');
        users.value = [];
        tusers.value = [];
        susers.value = [];
    }
}

const generateCalendar = async () => { // async キーワードを追加
    const year = currentYear.value;
    const month = currentMonth.value;
    const offsetValue = offset.value;

    // 前月の日を埋める
    const prevPaddingDays = (() => {
        const firstDay = (new Date(year, month)).getDay(); // 今月の1日の曜日
        const paddingDayCount = (firstDay + 7 - offsetValue) % 7; // 埋める日数
        const prevLastDate = (new Date(year, month, 0)).getDate(); // 前月の最終日
        return _.range(prevLastDate - paddingDayCount + 1, prevLastDate + 1).map((day) => ({
            date: new Date(year, month - 1, day),
            day,
            isPrev: true,
            isNext: false,
            isToday: false,
            eventList: [], // 前月・次月の日にはイベントがないと仮定
        }));
    })();

    // 今月の日にちリストを生成する
    const currentDays = (() => {
        const lastDate = new Date(year, month + 1, 0); // 今月の最終日
        const currentDayCount = lastDate.getDate(); // 今月の日数
        return _.range(1, currentDayCount + 1).map((day) => {
            const date = new Date(year, month, day);
            const isToday = moment(date).isSame(moment(), 'day'); // 今日かどうかを判定
            const dayEvents = getDayEvents(date); // その日のイベントを取得（日付オブジェクトを渡す）
            console.log("111"+dayEvents)
            return {
                date: date,
                day,
                isPrev: false,
                isNext: false,
                isToday: isToday,
                eventList: dayEvents,
            };
        });
    })();

    // 来月の日を埋める
    const nextPaddingDays = (() => {
        // カレンダーは通常6週分（42マス）で構成されることが多い
        const totalDays = prevPaddingDays.length + currentDays.length;
        const paddingDayCount = (42 - totalDays) % 7; // 足りない日数
        return _.range(1, paddingDayCount + 1).map((day) => ({
            date: new Date(year, month + 1, day),
            day,
            isPrev: false,
            isNext: true,
            isToday: false,
            eventList: [], // 前月・次月の日にはイベントがないと仮定
        }));
    })();

    // 全ての日を結合
    const flatCalendar = [
        ...prevPaddingDays,
        ...currentDays,
        ...nextPaddingDays,
    ];

    // 2次元配列にして calendarGrid にセット
    calendarGrid.value = _.chunk(flatCalendar, 7); // lodashのchunkを使って7日ごとに分割

    console.log("カレンダーデータ:", calendarGrid.value);
};

// イベントを取得する関数を分離
const getEvents = async () => {
    const year = currentYear.value ? currentYear.value : null;
    const month = currentMonth.value ? currentMonth.value : null;
    console.log('取得する月：' + year + '年' + (month + 1) + "月");

    const teacherId = selectedTeacher.value ? selectedTeacher.value.id : null;
    console.log('選択された先生:', teacherId);

    if (!teacherId) {
        console.warn('先生が選択されていません。');
        return;
    }
    try {
        const resT = await axios.get(`/api/available-times/teacher/${teacherId}`);//先生の予約状況を取得
        // const resS = await axios.get(`/api/available-times`);//生徒の予約状況を取得
        console.log('resT.data:', resT.data);
        if (resT.data) {
            
            // 年月でフィルタリング
            const filtered = resT.data.filter(event => {
                const eventMoment = moment(event.startTime);
                
                return eventMoment.year() === year && eventMoment.month() === month;
            });
            calendarEvent.value = filtered;
        } else {
            alert('イベントの情報を取得できませんでした。');
            calendarEvent.value = [];
        }
    } catch (error) {
        console.error("データ取得エラー:", error);
        alert('イベントの情報を取得中にエラーが起きました。');
        calendarEvent.value = [];
    }
};

// その日のイベントを取得する関数
const getDayEvents = (date) => {
    if (!Array.isArray(calendarEvent.value)) {
        console.warn("calendarEvent.value が配列ではありません。空のリストを返します。");
        return [];
    }
    // 指定日のイベントを取得
    return calendarEvent.value.filter(event => {
        
        const eventDate = moment(event.startTime).format('YYYY-MM-DD');
        const targetDate = moment(date).format('YYYY-MM-DD');
        return eventDate === targetDate;
    }).map(event => {
        // 先生名を付加（tusersから検索）
        const teacher = tusers.value.find(t => t.id === event.teacher_id);
        return {
            ...event,
            teacherName: teacher ? teacher.name : '不明な先生',
            title: teacher ? `${teacher.name}先生出席` : '先生出席',
        };
    });
};

// 日付クリック時のハンドラ
const handleDayClick = (dayObj) => {
    console.log('日付がクリックされました:', dayObj);

    if (dayObj.isPrev || dayObj.isNext) {
        
        selectedDayEvents.value = null;
        selectedDay.value = null;
    } else {
        // イベントリストをクリック時にgetDayEventsで再取得して上書き
        const events = getDayEvents(dayObj.date);
        
        selectedDayEvents.value = {
            ...dayObj,
            eventList: events,
        };
        selectedDay.value = dayObj;
        let eventInfo = '';
        if (dayObj.eventList && dayObj.eventList.length > 0) {
            // イベントタイトルをアラートに表示する例
            eventInfo = `\nイベント: ${dayObj.eventList.map(event => event.title).join(', ')}`;
        }
        // alert(`${currentMonthYear.value}の${dayObj.day}日 がクリックされました！${eventInfo}`);
    }
};

// 先生が選択されたときの処理
const onTeacherChange = async () => {
    await getEvents();
    await generateCalendar();
};

// ライフサイクルフック
onMounted(async () => {
    await getUsers();
    await getEvents();
    await generateCalendar();
});
</script>

<style scoped>
.calendar-view {
    font-family: Arial, sans-serif;
    padding: 20px;
    max-width: 800px;
    margin: 20px auto;
    border: 1px solid #eee;
    border-radius: 8px;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
    background-color: #fff;
}

.calendar-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    background-color: #f8f8f8;
    padding: 10px 15px;
    border-radius: 5px;
}

.calendar-header h2 {
    margin: 0;
    font-size: 1.8em;
    color: #333;
}

.calendar-header button {
    background-color: #007bff;
    color: white;
    border: none;
    padding: 8px 15px;
    border-radius: 5px;
    cursor: pointer;
    font-size: 1em;
    transition: background-color 0.3s ease;
}

.calendar-header button:hover {
    background-color: #0056b3;
}

.calendar-grid {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    /* 7列のグリッド */
    gap: 5px;
    /* グリッド間の隙間 */
}

.weekday-names {
    display: contents;
    /* 子要素が親のグリッドレイアウトに参加するようにする */
}

.weekday-name {
    text-align: center;
    font-weight: bold;
    padding: 10px 0;
    background-color: #e9e9e9;
    border-radius: 3px;
    color: #555;
}

/* 曜日によって色を変える */
.weekday-name:first-child {
    /* 日曜日 */
    color: #d9534f;
    /* 赤 */
}

.weekday-name:last-child {
    /* 土曜日 */
    color: #0275d8;
    /* 青 */
}

.calendar-week {
    display: contents;
    /* 各週の要素が親のグリッドレイアウトに参加するようにする */
}

.calendar-day {
    color: #333;
    text-align: center;
    padding: 15px 5px;
    border: 1px solid #ddd;
    border-radius: 3px;
    background-color: #fefefe;
    font-size: 1.1em;
    position: relative;
    /* イベントなどを配置するために */
    min-height: 80px;
    /* 一日あたりの最低高さ */
    cursor: pointer;
    transition: background-color 0.2s ease;
    /* ホバーエフェクト */
}

.calendar-day:hover:not(.is-prev-next-month) {
    background-color: #e0f2f7;
    /* ホバー時の背景色 */
}

.calendar-day.is-prev-next-month {
    color: #ccc;
    /* 前月や次月の日付は薄く表示 */
    background-color: #f4f4f4;
    cursor: default;
    /* クリック不可を示すカーソル */
    pointer-events: none;
    /* クリックイベントを無効化 */
}

.calendar-day.is-prev-next-month:hover {
    background-color: #f4f4f4;
    /* ホバーしても色が変わらないように */
}

.calendar-day.is-today {
    background-color: #eaf6ff;
    /* 今日の日付の背景色 */
    border-color: #007bff;
    /* 今日の日付のボーダー色 */
    font-weight: bold;
}

.event-count {
    font-size: 0.8em;
    color: #007bff;
    margin-top: 5px;
}

.event-list {
    list-style: none;
    padding: 0;
    margin: 0;
}

.event-title {
    font-size: 0.8em;
    color: #ff9800;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

/* このCSSを<style scoped>セクションに追加してください */
.selected-day-info {
    color: #333;
    margin-top: 30px;
    padding: 20px;
    border: 1px solid #ddd;
    border-radius: 8px;
    background-color: #f9f9f9;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.selected-day-info h3 {
    color: #007bff;
    margin-top: 0;
    margin-bottom: 15px;
}

.selected-day-info h4 {
    color: #555;
    margin-top: 15px;
    margin-bottom: 10px;
}

.selected-day-info ul {
    list-style-type: none;
    padding: 0;
    margin: 0;
}

.selected-day-info li {
    background-color: #e6f7ff;
    border-left: 4px solid #a1b7ff;
    padding: 10px;
    margin-bottom: 8px;
    border-radius: 4px;
    font-size: 0.95em;
    color: #333;
}

/* is-today より下に配置することで、is-selected の方が優先される可能性が高まります */
.calendar-day.is-selected {
    background-color: #ffd8d8;
    /* 選択された日の背景色 */
    border-color: #ff4e4e;
    /* 選択された日のボーダー色 */
    box-shadow: 0 0 8px rgba(250, 111, 111, 0.4);
}

.teacher-select-box {
    margin-bottom: 20px;
}

.teacher-label {
    display: block;
    margin-bottom: 5px;
    font-weight: bold;
    color: #333;
}

.teacher-dropdown {
    width: 100%;
    padding: 10px;
    border: 1px solid #ccc;
    border-radius: 4px;
    font-size: 1em;
    color: #333;
    background-color: #fff;
    transition: border-color 0.3s ease;
}

.teacher-dropdown:focus {
    border-color: #007bff;
    outline: none;
}
</style>