// 首页功能
class HomePage {
    constructor() {
        this.departmentsContainer = document.getElementById('departmentsContainer');
        this.doctorsContainer = document.getElementById('doctorsContainer');
        this.init();
    }

    async init() {
        await this.loadDepartments();
        await this.loadDoctors();
    }

    // 加载科室数据
    async loadDepartments() {
        if (!this.departmentsContainer) return;

        Utils.showLoading(this.departmentsContainer, '加载科室信息...');

        try {
            const departments = await ApiService.departments.getAll();
            
            if (departments && departments.length > 0) {
                this.renderDepartments(departments.slice(0, 6)); // 只显示前6个科室
            } else {
                Utils.showEmpty(this.departmentsContainer, '暂无科室信息');
            }
        } catch (error) {
            console.error('加载科室失败:', error);
            Utils.showEmpty(this.departmentsContainer, '加载科室信息失败');
            
            // 显示默认科室数据
            this.renderDefaultDepartments();
        }
    }

    // 渲染科室
    renderDepartments(departments) {
        const html = departments.map(dept => `
            <div class="col-md-4 col-sm-6 mb-4">
                <div class="department-card" onclick="window.location.href='/departments/${dept.id}'">
                    <div class="department-icon">
                        <i class="bi ${this.getDepartmentIcon(dept.name)}"></i>
                    </div>
                    <h5>${dept.name}</h5>
                    <p class="text-muted">${dept.description || '专业医疗服务'}</p>
                    <div class="mt-3">
                        <span class="badge bg-info">${dept.doctorCount || 0} 位医生</span>
                    </div>
                </div>
            </div>
        `).join('');

        this.departmentsContainer.innerHTML = html;
    }

    // 渲染默认科室数据
    renderDefaultDepartments() {
        const defaultDepartments = [
            { id: 1, name: '内科', description: '消化、呼吸、心血管等内科疾病诊治', doctorCount: 8 },
            { id: 2, name: '外科', description: '普外、骨科、神经外科等手术治疗', doctorCount: 6 },
            { id: 3, name: '妇产科', description: '妇科疾病诊治、产前检查、分娩服务', doctorCount: 5 },
            { id: 4, name: '儿科', description: '儿童常见疾病诊治、儿童保健', doctorCount: 7 },
            { id: 5, name: '眼科', description: '眼部疾病诊治、视力检查、眼部手术', doctorCount: 4 },
            { id: 6, name: '口腔科', description: '口腔疾病诊治、牙齿矫正、口腔修复', doctorCount: 5 }
        ];

        this.renderDepartments(defaultDepartments);
    }

    // 加载医生数据
    async loadDoctors() {
        if (!this.doctorsContainer) return;

        Utils.showLoading(this.doctorsContainer, '加载医生信息...');

        try {
            const doctors = await ApiService.doctors.getAll({ limit: 6 });
            
            if (doctors && doctors.length > 0) {
                this.renderDoctors(doctors);
            } else {
                Utils.showEmpty(this.doctorsContainer, '暂无医生信息');
            }
        } catch (error) {
            console.error('加载医生失败:', error);
            Utils.showEmpty(this.doctorsContainer, '加载医生信息失败');
            
            // 显示默认医生数据
            this.renderDefaultDoctors();
        }
    }

    // 渲染医生
    renderDoctors(doctors) {
        const html = doctors.map(doctor => `
            <div class="col-md-4 col-sm-6 mb-4">
                <div class="doctor-card">
                    <div class="text-center p-3">
                        <img src="${doctor.avatar || '/images/default-doctor.png'}" 
                             alt="${doctor.name}" 
                             class="doctor-avatar">
                    </div>
                    <div class="doctor-info">
                        <h5 class="doctor-name">${doctor.name}</h5>
                        <div class="doctor-title">${doctor.title || '主治医师'}</div>
                        <div class="doctor-department">
                            <i class="bi bi-building me-1"></i>${doctor.departmentName || '内科'}
                        </div>
                        <p class="doctor-bio">${doctor.bio || '经验丰富，专业诊治各种常见疾病'}</p>
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                ${Utils.getStatusBadge(doctor.status || 'AVAILABLE')}
                            </div>
                            <button class="btn btn-primary btn-sm" 
                                    onclick="window.location.href='/doctors/${doctor.id}'">
                                <i class="bi bi-calendar-plus me-1"></i>预约
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `).join('');

        this.doctorsContainer.innerHTML = html;
    }

    // 渲染默认医生数据
    renderDefaultDoctors() {
        const defaultDoctors = [
            {
                id: 1,
                name: '张医生',
                title: '主任医师',
                departmentName: '内科',
                bio: '从事内科临床工作20余年，擅长消化系统疾病诊治',
                status: 'AVAILABLE'
            },
            {
                id: 2,
                name: '李医生',
                title: '副主任医师',
                departmentName: '外科',
                bio: '外科手术经验丰富，专业从事普外科手术15年',
                status: 'AVAILABLE'
            },
            {
                id: 3,
                name: '王医生',
                title: '主治医师',
                departmentName: '妇产科',
                bio: '妇产科临床经验丰富，擅长产前检查和妇科疾病诊治',
                status: 'AVAILABLE'
            },
            {
                id: 4,
                name: '刘医生',
                title: '主任医师',
                departmentName: '儿科',
                bio: '儿科专家，擅长儿童常见疾病诊治和儿童保健',
                status: 'AVAILABLE'
            },
            {
                id: 5,
                name: '陈医生',
                title: '副主任医师',
                departmentName: '眼科',
                bio: '眼科专家，擅长眼部疾病诊治和视力矫正',
                status: 'AVAILABLE'
            },
            {
                id: 6,
                name: '赵医生',
                title: '主治医师',
                departmentName: '口腔科',
                bio: '口腔科专家，擅长口腔疾病诊治和牙齿矫正',
                status: 'AVAILABLE'
            }
        ];

        this.renderDoctors(defaultDoctors);
    }

    // 获取科室图标
    getDepartmentIcon(departmentName) {
        const iconMap = {
            '内科': 'bi-heart-pulse',
            '外科': 'bi-activity',
            '妇产科': 'bi-person-heart',
            '儿科': 'bi-person-arms-up',
            '眼科': 'bi-eye',
            '口腔科': 'bi-emoji-smile',
            '耳鼻喉科': 'bi-ear',
            '皮肤科': 'bi-patch-check',
            '中医科': 'bi-yin-yang',
            '急诊科': 'bi-ambulance',
            '检验科': 'bi-vial',
            '影像科': 'bi-x-ray'
        };

        for (const [key, icon] of Object.entries(iconMap)) {
            if (departmentName.includes(key)) {
                return icon;
            }
        }

        return 'bi-hospital'; // 默认图标
    }
}

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    // 只在首页初始化
    if (window.location.pathname === '/' || window.location.pathname === '/index.html') {
        new HomePage();
    }
});

// 导出到全局
window.HomePage = HomePage;