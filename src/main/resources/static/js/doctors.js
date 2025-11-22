// 医生科室页面功能
class DoctorsPage {
    constructor() {
        this.departments = [];
        this.doctors = [];
        this.filteredDoctors = [];
        this.currentPage = 1;
        this.pageSize = 12;
        this.selectedDepartment = '';
        this.selectedTitle = '';
        this.searchQuery = '';
        this.activeFilters = new Set();
        
        this.init();
    }
    
    async init() {
        try {
            // 检查用户登录状态
            authManager.updateAuthUI();
            
            // 加载数据
            await this.loadDepartments();
            await this.loadDoctors();
            
            // 初始化事件监听
            this.initEventListeners();
            
            // 渲染页面
            this.renderDepartments();
            this.renderDoctors();
            this.updateStatistics();
            
        } catch (error) {
            console.error('页面初始化失败:', error);
            Utils.showAlert('页面加载失败，请刷新重试', 'danger');
        }
    }
    
    // 加载科室数据
    async loadDepartments() {
        try {
            const response = await ApiService.departments.getAll();
            this.departments = response.data || [];
            
            // 更新科室筛选器
            this.updateDepartmentFilter();
        } catch (error) {
            console.error('加载科室数据失败:', error);
            // 使用默认数据
            this.departments = this.getDefaultDepartments();
        }
    }
    
    // 加载医生数据
    async loadDoctors() {
        try {
            const response = await ApiService.doctors.getAll();
            this.doctors = response.data || [];
            this.filteredDoctors = [...this.doctors];
        } catch (error) {
            console.error('加载医生数据失败:', error);
            // 使用默认数据
            this.doctors = this.getDefaultDoctors();
            this.filteredDoctors = [...this.doctors];
        }
    }
    
    // 初始化事件监听
    initEventListeners() {
        // 搜索功能
        document.getElementById('doctorSearch').addEventListener('input', (e) => {
            this.searchQuery = e.target.value.toLowerCase();
            this.filterDoctors();
        });
        
        // 科室筛选
        document.getElementById('departmentFilter').addEventListener('change', (e) => {
            this.selectedDepartment = e.target.value;
            this.filterDoctors();
        });
        
        // 职称筛选
        document.getElementById('titleFilter').addEventListener('change', (e) => {
            this.selectedTitle = e.target.value;
            this.filterDoctors();
        });
        
        // 快速筛选标签
        document.querySelectorAll('.filter-tag').forEach(tag => {
            tag.addEventListener('click', () => {
                const filter = tag.dataset.filter;
                if (this.activeFilters.has(filter)) {
                    this.activeFilters.delete(filter);
                    tag.classList.remove('active');
                } else {
                    this.activeFilters.add(filter);
                    tag.classList.add('active');
                }
                this.filterDoctors();
            });
        });
        
        // 加载更多按钮
        document.getElementById('loadMoreBtn').addEventListener('click', () => {
            this.loadMoreDoctors();
        });
    }
    
    // 筛选医生
    filterDoctors() {
        this.filteredDoctors = this.doctors.filter(doctor => {
            // 搜索查询筛选
            if (this.searchQuery) {
                const searchText = `${doctor.name} ${doctor.title} ${doctor.department} ${doctor.specialties?.join(' ') || ''}`.toLowerCase();
                if (!searchText.includes(this.searchQuery)) {
                    return false;
                }
            }
            
            // 科室筛选
            if (this.selectedDepartment && doctor.department !== this.selectedDepartment) {
                return false;
            }
            
            // 职称筛选
            if (this.selectedTitle && doctor.title !== this.selectedTitle) {
                return false;
            }
            
            // 快速筛选
            if (this.activeFilters.size > 0) {
                for (const filter of this.activeFilters) {
                    switch (filter) {
                        case 'expert':
                            if (!doctor.title?.includes('主任') && !doctor.title?.includes('主任')) {
                                return false;
                            }
                            break;
                        case 'available':
                            if (!doctor.availableToday) {
                                return false;
                            }
                            break;
                        case 'weekend':
                            if (!doctor.weekendClinic) {
                                return false;
                            }
                            break;
                        case 'online':
                            if (!doctor.onlineConsultation) {
                                return false;
                            }
                            break;
                    }
                }
            }
            
            return true;
        });
        
        this.currentPage = 1;
        this.renderDoctors();
    }
    
    // 渲染科室
    renderDepartments() {
        const container = document.getElementById('departmentsContainer');
        container.innerHTML = '';
        
        this.departments.slice(0, 8).forEach(dept => {
            const card = this.createDepartmentCard(dept);
            container.appendChild(card);
        });
    }
    
    // 创建科室卡片
    createDepartmentCard(dept) {
        const col = document.createElement('div');
        col.className = 'col-md-3';
        
        const icons = {
            '内科': 'bi-heart-pulse',
            '外科': 'bi-activity',
            '妇产科': 'bi-person-heart',
            '儿科': 'bi-emoji-smile',
            '眼科': 'bi-eye',
            '耳鼻喉科': 'bi-ear',
            '口腔科': 'bi-mouth',
            '皮肤科': 'bi-patch-check',
            '中医科': 'bi-yin-yang',
            '心理科': 'bi-emoji-smile'
        };
        
        const icon = icons[dept.name] || 'bi-hospital';
        
        col.innerHTML = `
            <div class="card department-card" onclick="doctorsPage.selectDepartment('${dept.name}')">
                <div class="card-body text-center">
                    <div class="department-icon">
                        <i class="bi ${icon}"></i>
                    </div>
                    <h5 class="card-title">${dept.name}</h5>
                    <p class="card-text text-muted">${dept.description || '专业医疗服务'}</p>
                    <div class="text-primary">
                        <small>${dept.doctorCount || 0} 位医生</small>
                    </div>
                </div>
            </div>
        `;
        
        return col;
    }
    
    // 选择科室
    selectDepartment(deptName) {
        document.getElementById('departmentFilter').value = deptName;
        this.selectedDepartment = deptName;
        this.filterDoctors();
        
        // 滚动到医生列表
        document.querySelector('#doctorsContainer').scrollIntoView({ 
            behavior: 'smooth' 
        });
    }
    
    // 渲染医生
    renderDoctors() {
        const container = document.getElementById('doctorsContainer');
        container.innerHTML = '';
        
        const doctorsToShow = this.filteredDoctors.slice(0, this.currentPage * this.pageSize);
        
        if (doctorsToShow.length === 0) {
            container.innerHTML = `
                <div class="col-12 text-center py-5">
                    <i class="bi bi-search" style="font-size: 3rem; color: #ccc;"></i>
                    <p class="text-muted mt-3">暂无符合条件的医生</p>
                </div>
            `;
            return;
        }
        
        doctorsToShow.forEach(doctor => {
            const card = this.createDoctorCard(doctor);
            container.appendChild(card);
        });
        
        // 更新加载更多按钮
        const loadMoreBtn = document.getElementById('loadMoreBtn');
        if (doctorsToShow.length >= this.filteredDoctors.length) {
            loadMoreBtn.style.display = 'none';
        } else {
            loadMoreBtn.style.display = 'inline-block';
        }
    }
    
    // 创建医生卡片
    createDoctorCard(doctor) {
        const col = document.createElement('div');
        col.className = 'col-md-4 col-lg-3';
        
        const specialties = doctor.specialties?.slice(0, 3).join('、') || '综合诊疗';
        const rating = doctor.rating || 4.5;
        const availableToday = doctor.availableToday !== false;
        
        col.innerHTML = `
            <div class="card doctor-card">
                <img src="${doctor.avatar || 'https://via.placeholder.com/300x250/f8f9fa/6c757d?text=医生照片'}" 
                     alt="${doctor.name}" class="doctor-avatar">
                <div class="doctor-info">
                    <h5 class="doctor-name">${doctor.name}</h5>
                    <div class="doctor-title">${doctor.title || '主治医师'}</div>
                    <div class="doctor-department">
                        <i class="bi bi-building me-1"></i>${doctor.department || '内科'}
                    </div>
                    <div class="doctor-badges">
                        <span class="badge-custom">${specialties}</span>
                        ${availableToday ? '<span class="badge-custom text-success">今日可约</span>' : ''}
                        ${doctor.onlineConsultation ? '<span class="badge-custom text-info">在线问诊</span>' : ''}
                    </div>
                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <div class="text-warning">
                            <i class="bi bi-star-fill"></i>
                            <span>${rating}</span>
                        </div>
                        <div class="text-muted small">
                            预约 ${doctor.appointmentCount || 128} 次
                        </div>
                    </div>
                    <div class="d-grid gap-2">
                        <button class="btn btn-primary btn-sm" onclick="doctorsPage.bookDoctor(${doctor.id})">
                            <i class="bi bi-calendar-plus me-1"></i>立即预约
                        </button>
                        <button class="btn btn-outline-info btn-sm" onclick="doctorsPage.viewDoctor(${doctor.id})">
                            <i class="bi bi-eye me-1"></i>查看详情
                        </button>
                    </div>
                </div>
            </div>
        `;
        
        return col;
    }
    
    // 预约医生
    bookDoctor(doctorId) {
        if (!authManager.isLoggedIn()) {
            Utils.showAlert('请先登录后再预约', 'warning');
            setTimeout(() => {
                window.location.href = '/login?redirect=/appointment.html?doctor=' + doctorId;
            }, 1500);
            return;
        }
        
        window.location.href = `/appointment.html?doctor=${doctorId}`;
    }
    
    // 查看医生详情
    viewDoctor(doctorId) {
        // 这里可以打开模态框或跳转到详情页
        window.location.href = `/doctor-detail.html?id=${doctorId}`;
    }
    
    // 加载更多医生
    loadMoreDoctors() {
        this.currentPage++;
        this.renderDoctors();
    }
    
    // 更新科室筛选器
    updateDepartmentFilter() {
        const select = document.getElementById('departmentFilter');
        select.innerHTML = '<option value="">全部科室</option>';
        
        this.departments.forEach(dept => {
            const option = document.createElement('option');
            option.value = dept.name;
            option.textContent = dept.name;
            select.appendChild(option);
        });
    }
    
    // 更新统计数据
    updateStatistics() {
        document.getElementById('totalDoctors').textContent = this.doctors.length;
        document.getElementById('totalDepartments').textContent = this.departments.length;
        
        // 模拟今日预约数
        const todayAppointments = Math.floor(Math.random() * 50) + 20;
        document.getElementById('todayAppointments').textContent = todayAppointments;
    }
    
    // 获取默认科室数据
    getDefaultDepartments() {
        return [
            { id: 1, name: '内科', description: '心血管、消化、呼吸等内科疾病', doctorCount: 45 },
            { id: 2, name: '外科', description: '普外、骨科、神经外科等', doctorCount: 38 },
            { id: 3, name: '妇产科', description: '妇科疾病、产科护理', doctorCount: 28 },
            { id: 4, name: '儿科', description: '儿童常见疾病、预防保健', doctorCount: 32 },
            { id: 5, name: '眼科', description: '眼部疾病、视力矫正', doctorCount: 22 },
            { id: 6, name: '耳鼻喉科', description: '耳鼻喉疾病治疗', doctorCount: 18 },
            { id: 7, name: '口腔科', description: '口腔疾病、牙齿矫正', doctorCount: 25 },
            { id: 8, name: '皮肤科', description: '皮肤疾病、美容护肤', doctorCount: 20 },
            { id: 9, name: '中医科', description: '中医诊疗、针灸推拿', doctorCount: 30 },
            { id: 10, name: '心理科', description: '心理咨询、精神健康', doctorCount: 15 }
        ];
    }
    
    // 获取默认医生数据
    getDefaultDoctors() {
        const departments = ['内科', '外科', '妇产科', '儿科', '眼科', '耳鼻喉科', '口腔科', '皮肤科', '中医科', '心理科'];
        const titles = ['主任医师', '副主任医师', '主治医师', '住院医师'];
        const doctors = [];
        
        for (let i = 1; i <= 50; i++) {
            doctors.push({
                id: i,
                name: `医生${i}`,
                title: titles[Math.floor(Math.random() * titles.length)],
                department: departments[Math.floor(Math.random() * departments.length)],
                specialties: ['心血管疾病', '高血压', '冠心病', '心律失常'].slice(0, Math.floor(Math.random() * 3) + 1),
                rating: (Math.random() * 1.5 + 3.5).toFixed(1),
                appointmentCount: Math.floor(Math.random() * 200) + 50,
                availableToday: Math.random() > 0.3,
                weekendClinic: Math.random() > 0.5,
                onlineConsultation: Math.random() > 0.4,
                avatar: `https://via.placeholder.com/300x250/0056b3/ffffff?text=医生${i}`
            });
        }
        
        return doctors;
    }
}

// 初始化页面
let doctorsPage;
document.addEventListener('DOMContentLoaded', function() {
    doctorsPage = new DoctorsPage();
});