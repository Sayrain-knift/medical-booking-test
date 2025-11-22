// API 配置和请求封装
class ApiClient {
    constructor() {
        this.baseURL = '/api';
        this.timeout = 10000;
    }

    // 通用请求方法
    async request(endpoint, options = {}) {
        const url = `${this.baseURL}${endpoint}`;
        const config = {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        };

        // 添加认证token
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        try {
            const response = await fetch(url, config);
            
            if (!response.ok) {
                if (response.status === 401) {
                    // Token过期，清除本地存储并跳转到登录页
                    localStorage.removeItem('token');
                    localStorage.removeItem('user');
                    window.location.href = '/login';
                    throw new Error('登录已过期，请重新登录');
                }
                
                // 尝试读取错误信息
                let errorMessage = `HTTP ${response.status}: ${response.statusText}`;
                try {
                    const errorData = await response.json();
                    if (errorData.message) {
                        errorMessage = errorData.message;
                    }
                } catch (e) {
                    // 如果无法解析JSON，使用默认错误信息
                }
                
                throw new Error(errorMessage);
            }

            const data = await response.json();
            return data;
        } catch (error) {
            console.error('API请求失败:', error);
            throw error;
        }
    }

    // GET请求
    async get(endpoint, params = {}) {
        const queryString = new URLSearchParams(params).toString();
        const url = queryString ? `${endpoint}?${queryString}` : endpoint;
        return this.request(url, { method: 'GET' });
    }

    // POST请求
    async post(endpoint, data = {}) {
        return this.request(endpoint, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    }

    // PUT请求
    async put(endpoint, data = {}) {
        return this.request(endpoint, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    }

    // DELETE请求
    async delete(endpoint) {
        return this.request(endpoint, { method: 'DELETE' });
    }
}

// 创建API客户端实例
const api = new ApiClient();

// API接口定义
const ApiService = {
    // 认证相关
    auth: {
        login: (credentials) => api.post('/auth/login', credentials),
        register: (userData) => api.post('/auth/register', userData),
        logout: () => api.post('/auth/logout'),
        refreshToken: () => api.post('/auth/refresh'),
        getCurrentUser: () => api.get('/auth/me')
    },

    // 用户相关
    users: {
        getProfile: () => api.get('/users/profile'),
        updateProfile: (data) => api.put('/users/profile', data),
        changePassword: (data) => api.put('/users/password', data)
    },

    // 科室相关
    departments: {
        getAll: () => api.get('/departments'),
        getById: (id) => api.get(`/departments/${id}`),
        getDoctors: (id) => api.get(`/departments/${id}/doctors`)
    },

    // 医生相关
    doctors: {
        getAll: (params = {}) => api.get('/doctors', params),
        getById: (id) => api.get(`/doctors/${id}`),
        getByDepartment: (departmentId) => api.get(`/doctors/department/${departmentId}`),
        getSchedules: (id) => api.get(`/doctors/${id}/schedules`),
        getAvailableSlots: (id, date) => api.get(`/doctors/${id}/available-slots`, { date })
    },

    // 预约相关
    appointments: {
        create: (data) => api.post('/appointments', data),
        getAll: (params = {}) => api.get('/appointments', params),
        getById: (id) => api.get(`/appointments/${id}`),
        getMyAppointments: (params = {}) => api.get('/appointments/my-appointments', params),
        cancel: (id) => api.put(`/appointments/${id}/cancel`),
        update: (id, data) => api.put(`/appointments/${id}`, data)
    },

    // 排班相关
    schedules: {
        getAll: (params = {}) => api.get('/schedules', params),
        getById: (id) => api.get(`/schedules/${id}`),
        getByDoctor: (doctorId, params = {}) => api.get(`/schedules/doctor/${doctorId}`, params)
    },

    // AI问诊相关
    aiChat: {
        sendMessage: (message) => api.post('/ai/chat', { message }),
        getHistory: () => api.get('/ai/chat/history'),
        clearHistory: () => api.delete('/ai/chat/history')
    }
};

// 工具函数
const Utils = {
    // 显示提示消息
    showMessage: (message, type = 'info') => {
        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${type} alert-dismissible fade show position-fixed`;
        alertDiv.style.cssText = 'top: 20px; left: 50%; transform: translateX(-50%); z-index: 9999; min-width: 300px; max-width: 80%;';
        alertDiv.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        document.body.appendChild(alertDiv);

        // 3秒后自动消失
        setTimeout(() => {
            if (alertDiv.parentNode) {
                alertDiv.parentNode.removeChild(alertDiv);
            }
        }, 3000);
    },

    // 显示提示消息（别名，兼容性）
    showAlert: (message, type = 'info') => {
        Utils.showMessage(message, type);
    },

    // 格式化日期
    formatDate: (dateString) => {
        const date = new Date(dateString);
        return date.toLocaleDateString('zh-CN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit'
        });
    },

    // 格式化时间
    formatTime: (timeString) => {
        const [hours, minutes] = timeString.split(':');
        return `${hours}:${minutes}`;
    },

    // 格式化日期时间
    formatDateTime: (dateTimeString) => {
        const date = new Date(dateTimeString);
        return date.toLocaleString('zh-CN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        });
    },

    // 获取状态标签
    getStatusBadge: (status) => {
        const statusMap = {
            'PENDING': { text: '待确认', class: 'bg-warning' },
            'CONFIRMED': { text: '已确认', class: 'bg-success' },
            'CANCELLED': { text: '已取消', class: 'bg-danger' },
            'COMPLETED': { text: '已完成', class: 'bg-info' }
        };
        const statusInfo = statusMap[status] || { text: status, class: 'bg-secondary' };
        return `<span class="badge ${statusInfo.class}">${statusInfo.text}</span>`;
    },

    // 显示加载状态
    showLoading: (element, text = '加载中...') => {
        element.innerHTML = `
            <div class="text-center py-4">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">${text}</span>
                </div>
                <div class="mt-2">${text}</div>
            </div>
        `;
    },

    // 显示空状态
    showEmpty: (element, message = '暂无数据') => {
        element.innerHTML = `
            <div class="text-center py-4">
                <i class="bi bi-inbox" style="font-size: 3rem; color: #ccc;"></i>
                <div class="mt-2 text-muted">${message}</div>
            </div>
        `;
    },

    // 防抖函数
    debounce: (func, wait) => {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }
};

// 导出到全局
window.api = api;
window.ApiService = ApiService;
window.Utils = Utils;