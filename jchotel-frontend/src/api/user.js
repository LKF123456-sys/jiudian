import request from './request'

export const listUsers = params => request.get('/user/list', { params })
export const getUser = id => request.get(`/user/${id}`)
export const addUser = data => request.post('/user', data)
export const updateUser = (id, data) => request.put(`/user/${id}`, data)
export const deleteUser = id => request.delete(`/user/${id}`)
export const toggleUserStatus = (id, status) => request.put(`/user/${id}/status/${status}`)
export const resetUserPassword = id => request.put(`/user/${id}/reset-password`)
