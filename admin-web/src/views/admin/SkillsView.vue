<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAppStore } from '../../stores/app'
import { adminApi, type SkillSaveBody } from '../../api'
import type { Skill } from '../../types'

const appStore = useAppStore()
onMounted(() => { appStore.fetchAll() })

const activeCategory = ref('全部')
const showForm = ref(false)
const editingSkillId = ref<number | null>(null)
const submitting = ref(false)
const error = ref('')
const form = ref<SkillSaveBody>({
  name: '', category: '前端', icon: '🟢', proficiency: 80, sortOrder: 0, visible: true,
})

const categories = computed(() => ['全部', ...Array.from(new Set(appStore.skills.map(s => s.category)))])
const filteredSkills = computed(() => {
  const list = activeCategory.value === '全部'
    ? appStore.skills
    : appStore.skills.filter(s => s.category === activeCategory.value)
  return [...list].sort((a, b) => (a.proficiency - b.proficiency) * -1)
})

function openAdd() {
  editingSkillId.value = null
  form.value = { name: '', category: activeCategory.value === '全部' ? '前端' : activeCategory.value, icon: '🟢', proficiency: 80, sortOrder: 0, visible: true }
  error.value = ''
  showForm.value = true
}

function openEdit(skill: Skill) {
  editingSkillId.value = skill.id
  form.value = {
    name: skill.name,
    category: skill.category,
    icon: skill.icon,
    proficiency: skill.proficiency,
    sortOrder: (skill as any).sortOrder ?? 0,
    visible: (skill as any).visible ?? true,
  }
  error.value = ''
  showForm.value = true
}

async function save() {
  if (!form.value.name.trim()) { error.value = '技能名称不能为空'; return }
  if (!form.value.category.trim()) { error.value = '分类不能为空'; return }
  if (form.value.proficiency < 0 || form.value.proficiency > 100) { error.value = '熟练度必须在 0~100 之间'; return }
  submitting.value = true
  error.value = ''
  try {
    let saved: Skill
    if (editingSkillId.value) {
      saved = await adminApi.updateSkill(editingSkillId.value, { ...form.value, name: form.value.name.trim(), category: form.value.category.trim() })
      const idx = appStore.skills.findIndex(s => s.id === saved.id)
      if (idx !== -1) appStore.skills.splice(idx, 1, saved)
    } else {
      saved = await adminApi.createSkill({ ...form.value, name: form.value.name.trim(), category: form.value.category.trim() })
      appStore.skills.unshift(saved)
    }
    showForm.value = false
  } catch (e: any) {
    error.value = e?.message || '保存失败'
  } finally { submitting.value = false }
}

async function remove(id: number) {
  if (!confirm('确定要删除这个技能吗？')) return
  try {
    await adminApi.deleteSkill(id)
    const idx = appStore.skills.findIndex(s => s.id === id)
    if (idx !== -1) appStore.skills.splice(idx, 1)
  } catch (e: any) {
    alert(e?.message || '删除失败')
  }
}
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-heading font-bold">技能管理</h1>
      <button @click="openAdd" class="btn-primary text-sm !px-4 !py-2">+ 新增技能</button>
    </div>

    <!-- 分类筛选 -->
    <div class="flex flex-wrap gap-2 mb-6">
      <button
        v-for="cat in categories"
        :key="cat"
        class="px-3 py-1.5 rounded-full text-sm transition-all"
        :class="activeCategory === cat
          ? 'bg-violet-600 text-white'
          : 'bg-light-card dark:bg-dark-card text-gray-600 dark:text-gray-400'"
        @click="activeCategory = cat"
      >
        {{ cat }}
      </button>
    </div>

    <!-- 表格（真实 adminApi.listSkills 数据） -->
    <div class="card p-0 overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-gray-50 dark:bg-dark-bg/50 border-b border-gray-200 dark:border-gray-700">
          <tr>
            <th class="text-left px-6 py-3 font-medium text-gray-500 dark:text-gray-400">图标</th>
            <th class="text-left px-6 py-3 font-medium text-gray-500 dark:text-gray-400">名称</th>
            <th class="text-left px-6 py-3 font-medium text-gray-500 dark:text-gray-400">分类</th>
            <th class="text-left px-6 py-3 font-medium text-gray-500 dark:text-gray-400">熟练度</th>
            <th class="text-left px-6 py-3 font-medium text-gray-500 dark:text-gray-400">排序</th>
            <th class="text-left px-6 py-3 font-medium text-gray-500 dark:text-gray-400">可见</th>
            <th class="text-right px-6 py-3 font-medium text-gray-500 dark:text-gray-400">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100 dark:divide-gray-800">
          <tr v-for="skill in filteredSkills" :key="skill.id" class="hover:bg-violet-50/50 dark:hover:bg-violet-900/10 transition-colors">
            <td class="px-6 py-4 text-2xl">{{ skill.icon }}</td>
            <td class="px-6 py-4 font-medium">{{ skill.name }}</td>
            <td class="px-6 py-4">
              <span class="px-2 py-0.5 bg-violet-100 dark:bg-violet-900/30 text-violet-700 dark:text-violet-300 rounded-full text-xs">{{ skill.category }}</span>
            </td>
            <td class="px-6 py-4">
              <div class="flex items-center gap-2 max-w-[240px]">
                <div class="flex-1 h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
                  <div class="h-full bg-violet-600 rounded-full" :style="{ width: skill.proficiency + '%' }"></div>
                </div>
                <span class="text-xs text-gray-500 tabular-nums w-10 text-right">{{ skill.proficiency }}%</span>
              </div>
            </td>
            <td class="px-6 py-4 text-xs text-gray-500 tabular-nums">#{{ (skill as any).sortOrder ?? 0 }}</td>
            <td class="px-6 py-4">
              <span v-if="(skill as any).visible !== false" class="px-2 py-0.5 rounded-full text-xs bg-emerald-100 dark:bg-emerald-900/20 text-emerald-700 dark:text-emerald-300">✔ 显示</span>
              <span v-else class="px-2 py-0.5 rounded-full text-xs bg-gray-100 dark:bg-gray-800 text-gray-500">隐藏</span>
            </td>
            <td class="px-6 py-4 text-right">
              <button @click="openEdit(skill)" class="text-violet-600 dark:text-violet-400 hover:underline mr-3 text-sm">编辑</button>
              <button @click="remove(skill.id)" class="text-red-500 hover:underline text-sm">删除</button>
            </td>
          </tr>
          <tr v-if="filteredSkills.length === 0">
            <td colspan="7" class="px-6 py-12 text-center text-gray-400 text-sm">暂无技能，点击右上角「+ 新增技能」添加</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 新增/编辑 弹窗 -->
    <div v-if="showForm" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div class="absolute inset-0 bg-black/50 backdrop-blur-sm" @click="!submitting && (showForm = false)"></div>
      <div class="relative bg-white dark:bg-dark-card rounded-2xl shadow-2xl w-full max-w-md p-6 z-10">
        <h3 class="text-lg font-bold mb-4">{{ editingSkillId ? '编辑技能' : '新增技能' }}</h3>

        <div v-if="error" class="mb-4 px-3 py-2 rounded-lg bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-300 text-sm">{{ error }}</div>

        <div class="space-y-4">
          <div class="grid grid-cols-5 gap-3 items-end">
            <div class="col-span-1">
              <label class="block text-sm font-medium mb-1">图标</label>
              <input v-model="form.icon" type="text" maxlength="4" placeholder="🟢"
                class="w-full px-3 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500 text-center text-xl" />
            </div>
            <div class="col-span-4">
              <label class="block text-sm font-medium mb-1">名称 *</label>
              <input v-model="form.name" type="text" placeholder="Vue 3 / Spring Boot"
                class="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500" />
            </div>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block text-sm font-medium mb-1">分类 *</label>
              <select v-model="form.category" class="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500">
                <option>前端</option><option>后端</option><option>数据库</option>
                <option>DevOps</option><option>设计</option><option>其他</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium mb-1">排序序号</label>
              <input v-model.number="form.sortOrder" type="number"
                class="w-full px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-dark-bg outline-none focus:ring-2 focus:ring-violet-500" />
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">熟练度: {{ form.proficiency }}%</label>
            <input v-model.number="form.proficiency" type="range" min="0" max="100" class="w-full accent-violet-600" />
          </div>
          <div class="flex items-center gap-3">
            <label class="block text-sm font-medium">前台可见</label>
            <input v-model="form.visible" type="checkbox" class="w-4 h-4 accent-violet-600" />
            <span class="text-xs text-gray-500">（取消则 hr-web 不展示此技能）</span>
          </div>
        </div>

        <div class="flex gap-3 mt-6">
          <button @click="save" :disabled="submitting" class="btn-primary flex-1 text-sm disabled:opacity-60">
            {{ submitting ? '保存中…' : '保存' }}
          </button>
          <button @click="showForm = false" :disabled="submitting" class="btn-outline flex-1 text-sm disabled:opacity-60">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>
