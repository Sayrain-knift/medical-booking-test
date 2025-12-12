<template>
  <div>
    <!-- 导航栏 -->
    <Navbar />
    
    <!-- 主要内容 -->
    <main style="padding-top: 80px;">
      <!-- 页面标题 -->
      <section class="py-5 bg-primary text-white">
        <div class="container">
          <h1 class="display-5 fw-bold">医生科室</h1>
          <p class="lead">选择您需要的科室和医生，享受专业医疗服务</p>
        </div>
      </section>
      
      <!-- 科室选择和医生列表 -->
      <section class="py-5">
        <div class="container">
          <div class="row">
            <!-- 科室筛选 -->
            <div class="col-md-3 mb-4">
              <div class="card shadow-sm">
                <div class="card-header bg-light">
                  <h5 class="mb-0">科室筛选</h5>
                </div>
                <div class="card-body">
                  <div class="mb-4">
                    <h6>所有科室</h6>
                    <div class="list-group">
                      <button 
                        type="button" 
                        class="list-group-item list-group-item-action" 
                        :class="{ active: selectedDepartmentId === null }"
                        @click="selectDepartment(null)"
                      >
                        全部医生
                      </button>
                      <button 
                        type="button" 
                        class="list-group-item list-group-item-action" 
                        v-for="dept in departments" 
                        :key="dept.id"
                        :class="{ active: selectedDepartmentId === dept.id }"
                        @click="selectDepartment(dept.id)"
                      >
                        {{ dept.name }}
                      </button>
                    </div>
                  </div>
                  
                  <div>
                    <h6>筛选条件</h6>
                    <div class="mb-3">
                      <label for="searchName" class="form-label">医生姓名</label>
                      <input 
                        type="text" 
                        class="form-control" 
                        id="searchName"
                        v-model="searchName"
                        placeholder="输入医生姓名"
                        @input="debouncedSearch"
                      >
                    </div>
                    <div class="mb-3">
                      <label for="title" class="form-label">职称</label>
                      <select class="form-select" id="title" v-model="selectedTitle">
                        <option value="">全部职称</option>
                        <option value="主任医师">主任医师</option>
                        <option value="副主任医师">副主任医师</option>
                        <option value="主治医师">主治医师</option>
                        <option value="住院医师">住院医师</option>
                      </select>
                    </div>
                    <div>
                      <button class="btn btn-primary w-100" @click="applyFilters">
                        <i class="bi bi-filter me-2"></i>应用筛选
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 医生列表 -->
            <div class="col-md-9">
              <div class="d-flex justify-content-between align-items-center mb-4">
                <h2 class="h4">医生列表</h2>
                <span class="badge bg-primary text-white">{{ doctors.length }} 位医生</span>
              </div>
              
              <div v-if="loading" class="text-center py-5">
                <div class="spinner-border text-primary" role="status">
                  <span class="visually-hidden">加载中...</span>
                </div>
                <div class="mt-2">加载医生数据中...</div>
              </div>
              
              <div v-else-if="doctors.length === 0" class="text-center py-5">
                <i class="bi bi-people" style="font-size: 3rem; color: #ccc;"></i>
                <div class="mt-2 text-muted">暂无医生数据</div>
              </div>
              
              <div v-else class="row">
                <div class="col-md-4 mb-4" v-for="doctor in doctors" :key="doctor.id">
                  <div class="doctor-card h-100">
                    <div class="doctor-avatar">
                      <i class="bi bi-person"></i>
                    </div>
                    <div class="card-body">
                      <div class="d-flex justify-content-between align-items-start mb-2">
                        <h5 class="card-title mb-0">{{ doctor.name }}</h5>
                        <span class="badge bg-secondary">{{ doctor.title }}</span>
                      </div>
                      <p class="text-muted small mb-2">{{ doctor.department.name }}</p>
                      <p class="card-text text-muted small mb-3">{{ doctor.expertise }}</p>
                      <div class="d-flex gap-2">
                        <router-link :to="`/appointment?doctorId=${doctor.id}`" class="btn btn-primary btn-sm flex-fill">
                          <i class="bi bi-calendar-plus me-1"></i>预约
                        </router-link>
                        <button class="btn btn-outline-primary btn-sm" @click="showDoctorDetails(doctor)">
                          <i class="bi bi-info-circle me-1"></i>详情
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>
    
    <!-- 医生详情模态框 -->
    <div class="modal fade" id="doctorDetailsModal" tabindex="-1" aria-labelledby="doctorDetailsModalLabel" aria-hidden="true">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="doctorDetailsModalLabel">{{ selectedDoctor?.name }} 医生详情</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div v-if="selectedDoctor" class="modal-body">
            <div class="row">
              <div class="col-md-4">
                <div class="doctor-avatar-large mb-4">
                  <i class="bi bi-person"></i>
                </div>
                <div class="list-group">
                  <div class="list-group-item">
                    <strong>姓名:</strong> {{ selectedDoctor.name }}
                  </div>
                  <div class="list-group-item">
                    <strong>职称:</strong> {{ selectedDoctor.title }}
                  </div>
                  <div class="list-group-item">
                    <strong>科室:</strong> {{ selectedDoctor.department.name }}
                  </div>
                  <div class="list-group-item">
                    <strong>专长:</strong> {{ selectedDoctor.expertise }}
                  </div>
                </div>
                <div class="mt-4 text-center">
                  <router-link :to="`/appointment?doctorId=${selectedDoctor.id}`" class="btn btn-primary w-100">
                    <i class="bi bi-calendar-plus me-2"></i>立即预约
                  </router-link>
                </div>
              </div>
              <div class="col-md-8">
                <h6>医生简介</h6>
                <p class="mb-4">{{ selectedDoctor.bio || '暂无简介信息' }}</p>
                
                <h6>擅长领域</h6>
                <ul class="list-unstyled mb-4">
                  <li v-for="(skill, index) in selectedDoctor.expertise.split('、')" :key="index">
                    <i class="bi bi-check-circle text-success me-2"></i>{{ skill }}
                  </li>
                </ul>
                
                <h6>近期排班</h6>
                <div class="table-responsive">
                  <table class="table table-sm">
                    <thead>
                      <tr>
                        <th>日期</th>
                        <th>时间段</th>
                        <th>剩余号源</th>
                        <th>操作</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="schedule in upcomingSchedules" :key="schedule.id">
                        <td>{{ formatDate(schedule.date) }}</td>
                        <td>{{ schedule.startTime }} - {{ schedule.endTime }}</td>
                        <td>
                          <span class="badge bg-success">{{ schedule.maxPatients }} 个号源</span>
                        </td>
                        <td>
                          <router-link :to="`/appointment?doctorId=${selectedDoctor.id}&scheduleId=${schedule.id}&date=${schedule.date}`" 
                                     class="btn btn-sm btn-outline-primary">
                            预约
                          </router-link>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
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
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Navbar from '../components/Navbar.vue'
import { departmentApi, doctorApi } from '../services/api'
import type { Department, Doctor, Schedule } from '../types'

// 状态
const loading = ref(false)
const departments = ref<Department[]>([])
const doctors = ref<Doctor[]>([])
const upcomingSchedules = ref<Schedule[]>([])
const selectedDepartmentId = ref<number | null>(null)
const selectedTitle = ref('')
const searchName = ref('')
const selectedDoctor = ref<Doctor | null>(null)
const pendingDoctorId = ref<number | null>(null)

const route = useRoute()
const router = useRouter()

const updateRouteQuery = (patch: Record<string, string | number | null | undefined>) => {
  const newQuery: Record<string, any> = { ...route.query }
  Object.entries(patch).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') {
      delete newQuery[key]
    } else {
      newQuery[key] = String(value)
    }
  })
  router.replace({ query: newQuery })
}

const syncRouteParams = () => {
  const { departmentId, doctorId } = route.query
  if (typeof departmentId === 'string') {
    selectedDepartmentId.value = Number(departmentId)
  } else if (departmentId === undefined) {
    selectedDepartmentId.value = null
  }
  if (typeof doctorId === 'string') {
    pendingDoctorId.value = Number(doctorId)
  } else if (doctorId === undefined) {
    pendingDoctorId.value = null
  }
}

// 防抖搜索
const debouncedSearch = computed(() => {
  let timeout: ReturnType<typeof setTimeout>
  return () => {
    clearTimeout(timeout)
    timeout = setTimeout(() => {
      loadDoctors()
    }, 500)
  }
})

// 格式化日期
const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    weekday: 'long'
  })
}

// 选择科室
const selectDepartment = (deptId: number | null) => {
  selectedDepartmentId.value = deptId
  updateRouteQuery({ departmentId: deptId ?? null })
  loadDoctors()
}

// 应用筛选条件
const applyFilters = () => {
  loadDoctors()
}

// 显示医生详情
const showDoctorDetails = async (doctor: Doctor, options: { updateRoute?: boolean } = {}) => {
  selectedDoctor.value = doctor
  if (options.updateRoute !== false) {
    updateRouteQuery({ doctorId: doctor.id })
  }
  try {
    const schedules = await doctorApi.getSchedules(doctor.id)
    // 获取未来7天的排班
    const now = new Date()
    upcomingSchedules.value = schedules.filter(schedule => {
      const scheduleDate = new Date(schedule.date)
      const diffDays = Math.ceil((scheduleDate.getTime() - now.getTime()) / (1000 * 60 * 60 * 24))
      return diffDays >= 0 && diffDays <= 7
    }).slice(0, 5) // 只显示最近5条
    
    // 显示模态框
    const modal = new (window as any).bootstrap.Modal(document.getElementById('doctorDetailsModal'))
    modal.show()
  } catch (error) {
    console.error('加载排班失败:', error)
  }
}

// 加载科室数据
const loadDepartments = async () => {
  try {
    departments.value = await departmentApi.getAll()
  } catch (error) {
    console.error('加载科室数据失败:', error)
  }
}

// 加载医生数据
const loadDoctors = async () => {
  loading.value = true
  try {
    let params: any = {}
    if (selectedDepartmentId.value) {
      params.departmentId = selectedDepartmentId.value
    }
    if (searchName.value) {
      params.name = searchName.value
    }
    if (selectedTitle.value) {
      params.title = selectedTitle.value
    }
    doctors.value = await doctorApi.getAll(params)

    if (pendingDoctorId.value) {
      const doctor = doctors.value.find(item => item.id === pendingDoctorId.value)
      if (doctor) {
        await showDoctorDetails(doctor, { updateRoute: false })
      }
      pendingDoctorId.value = null
    }
  } catch (error) {
    console.error('加载医生数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 组件挂载时加载数据
onMounted(() => {
  syncRouteParams()
  loadDepartments()
  loadDoctors()
})

watch(
  () => route.query,
  () => {
    syncRouteParams()
  }
)
</script>

<style scoped>
.doctor-card {
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 3px 10px rgba(0,0,0,0.1);
  transition: all 0.3s ease;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.doctor-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 20px rgba(0,0,0,0.15);
}

.doctor-avatar {
  width: 100%;
  height: 150px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 3rem;
  color: white;
}

.doctor-avatar-large {
  width: 200px;
  height: 200px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 5rem;
  color: white;
  margin: 0 auto;
  border-radius: 50%;
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
}
</style>
