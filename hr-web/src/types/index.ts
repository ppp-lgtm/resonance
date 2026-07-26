export interface Profile {
  name: string
  title: string[]
  slogan: string
  bio: string
  avatar: string
  resumeUrl: string
  yearsExperience: number
  location?: string
  focusAreas?: string[]
  workingMode?: string
}

export interface ContactItem {
  id: number
  platform: string
  icon: string
  value: string
  link?: string
  copyable?: boolean
}

export interface Skill {
  id: number
  name: string
  category: string
  icon: string
  proficiency: number
}

export interface ProjectImage {
  url: string
  alt: string
}

export interface Project {
  id: number
  title: string
  summary: string
  description: string
  tags: string[]
  images: ProjectImage[]
  githubUrl?: string
  demoUrl?: string
  videoUrl?: string
  isFeatured: boolean
  sortOrder: number
  /** 项目状态（后端枚举中文字符串序列化）："已上线" | "筹备中" | "规划中" | null */
  status?: string | null
  /** 完成年份（4 位整数）；未设置则 null */
  completionYear?: number | null
}

export interface Award {
  id: number
  title: string
  issuer: string
  date: string
  description?: string
  certificateUrl?: string
  /** alias for certificateUrl (前端兼容别名，两边都认） */
  certUrl?: string
  /** 奖项封面图 URL（奖状/奖杯照片），前台缩略图展示 */
  coverUrl?: string
}

export interface Education {
  id: number
  school: string
  degree: string
  major: string
  startDate: string
  endDate: string
  description?: string
}
