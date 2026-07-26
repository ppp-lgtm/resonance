<script setup lang="ts">
import { onMounted } from 'vue'
import { useAppStore } from '../../stores/app'
import HeroSection from './HeroSection.vue'
import AboutSection from './AboutSection.vue'
import SkillSection from './SkillSection.vue'
import ProjectSection from './ProjectSection.vue'
import TimelineSection from './TimelineSection.vue'

const appStore = useAppStore()
onMounted(() => { appStore.fetchAll() })
</script>

<template>
  <div>
    <HeroSection />
    <AboutSection />
    <SkillSection />
    <ProjectSection />
    <TimelineSection />
  </div>
</template>
