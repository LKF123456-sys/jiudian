import request from './request'

export const listInvoices = params => request.get('/invoices', { params })
export const getInvoice = id => request.get(`/invoices/${id}`)
export const getInvoicesByOrder = orderId => request.get(`/invoices/order/${orderId}`)
export const createInvoice = data => request.post('/invoices', data)
export const cancelInvoice = (id, reason) => request.post(`/invoices/${id}/cancel`, { reason })
export const redInvoice = (id, reason) => request.post(`/invoices/${id}/red`, { reason })
