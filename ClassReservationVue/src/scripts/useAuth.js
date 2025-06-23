// composables/useAuth.ts
import { ref, computed } from 'vue'
import axios from 'axios'

import { useRouter } from 'vue-router'


const user = ref(null)  // 全局用户状态
const role = ref(null)
const TEST_MODE = false;
const TEST_ROLE = 1;


export async function restoreLogin() {
    if (user.value) return user.value
    try {
        const res = await axios.get('/api/auth/me', { withCredentials: true, validateStatus: (status) => status < 500 })
        user.value = res.data
        sessionStorage.setItem('user', JSON.stringify(res.data))
        role.value = user.value.role
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
        role.value = user.value.role
        console.log('[dev] Mock user injected:', mockUser)
        router.push(`/top/${mockUser.role}`)
    }

    return {
        user, TEST_MODE,
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
                role.value = user.value.role
                router.push(`/top/${user.value.role}`)
            } catch (e) {
                console.error(e)
                alert('ログインが失敗しました。')
            }
        },
        logout: async () => {
            await axios.post('/api/auth/logout', {}, { withCredentials: true })

            user.value = null
            role.value = null
            sessionStorage.removeItem('user')

            await router.push('/')
            location.reload() // ✅ 强制刷新，确保所有状态重新初始化
        },
        restoreLogin, devLoginMockUser
    }
}
