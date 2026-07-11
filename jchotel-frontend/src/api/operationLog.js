import request from './request'

export const listOperationLogs = params => request.get('/operation-logs', { params })
