import axios from 'axios'

const http = axios.create({
  baseURL: '/',
  timeout: 10000,
})

http.interceptors.response.use(
  (resp) => resp,
  (error) => {
    const message = error?.response?.data?.msg || error.message || 'Network Error'
    return Promise.reject(new Error(message))
  }
)

export default http


