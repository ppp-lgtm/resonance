import { createRouter, createWebHistory } from 'vue-router'
import { authApi } from '../api'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/admin/LoginView.vue'),
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/admin/RegisterView.vue'),
    },
    {
      path: '/',
      component: () => import('../views/admin/AdminLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'dashboard',
          component: () => import('../views/admin/DashboardView.vue'),
        },
        {
          path: 'profile',
          name: 'profile-edit',
          component: () => import('../views/admin/ProfileEditView.vue'),
        },
        {
          path: 'skills',
          name: 'skills',
          component: () => import('../views/admin/SkillsView.vue'),
        },
        {
          path: 'projects',
          name: 'projects',
          component: () => import('../views/admin/ProjectsView.vue'),
        },
        {
          path: 'awards',
          name: 'awards',
          component: () => import('../views/admin/AwardsView.vue'),
        },
      ],
    },
  ],
})

router.beforeEach(async (to) => {
  const token = localStorage.getItem('admin_token')

  // 需要登录的页面：未登录则跳转登录页
  if (to.meta.requiresAuth && !token) {
    return { name: 'login' }
  }

  // 已登录用户访问登录/注册页 → 跳到首页
  if (token && (to.name === 'login' || to.name === 'register')) {
    return { name: 'dashboard' }
  }

  // 访问登录页时，检查管理员初始化状态
  if (to.name === 'login') {
    try {
      const status = await authApi.getStatus()
      if (!status.hasAdmin) {
        // 管理员表为空 → 强制跳转注册页
        return { name: 'register' }
      }
    } catch {
      // 接口不可用时（后端未启动 / 反代配错 / 405 等）→ 更安全的兜底：跳注册页
      // 为什么不留在登录页？—— 因为空数据库时登录永远失败，用户完全无法进入系统；
      // 而注册页 onMounted 会再次独立调用 getStatus()，若后端已经有管理员会再跳回登录页，
      // 同时后端 AuthService.register() 本身也校验了「管理员表为空才能注册」，不会重复创建
      return { name: 'register' }
    }
  }

  // 访问注册页时，检查管理员初始化状态
  if (to.name === 'register') {
    try {
      const status = await authApi.getStatus()
      if (status.hasAdmin) {
        // 管理员已存在 → 跳转登录页
        return { name: 'login' }
      }
    } catch {
      // 接口不可用时保持当前路由（注册页）
      // 不做跳转 — 注册页 onMounted 会再查一次，后端有管理员就会自己跳回登录页
    }
  }
})

export default router
