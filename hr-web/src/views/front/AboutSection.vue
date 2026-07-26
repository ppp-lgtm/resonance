<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useAppStore } from '../../stores/app'
import { useInView } from '../../composables'

const app = useAppStore()
const copied = ref<number | null>(null)

const { target: _ref, isInView } = useInView(0.08)

onMounted(() => { app.fetchAll() })

async function copyValue(v: string, id: number) {
  try { await navigator.clipboard.writeText(v) } catch { /* noop */ }
  copied.value = id
  setTimeout(() => copied.value = null, 1500)
}
</script>

<template>
  <!-- 板块 01 / 关于我 -->
  <section id="about" ref="_ref" class="relative bg-paper border-t border-light-rule dark:border-dark-rule">
    <!-- 横向滚动跑马灯 -->
    <div class="border-b border-light-rule dark:border-dark-rule overflow-hidden">
      <div class="ticker py-3 mask-fade-x">
        <div class="ticker-track">
          <span class="ticker-item">编辑设计</span>
          <span class="ticker-item">全栈工程</span>
          <span class="ticker-item">人本交互</span>
          <span class="ticker-item">开源贡献</span>
          <span class="ticker-item">系统思考</span>
          <span class="ticker-item">字体排印</span>
          <span class="ticker-item">性能优化</span>
          <span class="ticker-item">创意编程</span>
        </div>
        <div class="ticker-track" aria-hidden="true">
          <span class="ticker-item">编辑设计</span>
          <span class="ticker-item">全栈工程</span>
          <span class="ticker-item">人本交互</span>
          <span class="ticker-item">开源贡献</span>
          <span class="ticker-item">系统思考</span>
          <span class="ticker-item">字体排印</span>
          <span class="ticker-item">性能优化</span>
          <span class="ticker-item">创意编程</span>
        </div>
      </div>
    </div>

    <div class="mx-auto max-w-[1400px] px-6 md:px-16 lg:px-20 py-24 md:py-32">
      <div class="grid grid-cols-12 gap-6 md:gap-10">
        <!-- 左侧元信息 -->
        <aside class="col-span-12 md:col-span-3 order-2 md:order-1">
          <div class="md:sticky md:top-32 space-y-8">
            <h2 class="sect-title !text-heading-lg md:!text-[1.83rem] md:!leading-[1.15]">
              一段简短的<em>自述</em><br/>关于我
            </h2>
            <div class="rule-accent w-14"></div>
            <dl class="space-y-5 text-sm">
              <div class="flex justify-between gap-4 border-b border-light-rule dark:border-dark-rule pb-3">
                <dt class="footnote !tracking-[0.2em]">所在地</dt>
                <dd class="text-light-text dark:text-dark-text font-medium">{{ app.profile?.location || '—' }}</dd>
              </div>
              <div class="flex justify-between gap-4 border-b border-light-rule dark:border-dark-rule pb-3">
                <dt class="footnote !tracking-[0.2em]">专注方向</dt>
                <dd class="text-light-text dark:text-dark-text font-medium text-right leading-[1.7]">
                  {{ (app.profile?.focusAreas ?? []).length ? app.profile!.focusAreas!.join(' · ') : '—' }}
                </dd>
              </div>
              <div class="flex justify-between gap-4 border-b border-light-rule dark:border-dark-rule pb-3">
                <dt class="footnote !tracking-[0.2em]">从业经验</dt>
                <dd class="text-light-text dark:text-dark-text font-medium tabular-nums">
                  {{ (app.profile?.yearsExperience ?? 0) > 0 ? `${app.profile!.yearsExperience}+ 年` : '—' }}
                </dd>
              </div>
              <div class="flex justify-between gap-4 pb-3">
                <dt class="footnote !tracking-[0.2em]">合作模式</dt>
                <dd class="text-light-text dark:text-dark-text font-medium text-right leading-[1.7] max-w-[60%]">
                  {{ app.profile?.workingMode || '远程 / 驻场 / 咨询' }}
                </dd>
              </div>
            </dl>
          </div>
        </aside>

        <!-- 右侧正文介绍 -->
        <article
          class="col-span-12 md:col-span-9 order-1 md:order-2 md:pl-12 md:border-l border-light-rule dark:border-dark-rule"
          :class="isInView ? 'animate-fade-in-up' : 'opacity-0'"
          style="animation-fill-mode:forwards;animation-duration:.9s;"
        >
          <span class="font-heading text-5xl md:text-6xl float-left mr-2 leading-none mt-1 text-light-accent dark:text-dark-accent">“</span>
<p class="font-display italic text-2xl md:text-[2rem] leading-[1.45] text-light-text dark:text-dark-text">
  在严谨的工程与编辑设计的交汇处，构建有温度的软件。执着于那些没人注意到的细节——直到它们消失的那一刻。
  <span class="   text-5xl font-heading text-light-accent dark:text-dark-accent">”</span>
</p>
            
          <div class="mt-10 text-[0.95rem] leading-[1.9] text-light-muted dark:text-dark-muted">
  <p>
    我是一名专注于全栈 Web 应用的软件工程师兼设计者。我的工作融合了扎实的计算机科学学术基础，以及对字体、网格、交互工艺的深切热爱。
  </p>
</div>

          <p v-if="app.profile" class="mt-10 max-w-2xl text-[0.95rem] leading-[1.95] text-light-muted dark:text-dark-muted border-l-2 border-light-accent dark:border-dark-accent pl-6">
            {{ app.profile.bio }}
          </p>
        </article>
      </div>
    </div>
  </section>

  <!-- 板块 02 / 联系方式卡片 -->
  <section
    id="contact"
    class="relative bg-light-card dark:bg-dark-card border-y border-light-rule dark:border-dark-rule"
  >
    <div class="mx-auto max-w-[1400px] px-6 md:px-16 lg:px-20 py-20 md:py-24">
      <div class="flex items-end justify-between mb-10 md:mb-14 gap-8 flex-wrap">
        <div :class="isInView ? 'animate-fade-in-left' : 'opacity-0'" style="animation-fill-mode:forwards;animation-duration:.8s;">
          <p class="sect-eyebrow sect-eyebrow--no-line mb-4"> — 直接联络</p>
          <h3 class="font-heading text-3xl md:text-5xl leading-[1] text-light-text dark:text-dark-text">
            来<em class="font-display italic text-light-accent dark:text-dark-accent">聊聊 </em>吧。
          </h3>
        </div>
        <p class="max-w-sm text-sm text-light-muted dark:text-dark-muted leading-[1.8]">
          选一个你觉得舒服的方式就好。每一封真诚的来信，我都会在两个工作日内认真回复。
        </p>
      </div>

      <!-- 方格卡片（纯线性、无圆角、无渐变） -->
      <div
        class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 border border-light-rule dark:border-dark-rule"
        style="--tile-line:rgb(0 0 0 / 0.08)"
      >
        <div
          v-for="(c, i) in app.contacts"
          :key="c.id"
          class="group relative bg-light-bg/60 dark:bg-dark-bg/40 p-8 md:p-10 transition-all duration-500 tile-border"
          :class="i === 0 ? '!shadow-none sm:shadow-none' : ''"
          :style="{ '--tile-line': isInView ? 'rgba(0,0,0,0.08)' : 'transparent' }"
          @click="c.copyable && copyValue(c.value, c.id)"
        >
          <div class="absolute inset-0 origin-left scale-x-0 group-hover:scale-x-100 transition-transform duration-700 ease-out bg-light-text/[0.03] dark:bg-dark-text/[0.04] pointer-events-none"></div>
          <div class="relative flex flex-col h-full min-h-[10rem]">
            <div class="flex items-start justify-between mb-8">
              <span class="text-2xl md:text-3xl">{{ c.icon }}</span>
              <span class="font-mono text-[0.6rem] tracking-[0.3em] text-light-muted/50 dark:text-dark-muted/50">
                0{{ i + 1 }}
              </span>
            </div>

            <p class="footnote !tracking-[0.25em] mb-2">{{ c.platform }}</p>
            <p class="font-display italic text-lg md:text-xl text-light-text dark:text-dark-text mb-3 break-all">
              {{ c.value }}
            </p>

            <div class="mt-auto flex items-center justify-between">
              <div v-if="c.link" class="flex items-center gap-2">
                <a
                  :href="c.link"
                  target="_blank"
                  class="text-[0.7rem] tracking-[0.25em] uppercase font-mono text-light-text dark:text-dark-text border-b border-light-text/30 dark:border-dark-text/30 hover:border-light-accent dark:hover:border-dark-accent hover:text-light-accent dark:hover:text-dark-accent pb-0.5 transition-colors"
                >
                  去看看 →
                </a>
              </div>
              <div v-else-if="c.copyable" class="flex items-center gap-2">
                <span
                  class="text-[0.7rem] tracking-[0.25em] uppercase font-mono transition-all duration-500"
                  :class="copied === c.id
                    ? 'text-light-accent dark:text-dark-accent'
                    : 'text-light-muted dark:text-dark-muted group-hover:text-light-text dark:group-hover:text-dark-text'"
                >
                  {{ copied === c.id ? '已复制 ✓' : '点击复制' }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
