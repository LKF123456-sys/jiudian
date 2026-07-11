import request from './request'

export const getDashboardReminders = () => request.get('/reminders/dashboard')
export const getBirthdayCustomers = () => request.get('/reminders/birthdays')
export const getTodayArrivals = () => request.get('/reminders/today-arrivals')
export const getTodayDepartures = () => request.get('/reminders/today-departures')
export const getOverdueCheckouts = () => request.get('/reminders/overdue-checkouts')
export const getLowDepositOrders = () => request.get('/reminders/low-deposit')
