import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/HomeView.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/RegisterView.vue')
  },
  {
    path: '/doctors',
    name: 'Doctors',
    component: () => import('../views/DoctorsView.vue')
  },
  {
    path: '/appointment',
    name: 'Appointment',
    component: () => import('../views/AppointmentView.vue')
  },
  {
    path: '/ai-consultation',
    name: 'AiConsultation',
    component: () => import('../views/AiConsultationView.vue')
  },
  // 暂时注释，等待实现对应的组件
  // {
  //   path: '/profile',
  //   name: 'Profile',
  //   component: () => import('../views/ProfileView.vue')
  // },
  // {
  //   path: '/appointments',
  //   name: 'Appointments',
  //   component: () => import('../views/AppointmentsView.vue')
  // }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

export default router
