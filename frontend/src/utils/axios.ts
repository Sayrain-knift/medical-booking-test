import axios from 'axios'
import { useAuthStore } from '../stores/auth'
import router from '../router'

// 创建axios实例
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    // 如果是ResponseResult包装对象，直接返回data字段
    return response.data.data || response.data
  },
  (error) => {
    const authStore = useAuthStore()
    
    if (error.response && error.response.status === 401) {
      // Token过期或无效，清除认证信息并跳转到登录页
      authStore.logout()
      router.push('/login')
    }
    
    // 处理错误信息
    const errorMessage = error.response?.data?.message || error.message || '请求失败，请稍后重试'
    authStore.error = errorMessage
    
    return Promise.reject(error)
  }
)

export default api
