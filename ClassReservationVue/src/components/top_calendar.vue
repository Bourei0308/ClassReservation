<template>
    <div class="calendar-view">
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
                <div v-for="(dayObj, dayIndex) in week" :key="dayIndex"
                    :class="['calendar-day', { 'is-prev-next-month': dayObj.isPrev || dayObj.isNext, 'is-today': dayObj.isToday }]"
                    @click="handleDayClick(dayObj)">
                    {{ dayObj.day }}
                    <div v-if="dayObj.eventList && dayObj.eventList.length > 0" class="event-count">
                        イベント: {{ dayObj.eventList.length }}件
                    </div>
                </div>
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
    };

    const nextMonth = () => {
        currentDate.value = currentDate.value.clone().add(1, 'month');
        generateCalendar();
    };

    const generateCalendar = async () => { // async キーワードを追加
        const year = currentYear.value;
        const month = currentMonth.value;
        const offsetValue = offset.value;

        // イベントデータを先に取得する
        await getEvents(); // イベントが取得されるまで待機

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
        console.log('取得する月：' + currentYear.value + '年' + (currentMonth.value + 1) + "月");
        // try {
        //     const res = await axios.get(`http://localhost:8080/events/month?year=${currentYear.value}&month=${currentMonth.value + 1}`);
        //     if (res.data) {
        //         console.log(res.data);
        //         calendarEvent.value = res.data; // 取得したデータを calendarEvent.value に格納
        //     } else {
        //         alert('イベントの情報を取得できませんでした。');
        //         calendarEvent.value = [];
        //     }
        // } catch (error) {
        //     console.error("データ取得エラー:", error);
        //     alert('イベントの情報を取得中にエラーが起きました。');
        //     calendarEvent.value = [];
        // }
        calendarEvent.value = [];
    };

    // その日のイベントを取得する関数
    const getDayEvents = (date) => {
        // calendarEvent.value が配列であることを確認
        if (!Array.isArray(calendarEvent.value)) {
            console.warn("calendarEvent.value が配列ではありません。空のリストを返します。");
            return [];
        }

        return calendarEvent.value.filter(event => {
            let startDate = moment(event.start_datetime); // momentオブジェクトに変換
            let endDate = moment(event.end_datetime);     // momentオブジェクトに変換
            let targetDate = moment(date);                 // dayObj.date は Date オブジェクトなので moment() で変換

            // 開始日 <= 判定日 かつ 終了日 >= 判定日
            return targetDate.isSameOrAfter(startDate.startOf('day')) &&
                targetDate.isSameOrBefore(endDate.startOf('day'));
        });
    };

    // 日付クリック時のハンドラ
    const handleDayClick = (dayObj) => {
        console.log('日付がクリックされました:', dayObj);

        if (dayObj.isPrev) {
            alert(`${dayObj.day}日 (前月) がクリックされました！`);
        } else if (dayObj.isNext) {
            alert(`${dayObj.day}日 (次月) がクリックされました！`);
        } else {
            let eventInfo = '';
            if (dayObj.eventList && dayObj.eventList.length > 0) {
                // イベントタイトルをアラートに表示する例
                eventInfo = `\nイベント: ${dayObj.eventList.map(event => event.title).join(', ')}`;
            }
            alert(`${currentMonthYear.value}の${dayObj.day}日 がクリックされました！${eventInfo}`);
        }
    };

    // ライフサイクルフック
    onMounted(() => {
        generateCalendar(); // コンポーネントがマウントされたときにカレンダーを生成
        console.log("カレンダービューがマウントされました。");
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
</style>