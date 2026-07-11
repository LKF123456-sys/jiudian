import request from './request'

export const listCleaningTasks = params => request.get('/cleaning-tasks', { params })
export const getCleaningTask = id => request.get(`/cleaning-tasks/${id}`)
export const getPendingCleaningTasks = () => request.get('/cleaning-tasks/pending')
export const assignCleaningTask = (id, data) => request.post(`/cleaning-tasks/${id}/assign`, data)
export const startCleaningTask = id => request.post(`/cleaning-tasks/${id}/start`)
export const finishCleaningTask = id => request.post(`/cleaning-tasks/${id}/finish`)
export const inspectCleaningTask = id => request.post(`/cleaning-tasks/${id}/inspect`)
export const cancelCleaningTask = id => request.post(`/cleaning-tasks/${id}/cancel`)
