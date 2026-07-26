import type { Profile, ContactItem, Skill, Project, Award, Education, ProjectImage } from '../types'

export interface ApiResult<T> {
  code: number
  message: string
  data: T
  timestamp: number
}

const API_BASE = '/api'

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const url = `${API_BASE}${path}`
  const headers: Record<string, string> = {
    Accept: 'application/json',
    ...(init.headers as Record<string, string> | undefined),
  }
  if (!headers['Content-Type'] && init.body && !(init.body instanceof FormData)) {
    headers['Content-Type'] = 'application/json'
  }
  const res = await fetch(url, { ...init, headers })
  let json: ApiResult<T>
  try {
    json = await res.json()
  } catch {
    const text = await res.text()
    throw new Error(`HTTP ${res.status} ${res.statusText}: ${text.slice(0, 300)}`)
  }
  if (json.code !== 200 && json.code !== 0) {
    const err = new Error(`${json.message || '请求失败'}（code=${json.code}）`) as Error & { code?: number }
    err.code = json.code
    throw err
  }
  return json.data
}

export interface OverviewResp {
  profile: Profile
  skills: Skill[]
  projects: Project[]
  awards: Award[]
  education: Education[]
}

/** 前台只用 public 接口（无需鉴权） */
export const publicApi = {
  overview: () => request<OverviewResp>('/public/overview'),
  profile: () => request<Profile>('/public/profile'),
  skills: (category?: string) => request<Skill[]>(
    category ? `/public/skills?category=${encodeURIComponent(category)}` : '/public/skills'
  ),
  projects: (tag?: string) => request<Project[]>(
    tag ? `/public/projects?tag=${encodeURIComponent(tag)}` : '/public/projects'
  ),
  projectDetail: (id: number) => request<Project>(`/public/projects/${id}`),
  awards: () => request<Award[]>('/public/awards'),
  education: () => request<Education[]>('/public/education'),
}

export interface ContactItemVO {
  id: number
  platform: string
  icon: string
  value: string
  link?: string
  copyable: boolean
}

export type { Profile, ContactItem, Skill, Project, Award, Education, ProjectImage }

export const api = {
  getProfile: publicApi.profile,
  getContacts: async (): Promise<ContactItem[]> => {
    const p = await publicApi.profile()
    const cs = (p as unknown as { contacts?: Array<{ id?: number; platform: string; icon?: string; value: string; link?: string; copyable?: boolean }> }).contacts ?? []
    return cs.map((c, i) => ({
      id: Number(c.id ?? i),
      platform: c.platform,
      icon: c.icon ?? '🔗',
      value: c.value,
      link: c.link,
      copyable: !!c.copyable,
    }))
  },
  getSkills: publicApi.skills,
  getProjects: publicApi.projects,
  getAwards: publicApi.awards,
  getEducation: publicApi.education,
}
