<template>
    <div class="calendar-view">
        <div v-if="account == 'student'" class="teacher-select-box">
            <label for="teacher-select" class="teacher-label">先生を選択！</label>
            <select id="teacher-select" v-model="selectedTeacher" @change="onTeacherChange" class="teacher-dropdown">
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
                    {{ dayObj.day }}<div v-if="dayObj.eventList && dayObj.eventList.length > 0">
                        <!-- イベント: {{ dayObj.eventList.length }}件 -->
                        <ul class="event-list">
                            <li v-for="event in dayObj.eventList" :key="event.id">
                                <span v-if="event && !event.studentName" class="event-title">{{ event.title }}</span>
                                <span v-if="event && event.studentName" class="student-info">{{ event.title }}</span>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
        <div v-if="selectedDayEvents" class="selected-day-info">
            <h3>{{ selectedDayEvents.date.getFullYear() }}年{{ selectedDayEvents.date.getMonth() + 1 }}月{{
                selectedDayEvents.date.getDate() }}日</h3>
            <div v-if="selectedDayEvents.eventList && selectedDayEvents.eventList.length > 0">
                <h4>この日のイベント:</h4>
                <ul>
                    <li v-for="event in selectedDayEvents.eventList" :key="event.id" :class="event.studentName ? 'student-event' : 'teacher-event'">
                        {{ event.title }} ({{ moment(event.startTime).format('HH:mm') }} - {{
                            moment(event.endTime).format('HH:mm') }})
                        <button v-if="account === 'teacher' || (account === 'student' && event.studentName)" @click="openEditPopup(event)">編集</button>
                        <button v-if="account === 'teacher' || (account === 'student' && event.studentName)" @click="deleteEditPopup(event)">削除</button>
                    </li>
                </ul>
            </div>
            <div v-else>
                この日にはイベントがありません。
            </div>
            <div v-if="account === 'teacher' || account === 'student' && selectedDayEvents && selectedDayEvents.eventList && selectedDayEvents.eventList.some(event => event.teacherName)" class="new">
                <button class="reserve-btn" @click="openReservationPopup">新しい予約を入れる</button>
            </div>
            <div v-else class="message">
                <label>先生を選択して下さい</label>
            </div>
            <div v-if="showPopup" class="popup-overlay">
                <div class="popup-content">
                    <h4 v-if="popupMode === 'create'">新しい予約</h4>
                    <h4 v-else-if="popupMode === 'edit'">予定の編集</h4>
                    <h4 v-else-if="popupMode === 'delete'">予定の削除</h4>
                    <div v-if="popupMode === 'delete' && editingEvent">
                        <p>本当にこの予定を削除しますか？</p>
                        <div style="margin-top:10px;">
                            <button @click="submitDeleteReservation">削除</button>
                            <button @click="closeReservationPopup">キャンセル</button>
                        </div>
                    </div>
                    <div v-else-if="account === 'teacher'">
                        <label>開始日:
                            <input type="date" :value="selectedDayEvents ? formatDate(selectedDayEvents.date) : ''"
                                disabled />
                        </label>
                        <label>開始時間:
                            <input type="time" v-model="popupStartTime" />
                        </label>
                        <label>終了日:
                            <input type="date" :value="selectedDayEvents ? formatDate(selectedDayEvents.date) : ''"
                                disabled />
                        </label>
                        <label>終了時間:
                            <input type="time" v-model="popupEndTime" />
                        </label>
                        <div style="margin-top:10px;">
                            <button v-if="popupMode === 'create'" @click="submitReservation">登録</button>
                            <button v-else-if="popupMode === 'edit'" @click="submitTeacherEditReservation">更新</button>
                            <button @click="closeReservationPopup">キャンセル</button>
                        </div>
                    </div>
                    <div v-else-if="account === 'student'">
                        <label>その日の先生の予定:
                            
                        </label>
                        <label>開始日:
                            <input type="date" :value="selectedDayEvents ? formatDate(selectedDayEvents.date) : ''"
                                disabled />
                        </label>
                        <label>開始時間:
                            <input type="time" v-model="popupStartTime" />
                        </label>
                        <label>終了日:
                            <input type="date" :value="selectedDayEvents ? formatDate(selectedDayEvents.date) : ''"
                                disabled />
                        </label>
                        <label>終了時間:
                            <input type="time" v-model="popupEndTime" />
                        </label>
                        <label v-if="popupMode === 'edit'">承認状態:
                            <span v-if="popupMode === 'edit' && editingEvent" :style="{
                                color: editingEvent.status === 0 ? '#ff9800' : editingEvent.status === 1 ? '#2196f3' : editingEvent.status === 2 ? '#4caf50' : editingEvent.status === 3 ? '#f44336' : '#888',
                                fontWeight: 'bold',
                                marginLeft: '10px'
                            }">
                                {{
                                    editingEvent.status === 0 ? '承認待ち' :
                                    editingEvent.status === 1 ? '承認済み' :
                                    editingEvent.status === 2 ? '完了' :
                                    editingEvent.status === 3 ? 'キャンセル' :
                                    '不明'
                                }}
                            </span>
                            <span v-else style="color:#888; margin-left:10px;">申請時は「承認待ち」になります</span>
                        </label>
                        <div style="margin-top:10px;">
                            <button v-if="popupMode === 'create'" @click="submitStudentReservation">予約申請</button>
                            <button v-else-if="popupMode === 'edit'" @click="submitStudentEditReservation">更新</button>
                            <button @click="closeReservationPopup">キャンセル</button>
                        </div>
                    </div>

                    <div v-else>
                        <span>予約はこのアカウントでは入力できません。</span>
                        <button @click="closeReservationPopup">閉じる</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { defineProps } from 'vue';
import moment from "moment";
import _ from "lodash";
import axios from 'axios';

import { useAuth } from '@/scripts/useAuth'
const { user } = useAuth()


// 親から受け取るpropsを定義
const props = defineProps({
    account: {
        type: String,
        default: 'teacher'
    },
    teacherID: {
        type: [String, Number],
        default: null
    },
    studentID: {
        type: [String, Number],
        default: null
    }
});



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
const account = ref(props.account);
const teacherID = ref(props.teacherID);
const studentID = ref(props.studentID);
const showPopup = ref(false);
const popupStartTime = ref('');
const popupEndTime = ref('');
const popupMode = ref('create'); // 'create' or 'edit'
const editingEvent = ref(null); // 編集対象イベント

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
    onChange();
};

const nextMonth = () => {
    currentDate.value = currentDate.value.clone().add(1, 'month');
    onChange();
};

const getUsers = async () => {
    try {
        const res = await axios.get(`/api/users`);//全ユーザを取得
        if (res.data) {
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

    let teacherId = teacherID.value ? teacherID.value : (selectedTeacher.value ? selectedTeacher.value.id : null);
    let studentId = studentID ? studentID.value : null;
    console.log('選択された先生:', teacherId);
    let allEvents = [];
    try {
        // 先生の予定
        let resT = await axios.get(`/api/available-times/teacher/${teacherId}`);
        // 生徒の予定
        let resS = null;
        if (account.value === 'student' && studentId) {
            resS = await axios.get(`/api/class-schedules/student/${studentId}`);
        } else if (account.value === 'teacher') {
            resS = await axios.get(`/api/class-schedules`);
        }
        // 先生の予定
        let teacherEvents = resT.data ? resT.data.filter(event => {
            const eventMoment = moment(event.startTime);
            return eventMoment.year() === year && eventMoment.month() === month;
        }) : [];
        // 生徒の予定
        let studentEvents = (resS && resS.data) ? resS.data.filter(event => {
            const eventMoment = moment(event.start_time);
            return eventMoment.year() === year && eventMoment.month() === month;
        }) : [];
        // 統合
        allEvents = [
            ...teacherEvents.map(e => ({
                ...e,
                startTime: e.startTime || e.start_time,
                endTime: e.endTime || e.end_time,
                teacher_id: e.teacherId,
            })),
            ...studentEvents.map(e => ({
                ...e,
                startTime: e.startTime || e.start_time,
                endTime: e.endTime || e.end_time,
                student_id: e.studentId,
                teacher_id: e.teacherId,
                status: e.status
            }))
        ];
        calendarEvent.value = allEvents;
        console.log(calendarEvent.value)
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
    const targetDate = moment(date).format('YYYY-MM-DD');
    // accountによって表示するイベントを切り替え
    if (account.value === 'student') {
        // 生徒の場合: 選択先生の予定＋自分（生徒）の予定のみ
        return calendarEvent.value.filter(event => {
            const eventDate = moment(event.startTime).format('YYYY-MM-DD');
            // 先生の予定 or 自分の予定
            const isTeacher = teacherID.value ? event.teacher_id == teacherID.value : (selectedTeacher.value && event.teacher_id == selectedTeacher.value.id);
            const isStudent = event.student_id && users.value.find(u => u.id == event.student_id && u.role === 1);
            return eventDate === targetDate && (isTeacher || isStudent);
        }).map(event => {
            const teacher = tusers.value.find(t => t.id == event.teacher_id);
            const student = susers.value.find(s => s.id == event.student_id);
            let title = '';
            if (teacher && student) {
                title = `${teacher.name}先生・${student.name}さん出席`;
            } else if (teacher) {
                title = `${teacher.name}先生出席`;
            } else if (student) {
                title = `${student.name}さん出席`;
            } else {
                title = '予定あり';
            }
            return {
                ...event,
                teacherName: teacher ? teacher.name : '不明な先生',
                studentName: student ? student.name : '',
                title,
            };
        });
    } else if (account.value === 'teacher') {
        // 先生の場合: 自分の予定＋生徒が登録した予定すべて
        return calendarEvent.value.filter(event => {
            const eventDate = moment(event.startTime).format('YYYY-MM-DD');
            // 自分が担当 or 生徒が登録
            console.log("event",eventDate,event.teacher_id)
            const isTeacher = teacherID.value ? event.teacher_id == teacherID.value : (selectedTeacher.value && event.teacher_id == selectedTeacher.value.id);
            return (eventDate === targetDate) && isTeacher;
        }).map(event => {
            const teacher = tusers.value.find(t => t.id == event.teacher_id);
            const student = susers.value.find(s => s.id == event.student_id);
            let title = '';
            if (teacher && student) {
                title = `${teacher.name}先生・${student.name}さん出席`;
            } else if (teacher) {
                title = `${teacher.name}先生出席`;
            } else if (student) {
                title = `${student.name}さん出席`;
            } else {
                title = '予定あり';
            }
            return {
                ...event,
                teacherName: teacher ? teacher.name : '不明な先生',
                studentName: student ? student.name : '',
                title,
            };
        });
    } else {
        // その他は全て表示
        return calendarEvent.value.filter(event => {
            const eventDate = moment(event.startTime).format('YYYY-MM-DD');
            return eventDate === targetDate;
        }).map(event => {
            const teacher = tusers.value.find(t => t.id == event.teacher_id);
            const student = susers.value.find(s => s.id == event.student_id);
            let title = '';
            if (teacher && student) {
                title = `${teacher.name}先生・${student.name}さん出席`;
            } else if (teacher) {
                title = `${teacher.name}先生出席`;
            } else if (student) {
                title = `${student.name}さん出席`;
            } else {
                title = '予定あり';
            }
            return {
                ...event,
                teacherName: teacher ? teacher.name : '不明な先生',
                studentName: student ? student.name : '',
                title,
            };
        });
    }
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

// 画面が更新されたときの処理
const onChange = async () => {
    await getEvents();
    await generateCalendar();
    const events = getDayEvents(selectedDay.value.date);

    selectedDayEvents.value = {
        ...selectedDay.value,
        eventList: events,
    };
};

// 新しい予約のポップアップを開く
const openReservationPopup = () => {
    showPopup.value = true;
    popupMode.value = 'create';
    popupStartTime.value = '';
    popupEndTime.value = '';
    editingEvent.value = null;
};
// 編集ポップアップを開く
const openEditPopup = (event) => {
    showPopup.value = true;
    popupMode.value = 'edit';
    editingEvent.value = event;
    // 既存の値をセット
    popupStartTime.value = moment(event.startTime).format('HH:mm');
    popupEndTime.value = moment(event.endTime).format('HH:mm');
};

// 削除ポップアップを開く
const deleteEditPopup = (event) => {
    showPopup.value = true;
    popupMode.value = 'delete';
    editingEvent.value = event;
};

// 新しい予約のポップアップを閉じる
const closeReservationPopup = () => {
    showPopup.value = false;
};

// 予約を登録する
const submitReservation = async () => {
    // バリデーション例
    if (!popupStartTime.value || !popupEndTime.value) {
        alert('開始時間と終了時間を入力してください');
        return;
    }
    const date = selectedDayEvents.value.date;
    const startDateTime = `${formatDate(date)}T${popupStartTime.value}`;
    const endDateTime = `${formatDate(date)}T${popupEndTime.value}`;
    const payload = {
        teacherId: props.teacherID,
        startTime: startDateTime,
        endTime: endDateTime
    };
    // ここでAPI送信などの処理を実装
    try {
        const resT = await axios.post(`/api/available-times`, payload);//先生の予定予約
        // const resS = await axios.get(`/api/available-times`);//生徒の予約状況を取得
        console.log('resT.data:', resT.data);
        if (resT.data) {
            alert(`予約: ${popupStartTime.value} ～ ${popupEndTime.value}`);
        } else {
            alert('イベントの情報を登録できませんでした。');
        }
    } catch (error) {
        console.error("データ登録エラー:", error);
        alert('イベントの情報を登録中にエラーが起きました。');
    }
    onChange();
    showPopup.value = false;
};

// 生徒の編集の登録
const submitStudentEditReservation = async () => {
    if (!popupStartTime.value || !popupEndTime.value) {
        alert('開始時間と終了時間を入力してください');
        return;
    }
    const date = selectedDayEvents.value.date;
    const startDateTime = `${formatDate(date)}T${popupStartTime.value}`;
    const endDateTime = `${formatDate(date)}T${popupEndTime.value}`;
    if (!editingEvent.value) return;
    const payload = {
        id: editingEvent.value.id,
        teacherId: editingEvent.value.teacher_id,
        studentId: studentID.value,
        teacherId: teacherID.value || (selectedTeacher.value ? selectedTeacher.value.id : null),
        createdAt: moment().format('YYYY-MM-DDTHH:mm:ss'),
        startTime: startDateTime,
        endTime: endDateTime,
        status: editingEvent.value.status, // 承認待ち
    };
    try {
        await axios.put(`/api/class-schedules/${editingEvent.value.id}`, payload);
        alert('予定を更新しました');
    } catch (error) {
        console.error('編集エラー:', error);
        alert('予定の更新に失敗しました');
    }
    onChange();
    showPopup.value = false;
};

// 先生の編集の登録
const submitTeacherEditReservation = async () => {
    if (!popupStartTime.value || !popupEndTime.value) {
        alert('開始時間と終了時間を入力してください');
        return;
    }
    const date = selectedDayEvents.value.date;
    const startDateTime = `${formatDate(date)}T${popupStartTime.value}`;
    const endDateTime = `${formatDate(date)}T${popupEndTime.value}`;
    if (!editingEvent.value) return;
    const payload = {
        id: editingEvent.value.id,
        teacherId: editingEvent.value.teacher_id,
        startTime: startDateTime,
        endTime: endDateTime
    };
    try {
        await axios.put(`/api/available-times/${editingEvent.value.id}`, payload);
        alert('予定を更新しました');
    } catch (error) {
        console.error('編集エラー:', error);
        alert('予定の更新に失敗しました');
    }
    onChange();
    showPopup.value = false;
};

// 生徒用の予約申請
const submitStudentReservation = async () => {
    if (!popupStartTime.value || !popupEndTime.value) {
        alert('開始時間と終了時間を入力してください');
        return;
    }
    const date = selectedDayEvents.value.date;
    const startDateTime = `${formatDate(date)}T${popupStartTime.value}`;
    const endDateTime = `${formatDate(date)}T${popupEndTime.value}`;
    const payload = {
        studentId: studentID.value,
        teacherId: teacherID.value || (selectedTeacher.value ? selectedTeacher.value.id : null),
        startTime: startDateTime,
        endTime: endDateTime,
        createdAt: moment().format('YYYY-MM-DDTHH:mm:ss'),
        status: 0 // 承認待ち
    };
    try {
        await axios.post(`/api/class-schedules`, payload);
        alert('予約申請を送信しました');
    } catch (error) {
        console.error('生徒予約エラー:', error);
        alert('予約申請に失敗しました');
    }
    onChange();
    showPopup.value = false;
};

// 予定を削除する
const submitDeleteReservation = async () => {
    if (!editingEvent.value) return;
    const eventId = editingEvent.value.id;
    let url = '';
    // 先生の予定か生徒の予定かでURLを分岐
    if (editingEvent.value.teacher_id && !editingEvent.value.student_id) {
        // 先生の空き時間（available-times）
        url = `/api/available-times/${eventId}`;
    } else if (editingEvent.value.student_id) {
        // 生徒の予約（class-schedules）
        url = `/api/class-schedules/${eventId}`;
    } else {
        // アラート
        alert('生徒でも先生でもありません');
    }
    try {
        await axios.delete(url);
        alert('予定を削除しました');
    } catch (error) {
        console.error('削除エラー:', error);
        alert('予定の削除に失敗しました');
    }
    onChange();
    showPopup.value = false;
};

// 日付をyyyy-MM-dd形式で返す関数
const formatDate = (date) => {
    return moment(date).format('YYYY-MM-DD');
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

.student-info {
    font-size: 12px;
    color: hsl(211, 100%, 50%);
    margin-top: 5px;
}

.event-list {
    list-style: none;
    padding: 0;
    margin: 0;
}

.event-title {
    font-size: 12px;
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

.popup-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background: rgba(0, 0, 0, 0.3);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
}

.popup-content {
    background: #fff;
    padding: 30px 20px;
    border-radius: 8px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
    min-width: 300px;
    text-align: center;
}

.popup-content h4 {
    margin-top: 0;
    margin-bottom: 15px;
    color: #333;
}

.popup-content label {
    display: block;
    margin: 10px 0 5px 0;
    font-weight: bold;
}

.popup-content input[type="datetime-local"] {
    width: 90%;
    padding: 6px;
    margin-bottom: 10px;
    border: 1px solid #ccc;
    border-radius: 4px;
}

.popup-content button {
    margin: 0 8px;
    padding: 7px 18px;
    border: none;
    border-radius: 4px;
    background: #007bff;
    color: #fff;
    cursor: pointer;
    font-size: 1em;
    transition: background 0.2s;
}

.popup-content button:hover {
    background: #0056b3;
}

.popup-content div {
    margin-top: 10px;
}

.reserve-btn {
    background: linear-gradient(90deg, #ff9800 0%, #ffc107 100%);
    color: #fff;
    border: none;
    border-radius: 6px;
    padding: 10px 24px;
    font-size: 1.1em;
    font-weight: bold;
    box-shadow: 0 2px 6px rgba(255, 152, 0, 0.15);
    cursor: pointer;
    transition: background 0.2s, box-shadow 0.2s;
    margin: 10px 0;
}

.reserve-btn:hover {
    background: linear-gradient(90deg, #ffb74d 0%, #ffe082 100%);
    color: #ff9800;
    box-shadow: 0 4px 12px rgba(255, 152, 0, 0.25);
}

.message {
    background: #fffbe6;
    border: 1.5px solid #ffd700;
    color: #b8860b;
    border-radius: 8px;
    padding: 18px 0;
    margin: 18px 0;
    text-align: center;
    font-size: 1.1em;
    font-weight: bold;
    box-shadow: 0 2px 8px rgba(255, 215, 0, 0.08);
}

.student-event {
    background-color: #e3f2fd !important;
    border-left: 4px solid #2196f3 !important;
}
.teacher-event {
    background-color: #fff3e0 !important;
    border-left: 4px solid #ff9800 !important;
}
</style>
