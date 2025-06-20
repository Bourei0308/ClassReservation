<template>
    <div>
        <Top_Calender />
        <Top_ChatBox />
    </div>
</template>

<script setup>
    import { useRouter } from 'vue-router'; // Vue RouterのuseRouterフックをインポート
    import { ref , onMounted} from 'vue'; // VueのrefをインポートClassReservationVue\src\components\top_calendar.vue
    import Top_Calender from '@/components/top_calendar.vue'; // 子コンポーネントをインポート
    import Top_ChatBox from '@/components/top_chatbox.vue'; // 子コンポーネントをインポート
    const router = useRouter(); // useRouterフックを使用してルーターインスタンスを取得
</script>