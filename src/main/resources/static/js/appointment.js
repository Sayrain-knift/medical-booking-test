class AppointmentPage {
    constructor() {
        this.currentStep = 1;
        this.selectedDepartment = null;
        this.selectedDoctor = null;
        this.selectedSchedule = null;
        this.departments = [];
        this.doctors = [];
        this.schedules = [];
        
        this.init();
    }

    async init() {
        // 检查用户登录状态
        if (!auth.isLoggedIn()) {
            Utils.showMessage('请先登录后再进行预约', 'warning');
            setTimeout(() => {
                window.location.href = '/login.html';
            }, 2000);
            return;
        }

        // 加载科室数据
        await this.loadDepartments();
        
        // 绑定事件
        this.bindEvents();
    }

    bindEvents() {
        // 可以在这里添加额外的事件绑定
    }

    async loadDepartments() {
        try {
            this.showLoading(true);
            const response = await ApiService.departments.getAll();
            
            if (response.code === 200 && response.data) {
                this.departments = response.data;
                this.renderDepartments();
            } else {
                throw new Error(response.message || '获取科室列表失败');
            }
        } catch (error) {
            console.error('加载科室失败:', error);
            this.showError('获取科室列表失败，请刷新页面重试');
            // 使用默认数据
            this.loadDefaultDepartments();
        } finally {
            this.showLoading(false);
        }
    }

    loadDefaultDepartments() {
        this.departments = [
            { id: 1, name: '内科', description: '内科疾病诊疗' },
            { id: 2, name: '外科', description: '外科手术治疗' },
            { id: 3, name: '妇产科', description: '妇科产科疾病' },
            { id: 4, name: '儿科', description: '儿童疾病诊疗' },
            { id: 5, name: '眼科', description: '眼科疾病诊疗' },
            { id: 6, name: '口腔科', description: '口腔疾病治疗' }
        ];
        this.renderDepartments();
    }

    renderDepartments() {
        const container = document.getElementById('departmentList');
        container.innerHTML = '';

        this.departments.forEach(dept => {
            const col = document.createElement('div');
            col.className = 'col-md-4 mb-3';
            col.innerHTML = `
                <div class="card department-card h-100" onclick="appointmentPage.selectDepartment(${dept.id})" data-dept-id="${dept.id}">
                    <div class="card-body text-center">
                        <i class="bi bi-hospital text-primary mb-2" style="font-size: 2rem;"></i>
                        <h5 class="card-title">${dept.name}</h5>
                        <p class="card-text text-muted">${dept.description || '专业医疗服务'}</p>
                    </div>
                </div>
            `;
            container.appendChild(col);
        });
    }

    selectDepartment(deptId) {
        // 移除之前的选中状态
        document.querySelectorAll('.department-card').forEach(card => {
            card.classList.remove('selected');
        });

        // 添加选中状态
        const selectedCard = document.querySelector(`[data-dept-id="${deptId}"]`);
        if (selectedCard) {
            selectedCard.classList.add('selected');
        }

        this.selectedDepartment = this.departments.find(dept => dept.id === deptId);
        document.getElementById('step1Next').disabled = false;
    }

    async loadDoctors(departmentId) {
        try {
            this.showLoading(true);
            const response = await ApiService.doctors.getByDepartment(departmentId);
            
            if (response.code === 200 && response.data) {
                this.doctors = response.data;
                this.renderDoctors();
            } else {
                throw new Error(response.message || '获取医生列表失败');
            }
        } catch (error) {
            console.error('加载医生失败:', error);
            this.showError('获取医生列表失败，请重试');
            // 使用默认数据
            this.loadDefaultDoctors();
        } finally {
            this.showLoading(false);
        }
    }

    loadDefaultDoctors() {
        this.doctors = [
            { id: 1, name: '张医生', title: '主任医师', description: '擅长内科常见疾病' },
            { id: 2, name: '李医生', title: '副主任医师', description: '专注心血管疾病' },
            { id: 3, name: '王医生', title: '主治医师', description: '糖尿病专家' },
            { id: 4, name: '赵医生', title: '主任医师', description: '消化系统疾病' }
        ];
        this.renderDoctors();
    }

    renderDoctors() {
        const container = document.getElementById('doctorList');
        container.innerHTML = '';

        this.doctors.forEach(doctor => {
            const col = document.createElement('div');
            col.className = 'col-md-6 mb-3';
            col.innerHTML = `
                <div class="card doctor-card h-100" onclick="appointmentPage.selectDoctor(${doctor.id})" data-doctor-id="${doctor.id}">
                    <div class="card-body">
                        <div class="d-flex align-items-center">
                            <div class="avatar-placeholder me-3">
                                <i class="bi bi-person-circle text-primary" style="font-size: 3rem;"></i>
                            </div>
                            <div>
                                <h5 class="card-title mb-1">${doctor.name}</h5>
                                <p class="text-muted mb-1">${doctor.title || '医生'}</p>
                                <p class="card-text small">${doctor.description || '专业医生'}</p>
                            </div>
                        </div>
                    </div>
                </div>
            `;
            container.appendChild(col);
        });
    }

    selectDoctor(doctorId) {
        // 移除之前的选中状态
        document.querySelectorAll('.doctor-card').forEach(card => {
            card.classList.remove('selected');
        });

        // 添加选中状态
        const selectedCard = document.querySelector(`[data-doctor-id="${doctorId}"]`);
        if (selectedCard) {
            selectedCard.classList.add('selected');
        }

        this.selectedDoctor = this.doctors.find(doctor => doctor.id === doctorId);
        document.getElementById('step2Next').disabled = false;
    }

    async loadSchedules(doctorId) {
        try {
            this.showLoading(true);
            const response = await ApiService.schedules.getByDoctor(doctorId);
            
            if (response.code === 200 && response.data) {
                this.schedules = response.data;
                this.renderSchedules();
            } else {
                throw new Error(response.message || '获取排班列表失败');
            }
        } catch (error) {
            console.error('加载排班失败:', error);
            this.showError('获取排班列表失败，请重试');
            // 使用默认数据
            this.loadDefaultSchedules();
        } finally {
            this.showLoading(false);
        }
    }

    loadDefaultSchedules() {
        const today = new Date();
        const tomorrow = new Date(today);
        tomorrow.setDate(today.getDate() + 1);
        
        this.schedules = [
            { 
                id: 1, 
                date: tomorrow.toISOString().split('T')[0], 
                startTime: '09:00', 
                endTime: '11:00', 
                maxPatients: 20,
                currentAppointments: 5
            },
            { 
                id: 2, 
                date: tomorrow.toISOString().split('T')[0], 
                startTime: '14:00', 
                endTime: '16:00', 
                maxPatients: 20,
                currentAppointments: 8
            },
            { 
                id: 3, 
                date: new Date(tomorrow.getTime() + 24*60*60*1000).toISOString().split('T')[0], 
                startTime: '09:00', 
                endTime: '11:00', 
                maxPatients: 20,
                currentAppointments: 3
            }
        ];
        this.renderSchedules();
    }

    renderSchedules() {
        const container = document.getElementById('scheduleList');
        container.innerHTML = '';

        if (this.schedules.length === 0) {
            container.innerHTML = '<div class="col-12"><div class="alert alert-info">该医生暂无可预约的排班</div></div>';
            return;
        }

        this.schedules.forEach(schedule => {
            const availableSlots = schedule.maxPatients - (schedule.currentAppointments || 0);
            const isAvailable = availableSlots > 0;
            
            const col = document.createElement('div');
            col.className = 'col-md-4 mb-3';
            col.innerHTML = `
                <div class="card schedule-card h-100 ${!isAvailable ? 'disabled' : ''}" 
                     onclick="appointmentPage.selectSchedule(${schedule.id})" 
                     data-schedule-id="${schedule.id}">
                    <div class="card-body text-center">
                        <h6 class="card-title">${schedule.date}</h6>
                        <p class="schedule-time mb-2">
                            <i class="bi bi-clock"></i> ${schedule.startTime} - ${schedule.endTime}
                        </p>
                        <p class="schedule-slots mb-2">
                            <i class="bi bi-people"></i> 可预约: ${availableSlots}/${schedule.maxPatients}
                        </p>
                        ${!isAvailable ? '<span class="badge bg-danger">已约满</span>' : '<span class="badge bg-success">可预约</span>'}
                    </div>
                </div>
            `;
            container.appendChild(col);
        });
    }

    selectSchedule(scheduleId) {
        const schedule = this.schedules.find(s => s.id === scheduleId);
        if (!schedule) return;

        const availableSlots = schedule.maxPatients - (schedule.currentAppointments || 0);
        if (availableSlots <= 0) {
            Utils.showMessage('该时段已约满，请选择其他时间', 'warning');
            return;
        }

        // 移除之前的选中状态
        document.querySelectorAll('.schedule-card').forEach(card => {
            card.classList.remove('selected');
        });

        // 添加选中状态
        const selectedCard = document.querySelector(`[data-schedule-id="${scheduleId}"]`);
        if (selectedCard) {
            selectedCard.classList.add('selected');
        }

        this.selectedSchedule = schedule;
        document.getElementById('step3Next').disabled = false;
    }

    renderAppointmentSummary() {
        const container = document.getElementById('appointmentSummary');
        const availableSlots = this.selectedSchedule.maxPatients - (this.selectedSchedule.currentAppointments || 0);
        
        container.innerHTML = `
            <div class="row">
                <div class="col-md-6">
                    <h6>预约信息</h6>
                    <p><strong>科室：</strong>${this.selectedDepartment.name}</p>
                    <p><strong>医生：</strong>${this.selectedDoctor.name} ${this.selectedDoctor.title || ''}</p>
                    <p><strong>日期：</strong>${this.selectedSchedule.date}</p>
                    <p><strong>时间：</strong>${this.selectedSchedule.startTime} - ${this.selectedSchedule.endTime}</p>
                </div>
                <div class="col-md-6">
                    <h6>注意事项</h6>
                    <ul class="small">
                        <li>请按时就诊，过时可能需要重新排队</li>
                        <li>如需取消预约，请提前2小时操作</li>
                        <li>就诊时请携带有效身份证件</li>
                        <li>如有特殊情况，请及时联系医院</li>
                    </ul>
                </div>
            </div>
        `;
    }

    async confirmAppointment() {
        try {
            this.showLoading(true);
            document.getElementById('confirmBtn').disabled = true;

            const appointmentData = {
                scheduleId: this.selectedSchedule.id,
                patientId: null // 将在服务端自动设置
            };

            const response = await ApiService.appointments.create(appointmentData);
            
            if (response.code === 200 && response.data) {
                // 显示成功提示
                const modal = new bootstrap.Modal(document.getElementById('successModal'));
                document.getElementById('successMessage').textContent = 
                    `您的预约已成功创建，排队号：${response.data.queueNumber || '待确定'}`;
                modal.show();
            } else {
                throw new Error(response.message || '预约失败');
            }
        } catch (error) {
            console.error('预约失败:', error);
            this.showError('预约失败：' + (error.message || '请重试'));
        } finally {
            this.showLoading(false);
            document.getElementById('confirmBtn').disabled = false;
        }
    }

    showLoading(show) {
        const spinner = document.getElementById('loadingSpinner');
        spinner.style.display = show ? 'block' : 'none';
    }

    showError(message) {
        const errorDiv = document.getElementById('errorMessage');
        errorDiv.textContent = message;
        errorDiv.style.display = 'block';
        
        // 3秒后自动隐藏
        setTimeout(() => {
            errorDiv.style.display = 'none';
        }, 3000);
    }

    // 步骤控制方法
    nextStep() {
        if (this.currentStep === 1 && !this.selectedDepartment) {
            Utils.showMessage('请选择科室', 'warning');
            return;
        }
        
        if (this.currentStep === 2 && !this.selectedDoctor) {
            Utils.showMessage('请选择医生', 'warning');
            return;
        }
        
        if (this.currentStep === 3 && !this.selectedSchedule) {
            Utils.showMessage('请选择就诊时间', 'warning');
            return;
        }

        // 隐藏当前步骤
        document.getElementById(`step${this.currentStep}`).classList.remove('active');
        document.getElementById(`step${this.currentStep}Indicator`).classList.remove('active');
        document.getElementById(`step${this.currentStep}Indicator`).classList.add('completed');

        // 加载下一步数据
        if (this.currentStep === 1) {
            this.loadDoctors(this.selectedDepartment.id);
        } else if (this.currentStep === 2) {
            this.loadSchedules(this.selectedDoctor.id);
        } else if (this.currentStep === 3) {
            this.renderAppointmentSummary();
        }

        // 显示下一步骤
        this.currentStep++;
        document.getElementById(`step${this.currentStep}`).classList.add('active');
        document.getElementById(`step${this.currentStep}Indicator`).classList.add('active');
    }

    prevStep() {
        // 隐藏当前步骤
        document.getElementById(`step${this.currentStep}`).classList.remove('active');
        document.getElementById(`step${this.currentStep}Indicator`).classList.remove('active');

        // 显示上一步骤
        this.currentStep--;
        document.getElementById(`step${this.currentStep}`).classList.add('active');
        document.getElementById(`step${this.currentStep}Indicator`).classList.remove('completed');
        document.getElementById(`step${this.currentStep}Indicator`).classList.add('active');
    }

    goToAppointments() {
        window.location.href = '/appointments.html';
    }
}

// 全局函数，供HTML调用
let appointmentPage;

function nextStep() {
    appointmentPage.nextStep();
}

function prevStep() {
    appointmentPage.prevStep();
}

function confirmAppointment() {
    appointmentPage.confirmAppointment();
}

function goToAppointments() {
    appointmentPage.goToAppointments();
}

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    appointmentPage = new AppointmentPage();
});