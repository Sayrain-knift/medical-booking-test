<template>
  <div>
    <!-- 导航栏 -->
    <Navbar />
    
    <!-- 主要内容 -->
    <main style="padding-top: 80px;">
      <!-- 页面标题 -->
      <section class="py-5 bg-primary text-white">
        <div class="container">
          <h1 class="display-5 fw-bold">预约挂号</h1>
          <p class="lead">选择您需要的医生和时间，完成预约</p>
        </div>
      </section>
      
      <!-- 预约流程 -->
      <section class="py-5">
        <div class="container">
          <div class="row">
            <!-- 预约流程步骤 -->
            <div class="col-md-12 mb-4">
              <div class="card shadow-sm">
                <div class="card-body">
                  <div class="row text-center">
                    <div class="col-md-3">
                      <div class="step-item active">
                        <div class="step-number">1</div>
                        <div class="step-title">选择医生</div>
                      </div>
                    </div>
                    <div class="col-md-3">
                      <div class="step-item" :class="{ active: selectedDoctorId !== null }">
                        <div class="step-number">2</div>
                        <div class="step-title">选择日期</div>
                      </div>
                    </div>
                    <div class="col-md-3">
                      <div class="step-item" :class="{ active: selectedDate && selectedDoctorId !== null }">
                        <div class="step-number">3</div>
                        <div class="step-title">选择时段</div>
                      </div>
                    </div>
                    <div class="col-md-3">
                      <div class="step-item" :class="{ active: selectedSlot && selectedDate && selectedDoctorId !== null }">
                        <div class="step-number">4</div>
                        <div class="step-title">确认预约</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="row">
            <!-- 医生选择 -->
            <div class="col-md-4 mb-4">
              <div class="card shadow-sm">
                <div class="card-header bg-light">
                  <h5 class="mb-0">选择医生</h5>
                </div>
                <div class="card-body">
                  <div class="mb-3">
                    <label for="doctorSelect" class="form-label">请选择医生</label>
                    <select 
                      class="form-select" 
                      id="doctorSelect" 
                      v-model="selectedDoctorId"
                      @change="onDoctorChange"
                    >
                      <option value="">-- 请选择医生 --</option>
                      <option 
                        v-for="doctor in doctors" 
                        :key="doctor.id"
                        :value="doctor.id"
                      >
                        {{ doctor.name }} - {{ doctor.department.name }}
                      </option>
                    </select>
                  </div>
                  
                  <div v-if="selectedDoctor" class="doctor-info">
                    <div class="doctor-avatar-small mb-2">
                      <i class="bi bi-person"></i>
                    </div>
                    <h6 class="mb-0">{{ selectedDoctor.name }}</h6>
                    <p class="text-muted small">{{ selectedDoctor.title }} | {{ selectedDoctor.department.name }}</p>
                    <p class="text-muted small mt-2">{{ selectedDoctor.expertise }}</p>
                  </div>
                </div>
              </div>
              
              <!-- 医生详情卡片 -->
              <div v-if="selectedDoctor" class="card shadow-sm mt-4">
                <div class="card-header bg-light">
                  <h6 class="mb-0">医生详情</h6>
                </div>
                <div class="card-body">
                  <p class="text-muted small">{{ selectedDoctor.bio || '暂无详细介绍' }}</p>
                </div>
              </div>
            </div>
            
            <!-- 预约信息 -->
            <div class="col-md-8">
              <div class="card shadow-sm">
                <div class="card-header bg-light">
                  <h5 class="mb-0">预约信息</h5>
                </div>
                <div class="card-body">
                  <!-- 日期选择 -->
                  <div v-if="selectedDoctorId" class="mb-4">
                    <h6>选择日期</h6>
                    <div class="calendar-container">
                      <div class="calendar-header">
                        <button class="btn btn-sm btn-outline-primary" @click="prevMonth">&lt;</button>
                        <h6 class="mb-0 mx-3">{{ currentMonthYear }}</h6>
                        <button class="btn btn-sm btn-outline-primary" @click="nextMonth">&gt;</button>
                      </div>
                      <div class="calendar-grid">
                        <div class="calendar-day-header" v-for="day in weekDays" :key="day">{{ day }}</div>
                        <div 
                          class="calendar-day" 
                          v-for="day in calendarDays" 
                          :key="day.date"
                          :class="{
                            'calendar-day-today': isToday(day.date),
                            'calendar-day-selected': isSelectedDate(day.date),
                            'calendar-day-disabled': day.disabled
                          }"
                          @click="selectDate(day.date)"
                        >
                          {{ day.day }}
                        </div>
                      </div>
                    </div>
                  </div>
                  
                  <!-- 时段选择 -->
                  <div v-if="selectedDate" class="mb-4">
                    <h6>选择时段</h6>
                    <div class="slots-container">
                      <button 
                        v-for="slot in availableSlots" 
                        :key="slot.id"
                        class="slot-item"
                        :class="{
                          'slot-item-selected': selectedSlot?.id === slot.id
                        }"
                        @click="selectSlot(slot)"
                      >
                        {{ slot.startTime }} - {{ slot.endTime }}
                      </button>
                    </div>
                    <div v-if="availableSlots.length === 0" class="text-center py-3">
                      <p class="text-muted">该日期暂无可用时段</p>
                    </div>
                  </div>
                  
                  <!-- 预约确认 -->
                  <div v-if="selectedSlot" class="mb-4">
                    <h6>预约确认</h6>
                    <div class="table-responsive">
                      <table class="table table-sm">
                        <tbody>
                          <tr>
                            <th scope="row">医生</th>
                            <td>{{ selectedDoctor?.name }} - {{ selectedDoctor?.department.name }}</td>
                          </tr>
                          <tr>
                            <th scope="row">日期</th>
                            <td>{{ formatDate(selectedDate) }}</td>
                          </tr>
                          <tr>
                            <th scope="row">时段</th>
                            <td>{{ selectedSlot.startTime }} - {{ selectedSlot.endTime }}</td>
                          </tr>
                          <tr>
                            <th scope="row">就诊人</th>
                            <td>
                              <input 
                                type="text" 
                                class="form-control" 
                                v-model="patientName"
                                placeholder="请输入就诊人姓名"
                              >
                            </td>
                          </tr>
                          <tr>
                            <th scope="row">联系电话</th>
                            <td>
                              <input 
                                type="tel" 
                                class="form-control" 
                                v-model="patientPhone"
                                placeholder="请输入联系电话"
                                pattern="[0-9]{11}"
                              >
                            </td>
                          </tr>
                          <tr>
                            <th scope="row">症状描述</th>
                            <td>
                              <textarea 
                                class="form-control" 
                                v-model="symptoms"
                                placeholder="请简要描述症状"
                                rows="3"
                              ></textarea>
                            </td>
                          </tr>
                        </tbody>
                      </table>
                    </div>
                    
                    <div class="d-flex gap-2">
                      <button 
                        class="btn btn-primary flex-fill"
                        @click="submitAppointment"
                        :disabled="!isFormValid || loading"
                      >
                        <i class="bi bi-check-circle me-2"></i>
                        {{ loading ? '提交中...' : '确认预约' }}
                      </button>
                      <button 
                        class="btn btn-outline-secondary"
                        @click="resetForm"
                      >
                        <i class="bi bi-x-circle me-2"></i>重置
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>
    
    <!-- 预约成功模态框 -->
    <div class="modal fade" id="successModal" tabindex="-1" aria-labelledby="successModalLabel" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header bg-success text-white">
            <h5 class="modal-title" id="successModalLabel">预约成功</h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body text-center">
            <div class="success-icon mb-3">
              <i class="bi bi-check-circle" style="font-size: 4rem; color: #198754;"></i>
            </div>
            <h6>您的预约已成功提交！</h6>
            <p class="text-muted mb-4">预约信息已发送至您的手机，请按时就诊。</p>
            <div class="text-start">
              <p><strong>预约编号:</strong> {{ appointmentNumber }}</p>
              <p><strong>就诊时间:</strong> {{ formatDate(selectedDate) }} {{ selectedSlot?.startTime }} - {{ selectedSlot?.endTime }}</p>
              <p><strong>医生:</strong> {{ selectedDoctor?.name }}</p>
              <p><strong>就诊人:</strong> {{ patientName }}</p>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-primary" data-bs-dismiss="modal">查看我的预约</button>
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal" @click="resetForm">返回首页</button>
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
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import  Navbar  from '../components/Navbar.vue'
import { doctorApi, appointmentApi } from '../services/api'
import type { Doctor, Schedule } from '../types'

// 状态
const loading = ref(false)
const doctors = ref<Doctor[]>([])
const selectedDoctorId = ref<string>('')
const selectedDoctor = ref<Doctor | null>(null)
const selectedDate = ref<string>('')
const selectedSlot = ref<Schedule | null>(null)
const availableSlots = ref<Schedule[]>([])
const patientName = ref<string>('')
const patientPhone = ref<string>('')
const symptoms = ref<string>('')
const appointmentNumber = ref<string>('')
const route = useRoute()

// 日历相关
const currentDate = ref(new Date())
const weekDays = ['日', '一', '二', '三', '四', '五', '六']

// 计算当前月年份
const currentMonthYear = computed(() => {
  return currentDate.value.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long'
  })
})

// 生成日历天数
  const calendarDays = computed(() => {
    const year = currentDate.value.getFullYear()
    const month = currentDate.value.getMonth()
    
    const firstDay = new Date(year, month, 1)
    const startDate = new Date(firstDay)
    startDate.setDate(startDate.getDate() - firstDay.getDay())
    
    const days = []
    for (let i = 0; i < 42; i++) {
      const date = new Date(startDate)
      date.setDate(startDate.getDate() + i)
      
      const day = date.getDate()
      const isCurrentMonth = date.getMonth() === month
      const dateStr = date.toISOString().split('T')[0]
      
      days.push({
        date: dateStr,
        day,
        disabled: !isCurrentMonth
      })
    }
    
    return days
  })

// 计算表单是否有效
const isFormValid = computed(() => {
  return (
    selectedDoctorId.value &&
    selectedDate.value &&
    selectedSlot.value?.id &&
    patientName.value.trim() &&
    patientPhone.value.trim() &&
    /^1[3-9]\d{9}$/.test(patientPhone.value)
  )
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

// 判断是否为今天
const isToday = (dateString?: string) => {
  if (!dateString) return false
  const today = new Date().toISOString().split('T')[0]
  return dateString === today
}

// 判断是否为选中日期
const isSelectedDate = (dateString?: string) => {
  if (!dateString) return false
  return dateString === selectedDate.value
}

// 选择日期
const selectDate = (date?: string) => {
  if (!date) return
  selectedDate.value = date
  loadAvailableSlots()
}

// 选择时段
const selectSlot = (slot: Schedule) => {
  selectedSlot.value = slot
}

// 上一个月
const prevMonth = () => {
  currentDate.value.setMonth(currentDate.value.getMonth() - 1)
}

// 下一个月
const nextMonth = () => {
  currentDate.value.setMonth(currentDate.value.getMonth() + 1)
}

// 医生变更
const onDoctorChange = async () => {
  selectedDoctor.value = doctors.value.find(d => d.id === parseInt(selectedDoctorId.value)) || null
  selectedDate.value = ''
  selectedSlot.value = null
  availableSlots.value = []
  
  if (selectedDoctor.value) {
    // 自动选择最早可预约日期
    await loadAvailableSlots()
  }
}

// 加载可用时段
const normalizeDate = (value: string | Date): string => {
  const dateString = typeof value === 'string' ? value : new Date(value).toISOString()
  const [datePart] = dateString.split('T')
  return datePart || dateString
}

const isSlotAvailable = (slot: Schedule) => {
  if (typeof slot.status === 'number') {
    return slot.status === 1
  }
  const normalizedStatus = slot.status.toUpperCase()
  return slot.status === '1' || normalizedStatus === 'AVAILABLE'
}

const loadAvailableSlots = async (options?: { targetScheduleId?: number }) => {
  if (!selectedDoctorId.value) {
    availableSlots.value = []
    return
  }
  
  try {
    const slots = await doctorApi.getSchedules(parseInt(selectedDoctorId.value))
    const slotsArray = Array.isArray(slots) ? slots : []

    if (options?.targetScheduleId) {
      const matchedSlot = slotsArray.find(slot => slot.id === options.targetScheduleId)
      if (matchedSlot) {
        const targetDate = normalizeDate(matchedSlot.date as string)
        selectedDate.value = targetDate
        availableSlots.value = slotsArray.filter(slot => normalizeDate(slot.date as string) === targetDate && isSlotAvailable(slot))
        selectedSlot.value = matchedSlot
        return
      }
    }

    if (!selectedDate.value) {
      availableSlots.value = []
      return
    }

    availableSlots.value = slotsArray.filter(slot => {
      const slotDateStr = normalizeDate(slot.date as string)
      return slotDateStr === selectedDate.value && isSlotAvailable(slot)
    })
  } catch (error) {
    console.error('加载可用时段失败:', error)
    availableSlots.value = []
  }
}

// 提交预约
const submitAppointment = async () => {
  if (!isFormValid.value) return
  
  loading.value = true
  try {
    // 只需要发送scheduleId到后端，其他信息从用户认证中获取
    const appointmentData = {
      scheduleId: selectedSlot.value?.id
    }
    
    await appointmentApi.create(appointmentData)
    
    // 生成预约编号
    appointmentNumber.value = `AP${Date.now().toString().slice(-8)}`
    
    // 显示成功模态框
    const modal = new (window as any).bootstrap.Modal(document.getElementById('successModal'))
    modal.show()
    
  } catch (error) {
    console.error('预约失败:', error)
    // 显示错误信息给用户
    alert('预约失败，请重试')
  } finally {
    loading.value = false
  }
}

// 重置表单
const resetForm = () => {
  selectedDoctorId.value = ''
  selectedDoctor.value = null
  selectedDate.value = ''
  selectedSlot.value = null
  availableSlots.value = []
  patientName.value = ''
  patientPhone.value = ''
  symptoms.value = ''
}

// 加载医生列表
const loadDoctors = async () => {
  try {
    const data = await doctorApi.getAll()
    doctors.value = data
  } catch (error) {
    console.error('加载医生列表失败:', error)
  }
}

const applyRouteParams = async () => {
  const doctorIdParam = typeof route.query.doctorId === 'string' ? route.query.doctorId : ''
  const scheduleIdParam = typeof route.query.scheduleId === 'string' ? Number(route.query.scheduleId) : null
  const dateParam = typeof route.query.date === 'string' ? route.query.date : ''

  if (doctorIdParam) {
    selectedDoctorId.value = doctorIdParam
    await onDoctorChange()
  }

  if (dateParam) {
    selectedDate.value = dateParam
  }

  if (scheduleIdParam) {
    await loadAvailableSlots({ targetScheduleId: scheduleIdParam })
    return
  }

  if (dateParam && selectedDoctorId.value) {
    await loadAvailableSlots()
  }
}

// 组件挂载时加载数据
onMounted(async () => {
  await loadDoctors()
  await applyRouteParams()
})

// 监听URL参数
watch(selectedDoctorId, (newId) => {
  if (newId) {
    onDoctorChange()
  }
})

watch(
  () => route.query,
  () => {
    applyRouteParams()
  }
)
</script>

<style scoped>
/* 步骤样式 */
.step-item {
  position: relative;
  padding: 1rem;
  transition: all 0.3s ease;
}

.step-number {
  width: 30px;
  height: 30px;
  background: #e9ecef;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 0.5rem;
  font-weight: 600;
  transition: all 0.3s ease;
}

.step-title {
  font-size: 0.9rem;
  color: #6c757d;
  transition: all 0.3s ease;
}

.step-item.active .step-number {
  background: #007bff;
  color: white;
}

.step-item.active .step-title {
  color: #007bff;
  font-weight: 600;
}

/* 医生信息样式 */
.doctor-info {
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
  text-align: center;
}

.doctor-avatar-small {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  color: white;
  margin: 0 auto 0.5rem;
}

/* 日历样式 */
.calendar-container {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  background: #f8f9fa;
  border-bottom: 1px solid #e0e0e0;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
  background: #e0e0e0;
}

.calendar-day-header {
  padding: 0.5rem;
  text-align: center;
  background: #f8f9fa;
  font-weight: 600;
  font-size: 0.9rem;
}

.calendar-day {
  padding: 0.75rem;
  text-align: center;
  background: white;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 0.9rem;
}

.calendar-day:hover:not(.calendar-day-disabled) {
  background: #e3f2fd;
}

.calendar-day-today {
  background: #007bff;
  color: white;
  font-weight: 600;
}

.calendar-day-selected {
  background: #0056b3;
  color: white;
  font-weight: 600;
}

.calendar-day-disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 时段样式 */
.slots-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 0.75rem;
  margin-top: 1rem;
}

.slot-item {
  padding: 0.75rem;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background: white;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: center;
  font-size: 0.9rem;
}

.slot-item:hover {
  border-color: #007bff;
  background: #e3f2fd;
}

.slot-item-selected {
  background: #007bff;
  color: white;
  border-color: #007bff;
}
</style>
