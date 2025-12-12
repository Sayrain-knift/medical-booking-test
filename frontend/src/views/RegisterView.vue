<template>
  <div class="register-container">
    <div class="register-card">
      <div class="row g-0">
        <!-- 左侧特色功能 -->
        <div class="col-lg-5">
          <div class="register-left">
            <div class="text-center mb-4">
              <div class="brand-logo mb-3">
                <i class="bi bi-hospital" style="font-size: 3rem;"></i>
              </div>
              <h3>加入我们，享受专业医疗服务</h3>
            </div>
            
            <div class="feature-item">
              <div class="feature-icon">
                <i class="bi bi-shield-check"></i>
              </div>
              <div>
                <h5>安全保障</h5>
                <p>您的健康信息严格保密，采用银行级加密技术</p>
              </div>
            </div>
            
            <div class="feature-item">
              <div class="feature-icon">
                <i class="bi bi-calendar-check"></i>
              </div>
              <div>
                <h5>便捷预约</h5>
                <p>24小时在线预约服务，随时随地轻松挂号</p>
              </div>
            </div>
            
            <div class="feature-item">
              <div class="feature-icon">
                <i class="bi bi-people"></i>
              </div>
              <div>
                <h5>专业团队</h5>
                <p>资深医生为您服务，三甲医院专家坐诊</p>
              </div>
            </div>
            
            <div class="feature-item">
              <div class="feature-icon">
                <i class="bi bi-robot"></i>
              </div>
              <div>
                <h5>AI问诊</h5>
                <p>智能健康咨询助手，快速初步诊断</p>
              </div>
            </div>
            
            <div class="feature-item">
              <div class="feature-icon">
                <i class="bi bi-heart-pulse"></i>
              </div>
              <div>
                <h5>健康管理</h5>
                <p>个性化健康档案，长期跟踪健康状况</p>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 右侧注册表单 -->
        <div class="col-lg-7">
          <div class="register-right">
            <div class="text-center mb-4">
              <h3 class="fw-bold">创建新账户</h3>
              <p class="text-muted">填写以下信息完成注册</p>
            </div>
            
            <form @submit.prevent="handleRegister">
              <div class="row">
                <div class="col-md-6">
                  <div class="form-floating">
                    <input 
                      type="text" 
                      class="form-control" 
                      v-model="form.username"
                      placeholder="用户名" 
                      required
                    >
                    <label for="username">
                      <i class="bi bi-person me-2"></i>用户名 *
                    </label>
                  </div>
                </div>
                <div class="col-md-6">
                  <div class="form-floating">
                    <input 
                      type="text" 
                      class="form-control" 
                      v-model="form.realName"
                      placeholder="真实姓名" 
                      required
                    >
                    <label for="realName">
                      <i class="bi bi-person-badge me-2"></i>真实姓名 *
                    </label>
                  </div>
                </div>
              </div>
              
              <div class="form-floating">
                <input 
                  type="email" 
                  class="form-control" 
                  v-model="form.email"
                  placeholder="邮箱" 
                  required
                >
                <label for="email">
                  <i class="bi bi-envelope me-2"></i>邮箱地址 *
                </label>
              </div>
              
              <div class="form-floating">
                <input 
                  type="tel" 
                  class="form-control" 
                  v-model="form.phone"
                  placeholder="手机号" 
                  pattern="[0-9]{11}" 
                  required
                >
                <label for="phone">
                  <i class="bi bi-telephone me-2"></i>手机号码 *
                </label>
              </div>
              
              <div class="form-floating">
                <input 
                  type="date" 
                  class="form-control" 
                  v-model="form.birthDate"
                  placeholder="出生日期" 
                  required
                >
                <label for="birthDate">
                  <i class="bi bi-calendar me-2"></i>出生日期 *
                </label>
              </div>
              
              <div class="form-floating">
                <select 
                  class="form-control" 
                  v-model="form.gender" 
                  required
                >
                  <option value="">请选择性别</option>
                  <option value="MALE">男</option>
                  <option value="FEMALE">女</option>
                </select>
                <label for="gender">
                  <i class="bi bi-gender-ambiguous me-2"></i>性别 *
                </label>
              </div>
              
              <div class="form-floating position-relative">
                <input 
                  :type="showPassword ? 'text' : 'password'" 
                  class="form-control" 
                  v-model="form.password"
                  placeholder="密码" 
                  required 
                  minlength="6"
                  @input="checkPasswordStrength"
                >
                <label for="password">
                  <i class="bi bi-lock me-2"></i>密码 *
                </label>
                <span class="password-toggle" @click="togglePassword('password')">
                  <i :class="showPassword ? 'bi bi-eye-slash' : 'bi bi-eye'" id="passwordIcon"></i>
                </span>
                <div :class="['password-strength', passwordStrengthClass]"></div>
              </div>
              
              <div class="form-floating position-relative">
                <input 
                  :type="showConfirmPassword ? 'text' : 'password'" 
                  class="form-control" 
                  v-model="form.confirmPassword"
                  placeholder="确认密码" 
                  required
                >
                <label for="confirmPassword">
                  <i class="bi bi-lock-fill me-2"></i>确认密码 *
                </label>
                <span class="password-toggle" @click="togglePassword('confirmPassword')">
                  <i :class="showConfirmPassword ? 'bi bi-eye-slash' : 'bi bi-eye'" id="confirmPasswordIcon"></i>
                </span>
                <div v-if="form.confirmPassword && form.password !== form.confirmPassword" 
                     class="text-danger text-sm mt-1">
                  两次输入的密码不一致
                </div>
              </div>
              
              <div class="form-check mb-3">
                <input 
                  class="form-check-input" 
                  type="checkbox" 
                  v-model="form.agreeTerms" 
                  required
                >
                <label class="form-check-label" for="agreeTerms">
                  我已阅读并同意 <a href="#" class="text-decoration-none">服务条款</a> 和 
                  <a href="#" class="text-decoration-none">隐私政策</a>
                </label>
              </div>
              
              <button type="submit" class="btn btn-primary register-btn" :disabled="loading || (!!form.confirmPassword && form.password !== form.confirmPassword)">
                <i class="bi bi-person-plus me-2"></i>{{ loading ? '注册中...' : '立即注册' }}
              </button>
            </form>
            
            <div class="text-center mt-4">
              <p class="mb-0">已有账户？</p>
              <router-link to="/login" class="btn btn-outline-primary">
                <i class="bi bi-box-arrow-in-right me-2"></i>立即登录
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { authApi } from '../services/api'
import type { RegisterRequest } from '../types'

const router = useRouter()

// 表单数据
const form = ref<RegisterRequest & {
  realName: string
  phone: string
  birthDate: string
  gender: string
  confirmPassword: string
  agreeTerms: boolean
}>({
  username: '',
  email: '',
  password: '',
  realName: '',
  phone: '',
  birthDate: '',
  gender: '',
  confirmPassword: '',
  agreeTerms: false
})

// 状态
const loading = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const passwordStrength = ref(0)

// 计算密码强度类
const passwordStrengthClass = computed(() => {
  if (passwordStrength.value <= 2) return 'strength-weak'
  if (passwordStrength.value <= 4) return 'strength-medium'
  return 'strength-strong'
})

// 切换密码显示/隐藏
const togglePassword = (field: 'password' | 'confirmPassword') => {
  if (field === 'password') {
    showPassword.value = !showPassword.value
  } else {
    showConfirmPassword.value = !showConfirmPassword.value
  }
}

// 检测密码强度
const checkPasswordStrength = () => {
  const password = form.value.password
  let strength = 0
  
  if (password.length === 0) {
    strength = 0
    return
  }
  
  // 长度检测
  if (password.length >= 8) strength++
  if (password.length >= 12) strength++
  
  // 包含数字
  if (/\d/.test(password)) strength++
  
  // 包含小写字母
  if (/[a-z]/.test(password)) strength++
  
  // 包含大写字母
  if (/[A-Z]/.test(password)) strength++
  
  // 包含特殊字符
  if (/[!@#$%^&*(),.?":{}|<>]/.test(password)) strength++
  
  passwordStrength.value = strength
}

// 处理注册
const handleRegister = async () => {
  if (form.value.password !== form.value.confirmPassword) {
    return
  }
  
  const authStore = useAuthStore()
  loading.value = true
  try {
    // 构建注册请求数据
    const registerData: RegisterRequest = {
      username: form.value.username,
      email: form.value.email,
      password: form.value.password,
      phone: form.value.phone
    }
    
    const response = await authApi.register(registerData)
    // 注册成功后自动登录
    authStore.setToken(response.token)
    // 根据响应创建用户对象
    const user = {
      id: 0, // 暂时使用0，实际id会在后续请求中获取
      username: response.username,
      email: form.value.email,
      role: response.role,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    }
    authStore.setUser(user)
    router.push('/')
  } catch (error: any) {
    console.error('注册失败:', error)
    // 显示错误信息
    const errorMessage = error.response?.data?.message || error.message || '注册失败，请稍后重试'
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
  --info-color: #17a2b8;
}

.register-container {
  min-height: 100vh;
  background: linear-gradient(135deg, var(--primary-color) 0%, #0056b3 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem 0;
}

.register-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.1);
  overflow: hidden;
  max-width: 1000px;
  width: 100%;
  margin: 0 1rem;
  border: 1px solid rgba(0,0,0,0.05);
}

.register-left {
  background: linear-gradient(135deg, var(--info-color), #087990);
  color: white;
  padding: 4rem 3rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 600px;
  position: relative;
  overflow: hidden;
}

.register-left::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  animation: float 25s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  33% { transform: translate(20px, -20px) rotate(120deg); }
  66% { transform: translate(-20px, 10px) rotate(240deg); }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

.brand-logo {
  animation: pulse 2s infinite ease-in-out;
  display: inline-block;
  color: white;
  text-shadow: 0 4px 8px rgba(0,0,0,0.3);
}

.register-right {
  padding: 2rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 600px;
}

.register-left h3 {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 2rem;
  position: relative;
  z-index: 2;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
}

.feature-item {
  display: flex;
  align-items: center;
  margin-bottom: 1.5rem;
  padding: 1rem;
  background: rgba(255,255,255,0.1);
  border-radius: 15px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.2);
  transition: all 0.3s ease;
  position: relative;
  z-index: 2;
}

.feature-item:hover {
  background: rgba(255,255,255,0.2);
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.2);
}

.feature-icon {
  width: 60px;
  height: 60px;
  background: rgba(255,255,255,0.25);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 1.5rem;
  font-size: 1.5rem;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.feature-item:hover .feature-icon {
  background: rgba(255,255,255,0.35);
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
}

.feature-item h5 {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 0.25rem;
  text-shadow: 0 1px 2px rgba(0,0,0,0.2);
}

.feature-item p {
  margin: 0;
  opacity: 0.9;
  font-size: 0.9rem;
  text-shadow: 0 1px 2px rgba(0,0,0,0.1);
}

.form-floating {
  margin-bottom: 1rem;
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

.register-btn {
  width: 100%;
  padding: 0.75rem;
  border-radius: 10px;
  font-weight: 600;
  margin-top: 1rem;
  transition: all 0.2s;
}

.register-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 123, 255, 0.3);
}

.password-strength {
  height: 5px;
  border-radius: 3px;
  margin-top: 0.5rem;
  transition: all 0.3s ease;
}

.strength-weak { background: #dc3545; width: 33%; }
.strength-medium { background: #ffc107; width: 66%; }
.strength-strong { background: #198754; width: 100%; }

@media (max-width: 768px) {
  .register-left {
    display: none;
  }
  
  .register-right {
    padding: 1.5rem;
    min-height: auto;
  }
  
  .register-card {
    margin: 0 0.5rem;
  }
}

@media (max-width: 576px) {
  .register-right {
    padding: 1rem;
  }
  
  .row > .col-md-6 {
    margin-bottom: 1rem;
  }
}
</style>
