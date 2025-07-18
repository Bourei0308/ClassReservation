// 修改 useAuth.ts
import { ref, computed, watch } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const user = ref(null)
const role = ref(null)
const language = ref(localStorage.getItem('language') || 'zh')

// 监听语言变更并存入 cookie
watch(language, (newLang) => {
    document.cookie = `lang=${newLang};path=/;max-age=31536000`
    localStorage.setItem('language', newLang)
})

export async function restoreLogin() {
    if (user.value) return user.value
    try {
        const res = await axios.get('/api/auth/me', { withCredentials: true, validateStatus: s => s < 500 })
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
    return {
        user,
        role,
        language,
        isLoggedIn: computed(() => !!user.value),
        hasEmail: computed(() => Boolean(user.value?.email?.trim())),
        login: async (account, password) => {
            const { useWebSocket } = await import('@/scripts/useWebSocket')
            const { connect, subscribe } = useWebSocket()
            try {
                const res = await axios.post('/api/auth/login', { account, password }, { withCredentials: true })
                user.value = res.data
                if (!user.value?.id) {
                    alert('アカウントとパスワードが一致していません。')
                    return
                }
                sessionStorage.setItem('user', JSON.stringify(res.data))
                role.value = user.value.role
                if (!user.value.email?.trim()) {
                    router.push('/account/set-email')
                    return
                }
                connect(() => {
                    subscribe(`/api/topic/unread/${user.value.id}`, () => {
                        hasUnreadMessage.value = true
                    })
                    subscribe(`/api/topic/notice/${user.value.id}`, () => {
                        hasUnreadNotification.value = true
                    })
                })
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
            location.reload()
        },
        restoreLogin
    }
}
