import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = 'Bearer ' + token
    }
    return config
  },
  error => Promise.reject(error)
)

request.interceptors.response.use(
  response => {
    const data = response.data
    if (data.code !== 200) {
      ElMessage.error(data.message || '操作失败')
      if (data.code === 401) {
        localStorage.removeItem('token')
        ElMessageBox.alert('登录已过期，请重新登录', '提示', {
          confirmButtonText: '确定',
          type: 'warning',
          callback: () => {
            window.location.href = '/login'
          }
        })
      }
      return Promise.reject(new Error(data.message))
    }
    return data
  },
  error => {
    const status = error.response?.status
    if (status === 401) {
      localStorage.removeItem('token')
      ElMessageBox.alert('登录已过期，请重新登录', '提示', {
        confirmButtonText: '确定',
        type: 'warning',
        callback: () => {
          window.location.href = '/login'
        }
      })
    } else {
      ElMessage.error(error.response?.data?.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request