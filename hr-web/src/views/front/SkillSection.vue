<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { useAppStore } from '../../stores/app'
import { useInView } from '../../composables'

const app = useAppStore()
const { target: _ref, isInView } = useInView(0.05)
const active = ref<string>('全部')

/* 真实数据优先，空时 fallback 到空数组（不依赖 mockData） */
const skills = computed(() => app.skills ?? [])
const categories = computed(() => {
  const cats = ['全部', ...Array.from(new Set(skills.value.map(s => s.category)))]
  return cats.length > 1 ? cats : ['全部']
})

const filtered = computed(() => {
  if (active.value === '全部') return skills.value
  return skills.value.filter(s => s.category === active.value)
})

const revealKey = ref(0)
watch(active, () => { revealKey.value++; nextTick() })
</script>

<template>
  <section id="skills" ref="_ref" class="relative bg-paper border-t border-light-rule dark:border-dark-rule">
    <div class="mx-auto max-w-[1400px] px-6 md:px-16 lg:px-20 py-24 md:py-32">
      <!-- HEADER -->
      <div class="grid grid-cols-12 gap-6 mb-14 md:mb-20">
        <div class="col-span-12 md:col-span-4">
          <p class="sect-eyebrow sect-eyebrow--no-line mb-4"> — 技能仓库</p>
          <h2 class="sect-title md:text-[3.2rem]">
            我的<em>工具箱</em>。
          </h2>
        </div>

        <div class="col-span-12 md:col-span-5 md:pt-4">
          <p class="text-sm md:text-[0.95rem] leading-[1.9] text-light-muted dark:text-dark-muted">
            一份动态更新的清单，罗列了我日常工作中会使用到的语言、框架与系统。熟练度以实际交付项目为衡量标准——而非刷过的教程数量。
          </p>
        </div>

        <div class="col-span-12 md:col-span-3 md:pt-4">
          <div class="flex flex-wrap gap-3 md:justify-end">
            <button
              v-for="cat in categories"
              :key="cat"
              type="button"
              class="pill-cat !text-sm !font-semibold !tracking-[0.15em] px-3.5 py-1.5 rounded-md transition-all"
              :class="active === cat
                ? '!bg-light-accent !text-light-bg dark:!bg-dark-accent dark:!text-dark-bg shadow-sm'
                : 'border border-light-rule dark:border-dark-rule hover:!bg-light-card dark:hover:!bg-dark-card !text-light-text dark:!text-dark-text'"
              @click="active = cat"
            >
              {{ cat }}
            </button>
          </div>
        </div>
      </div>

      <!-- HEADER ROW (table head swiss style) -->
      <div class="hidden md:grid grid-cols-12 gap-3 lg:gap-6 items-center px-4 py-3 border-y border-light-rule dark:border-dark-rule">
        <div class="col-span-1 footnote">序号</div>
        <div class="col-span-1 footnote">图标</div>
        <div class="col-span-4 footnote">名称</div>
        <div class="col-span-2 footnote">分类</div>
        <div class="col-span-3 footnote">熟练度</div>
        <div class="col-span-1 footnote text-right">进度</div>
      </div>

      <!-- SKILL LIST -->
      <div :key="revealKey">
        <div
          v-for="(s, i) in filtered"
          :key="s.id + revealKey"
          class="skill-row px-4 group"
          :class="isInView ? 'animate-fade-in-up opacity-0' : 'opacity-0'"
          style="animation-fill-mode:forwards;animation-duration:.55s;"
          :style="{ animationDelay: `${i * 45}ms` }"
        >
          <!-- # -->
          <div class="col-span-1 md:col-span-1 flex items-center">
            <span class="project-index tabular-nums">
              {{ String(i + 1).padStart(2, '0') }}
            </span>
          </div>

          <!-- icon -->
          <div class="col-span-2 md:col-span-1 flex items-center">
            <span class="text-2xl md:text-3xl transition-transform duration-500 group-hover:-translate-y-0.5 group-hover:scale-110">
              {{ s.icon }}
            </span>
          </div>

          <!-- name -->
          <div class="col-span-9 sm:col-span-6 md:col-span-4 flex items-center">
            <span class="font-heading text-xl md:text-2xl text-light-text dark:text-dark-text group-hover:text-light-accent dark:group-hover:text-dark-accent transition-colors duration-300">
              {{ s.name }}
            </span>
          </div>

          <!-- category (mobile hidden short, desktop full) -->
          <div class="hidden md:flex col-span-2 items-center">
            <span class="pill-tag !py-0.5 !border-light-rule dark:!border-dark-rule">
              {{ s.category }}
            </span>
          </div>

          <!-- bar -->
          <div class="col-span-10 sm:col-span-9 md:col-span-3 flex items-center gap-4 order-last col-start-1 md:order-none md:col-start-auto">
            <div class="skill-bar-track flex-1">
              <div
                class="skill-bar-fill transition-[width] duration-[1200ms] ease-out"
                :style="{ width: isInView ? s.proficiency + '%' : '0%' }"
              ></div>
            </div>
          </div>

          <!-- % -->
          <div class="col-span-2 sm:col-span-3 md:col-span-1 flex items-center justify-end md:justify-end">
            <span class="font-mono text-sm tabular-nums text-light-muted dark:text-dark-muted group-hover:text-light-text dark:group-hover:text-dark-text transition-colors duration-300">
              {{ String(s.proficiency).padStart(2, '0') }}
            </span>
          </div>
        </div>
      </div>

      <!-- LEGEND -->
      <div class="mt-14 md:mt-20 grid grid-cols-12 gap-4 pt-8 border-t border-light-rule dark:border-dark-rule text-[0.7rem] tracking-[0.25em] uppercase font-mono">
        <div class="col-span-6 md:col-span-4 flex items-center gap-3 text-light-muted dark:text-dark-muted">
          <span class="inline-block w-12 h-[1.5px] bg-light-text/30 dark:bg-dark-text/30"></span>
          入门级
        </div>
        <div class="col-span-6 md:col-span-4 flex items-center gap-3 text-light-muted dark:text-dark-muted">
          <span class="inline-block w-12 h-[1.5px] bg-light-text/60 dark:bg-dark-text/60"></span>
          熟练级
        </div>
        <div class="col-span-12 md:col-span-4 flex items-center gap-3 md:justify-end text-light-muted dark:text-dark-muted">
          <span class="inline-block w-12 h-[1.5px] bg-light-text dark:bg-dark-text"></span>
          专家级
        </div>
      </div>
    </div>
  </section>
</template>
