import type {
  Profile, ContactItem, Skill, Project, Award, Education, ProjectImage
} from '../types'

// ============================================================
// 类型：后端统一响应包装
// ============================================================
export interface ApiResult<T> {
  code: number
  message: string
  data: T
  timestamp: number
}

// ============================================================
// 后端基址 + 轻量 fetch 封装（无需 axios 依赖，避免新增依赖）
// 默认后端跑在 8080；通过 Vite 代理访问
// ============================================================
const API_BASE = '/api'

function getAuthToken(): string | null {
  return localStorage.getItem('admin_token')
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const url = `${API_BASE}${path}`
  const headers: Record<string, string> = {
    Accept: 'application/json',
    ...(init.headers as Record<string, string> | undefined),
  }
  if (!headers['Content-Type']
      && !(init.body instanceof FormData)
      && init.body != null) {
    headers['Content-Type'] = 'application/json'
  }
  const token = getAuthToken()
  if (token) headers['Authorization'] = `Bearer ${token}`

  const res = await fetch(url, { ...init, headers })
  // 关键：先把 body 当 text 读出来（流只读一次，避免 json→text 连续读造成 body stream already read）
  // 后端返回的 HTML 错误页（404/405/502 时 Nginx/Spring 常吐 HTML）也能正确展示给用户看
  const raw = await res.text()
  let json: ApiResult<T>
  try {
    json = JSON.parse(raw) as ApiResult<T>
  } catch {
    // 不是 JSON：大概率是 Nginx 的 404/405/502 HTML，截取前 300 字符方便定位
    const snippet = raw.replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim().slice(0, 300)
    throw new Error(`HTTP ${res.status} ${res.statusText}: ${snippet || '(empty body)'}`)
  }
  if (json.code !== 200 && json.code !== 0) {
    // 后端错误码：统一 code + message
    const err = new Error(`${json.message || '请求失败'}（code=${json.code}）`) as Error & { code?: number }
    err.code = json.code
    throw err
  }
  return json.data
}

// ============================================================
// 业务类型
// ============================================================
export interface ProjectSaveBody {
  title: string
  summary: string
  description: string
  tags: string[]
  images: ProjectImage[]
  githubUrl?: string
  demoUrl?: string
  videoUrl?: string
  isFeatured?: boolean
  sortOrder?: number
  /** 项目状态：已上线 / 筹备中 / 规划中；空串表示不设置 */
  status?: string
  /** 完成年份：4 位整数；null/undefined 表示不设置 */
  completionYear?: number | null
}

export interface ProfileSaveBody {
  name: string
  title: string[]
  slogan: string
  bio: string
  avatar?: string
  resumeUrl?: string
  location?: string
  yearsExperience?: number
  focusAreas?: string[]
  workingMode?: string
  contacts: Array<{
    id?: number
    platform: string
    icon?: string
    value: string
    link?: string
    copyable?: boolean
    visible?: boolean
    sortOrder?: number
  }>
}

export interface SkillSaveBody {
  name: string
  category: string
  icon?: string
  proficiency: number
  sortOrder?: number
  visible?: boolean
}

export interface AwardSaveBody {
  title: string
  issuer: string
  date: string
  description?: string
  certificateUrl?: string
  coverUrl?: string
  sortOrder?: number
}

export interface EducationSaveBody {
  school: string
  degree: string
  major: string
  startDate: string
  endDate?: string
  description?: string
  sortOrder?: number
}

export interface LoginBody {
  username: string
  password: string
}

export interface RegisterBody {
  username: string
  password: string
}

export interface LoginResp {
  token: string
  tokenType: string
  expiresIn: number
  user: { id: number; username: string; avatarUrl?: string; createdAt?: number }
}

export interface DashboardStats {
  projectCount: number
  skillCount: number
  awardCount: number
  educationCount: number
  contactCount: number
}

export interface UploadResp {
  url: string
  bucket: string
  objectKey: string
  sizeBytes: number
  mimeType: string
}

// ============================================================
// 认证
// ============================================================
export interface SystemStatus {
  hasAdmin: boolean
  adminCount: number
}

export const authApi = {
  getStatus: () => request<SystemStatus>('/auth/status'),

  login: (body: LoginBody) => request<LoginResp>('/auth/login', {
    method: 'POST',
    body: JSON.stringify(body),
  }),

  register: (body: RegisterBody) => request<LoginResp>('/auth/register', {
    method: 'POST',
    body: JSON.stringify(body),
  }),
}

// ============================================================
// 前台（公开，供 hr-web 使用）
// ============================================================
export const publicApi = {
  overview: () => request<{
    profile: Profile
    skills: Skill[]
    projects: Project[]
    awards: Award[]
    education: Education[]
  }>('/public/overview'),

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

// ============================================================
// 后台管理（供 admin-web 使用，均需 Bearer Token）
// ============================================================
export const adminApi = {
  dashboard: () => request<DashboardStats>('/admin/dashboard/stats'),

  getProfile: () => request<ProfileSaveBody>('/admin/profile'),
  saveProfile: (body: ProfileSaveBody) => request<ProfileSaveBody>('/admin/profile', {
    method: 'PUT',
    body: JSON.stringify(body),
  }),

  listSkills: (category?: string) => request<Skill[]>(
    category ? `/admin/skills?category=${encodeURIComponent(category)}` : '/admin/skills'
  ),
  createSkill: (body: SkillSaveBody) => request<Skill>('/admin/skills', {
    method: 'POST', body: JSON.stringify(body),
  }),
  updateSkill: (id: number, body: SkillSaveBody) => request<Skill>(`/admin/skills/${id}`, {
    method: 'PUT', body: JSON.stringify(body),
  }),
  deleteSkill: (id: number) => request<void>(`/admin/skills/${id}`, { method: 'DELETE' }),

  listProjects: (tag?: string, keyword?: string) => {
    const q = new URLSearchParams()
    if (tag) q.set('tag', tag)
    if (keyword) q.set('keyword', keyword)
    const qs = q.toString()
    return request<Project[]>(`/admin/projects${qs ? `?${qs}` : ''}`)
  },
  getProject: (id: number) => request<Project>(`/admin/projects/${id}`),
  createProject: (body: ProjectSaveBody) => request<Project>('/admin/projects', {
    method: 'POST', body: JSON.stringify(body),
  }),
  updateProject: (id: number, body: ProjectSaveBody) => request<Project>(`/admin/projects/${id}`, {
    method: 'PUT', body: JSON.stringify(body),
  }),
  deleteProject: (id: number) => request<void>(`/admin/projects/${id}`, { method: 'DELETE' }),

  listAwards: () => request<Award[]>('/admin/awards'),
  createAward: (body: AwardSaveBody) => request<Award>('/admin/awards', {
    method: 'POST', body: JSON.stringify(body),
  }),
  updateAward: (id: number, body: AwardSaveBody) => request<Award>(`/admin/awards/${id}`, {
    method: 'PUT', body: JSON.stringify(body),
  }),
  deleteAward: (id: number) => request<void>(`/admin/awards/${id}`, { method: 'DELETE' }),

  listEducation: () => request<Education[]>('/admin/education'),
  createEducation: (body: EducationSaveBody) => request<Education>('/admin/education', {
    method: 'POST', body: JSON.stringify(body),
  }),
  updateEducation: (id: number, body: EducationSaveBody) => request<Education>(`/admin/education/${id}`, {
    method: 'PUT', body: JSON.stringify(body),
  }),
  deleteEducation: (id: number) => request<void>(`/admin/education/${id}`, { method: 'DELETE' }),

  uploadImage: (file: File) => {
    const fd = new FormData()
    fd.append('file', file)
    return request<UploadResp>('/admin/upload/image', { method: 'POST', body: fd })
  },
  uploadFile: (file: File) => {
    const fd = new FormData()
    fd.append('file', file)
    return request<UploadResp>('/admin/upload/file', { method: 'POST', body: fd })
  },
}

// 兼容历史（旧组件使用）
export const api = {
  getProfile: publicApi.profile,
  getContacts: async (): Promise<ContactItem[]> => (await publicApi.profile()) as unknown as ContactItem[],
  getSkills: publicApi.skills,
  getProjects: publicApi.projects,
  getAwards: publicApi.awards,
  getEducation: publicApi.education,
}
