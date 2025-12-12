// 用户类型
export interface User {
  id: number
  username: string
  email: string
  role: string
  createdAt: string
  updatedAt: string
}

// 科室类型
export interface Department {
  id: number
  name: string
  description: string
  icon: string
  createdAt: string
  updatedAt: string
}

// 医生类型
export interface Doctor {
  id: number
  name: string
  departmentId: number
  department: Department
  title: string
  expertise: string
  bio: string
  avatar: string
  createdAt: string
  updatedAt: string
}

// 排班类型
export interface Schedule {
  id: number
  doctorId: number
  doctor: Doctor
  date: string
  startTime: string
  endTime: string
  maxPatients: number
  status: string | number
  createdAt: string
  updatedAt: string
}

// 预约类型
export interface Appointment {
  id: number
  patientId: number
  doctorId: number
  doctor: Doctor
  scheduleId: number
  schedule: Schedule
  date: string
  timeSlot: string
  status: string
  createdAt: string
  updatedAt: string
}

// 登录请求类型
export interface LoginRequest {
  username: string
  password: string
}

// 注册请求类型
export interface RegisterRequest {
  username: string
  email: string
  password: string
  phone: string
}

// 认证响应类型
export interface AuthResponse {
  token: string
  username: string
  role: string
  message: string
}

// AI聊天
export interface AiChatMessage {
  role: string
  content: string
  timestamp: string
}

export interface AiChatResponsePayload {
  chatId: string
  response: string
  timestamp: string
}

export interface AiChatHistoryResponse {
  chatId: string
  messages: AiChatMessage[]
}
