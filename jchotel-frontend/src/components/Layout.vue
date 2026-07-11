<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="sidebar">
      <div class="logo">锦程酒店</div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#1e3a5f"
        text-color="#fff"
        active-text-color="#d4af37"
        class="side-menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataLine /></el-icon>
          <span>首页看板</span>
        </el-menu-item>
        <el-sub-menu index="room-group">
          <template #title>
            <el-icon><OfficeBuilding /></el-icon>
            <span>客房管理</span>
          </template>
          <el-menu-item index="/rooms/board">房态看板</el-menu-item>
          <el-menu-item index="/rooms">客房列表</el-menu-item>
          <el-menu-item v-if="authStore.isManager" index="/rooms/types">房型管理</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/customers">
          <el-icon><User /></el-icon>
          <span>客户管理</span>
        </el-menu-item>
        <el-menu-item index="/checkin">
          <el-icon><Calendar /></el-icon>
          <span>入住退房</span>
        </el-menu-item>
        <el-sub-menu index="order-group">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>订单管理</span>
          </template>
          <el-menu-item index="/orders">订单列表</el-menu-item>
          <el-menu-item v-if="authStore.isManager" index="/orders/stats">营收统计</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="service-group">
          <template #title>
            <el-icon><Service /></el-icon>
            <span>客房服务</span>
          </template>
          <el-menu-item index="/cleaning">清扫管理</el-menu-item>
          <el-menu-item index="/maintenance">维修管理</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/invoices">
          <el-icon><Tickets /></el-icon>
          <span>发票管理</span>
        </el-menu-item>
        <el-menu-item v-if="authStore.isManager" index="/charge-items">
          <el-icon><Goods /></el-icon>
          <span>消费品管理</span>
        </el-menu-item>
        <el-menu-item v-if="authStore.isManager" index="/reports">
          <el-icon><PieChart /></el-icon>
          <span>报表中心</span>
        </el-menu-item>
        <el-menu-item v-if="authStore.isManager" index="/operation-logs">
          <el-icon><Notebook /></el-icon>
          <span>操作日志</span>
        </el-menu-item>
        <el-menu-item v-if="authStore.isManager" index="/users">
          <el-icon><UserFilled /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/profile">
          <el-icon><Setting /></el-icon>
          <span>个人中心</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="title">锦程酒店运营管理系统</span>
        <div class="header-right">
          <el-badge :value="reminderCount" :hidden="reminderCount === 0" class="reminder-badge" @click="goToDashboard">
            <el-icon :size="20" style="cursor: pointer"><Bell /></el-icon>
          </el-badge>
          <el-tag size="small" :type="roleTagType">{{ roleLabel }}</el-tag>
          <span>{{ authStore.user.realName || authStore.user.username }}</span>
          <el-button type="text" @click="logout">退出登录</el-button>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { getDashboardReminders } from '@/api/reminder'
import { DataLine, OfficeBuilding, User, Calendar, Document, Service, Tickets, Goods, PieChart, Notebook, UserFilled, Setting, Bell } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const reminderCount = ref(0)

const activeMenu = computed(() => route.path)

const roleLabel = computed(() => {
  const map = { admin: '管理员', manager: '经理', receptionist: '前台', housekeeping: '客房', engineering: '工程' }
  return map[authStore.role] || '未知'
})

const roleTagType = computed(() => {
  const map = { admin: 'danger', manager: 'warning', receptionist: 'success', housekeeping: 'info', engineering: 'info' }
  return map[authStore.role] || 'info'
})

const fetchReminders = async () => {
  try {
    const data = await getDashboardReminders()
    let count = 0
    if (data) {
      count = (data.birthdayCount || 0) + (data.overdueCount || 0) + (data.lowDepositCount || 0)
    }
    reminderCount.value = count
  } catch (e) {
    console.error('获取提醒失败', e)
  }
}

const goToDashboard = () => {
  router.push('/dashboard')
}

const logout = () => {
  authStore.clearAuth()
  router.replace('/login')
}

onMounted(() => {
  fetchReminders()
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.sidebar {
  background-color: #1e3a5f;
  overflow-x: hidden;
}
.side-menu {
  border-right: none;
}
.logo {
  height: 64px;
  line-height: 64px;
  text-align: center;
  color: #d4af37;
  font-size: 20px;
  font-weight: bold;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  padding: 0 24px;
}
.title {
  font-size: 18px;
  font-weight: bold;
  color: #1e3a5f;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.reminder-badge {
  cursor: pointer;
}
</style>
