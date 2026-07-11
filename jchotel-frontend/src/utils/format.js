import dayjs from 'dayjs'

export function formatDate(dateStr) {
  if (!dateStr) return '-'
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm')
}

export function formatDateShort(dateStr) {
  if (!dateStr) return '-'
  return dayjs(dateStr).format('MM-DD HH:mm')
}

export function formatMoney(amount) {
  if (amount === null || amount === undefined) return '0.00'
  return Number(amount).toFixed(2)
}

export function nowString() {
  return dayjs().format('YYYY-MM-DD HH:mm:ss')
}

export function tomorrowString() {
  return dayjs().add(1, 'day').format('YYYY-MM-DD HH:mm:ss')
}
