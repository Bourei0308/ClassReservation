
import { createRouter, createWebHistory } from 'vue-router'

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
      meta: { requiresAuth: true, role_number: 0 },

      component: () => import('../views/admin/admin_top.vue'),
    },
    {
      path: '/top/1',
      name: '学生トップ',
      meta: { requiresAuth: true, role_number: 1 },
      component: () => import('../views/general_user/student_top.vue'),
    },
    {
      path: '/top/2',
      name: '先生トップ',
      meta: { requiresAuth: true, role_number: 2 },
      component: () => import('../views/general_user/teacher_top.vue'),
    },
    {
      path: '/admin/register',
      name: 'アカウント作成',
      component: () => import('../views/admin/admin_register_create.vue'),
    },
    {
      path: '/admin/confirm',
      name: '作成確認',
      component: () => import('../views/admin/admin_register_confirm.vue'),
    },
    {
      path: '/admin/userlist',
      name: '管理者用ユーザ一覧',
      meta: { requiresAuth: true, role_number: 0 },
      component: () => import('../views/admin/admin_accountlist.vue'),
    },
    {
      path: '/admin/charge',
      name: '授業時間チャージ',
      meta: { requiresAuth: true, role_number: 0 },
      component: () => import('../views/admin/admin_charge.vue'),
    },
    {
      path: '/chat',
      name: 'チャット画面',
      meta: { requiresAuth: true },
      component: () => import('../views/general_user/chat_view.vue'),
    },
    {
      path: '/notice',
      name: '通知画面',
      meta: { requiresAuth: true },
      component: () => import('../views/general_user/notice_view.vue'),
    },
    {
      path: '/account',
      name: 'ユーザ情報画面',
      meta: { requiresAuth: true },
      component: () => import('../views/general_user/account_detail.vue'),
    },
    {
      path: '/account/edit',
      name: 'ユーザ情報編集',
      meta: { requiresAuth: true },
      component: () => import('../views/general_user/account_editinfo.vue'),
    },
    {
      path: '/account/newpassword',
      name: 'ユーザパスワード変更',
      meta: { requiresAuth: true },
      component: () => import('../views/general_user/account_editpassword.vue'),
    },
    {
      path: '/account/passwordedit',
      name: 'ユーザパスワード変更認証コード',
      meta: { requiresAuth: true },
      component: () => import('../views/general_user/account_passwordinfo.vue'),
    },

  ],
})


import { useAuth } from '@/scripts/useAuth'
const { restoreLogin } = useAuth()
// ✅ グローバル認証
router.beforeEach(async (to, from, next) => {
  const { user, isLoggedIn, login, logout, role } = useAuth()
  await restoreLogin()

  if (to.meta.requiresAuth) {
    if (!user.value) {
      alert('ログインしてください')
      return next('/')
    }

    if (to.meta.role_number!=null&& user.value.role !== to.meta.role_number) {
      console.log("no")
      alert('認証情報が違います。')
      return next(`/top/${user.value.role}`)
    }
  }

  return next()
})
export default router
