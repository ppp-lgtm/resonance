<script setup lang="ts">
import { onMounted } from 'vue'
import NavBar from './NavBar.vue'
import FooterSection from './FooterSection.vue'
import { useThemeStore } from '../../stores/theme'

const theme = useThemeStore()
onMounted(() => { theme.init() })
</script>

<template>
  <div class="min-h-screen flex flex-col bg-paper">
    <NavBar />
    <main class="flex-1">
      <RouterView />
    </main>
    <FooterSection />
  </div>
</template>
