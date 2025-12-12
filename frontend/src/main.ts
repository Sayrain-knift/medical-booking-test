import { createApp } from 'vue'
import { createPinia } from 'pinia'
import './style.css'
// 导入Bootstrap样式
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap-icons/font/bootstrap-icons.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
import App from './App.vue'
import router from './router'

const app = createApp(App)
const pinia = createPinia()

// 使用插件
app.use(pinia)
app.use(router)

// 挂载应用
app.mount('#app')
