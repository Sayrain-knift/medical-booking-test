import api from '../utils/axios'
import type {
  LoginRequest,
  RegisterRequest,
  AuthResponse,
  User,
  Department,
  Doctor,
  Schedule,
  Appointment,
  AiChatHistoryResponse,
  AiChatResponsePayload
} from '../types'

// 认证相关API
export const authApi = {
  login: (data: LoginRequest): Promise<AuthResponse> => api.post('/auth/login', data),
  register: (data: RegisterRequest): Promise<AuthResponse> => api.post('/auth/register', data),
  logout: (): Promise<string> => api.post('/auth/logout'),
  validateToken: (): Promise<boolean> => api.get('/auth/validate-token')
}

// 用户相关API
export const userApi = {
  getProfile: (): Promise<User> => api.get('/users/profile'),
  updateProfile: (data: Partial<User>): Promise<User> => api.put('/users/profile', data),
  changePassword: (data: { oldPassword: string; newPassword: string }): Promise<string> => 
    api.put('/users/password', data)
}

// 科室相关API
export const departmentApi = {
  getAll: (): Promise<Department[]> => api.get('/departments'),
  getById: (id: number): Promise<Department> => api.get(`/departments/${id}`),
  getDoctors: (id: number): Promise<Doctor[]> => api.get(`/doctors/department/${id}`)
}

// 医生相关API
export const doctorApi = {
  getAll: (params?: Record<string, any>): Promise<Doctor[]> => api.get('/doctors', { params }),
  getById: (id: number): Promise<Doctor> => api.get(`/doctors/${id}`),
  getByDepartment: (departmentId: number): Promise<Doctor[]> => api.get(`/doctors/department/${departmentId}`),
  getSchedules: (id: number): Promise<Schedule[]> => api.get(`/schedules/doctor/${id}`),
  getAvailableSlots: (id: number): Promise<any[]> => 
    api.get(`/schedules/doctor/${id}`)
}

// 预约相关API
export const appointmentApi = {
  create: (data: any): Promise<Appointment> => api.post('/appointments', data),
  getAll: (params?: Record<string, any>): Promise<Appointment[]> => api.get('/appointments', { params }),
  getById: (id: number): Promise<Appointment> => api.get(`/appointments/${id}`),
  getMyAppointments: (params?: Record<string, any>): Promise<Appointment[]> => 
    api.get('/appointments/my-appointments', { params }),
  cancel: (id: number): Promise<Appointment> => api.put(`/appointments/${id}/cancel`),
  update: (id: number, data: Partial<Appointment>): Promise<Appointment> => 
    api.put(`/appointments/${id}`, data)
}

// AI问诊相关API
export const aiApi = {
  sendMessage: (message: string, chatId?: string): Promise<AiChatResponsePayload> => 
    api.post('/ai/chat', { message, chatId }),
  getHistory: (chatId?: string): Promise<AiChatHistoryResponse> => 
    api.get('/ai/chat/history', { params: chatId ? { chatId } : undefined }),
  clearHistory: (chatId?: string): Promise<void> => 
    api.delete('/ai/chat/history', { params: chatId ? { chatId } : undefined })
}
