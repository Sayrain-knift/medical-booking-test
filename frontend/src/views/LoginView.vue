<template>
  <div class="login-container">
    <div class="login-card">
      <div class="row g-0">
        <!-- 左侧品牌信息 -->
        <div class="col-lg-6">
          <div class="login-left">
            <div class="brand-logo">
              <i class="bi bi-hospital"></i>
            </div>
            <h2 class="brand-title">医疗预约系统</h2>
            <p class="brand-description">
              专业医疗，贴心服务<br>
              在线预约挂号，告别排队烦恼<br>
              24小时服务，随时随地预约
            </p>
            <div class="brand-features">
              <div class="feature-item">
                <i class="bi bi-shield-check"></i>
                <div>
                  <strong>安全保障</strong><br>
                  <small>银行级加密技术保护您的隐私</small>
                </div>
              </div>
              <div class="feature-item">
                <i class="bi bi-clock-history"></i>
                <div>
                  <strong>24小时在线服务</strong><br>
                  <small>全天候医疗咨询，随时为您服务</small>
                </div>
              </div>
              <div class="feature-item">
                <i class="bi bi-people"></i>
                <div>
                  <strong>专业医疗团队</strong><br>
                  <small>三甲医院专家坐诊，权威可靠</small>
                </div>
              </div>
              <div class="feature-item">
                <i class="bi bi-robot"></i>
                <div>
                  <strong>AI智能问诊</strong><br>
                  <small>智能健康咨询，快速初步诊断</small>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 右侧登录表单 -->
        <div class="col-lg-6">
          <div class="login-right">
            <div class="text-center mb-4">
              <h3 class="fw-bold">欢迎回来</h3>
              <p class="text-muted">请登录您的账户</p>
            </div>
            
            <form @submit.prevent="handleLogin">
              <div class="form-floating">
                <input 
                  type="text" 
                  class="form-control" 
                  v-model="form.username"
                  placeholder="用户名" 
                  required
                >
                <label for="username">
                  <i class="bi bi-person me-2"></i>用户名
                </label>
              </div>
              
              <div class="form-floating position-relative">
                <input 
                  :type="showPassword ? 'text' : 'password'" 
                  class="form-control" 
                  v-model="form.password"
                  placeholder="密码" 
                  required
                >
                <label for="password">
                  <i class="bi bi-lock me-2"></i>密码
                </label>
                <span class="password-toggle" @click="togglePassword">
                  <i :class="showPassword ? 'bi bi-eye-slash' : 'bi bi-eye'" id="passwordIcon"></i>
                </span>
              </div>
              
              <div class="d-flex justify-content-between align-items-center mb-3">
                <div class="form-check">
                  <input class="form-check-input" type="checkbox" v-model="form.rememberMe">
                  <label class="form-check-label" for="rememberMe">
                    记住我
                  </label>
                </div>
                <a href="#" class="text-decoration-none">忘记密码？</a>
              </div>
              
              <button type="submit" class="btn btn-primary login-btn" :disabled="loading">
                <i class="bi bi-box-arrow-in-right me-2"></i>
                {{ loading ? '登录中...' : '登录' }}
              </button>
            </form>
            
            <div class="divider">
              <span>或</span>
            </div>
            
            <div class="text-center">
              <p class="mb-3">还没有账户？</p>
              <router-link to="/register" class="btn btn-outline-primary">
                <i class="bi bi-person-plus me-2"></i>立即注册
              </router-link>
            </div>
            
            <!-- 快速登录选项 -->
            <div class="mt-4">
              <p class="text-center text-muted mb-3">快速登录</p>
              <div class="d-flex justify-content-center gap-3">
                <button class="btn btn-outline-secondary btn-sm">
                  <i class="bi bi-telephone me-1"></i>手机号登录
                </button>
                <button class="btn btn-outline-secondary btn-sm">
                  <i class="bi bi-envelope me-1"></i>邮箱登录
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { authApi } from '../services/api'
import type { LoginRequest } from '../types'

const router = useRouter()
const authStore = useAuthStore()

// 表单数据
const form = ref<LoginRequest & { rememberMe: boolean }>({
  username: '',
  password: '',
  rememberMe: false
})

// 状态
const loading = ref(false)
const showPassword = ref(false)

// 切换密码显示/隐藏
const togglePassword = () => {
  showPassword.value = !showPassword.value
}

// 处理登录
const handleLogin = async () => {
  loading.value = true
  try {
    const response = await authApi.login(form.value)
    authStore.setToken(response.token)
    // 根据响应创建用户对象
    const user = {
      id: 0, // 暂时使用0，实际id会在后续请求中获取
      username: response.username,
      email: '', // 登录响应中没有email，后续可以通过profile请求获取
      role: response.role,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    }
    authStore.setUser(user)
    authStore.clearError()
    router.push('/')
  } catch (error: any) {
    console.error('登录失败:', error)
    // 显示错误信息
    const errorMessage = error.response?.data?.message || error.message || '登录失败，请稍后重试'
    alert(errorMessage)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
:root {
  --primary-color: #007bff;
  --secondary-color: #6c757d;
}

.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, var(--primary-color) 0%, #0056b3 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem 0;
}

.login-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.1);
  overflow: hidden;
  max-width: 900px;
  width: 100%;
  margin: 0 1rem;
  border: 1px solid rgba(0,0,0,0.05);
}

.login-left {
  background: linear-gradient(135deg, var(--primary-color), #0056b3);
  color: white;
  padding: 4rem 3rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  min-height: 500px;
  position: relative;
  overflow: hidden;
}

.login-left::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  animation: float 20s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  33% { transform: translate(-20px, -20px) rotate(120deg); }
  66% { transform: translate(20px, -10px) rotate(240deg); }
}

.login-right {
  padding: 3rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 500px;
}

.brand-logo {
  font-size: 4rem;
  margin-bottom: 2rem;
  opacity: 0.95;
  position: relative;
  z-index: 2;
  text-shadow: 0 4px 8px rgba(0,0,0,0.3);
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

.brand-title {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 1.5rem;
  position: relative;
  z-index: 2;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
}

.brand-description {
  opacity: 0.95;
  line-height: 1.8;
  font-size: 1rem;
  margin-bottom: 2rem;
  position: relative;
  z-index: 2;
  text-shadow: 0 1px 2px rgba(0,0,0,0.2);
}

.brand-features {
  position: relative;
  z-index: 2;
}

.brand-features .feature-item {
  margin-bottom: 1rem;
  padding: 1rem 1.5rem;
  background: rgba(255,255,255,0.15);
  border-radius: 15px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.2);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  text-align: left;
}

.brand-features .feature-item:hover {
  background: rgba(255,255,255,0.25);
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.2);
}

.brand-features .feature-item i {
  font-size: 1.2rem;
  margin-right: 0.75rem;
  flex-shrink: 0;
}

.brand-features .feature-item div {
  flex: 1;
}

.brand-features .feature-item strong {
  display: block;
  margin-bottom: 0.25rem;
  font-size: 1rem;
  text-shadow: 0 1px 2px rgba(0,0,0,0.2);
}

.brand-features .feature-item small {
  opacity: 0.9;
  font-size: 0.85rem;
  text-shadow: 0 1px 2px rgba(0,0,0,0.1);
}

.form-floating {
  margin-bottom: 1.5rem;
}

.form-floating > .form-control {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  height: calc(3.5rem + 2px);
}

.form-floating > .form-control:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
}

.form-floating > label {
  color: #6c757d;
}

.password-toggle {
  position: absolute;
  right: 1rem;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer;
  color: var(--secondary-color);
  z-index: 10;
  transition: color 0.2s;
}

.password-toggle:hover {
  color: var(--primary-color);
}

.login-btn {
  width: 100%;
  padding: 0.75rem;
  border-radius: 10px;
  font-weight: 600;
  margin-top: 1rem;
  transition: all 0.2s;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 123, 255, 0.3);
}

.divider {
  text-align: center;
  margin: 2rem 0;
  position: relative;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: #e9ecef;
}

.divider span {
  background: white;
  padding: 0 1rem;
  position: relative;
  color: var(--secondary-color);
}

@media (max-width: 768px) {
  .login-left {
    display: none;
  }
  
  .login-right {
    padding: 2rem;
    min-height: auto;
  }
  
  .login-card {
    margin: 0 0.5rem;
  }
}

@media (max-width: 576px) {
  .login-right {
    padding: 1.5rem;
  }
}
</style>
