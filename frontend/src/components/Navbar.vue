<template>
  <nav class="navbar navbar-expand-lg navbar-light bg-white shadow-sm fixed-top">
    <div class="container">
      <router-link to="/" class="navbar-brand fw-bold">
        <i class="bi bi-hospital text-primary me-2"></i>医疗预约系统
      </router-link>
      
      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
        <span class="navbar-toggler-icon"></span>
      </button>
      
      <div class="collapse navbar-collapse" id="navbarNav">
        <ul class="navbar-nav mx-auto">
          <li class="nav-item">
            <router-link to="/" class="nav-link" :class="{ active: $route.path === '/' }">首页</router-link>
          </li>
          <li class="nav-item">
            <router-link to="/doctors" class="nav-link" :class="{ active: $route.path === '/doctors' }">医生科室</router-link>
          </li>
          <li class="nav-item">
            <router-link to="/appointment" class="nav-link" :class="{ active: $route.path === '/appointment' }">预约挂号</router-link>
          </li>
          <li class="nav-item">
            <router-link to="/ai-consultation" class="nav-link" :class="{ active: $route.path === '/ai-consultation' }">AI问诊</router-link>
          </li>
          <li class="nav-item">
            <router-link to="/profile" class="nav-link" :class="{ active: $route.path === '/profile' }">个人中心</router-link>
          </li>
        </ul>
        
        <div class="d-flex align-items-center">
          <div v-if="isAuthenticated" id="userSection">
            <div class="dropdown">
              <button class="btn btn-outline-primary dropdown-toggle" type="button" data-bs-toggle="dropdown">
                <i class="bi bi-person-circle me-1"></i>
                <span>{{ user?.username || '用户' }}</span>
              </button>
              <ul class="dropdown-menu">
                <li><router-link to="/profile" class="dropdown-item">个人中心</router-link></li>
                <li><router-link to="/appointments" class="dropdown-item">我的预约</router-link></li>
                <li><hr class="dropdown-divider"></li>
                <li><a href="#" class="dropdown-item" @click="logout">退出登录</a></li>
              </ul>
            </div>
          </div>
          <div v-else id="authSection">
            <router-link to="/login" class="btn btn-outline-primary me-2">登录</router-link>
            <router-link to="/register" class="btn btn-primary">注册</router-link>
          </div>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// 计算属性
const isAuthenticated = computed(() => authStore.isAuthenticated)
const user = computed(() => authStore.currentUser)

// 退出登录
const logout = () => {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
/* 导航栏响应式优化 */
@media (max-width: 991.98px) {
  .navbar-nav {
    text-align: center;
  }
  
  .nav-item {
    margin: 0.5rem 0;
  }
}
</style>
