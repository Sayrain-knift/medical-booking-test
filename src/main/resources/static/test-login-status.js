// 登录状态测试脚本
console.log('开始测试登录状态切换功能...');

// 1. 检查AuthManager是否正确初始化
if (typeof authManager !== 'undefined') {
    console.log('✓ AuthManager 已正确初始化');
} else {
    console.error('✗ AuthManager 未找到');
    return;
}

// 2. 检查当前登录状态
console.log('当前登录状态:', authManager.isLoggedIn());
console.log('当前用户:', authManager.getCurrentUser());

// 3. 检查DOM元素
const authSection = document.getElementById('authSection');
const userSection = document.getElementById('userSection');
const usernameSpan = document.getElementById('username');

console.log('authSection 元素:', authSection);
console.log('userSection 元素:', userSection);
console.log('usernameSpan 元素:', usernameSpan);

// 4. 检查元素显示状态
if (authSection && userSection) {
    console.log('authSection 可见:', !authSection.classList.contains('d-none'));
    console.log('userSection 可见:', !userSection.classList.contains('d-none'));
} else {
    console.error('✗ 找不到必要的DOM元素');
}

// 5. 测试UI更新功能
console.log('测试UI更新...');
authManager.updateUI();

setTimeout(() => {
    if (authSection && userSection) {
        console.log('更新后 authSection 可见:', !authSection.classList.contains('d-none'));
        console.log('更新后 userSection 可见:', !userSection.classList.contains('d-none'));
    }
}, 100);

// 6. 模拟登录测试
console.log('模拟登录测试...');
const mockUser = {
    username: 'testuser',
    role: 'PATIENT'
};

localStorage.setItem('token', 'mock.jwt.token');
localStorage.setItem('user', JSON.stringify(mockUser));
authManager.currentUser = mockUser;

setTimeout(() => {
    authManager.updateUI();
    console.log('登录后 authSection 可见:', !authSection.classList.contains('d-none'));
    console.log('登录后 userSection 可见:', !userSection.classList.contains('d-none'));
    console.log('用户名显示:', usernameSpan ? usernameSpan.textContent : '未找到元素');
    
    // 7. 模拟退出登录测试
    console.log('模拟退出登录测试...');
    setTimeout(() => {
        authManager.logout();
        setTimeout(() => {
            console.log('退出后 authSection 可见:', !authSection.classList.contains('d-none'));
            console.log('退出后 userSection 可见:', !userSection.classList.contains('d-none'));
            console.log('测试完成！');
        }, 100);
    }, 1000);
}, 100);