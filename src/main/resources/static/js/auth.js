// 认证相关功能
class AuthManager {
    constructor() {
        this.currentUser = null;
        this.init();
    }

    // 初始化
    init() {
        // 检查本地存储的用户信息
        const token = localStorage.getItem('token');
        const user = localStorage.getItem('user');
        
        if (token && user) {
            try {
                this.currentUser = JSON.parse(user);
                // 延迟更新UI，确保DOM已加载
                setTimeout(() => this.updateUI(), 0);
            } catch (error) {
                console.error('解析用户信息失败:', error);
                this.logout();
            }
        }
    }

    // 注册
    async register(userData) {
        try {
            const response = await ApiService.auth.register(userData);
            
            if (response.message) {
                Utils.showMessage('注册成功！请登录', 'success');
                
                // 跳转到登录页
                setTimeout(() => {
                    window.location.href = '/login.html';
                }, 1500);
                
                return true;
            } else {
                throw new Error('注册响应数据异常');
            }
        } catch (error) {
            console.error('注册失败:', error);
            Utils.showMessage(error.message || '注册失败，请稍后重试', 'danger');
            return false;
        }
    }

    // 登录
    async login(credentials) {
        try {
            const response = await ApiService.auth.login(credentials);
            
            if (response.token && response.username) {
                // 构建用户对象
                const user = {
                    username: response.username,
                    role: response.role
                };
                
                // 保存token和用户信息
                localStorage.setItem('token', response.token);
                localStorage.setItem('user', JSON.stringify(user));
                
                this.currentUser = user;
                this.updateUI();
                
                Utils.showMessage('登录成功！', 'success');
                
                // 跳转到首页或个人中心
                setTimeout(() => {
                    window.location.href = '/';
                }, 1000);
                
                return true;
            } else {
                throw new Error('登录响应数据异常');
            }
        } catch (error) {
            console.error('登录失败:', error);
            Utils.showMessage(error.message || '登录失败，请检查用户名和密码', 'danger');
            return false;
        }
    }

    // 注册
    async register(userData) {
        try {
            const response = await ApiService.auth.register(userData);
            
            if (response.message) {
                Utils.showMessage('注册成功！请登录', 'success');
                
                // 跳转到登录页
                setTimeout(() => {
                    window.location.href = '/login';
                }, 1500);
                
                return true;
            } else {
                throw new Error('注册响应数据异常');
            }
        } catch (error) {
            console.error('注册失败:', error);
            Utils.showMessage(error.message || '注册失败，请稍后重试', 'danger');
            return false;
        }
    }

    // 退出登录
    async logout() {
        try {
            // 调用后端退出登录接口
            await ApiService.auth.logout();
        } catch (error) {
            console.error('退出登录请求失败:', error);
        } finally {
            // 清除本地存储
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            
            this.currentUser = null;
            this.updateUI();
            
            Utils.showMessage('已退出登录', 'info');
            
            // 跳转到首页
            setTimeout(() => {
                window.location.href = '/';
            }, 1000);
        }
    }

    // 检查是否已登录
    isLoggedIn() {
        return !!this.currentUser && !!localStorage.getItem('token');
    }

    // 获取当前用户
    getCurrentUser() {
        return this.currentUser;
    }

    // 检查用户角色
    hasRole(role) {
        return this.currentUser && this.currentUser.roles && 
               this.currentUser.roles.includes(role);
    }

    // 更新UI显示
    updateUI() {
        const authSection = document.getElementById('authSection');
        const userSection = document.getElementById('userSection');
        const usernameSpan = document.getElementById('username');

        if (this.isLoggedIn()) {
            // 已登录状态
            if (authSection) authSection.classList.add('d-none');
            if (userSection) {
                userSection.classList.remove('d-none');
                if (usernameSpan) {
                    usernameSpan.textContent = this.currentUser.username || this.currentUser.name || '用户';
                }
            }
        } else {
            // 未登录状态
            if (authSection) authSection.classList.remove('d-none');
            if (userSection) userSection.classList.add('d-none');
        }
    }

    // 更新认证UI（别名方法，保持兼容性）
    updateAuthUI() {
        this.updateUI();
    }

    // 刷新token
    async refreshToken() {
        try {
            const response = await ApiService.auth.refreshToken();
            if (response.token) {
                localStorage.setItem('token', response.token);
                return true;
            }
        } catch (error) {
            console.error('刷新token失败:', error);
            this.logout();
            return false;
        }
    }
}

// 创建认证管理器实例
const authManager = new AuthManager();

// 为了兼容性，创建全局auth变量
const auth = authManager;

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    // 确保UI状态正确
    authManager.updateUI();
    
    // 如果是登录页面，初始化登录表单
    if (window.location.pathname === '/login' || document.getElementById('loginForm')) {
        initLoginForm();
    }
    
    // 如果是注册页面，初始化注册表单
    if (window.location.pathname === '/register' || document.getElementById('registerForm')) {
        initRegisterForm();
    }
});

// 初始化登录表单
function initLoginForm() {
    const loginForm = document.getElementById('loginForm');
    if (!loginForm) return;

    loginForm.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;
        const submitBtn = loginForm.querySelector('button[type="submit"]');
        
        if (!username || !password) {
            Utils.showMessage('请填写用户名和密码', 'warning');
            return;
        }

        // 显示加载状态
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>登录中...';

        try {
            await authManager.login({ username, password });
        } finally {
            // 恢复按钮状态
            submitBtn.disabled = false;
            submitBtn.innerHTML = '登录';
        }
    });
}

// 初始化注册表单
function initRegisterForm() {
    const registerForm = document.getElementById('registerForm');
    if (!registerForm) return;

    registerForm.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;
        const confirmPassword = document.getElementById('confirmPassword').value;
        const email = document.getElementById('email').value.trim();
        const phone = document.getElementById('phone').value.trim();
        const submitBtn = registerForm.querySelector('button[type="submit"]');
        
        // 表单验证
        if (!username || !password || !confirmPassword || !email) {
            Utils.showMessage('请填写所有必填字段', 'warning');
            return;
        }

        if (password !== confirmPassword) {
            Utils.showMessage('两次输入的密码不一致', 'warning');
            return;
        }

        if (password.length < 6) {
            Utils.showMessage('密码长度至少6位', 'warning');
            return;
        }

        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            Utils.showMessage('请输入有效的邮箱地址', 'warning');
            return;
        }

        // 显示加载状态
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>注册中...';

        try {
            await authManager.register({
                username,
                password,
                email,
                phone,
                role: 'PATIENT' // 默认角色为患者
            });
        } finally {
            // 恢复按钮状态
            submitBtn.disabled = false;
            submitBtn.innerHTML = '注册';
        }
    });
}

// 全局退出登录函数
function logout() {
    if (confirm('确定要退出登录吗？')) {
        authManager.logout();
    }
}

// 导出到全局
window.authManager = authManager;
window.logout = logout;