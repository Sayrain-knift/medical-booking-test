<template>
  <div>
    <!-- 导航栏 -->
    <Navbar />
    
    <!-- 主要内容 -->
    <main style="padding-top: 80px;">
      <!-- 英雄区域 -->
      <section class="hero-section">
        <div class="container">
          <div class="row align-items-center min-vh-100">
            <div class="col-lg-6">
              <div class="hero-content">
                <h1 class="display-4 fw-bold text-white mb-4">专业医疗，贴心服务</h1>
                <p class="lead text-white-50 mb-4">在线预约挂号，告别排队烦恼。专业医生团队，为您提供优质的医疗服务。</p>
                <div class="d-flex gap-3">
                  <router-link to="/appointment" class="btn btn-light btn-lg">
                    <i class="bi bi-calendar-plus me-2"></i>立即预约
                  </router-link>
                  <router-link to="/ai-consultation" class="btn btn-outline-light btn-lg">
                    <i class="bi bi-robot me-2"></i>AI问诊
                  </router-link>
                </div>
              </div>
            </div>
            <div class="col-lg-6">
              <div class="hero-image text-center">
                <i class="bi bi-hospital" style="font-size: 15rem; color: rgba(255,255,255,0.1);"></i>
              </div>
            </div>
          </div>
        </div>
      </section>
      
      <!-- 特色服务 -->
      <section class="py-5 bg-light">
        <div class="container">
          <div class="row text-center">
            <div class="col-md-4 mb-4">
              <router-link to="/appointment" class="feature-card-link">
                <div class="feature-card">
                  <div class="feature-icon mb-3">
                    <i class="bi bi-calendar-check"></i>
                  </div>
                  <h4>在线预约</h4>
                  <p>24小时在线预约，随时选择心仪医生</p>
                </div>
              </router-link>
            </div>
            <div class="col-md-4 mb-4">
              <router-link to="/doctors" class="feature-card-link">
                <div class="feature-card">
                  <div class="feature-icon mb-3">
                    <i class="bi bi-people"></i>
                  </div>
                  <h4>专业团队</h4>
                  <p>资深医生团队，提供专业诊疗服务</p>
                </div>
              </router-link>
            </div>
            <div class="col-md-4 mb-4">
              <router-link to="/ai-consultation" class="feature-card-link">
                <div class="feature-card">
                  <div class="feature-icon mb-3">
                    <i class="bi bi-robot"></i>
                  </div>
                  <h4>AI问诊</h4>
                  <p>智能AI助手，提供健康咨询建议</p>
                </div>
              </router-link>
            </div>
          </div>
        </div>
      </section>
      
      <!-- 热门科室 -->
      <section class="py-5">
        <div class="container">
          <h2 class="text-center mb-5">热门科室</h2>
          <div class="row" id="departmentsContainer">
            <div v-if="loadingDepartments" class="col-12 text-center py-5">
              <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">加载中...</span>
              </div>
              <div class="mt-2">加载科室数据中...</div>
            </div>
            <div v-else-if="departments.length === 0" class="col-12 text-center py-5">
              <i class="bi bi-inbox" style="font-size: 3rem; color: #ccc;"></i>
              <div class="mt-2 text-muted">暂无科室数据</div>
            </div>
            <div v-else class="col-md-3 mb-4" v-for="department in departments" :key="department.id">
              <router-link :to="`/doctors?departmentId=${department.id}`" class="department-card-link">
                <div class="department-card">
                  <div class="department-icon">
                    <i :class="department.icon || 'bi bi-hospital'"></i>
                  </div>
                  <h5 class="mb-2">{{ department.name }}</h5>
                  <p class="text-muted">{{ department.description }}</p>
                </div>
              </router-link>
            </div>
          </div>
        </div>
      </section>
      
      <!-- 热门医生 -->
      <section class="py-5 bg-light">
        <div class="container">
          <h2 class="text-center mb-5">推荐医生</h2>
          <div class="row" id="doctorsContainer">
            <div v-if="loadingDoctors" class="col-12 text-center py-5">
              <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">加载中...</span>
              </div>
              <div class="mt-2">加载医生数据中...</div>
            </div>
            <div v-else-if="doctors.length === 0" class="col-12 text-center py-5">
              <i class="bi bi-people" style="font-size: 3rem; color: #ccc;"></i>
              <div class="mt-2 text-muted">暂无医生数据</div>
            </div>
            <div v-else class="col-md-4 mb-4" v-for="doctor in doctors" :key="doctor.id">
              <router-link :to="`/doctors?doctorId=${doctor.id}`" class="doctor-card-link">
                <div class="doctor-card">
                  <div class="doctor-avatar">
                    <i class="bi bi-person"></i>
                  </div>
                  <div class="card-body">
                    <h5 class="card-title">{{ doctor.name }}</h5>
                    <p class="card-text text-muted">{{ doctor.title }}</p>
                    <p class="card-text">{{ doctor.department.name }}</p>
                    <p class="card-text">{{ doctor.expertise }}</p>
                  </div>
                </div>
              </router-link>
            </div>
          </div>
        </div>
      </section>
    </main>
    
    <!-- 页脚 -->
    <footer class="bg-dark text-light py-4">
      <div class="container">
        <div class="row">
          <div class="col-md-6">
            <h5>医疗预约系统</h5>
            <p>为您提供专业、便捷的医疗服务</p>
          </div>
          <div class="col-md-6 text-md-end">
            <p>&copy; 2024 医疗预约系统. 保留所有权利.</p>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { defineAsyncComponent } from 'vue'
const Navbar = defineAsyncComponent(() => import('../components/Navbar.vue'))
import { departmentApi, doctorApi } from '../services/api'
import type { Department, Doctor } from '../types'

// 状态
const loadingDepartments = ref(false)
const loadingDoctors = ref(false)
const departments = ref<Department[]>([])
const doctors = ref<Doctor[]>([])

// 加载科室数据
const loadDepartments = async () => {
  loadingDepartments.value = true
  try {
    const response = await departmentApi.getAll()
    departments.value = response || []
  } catch (error) {
    console.error('加载科室数据失败:', error)
    departments.value = []
  } finally {
    loadingDepartments.value = false
  }
}

// 加载医生数据
const loadDoctors = async () => {
  loadingDoctors.value = true
  try {
    const response = await doctorApi.getAll()
    // 只显示前6个医生
    doctors.value = (response || []).slice(0, 6)
  } catch (error) {
    console.error('加载医生数据失败:', error)
    doctors.value = []
  } finally {
    loadingDoctors.value = false
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadDepartments()
  loadDoctors()
})
</script>

<style scoped>
.hero-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  display: flex;
  align-items: center;
}

.hero-content {
  animation: fadeInUp 0.8s ease-out;
}

.hero-image {
  animation: fadeInRight 0.8s ease-out;
}

.feature-card {
  padding: 2rem;
  border-radius: 10px;
  background: white;
  box-shadow: 0 5px 15px rgba(0,0,0,0.08);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  height: 100%;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0,0,0,0.15);
}

.feature-card-link {
  text-decoration: none;
  color: inherit;
}

.feature-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  font-size: 2rem;
  color: white;
}

.department-card {
  padding: 1.5rem;
  border-radius: 10px;
  background: white;
  box-shadow: 0 3px 10px rgba(0,0,0,0.1);
  transition: all 0.3s ease;
  cursor: pointer;
  text-align: center;
}

.department-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 20px rgba(0,0,0,0.15);
}

.department-card-link {
  text-decoration: none;
  color: inherit;
}

.department-icon {
  font-size: 3rem;
  color: #667eea;
  margin-bottom: 1rem;
}

.doctor-card {
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 3px 10px rgba(0,0,0,0.1);
  transition: all 0.3s ease;
  background: white;
}

.doctor-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 20px rgba(0,0,0,0.15);
}

.doctor-card-link {
  text-decoration: none;
  color: inherit;
}

.doctor-avatar {
  width: 100%;
  height: 200px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 4rem;
  color: white;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInRight {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-section {
    min-height: auto;
    padding: 4rem 0;
  }
  
  .hero-content h1 {
    font-size: 2.5rem;
  }
  
  .hero-image {
    margin-top: 2rem;
  }
  
  .hero-image i {
    font-size: 8rem !important;
  }
}
</style>
