// 注册页面功能
document.addEventListener('DOMContentLoaded', function() {
    const registerForm = document.getElementById('registerForm');
    
    // 注册表单提交
    registerForm.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        // 获取表单数据
        const formData = {
            username: document.getElementById('username').value.trim(),
            realName: document.getElementById('realName').value.trim(),
            email: document.getElementById('email').value.trim(),
            phone: document.getElementById('phone').value.trim(),
            birthDate: document.getElementById('birthDate').value,
            gender: document.getElementById('gender').value,
            password: document.getElementById('password').value,
            confirmPassword: document.getElementById('confirmPassword').value
        };
        
        // 表单验证
        if (!validateRegisterForm(formData)) {
            return;
        }
        
        // 显示加载状态
        const submitBtn = registerForm.querySelector('button[type="submit"]');
        const originalText = submitBtn.innerHTML;
        submitBtn.innerHTML = '<i class="bi bi-hourglass-split me-2"></i>注册中...';
        submitBtn.disabled = true;
        
        try {
            // 构建符合后端API期望的数据格式
            const registerData = {
                username: formData.username,
                password: formData.password,
                email: formData.email,
                phone: formData.phone,
                role: 'PATIENT' // 默认角色为患者
            };
            
            // 调用注册API
            const success = await authManager.register(registerData);
            
            if (success) {
                // 注册成功，跳转逻辑已在auth.js中处理
                registerForm.reset();
            }
        } catch (error) {
            console.error('注册过程出错:', error);
        } finally {
            // 恢复按钮状态
            submitBtn.innerHTML = originalText;
            submitBtn.disabled = false;
        }
    });
    
    // 实时验证
    const inputs = registerForm.querySelectorAll('input, select');
    inputs.forEach(input => {
        input.addEventListener('blur', function() {
            validateField(this);
        });
        
        input.addEventListener('input', function() {
            // 清除错误提示
            this.classList.remove('is-invalid');
            const feedback = this.parentNode.querySelector('.invalid-feedback');
            if (feedback) {
                feedback.style.display = 'none';
            }
        });
    });
});

// 验证注册表单
function validateRegisterForm(data) {
    let isValid = true;
    
    // 验证用户名
    if (!data.username || data.username.length < 3) {
        showFieldError('username', '用户名至少需要3个字符');
        isValid = false;
    } else if (!/^[a-zA-Z0-9_]+$/.test(data.username)) {
        showFieldError('username', '用户名只能包含字母、数字和下划线');
        isValid = false;
    }
    
    // 验证真实姓名
    if (!data.realName || data.realName.length < 2) {
        showFieldError('realName', '请输入真实姓名');
        isValid = false;
    }
    
    // 验证邮箱
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!data.email || !emailRegex.test(data.email)) {
        showFieldError('email', '请输入有效的邮箱地址');
        isValid = false;
    }
    
    // 验证手机号
    const phoneRegex = /^1[3-9]\d{9}$/;
    if (!data.phone || !phoneRegex.test(data.phone)) {
        showFieldError('phone', '请输入有效的11位手机号码');
        isValid = false;
    }
    
    // 验证出生日期
    if (!data.birthDate) {
        showFieldError('birthDate', '请选择出生日期');
        isValid = false;
    } else {
        const birthDate = new Date(data.birthDate);
        const today = new Date();
        const age = today.getFullYear() - birthDate.getFullYear();
        
        if (age < 0 || age > 120) {
            showFieldError('birthDate', '请输入有效的出生日期');
            isValid = false;
        }
    }
    
    // 验证性别
    if (!data.gender) {
        showFieldError('gender', '请选择性别');
        isValid = false;
    }
    
    // 验证密码
    if (!data.password || data.password.length < 6) {
        showFieldError('password', '密码至少需要6个字符');
        isValid = false;
    }
    
    // 验证确认密码
    if (data.password !== data.confirmPassword) {
        showFieldError('confirmPassword', '两次输入的密码不一致');
        isValid = false;
    }
    
    // 验证服务条款
    const agreeTerms = document.getElementById('agreeTerms');
    if (!agreeTerms.checked) {
        Utils.showAlert('请阅读并同意服务条款和隐私政策', 'warning');
        isValid = false;
    }
    
    return isValid;
}

// 验证单个字段
function validateField(field) {
    const fieldId = field.id;
    const value = field.value.trim();
    
    switch (fieldId) {
        case 'username':
            if (!value || value.length < 3) {
                showFieldError(fieldId, '用户名至少需要3个字符');
                return false;
            }
            if (!/^[a-zA-Z0-9_]+$/.test(value)) {
                showFieldError(fieldId, '用户名只能包含字母、数字和下划线');
                return false;
            }
            break;
            
        case 'realName':
            if (!value || value.length < 2) {
                showFieldError(fieldId, '请输入真实姓名');
                return false;
            }
            break;
            
        case 'email':
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!value || !emailRegex.test(value)) {
                showFieldError(fieldId, '请输入有效的邮箱地址');
                return false;
            }
            break;
            
        case 'phone':
            const phoneRegex = /^1[3-9]\d{9}$/;
            if (!value || !phoneRegex.test(value)) {
                showFieldError(fieldId, '请输入有效的11位手机号码');
                return false;
            }
            break;
            
        case 'birthDate':
            if (!value) {
                showFieldError(fieldId, '请选择出生日期');
                return false;
            }
            break;
            
        case 'gender':
            if (!value) {
                showFieldError(fieldId, '请选择性别');
                return false;
            }
            break;
            
        case 'password':
            if (!value || value.length < 6) {
                showFieldError(fieldId, '密码至少需要6个字符');
                return false;
            }
            break;
            
        case 'confirmPassword':
            const password = document.getElementById('password').value;
            if (value !== password) {
                showFieldError(fieldId, '两次输入的密码不一致');
                return false;
            }
            break;
    }
    
    return true;
}

// 显示字段错误
function showFieldError(fieldId, message) {
    const field = document.getElementById(fieldId);
    field.classList.add('is-invalid');
    
    // 查找或创建错误提示元素
    let feedback = field.parentNode.querySelector('.invalid-feedback');
    if (!feedback) {
        feedback = document.createElement('div');
        feedback.className = 'invalid-feedback';
        field.parentNode.appendChild(feedback);
    }
    
    feedback.textContent = message;
    feedback.style.display = 'block';
}

// 密码强度检测（增强版）
function checkPasswordStrength(password) {
    let strength = 0;
    let feedback = [];
    
    // 长度检测
    if (password.length >= 6) strength++;
    if (password.length >= 8) strength++;
    if (password.length >= 12) strength++;
    
    // 字符类型检测
    if (/[a-z]/.test(password)) {
        strength++;
        feedback.push('包含小写字母');
    }
    if (/[A-Z]/.test(password)) {
        strength++;
        feedback.push('包含大写字母');
    }
    if (/\d/.test(password)) {
        strength++;
        feedback.push('包含数字');
    }
    if (/[!@#$%^&*(),.?":{}|<>]/.test(password)) {
        strength++;
        feedback.push('包含特殊字符');
    }
    
    return { strength, feedback };
}

// 检查用户名是否可用（异步）
async function checkUsernameAvailable(username) {
    try {
        const response = await ApiService.user.checkUsername(username);
        return response.available;
    } catch (error) {
        console.error('检查用户名可用性失败:', error);
        return true; // 出错时默认可用
    }
}

// 检查邮箱是否已注册（异步）
async function checkEmailAvailable(email) {
    try {
        const response = await ApiService.user.checkEmail(email);
        return response.available;
    } catch (error) {
        console.error('检查邮箱可用性失败:', error);
        return true; // 出错时默认可用
    }
}