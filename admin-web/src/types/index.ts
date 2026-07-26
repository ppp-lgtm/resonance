// 数据类型定义

export interface Profile {
  name: string
  title: string[]
  slogan: string
  bio: string
  avatar: string
  resumeUrl: string
  location?: string
  yearsExperience?: number
  focusAreas?: string[]
  workingMode?: string
  contacts?: Array<ContactItem & { sortOrder?: number; visible?: boolean }>
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
  proficiency: number // 0-100
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
  /** 项目状态："已上线" | "筹备中" | "规划中" | ''（前端空串表示未设置） */
  status?: string
  /** 完成年份：4 位整数；空则 null/undefined */
  completionYear?: number | null
}

export interface Award {
  id: number
  title: string
  issuer: string
  date: string
  description: string
  certificateUrl: string
  coverUrl?: string
}

export interface Education {
  id: number
  school: string
  degree: string
  major: string
  startDate: string
  endDate: string
  description: string
}
