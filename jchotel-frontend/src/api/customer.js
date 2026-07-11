import request from './request'

export const listCustomers = params => request.get('/customers', { params })
export const getCustomer = id => request.get(`/customers/${id}`)
export const addCustomer = data => request.post('/customers', data)
export const updateCustomer = (id, data) => request.put(`/customers/${id}`, data)
export const deleteCustomer = id => request.delete(`/customers/${id}`)
export const getCustomerStats = id => request.get(`/customers/${id}/stats`)
export const addToBlacklist = (id, reason) => request.post(`/customers/${id}/blacklist`, { reason })
export const removeFromBlacklist = id => request.delete(`/customers/${id}/blacklist`)
export const getBlacklist = () => request.get('/customers/blacklist')
export const getBirthdays = () => request.get('/customers/birthdays')
export const recommendRooms = params => request.get('/customers/recommend-rooms', { params })
