import request from './request'

export const listChargeItems = params => request.get('/charge-items', { params })
export const listAllEnabledChargeItems = () => request.get('/charge-items/all-enabled')
export const getChargeItem = id => request.get(`/charge-items/${id}`)
export const addChargeItem = data => request.post('/charge-items', data)
export const updateChargeItem = (id, data) => request.put(`/charge-items/${id}`, data)
export const deleteChargeItem = id => request.delete(`/charge-items/${id}`)
