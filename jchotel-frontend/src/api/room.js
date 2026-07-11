import request from './request'

export const listRooms = params => request.get('/rooms', { params })
export const getRoom = id => request.get(`/rooms/${id}`)
export const addRoom = data => request.post('/rooms', data)
export const updateRoom = (id, data) => request.put(`/rooms/${id}`, data)
export const deleteRoom = id => request.delete(`/rooms/${id}`)
export const updateRoomStatus = (id, status) => request.put(`/rooms/${id}/status`, { status })
export const getRoomBoard = () => request.get('/rooms/board')
export const getRoomStatusStats = () => request.get('/rooms/status-stats')
export const getAvailableRooms = params => request.get('/rooms/available', { params })

export const listRoomTypes = () => request.get('/room-types')
export const addRoomType = data => request.post('/room-types', data)
export const updateRoomType = (id, data) => request.put(`/room-types/${id}`, data)
export const deleteRoomType = id => request.delete(`/room-types/${id}`)
