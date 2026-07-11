import request from './request'

export const listMaintenanceOrders = params => request.get('/maintenance', { params })
export const getMaintenanceOrder = id => request.get(`/maintenance/${id}`)
export const createMaintenanceOrder = data => request.post('/maintenance', data)
export const assignMaintenanceOrder = (id, data) => request.post(`/maintenance/${id}/assign`, data)
export const startMaintenanceOrder = id => request.post(`/maintenance/${id}/start`)
export const finishMaintenanceOrder = (id, data) => request.post(`/maintenance/${id}/finish`, data)
export const verifyMaintenanceOrder = id => request.post(`/maintenance/${id}/verify`)
export const cancelMaintenanceOrder = id => request.post(`/maintenance/${id}/cancel`)
