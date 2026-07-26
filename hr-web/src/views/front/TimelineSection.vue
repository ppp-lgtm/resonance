<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useAppStore } from '../../stores/app'
import { useInView } from '../../composables'
import type { Award, Education } from '../../types'

const app = useAppStore()
const { target: _ref, isInView } = useInView(0.06)

onMounted(() => { app.fetchAll() })

const awards = computed<Award[]>(() =>
  (app.awards ?? []).map(a => ({
    ...a,
    certUrl: (a as any).certUrl ?? (a as any).certificateUrl,
  })))
const edu    = computed<Education[]>(() => app.education ?? [])

type TimelineRow =
  | { kind: 'award';     id: number; title: string; issuer: string; date: string; desc: string; certUrl: string; coverUrl?: string }
  | { kind: 'education'; id: number; school: string; degree: string; major: string; start: string; end: string; desc: string }

const rows = computed<TimelineRow[]>(() => {
  const a: TimelineRow[] = awards.value.map(x => ({
    kind: 'award', id: x.id, title: x.title, issuer: x.issuer, date: x.date,
    desc: x.description ?? '', certUrl: x.certUrl ?? '', coverUrl: x.coverUrl,
  }))
  const e: TimelineRow[] = edu.value.map(x => ({
    kind: 'education', id: x.id, school: x.school, degree: x.degree, major: x.major,
    start: x.startDate, end: x.endDate ?? x.startDate, desc: x.description ?? '',
  }))
  const byDate = (x: TimelineRow) => x.kind === 'award' ? x.date : x.start
  return [...a, ...e].sort((a, b) => (byDate(b) || '').localeCompare(byDate(a) || ''))
})
</script>

<template>
  <section id="timeline" ref="_ref" class="relative bg-paper border-t border-light-rule dark:border-dark-rule">
    <div class="mx-auto max-w-[1400px] px-6 md:px-16 lg:px-20 py-24 md:py-32">
      <!-- HEADER -->
      <header class="grid grid-cols-12 gap-6 items-end mb-14 md:mb-20">
        <div class="col-span-12 md:col-span-5">
          <p class="sect-eyebrow sect-eyebrow--no-line mb-4">— 人生履历</p>
          <h2 class="sect-title md:text-[3.4rem]">
            荣誉奖项<em>与</em> 教育背景
          </h2>
        </div>
        <p class="col-span-12 md:col-span-6 md:col-start-7 text-sm md:text-[0.95rem] leading-[1.9] text-light-muted dark:text-dark-muted">
          一份按时间倒序排列的荣誉与学业记录。每一个条目，都代表着一段真实付出的历程——而不仅仅是一张证书。
        </p>
      </header>

      <!-- TABLE-LIKE HEAD -->
      <div class="hidden md:grid grid-cols-12 gap-6 px-4 py-3 border-y border-light-rule dark:border-dark-rule items-center">
        <div class="col-span-1 footnote">年份</div>
        <div class="col-span-1 footnote">类别</div>
        <div class="col-span-6 footnote">标题 / 院校机构</div>
        <div class="col-span-3 footnote">详细说明</div>
        <div class="col-span-1 footnote text-right">序号</div>
      </div>

      <!-- TIMELINE -->
      <div class="relative md:px-4">
        <!-- accent rule down the gutter -->
        <div class="t-line hidden md:block md:left-[4.166%]"></div>
        <div class="t-line md:hidden"></div>

        <div v-for="(row, idx) in rows" :key="row.kind + row.id" class="relative group">
          <!-- dot -->
          <span class="t-dot" style="top: 2.25rem;"></span>

          <div
            class="grid grid-cols-12 gap-4 md:gap-6 py-8 md:py-9 ml-10 md:ml-0 border-b border-light-rule/70 dark:border-dark-rule/70"
            :class="isInView ? 'animate-fade-in-up opacity-0' : 'opacity-0'"
            style="animation-fill-mode:forwards;animation-duration:.7s;"
            :style="{ animationDelay: `${idx * 80}ms` }"
          >
            <!-- YEAR -->
            <div class="col-span-3 md:col-span-1 pt-1">
              <div class="font-mono text-sm md:text-base tabular-nums text-light-text dark:text-dark-text leading-none">
                {{ (row.kind === 'award' ? row.date : row.start).slice(0, 4) }}
              </div>
              <div class="mt-1 text-[0.6rem] tracking-[0.25em] uppercase font-mono text-light-muted/60 dark:text-dark-muted/60">
                {{ row.kind === 'award' ? row.date : `${row.start.slice(5)} — ${row.end.slice(5)}` }}
              </div>
            </div>

            <!-- TYPE PILL -->
            <div class="hidden md:flex col-span-1 items-start pt-1.5">
              <span
                v-if="row.kind === 'award'"
                class="inline-flex items-center gap-1.5 text-[0.62rem] tracking-[0.25em] uppercase font-mono px-2 py-1 bg-light-accent/10 dark:bg-dark-accent/10 text-light-accent dark:text-dark-accent"
              >
                <span class="w-1 h-1 rounded-full bg-light-accent dark:bg-dark-accent"></span>
                荣誉
              </span>
              <span
                v-else
                class="inline-flex items-center gap-1.5 text-[0.62rem] tracking-[0.25em] uppercase font-mono px-2 py-1 bg-light-accent2/15 dark:bg-dark-accent2/15 text-light-accent2 dark:text-dark-accent2"
              >
                <span class="w-1 h-1 rounded-full bg-light-accent2 dark:bg-dark-accent2"></span>
                学历
              </span>
            </div>

            <!-- TITLE / SCHOOL (+ award cover 杂志风缩略图) -->
            <div class="col-span-9 md:col-span-6">
              <div class="md:hidden mb-2">
                <span
                  v-if="row.kind === 'award'"
                  class="inline-flex items-center gap-1.5 text-[0.6rem] tracking-[0.25em] uppercase font-mono px-2 py-0.5 bg-light-accent/10 dark:bg-dark-accent/10 text-light-accent dark:text-dark-accent"
                >
                  荣誉
                </span>
                <span
                  v-else
                  class="inline-flex items-center gap-1.5 text-[0.6rem] tracking-[0.25em] uppercase font-mono px-2 py-0.5 bg-light-accent2/15 dark:bg-dark-accent2/15 text-light-accent2 dark:text-dark-accent2"
                >
                  学历
                </span>
              </div>

              <!-- 奖项 + 有封面图 → 杂志风「照片+标题」双栏排版 -->
              <template v-if="row.kind === 'award' && row.coverUrl">
                <div class="flex items-start gap-4 md:gap-5">
                  <!-- Cover 卡片（瑞士杂志风：1px 细边 + 编号角条 + 悬停朱砂红描边） -->
                  <a
                    :href="row.coverUrl"
                    target="_blank"
                    rel="noopener noreferrer"
                    class="group/cover shrink-0 relative w-28 md:w-36 overflow-hidden rounded-md border border-light-rule/80 dark:border-dark-rule/80 bg-white dark:bg-dark-card shadow-[0_1px_0_rgba(0,0,0,0.04)] hover:border-[#B23A2E] hover:shadow-[0_8px_24px_-12px_rgba(178,58,46,0.35)] transition-all duration-300 hover:-translate-y-0.5"
                    style="aspect-ratio: 4 / 3;"
                    :title="row.title + ' · 封面原图'"
                  >
                    <img
                      :src="row.coverUrl"
                      :alt="row.title + ' 荣誉封面'"
                      class="w-full h-full object-cover transition-transform duration-500 group-hover/cover:scale-[1.04]"
                      loading="lazy"
                    />
                    <!-- 杂志编号角条 → 右上角 §N / honor-cover -->
                    <span class="absolute top-2 right-2 inline-flex items-center gap-1 px-1.5 py-0.5 bg-black/55 backdrop-blur-[1px] text-white font-mono text-[9px] tracking-[0.22em] uppercase rounded-sm">
                      §&nbsp;{{ String(idx + 1).padStart(2, '0') }}
                    </span>
                    <!-- 悬停 → 左下「VIEW」小胶囊 -->
                    <span class="absolute left-2 bottom-2 inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-[#B23A2E] text-white text-[9px] font-mono tracking-[0.2em] uppercase opacity-0 group-hover/cover:opacity-100 transition-opacity duration-300">
                      View →
                    </span>
                  </a>
                  <!-- 标题文字区 -->
                  <div class="min-w-0 flex-1 pt-0.5">
                    <h3 class="font-heading text-xl md:text-[1.5rem] leading-[1.2] text-light-text dark:text-dark-text group-hover:text-light-accent dark:group-hover:text-dark-accent transition-colors duration-400">
                      {{ row.title }}
                    </h3>
                    <p class="mt-2 text-[0.8rem] md:text-sm text-light-muted dark:text-dark-muted">
                      {{ row.issuer }}
                    </p>
                    <!-- 证书链接（如果同时有 cert 和 cover，放在封面下做小字行） -->
                    <a
                      v-if="row.certUrl"
                      :href="row.certUrl"
                      target="_blank"
                      rel="noopener noreferrer"
                      class="inline-flex items-center gap-1 mt-2 text-[10px] font-mono tracking-[0.2em] uppercase text-[#B23A2E] hover:underline underline-offset-4"
                    >
                      🔗&nbsp;证书链接
                    </a>
                  </div>
                </div>
              </template>

              <!-- 奖项无封面 / 教育经历 → 保持原排版 -->
              <template v-else>
                <h3 class="font-heading text-xl md:text-[1.5rem] leading-[1.2] text-light-text dark:text-dark-text group-hover:text-light-accent dark:group-hover:text-dark-accent transition-colors duration-400">
                  {{ row.kind === 'award' ? row.title : row.school }}
                </h3>
                <p class="mt-2 text-[0.8rem] md:text-sm text-light-muted dark:text-dark-muted">
                  <template v-if="row.kind === 'award'">
                    {{ row.issuer }}
                    <a
                      v-if="row.certUrl"
                      :href="row.certUrl"
                      target="_blank"
                      rel="noopener noreferrer"
                      class="ml-2 inline-flex items-center gap-1 text-[10px] font-mono tracking-[0.2em] uppercase text-[#B23A2E] hover:underline underline-offset-4 align-middle"
                    >
                      🔗&nbsp;证书
                    </a>
                  </template>
                  <template v-else>{{ row.degree }} · {{ row.major }}</template>
                </p>
              </template>
            </div>

            <!-- DESCRIPTION -->
            <div class="col-span-12 md:col-span-3 md:pt-1 md:border-l border-light-rule/50 dark:border-dark-rule/50 md:pl-6">
              <p class="text-[0.85rem] md:text-[0.88rem] leading-[1.85] text-light-muted dark:text-dark-muted">
                {{ row.desc }}
              </p>
            </div>

            <!-- INDEX -->
            <div class="hidden md:flex col-span-1 items-start justify-end pt-1">
              <span class="font-mono text-[0.65rem] tracking-[0.3em] text-light-muted/50 dark:text-dark-muted/50 tabular-nums">
                编号 {{ String(idx + 1).padStart(2,'0') }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- SIGN OFF -->
      <div class="mt-16 md:mt-24 grid grid-cols-12 gap-6 items-center">
        <div class="col-span-12 md:col-span-4">
          <div class="font-display italic text-2xl md:text-3xl text-light-text dark:text-dark-text">
            “更多篇章，尚在书写。”
          </div>
        </div>
        <div class="col-span-12 md:col-span-8 rule"></div>
        <div class="col-span-12 md:col-span-4 md:col-start-9 text-right">
          <span class="footnote">— 未完待续</span>
        </div>
      </div>
    </div>
  </section>
</template>
