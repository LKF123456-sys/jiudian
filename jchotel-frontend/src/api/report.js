import request from './request'

export const getShiftReport = () => request.get('/reports/shift')
export const getOccupancyReport = params => request.get('/reports/occupancy', { params })
export const getDailyShiftReport = params => request.get('/reports/daily-shift', { params })
export const getPaymentStats = params => request.get('/reports/payment-stats', { params })
export const getRoomTypeRevenue = params => request.get('/reports/room-type-revenue', { params })
