// composables/useAuth.ts
import { ref,computed } from 'vue'
import axios from 'axios'

import { useRouter } from 'vue-router'


const user = ref(null)  // 全局用户状态


// ⬇️ ！！テスト用ユーザ　削除予定
;(async () => {
  try {
    const res = await axios.post('/api/auth/login', {
      account: 'admin123',
      password: 'admin123'
    }, { withCredentials: true })
    user.value = res.data
    sessionStorage.setItem('user', JSON.stringify(res.data))
    console.log('[dev] Admin user logged in:', res.data)
  } catch (e) {
    console.warn('[dev] テストログイン失敗:', e?.response?.data || e)
  }
})()

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
    return {
        user,
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
        restoreLogin
    }
}
