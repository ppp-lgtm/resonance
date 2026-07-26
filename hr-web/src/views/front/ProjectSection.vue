<script setup lang="ts">
import { ref, computed, watch, nextTick, onBeforeUnmount, watchEffect } from 'vue'
import { useAppStore } from '../../stores/app'
import { useInView } from '../../composables'
import type { Project, ProjectImage } from '../../types'
import { publicApi } from '../../api'

const app = useAppStore()
const { target: _ref, isInView } = useInView(0.04)
const activeTag = ref<string>('全部')
const selected = ref<Project | null>(null)
const detailLoading = ref(false)
const detailError = ref<string | null>(null)
const imageErrored = ref<Record<number, boolean>>({})
const coverImageErrored = ref<Record<number, boolean>>({})

const revealKey = ref(0)
watch(activeTag, () => { revealKey.value++; nextTick() })

const projects = computed<Project[]>(() => app.projects ?? [])
const tags = computed(() => ['全部', ...Array.from(new Set(projects.value.flatMap(p => p.tags)))])
const filtered = computed(() => {
  if (activeTag.value === '全部') return projects.value
  return projects.value.filter(p => p.tags.includes(activeTag.value))
})

/**
 * 「详细描述标题右侧要显示的正文」：
 * 1. 优先详情接口完整 description
 * 2. 列表对象里 description 就是 null（toListVO 省流量传 null）时，fallback 到 summary（列表卡片上的简介一定有值，不会空）
 * 3. 极端兜底：还是空就显示一个占位小字给用户看，不会右侧完全空白
 */
const displayedDescription = computed(() => {
  if (!selected.value) return ''
  const desc = (selected.value.description ?? '').trim()
  if (desc) return desc
  const summ = (selected.value.summary ?? '').trim()
  if (summ) return summ
  return '（详细描述尚未填写，请在后台「编辑项目」中补充「详细描述」内容。）'
})

/* ============================================================
 * 修复 问题1+2：封面图 URL 解析 + 错误兜底
 *   - 如果后端返回 /files/... 相对路径，直接用（Vite 代理）
 *   - 如果返回 http(s)://localhost:8080/files/... 老格式，剥成相对路径
 *   - 空或加载失败 → 显示原几何色块（占位兜底）
 * ============================================================ */
function resolveImageUrl(url?: string | null): string | null {
  if (!url) return null
  // 历史脏数据：UploadController 旧版返回 localhost 绝对 URL，剥成相对路径
  let clean = String(url).trim()
  clean = clean.replace(/^https?:\/\/localhost:\d+(?=\/)/i, '')
  clean = clean.replace(/^https?:\/\/127\.0\.0\.1:\d+(?=\/)/i, '')
  if (!clean) return null
  return clean
}

function coverOf(p: Project): ProjectImage | null {
  const first = p.images?.[0]
  return first?.url ? first : null
}

function onCardImgError(id: number) { imageErrored.value[id] = true }
function onCoverImgError(id: number) { coverImageErrored.value[id] = true }

/* ============================================================
 * 修复 问题6：滚动穿透
 *   弹窗打开 → body 加 overflow:hidden 锁定背景；关闭 → 移除
 * ============================================================ */
watchEffect(() => {
  if (typeof document === 'undefined') return
  if (selected.value) {
    document.body.style.overflow = 'hidden'
    // 移动端：防止 iOS 橡皮筋漏滚动
    document.body.style.touchAction = 'none'
  } else {
    document.body.style.overflow = ''
    document.body.style.touchAction = ''
  }
})
onBeforeUnmount(() => {
  if (typeof document === 'undefined') return
  document.body.style.overflow = ''
  document.body.style.touchAction = ''
})

// --- 封面配色（纯色块，无渐变）---
const coverPlan = computed(() => ([
  { bg: 'bg-light-accent dark:bg-dark-accent', mark: '色板·壹', pat: 0, text: 'text-light-bg dark:text-dark-bg' },
  { bg: 'bg-light-accent2 dark:bg-dark-accent2', mark: '色板·贰', pat: 1, text: 'text-light-bg dark:text-dark-bg' },
  { bg: 'bg-light-text dark:bg-dark-text',     mark: '色板·叁', pat: 2, text: 'text-light-bg dark:text-dark-bg' },
  { bg: 'bg-light-surface dark:bg-dark-surface', mark: '色板·肆', pat: 3, text: 'text-light-text dark:text-dark-text' },
  { bg: 'bg-light-muted/40 dark:bg-dark-muted/20', mark: '色板·伍', pat: 0, text: 'text-light-text dark:text-dark-text' },
  { bg: 'bg-light-card dark:bg-dark-card border border-light-rule dark:border-dark-rule', mark: '色板·陆', pat: 1, text: 'text-light-text dark:text-dark-text' },
]))

/**
 * 点击卡片打开详情弹窗：
 * 1. 先塞列表对象兜底：封面/标题/标签/summary 立刻显示，不会白屏也不会右侧空
 * 2. 异步再调 /public/projects/:id 拿完整详情（包含 description）→ 覆盖 selected
 * 3. 失败静默保留兜底，红色小字提示错误即可
 */
async function selectProject(p: Project) {
  coverImageErrored.value = {}
  detailError.value = null
  selected.value = p
  detailLoading.value = true
  try {
    if (typeof p.id === 'number' || typeof p.id === 'string') {
      const fresh = await publicApi.projectDetail(Number(p.id))
      if (fresh && selected.value && String(selected.value.id) === String(p.id)) {
        selected.value = fresh
      }
    }
  } catch (e: any) {
    detailError.value = `详情加载失败：${e?.message || e}（以下为列表摘要数据）`
  } finally {
    detailLoading.value = false
  }
}
function closeModal() {
  selected.value = null
  detailError.value = null
  detailLoading.value = false
}

function onKey(e: KeyboardEvent) {
  if (e.key === 'Escape') closeModal()
}
if (typeof window !== 'undefined') {
  window.addEventListener('keydown', onKey)
  onBeforeUnmount(() => window.removeEventListener('keydown', onKey))
}
</script>

<template>
  <section id="projects" ref="_ref" class="relative bg-light-card dark:bg-dark-card border-t border-light-rule dark:border-dark-rule">
    <div class="mx-auto max-w-[1400px] px-6 md:px-16 lg:px-20 py-24 md:py-32">
      <!-- HEADER -->
      <header class="grid grid-cols-12 gap-6 items-end mb-12 md:mb-16">
        <div class="col-span-12 md:col-span-5">
          <p class="sect-eyebrow sect-eyebrow--no-line mb-4"> — 作品精选</p>
          <h2 class="sect-title md:text-[3.5rem]">
            案例档案<em>与研究</em>。
          </h2>
        </div>
        <p class="col-span-12 md:col-span-5 md:pl-8 text-sm md:text-[0.95rem] leading-[1.9] text-light-muted dark:text-dark-muted">
          一份精心挑选的项目合集，涵盖生产级系统到周末实验。点击任意条目，即可阅读完整的案例说明、源码链接与在线演示地址。
        </p>
        <div class="col-span-12 md:col-span-2 md:text-right">
          <span class="font-mono text-[0.65rem] tracking-[0.3em] text-light-muted/60 dark:text-dark-muted/60">
            共 {{ String(projects.length).padStart(2,'0') }} 件
          </span>
        </div>
      </header>

      <!-- TAGS (underlined filter) -->
      <div class="flex flex-wrap items-center gap-x-4 gap-y-3 pb-6 mb-2 border-b border-light-rule dark:border-dark-rule">
        <button
          v-for="t in tags"
          :key="t"
          type="button"
          class="group relative px-4 py-2 !text-sm !font-semibold !tracking-[0.12em] rounded-md transition-all border"
          :class="activeTag === t
            ? '!bg-light-accent !text-light-bg dark:!bg-dark-accent dark:!text-dark-bg border-transparent shadow-sm'
            : 'border-light-rule dark:border-dark-rule text-light-text dark:text-dark-text hover:!bg-light-card dark:hover:!bg-dark-card'"
          @click="activeTag = t"
        >
          {{ t }}
        </button>
      </div>

      <!-- LIST (magazine index / swiss rows) -->
      <div :key="revealKey">
        <article
          v-for="(p, i) in filtered"
          :key="p.id + revealKey"
          class="project-row group px-2 md:px-4"
          :class="isInView ? 'animate-fade-in-up opacity-0' : 'opacity-0'"
          style="animation-fill-mode:forwards;animation-duration:.7s;"
          :style="{ animationDelay: `${70 * i}ms` }"
          @click="selectProject(p)"
        >
          <!-- index -->
          <div class="col-span-2 md:col-span-1 flex md:items-center pt-2 md:pt-0">
            <div class="flex md:flex-col items-start md:items-center gap-3">
              <span class="project-index tabular-nums">
                {{ String(i + 1).padStart(2, '0') }}
              </span>
              <span class="hidden md:block w-px h-10 bg-light-rule dark:bg-dark-rule"></span>
            </div>
          </div>

          <!-- cover (solid plate) -->
          <div class="col-span-10 md:col-span-4">
            <div
              class="img-frame aspect-[4/3] md:aspect-[16/10] w-full overflow-hidden"
              :class="[
                !resolveImageUrl(coverOf(p)?.url) || imageErrored[p.id]
                  ? coverPlan[i % coverPlan.length].bg
                  : ''
              ]"
            >
              <img
                v-if="resolveImageUrl(coverOf(p)?.url) && !imageErrored[p.id]"
                :src="resolveImageUrl(coverOf(p)!.url)!"
                :alt="coverOf(p)!.alt || p.title || '项目封面图'"
                class="absolute inset-0 w-full h-full object-cover z-[1] transition-transform duration-500 group-hover:scale-[1.03]"
                loading="lazy"
                @error="onCardImgError(p.id)"
              />

              <!-- 几何色块/叠印（兜底或图片加载失败时显示，图片时也弱叠加）-->
              <template v-if="!resolveImageUrl(coverOf(p)?.url) || imageErrored[p.id]">
                <span class="frame-mark">{{ coverPlan[i % coverPlan.length].mark }}</span>
                <span class="frame-corner-tr"></span>
                <span class="frame-corner-bl"></span>
                <!-- geometric overlay (no gradient) -->
                <div class="absolute inset-0 flex items-center justify-center overflow-hidden">
                  <template v-if="coverPlan[i % coverPlan.length].pat === 0">
                    <div class="grid grid-cols-6 grid-rows-6 gap-2 w-[70%] h-[70%] opacity-70">
                      <div v-for="n in 36" :key="n"
                        class="border border-current/20"
                        :class="(n + i) % 3 === 0 ? coverPlan[i % coverPlan.length].text + ' bg-current/10' : ''"
                      ></div>
                    </div>
                  </template>
                  <template v-else-if="coverPlan[i % coverPlan.length].pat === 1">
                    <div :class="coverPlan[i % coverPlan.length].text + ' opacity-60'">
                      <svg viewBox="0 0 100 100" class="w-[55%] h-[55%]" fill="none" stroke="currentColor" stroke-width="1.2">
                        <circle cx="50" cy="50" r="36" />
                        <circle cx="50" cy="50" r="22" />
                        <circle cx="50" cy="50" r="8" fill="currentColor" stroke="none" />
                      </svg>
                    </div>
                  </template>
                  <template v-else-if="coverPlan[i % coverPlan.length].pat === 2">
                    <div class="w-[72%] h-[72%] grid grid-cols-2 grid-rows-2 gap-2 opacity-70" :class="coverPlan[i % coverPlan.length].text">
                      <div class="border border-current/25"></div>
                      <div class="bg-current/15"></div>
                      <div class="bg-current/25"></div>
                      <div class="border border-current/25"></div>
                    </div>
                  </template>
                  <template v-else>
                    <div :class="coverPlan[i % coverPlan.length].text + ' opacity-70 flex items-center justify-center'">
                      <div class="font-heading text-[3.5rem] md:text-[5rem] leading-none">
                        {{ String(i + 1).padStart(2, '0') }}
                      </div>
                    </div>
                  </template>
                </div>
              </template>

              <!-- hover "read case" label -->
              <div class="absolute bottom-4 right-4 left-4 flex items-center justify-between translate-y-3 opacity-0 group-hover:translate-y-0 group-hover:opacity-100 transition-all duration-500 z-[2]">
                <span class="font-mono text-[0.6rem] tracking-[0.3em] uppercase" :class="resolveImageUrl(coverOf(p)?.url) && !imageErrored[p.id] ? 'text-white drop-shadow-sm' : coverPlan[i % coverPlan.length].text">
                  点击查看
                </span>
                <svg class="w-4 h-4" :class="resolveImageUrl(coverOf(p)?.url) && !imageErrored[p.id] ? 'text-white drop-shadow-sm' : coverPlan[i % coverPlan.length].text" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3"/></svg>
              </div>
            </div>
          </div>

          <!-- meta -->
          <div class="col-span-12 md:col-span-5 md:pl-2 lg:pl-6 pt-4 md:pt-0 flex flex-col justify-center">
            <div class="flex items-center gap-3 mb-3">
              <span class="w-6 h-px bg-light-accent dark:bg-dark-accent"></span>
              <span class="font-mono text-[0.65rem] tracking-[0.28em] uppercase text-light-accent dark:text-dark-accent">
                {{ p.tags.slice(0, 2).join('  ·  ') }}
              </span>
              <span v-if="p.isFeatured" class="ml-auto md:hidden text-[0.6rem] tracking-[0.25em] uppercase font-mono text-light-accent dark:text-dark-accent border border-light-accent/30 dark:border-dark-accent/30 px-2 py-0.5">
                精选
              </span>
            </div>

            <h3 class="font-heading text-2xl md:text-[2rem] lg:text-[2.2rem] leading-[1.1] text-light-text dark:text-dark-text mb-4 group-hover:text-light-accent dark:group-hover:text-dark-accent transition-colors duration-400">
              {{ p.title }}
            </h3>

            <p class="text-sm md:text-[0.92rem] leading-[1.85] text-light-muted dark:text-dark-muted line-clamp-2 mb-4 max-w-xl">
              {{ p.summary }}
            </p>

            <!-- 核心修复：卡片级快速入口（GitHub/演示/视频），让 HR 不需要点开详情就能访问外链 -->
            <div class="flex flex-wrap items-center gap-2 mb-4" @click.stop>
              <a v-if="p.githubUrl" :href="p.githubUrl" target="_blank" rel="noopener noreferrer"
                 class="pill-link pill-link--muted group">
                <span class="inline-block w-3.5 h-3.5 mr-1 opacity-80 group-hover:opacity-100" aria-hidden="true">
                  <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 .3a12 12 0 00-3.8 23.4c.6.1.8-.3.8-.6v-2.2c-3.3.7-4-1.6-4-1.6-.6-1.4-1.4-1.8-1.4-1.8-1.1-.8.1-.8.1-.8 1.3.1 1.9 1.3 1.9 1.3 1.1 1.9 2.9 1.3 3.6 1 .1-.8.4-1.3.8-1.6-2.7-.3-5.5-1.3-5.5-5.9 0-1.3.5-2.4 1.2-3.2-.1-.3-.5-1.5.1-3.2 0 0 1-.3 3.3 1.2a11.5 11.5 0 016 0C17.3 4.7 18.3 5 18.3 5c.6 1.7.2 2.9.1 3.2.8.8 1.2 1.9 1.2 3.2 0 4.6-2.8 5.6-5.5 5.9.4.4.8 1.1.8 2.2v3.3c0 .3.2.7.8.6A12 12 0 0012 .3z"/></svg>
                </span>
                <span class="pill-link__label">源码</span>
              </a>
              <a v-if="p.demoUrl" :href="p.demoUrl" target="_blank" rel="noopener noreferrer"
                 class="pill-link pill-link--accent group">
                <svg class="w-3.5 h-3.5 mr-1 opacity-90 group-hover:opacity-100" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"/>
                </svg>
                <span class="pill-link__label">在线演示</span>
              </a>
              <a v-if="p.videoUrl" :href="p.videoUrl" target="_blank" rel="noopener noreferrer"
                 class="pill-link pill-link--ghost group">
                <svg class="w-3.5 h-3.5 mr-1 opacity-90 group-hover:opacity-100" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 10l4.55-2.28A1 1 0 0121 8.62v6.76a1 1 0 01-1.45.9L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z"/>
                </svg>
                <span class="pill-link__label">视频介绍</span>
              </a>
            </div>

            <div class="hidden md:flex flex-wrap items-center gap-2">
              <span v-for="tg in p.tags" :key="tg" class="pill-tag">
                {{ tg }}
              </span>
            </div>
          </div>

          <!-- year / arrow -->
          <div class="hidden md:flex col-span-2 flex-col items-end justify-between py-1">
            <span v-if="p.isFeatured" class="text-[0.6rem] tracking-[0.3em] uppercase font-mono text-light-accent dark:text-dark-accent border border-light-accent/30 dark:border-dark-accent/30 px-2.5 py-1">
              ★ 精选作品
            </span>
            <span v-else class="footnote opacity-40">—</span>
            <div class="flex items-end gap-3">
              <div class="text-right">
                <div class="footnote">案例档案</div>
                <div class="font-heading text-2xl leading-none text-light-text dark:text-dark-text tabular-nums mt-1">
                  编号 {{ String(i + 1).padStart(2, '0') }}
                </div>
              </div>
              <div class="w-10 h-10 border border-light-rule dark:border-dark-rule flex items-center justify-center -translate-x-2 opacity-0 group-hover:translate-x-0 group-hover:opacity-100 group-hover:border-light-text dark:group-hover:border-dark-text transition-all duration-500">
                <svg class="w-3.5 h-3.5 text-light-text dark:text-dark-text" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 17L17 7M17 7H8M17 7v9"/></svg>
              </div>
            </div>
          </div>
        </article>
      </div>
    </div>

    <!-- ============== MODAL ============== -->
    <!--
      修复 问题4：header 与 body 同一无缝容器（背景色完全一致）
      修复 问题5：header 去掉 sticky，随内容整体滚动（不再固定悬浮）
      修复 问题6：滚动容器是内层 div.max-h-[86vh].overflow-y-auto（独立滚动区）
                  + 外层 watchEffect 给 body 加 overflow:hidden 锁背景滚动
    -->
    <Teleport to="body">
      <Transition name="modal-fade">
        <div
          v-if="selected"
          class="fixed inset-0 z-[80] flex items-start md:items-center justify-start md:justify-center p-0 md:p-6"
          @click.self="closeModal"
        >
          <div
            class="absolute inset-0 bg-light-bg/95 dark:bg-dark-bg/95 backdrop-blur-sm"
            @click="closeModal"
            aria-hidden="true"
          ></div>

          <div
            class="relative w-full md:max-w-5xl mt-12 md:mt-0 z-10
                   max-h-[86vh] overflow-y-auto overflow-x-hidden
                   bg-light-card dark:bg-dark-card
                   border border-light-rule dark:border-dark-rule
                   shadow-paper"
          >
            <div
              class="flex items-center justify-between px-6 md:px-10 py-5
                     bg-light-card dark:bg-dark-card
                     border-b border-light-rule dark:border-dark-rule"
            >
              <div class="flex items-center gap-4 min-w-0">
                <span class="w-px h-4 bg-light-rule dark:bg-dark-rule shrink-0"></span>
                <span class="font-heading text-lg md:text-xl text-light-text dark:text-dark-text truncate">
                  {{ selected.title }}
                </span>
              </div>
              <button
                type="button"
                aria-label="Close"
                class="group w-10 h-10 flex items-center justify-center border border-light-rule dark:border-dark-rule hover:border-light-text dark:hover:border-dark-text transition-colors shrink-0"
                @click="closeModal"
              >
                <svg class="w-4 h-4 text-light-text dark:text-dark-text" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
              </button>
            </div>

            <!-- body（与 header 同色无间隙）-->
            <div class="px-6 md:px-10 py-10 bg-light-card dark:bg-dark-card">
              <!--
                状态条：
                · loading：小字 ⟳ 同步中（让用户知道详情马上会刷新出完整 description）
                · error：红字提示，兜底显示列表摘要（至少 summary 会在右侧显示）
                · 正常：隐藏，不占高度
              -->
              <div class="min-h-[0px] mb-4">
                <div
                  v-if="detailLoading"
                  class="text-[11px] tracking-[0.2em] uppercase font-mono text-violet-500 dark:text-violet-400 animate-pulse"
                >
                  ⟳ 同步详情（详细描述正文 / 状态 / 年份）…
                </div>
                <div
                  v-else-if="detailError"
                  class="text-xs leading-relaxed text-red-500/90 dark:text-red-400/90"
                >
                  {{ detailError }}
                </div>
              </div>
              <!-- cover plate + cover image 加载失败兜底 -->
              <div
                class="img-frame aspect-[21/9] mb-10 w-full overflow-hidden"
                :class="[
                  !resolveImageUrl(coverOf(selected)?.url) || coverImageErrored[selected.id]
                    ? coverPlan[(selected.id - 1) % coverPlan.length].bg
                    : ''
                ]"
              >
                <!-- 修复 问题2：详情弹窗封面图也未显示（与卡片共用 resolveImageUrl + error 兜底） -->
                <img
                  v-if="resolveImageUrl(coverOf(selected)?.url) && !coverImageErrored[selected.id]"
                  :src="resolveImageUrl(coverOf(selected)!.url)!"
                  :alt="coverOf(selected)!.alt || selected.title || '项目详情封面'"
                  class="absolute inset-0 w-full h-full object-cover z-[1]"
                  @error="onCoverImgError(selected.id)"
                />
                <template v-if="!resolveImageUrl(coverOf(selected)?.url) || coverImageErrored[selected.id]">
                  <span class="frame-mark">{{ coverPlan[(selected.id - 1) % coverPlan.length].mark }}</span>
                  <span class="frame-corner-tr"></span>
                  <span class="frame-corner-bl"></span>
                  <div class="absolute inset-0 flex items-center justify-center">
                    <div class="grid grid-cols-8 gap-1.5 w-[62%] h-[62%] opacity-80" :class="coverPlan[(selected.id - 1) % coverPlan.length].text">
                      <div v-for="n in 64" :key="n"
                        class="border border-current/15"
                        :class="(n + (selected.id * 7)) % 5 === 0 ? 'bg-current/20' : ''"
                      ></div>
                    </div>
                  </div>
                </template>
              </div>

              <!-- meta -->
              <div class="grid grid-cols-12 gap-6 mb-10 items-start">
                <div class="col-span-12 md:col-span-8">
                  <div class="flex flex-wrap items-center gap-3 mb-4">
                    <span
                      v-for="t in selected.tags"
                      :key="t"
                      class="pill-tag"
                    >{{ t }}</span>
                  </div>
                  <h3 class="font-heading text-3xl md:text-5xl leading-[1.05] text-light-text dark:text-dark-text mb-4">
                    {{ selected.title }}
                  </h3>
                  <p class="text-[1rem] md:text-[1.05rem] leading-[1.9] text-light-muted dark:text-dark-muted">
                    {{ selected.summary }}
                  </p>
                </div>
                <!--
                  元数据：项目状态 / 完成年份 / 排序编号
                  （按要求删除：担任角色字段已在整个项目链路中移除）
                  字段来源：后端 Project.status / Project.completionYear（真实字段）
                  空 → 显示 "—"
                -->
                <div class="col-span-12 md:col-span-4 md:pl-6 md:border-l border-light-rule dark:border-dark-rule space-y-4">
                  <div class="flex justify-between items-center border-b border-light-rule dark:border-dark-rule pb-3 gap-3">
                    <span class="footnote shrink-0">项目状态</span>
                    <span class="text-sm text-light-text dark:text-dark-text">
                      <template v-if="selected.status">
                        <span
                          class="inline-block w-1.5 h-1.5 mr-2 align-middle rounded-full"
                          :class="
                            selected.status === '已上线'
                              ? 'bg-emerald-500 dark:bg-emerald-400'
                              : selected.status === '筹备中'
                                ? 'bg-amber-500 dark:bg-amber-400'
                                : selected.status === '规划中'
                                  ? 'bg-sky-500 dark:bg-sky-400'
                                  : 'bg-light-accent2 dark:bg-dark-accent2'
                          "
                        ></span>
                        {{ selected.status }}
                      </template>
                      <template v-else>
                        <span class="text-light-muted/70 dark:text-dark-muted/70 font-mono tracking-[0.2em] text-[0.72rem]">—</span>
                      </template>
                    </span>
                  </div>
                  <div class="flex justify-between items-center border-b border-light-rule dark:border-dark-rule pb-3 gap-3">
                    <span class="footnote shrink-0">完成年份</span>
                    <span class="text-sm font-medium tabular-nums text-light-text dark:text-dark-text">
                      <template v-if="selected.completionYear || selected.completionYear === 0">
                        {{ selected.completionYear }}
                      </template>
                      <template v-else>
                        <span class="text-light-muted/70 dark:text-dark-muted/70 font-mono tracking-[0.2em] text-[0.72rem]">—</span>
                      </template>
                    </span>
                  </div>
                  <div class="flex justify-between items-center pb-1 gap-3">
                    <span class="footnote shrink-0">排序编号</span>
                    <span class="font-mono text-sm tabular-nums text-light-text dark:text-dark-text">
                      0{{ selected.sortOrder }}
                    </span>
                  </div>
                </div>
              </div>

              <!-- body -->
              <div class="rule mb-8"></div>
              <div class="grid grid-cols-12 gap-8">
                <div class="col-span-12 md:col-span-2">
                  <!-- 章节标题：项目概述 → 详细描述（按用户最新要求） -->
                  <span class="footnote block mb-3">— 详细描述</span>
                  <!--
                    副文案：按用户给出的基线严格实现
                    · 有状态或年份 → 显示 状态 · 年份
                    · 都为空 → 01 / 01（用户指定的占位符基线）
                  -->
                  <span
                    v-if="selected.status || selected.completionYear"
                    class="font-heading text-sm leading-snug text-light-muted dark:text-dark-muted"
                  >
                    <template v-if="selected.status">{{ selected.status }}</template>
                    <template v-if="selected.status && selected.completionYear"><span class="mx-1.5 text-light-rule dark:text-dark-rule">·</span></template>
                    <template v-if="selected.completionYear" class="tabular-nums">{{ selected.completionYear }}</template>
                  </span>
                  <span v-else class="font-display italic text-light-accent dark:text-dark-accent tabular-nums">
                    01 / 01
                  </span>
                </div>
                <div class="col-span-12 md:col-span-10">
                  <!--
                    正文：后台的「详细描述」字段内容（固定不再分离 overview 字段）
                    · 详情接口未返回时，列表对象里 description 为 null，会自动 fallback summary 兜底 → 不会右侧空
                    · 详情接口拉到后刷新为完整 description → 符合用户要求：后台写的详细描述 100% 出现在这里
                  -->
                  <div class="font-display italic text-xl md:text-2xl leading-[1.6] text-light-text dark:text-dark-text whitespace-pre-line">
                    {{ displayedDescription }}
                  </div>
                </div>
              </div>

              <!-- actions：4 类按钮全部补齐（此前缺少视频介绍入口） -->
              <div class="mt-14 pt-10 border-t border-light-rule dark:border-dark-rule flex flex-wrap items-center gap-4 justify-end">
                <a
                  v-if="selected.videoUrl"
                  :href="selected.videoUrl"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="btn-ghost"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 10l4.55-2.28A1 1 0 0121 8.62v6.76a1 1 0 01-1.45.9L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z"/>
                  </svg>
                  <span>观看视频介绍</span>
                </a>
                <a
                  v-if="selected.demoUrl"
                  :href="selected.demoUrl"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="btn-accent"
                >
                  <span>在线演示</span>
                  <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"/></svg>
                </a>
                <a
                  v-if="selected.githubUrl"
                  :href="selected.githubUrl"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="btn-ghost"
                >
                  <span class="inline-block w-4 h-4 mr-0.5" aria-hidden="true">
                    <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 .3a12 12 0 00-3.8 23.4c.6.1.8-.3.8-.6v-2.2c-3.3.7-4-1.6-4-1.6-.6-1.4-1.4-1.8-1.4-1.8-1.1-.8.1-.8.1-.8 1.3.1 1.9 1.3 1.9 1.3 1.1 1.9 2.9 1.3 3.6 1 .1-.8.4-1.3.8-1.6-2.7-.3-5.5-1.3-5.5-5.9 0-1.3.5-2.4 1.2-3.2-.1-.3-.5-1.5.1-3.2 0 0 1-.3 3.3 1.2a11.5 11.5 0 016 0C17.3 4.7 18.3 5 18.3 5c.6 1.7.2 2.9.1 3.2.8.8 1.2 1.9 1.2 3.2 0 4.6-2.8 5.6-5.5 5.9.4.4.8 1.1.8 2.2v3.3c0 .3.2.7.8.6A12 12 0 0012 .3z"/></svg>
                  </span>
                  <span>查看 GitHub 源码</span>
                  <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3"/></svg>
                </a>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </section>
</template>

<style scoped>
.modal-fade-enter-active, .modal-fade-leave-active { transition: opacity .35s ease; }
.modal-fade-enter-from, .modal-fade-leave-to   { opacity: 0; }
.modal-fade-enter-active > div:last-child,
.modal-fade-leave-active > div:last-child     { transition: transform .5s cubic-bezier(.2,.8,.2,1), opacity .4s; }
.modal-fade-enter-from   > div:last-child     { transform: translateY(30px); opacity: 0; }
.modal-fade-leave-to     > div:last-child     { transform: translateY(20px); opacity: 0; }

/* ===== URL 超链接胶囊按钮（Swiss 风格，无渐变）===== */
.pill-link {
  display: inline-flex;
  align-items: center;
  height: 1.75rem;
  padding: 0 0.65rem;
  border-radius: 9999px;
  font-size: 0.7rem;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  font-family: var(--font-mono, ui-monospace, SFMono-Regular, Menlo, monospace);
  border: 1px solid transparent;
  transition: all .3s ease;
  text-decoration: none;
  white-space: nowrap;
}
.pill-link__label { line-height: 1; }

.pill-link--muted {
  color: var(--color-light-muted, #6b7280);
  border-color: var(--color-light-rule, #e5e7eb);
  background: transparent;
}
.dark .pill-link--muted {
  color: var(--color-dark-muted, #9ca3af);
  border-color: var(--color-dark-rule, #262626);
}
.pill-link--muted:hover {
  color: var(--color-light-text, #0b0b0b);
  border-color: var(--color-light-text, #0b0b0b);
}
.dark .pill-link--muted:hover {
  color: var(--color-dark-text, #f5f5f5);
  border-color: var(--color-dark-text, #f5f5f5);
}

.pill-link--accent {
  color: var(--color-light-bg, #fafafa);
  background: var(--color-light-accent, #0b0b0b);
  border-color: var(--color-light-accent, #0b0b0b);
}
.dark .pill-link--accent {
  color: var(--color-dark-bg, #0a0a0a);
  background: var(--color-dark-accent, #f5f5f5);
  border-color: var(--color-dark-accent, #f5f5f5);
}
.pill-link--accent:hover { opacity: 0.88; }

.pill-link--ghost {
  color: var(--color-light-text, #0b0b0b);
  border-color: var(--color-light-rule, #e5e7eb);
  background: transparent;
}
.dark .pill-link--ghost {
  color: var(--color-dark-text, #f5f5f5);
  border-color: var(--color-dark-rule, #262626);
}
.pill-link--ghost:hover {
  border-color: var(--color-light-accent, #0b0b0b);
  color: var(--color-light-accent, #0b0b0b);
}
.dark .pill-link--ghost:hover {
  border-color: var(--color-dark-accent, #f5f5f5);
  color: var(--color-dark-accent, #f5f5f5);
}
</style>
