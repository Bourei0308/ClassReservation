import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'ログイン',
      component: () => import('../views/login.vue'),
    },
    {
      path: '/top/0',
      name: '管理者トップ',
      component: () => import('../views/admin_top.vue'),
    },
    {
      path: '/top/1',
      name: '学生トップ',
      component: () => import('../views/student_top.vue'),
    },
    {
      path: '/admin/register',
      name: 'アカウント作成',
      component: () => import('../views/admin_register_create.vue'),
    },
    {
      path: '/admin/confirm',
      name: '作成確認',
      component: () => import('../views/admin_register_confirm.vue'),
    },
    {
      path: '/admin/userlist',
      name: '作成確認',
      component: () => import('../views/admin_accountlist.vue'),
    },
    {
      path: '/admin/charge',
      name: '授業時間チャージ',
      component: () => import('../views/admin_charge.vue'),
    },
    {
      path: '/chat',
      name: 'チャット画面',
      component: () => import('../views/chat_view.vue'),
    },
    {
      path: '/notice',
      name: '通知画面',
      component: () => import('../views/notice_view.vue'),
    },
    {
      path: '/account',
      name: 'ユーザ情報画面',
      component: () => import('../views/account_detail.vue'),
    },
    {
      path: '/account/edit',
      name: 'ユーザ情報編集',
      component: () => import('../views/account_editinfo.vue'),
    },
    {
      path: '/account/newpassword',
      name: 'ユーザパスワード変更',
      component: () => import('../views/account_editpassword.vue'),
    },

  ],
})


import { useAuth } from '@/scripts/useAuth'
const { restoreLogin } = useAuth()
// ✅ グローバル認証
router.beforeEach(async (to, from, next) => {
  const { user, isLoggedIn, login, logout } = useAuth()
  await restoreLogin()
  
  if (to.meta.requiresAuth) {
    if (!user.value) {
      alert('ログインしてください')
      return next('/login')
    }

    if (to.meta.adminOnly && !user.value.admin) {
      alert('管理者権限が必要です。')
      logout()
      return next('/login')
    }
  }

  return next()
})
export default router
