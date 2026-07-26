<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useAppStore } from '../../stores/app'
import { adminApi, type AwardSaveBody, type EducationSaveBody } from '../../api'
import type { Award, Education } from '../../types'

const appStore = useAppStore()
onMounted(() => { appStore.fetchAll() })

const showForm = ref(false)
const formType = ref<'award' | 'education'>('award')
const editingAwardId = ref<number | null>(null)
const editingEduId = ref<number | null>(null)
const submitting = ref(false)
const error = ref('')

/* ========= 新增：奖项封面上传（杂志编辑风 + 瑞士设计） ========= */
const awardCoverUploading = ref(false)
const awardCoverDragOver = ref(false)
const awardCoverFileInput = ref<HTMLInputElement | null>(null)
async function onAwardCoverSelected(fileList: FileList | null) {
  if (!fileList?.length) return
  const file = fileList[0]
  if (!/^image\//.test(file.type)) { alert('请选择 JPG / PNG / WebP / GIF / SVG 等图片文件'); return }
  if (file.size > 10 * 1024 * 1024) { alert('封面图不得超过 10MB'); return }
  awardCoverUploading.value = true
  try {
    const r = await adminApi.uploadImage(file)
    awardForm.value.coverUrl = r.url
  } catch (e: any) {
    error.value = `封面上传失败：${e?.message || e}`
  } finally {
    awardCoverUploading.value = false
    awardCoverFileInput.value && (awardCoverFileInput.value.value = '')
  }
}
function onAwardCoverDrop(e: DragEvent) {
  e.preventDefault()
  awardCoverDragOver.value = false
  onAwardCoverSelected(e.dataTransfer?.files ?? null)
}
function removeAwardCover() {
  if (awardCoverUploading.value) return
  awardForm.value.coverUrl = ''
}
const awardCoverGradientPool = [
  ['#3F2A1A', '#C2410C'],
  ['#164E63', '#0891B2'],
  ['#3B1D50', '#9333EA'],
  ['#1E293B', '#B23A2E'],
]
const awardCoverGradientCss = computed(() => {
  const pick = awardCoverGradientPool[(awardForm.value.title || '').length % awardCoverGradientPool.length]
  return `background-image: linear-gradient(135deg, ${pick[0]} 0%, ${pick[1]} 100%);`
})

/* ===== URL 校验：必须 http(s) 或空 ===== */
const URL_RE = /^(https?:\/\/[^\s]+)?$/i

const awardForm = ref<AwardSaveBody & { certUrlErr: string; coverUrl: string }>({
  title: '', issuer: '', date: '', description: '',
  certificateUrl: '', coverUrl: '', certUrlErr: '',
})

const eduForm = ref<EducationSaveBody>({
  school: '', degree: '', major: '', startDate: '', endDate: '', description: '',
})

function resetForms() {
  awardForm.value = { title: '', issuer: '', date: '', description: '', certificateUrl: '', coverUrl: '', certUrlErr: '' }
  eduForm.value = { school: '', degree: '学士', major: '', startDate: '', endDate: '', description: '' }
  editingAwardId.value = null
  editingEduId.value = null
  error.value = ''
  awardCoverUploading.value = false
}

function openAdd(type: 'award' | 'education') {
  formType.value = type
  resetForms()
  showForm.value = true
}

function openEditAward(a: Award) {
  formType.value = 'award'
  resetForms()
  editingAwardId.value = a.id
  awardForm.value = {
    title: a.title,
    issuer: a.issuer,
    date: a.date,
    description: a.description ?? '',
    certificateUrl: (a as any).certificateUrl ?? (a as any).certUrl ?? '',
    coverUrl: (a as any).coverUrl ?? '',
    certUrlErr: '',
  }
  showForm.value = true
}

function openEditEducation(e: Education) {
  formType.value = 'education'
  resetForms()
  editingEduId.value = e.id
  eduForm.value = {
    school: e.school,
    degree: e.degree,
    major: e.major,
    startDate: e.startDate,
    endDate: e.endDate,
    description: e.description ?? '',
  }
  showForm.value = true
}

function validateAward(): boolean {
  if (!awardForm.value.title.trim()) { error.value = '奖项名称不能为空'; return false }
  if (!awardForm.value.issuer.trim()) { error.value = '颁发机构不能为空'; return false }
  if (!awardForm.value.date.trim()) { error.value = '获得时间不能为空'; return false }
  const u = awardForm.value.certificateUrl ?? ''
  if (u && !URL_RE.test(u)) {
    awardForm.value.certUrlErr = '证书链接必须以 http:// 或 https:// 开头'
    return false
  }
  awardForm.value.certUrlErr = ''
  return true
}

async function saveAward() {
  if (!validateAward()) return
  submitting.value = true
  try {
    const body: AwardSaveBody = {
      title: awardForm.value.title.trim(),
      issuer: awardForm.value.issuer.trim(),
      date: awardForm.value.date.trim(),
      description: awardForm.value.description?.trim() || undefined,
      certificateUrl: awardForm.value.certificateUrl?.trim() || undefined,
      coverUrl: awardForm.value.coverUrl?.trim() || undefined,
    }
    let saved: Award
    if (editingAwardId.value) {
      saved = await adminApi.updateAward(editingAwardId.value, body)
      const idx = appStore.awards.findIndex(a => a.id === saved.id)
      if (idx !== -1) appStore.awards.splice(idx, 1, saved)
    } else {
      saved = await adminApi.createAward(body)
      appStore.awards.unshift(saved)
    }
    showForm.value = false
  } catch (e: any) { error.value = e?.message || '保存失败'
  } finally { submitting.value = false }
}

async function removeAwards(id: number) {
  if (!confirm('确定要删除这个奖项吗？')) return
  try {
    await adminApi.deleteAward(id)
    const idx = appStore.awards.findIndex(a => a.id === id)
    if (idx !== -1) appStore.awards.splice(idx, 1)
  } catch (e: any) { alert(e?.message || '删除失败') }
}

async function saveEducation() {
  if (!eduForm.value.school.trim()) { error.value = '学校不能为空'; return }
  if (!eduForm.value.degree.trim()) { error.value = '学位不能为空'; return }
  if (!eduForm.value.major.trim()) { error.value = '专业不能为空'; return }
  if (!eduForm.value.startDate.trim() || !(eduForm.value.endDate ?? '').trim()) { error.value = '起止时间不能为空'; return }
  eduForm.value.endDate = eduForm.value.endDate ?? ''
  submitting.value = true
  try {
    const body: EducationSaveBody = { ...eduForm.value }
    let saved: Education
    if (editingEduId.value) {
      saved = await adminApi.updateEducation(editingEduId.value, body)
      const idx = appStore.education.findIndex(e => e.id === saved.id)
      if (idx !== -1) appStore.education.splice(idx, 1, saved)
    } else {
      saved = await adminApi.createEducation(body)
      appStore.education.unshift(saved)
    }
    showForm.value = false
  } catch (e: any) { error.value = e?.message || '保存失败'
  } finally { submitting.value = false }
}

async function removeEducation(id: number) {
  if (!confirm('确定要删除这条教育经历吗？')) return
  try {
    await adminApi.deleteEducation(id)
    const idx = appStore.education.findIndex(e => e.id === id)
    if (idx !== -1) appStore.education.splice(idx, 1)
  } catch (e: any) { alert(e?.message || '删除失败') }
}

const sortedAwards = computed(() =>
  [...appStore.awards].sort((a, b) => (b.date || '').localeCompare(a.date || '')))
const sortedEdu = computed(() =>
  [...appStore.education].sort((a, b) => (b.endDate || '').localeCompare(a.endDate || '')))
</script>

<template>
  <div>
    <h1 class="text-2xl font-heading font-bold mb-6">奖项与教育管理</h1>

    <div class="flex gap-3 mb-8">
      <button @click="openAdd('award')" class="btn-primary text-sm !px-4 !py-2">+ 新增奖项</button>
      <button @click="openAdd('education')" class="btn-outline text-sm !px-4 !py-2">+ 新增教育经历</button>
    </div>

    <!-- 奖项列表：增加 certUrl 链接展示（修复此前的 URL 类字段遗漏） -->
    <div class="mb-10">
      <h2 class="text-lg font-semibold mb-4 flex items-center gap-2">
        <span>🏆</span> 奖项列表（{{ appStore.awards.length }}）
      </h2>
      <div class="space-y-3">
        <div v-for="award in sortedAwards" :key="award.id"
          class="card p-4 flex items-start justify-between group gap-4">
          <div class="flex-1 min-w-0">
            <div class="flex items-start gap-4 mb-1 flex-wrap">
              <div v-if="award.coverUrl" class="shrink-0">
                <a :href="award.coverUrl" target="_blank" rel="noopener noreferrer"
                   class="block w-20 h-20 md:w-24 md:h-24 rounded-lg overflow-hidden border border-gray-100 dark:border-gray-700 shadow-sm hover:shadow-md transition-shadow bg-gray-50 dark:bg-gray-900">
                  <img :src="award.coverUrl" :alt="award.title + ' 封面'" class="w-full h-full object-cover" />
                </a>
              </div>
              <div class="min-w-0 flex-1">
                <div class="flex items-center gap-3 mb-1 flex-wrap">
                  <h4 class="font-semibold">{{ award.title }}</h4>
                  <span class="text-xs text-gray-500 font-mono">{{ award.date }}</span>
                </div>
                <p class="text-sm text-violet-600 dark:text-violet-400">{{ award.issuer }}</p>
                <p v-if="award.description" class="text-sm text-gray-500 dark:text-gray-400 mt-1 line-clamp-2">{{ award.description }}</p>
                <div v-if="(award as any).certificateUrl || (award as any).certUrl" class="mt-2">
                  <a :href="(award as any).certificateUrl || (award as any).certUrl" target="_blank" rel="noopener noreferrer"
                    class="inline-flex items-center gap-1 text-xs text-gray-600 dark:text-gray-300 hover:text-violet-600 dark:hover:text-violet-400 underline underline-offset-2">
                    <span>🔗</span><span class="max-w-[20rem] truncate">{{ (award as any).certificateUrl || (award as any).certUrl }}</span>
                  </a>
                </div>
              </div>
            </div>
          </div>
          <div class="flex items-center gap-2 shrink-0">
            <button @click="openEditAward(award)"
              class="text-xs text-violet-600 dark:text-violet-400 hover:underline opacity-0 group-hover:opacity-100 transition-opacity">
              编辑
            </button>
            <button @click="removeAwards(award.id)"
              class="text-red-400 hover:text-red-600 opacity-0 group-hover:opacity-100 transition-opacity text-sm">🗑️</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 教育经历 -->
    <div>
      <h2 class="text-lg font-semibold mb-4 flex items-center gap-2">
        <span>🎓</span> 教育经历（{{ appStore.education.length }}）
      </h2>
      <div class="space-y-3">
        <div v-for="edu in sortedEdu" :key="edu.id"
          class="card p-4 flex items-start justify-between group gap-4 border-l-4 border-l-indigo-500">
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-3 mb-1 flex-wrap">
              <h4 class="font-semibold">{{ edu.school }}</h4>
              <span class="text-xs text-gray-500 font-mono">{{ edu.startDate }} — {{ edu.endDate ?? edu.startDate }}</span>
            </div>
            <p class="text-sm text-gray-600 dark:text-gray-400">{{ edu.degree }} · {{ edu.major }}</p>
            <p v-if="edu.description" class="text-sm text-gray-500 dark:text-gray-400 mt-1 line-clamp-2">{{ edu.description }}</p>
          </div>
          <div class="flex items-center gap-2 shrink-0">
            <button @click="openEditEducation(edu)"
              class="text-xs text-violet-600 dark:text-violet-400 hover:underline opacity-0 group-hover:opacity-100 transition-opacity">
              编辑
            </button>
            <button @click="removeEducation(edu.id)"
              class="text-red-400 hover:text-red-600 opacity-0 group-hover:opacity-100 transition-opacity text-sm">🗑️</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增/编辑 弹窗 -->
    <div v-if="showForm" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div class="absolute inset-0 bg-black/50 backdrop-blur-sm" @click="!submitting && (showForm = false)"></div>
      <div class="relative bg-white dark:bg-dark-card rounded-2xl shadow-2xl w-full max-w-lg p-6 z-10 max-h-[92vh] overflow-y-auto">
        <h3 class="text-lg font-bold mb-1">
          {{ formType === 'award'
            ? (editingAwardId ? '编辑奖项' : '新增奖项')
            : (editingEduId ? '编辑教育经历' : '新增教育经历') }}
        </h3>
        <p v-if="formType === 'award'" class="text-xs text-gray-500 mb-4">
          证书链接需以 <code class="px-1 bg-gray-100 dark:bg-gray-800 rounded">https://</code> 开头，留空则不展示。
        </p>
        <div v-if="error" class="mb-4 px-3 py-2 rounded-lg bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-300 text-sm">
          {{ error }}
        </div>

        <div class="space-y-4">
          <template v-if="formType === 'award'">
            <div>
              <label class="block text-sm font-medium mb-1">奖项名称 *</label>
              <input v-model="awardForm.title" type="text"
                class="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500" />
            </div>
            <div>
              <label class="block text-sm font-medium mb-1">颁发机构 *</label>
              <input v-model="awardForm.issuer" type="text"
                class="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500" />
            </div>
            <div>
              <label class="block text-sm font-medium mb-1">获得时间 *</label>
              <input v-model="awardForm.date" type="month" @keydown.prevent
                class="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500" />
            </div>

            <!-- 新增：奖项封面上传（杂志编辑风） -->
            <div>
              <label class="block text-sm font-semibold mb-2 tracking-[0.15em] uppercase">
                <span class="inline-flex items-center gap-2">
                  <span class="w-6 h-6 inline-flex items-center justify-center text-[11px] font-mono text-white" style="background:#B23A2E;">02</span>
                  荣誉封面 Cover
                </span>
              </label>
              <input
                ref="awardCoverFileInput"
                type="file"
                accept="image/jpeg,image/png,image/webp,image/gif,image/svg+xml"
                class="hidden"
                @change="(e: any) => onAwardCoverSelected(e.target?.files)"
              />
              <label
                class="relative block w-full aspect-[16/9] overflow-hidden rounded-xl cursor-pointer select-none border-2 transition-all duration-300"
                :class="[
                  awardCoverDragOver
                    ? 'border-[#B23A2E] shadow-[0_0_0_6px_rgba(178,58,46,0.08)] scale-[1.01]'
                    : awardForm.coverUrl
                      ? 'border-gray-100 dark:border-gray-700'
                      : 'border-dashed border-gray-300 dark:border-gray-600 hover:border-[#B23A2E]/60',
                ]"
                :style="!awardForm.coverUrl ? awardCoverGradientCss : ''"
                @dragover.prevent="awardCoverDragOver = true"
                @dragleave.prevent="awardCoverDragOver = false"
                @drop="onAwardCoverDrop"
                @click="awardCoverUploading ? null : awardCoverFileInput?.click()"
              >
                <img v-if="awardForm.coverUrl"
                     :src="awardForm.coverUrl"
                     :alt="awardForm.title || 'award cover preview'"
                     class="absolute inset-0 w-full h-full object-cover" />
                <div v-if="!awardForm.coverUrl" class="absolute inset-0 overflow-hidden pointer-events-none">
                  <span class="absolute -left-3 -top-14 font-heading italic text-white/[0.08]"
                    style="font-size: 18rem; line-height: 1; letter-spacing: -0.05em;">HONOR</span>
                  <span class="absolute right-5 bottom-3 font-mono text-white/30 text-[10px] tracking-[0.4em] uppercase">§.02 / award-cover</span>
                </div>
                <span class="absolute top-4 left-4 inline-flex items-center gap-2 px-3 py-1.5 text-[10px] font-mono tracking-[0.25em] uppercase backdrop-blur border"
                      :class="awardForm.coverUrl
                        ? 'bg-black/40 text-white border-white/10'
                        : 'bg-white/10 text-white border-white/10'">
                  <span class="w-1.5 h-1.5 rounded-full"
                        :style="{ background: awardCoverUploading ? '#FACC15' : awardCoverDragOver ? '#fff' : '#B23A2E' }"></span>
                  {{ awardCoverUploading ? 'UPLOADING…' : awardForm.coverUrl ? 'READY · 封面图' : 'DRAG & DROP / CLICK' }}
                </span>
                <button v-if="awardForm.coverUrl && !awardCoverUploading" type="button"
                  @click.stop.prevent="removeAwardCover"
                  class="group absolute top-4 right-4 w-9 h-9 inline-flex items-center justify-center rounded-full bg-white/95 dark:bg-black/70 text-gray-700 dark:text-white shadow-md hover:bg-[#B23A2E] hover:text-white transition-all duration-200"
                  title="移除封面">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" class="w-4 h-4">
                    <path d="M18 6 6 18M6 6l12 12"/>
                  </svg>
                </button>
                <div v-if="awardCoverUploading" class="absolute inset-0 flex flex-col items-center justify-center gap-3 bg-black/55 text-white">
                  <svg viewBox="0 0 48 48" class="w-12 h-12 animate-spin">
                    <circle cx="24" cy="24" r="18" fill="none" stroke="#ffffff22" stroke-width="5"/>
                    <path d="M24 6a18 18 0 0 1 18 18" stroke="#ffffff" stroke-width="5" stroke-linecap="round"/>
                  </svg>
                  <span class="font-mono text-[11px] tracking-[0.3em] uppercase">Uploading Cover…</span>
                </div>
                <div v-if="!awardCoverUploading && !awardForm.coverUrl"
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
                      将<strong class="italic text-white/95">奖状/奖杯照片</strong>拖到这里或点击选择
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
              <p class="text-[11px] text-gray-500 mt-2">
                可选：一张奖状/奖杯实物图，会出现在奖项列表和 HR 端时间轴。不传则只显示文字信息。
              </p>
            </div>

            <div>
              <label class="block text-sm font-medium mb-1">🔗 证书链接（URL）</label>
              <input v-model="awardForm.certificateUrl" type="url" placeholder="https://.../certificate.pdf"
                :class="['w-full px-4 py-2 rounded-xl border bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500',
                  awardForm.certUrlErr ? 'border-red-400' : 'border-gray-200 dark:border-gray-700']" />
              <p v-if="awardForm.certUrlErr" class="text-xs text-red-500 mt-1">{{ awardForm.certUrlErr }}</p>
            </div>
            <div>
              <label class="block text-sm font-medium mb-1">描述</label>
              <textarea v-model="awardForm.description" rows="3"
                class="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500 resize-none"></textarea>
            </div>
          </template>

          <template v-else>
            <div>
              <label class="block text-sm font-medium mb-1">学校 *</label>
              <input v-model="eduForm.school" type="text"
                class="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500" />
            </div>
            <div>
              <label class="block text-sm font-medium mb-1">学位 *</label>
              <select v-model="eduForm.degree"
                class="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500">
                <option>学士</option><option>硕士</option><option>博士</option>
                <option>大专</option><option>博士后</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium mb-1">专业 *</label>
              <input v-model="eduForm.major" type="text"
                class="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500" />
            </div>
            <div>
              <label class="block text-sm font-medium mb-1">起止时间 *（YYYY-MM）</label>
              <div class="flex gap-2">
                <input v-model="eduForm.startDate" type="month" @keydown.prevent
                  class="flex-1 px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500" />
                <input v-model="eduForm.endDate" type="month" @keydown.prevent
                  class="flex-1 px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500" />
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium mb-1">描述</label>
              <textarea v-model="eduForm.description" rows="3"
                class="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500 resize-none"></textarea>
            </div>
          </template>
        </div>

        <div class="flex gap-3 mt-6">
          <button
            @click="formType === 'award' ? saveAward() : saveEducation()"
            :disabled="submitting"
            class="btn-primary flex-1 text-sm disabled:opacity-60">
            {{ submitting ? '保存中…' : '保存' }}
          </button>
          <button @click="showForm = false" :disabled="submitting" class="btn-outline flex-1 text-sm disabled:opacity-60">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>
