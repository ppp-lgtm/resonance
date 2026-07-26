<script setup lang="ts">
</script>

<template>
  <footer class="relative bg-light-text dark:bg-dark-text text-light-bg dark:text-dark-bg overflow-hidden">
    <!-- 背景网格 -->
    <div class="pointer-events-none absolute inset-0 opacity-[0.07] bg-light-bg dark:bg-dark-bg"
         style="background-image:linear-gradient(to right, currentColor 1px, transparent 1px),linear-gradient(to bottom, currentColor 1px, transparent 1px);background-size: 56px 56px;"></div>

    <div class="relative mx-auto max-w-[1400px] px-6 md:px-16 lg:px-20 pt-20 pb-10">

      <!-- 4 栏信息 -->
      <div class="grid grid-cols-12 gap-8 py-14 md:py-16 border-b border-light-bg/15 dark:border-dark-bg/15">
        <div class="col-span-12 sm:col-span-6 md:col-span-4">
          <p class="footnote !text-light-bg/50 dark:!text-dark-bg/50 mb-4">工作室 / 品牌</p>
          <p class="font-heading text-2xl md:text-3xl leading-tight text-light-bg dark:text-dark-bg">
            共鸣<em class="italic font-display text-light-accent dark:text-dark-accent"></em>
          </p>
          <p class="mt-4 text-[0.88rem] leading-[1.85] text-light-bg/70 dark:text-dark-bg/70 max-w-xs">
            个人作品集与数字简历,独立编辑、排印、编码于安徽——以手工之心，细致打磨每一处。
          </p>
        </div>

        <div class="col-span-6 sm:col-span-3 md:col-span-2 md:col-start-6">
          <p class="footnote !text-light-bg/50 dark:!text-dark-bg/50 mb-5">目录导航</p>
          <ul class="space-y-3 text-sm">
            <li><a class="group inline-flex items-center gap-2 hover:text-light-accent dark:hover:text-dark-accent transition-colors" href="#about">
              关于<span class="w-4 h-px bg-light-bg/30 dark:bg-dark-bg/30 group-hover:w-8 group-hover:bg-light-accent dark:group-hover:bg-dark-accent transition-all"></span>
            </a></li>
            <li><a class="group inline-flex items-center gap-2 hover:text-light-accent dark:hover:text-dark-accent transition-colors" href="#skills">
              技能<span class="w-4 h-px bg-light-bg/30 dark:bg-dark-bg/30 group-hover:w-8 group-hover:bg-light-accent dark:group-hover:bg-dark-accent transition-all"></span>
            </a></li>
            <li><a class="group inline-flex items-center gap-2 hover:text-light-accent dark:hover:text-dark-accent transition-colors" href="#projects">
              作品<span class="w-4 h-px bg-light-bg/30 dark:bg-dark-bg/30 group-hover:w-8 group-hover:bg-light-accent dark:group-hover:bg-dark-accent transition-all"></span>
            </a></li>
            <li><a class="group inline-flex items-center gap-2 hover:text-light-accent dark:hover:text-dark-accent transition-colors" href="#timeline">
              履历<span class="w-4 h-px bg-light-bg/30 dark:bg-dark-bg/30 group-hover:w-8 group-hover:bg-light-accent dark:group-hover:bg-dark-accent transition-all"></span>
            </a></li>
          </ul>
        </div>

        <div class="col-span-6 sm:col-span-3 md:col-span-3">
          <p class="footnote !text-light-bg/50 dark:!text-dark-bg/50 mb-5">外部链接</p>
          <ul class="space-y-3 text-sm">
            <li><a target="_blank" href="https://github.com/ppp-lgtm" class="group inline-flex items-center gap-2 hover:text-light-accent dark:hover:text-dark-accent transition-colors">
              GitHub<span class="w-4 h-px bg-light-bg/30 dark:bg-dark-bg/30 group-hover:w-8 group-hover:bg-light-accent dark:group-hover:bg-dark-accent transition-all"></span>
            </a></li>
            <li><a href="mailto:2652278966@qq.com" class="group inline-flex items-center gap-2 hover:text-light-accent dark:hover:text-dark-accent transition-colors">
              写邮件<span class="w-4 h-px bg-light-bg/30 dark:bg-dark-bg/30 group-hover:w-8 group-hover:bg-light-accent dark:group-hover:bg-dark-accent transition-all"></span>
            </a></li>
          </ul>
        </div>

        <div class="col-span-12 md:col-span-3">
          <p class="footnote !text-light-bg/50 dark:!text-dark-bg/50 mb-5">直接联络</p>
          <a href="mailto:zhangmy@example.com" class="block font-display italic text-xl md:text-2xl leading-tight text-light-bg dark:text-dark-bg hover:text-light-accent dark:hover:text-dark-accent transition-colors break-all">
            2652278966@qq.com
          </a>
          <div class="mt-8 flex items-center gap-3">
            <span class="relative flex h-2 w-2">
              <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-light-accent2 dark:bg-dark-accent2 opacity-75"></span>
              <span class="relative inline-flex rounded-full h-2 w-2 bg-light-accent2 dark:bg-dark-accent2"></span>
            </span>
            <span class="footnote !text-light-bg/70 dark:!text-dark-bg/70 !tracking-[0.2em]">
              目前开放合作中
            </span>
          </div>
        </div>
      </div>

      <!-- 底部元信息 -->
      <div class="pt-8 flex flex-col md:flex-row md:items-center md:justify-between gap-6">
        <div class="flex items-center gap-6 flex-wrap">
          <span class="footnote !text-light-bg/50 dark:!text-dark-bg/50">
            字体：DM Serif Display、Lora、DM Sans、JetBrains Mono
          </span>
          <span class="w-px h-3 bg-light-bg/20 dark:bg-dark-bg/20 hidden md:block"></span>
          <span class="footnote !text-light-bg/50 dark:!text-dark-bg/50">
            技术：Vue 3 · Tailwind CSS
          </span>
        </div>
      </div>
    </div>
  </footer>
</template>
