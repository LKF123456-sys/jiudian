export const ROOM_STATUS_MAP = {
  idle: { label: '空闲', type: 'success', color: '#67C23A' },
  occupied: { label: '入住中', type: 'warning', color: '#E6A23C' },
  maintenance: { label: '维修中', type: 'info', color: '#909399' },
  cleaning: { label: '清扫中', type: 'primary', color: '#409EFF' },
  dirty: { label: '待清扫', type: 'danger', color: '#F56C6C' }
}

export const ORDER_STATUS_MAP = {
  pending: { label: '待入住', type: 'info' },
  checkedIn: { label: '已入住', type: 'warning' },
  checkedOut: { label: '已退房', type: 'success' },
  cancelled: { label: '已取消', type: 'danger' }
}

export const VIP_LEVEL_MAP = {
  0: '普通',
  1: '银卡',
  2: '金卡',
  3: '钻石'
}

export const CLEANING_STATUS_MAP = {
  pending: { label: '待分配', type: 'info' },
  assigned: { label: '已分配', type: 'warning' },
  cleaning: { label: '清扫中', type: 'primary' },
  inspecting: { label: '待检查', type: 'warning' },
  completed: { label: '已完成', type: 'success' },
  cancelled: { label: '已取消', type: 'danger' }
}

export const MAINTENANCE_STATUS_MAP = {
  pending: { label: '待处理', type: 'info' },
  assigned: { label: '已分配', type: 'warning' },
  processing: { label: '维修中', type: 'primary' },
  completed: { label: '已完成', type: 'success' },
  verified: { label: '已验收', type: 'success' },
  cancelled: { label: '已取消', type: 'danger' }
}

export const INVOICE_STATUS_MAP = {
  issued: { label: '已开具', type: 'success' },
  cancelled: { label: '已作废', type: 'danger' },
  red: { label: '已红冲', type: 'warning' }
}

export const USER_ROLE_MAP = {
  admin: { label: '管理员', type: 'danger' },
  manager: { label: '经理', type: 'warning' },
  receptionist: { label: '前台', type: 'success' },
  housekeeping: { label: '客房', type: 'info' },
  engineering: { label: '工程', type: 'info' }
}
