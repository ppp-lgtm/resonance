<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { useThemeStore } from '../../stores/theme'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const themeStore = useThemeStore()

const sidebarItems = [
  { name: '仪表盘', path: '/', icon: '📊' },
  { name: '个人信息', path: '/profile', icon: '👤' },
  { name: '技能管理', path: '/skills', icon: '💡' },
  { name: '项目管理', path: '/projects', icon: '🚀' },
  { name: '奖项管理', path: '/awards', icon: '🏆' },
]

const activeItem = computed(() => {
  const currentPath = route.path.replace(/\/$/, '') || '/'
  return sidebarItems.findIndex(item => item.path === currentPath)
})

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="min-h-screen flex bg-light-bg dark:bg-dark-bg">
    <!-- 侧边栏 -->
    <aside class="w-64 bg-white dark:bg-dark-card border-r border-gray-200/50 dark:border-gray-700/50 flex flex-col">
      <!-- Logo -->
      <div class="p-6 border-b border-gray-200/50 dark:border-gray-700/50">
        <h1 class="text-xl font-heading font-bold accent-gradient">Resonance Admin</h1>
      </div>

      <!-- 导航 -->
      <nav class="flex-1 p-4 space-y-1">
        <button
          v-for="(item, i) in sidebarItems"
          :key="item.path"
          @click="router.push(item.path)"
          class="w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium transition-all duration-200"
          :class="activeItem === i
            ? 'bg-gradient-to-r from-violet-600 to-indigo-600 text-white shadow-md shadow-violet-500/20'
            : 'text-gray-600 dark:text-gray-400 hover:bg-violet-50 dark:hover:bg-violet-900/20'"
        >
          <span class="text-lg">{{ item.icon }}</span>
          {{ item.name }}
        </button>
      </nav>

      <!-- 底部操作 -->
      <div class="p-4 border-t border-gray-200/50 dark:border-gray-700/50 space-y-2">
        <button @click="themeStore.toggle()" class="w-full flex items-center gap-3 px-4 py-2.5 rounded-xl text-sm text-gray-600 dark:text-gray-400 hover:bg-violet-50 dark:hover:bg-violet-900/20 transition-colors">
          <span>{{ themeStore.isDark ? '☀️' : '🌙' }}</span>
          {{ themeStore.isDark ? '亮色模式' : '暗色模式' }}
        </button>
        <button @click="handleLogout" class="w-full flex items-center gap-3 px-4 py-2.5 rounded-xl text-sm text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors">
          <span>🚪</span>
          退出登录
        </button>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="flex-1 overflow-auto">
      <!-- 顶部栏 -->
      <header class="glass-nav px-6 py-4 flex items-center justify-between sticky top-0 z-10">
        <h2 class="text-lg font-semibold">
          {{ sidebarItems[activeItem]?.name || '仪表盘' }}
        </h2>
        <div class="flex items-center gap-3">
          <span class="text-sm text-gray-500 dark:text-gray-400">欢迎, {{ authStore.username }}</span>
          <div class="w-8 h-8 rounded-full bg-gradient-to-br from-violet-500 to-indigo-600 flex items-center justify-center text-sm text-white">
            👨‍💻
          </div>
        </div>
      </header>

      <div class="p-6">
        <RouterView />
      </div>
    </main>
  </div>
</template>
