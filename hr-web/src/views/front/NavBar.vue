<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useThemeStore } from '../../stores/theme'

const themeStore = useThemeStore()
const scrolled = ref(false)
const mobileOpen = ref(false)
const clock = ref('')
let tickTimer: number | null = null

onMounted(() => {
  const onScroll = () => { scrolled.value = window.scrollY > 40 }
  window.addEventListener('scroll', onScroll, { passive: true })
  onScroll()
  tickTimer = window.setInterval(() => {
    const d = new Date()
    const hh = String(d.getHours()).padStart(2, '0')
    const mm = String(d.getMinutes()).padStart(2, '0')
    clock.value = `${hh}:${mm}`
  }, 1000 * 30)
  const d = new Date()
  clock.value = `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
})
onBeforeUnmount(() => {
  if (tickTimer) window.clearInterval(tickTimer)
})

const navItems = [
  { label: '关于',     href: '#about'    },
  { label: '技能',     href: '#skills'   },
  { label: '作品',     href: '#projects' },
  { label: '履历',     href: '#timeline' },
]

function handleClick(href: string) {
  if (!href) return
  mobileOpen.value = false
  const id = href.replace('#', '')
  const el = document.getElementById(id)
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
}
</script>

<template>
  <header
    class="fixed top-0 inset-x-0 z-50 transition-all duration-500"
    :class="scrolled
      ? 'bg-light-bg/85 dark:bg-dark-bg/85 backdrop-blur border-b border-light-rule/60 dark:border-dark-rule/60'
      : 'bg-transparent'"
  >
    <nav class="mx-auto max-w-[1400px] px-6 md:px-10 lg:px-14">
      <div class="grid grid-cols-12 items-center h-16 md:h-20">
        <!-- LEFT: Index + time -->
        <div class="col-span-4 md:col-span-3 flex items-center gap-5">
          <span class="hidden md:inline font-mono text-[0.65rem] tracking-[0.25em] text-light-muted/70 dark:text-dark-muted/70">
            N°{{ clock }}
          </span>
          <a
            href="#"
            @click.prevent="handleClick('')"
            class="group flex items-center gap-2.5"
          >
            <span class="block w-2.5 h-2.5 bg-light-accent dark:bg-dark-accent transition-transform duration-500 group-hover:rotate-45"></span>
            <span class="font-heading text-xl md:text-[1.35rem] leading-none text-light-text dark:text-dark-text">
              Resonance<em class="font-display italic text-light-accent dark:text-dark-accent">.</em>
            </span>
          </a>
        </div>

        <!-- CENTER: Nav Links -->
        <div class="hidden md:flex col-span-6 items-center justify-center gap-10">
          <a
            v-for="item in navItems"
            :key="item.href"
            :href="item.href"
            class="nav-link-swiss"
            @click.prevent="handleClick(item.href)"
          >
            {{ item.label }}
          </a>
        </div>

        <!-- RIGHT: Theme toggle + Admin -->
        <div class="col-span-8 md:col-span-3 flex items-center justify-end gap-5">
          

          <button
            type="button"
            @click="themeStore.toggle()"
            aria-label="Toggle theme"
            class="group relative flex items-center h-9 w-9 border border-light-rule dark:border-dark-rule hover:border-light-text/50 dark:hover:border-dark-text/50 transition-colors"
          >
            <span class="sr-only">Toggle theme</span>
            <div class="flex items-center justify-center w-full h-full relative">
              <span
                class="absolute text-[0.85rem] transition-all duration-500"
                :class="themeStore.isDark ? 'opacity-0 -translate-y-2' : 'opacity-100 translate-y-0'"
              >
                ☉
              </span>
              <span
                class="absolute text-[0.85rem] transition-all duration-500"
                :class="themeStore.isDark ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-2'"
              >
                ☾
              </span>
            </div>
          </button>

          <button
            type="button"
            class="md:hidden flex items-center justify-center w-9 h-9 border border-light-rule dark:border-dark-rule"
            @click="mobileOpen = !mobileOpen"
            aria-label="Menu"
          >
            <span class="block w-4 relative h-[11px]">
              <span
                class="absolute left-0 right-0 h-px bg-light-text dark:bg-dark-text transition-all duration-300"
                :class="mobileOpen ? 'top-1/2 -translate-y-1/2 rotate-45' : 'top-0'"
              ></span>
              <span
                class="absolute left-0 right-0 top-1/2 -translate-y-1/2 h-px bg-light-text dark:bg-dark-text transition-opacity duration-300"
                :class="mobileOpen ? 'opacity-0' : 'opacity-100'"
              ></span>
              <span
                class="absolute left-0 right-0 h-px bg-light-text dark:bg-dark-text transition-all duration-300"
                :class="mobileOpen ? 'top-1/2 -translate-y-1/2 -rotate-45' : 'bottom-0'"
              ></span>
            </span>
          </button>
        </div>
      </div>

      <!-- MOBILE PANEL -->
      <div
        v-if="mobileOpen"
        class="md:hidden border-t border-light-rule dark:border-dark-rule -mx-6 px-6 md:-mx-10 md:px-10 bg-light-bg dark:bg-dark-bg"
      >
        <div class="py-8 space-y-5">
          <a
            v-for="item in navItems"
            :key="item.href"
            :href="item.href"
            class="flex items-center justify-between group"
            @click.prevent="handleClick(item.href)"
          >
            <span class="text-[0.85rem] tracking-[0.25em] uppercase text-light-text dark:text-dark-text">
              {{ item.label }}
            </span>
            <svg class="w-4 h-4 text-light-muted/50 dark:text-dark-muted/50 group-hover:text-light-accent dark:group-hover:text-dark-accent transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3"/></svg>
          </a>
          <div class="rule my-4"></div>
          
        </div>
      </div>
    </nav>
  </header>
</template>
