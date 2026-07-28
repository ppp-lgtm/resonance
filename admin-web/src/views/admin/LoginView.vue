<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { authApi } from '../../api'

const router = useRouter()
const authStore = useAuthStore()
const username = ref('')
const password = ref('')
const error = ref('')
const submitting = ref(false)
const systemStatus = ref<{ hasAdmin: boolean; adminCount: number }>({
  hasAdmin: false,
  adminCount: 0,
})
const loadingStatus = ref(true)

/* 检查管理员初始化状态，控制注册/登录通道 */
onMounted(async () => {
  try {
    const status = await authApi.getStatus()
    systemStatus.value = status
    // 如果管理员表为空，跳转到注册页
    if (!status.hasAdmin) {
      router.push('/register')
    }
  } catch {
    // 接口不可用时（后端未启动/405/反代配错等）→ 更安全地跳注册页
    // 理由同 router/index.ts 守卫的 catch：管理员表为空时登录表单永远不可用，
    // 但注册页自己会再查一次 status + 后端也会做注册时表非空校验，双保险不会重复建管理员
    router.push('/register')
  } finally {
    loadingStatus.value = false
  }
})

/* 真实登录 → 请求 /api/auth/login → 后端签发 JWT */
async function handleLogin() {
  if (!username.value || !password.value) {
    error.value = '请输入用户名和密码'
    return
  }
  submitting.value = true
  error.value = ''
  try {
    const resp = await authApi.login({
      username: username.value.trim(),
      password: password.value,
    })
    authStore.login(resp.token, resp.user?.username || username.value.trim())
    localStorage.setItem('admin_expire_at', String(Date.now() + resp.expiresIn * 1000))
    router.push('/')
  } catch (e: any) {
    error.value = e?.message || '用户名或密码错误'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-violet-50 via-white to-indigo-50 dark:from-dark-bg dark:via-dark-bg dark:to-dark-card relative overflow-hidden">
    <!-- 背景装饰 -->
    <div class="absolute top-0 right-0 w-96 h-96 bg-violet-500/5 rounded-[32px] -translate-y-1/2 translate-x-1/2 rotate-12"></div>
    <div class="absolute bottom-0 left-0 w-96 h-96 bg-indigo-500/5 rounded-[32px] translate-y-1/2 -translate-x-1/2 -rotate-12"></div>

    <div class="card w-full max-w-md mx-4 relative z-10">
      <div class="text-center mb-8">
        <div class="text-5xl mb-4">🔐</div>
        <h1 class="text-2xl font-heading font-bold accent-gradient">Resonance 后台管理</h1>
        <p v-if="!systemStatus.hasAdmin" class="text-sm text-amber-600 mt-2">
          系统尚未初始化，请先创建管理员账号
        </p>
        <p v-else class="text-sm text-gray-500 mt-2">请登录以继续（后端 JWT 鉴权）</p>
      </div>

      <form @submit.prevent="handleLogin" class="space-y-4">
        <div>
          <label class="block text-sm font-medium mb-1">用户名</label>
          <input v-model="username" type="text" placeholder="请输入用户名" :disabled="submitting"
            class="w-full px-4 py-3 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-card focus:ring-2 focus:ring-violet-500 focus:border-transparent outline-none transition-all disabled:opacity-60" />
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">密码</label>
          <input v-model="password" type="password" placeholder="请输入密码" :disabled="submitting"
            @keydown.enter="handleLogin"
            class="w-full px-4 py-3 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-card focus:ring-2 focus:ring-violet-500 focus:border-transparent outline-none transition-all disabled:opacity-60" />
        </div>

        <p v-if="error" class="text-red-500 text-sm">{{ error }}</p>

        <button type="submit" :disabled="submitting" class="btn-primary w-full disabled:opacity-60">
          {{ submitting ? '登录中…' : '登 录' }}
        </button>
      </form>

      <!-- 注册通道：仅当管理员表为空时显示 -->
      <div v-if="!systemStatus.hasAdmin" class="mt-4 text-center">
        <router-link to="/register" class="text-sm text-violet-600 dark:text-violet-400 hover:underline">
          还没有账号？创建管理员账号
        </router-link>
      </div>

      <div v-if="systemStatus.hasAdmin" class="mt-6 pt-4 border-t border-gray-100 dark:border-gray-800 text-xs text-gray-400 space-y-1">
        <p>• 后端路径：<code class="px-1 bg-gray-100 dark:bg-gray-800 rounded">POST /api/auth/login</code></p>
      </div>
    </div>
  </div>
</template>
