import { defineStore } from 'pinia'
import type { Profile, ContactItem, Skill, Project, Award, Education } from '../types'
import { adminApi, type ProfileSaveBody } from '../api'

interface AppState {
  profile: Profile | null
  profileAdmin: ProfileSaveBody | null
  contacts: ContactItem[]
  skills: Skill[]
  projects: Project[]
  awards: Award[]
  education: Education[]
  loading: boolean
}

/**
 * 管理端数据仓库：所有数据通过 /api/admin/* 真实拉取（带 Bearer Token）
 */
export const useAppStore = defineStore('app', {
  state: (): AppState => ({
    profile: null,
    profileAdmin: null,
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
        // 并发：技能/项目/奖项/教育 + profile 全量拉
        const [profileResp, skills, projects, awards, education] = await Promise.all([
          adminApi.getProfile(),
          adminApi.listSkills(),
          adminApi.listProjects(),
          adminApi.listAwards(),
          adminApi.listEducation(),
        ])
        this.profileAdmin = profileResp
        this.contacts = (profileResp.contacts ?? []).map(c => ({
          id: Number(c.id ?? 0),
          platform: c.platform,
          icon: c.icon ?? '🔗',
          value: c.value,
          link: c.link,
          copyable: !!c.copyable,
        })) as ContactItem[]
        this.profile = {
          name: profileResp.name,
          title: profileResp.title,
          slogan: profileResp.slogan,
          bio: profileResp.bio,
          avatar: profileResp.avatar ?? '',
          resumeUrl: profileResp.resumeUrl ?? '',
          yearsExperience: 0,
        } as any
        this.skills = skills
        this.projects = projects
        this.awards = awards.map(a => ({ ...a, certUrl: (a as any).certUrl || (a as any).certificateUrl }))
        this.education = education
      } finally {
        this.loading = false
      }
    },
  },
})
