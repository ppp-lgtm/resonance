<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useAppStore } from '../../stores/app'
import { adminApi, type ProfileSaveBody, type UploadResp } from '../../api'

const appStore = useAppStore()
onMounted(() => { if (!appStore.profileAdmin) appStore.fetchAll() })

/* ========== 基础字段 ========== */
const saving = ref(false)
const error = ref('')
const successMsg = ref('')
const titleText = ref('')
const focusAreasText = ref('')

const contactIconOptions = [
  { v: '📧', label: '邮箱' },
  { v: '📱', label: '电话' },
  { v: '💬', label: '微信' },
  { v: '🐙', label: 'GitHub' },
  { v: '💼', label: 'LinkedIn' },
  { v: '🏠', label: '博客' },
  { v: '🎨', label: 'Dribbble' },
  { v: '📦', label: 'NPM' },
  { v: '🔗', label: '通用链接' },
]

watch(() => appStore.profileAdmin, (p) => {
  if (p) {
    titleText.value = (p.title || []).join(' | ')
    focusAreasText.value = (p.focusAreas || []).join('，')
  }
}, { immediate: true })

const p = computed(() => appStore.profileAdmin)

const SPLIT_RE = /[\s,，、|｜\/;；\n\r\t]+/

function parseTags(raw: string | undefined | null): string[] {
  return (raw ?? '')
    .split(SPLIT_RE)
    .map(s => s.trim())
    .filter(Boolean)
}

function syncTitle() {
  if (!appStore.profileAdmin) return
  appStore.profileAdmin.title = parseTags(titleText.value)
}

function syncFocusAreas() {
  if (!appStore.profileAdmin) return
  appStore.profileAdmin.focusAreas = parseTags(focusAreasText.value)
}

function syncTagFields() {
  syncTitle()
  syncFocusAreas()
}

function onTitleInputBlur() {
  syncTitle()
}

function onFocusAreasInputBlur() {
  syncFocusAreas()
}

/* ========== 上传：头像 / 简历 ========== */
const avatarUploading = ref(false)
const resumeUploading = ref(false)
const autoSaving = ref(false)

/**
 * 上传头像/简历后，立即单独持久化到后端
 * （修复：此前仅改 Pinia 内存，用户不点"保存全部"就写不到数据库）
 */
async function persistUploadedProfile(field: 'avatar' | 'resumeUrl') {
  if (!appStore.profileAdmin) return
  syncTagFields()
  autoSaving.value = true
  try {
    const body: ProfileSaveBody = {
      ...appStore.profileAdmin,
      contacts: (appStore.profileAdmin.contacts ?? []).map((c, i) => ({
        ...c,
        id: c.id && Number(c.id) > 0 ? c.id : undefined,
        sortOrder: c.sortOrder ?? i,
        visible: c.visible !== false,
        copyable: !!c.copyable,
      })),
    }
    const saved = await adminApi.saveProfile(body)
    appStore.profileAdmin = saved
    flashSuccess(
      field === 'avatar'
        ? '✓ 头像已上传并保存到数据库'
        : '✓ 简历已上传并保存到数据库，前台可立即下载'
    )
  } catch (e: any) {
    error.value = `上传成功，但保存到数据库失败：${e?.message || e}（请点"保存全部"重试）`
  } finally {
    autoSaving.value = false
  }
}

async function onAvatarChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  avatarUploading.value = true
  error.value = ''
  try {
    const r: UploadResp = await adminApi.uploadImage(file)
    if (appStore.profileAdmin) appStore.profileAdmin.avatar = r.url
    await persistUploadedProfile('avatar')
  } catch (err: any) { error.value = '头像上传失败：' + (err?.message || '')
  } finally { avatarUploading.value = false; input.value = '' }
}

async function onResumeChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  if (!/pdf$/i.test(file.name) && !/^image\//.test(file.type)) {
    alert('简历仅支持 PDF / 图片文件'); input.value = ''; return
  }
  if (file.size > 10 * 1024 * 1024) {
    alert('简历文件不得超过 10MB'); input.value = ''; return
  }
  resumeUploading.value = true
  error.value = ''
  try {
    const r: UploadResp = await adminApi.uploadFile(file)
    if (appStore.profileAdmin) appStore.profileAdmin.resumeUrl = r.url
    await persistUploadedProfile('resumeUrl')
  } catch (err: any) { error.value = '简历上传失败：' + (err?.message || '')
  } finally { resumeUploading.value = false; input.value = '' }
}

/* ========== 联系方式 CRUD ========== */
function addContact() {
  if (!appStore.profileAdmin) return
  const list = appStore.profileAdmin.contacts ??= []
  list.push({
    platform: '邮箱',
    icon: '📧',
    value: '',
    link: '',
    copyable: true,
    visible: true,
    sortOrder: list.length,
  })
}
function removeContact(i: number) {
  if (!appStore.profileAdmin?.contacts) return
  appStore.profileAdmin.contacts.splice(i, 1)
}

/* ========== 保存 ========== */
async function saveAll() {
  if (!appStore.profileAdmin) return
  if (!appStore.profileAdmin.name.trim()) { error.value = '姓名不能为空'; return }
  saving.value = true
  error.value = ''
  try {
    syncTagFields()
    const body: ProfileSaveBody = {
      ...appStore.profileAdmin,
      contacts: (appStore.profileAdmin.contacts ?? []).map((c, i) => ({
        ...c,
        id: c.id && Number(c.id) > 0 ? c.id : undefined,
        sortOrder: c.sortOrder ?? i,
        visible: c.visible !== false,
        copyable: !!c.copyable,
      })),
    }
    const saved = await adminApi.saveProfile(body)
    appStore.profileAdmin = saved
    flashSuccess('个人信息已保存')
  } catch (e: any) {
    error.value = e?.message || '保存失败'
  } finally {
    saving.value = false
  }
}

function flashSuccess(msg: string) {
  successMsg.value = msg
  setTimeout(() => { successMsg.value = '' }, 2200)
}
</script>

<template>
  <div class="max-w-4xl">
    <div class="flex items-center justify-between mb-6 flex-wrap gap-3">
      <h1 class="text-2xl font-heading font-bold">个人信息设置</h1>
      <div class="flex items-center gap-3">
        <span v-if="successMsg" class="text-sm text-emerald-600 dark:text-emerald-400 animate-pulse">✓ {{ successMsg }}</span>
        <span v-if="error" class="text-sm text-red-500">{{ error }}</span>
      </div>
    </div>

    <div v-if="!p && appStore.loading" class="card py-16 text-center text-sm text-gray-400">加载中…</div>

    <template v-else-if="p">
      <!-- 基础资料 -->
      <div class="card space-y-6 mb-6">
        <!-- 头像 + 姓名 + 头衔 -->
        <div class="flex flex-col md:flex-row gap-6 md:items-start">
          <div class="flex flex-col items-center gap-3 shrink-0">
            <label class="relative cursor-pointer group">
              <div v-if="p.avatar" class="w-24 h-24 rounded-full overflow-hidden border-4 border-violet-100 dark:border-violet-900 shadow-md">
                <img :src="p.avatar" alt="avatar" class="w-full h-full object-cover" />
              </div>
              <div v-else class="w-24 h-24 rounded-full bg-gradient-to-br from-violet-500 to-indigo-600 flex items-center justify-center text-4xl shadow-lg text-white">
                {{ (p.name || 'U')[0] }}
              </div>
              <div class="absolute inset-0 rounded-full bg-black/50 text-white text-xs flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                {{ avatarUploading ? '上传中…' : '更换头像' }}
              </div>
              <input type="file" accept="image/*" class="hidden" :disabled="avatarUploading" @change="onAvatarChange">
            </label>
            <span class="text-xs text-gray-400">支持 JPG/PNG/WebP</span>
          </div>

          <div class="flex-1 space-y-4">
            <div>
              <label class="block text-sm font-medium mb-1">姓名 *</label>
              <input v-model="p.name" type="text"
                class="w-full px-4 py-2.5 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500 transition-all" />
            </div>
            <div>
              <label class="block text-sm font-medium mb-1">个人头衔（多个用 | 或 / 分隔，会出现在打字机动画）</label>
              <input v-model="titleText" type="text" placeholder="全栈开发工程师 | UI/UX 设计爱好者 | 开源贡献者"
                @blur="onTitleInputBlur"
                class="w-full px-4 py-2.5 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500 transition-all" />
            </div>
            <div>
              <label class="block text-sm font-medium mb-1">个人 Slogan</label>
              <input v-model="p.slogan" type="text"
                class="w-full px-4 py-2.5 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500 transition-all" />
            </div>
          </div>
        </div>

        <div>
          <label class="block text-sm font-medium mb-1">个人介绍（出现在「关于我」板块尾部）</label>
          <textarea v-model="p.bio" rows="6"
            class="w-full px-4 py-2.5 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500 transition-all resize-none"></textarea>
        </div>

        <div>
          <label class="block text-sm font-medium mb-2">简历 PDF / 图片</label>
          <div class="flex items-center gap-3 flex-wrap">
            <label
              :class="[
                'btn-outline text-sm !px-4 !py-2 cursor-pointer inline-flex items-center gap-2 transition-opacity',
                (resumeUploading || autoSaving) ? 'opacity-60 pointer-events-none' : '',
              ]">
              <span>📄</span>
              <span>
                {{ resumeUploading ? '上传中…' : autoSaving ? '保存中…' : (p.resumeUrl ? '重新上传' : '上传简历') }}
              </span>
              <input type="file" accept=".pdf,image/*" class="hidden" :disabled="resumeUploading || autoSaving" @change="onResumeChange">
            </label>
            <template v-if="p.resumeUrl">
              <a
                :href="p.resumeUrl"
                download
                rel="noopener noreferrer"
                class="btn-primary !py-1.5 !px-3 text-xs inline-flex items-center gap-1.5">
                <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3M3 17V7a2 2 0 012-2h6l2 2h6a2 2 0 012 2v8a2 2 0 01-2 2H5a2 2 0 01-2-2z"/></svg>
                立即下载
              </a>
              <a
                :href="p.resumeUrl"
                target="_blank"
                rel="noopener noreferrer"
                class="text-sm text-violet-600 dark:text-violet-400 hover:underline truncate max-w-[22rem]">
                {{ p.resumeUrl }}
              </a>
              <button
                @click="p.resumeUrl = ''; saveAll();"
                class="text-xs text-red-400 hover:text-red-600 underline underline-offset-2">
                移除并保存
              </button>
            </template>
          </div>
          <p class="text-xs text-gray-400 mt-2">仅支持 PDF 或图片文件，≤10MB。上传成功后会自动保存到数据库，前台「下载简历 PDF」按钮会直接链接到此文件。</p>
        </div>
      </div>

      <!-- 关于我 · 关键信息（前台 About 4 卡片 + FACT_GROUPS 数据来源） -->
      <div class="card space-y-5 mb-6">
        <h3 class="font-semibold flex items-center gap-2 !mb-1">
          <span>🧭</span>关于我 · 关键信息（前台「关于」模块数据来源）
        </h3>

        <div class="grid grid-cols-12 gap-4">
          <div class="col-span-12 md:col-span-6">
            <label class="block text-sm font-medium mb-1">
              模块所在地
              <span class="text-gray-400 font-normal ml-2">（对应 About 模块 "所在城市 / 模块所在地"）</span>
            </label>
            <input v-model="p.location" type="text" placeholder="例如：杭州"
              class="w-full px-4 py-2.5 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500 transition-all" />
          </div>
          <div class="col-span-12 md:col-span-6">
            <label class="block text-sm font-medium mb-1">
              从业经验（年）
              <span class="text-gray-400 font-normal ml-2">（前台会自动展示成「X+ 年」形式）</span>
            </label>
            <input v-model.number="p.yearsExperience" type="number" min="0" step="1" placeholder="例如：5"
              class="w-full px-4 py-2.5 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500 transition-all" />
          </div>
          <div class="col-span-12">
            <label class="block text-sm font-medium mb-1">
              专注方向 / 服务模块（多个用逗号 / 顿号 / 换行分隔，<span class="text-violet-600 dark:text-violet-400">前台 About 4 卡片</span>由此生成）
            </label>
            <textarea v-model="focusAreasText" rows="2"
              @blur="onFocusAreasInputBlur"
              placeholder="服务设计，产品工程，团队协作，持续学习"
              class="w-full px-4 py-2.5 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500 transition-all resize-none"></textarea>
            <p class="text-xs text-gray-400 mt-2">
              已解析到 <span class="font-semibold text-violet-600 dark:text-violet-400">{{ parseTags(focusAreasText).length }}</span>
              个：{{ parseTags(focusAreasText).join('  ·  ') || '（空）' }}
            </p>
          </div>
          <div class="col-span-12">
            <label class="block text-sm font-medium mb-1">
              合作模式
              <span class="text-gray-400 font-normal ml-2">（About 模块最后一组展示。例：远程 / 驻场 / 咨询）</span>
            </label>
            <input v-model="p.workingMode" type="text" placeholder="远程 / 驻场 / 咨询"
              class="w-full px-4 py-2.5 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500 transition-all" />
          </div>
        </div>
      </div>

      <div class="card space-y-4 mb-6">
        <div class="flex items-center justify-between">
          <h3 class="font-semibold flex items-center gap-2">
            <span>📮</span>联系方式（会出现在 Hero 和 About 板块的联络卡片）
          </h3>
          <button @click="addContact" class="btn-primary text-sm !px-3 !py-1.5">+ 新增联系方式</button>
        </div>

        <div v-if="!p.contacts || p.contacts.length === 0" class="py-10 text-center text-sm text-gray-400 border border-dashed border-gray-200 dark:border-gray-700 rounded-xl">
          暂无联系方式，点击右上角「+ 新增联系方式」添加。
        </div>

        <div v-else class="space-y-3">
          <div v-for="(c, i) in p.contacts" :key="i"
            class="grid grid-cols-12 gap-3 items-center p-4 border border-gray-100 dark:border-gray-800 rounded-xl bg-gray-50/40 dark:bg-dark-bg/40">
            <div class="col-span-12 md:col-span-1">
              <select v-model="c.icon" class="w-full px-2 py-2 rounded-lg border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-bg text-lg">
                <option v-for="ic in contactIconOptions" :key="ic.v" :value="ic.v" :title="ic.label">{{ ic.v }}</option>
              </select>
            </div>
            <div class="col-span-12 md:col-span-3">
              <label class="block text-xs text-gray-400 mb-1">平台名称</label>
              <input v-model="c.platform" type="text" placeholder="邮箱 / GitHub / 微信 / 博客…"
                class="w-full px-3 py-2 rounded-lg border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500 text-sm" />
            </div>
            <div class="col-span-12 md:col-span-4">
              <label class="block text-xs text-gray-400 mb-1">内容（显示给访客的值）</label>
              <input v-model="c.value" type="text" placeholder="示例：hello@example.com"
                class="w-full px-3 py-2 rounded-lg border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500 text-sm" />
            </div>
            <div class="col-span-12 md:col-span-3">
              <label class="block text-xs text-gray-400 mb-1">跳转链接（可选，空则不跳转）</label>
              <input v-model="c.link" type="url" placeholder="https://...  留空=无链接"
                class="w-full px-3 py-2 rounded-lg border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500 text-sm" />
            </div>
            <div class="col-span-6 md:col-span-1 flex items-end gap-2 justify-end">
              <label class="inline-flex items-center gap-1 text-xs text-gray-500" title="允许访客一键复制此内容">
                <input v-model="c.copyable" type="checkbox" class="w-4 h-4 accent-violet-600">
                可复制
              </label>
            </div>
            <div class="col-span-6 md:col-span-0 flex justify-end">
              <button @click="removeContact(i)" class="text-red-400 hover:text-red-600 text-sm">🗑️</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 提交按钮 -->
      <div class="flex items-center justify-end gap-3">
        <button @click="appStore.fetchAll()" class="btn-outline text-sm disabled:opacity-60" :disabled="saving">
          🔄 重置为后端最新
        </button>
        <button @click="saveAll()" :disabled="saving" class="btn-primary disabled:opacity-60">
          {{ saving ? '保存中…' : '💾 保存全部' }}
        </button>
      </div>
    </template>
  </div>
</template>
