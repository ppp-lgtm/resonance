<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useAppStore } from '../../stores/app'
import type { Profile, ContactItem } from '../../types'

const app = useAppStore()
const profile = ref<Profile | null>(null)
const contacts = ref<ContactItem[]>([])
const typed = ref('')
const startedTyping = ref(false)

/* 修复：用真实仓库 / 接口返回数据（不再写死 demoData） */
const statCounts = computed(() => ([
  { num: String(app.projects.length || 0).padStart(2, '0'), label: '代表作品' },
  { num: String(app.skills.length || 0).padStart(2, '0'),   label: '技术栈点' },
  { num: String(app.awards.length || 0).padStart(2, '0'),   label: '荣誉奖项' },
  { num: (profile.value?.yearsExperience || 0).toString().padStart(2, '0'), label: '从业年限' },
]))

const displayName = computed<string>(() => (profile.value?.name?.trim() || ''))

const roleLine = computed<string>(() => profile.value?.title?.[0] ?? '全栈工程师')

watch(
  () => [app.profile, app.contacts, app.loading],
  () => {
    if (app.profile) profile.value = app.profile
    if (app.contacts?.length) contacts.value = app.contacts
  },
  { immediate: true }
)

onMounted(async () => {
  await app.fetchAll()
  if (app.profile)  profile.value = app.profile
  if (app.contacts?.length) contacts.value = app.contacts
  setTimeout(() => startTypewriter(), 900)
})

function startTypewriter() {
  const words = profile.value?.title ?? []
  if (!words.length) return
  startedTyping.value = true
  let wi = 0, ci = 0, deleting = false
  const tick = () => {
    const w = words[wi % words.length]
    if (!deleting) {
      ci++
      typed.value = w.slice(0, ci)
      if (ci === w.length) { deleting = true; setTimeout(tick, 1800); return }
    } else {
      typed.value = w.slice(0, --ci)
      if (ci === 0) { deleting = false; wi++ }
    }
    setTimeout(tick, deleting ? 32 : 70)
  }
  tick()
}

function isGithub(p: string) {
  return /github|git hub|gi thub/i.test(p || '')
}
function resolveContactLink(c: ContactItem): string | undefined {
  if (c.link && /^https?:\/\//i.test(c.link)) return c.link
  const v = (c.value || '').trim()
  if (!v) return undefined
  if (isGithub(c.platform) || isGithub(v)) {
    const handle = v.replace(/^https?:\/\/(www\.)?github\.com\/?/i, '').replace(/\/$/, '')
    return `https://github.com/${handle}`
  }
  if (/^https?:\/\//i.test(v)) return v
  if (/^[\w.+-]+@[\w-]+\.[\w.-]+$/.test(v)) return `mailto:${v}`
  if (/^1[3-9]\d{9}$/.test(v)) return `tel:${v}`
  return undefined
}
function resolveContactDisplay(c: ContactItem): string {
  const v = (c.value || '').trim()
  if (!v) return c.platform
  if (isGithub(c.platform) || isGithub(v)) {
    const handle = v.replace(/^https?:\/\/(www\.)?github\.com\/?/i, '').replace(/\/$/, '')
    return handle ? `@${handle.replace(/^@/, '')}` : c.platform
  }
  return v
}

/** 从 resume URL 中提取扩展名，用于 download="xxx.pdf" / xxx.png 等文件名建议 */
function resolveDownloadExt(url: string): string {
  const clean = (url || '').split('?')[0].split('#')[0]
  const m = clean.match(/\.([a-zA-Z0-9]{1,8})$/)
  if (m) return '.' + m[1].toLowerCase()
  // 无扩展名 → 判断 content-type 不现实，给 .pdf 兜底（简历大多是 PDF）
  return '.pdf'
}

/** 兜底：Safari 等浏览器对跨域直链 download 属性不生效，fallback 到 window.open */
function onDownloadResume(e: MouseEvent) {
  const anchor = e.currentTarget as HTMLAnchorElement | null
  if (!anchor) return
  const url = anchor.getAttribute('href') || ''
  if (!url) return
  // 如果是同源（/files/...），浏览器通常会尊重 download；
  // 跨域或发现预览行为时，提供手动提示 + 新标签兜底
  const isSameOrigin = /^\//.test(url) || url.startsWith(location.origin)
  if (!isSameOrigin) {
    e.preventDefault()
    window.open(url, '_blank', 'noopener,noreferrer')
  }
}
</script>

<template>
  <section id="hero" class="relative min-h-screen w-full overflow-hidden bg-paper">
    
    <div class="hidden lg:flex absolute inset-y-0 right-0 w-14 items-start justify-center py-10 pointer-events-none">
      <span class="footnote writing-vertical-rl tracking-[0.4em]">
        39.9042° N · 116.4074° E / 在线
      </span>
    </div>

    <!-- 四角装饰标记 -->
    <div class="pointer-events-none absolute top-24 left-14 md:left-28 w-5 h-5 border-t border-l border-light-rule/70 dark:border-dark-rule/70"></div>
    <div class="pointer-events-none absolute top-24 right-14 md:right-28 w-5 h-5 border-t border-r border-light-rule/70 dark:border-dark-rule/70"></div>

    <!-- 主内容列 -->
    <div class="relative mx-auto max-w-[1400px] px-6 md:px-28 lg:px-32 pt-36 md:pt-40 pb-24 md:pb-28 min-h-screen flex flex-col">
      <!-- 顶部元信息栏 -->
      <div class="hero-meta-row opacity-0 animate-fade-in" style="animation-delay:.12s;animation-fill-mode:forwards;">
        <div class="flex items-center gap-6">
          <span class="flex items-center gap-2">
            <span class="relative flex h-1.5 w-1.5">
              <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-light-accent dark:bg-dark-accent opacity-60"></span>
              <span class="relative inline-flex rounded-full h-1.5 w-1.5 bg-light-accent dark:bg-dark-accent"></span>
            </span>
            开放合作机会
          </span>
        </div>
        <div></div>
      </div>

      <div class="rule my-7 opacity-0 animate-fade-in" style="animation-delay:.22s;animation-fill-mode:forwards;"></div>

      <!-- 标题区 -->
      <div class="mt-2 md:mt-6 flex-1 flex flex-col justify-center">
        <div
          class="flex items-baseline gap-4 opacity-0 animate-fade-in-up"
          style="animation-delay:.35s;animation-fill-mode:forwards;transform-origin:left bottom;"
        >
          <span class="sect-eyebrow sect-eyebrow--no-line !text-xs md:!text-[0.7rem] shrink-0">
             — 你好，世界
          </span>
          <span class="rule flex-1"></span>
          <span class="font-mono text-[0.65rem] tracking-[0.3em] text-light-muted/70 dark:text-dark-muted/70 shrink-0 hidden md:inline">
            个人简历 / Résumé / CV
          </span>
        </div>

        <h1 class="mt-8 md:mt-12 hero-display opacity-0 animate-fade-in-up" style="animation-delay:.55s;animation-fill-mode:forwards;">
  <span class="block hero-name text-5xl md:text-6xl lg:text-7xl">{{ displayName }}</span>
  <span class="block italic font-display text-light-accent dark:text-dark-accent font-normal mt-2 md:mt-3 text-2xl md:text-3xl lg:text-4xl">
    {{ roleLine }}
  </span>
</h1>
        <!-- 打字机 -->
        <div class="mt-10 md:mt-12 grid grid-cols-12 gap-6 items-start opacity-0 animate-fade-in-up" style="animation-delay:.9s;animation-fill-mode:forwards;">
          <div class="col-span-12 md:col-span-8">
            <div class="flex items-start gap-4 md:gap-6">
              <span class="shrink-0 font-mono text-[0.7rem] tracking-[0.3em] text-light-muted/60 dark:text-dark-muted/60 pt-2">
                — 当前身份
              </span>
              <div>
                <p class="font-display italic text-xl md:text-2xl leading-relaxed text-light-text dark:text-dark-text min-h-[3.5rem]">
                  {{ startedTyping ? '›' : '' }}&nbsp;{{ typed }}<span v-if="startedTyping" class="animate-blink-cursor text-light-accent dark:text-dark-accent">▍</span>
                </p>
                <p v-if="profile" class="mt-5 md:mt-6 max-w-xl text-sm md:text-[0.95rem] leading-[1.85] text-light-muted dark:text-dark-muted">
                  {{ profile.slogan }}
                </p>
              </div>
            </div>
          </div>

          <!-- 联系方式（右侧清单） -->
          <div class="col-span-12 md:col-span-4 md:pl-6 md:border-l border-light-rule dark:border-dark-rule">
            <p class="font-mono text-[0.65rem] tracking-[0.3em] text-light-muted/60 dark:text-dark-muted/60 mb-4">
              — 联系方式
            </p>
            <ul class="space-y-3">
              <li v-for="c in contacts" :key="c.id">
                <a
                  v-if="resolveContactLink(c)"
                  :href="resolveContactLink(c)"
                  target="_blank"
                  class="group flex items-center justify-between py-1 border-b border-light-rule/0 hover:border-light-rule/60 dark:hover:border-dark-rule/60 transition-colors"
                >
                  <span class="flex items-center gap-3">
                    <span class="text-sm">{{ c.icon }}</span>
                    <span class="text-sm text-light-text dark:text-dark-text group-hover:text-light-accent dark:group-hover:text-dark-accent transition-colors">
                      {{ c.platform }}
                    </span>
                  </span>
                  <span class="text-xs md:text-sm text-light-muted dark:text-dark-muted group-hover:text-light-accent dark:group-hover:text-dark-accent transition-colors max-w-[60%] truncate">
                    {{ resolveContactDisplay(c) }}
                  </span>
                  <svg class="w-3.5 h-3.5 text-light-muted/50 dark:text-dark-muted/50 -translate-x-2 opacity-0 group-hover:translate-x-0 group-hover:opacity-100 transition-all ml-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3"/></svg>
                </a>
                <div v-else class="flex items-center justify-between py-1 text-sm">
                  <span class="flex items-center gap-3">
                    <span>{{ c.icon }}</span>
                    <span class="text-light-muted dark:text-dark-muted">{{ c.platform }}</span>
                  </span>
                  <span class="text-light-text dark:text-dark-text text-xs md:text-sm">{{ c.value }}</span>
                </div>
              </li>
            </ul>
          </div>
        </div>

        <!-- CTA 按钮行 -->
        <div class="mt-14 md:mt-16 flex flex-wrap items-center gap-5 opacity-0 animate-fade-in-up" style="animation-delay:1.15s;animation-fill-mode:forwards;">
          <a href="#projects" class="btn-accent">
            <span>浏览作品集</span>
            <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 14l-7 7m0 0l-7-7m7 7V3"/></svg>
          </a>
          <!-- 简历下载：显式加 download 属性，避免 Chrome 直接预览 PDF（用户以为没下载） -->
          <a
            v-if="profile && profile.resumeUrl"
            :href="profile.resumeUrl"
            :download="(profile.name ? profile.name + '_简历' : 'resume') + resolveDownloadExt(profile.resumeUrl)"
            rel="noopener noreferrer"
            class="btn-ghost"
            @click="onDownloadResume"
          >
            <span>下载简历 PDF</span>
            <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3M3 17V7a2 2 0 012-2h6l2 2h6a2 2 0 012 2v8a2 2 0 01-2 2H5a2 2 0 01-2-2z"/></svg>
          </a>
          <button
            v-else-if="profile && !profile.resumeUrl"
            type="button"
            disabled
            class="btn-ghost opacity-50 cursor-not-allowed"
            title="管理员暂未上传简历文件"
          >
            <span>简历暂未上架</span>
            <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728L5.636 5.636"/></svg>
          </button>
        </div>

        <!-- 数据统计行 -->
        <div class="mt-20 md:mt-28 grid grid-cols-2 md:grid-cols-4 gap-0 border-y border-light-rule dark:border-dark-rule opacity-0 animate-fade-in-up" style="animation-delay:1.35s;animation-fill-mode:forwards;">
          <div
            v-for="(s, i) in statCounts"
            :key="s.label"
            class="relative py-8 md:py-9 px-4 md:px-8"
            :class="i !== 0 ? 'md:border-l border-light-rule dark:border-dark-rule' : ''"
          >
            <span class="absolute top-4 left-4 text-[0.6rem] tracking-[0.3em] uppercase font-mono text-light-muted/50 dark:text-dark-muted/50">
              图 0{{ i + 1 }}
            </span>
            <div class="font-heading text-[2.75rem] md:text-[3.25rem] leading-none text-light-text dark:text-dark-text tabular-nums mt-3">
              {{ s.num }}
            </div>
            <div class="mt-3 text-[0.65rem] tracking-[0.3em] uppercase font-mono text-light-muted dark:text-dark-muted">
              {{ s.label }}
            </div>
          </div>
        </div>
      </div>

      <!-- 底部滚动提示 -->
      <div class="mt-16 md:mt-20 flex items-center justify-between footnote opacity-0 animate-fade-in" style="animation-delay:1.6s;animation-fill-mode:forwards;">
        <a href="#about" class="group flex items-center gap-3 hover:text-light-accent dark:hover:text-dark-accent transition-colors">
          <span class="relative block w-10 h-[1px] bg-light-muted/50 dark:bg-dark-muted/50 overflow-hidden">
            <span class="absolute inset-0 bg-light-accent dark:bg-dark-accent -translate-x-full group-hover:translate-x-0 transition-transform duration-700"></span>
          </span>
          <span>向下滚动继续阅读</span>
          <svg class="w-3 h-3 animate-bounce" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 14l-7 7m0 0l-7-7m7 7V3"/></svg>
        </a>
      </div>
    </div>
  </section>
</template>
