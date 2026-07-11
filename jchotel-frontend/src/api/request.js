import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/auth'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

let isRedirecting = false

request.interceptors.request.use(config => {
  const authStore = useAuthStore()
  if (authStore.token) {
    config.headers.Authorization = 'Bearer ' + authStore.token
  }
  return config
}, error => Promise.reject(error))

request.interceptors.response.use(res => {
  const newToken = res.headers['x-new-token']
  if (newToken) {
    const authStore = useAuthStore()
    authStore.setToken(newToken)
  }

  if (res.data.code === 0) {
    return res.data.data
  } else {
    ElMessage.error(res.data.message || '操作失败')
    return Promise.reject(new Error(res.data.message || '操作失败'))
  }
}, error => {
  if (error.response) {
    const status = error.response.status
    const data = error.response.data
    if (status === 401) {
      if (!isRedirecting) {
        isRedirecting = true
        const authStore = useAuthStore()
        authStore.clearAuth()
        ElMessage.error(data?.message || '登录已过期，请重新登录')
        router.replace('/login').finally(() => {
          isRedirecting = false
        })
      }
    } else if (status === 403) {
      ElMessage.error(data?.message || '权限不足')
    } else if (status >= 500) {
      ElMessage.error('服务器异常，请稍后重试')
    } else {
      ElMessage.error(data?.message || '请求失败')
    }
  } else if (error.code === 'ECONNABORTED') {
    ElMessage.error('请求超时，请检查网络')
  } else if (!error.message || !error.message.includes('canceled')) {
    ElMessage.error('网络异常，请检查连接')
  }
  return Promise.reject(error)
})

export default request
