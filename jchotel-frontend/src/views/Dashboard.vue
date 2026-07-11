<template>
  <div class="dashboard-page">
    <el-row :gutter="16">
      <el-col :span="4">
        <div class="stat-card primary">
          <div class="stat-icon"><el-icon><Money /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">¥{{ formatMoney(stats.todayRevenue) }}</div>
            <div class="stat-label">今日营收</div>
          </div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-card success">
          <div class="stat-icon"><el-icon><OfficeBuilding /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.currentCheckIn }}</div>
            <div class="stat-label">当前在住</div>
          </div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-card warning">
          <div class="stat-icon"><el-icon><CircleCheckFilled /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.todayCheckIn }}</div>
            <div class="stat-label">今日入住</div>
          </div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-card info">
          <div class="stat-icon"><el-icon><Clock /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.pendingCount }}</div>
            <div class="stat-label">待入住预约</div>
          </div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-card purple">
          <div class="stat-icon"><el-icon><Brush /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ cleaningPending + cleaningAssigned }}</div>
            <div class="stat-label">待清扫</div>
          </div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-card danger">
          <div class="stat-icon"><el-icon><Warning /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.overdueCheckoutCount || 0 }}</div>
            <div class="stat-label">逾期退房</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>房态概览</span>
              <div class="legend">
                <span v-for="(item, key) in ROOM_STATUS_MAP" :key="key" class="legend-item">
                  <span class="dot" :style="{background: item.color}"></span>{{ item.label }}
                </span>
              </div>
              <el-button type="primary" link @click="$router.push('/rooms/board')">查看房态看板</el-button>
            </div>
          </template>
          <div class="room-grid">
            <div
              v-for="room in rooms"
              :key="room.id"
              class="room-card"
              :style="{borderColor: ROOM_STATUS_MAP[room.status]?.color || '#ddd', background: ROOM_STATUS_MAP[room.status]?.color + '15'}"
              @click="$router.push('/checkin')"
            >
              <div class="room-no">{{ room.roomNo }}</div>
              <div class="room-type">{{ room.typeName || '' }}</div>
              <div class="room-status" :style="{color: ROOM_STATUS_MAP[room.status]?.color}">{{ ROOM_STATUS_MAP[room.status]?.label }}</div>
            </div>
            <div v-if="rooms.length === 0" class="empty">暂无客房</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card style="margin-bottom: 16px;">
          <template #header>
            <div class="card-header">
              <span><el-icon><Bell /></el-icon> 提醒事项</span>
              <el-badge :value="totalReminders" :hidden="totalReminders === 0" type="danger"></el-badge>
            </div>
          </template>
          <div class="reminder-list">
            <div v-if="reminders && reminders.birthdayCustomers && reminders.birthdayCustomers.length > 0" class="reminder-item birthday">
              <el-icon><Present /></el-icon>
              <span>今日有 <b>{{ reminders.birthdayCustomers.length }}</b> 位客户生日</span>
            </div>
            <div v-if="reminders && reminders.arrivalCount > 0" class="reminder-item arrival" @click="$router.push('/orders')">
              <el-icon><Calendar /></el-icon>
              <span>今日预计到店 <b>{{ reminders.arrivalCount }}</b> 单</span>
            </div>
            <div v-if="reminders && reminders.departureCount > 0" class="reminder-item departure" @click="$router.push('/checkin')">
              <el-icon><Suitcase /></el-icon>
              <span>今日预计离店 <b>{{ reminders.departureCount }}</b> 单</span>
            </div>
            <div v-if="reminders && reminders.overdueCount > 0" class="reminder-item overdue" @click="$router.push('/checkin')">
              <el-icon><Warning /></el-icon>
              <span><b>{{ reminders.overdueCount }}</b> 个订单逾期未退房</span>
            </div>
            <div v-if="reminders && reminders.lowDepositCount > 0" class="reminder-item low-deposit">
              <el-icon><Wallet /></el-icon>
              <span><b>{{ reminders.lowDepositCount }}</b> 个订单押金不足</span>
            </div>
            <div v-if="totalReminders === 0" class="no-reminder">
              <el-icon><CircleCheckFilled /></el-icon>
              <span>暂无待处理提醒</span>
            </div>
          </div>
        </el-card>

        <el-card>
          <template #header>
            <div class="card-header"><span>快捷操作</span></div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/checkin')">
              <el-icon><Plus /></el-icon> 办理入住
            </el-button>
            <el-button @click="$router.push('/rooms/board')">
              <el-icon><Grid /></el-icon> 房态看板
            </el-button>
            <el-button @click="$router.push('/customers')">
              <el-icon><User /></el-icon> 客户管理
            </el-button>
            <el-button @click="$router.push('/cleaning')">
              <el-icon><Brush /></el-icon> 清扫管理
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>今日到店</span>
              <el-button type="text" @click="$router.push('/orders')">更多</el-button>
            </div>
          </template>
          <el-table :data="todayArrivals" size="small" stripe>
            <el-table-column prop="customerName" label="客户"></el-table-column>
            <el-table-column prop="roomNo" label="房间" width="80"></el-table-column>
            <el-table-column label="预计到店" width="100">
              <template #default="scope">{{ formatTime(scope.row.checkInTime) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="scope">
                <el-tag :type="ORDER_STATUS_MAP[scope.row.status]?.type" size="small">{{ ORDER_STATUS_MAP[scope.row.status]?.label }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="!todayArrivals || todayArrivals.length === 0" class="empty-small">暂无今日到店</div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>逾期退房</span>
              <el-button type="text" @click="$router.push('/checkin')">前往处理</el-button>
            </div>
          </template>
          <el-table :data="overdueList" size="small" stripe>
            <el-table-column prop="roomNo" label="房间" width="80"></el-table-column>
            <el-table-column prop="customerName" label="客户"></el-table-column>
            <el-table-column prop="customerPhone" label="电话" width="120"></el-table-column>
            <el-table-column label="应退时间" width="160">
              <template #default="scope">{{ formatDate(scope.row.expectedCheckOutTime) }}</template>
            </el-table-column>
          </el-table>
          <div v-if="!overdueList || overdueList.length === 0" class="empty-small">暂无逾期退房</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { getDashboard } from '@/api/dashboard'
import { getRoomBoard } from '@/api/room'
import { formatDate, formatMoney } from '@/utils/format'
import { ROOM_STATUS_MAP, ORDER_STATUS_MAP } from '@/utils/constants'
import dayjs from 'dayjs'
import { Money, OfficeBuilding, CircleCheckFilled, Clock, Brush, Warning, Bell, Present, Calendar, Suitcase, Wallet, Plus, Grid, User } from '@element-plus/icons-vue'

const stats = ref({
  todayCheckIn: 0,
  currentCheckIn: 0,
  pendingCount: 0,
  idleRoom: 0,
  overdueCheckoutCount: 0,
  overdueList: [],
  todayRevenue: 0,
  cleaningPendingCount: 0,
  cleaningAssignedCount: 0,
  maintenancePendingCount: 0
})
const rooms = ref([])
const reminders = ref(null)
const todayArrivals = ref([])
const overdueList = ref([])
const cleaningPending = ref(0)
const cleaningAssigned = ref(0)
let timer = null

const totalReminders = computed(() => {
  if (!reminders.value) return 0
  return (reminders.value.birthdayCount || 0) +
         (reminders.value.overdueCount || 0) +
         (reminders.value.lowDepositCount || 0)
})

const formatTime = (t) => t ? dayjs(t).format('HH:mm') : ''

const loadData = async () => {
  try {
    const data = await getDashboard()
    stats.value = { ...stats.value, ...(data || {}) }
    reminders.value = data?.reminders || null
    todayArrivals.value = data?.reminders?.todayArrivals || []
    overdueList.value = data?.reminders?.overdueCheckouts || data?.overdueList || []
    cleaningPending.value = data?.cleaningPendingCount || 0
    cleaningAssigned.value = data?.cleaningAssignedCount || 0
  } catch (e) {
    console.error(e)
  }

  try {
    rooms.value = await getRoomBoard()
  } catch (e) {
    rooms.value = []
  }
}

onMounted(() => {
  loadData()
  timer = setInterval(loadData, 60000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.dashboard-page {
  padding: 0;
}
.stat-card {
  display: flex;
  align-items: center;
  padding: 18px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  border-left: 4px solid;
}
.stat-card.primary { border-color: #409EFF; }
.stat-card.success { border-color: #67C23A; }
.stat-card.warning { border-color: #E6A23C; }
.stat-card.info { border-color: #909399; }
.stat-card.purple { border-color: #9B59B6; }
.stat-card.danger { border-color: #F56C6C; }
.stat-icon {
  font-size: 36px;
  margin-right: 12px;
  opacity: 0.8;
}
.stat-card.primary .stat-icon { color: #409EFF; }
.stat-card.success .stat-icon { color: #67C23A; }
.stat-card.warning .stat-icon { color: #E6A23C; }
.stat-card.info .stat-icon { color: #909399; }
.stat-card.purple .stat-icon { color: #9B59B6; }
.stat-card.danger .stat-icon { color: #F56C6C; }
.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 2px;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.legend {
  display: flex;
  gap: 12px;
  font-size: 12px;
  flex: 1;
  justify-content: center;
}
.legend-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #606266;
}
.dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 2px;
}
.room-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(90px, 1fr));
  gap: 10px;
}
.room-card {
  padding: 10px 8px;
  border-radius: 6px;
  border: 2px solid;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
}
.room-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.12);
}
.room-no {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}
.room-type {
  font-size: 11px;
  color: #909399;
  margin: 2px 0;
}
.room-status {
  font-size: 12px;
  font-weight: 500;
}
.empty, .empty-small {
  text-align: center;
  color: #C0C4CC;
  padding: 40px 0;
  font-size: 14px;
}
.empty-small {
  padding: 20px 0;
  font-size: 13px;
}
.reminder-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.reminder-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}
.reminder-item:hover {
  filter: brightness(0.95);
}
.reminder-item.birthday {
  background: #fdf6ec;
  color: #e6a23c;
}
.reminder-item.arrival {
  background: #ecf5ff;
  color: #409eff;
}
.reminder-item.departure {
  background: #f0f9eb;
  color: #67c23a;
}
.reminder-item.overdue {
  background: #fef0f0;
  color: #f56c6c;
}
.reminder-item.low-deposit {
  background: #faf0f9;
  color: #9b59b6;
}
.no-reminder {
  text-align: center;
  padding: 20px 0;
  color: #67c23a;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}
.quick-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}
</style>
