<template>
  <div>
    <!-- 导航栏 -->
    <Navbar />
    
    <!-- 主要内容 -->
    <main style="padding-top: 80px;">
      <!-- 页面标题 -->
      <section class="py-5 bg-primary text-white">
        <div class="container">
          <h1 class="display-5 fw-bold">AI智能问诊</h1>
          <p class="lead">智能医疗咨询，随时为您服务</p>
        </div>
      </section>
      
      <!-- AI问诊界面 -->
      <section class="py-5">
        <div class="container">
          <div class="card shadow-lg">
            <div class="card-header bg-light">
              <div class="d-flex justify-content-between align-items-center">
                <h5 class="mb-0">
                  <i class="bi bi-robot me-2 text-primary"></i>
                  AI医疗助手
                </h5>
                <button class="btn btn-sm btn-outline-danger" @click="clearChatHistory">
                  <i class="bi bi-trash me-1"></i>清除历史
                </button>
              </div>
            </div>
            <div class="card-body">
              <!-- 聊天历史 -->
              <div class="chat-container mb-4">
                <div 
                  v-for="message in chatMessages" 
                  :key="message.id" 
                  :class="['chat-message', message.isUser ? 'chat-user' : 'chat-ai']"
                >
                  <div class="chat-avatar">
                    <i v-if="message.isUser" class="bi bi-person-circle text-primary"></i>
                    <i v-else class="bi bi-robot text-danger"></i>
                  </div>
                  <div class="chat-content">
                    <div class="chat-text">{{ message.content }}</div>
                    <div class="chat-time">{{ formatTime(message.timestamp) }}</div>
                  </div>
                </div>
                <div v-if="loading" class="chat-message chat-ai">
                  <div class="chat-avatar">
                    <i class="bi bi-robot text-danger"></i>
                  </div>
                  <div class="chat-content">
                    <div class="chat-text">
                      <div class="typing-indicator">
                        <span></span>
                        <span></span>
                        <span></span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 输入区域 -->
              <div class="input-area">
                <div class="input-group">
                  <textarea 
                    class="form-control" 
                    v-model="inputMessage"
                    placeholder="请输入您的症状或问题..."
                    rows="3"
                    @keyup.enter.exact="sendMessage"
                    @keyup.enter.shift="addNewline"
                    :disabled="loading"
                  ></textarea>
                  <button 
                    class="btn btn-primary"
                    @click="sendMessage"
                    :disabled="!inputMessage.trim() || loading"
                  >
                    <i class="bi bi-send"></i>
                  </button>
                </div>
                <div class="text-muted small mt-2">
                  提示：您可以咨询症状、疾病预防、健康养生等问题
                </div>
              </div>
            </div>
          </div>
          
          <!-- 常见问题 -->
          <div class="mt-4">
            <h6>常见问题</h6>
            <div class="row mt-3">
              <div class="col-md-4 mb-2" v-for="(faq, index) in commonFaqs" :key="index">
                <button 
                  class="btn btn-outline-primary btn-sm me-2 mb-2"
                  @click="selectFaq(faq)"
                >
                  {{ faq }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>
    
    <!-- 页脚 -->
    <footer class="bg-dark text-light py-4">
      <div class="container">
        <div class="row">
          <div class="col-md-6">
            <h5>医疗预约系统</h5>
            <p>为您提供专业、便捷的医疗服务</p>
          </div>
          <div class="col-md-6 text-md-end">
            <p>&copy; 2024 医疗预约系统. 保留所有权利.</p>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import Navbar from '../components/Navbar.vue'
import { aiApi } from '../services/api'

// 状态
const chatMessages = ref<Array<{
  id: string
  content: string
  isUser: boolean
  timestamp: Date
}>>([])
const inputMessage = ref('')
const loading = ref(false)
const chatId = ref<string | null>(null)

// 常见问题
const commonFaqs = [
  '感冒了怎么办？',
  '如何预防高血压？',
  '糖尿病患者的饮食注意事项',
  '头痛的原因有哪些？',
  '如何改善睡眠质量？',
  '新冠疫苗接种注意事项',
  '如何预防颈椎病？',
  '肥胖的危害有哪些？',
  '如何正确洗手？',
  '儿童发烧怎么办？'
]

// 格式化时间
const formatTime = (timestamp: Date) => {
  return timestamp.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || loading.value) return
  
  const messageContent = inputMessage.value.trim()
  inputMessage.value = ''
  
  // 添加用户消息
  const userMessage = {
    id: `user-${Date.now()}`,
    content: messageContent,
    isUser: true,
    timestamp: new Date()
  }
  chatMessages.value.push(userMessage)
  
  // 滚动到底部
  await nextTick()
  scrollToBottom()
  
  // 发送请求
  loading.value = true
  try {
    const response = await aiApi.sendMessage(messageContent, chatId.value || undefined)
    chatId.value = response.chatId
    
    // 添加AI回复
    const aiMessage = {
      id: `ai-${Date.now()}`,
      content: response.response,
      isUser: false,
      timestamp: new Date(response.timestamp)
    }
    chatMessages.value.push(aiMessage)
    
    // 滚动到底部
    await nextTick()
    scrollToBottom()
  } catch (error) {
    console.error('发送消息失败:', error)
    
    // 添加错误消息
    const errorMessage = {
      id: `error-${Date.now()}`,
      content: '抱歉，服务器暂时无法响应，请稍后重试。',
      isUser: false,
      timestamp: new Date()
    }
    chatMessages.value.push(errorMessage)
    
    // 滚动到底部
    await nextTick()
    scrollToBottom()
  } finally {
    loading.value = false
  }
}

// 添加换行
const addNewline = () => {
  inputMessage.value += '\n'
}

// 清除聊天历史
const clearChatHistory = async () => {
  if (chatMessages.value.length === 0) return
  
  if (confirm('确定要清除聊天历史吗？')) {
    try {
      await aiApi.clearHistory(chatId.value || undefined)
      chatMessages.value = []
      chatId.value = null
    } catch (error) {
      console.error('清除历史记录失败:', error)
    }
  }
}

// 选择常见问题
const selectFaq = (faq: string) => {
  inputMessage.value = faq
}

// 滚动到底部
const scrollToBottom = () => {
  const chatContainer = document.querySelector('.chat-container')
  if (chatContainer) {
    chatContainer.scrollTop = chatContainer.scrollHeight
  }
}

// 加载聊天历史
const loadChatHistory = async () => {
  try {
    const history = await aiApi.getHistory(chatId.value || undefined)
    chatId.value = history.chatId
    // 将历史记录转换为聊天消息格式
    chatMessages.value = history.messages.map((item, index) => ({
      id: `history-${index}`,
      content: item.content,
      isUser: item.role === 'user',
      timestamp: new Date(item.timestamp || Date.now())
    }))
    
    // 滚动到底部
    await nextTick()
    scrollToBottom()
  } catch (error) {
    console.error('加载聊天历史失败:', error)
  }
}

// 组件挂载时加载聊天历史
onMounted(() => {
  loadChatHistory()
})
</script>

<style scoped>
/* 聊天容器样式 */
.chat-container {
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 1rem;
  background-color: #f8f9fa;
  scroll-behavior: smooth;
}

/* 聊天消息样式 */
.chat-message {
  display: flex;
  margin-bottom: 1rem;
  animation: fadeIn 0.3s ease;
}

.chat-message.chat-user {
  justify-content: flex-end;
  flex-direction: row-reverse;
}

.chat-avatar {
  font-size: 1.5rem;
  margin: 0 0.5rem;
  flex-shrink: 0;
}

.chat-content {
  max-width: 70%;
  padding: 0.75rem;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  position: relative;
}

.chat-user .chat-content {
  background-color: #007bff;
  color: white;
  border-bottom-right-radius: 4px;
}

.chat-ai .chat-content {
  background-color: white;
  color: #333;
  border-bottom-left-radius: 4px;
}

.chat-text {
  margin-bottom: 0.25rem;
  line-height: 1.5;
}

.chat-time {
  font-size: 0.75rem;
  opacity: 0.7;
  text-align: right;
}

.chat-ai .chat-time {
  color: #6c757d;
}

/* 输入区域样式 */
.input-area {
  background-color: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 1rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.input-group {
  display: flex;
  gap: 0.5rem;
  align-items: flex-end;
}

.input-group textarea {
  flex: 1;
  resize: vertical;
  min-height: 80px;
  max-height: 150px;
  border-radius: 8px;
}

.input-group button {
  height: fit-content;
  padding: 0.5rem 1rem;
  border-radius: 8px;
}

/* 打字指示器 */
.typing-indicator {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.5rem;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background-color: #6c757d;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.5;
  }
  30% {
    transform: translateY(-10px);
    opacity: 1;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-content {
    max-width: 85%;
  }
}
</style>
