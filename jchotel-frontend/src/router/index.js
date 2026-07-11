import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { ElMessage } from 'element-plus'

const MANAGER_ROUTES = ['/users', '/orders/stats', '/rooms/types', '/reports', '/operation-logs']

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue') },
  {
    path: '/',
    component: () => import('@/components/Layout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/Dashboard.vue') },
      { path: 'rooms/board', name: 'RoomBoard', component: () => import('@/views/RoomBoard.vue') },
      { path: 'rooms', name: 'Rooms', component: () => import('@/views/Room.vue') },
      { path: 'rooms/types', name: 'RoomTypes', component: () => import('@/views/RoomType.vue'), meta: { requireManager: true } },
      { path: 'customers', name: 'Customers', component: () => import('@/views/Customer.vue') },
      { path: 'checkin', name: 'Checkin', component: () => import('@/views/Checkin.vue') },
      { path: 'orders', name: 'Orders', component: () => import('@/views/Order.vue') },
      { path: 'orders/stats', name: 'OrderStats', component: () => import('@/views/OrderStats.vue'), meta: { requireManager: true } },
      { path: 'cleaning', name: 'Cleaning', component: () => import('@/views/Cleaning.vue') },
      { path: 'maintenance', name: 'Maintenance', component: () => import('@/views/Maintenance.vue') },
      { path: 'invoices', name: 'Invoices', component: () => import('@/views/Invoice.vue') },
      { path: 'charge-items', name: 'ChargeItems', component: () => import('@/views/ChargeItem.vue'), meta: { requireManager: true } },
      { path: 'reports', name: 'Reports', component: () => import('@/views/Report.vue'), meta: { requireManager: true } },
      { path: 'operation-logs', name: 'OperationLogs', component: () => import('@/views/OperationLog.vue'), meta: { requireManager: true } },
      { path: 'users', name: 'Users', component: () => import('@/views/User.vue'), meta: { requireManager: true } },
      { path: 'profile', name: 'Profile', component: () => import('@/views/Profile.vue') }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  if (to.path === '/login') {
    if (authStore.token) {
      next('/dashboard')
    } else {
      next()
    }
  } else {
    if (!authStore.token) {
      next('/login')
      return
    }
    if (to.meta.requireManager && !authStore.isManager) {
      ElMessage.warning('您没有权限访问该页面')
      next('/dashboard')
      return
    }
    next()
  }
})

export default router
