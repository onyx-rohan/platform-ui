import axios from 'axios'
import { fetchAuthSession } from 'aws-amplify/auth'

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
})

apiClient.interceptors.request.use(async (config) => {
  const session = await fetchAuthSession()
  const token = session.tokens?.idToken?.toString()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status
    const url = error.config?.url
    console.error(`API error ${status ?? 'NETWORK'} — ${url}`, error)
    return Promise.reject(error)
  }
)

export default apiClient