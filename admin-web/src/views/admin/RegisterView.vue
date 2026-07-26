<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { authApi } from '../../api'

const router = useRouter()
const authStore = useAuthStore()
const username = ref('')
const password = ref('')
const confirmPassword = ref('')
const error = ref('')
const submitting = ref(false)
const systemStatus = ref<{ hasAdmin: boolean; adminCount: number }>({
  hasAdmin: false,
  adminCount: 0,
})
const loadingStatus = ref(true)

/* 检查管理员初始化状态 */
onMounted(async () => {
  try {
    const status = await authApi.getStatus()
    systemStatus.value = status
    // 如果管理员已存在，跳转到登录页
    if (status.hasAdmin) {
      router.push('/login')
    }
  } catch {
    systemStatus.value = { hasAdmin: false, adminCount: 0 }
  } finally {
    loadingStatus.value = false
  }
})

async function handleRegister() {
  if (!username.value || !password.value) {
    error.value = '请填写用户名和密码'
    return
  }
  if (password.value !== confirmPassword.value) {
    error.value = '两次输入的密码不一致'
    return
  }
  if (password.value.length < 6) {
    error.value = '密码长度不能少于 6 位'
    return
  }
  submitting.value = true
  error.value = ''
  try {
    const resp = await authApi.register({
      username: username.value.trim(),
      password: password.value,
    })
    authStore.login(resp.token, resp.user?.username || username.value.trim())
    localStorage.setItem('admin_expire_at', String(Date.now() + resp.expiresIn * 1000))
    router.push('/')
  } catch (e: any) {
    error.value = e?.message || '注册失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-violet-50 via-white to-indigo-50 dark:from-dark-bg dark:via-dark dark:to-dark-card relative overflow-hidden">
    <!-- 背景装饰 -->
    <div class="absolute top-0 right-0 w-96 h-96 bg-violet-500/5 rounded-[32px] -translate-y-1/2 translate-x-1/2 rotate-12"></div>
    <div class="absolute bottom-0 left-0 w-96 h-96 bg-indigo-500/5 rounded-[32px] translate-y-1/2 -translate-x-1/2 -rotate-12"></div>

    <div class="card w-full max-w-md mx-4 relative z-10">
      <div class="text-center mb-8">
        <div class="text-5xl mb-4">📝</div>
        <h1 class="text-2xl font-heading font-bold accent-gradient">创建管理账号</h1>
        <p v-if="!systemStatus.hasAdmin" class="text-sm text-gray-500 mt-2">
          首次启动，请创建唯一的管理员账号（创建后不可再注册）
        </p>
        <p v-else class="text-sm text-amber-600 mt-2">
          管理员账号已存在，无法注册
        </p>
      </div>

      <form @submit.prevent="handleRegister" class="space-y-4">
        <div>
          <label class="block text-sm font-medium mb-1">用户名</label>
          <input v-model="username" type="text" placeholder="请输入用户名" :disabled="submitting || systemStatus.hasAdmin"
            class="w-full px-4 py-3 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-card focus:ring-2 focus:ring-violet-500 focus:border-transparent outline-none transition-all disabled:opacity-60" />
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">密码</label>
          <input v-model="password" type="password" placeholder="至少 6 位" :disabled="submitting || systemStatus.hasAdmin"
            class="w-full px-4 py-3 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-card focus:ring-2 focus:ring-violet-500 focus:border-transparent outline-none transition-all disabled:opacity-60" />
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">确认密码</label>
          <input v-model="confirmPassword" type="password" placeholder="再次输入密码" :disabled="submitting || systemStatus.hasAdmin"
            @keydown.enter="handleRegister"
            class="w-full px-4 py-3 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-card focus:ring-2 focus:ring-violet-500 focus:border-transparent outline-none transition-all disabled:opacity-60" />
        </div>

        <p v-if="error" class="text-red-500 text-sm">{{ error }}</p>

        <button type="submit" :disabled="submitting || systemStatus.hasAdmin" class="btn-primary w-full disabled:opacity-60">
          {{ submitting ? '注册中…' : '创建账号' }}
        </button>
      </form>

      <div class="mt-4 text-center">
        <router-link to="/login" class="text-sm text-violet-600 dark:text-violet-400 hover:underline">
          返回登录
        </router-link>
      </div>
    </div>
  </div>
</template>
