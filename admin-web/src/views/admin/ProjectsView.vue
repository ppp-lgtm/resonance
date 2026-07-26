<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useAppStore } from '../../stores/app'
import { adminApi, type ProjectSaveBody } from '../../api'
import type { Project } from '../../types'

const appStore = useAppStore()
onMounted(() => { appStore.fetchAll() })

const showForm = ref(false)
const editingProject = ref<Project | null>(null)
const submitting = ref(false)
const error = ref('')

/* 表单：title/summary/description + tags + 3 个 URL + 封面（现在是上传图片） */
const PROJECT_STATUS_OPTIONS: Array<{ value: string; label: string; hint: string }> = [
  { value: '',         label: '不设置（前台显示占位符 "—"）', hint: '可选' },
  { value: '已上线',   label: '已上线',     hint: '已交付对外可访问' },
  { value: '筹备中',   label: '筹备中',     hint: '正在设计 / 研发中' },
  { value: '规划中',   label: '规划中',     hint: '立项与需求阶段' },
]
const form = ref<ProjectSaveBody & { tagsText: string; coverUrl: string; coverAlt: string; status: string; completionYearInput: string }>({
  title: '',
  summary: '',
  description: '',
  tags: [],
  tagsText: '',
  images: [],
  githubUrl: '',
  demoUrl: '',
  videoUrl: '',
  isFeatured: false,
  sortOrder: 0,
  coverUrl: '',
  coverAlt: '',
  status: '',
  completionYearInput: '',
})

/* ================= 新增：封面上传（杂志编辑风 + 瑞士设计） ================= */
const coverUploading = ref(false)
const coverDragOver = ref(false)
const coverFileInput = ref<HTMLInputElement | null>(null)

async function onCoverFileSelected(fileList: FileList | null) {
  if (!fileList?.length) return
  const file = fileList[0]
  if (!/^image\//.test(file.type)) { alert('请选择 JPG / PNG / WebP / GIF / SVG 等图片文件'); return }
  if (file.size > 10 * 1024 * 1024) { alert('封面图不得超过 10MB'); return }
  coverUploading.value = true
  try {
    const r = await adminApi.uploadImage(file)
    form.value.coverUrl = r.url
    if (!form.value.coverAlt.trim()) form.value.coverAlt = form.value.title || file.name.replace(/\.[^.]+$/, '')
  } catch (e: any) {
    error.value = `封面上传失败：${e?.message || e}`
  } finally {
    coverUploading.value = false
    coverFileInput.value && (coverFileInput.value.value = '')
  }
}
function onCoverDrop(e: DragEvent) {
  e.preventDefault()
  coverDragOver.value = false
  onCoverFileSelected(e.dataTransfer?.files ?? null)
}
function removeCover() {
  if (coverUploading.value) return
  form.value.coverUrl = ''
  form.value.coverAlt = ''
}
const coverGradientPool = [
  ['#111827', '#B23A2E'],
  ['#0F172A', '#6366F1'],
  ['#18181B', '#0D9488'],
  ['#1C1917', '#D97706'],
]
const coverGradientCss = computed(() => {
  const pick = coverGradientPool[(form.value.title || '').length % coverGradientPool.length]
  return `background-image: linear-gradient(135deg, ${pick[0]} 0%, ${pick[1]} 100%);`
})

const gradients = [
  'from-violet-500 to-purple-600',
  'from-blue-500 to-cyan-600',
  'from-emerald-500 to-teal-600',
  'from-orange-500 to-red-600',
]
const getGradient = (id: number) => gradients[id % gradients.length]

/* ===== URL 校验（正则：http(s) 开头或空）===== */
const URL_RE = /^(https?:\/\/[^\s]+)?$/i

const formErrors = ref<Record<string, string>>({})

function openAdd() {
  editingProject.value = null
  form.value = {
    title: '', summary: '', description: '', tags: [], tagsText: '',
    images: [], githubUrl: '', demoUrl: '', videoUrl: '',
    isFeatured: false, sortOrder: 0, coverUrl: '', coverAlt: '',
    status: '', completionYearInput: '',
  }
  formErrors.value = {}
  error.value = ''
  showForm.value = true
}

function openEdit(project: Project) {
  editingProject.value = project
  const cover = project.images?.[0]
  form.value = {
    title: project.title,
    summary: project.summary,
    description: project.description ?? '',
    tags: project.tags ?? [],
    tagsText: (project.tags ?? []).join(', '),
    images: project.images ?? [],
    githubUrl: project.githubUrl ?? '',
    demoUrl: project.demoUrl ?? '',
    videoUrl: project.videoUrl ?? '',
    isFeatured: !!project.isFeatured,
    sortOrder: project.sortOrder ?? 0,
    coverUrl: cover?.url ?? '',
    coverAlt: cover?.alt ?? project.title,
    status: (project.status ?? '') as string,
    completionYearInput: (project.completionYear != null && project.completionYear !== undefined)
      ? String(project.completionYear)
      : '',
  }
  formErrors.value = {}
  error.value = ''
  showForm.value = true
}

function buildBody(): ProjectSaveBody {
  const tags = form.value.tagsText
    .split(/[,，]/)
    .map(t => t.trim())
    .filter(Boolean)
  const images = form.value.coverUrl.trim()
    ? [{ url: form.value.coverUrl.trim(), alt: form.value.coverAlt.trim() || form.value.title }]
    : []
  const statusRaw = form.value.status ?? ''
  const status = PROJECT_STATUS_OPTIONS.some(o => o.value && o.value === statusRaw) ? statusRaw : ''
  const yearRaw = String(form.value.completionYearInput ?? '').trim()
  let completionYear: number | null = null
  if (yearRaw) {
    const y = Number(yearRaw)
    if (!Number.isNaN(y) && Number.isInteger(y) && y >= 1900 && y <= 2300) completionYear = y
  }
  return {
    title: form.value.title.trim(),
    summary: form.value.summary.trim(),
    description: form.value.description.trim(),
    tags,
    images,
    githubUrl: form.value.githubUrl?.trim() || undefined,
    demoUrl: form.value.demoUrl?.trim() || undefined,
    videoUrl: form.value.videoUrl?.trim() || undefined,
    isFeatured: !!form.value.isFeatured,
    sortOrder: Number(form.value.sortOrder) || 0,
    status: status || undefined,
    completionYear,
  }
}

function validateForm(): boolean {
  const errs: Record<string, string> = {}
  if (!form.value.title.trim()) errs.title = '项目名称不能为空'
  if (!form.value.summary.trim()) errs.summary = '项目简介不能为空'
  if (!form.value.description.trim()) errs.description = '详细描述不能为空'
  const tags = form.value.tagsText
    .split(/[,，]/)
    .map(t => t.trim())
    .filter(Boolean)
  if (tags.length === 0) errs.tags = '至少填一个技术标签'
  if (form.value.githubUrl && !URL_RE.test(form.value.githubUrl)) errs.githubUrl = 'GitHub 地址需以 http:// 或 https:// 开头'
  if (form.value.demoUrl && !URL_RE.test(form.value.demoUrl)) errs.demoUrl = '在线演示地址需以 http:// 或 https:// 开头'
  if (form.value.videoUrl && !URL_RE.test(form.value.videoUrl)) errs.videoUrl = '视频介绍地址需以 http:// 或 https:// 开头'
  if (form.value.status && !PROJECT_STATUS_OPTIONS.some(o => o.value === form.value.status)) {
    errs.status = '请从下拉中选择合法的项目状态'
  }
  const yearRaw = String(form.value.completionYearInput ?? '').trim()
  if (yearRaw) {
    const y = Number(yearRaw)
    if (Number.isNaN(y) || !Number.isInteger(y) || y < 1900 || y > 2300) {
      errs.completionYear = '完成年份为空表示不设置；若填写则必须是 1900 ~ 2300 的整数年份'
    }
  }
  formErrors.value = errs
  return Object.keys(errs).length === 0
}

async function save() {
  if (!validateForm()) return
  submitting.value = true
  error.value = ''
  try {
    const body = buildBody()
    let saved: Project
    if (editingProject.value) {
      saved = await adminApi.updateProject(editingProject.value.id, body)
      const idx = appStore.projects.findIndex(p => p.id === saved.id)
      if (idx !== -1) appStore.projects.splice(idx, 1, saved)
    } else {
      saved = await adminApi.createProject(body)
      appStore.projects.unshift(saved)
    }
    showForm.value = false
  } catch (e: any) {
    error.value = e?.message || '保存失败'
  } finally {
    submitting.value = false
  }
}

async function remove(id: number) {
  if (!confirm('确定要删除这个项目吗？此操作不可恢复。')) return
  try {
    await adminApi.deleteProject(id)
    const idx = appStore.projects.findIndex(p => p.id === id)
    if (idx !== -1) appStore.projects.splice(idx, 1)
  } catch (e: any) {
    alert(e?.message || '删除失败')
  }
}

/* 小工具：首图 URL 预览时的安全回退 */
const projectCover = computed(() => (p: Project) => p.images?.[0]?.url || '')
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-heading font-bold">项目管理</h1>
      <button @click="openAdd" class="btn-primary text-sm !px-4 !py-2">+ 新增项目</button>
    </div>

    <!-- 项目列表 -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <div
        v-for="project in appStore.projects"
        :key="project.id"
        class="card p-0 overflow-hidden group"
      >
        <!-- 封面 -->
        <div :class="'h-40 relative overflow-hidden ' + (projectCover(project) ? '' : 'bg-gradient-to-br ' + getGradient(project.id))">
          <img v-if="projectCover(project)"
               :src="projectCover(project)"
               :alt="project.images?.[0]?.alt || project.title"
               class="w-full h-full object-cover" />
          <div class="absolute inset-0 bg-black/0 group-hover:bg-black/20 transition-colors flex items-center justify-center">
            <span class="text-white text-xl opacity-0 group-hover:opacity-100 transition-opacity">
              {{ project.isFeatured ? '⭐ 精选作品' : '' }}
            </span>
          </div>
          <div v-if="project.isFeatured" class="absolute top-3 right-3 px-2 py-1 bg-white/20 backdrop-blur-sm rounded-full text-xs text-white">
            ⭐ Featured
          </div>
        </div>

        <div class="p-5">
          <div class="flex items-start justify-between gap-3 mb-1">
            <h3 class="font-semibold">{{ project.title }}</h3>
            <span class="text-[10px] font-mono text-gray-400 tabular-nums whitespace-nowrap">
              sort #{{ project.sortOrder ?? 0 }}
            </span>
          </div>
          <p class="text-sm text-gray-500 dark:text-gray-400 mb-3 line-clamp-2">{{ project.summary }}</p>

          <!-- URL 快速预览（这是之前缺失的关键展示点） -->
          <div class="flex flex-wrap gap-2 mb-3 text-xs">
            <a v-if="project.githubUrl" :href="project.githubUrl" target="_blank"
               class="inline-flex items-center gap-1 text-gray-600 dark:text-gray-300 hover:text-violet-600 dark:hover:text-violet-400 underline underline-offset-2">
              <span>🐙</span><span class="max-w-[10rem] truncate">源码</span>
            </a>
            <a v-if="project.demoUrl" :href="project.demoUrl" target="_blank"
               class="inline-flex items-center gap-1 text-gray-600 dark:text-gray-300 hover:text-violet-600 dark:hover:text-violet-400 underline underline-offset-2">
              <span>🌐</span><span class="max-w-[10rem] truncate">演示</span>
            </a>
            <a v-if="project.videoUrl" :href="project.videoUrl" target="_blank"
               class="inline-flex items-center gap-1 text-gray-600 dark:text-gray-300 hover:text-violet-600 dark:hover:text-violet-400 underline underline-offset-2">
              <span>🎬</span><span class="max-w-[10rem] truncate">视频</span>
            </a>
          </div>

          <div class="flex gap-1.5 flex-wrap mb-3">
            <span
              v-for="tag in project.tags.slice(0, 4)"
              :key="tag"
              class="px-2 py-0.5 bg-violet-100 dark:bg-violet-900/30 text-violet-700 dark:text-violet-300 rounded-full text-xs"
            >{{ tag }}</span>
          </div>

          <div class="flex gap-3 mt-4 pt-3 border-t border-gray-100 dark:border-gray-800">
            <button @click="openEdit(project)" class="text-violet-600 dark:text-violet-400 hover:underline text-sm flex-1 text-center">编辑</button>
            <button @click="remove(project.id)" class="text-red-500 hover:underline text-sm flex-1 text-center">删除</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增/编辑 弹窗：Github / 演示 / 视频 三个 URL 全部补齐（核心修复项） -->
    <div v-if="showForm" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div class="absolute inset-0 bg-black/50 backdrop-blur-sm" @click="!submitting && (showForm = false)"></div>
      <div class="relative bg-white dark:bg-dark-card rounded-2xl shadow-2xl w-full max-w-3xl p-6 z-10 max-h-[92vh] overflow-y-auto">
        <h3 class="text-lg font-bold mb-1">{{ editingProject ? '编辑项目' : '新增项目' }}</h3>
        <p class="text-xs text-gray-500 mb-4">
          所有 URL 字段必须以 <code class="px-1 bg-gray-100 dark:bg-gray-800 rounded">http://</code> 或
          <code class="px-1 bg-gray-100 dark:bg-gray-800 rounded">https://</code> 开头；留空表示不展示。
        </p>

        <div v-if="error" class="mb-4 px-3 py-2 rounded-lg bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-300 text-sm">
          {{ error }}
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <!-- 基础信息 -->
          <div class="md:col-span-2">
            <label class="block text-sm font-medium mb-1">项目名称 <span class="text-red-500">*</span></label>
            <input v-model="form.title" type="text"
              :class="['w-full px-4 py-2 rounded-xl border bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500',
                formErrors.title ? 'border-red-400' : 'border-gray-200 dark:border-gray-700']" />
            <p v-if="formErrors.title" class="text-xs text-red-500 mt-1">{{ formErrors.title }}</p>
          </div>

          <div class="md:col-span-2">
            <label class="block text-sm font-medium mb-1">一句话简介 <span class="text-red-500">*</span></label>
            <textarea v-model="form.summary" rows="2"
              :class="['w-full px-4 py-2 rounded-xl border bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500 resize-none',
                formErrors.summary ? 'border-red-400' : 'border-gray-200 dark:border-gray-700']"></textarea>
            <p v-if="formErrors.summary" class="text-xs text-red-500 mt-1">{{ formErrors.summary }}</p>
          </div>

          <div class="md:col-span-2">
            <label class="block text-sm font-medium mb-1">详细描述 <span class="text-red-500">*</span></label>
            <textarea v-model="form.description" rows="5"
              :class="['w-full px-4 py-2 rounded-xl border bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500 resize-y',
                formErrors.description ? 'border-red-400' : 'border-gray-200 dark:border-gray-700']"></textarea>
            <p v-if="formErrors.description" class="text-xs text-red-500 mt-1">{{ formErrors.description }}</p>
          </div>

          <div class="md:col-span-2">
            <label class="block text-sm font-medium mb-1">技术标签（用中文/英文逗号分隔）<span class="text-red-500">*</span></label>
            <input v-model="form.tagsText" type="text" placeholder="Vue 3, TypeScript, Spring Boot"
              :class="['w-full px-4 py-2 rounded-xl border bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500',
                formErrors.tags ? 'border-red-400' : 'border-gray-200 dark:border-gray-700']" />
            <p v-if="formErrors.tags" class="text-xs text-red-500 mt-1">{{ formErrors.tags }}</p>
          </div>

          <!-- 链接组：三个 URL 字段（此前 GitHub 缺失，现已全部补齐 + 校验） -->
          <div class="md:col-span-1">
            <label class="block text-sm font-medium mb-1">
              🐙 GitHub 仓库地址
            </label>
            <input v-model="form.githubUrl" type="url" placeholder="https://github.com/your/project"
              :class="['w-full px-4 py-2 rounded-xl border bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500',
                formErrors.githubUrl ? 'border-red-400' : 'border-gray-200 dark:border-gray-700']" />
            <p v-if="formErrors.githubUrl" class="text-xs text-red-500 mt-1">{{ formErrors.githubUrl }}</p>
          </div>
          <div class="md:col-span-1">
            <label class="block text-sm font-medium mb-1">
              🌐 在线演示 / 官网地址
            </label>
            <input v-model="form.demoUrl" type="url" placeholder="https://your-demo.com"
              :class="['w-full px-4 py-2 rounded-xl border bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500',
                formErrors.demoUrl ? 'border-red-400' : 'border-gray-200 dark:border-gray-700']" />
            <p v-if="formErrors.demoUrl" class="text-xs text-red-500 mt-1">{{ formErrors.demoUrl }}</p>
          </div>
          <div class="md:col-span-2">
            <label class="block text-sm font-medium mb-1">
              🎬 视频介绍链接（B 站 / YouTube 等，留空不展示）
            </label>
            <input v-model="form.videoUrl" type="url" placeholder="https://www.bilibili.com/video/xxx 或 https://youtu.be/xxx"
              :class="['w-full px-4 py-2 rounded-xl border bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500',
                formErrors.videoUrl ? 'border-red-400' : 'border-gray-200 dark:border-gray-700']" />
            <p v-if="formErrors.videoUrl" class="text-xs text-red-500 mt-1">{{ formErrors.videoUrl }}</p>
          </div>

          <!-- 项目状态 + 完成年份 -->
          <div class="md:col-span-1">
            <label class="block text-sm font-medium mb-1">
              📌 项目状态
            </label>
            <select v-model="form.status"
              :class="['w-full px-4 py-2 rounded-xl border bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500 appearance-none pr-9',
                formErrors.status ? 'border-red-400' : 'border-gray-200 dark:border-gray-700']">
              <option
                v-for="opt in PROJECT_STATUS_OPTIONS"
                :key="opt.value || '__empty__'"
                :value="opt.value"
              >
                {{ opt.label }}
              </option>
            </select>
            <p v-if="formErrors.status" class="text-xs text-red-500 mt-1">{{ formErrors.status }}</p>
            <p v-else class="text-[11px] mt-1 text-gray-500 dark:text-gray-400 font-mono tracking-[0.18em] uppercase">
              STATUS · 三选一或留空
            </p>
          </div>
          <div class="md:col-span-1">
            <label class="block text-sm font-medium mb-1">
              🗓 完成年份（留空则前台显示 "—"）
            </label>
            <input v-model="form.completionYearInput" type="number" min="1900" max="2300" step="1"
                   placeholder="例如：2026"
              :class="['w-full px-4 py-2 rounded-xl border bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500 tabular-nums',
                formErrors.completionYear ? 'border-red-400' : 'border-gray-200 dark:border-gray-700']" />
            <p v-if="formErrors.completionYear" class="text-xs text-red-500 mt-1">{{ formErrors.completionYear }}</p>
            <p v-else class="text-[11px] mt-1 text-gray-500 dark:text-gray-400 font-mono tracking-[0.18em] uppercase">
              YEAR · 4 位整数 / 1900 ~ 2300
            </p>
          </div>

          <!-- 图片：封面（手填 URL → 改为本地上传图片，杂志编辑风） -->
          <div class="md:col-span-2">
            <label class="block text-sm font-semibold mb-2 tracking-[0.15em] uppercase">
              <span class="inline-flex items-center gap-2">
                <span class="w-6 h-6 inline-flex items-center justify-center text-[11px] font-mono text-white" style="background:#B23A2E;">01</span>
                封面图 Cover
              </span>
            </label>

            <input
              ref="coverFileInput"
              type="file"
              accept="image/jpeg,image/png,image/webp,image/gif,image/svg+xml"
              class="hidden"
              @change="(e: any) => onCoverFileSelected(e.target?.files)"
            />

            <!-- 主区：封面大卡 16:9 -->
            <label
              class="relative block w-full aspect-[16/9] overflow-hidden rounded-xl cursor-pointer select-none border-2 transition-all duration-300"
              :class="[
                coverDragOver
                  ? 'border-[#B23A2E] shadow-[0_0_0_6px_rgba(178,58,46,0.08)] scale-[1.01]'
                  : form.coverUrl
                    ? 'border-gray-100 dark:border-gray-700'
                    : 'border-dashed border-gray-300 dark:border-gray-600 hover:border-[#B23A2E]/60',
              ]"
              :style="!form.coverUrl ? coverGradientCss : ''"
              @dragover.prevent="coverDragOver = true"
              @dragleave.prevent="coverDragOver = false"
              @drop="onCoverDrop"
              @click="coverUploading ? null : coverFileInput?.click()"
            >
              <!-- 已有图：平铺显示 -->
              <img v-if="form.coverUrl"
                   :src="form.coverUrl"
                   :alt="form.coverAlt || 'cover preview'"
                   class="absolute inset-0 w-full h-full object-cover" />

              <!-- 杂志感「序号 + 背景叠字水印」(空态 / 上传前) -->
              <div v-if="!form.coverUrl"
                   class="absolute inset-0 overflow-hidden pointer-events-none">
                <span
                  class="absolute -left-3 -top-14 font-heading italic text-white/[0.08]"
                  style="font-size: 18rem; line-height: 1; letter-spacing: -0.05em;">
                  COVER
                </span>
                <span
                  class="absolute right-5 bottom-3 font-mono text-white/30 text-[10px] tracking-[0.4em] uppercase">
                  §.01 / project-cover
                </span>
              </div>

              <!-- 左上角：杂志风格「标签条」 -->
              <span
                class="absolute top-4 left-4 inline-flex items-center gap-2 px-3 py-1.5 text-[10px] font-mono tracking-[0.25em] uppercase backdrop-blur"
                :class="form.coverUrl
                  ? 'bg-black/40 text-white border border-white/10'
                  : 'bg-white/10 text-white border border-white/10'">
                <span class="w-1.5 h-1.5 rounded-full"
                      :style="{ background: coverUploading ? '#FACC15' : coverDragOver ? '#fff' : '#B23A2E' }"></span>
                {{ coverUploading ? 'UPLOADING…' : form.coverUrl ? 'READY · 封面图' : 'DRAG & DROP / CLICK' }}
              </span>

              <!-- 右上：删除按钮（有图才显示） -->
              <button
                v-if="form.coverUrl && !coverUploading"
                type="button"
                @click.stop.prevent="removeCover"
                class="group absolute top-4 right-4 w-9 h-9 inline-flex items-center justify-center rounded-full bg-white/95 dark:bg-black/70 text-gray-700 dark:text-white shadow-md hover:bg-[#B23A2E] hover:text-white transition-all duration-200"
                title="移除封面">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" class="w-4 h-4">
                  <path d="M18 6 6 18M6 6l12 12"/>
                </svg>
              </button>

              <!-- 中央：上传中旋转加载 -->
              <div v-if="coverUploading"
                   class="absolute inset-0 flex flex-col items-center justify-center gap-3 bg-black/55 text-white">
                <svg viewBox="0 0 48 48" class="w-12 h-12 animate-spin">
                  <circle cx="24" cy="24" r="18" fill="none" stroke="#ffffff22" stroke-width="5"/>
                  <path d="M24 6a18 18 0 0 1 18 18" stroke="#ffffff" stroke-width="5" stroke-linecap="round"/>
                </svg>
                <span class="font-mono text-[11px] tracking-[0.3em] uppercase">Uploading Cover…</span>
              </div>

              <!-- 中央：空态 + 非上传中 → 大按钮区 + 文案 -->
              <div v-if="!coverUploading && !form.coverUrl"
                   class="absolute inset-0 flex flex-col items-center justify-center gap-4 px-8 text-center text-white">
                <div class="w-16 h-16 rounded-2xl border border-white/20 bg-white/10 backdrop-blur flex items-center justify-center">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" class="w-8 h-8">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                    <polyline points="17 8 12 3 7 8"/>
                    <line x1="12" y1="3" x2="12" y2="15"/>
                  </svg>
                </div>
                <div class="space-y-1">
                  <p class="font-heading text-xl md:text-2xl leading-tight">
                    将封面图<strong class="italic text-white/95">拖到这里</strong>或点击选择
                  </p>
                  <p class="text-[11px] font-mono tracking-[0.2em] uppercase text-white/60">
                    JPG · PNG · WebP · 16:9 最佳 · ≤ 10MB
                  </p>
                </div>
                <span class="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-white text-neutral-900 text-xs font-semibold shadow-lg">
                  <span class="w-2 h-2 rounded-full" style="background:#B23A2E;"></span>
                  选择本地图片
                </span>
              </div>
            </label>

            <!-- alt 文案（SEO 用，放在卡片下方小字行） -->
            <div class="mt-3">
              <label class="block text-[11px] font-mono tracking-[0.2em] uppercase text-gray-500 mb-1.5">
                Alt 文案 · SEO / 无障碍（建议不填则默认为项目标题）
              </label>
              <input v-model="form.coverAlt" type="text" placeholder="例：简历平台项目封面缩略图"
                class="w-full px-3.5 py-2 rounded-lg border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-bg outline-none text-sm focus:ring-2 focus:ring-[#B23A2E]/40 focus:border-[#B23A2E]/60" />
            </div>
          </div>

          <!-- 排序 + 精选 -->
          <div class="md:col-span-1 flex items-center gap-3">
            <label class="block text-sm font-medium mb-1 whitespace-nowrap">⭐ 精选</label>
            <input v-model="form.isFeatured" type="checkbox" class="w-4 h-4 accent-violet-600" />
          </div>
          <div class="md:col-span-1">
            <label class="block text-sm font-medium mb-1">排序序号（小的在前）</label>
            <input v-model.number="form.sortOrder" type="number"
              class="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500" />
          </div>
        </div>

        <div class="flex gap-3 mt-6">
          <button @click="save" :disabled="submitting" class="btn-primary flex-1 text-sm disabled:opacity-60">
            {{ submitting ? '保存中…' : '保存' }}
          </button>
          <button @click="showForm = false" :disabled="submitting" class="btn-outline flex-1 text-sm disabled:opacity-60">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>
