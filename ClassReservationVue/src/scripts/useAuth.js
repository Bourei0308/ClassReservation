// composables/useAuth.ts
import { ref, computed } from 'vue'
import axios from 'axios'

import { useRouter } from 'vue-router'


const user = ref(null)  // 全局用户状态
const TEST_MODE=false;
const TEST_ROLE=1;

export async function restoreLogin() {
    if (user.value) return user.value
    try {
        const res = await axios.get('/api/auth/me', { withCredentials: true })
        user.value = res.data
        sessionStorage.setItem('user', JSON.stringify(res.data))
        return res.data
    } catch {
        user.value = null
        return null
    }
}

export function useAuth() {
    const router = useRouter()

    // ✅ ダミーユーザ
    const devLoginMockUser = () => {
        const mockUser = {
            id: '0',
            name: 'admin',
            email: 'admin@gmail.com',
            password: '$2a$10$TnRTrRgTLdOI68qWaL6XSuVpm7SdV02kXMonfGDqQ2Ueg0UA1Mtby',
            role: TEST_ROLE,
            account: 'admin123'
        }

        user.value = mockUser
        sessionStorage.setItem('user', JSON.stringify(mockUser))
        console.log('[dev] Mock user injected:', mockUser)
        router.push(`/top/${mockUser.role}`)
    }

    return {
        user,TEST_MODE,
        isLoggedIn: computed(() => !!user.value),
        login: async (account, password) => {
            try {
                const res = await axios.post('/api/auth/login', { account, password }, { withCredentials: true })
                user.value = res.data

                if (!user.value || !user.value.id) {
                    alert('アカウントとパスワードが一致していません。')
                    return
                }

                //if (user.value.removeFlag) {
                //    alert('このアカウントはすでに削除されました。');
                //    return;
                //}

                sessionStorage.setItem('user', JSON.stringify(res.data))
                console.log(user.value)
                router.push(`/top/${user.value.role}`)
            } catch (e) {
                console.error(e)
                alert('ログインが失敗しました。')
            }
        },
        logout: () => {
            axios.post('/api/auth/logout', {}, { withCredentials: true })
            user.value = null
            sessionStorage.removeItem('user')
            router.push('/')
        },
        restoreLogin,devLoginMockUser
    }
}
