import { defineStore } from 'pinia'
import type { Profile, ContactItem, Skill, Project, Award, Education } from '../types'
import { publicApi } from '../api'

interface AppState {
  profile: Profile | null
  contacts: ContactItem[]
  skills: Skill[]
  projects: Project[]
  awards: Award[]
  education: Education[]
  loading: boolean
}

/**
 * 前台 HR 端数据仓库
 * 优化：调用 1 个 /api/public/overview 接口拿全首屏数据，减少 5 次 RTT，提升 LCP
 */
export const useAppStore = defineStore('app', {
  state: (): AppState => ({
    profile: null,
    contacts: [],
    skills: [],
    projects: [],
    awards: [],
    education: [],
    loading: false,
  }),
  actions: {
    async fetchAll() {
      this.loading = true
      try {
        // 首屏用 overview，后端已聚合 6 个查询
        const ov = await publicApi.overview()
        this.profile = ov.profile
        this.contacts = (ov.profile as any).contacts
          ? (ov.profile as unknown as { contacts: ContactItem[] }).contacts
          : []
        this.skills = ov.skills
        this.projects = ov.projects
        this.awards = ov.awards
        this.education = ov.education
      } catch (e) {
        // 回退：逐个接口请求（开发环境后端没启动时兜底）
        try {
          const [profile, skills, projects, awards, education] = await Promise.all([
            publicApi.profile(),
            publicApi.skills(),
            publicApi.projects(),
            publicApi.awards(),
            publicApi.education(),
          ])
          this.profile = profile
          this.skills = skills
          this.projects = projects
          this.awards = awards
          this.education = education
        } catch (_err) {
          console.warn('[hr-web] 获取真实接口失败，检查后端是否启动：', _err)
        }
      } finally {
        this.loading = false
      }
    },
  },
})
