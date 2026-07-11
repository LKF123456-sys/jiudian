import request from './request'

export const login = data => request.post('/auth/login', data)
export const logout = () => request.post('/auth/logout')
export const changePassword = data => request.post('/auth/password', data)
export const getUserInfo = () => request.get('/user/info')
