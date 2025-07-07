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
      path: '/account/reset-request',
      component: () => import('../views/general_user/ResetRequest.vue')
    },
    {
      path: '/contact',
      name: 'お問い合わせ',
      component: () => import('../views/general_user/ContactForm.vue')
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
      path: '/admin/register/create',
      name: 'アカウント作成',
      component: () => import('../views/admin/admin_register_create.vue'),
    },
    {
      path: '/admin/accountlist',
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
      path: '/admin/lesson',
      name: '授業管理',
      meta: { requiresAuth: true, role_number: 0 },
      component: () => import('../views/admin/admin_lesson.vue'),
    },
    {
      path: '/admin/lesson-add',
      name: '授業入力',
      meta: { requiresAuth: true, role_number: 0 },
      component: () => import('../views/admin/admin_addclass.vue'),
    },
    {
      path: '/admin/user-add',
      name: 'ユーザ入力',
      meta: { requiresAuth: true, role_number: 0 },
      component: () => import('../views/admin/admin_adduser.vue'),
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
      path: '/account/passwordinfo',
      name: 'ユーザパスワード',
      meta: { requiresAuth: true },
      component: () => import('../views/general_user/account_passwordinfo.vue'),
    },
    {
      path: '/account/set-email',
      name: 'SetEmail',
      component: () => import('../views/general_user/account_setEmail.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      meta: { isNotFound: true },
      component: {
        render() {
          return null  // 什么都不渲染
        }
      }
    }
  ],
})


import { useAuth } from '@/scripts/useAuth'
import { useModalManager } from '@/scripts/useModalManager'
const { restoreLogin } = useAuth()
const { openConfirmAsync } = useModalManager()

// ✅ グローバル認証
router.beforeEach(async (to, from, next) => {
  const { user } = useAuth()
  await restoreLogin()

  const proceedNext = (path) => {
    if (path) next(path)
    else next()
  }

  // 先单独处理 404 路由
  if (to.meta.isNotFound) {
    const confirmed = await openConfirmAsync('存在しないページです。ホームに戻りますか？')
    if (confirmed) {
      if (!user.value) {
        return next('/')  // 跳转首页
      } else {
        proceedNext(`/top/${user.value.role}`)
      }

    } else {
      return next(false)  // 阻止跳转
    }
  }

  // 下面是认证逻辑
  const isEmailEmpty = user.value && (!user.value.email || user.value.email.trim() === '')

  if (to.meta.requiresAuth) {
    if (!user.value) {
      proceedNext('/')
      return
    }

    if (to.meta.role_number != null && user.value.role !== to.meta.role_number) {
      proceedNext(`/top/${user.value.role}`)
      return
    }

    if (isEmailEmpty && to.path !== '/account/set-email' && to.path !== '/') {
      const confirmed = await openConfirmAsync('メールアドレスを登録してください。設定画面に移動しますか？')
      if (confirmed) {
        return next('/account/set-email')
      } else {
        return next(false)
      }
    }
  }

  proceedNext()
})


export default router
