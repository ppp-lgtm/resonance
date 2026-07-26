<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useAppStore } from '../../stores/app'
import { adminApi, type DashboardStats, type ProfileSaveBody, type UploadResp } from '../../api'

const appStore = useAppStore()
onMounted(() => { appStore.fetchAll(); loadStats() })

const stats = ref<DashboardStats | null>(null)
const statCards = computed(() => [
  { label: '项目总数', value: stats.value?.projectCount ?? appStore.projects.length, icon: '🚀', accent: 'from-violet-500 to-indigo-500' },
  { label: '技能数量', value: stats.value?.skillCount ?? appStore.skills.length, icon: '💡', accent: 'from-blue-500 to-cyan-500' },
  { label: '奖项数量', value: stats.value?.awardCount ?? appStore.awards.length, icon: '🏆', accent: 'from-emerald-500 to-teal-500' },
  { label: '教育经历', value: stats.value?.educationCount ?? appStore.education.length, icon: '🎓', accent: 'from-orange-500 to-red-500' },
  { label: '联系方式', value: stats.value?.contactCount ?? appStore.contacts.length, icon: '📮', accent: 'from-pink-500 to-rose-500' },
])

async function loadStats() {
  try {
    stats.value = await adminApi.dashboard()
  } catch {
    // fallback：用 store 里已有的 count
  }
}

/* ===== 上传：头像 / 简历 PDF（通过 /api/admin/upload/*）===== */
const avatarUploading = ref(false)
const resumeUploading = ref(false)

async function onAvatarChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  avatarUploading.value = true
  try {
    const r: UploadResp = await adminApi.uploadImage(file)
    if (appStore.profileAdmin) {
      appStore.profileAdmin.avatar = r.url
      // 立即持久化（PUT /api/admin/profile）
      await saveProfileLight('avatar')
    }
  } catch (err: any) { alert('头像上传失败：' + (err?.message || ''))
  } finally { avatarUploading.value = false; input.value = '' }
}

async function onResumeChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  if (!/pdf$/i.test(file.name) && !/^image\//.test(file.type)) {
    alert('简历仅支持 PDF / 图片文件')
    input.value = ''
    return
  }
  resumeUploading.value = true
  try {
    const r: UploadResp = await adminApi.uploadFile(file)
    if (appStore.profileAdmin) {
      appStore.profileAdmin.resumeUrl = r.url
      await saveProfileLight('resumeUrl')
    }
  } catch (err: any) { alert('简历上传失败：' + (err?.message || ''))
  } finally { resumeUploading.value = false; input.value = '' }
}

/* ===== Profile 编辑表单（真实 PUT /api/admin/profile）===== */
const savingProfile = ref(false)
const profileError = ref('')
const titleText = ref('')

watch(() => appStore.profileAdmin, (p) => {
  if (p) titleText.value = (p.title || []).join(' | ')
}, { immediate: true })

function syncTitle() {
  if (!appStore.profileAdmin) return
  appStore.profileAdmin.title = titleText.value
    .split(/[|｜、,，\/]/)
    .map(s => s.trim())
    .filter(Boolean)
}

async function saveProfileLight(_field?: 'avatar' | 'resumeUrl') {
  if (!appStore.profileAdmin) return
  savingProfile.value = true
  profileError.value = ''
  try {
    syncTitle()
    const body: ProfileSaveBody = { ...appStore.profileAdmin }
    const saved = await adminApi.saveProfile(body)
    appStore.profileAdmin = saved
    if (_field !== 'avatar' && _field !== 'resumeUrl') alert('个人信息保存成功')
  } catch (e: any) {
    profileError.value = e?.message || '保存失败'
  } finally { savingProfile.value = false }
}
</script>

<template>
  <div>
    <h1 class="text-2xl font-heading font-bold mb-6">仪表盘</h1>

    <!-- 统计卡片（真实：adminApi.dashboard） -->
    <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-4 md:gap-6 mb-8">
      <div
        v-for="(stat, idx) in statCards"
        :key="stat.label"
        class="card p-0 overflow-hidden group"
      >
        <div :class="'h-1.5 bg-gradient-to-r ' + stat.accent"></div>
        <div class="p-5">
          <div class="flex items-center justify-between mb-4">
            <span class="text-3xl">{{ stat.icon }}</span>
            <span class="text-3xl font-bold tabular-nums">{{ stat.value }}</span>
          </div>
          <div class="flex items-center justify-between">
            <p class="text-sm text-gray-500 dark:text-gray-400">{{ stat.label }}</p>
            <span class="font-mono text-[10px] text-gray-300 dark:text-gray-600">0{{ idx + 1 }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 双栏：快速操作 + Profile 快编 -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <div class="card">
        <h3 class="font-semibold mb-3 flex items-center gap-2">
          <span>⚡</span>快速操作
        </h3>
        <div class="space-y-2">
          <RouterLink to="/projects" class="block px-4 py-2.5 rounded-lg bg-violet-50 dark:bg-violet-900/20 text-violet-700 dark:text-violet-300 hover:bg-violet-100 dark:hover:bg-violet-900/30 transition-colors text-sm flex items-center gap-2">
            <span>➕</span>新增项目
          </RouterLink>
          <RouterLink to="/skills" class="block px-4 py-2.5 rounded-lg bg-blue-50 dark:bg-blue-900/20 text-blue-700 dark:text-blue-300 hover:bg-blue-100 dark:hover:bg-blue-900/30 transition-colors text-sm flex items-center gap-2">
            <span>➕</span>新增技能
          </RouterLink>
          <RouterLink to="/awards" class="block px-4 py-2.5 rounded-lg bg-emerald-50 dark:bg-emerald-900/20 text-emerald-700 dark:text-emerald-300 hover:bg-emerald-100 dark:hover:bg-emerald-900/30 transition-colors text-sm flex items-center gap-2">
            <span>➕</span>新增奖项
          </RouterLink>
          <RouterLink to="/profile" class="block px-4 py-2.5 rounded-lg bg-orange-50 dark:bg-orange-900/20 text-orange-700 dark:text-orange-300 hover:bg-orange-100 dark:hover:bg-orange-900/30 transition-colors text-sm flex items-center gap-2">
            <span>✏️</span>编辑个人信息
          </RouterLink>
        </div>
      </div>

      <!-- Profile 快捷编辑：头像 / 简历 PDF / 姓名 / 头衔 都能立即保存（真实 PUT /api/admin/profile） -->
      <div class="card">
        <h3 class="font-semibold mb-4 flex items-center gap-2">
          <span>👤</span>个人信息·快捷编辑
        </h3>

        <div v-if="!appStore.profileAdmin && appStore.loading" class="text-sm text-gray-400 py-10 text-center">加载中…</div>
        <template v-else-if="appStore.profileAdmin">
          <div class="flex items-center gap-5 mb-5">
            <label class="relative cursor-pointer group">
              <div v-if="appStore.profileAdmin.avatar" class="w-20 h-20 rounded-full overflow-hidden border-2 border-violet-100 dark:border-violet-900 shadow-sm">
                <img :src="appStore.profileAdmin.avatar" alt="avatar" class="w-full h-full object-cover" />
              </div>
              <div v-else class="w-20 h-20 rounded-full bg-gradient-to-br from-violet-500 to-indigo-600 flex items-center justify-center text-3xl shadow-lg text-white">
                {{ (appStore.profileAdmin.name || 'U')[0] }}
              </div>
              <div class="absolute inset-0 rounded-full bg-black/40 text-white text-xs flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                {{ avatarUploading ? '上传中…' : '更换头像' }}
              </div>
              <input type="file" accept="image/*" class="hidden" :disabled="avatarUploading" @change="onAvatarChange">
            </label>
            <div class="flex-1 space-y-2">
              <div>
                <label class="block text-xs font-medium text-gray-500 mb-1">姓名</label>
                <input v-model="appStore.profileAdmin.name" type="text"
                  class="w-full px-3 py-1.5 rounded-lg border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500 text-sm" />
              </div>
              <div>
                <label class="block text-xs font-medium text-gray-500 mb-1">头衔（用「| / , 」分隔多个）</label>
                <input v-model="titleText" type="text" placeholder="全栈工程师 | UI/UX 设计爱好者"
                  class="w-full px-3 py-1.5 rounded-lg border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500 text-sm" />
              </div>
            </div>
          </div>

          <div class="space-y-2 mb-4">
            <div>
              <label class="block text-xs font-medium text-gray-500 mb-1">Slogan</label>
              <input v-model="appStore.profileAdmin.slogan" type="text"
                class="w-full px-3 py-1.5 rounded-lg border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500 text-sm" />
            </div>
            <div>
              <label class="block text-xs font-medium text-gray-500 mb-1">个人介绍</label>
              <textarea v-model="appStore.profileAdmin.bio" rows="3"
                class="w-full px-3 py-1.5 rounded-lg border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500 text-sm resize-none"></textarea>
            </div>
            <div>
              <label class="block text-xs font-medium text-gray-500 mb-1">简历 PDF</label>
              <div class="flex items-center gap-2 flex-wrap">
                <label class="btn-outline text-sm !px-3 !py-1.5 cursor-pointer inline-flex items-center gap-1">
                  <span>📄</span>
                  <span>{{ resumeUploading ? '上传中…' : (appStore.profileAdmin.resumeUrl ? '重新上传' : '上传文件') }}</span>
                  <input type="file" accept=".pdf,image/*" class="hidden" :disabled="resumeUploading" @change="onResumeChange">
                </label>
                <a v-if="appStore.profileAdmin.resumeUrl" :href="appStore.profileAdmin.resumeUrl" target="_blank" rel="noopener noreferrer"
                  class="text-xs text-violet-600 dark:text-violet-400 hover:underline truncate max-w-[16rem]">
                  {{ appStore.profileAdmin.resumeUrl }}
                </a>
              </div>
            </div>
          </div>

          <p v-if="profileError" class="mb-3 text-sm text-red-500">{{ profileError }}</p>

          <button @click="saveProfileLight()" :disabled="savingProfile"
            class="btn-primary w-full text-sm disabled:opacity-60">
            {{ savingProfile ? '保存中…' : '💾 保存个人信息' }}
          </button>
        </template>
      </div>
    </div>
  </div>
</template>
