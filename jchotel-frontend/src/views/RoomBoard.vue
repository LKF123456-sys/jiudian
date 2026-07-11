<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">房态看板</span>
      <div style="display: flex; gap: 12px;">
        <el-switch v-model="autoRefresh" active-text="自动刷新" @change="handleAutoRefreshChange"></el-switch>
        <el-button @click="loadData" :loading="loading">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
    </div>

    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="4">
        <div class="stat-card">
          <div class="stat-label">空闲</div>
          <div class="stat-value" style="color: #67C23A;">{{ stats.idle || 0 }}</div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-card">
          <div class="stat-label">入住中</div>
          <div class="stat-value" style="color: #E6A23C;">{{ stats.occupied || 0 }}</div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-card">
          <div class="stat-label">维修中</div>
          <div class="stat-value" style="color: #909399;">{{ stats.maintenance || 0 }}</div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-card">
          <div class="stat-label">清扫中</div>
          <div class="stat-value" style="color: #409EFF;">{{ stats.cleaning || 0 }}</div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-card">
          <div class="stat-label">待清扫</div>
          <div class="stat-value" style="color: #F56C6C;">{{ stats.dirty || 0 }}</div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-card">
          <div class="stat-label">入住率</div>
          <div class="stat-value" style="color: #303133;">{{ occupancyRate }}%</div>
        </div>
      </el-col>
    </el-row>

    <div class="table-card">
      <div class="legend-bar">
        <span v-for="(info, key) in ROOM_STATUS_MAP" :key="key" class="legend-item">
          <span class="legend-dot" :style="{ backgroundColor: info.color }"></span>{{ info.label }}
        </span>
      </div>

      <div v-loading="loading">
        <div v-for="(rooms, floor) in floorRooms" :key="floor" class="floor-section">
          <div class="floor-title">{{ floor }}楼</div>
          <el-row :gutter="12">
            <el-col :span="3" v-for="room in rooms" :key="room.id" style="margin-bottom: 12px;">
              <div
                class="room-card"
                :style="{ backgroundColor: ROOM_STATUS_MAP[room.status]?.color + '20', borderColor: ROOM_STATUS_MAP[room.status]?.color }"
                @click="showRoomDetail(room)"
              >
                <div class="room-no">{{ room.roomNo }}</div>
                <div class="room-type">{{ room.typeName }}</div>
                <div class="room-price">¥{{ formatMoney(room.price) }}</div>
                <div class="room-status-tag" :style="{ backgroundColor: ROOM_STATUS_MAP[room.status]?.color }">
                  {{ ROOM_STATUS_MAP[room.status]?.label }}
                </div>
              </div>
            </el-col>
          </el-row>
        </div>
        <div v-if="Object.keys(floorRooms).length === 0 && !loading" style="text-align: center; color: #909399; padding: 40px 0;">
          暂无客房数据
        </div>
      </div>
    </div>

    <el-dialog title="房间详情" v-model="detailVisible" width="600px">
      <div v-if="selectedRoom" v-loading="detailLoading">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="房间号">{{ selectedRoom.roomNo }}</el-descriptions-item>
          <el-descriptions-item label="房型">{{ selectedRoom.typeName }}</el-descriptions-item>
          <el-descriptions-item label="楼层">{{ selectedRoom.floor }}楼</el-descriptions-item>
          <el-descriptions-item label="价格">¥{{ formatMoney(selectedRoom.price) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="ROOM_STATUS_MAP[selectedRoom.status]?.type" size="small">
              {{ ROOM_STATUS_MAP[selectedRoom.status]?.label }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="备注">{{ selectedRoom.remark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="currentOrder" style="margin-top: 20px;">
          <div class="section-title">当前订单信息</div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="客户">{{ currentOrder.customerName }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ currentOrder.customerPhone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="入住时间">{{ formatDate(currentOrder.checkInTime) }}</el-descriptions-item>
            <el-descriptions-item label="预计退房">{{ formatDate(currentOrder.expectedCheckOutTime) }}</el-descriptions-item>
            <el-descriptions-item label="订单金额">¥{{ formatMoney(currentOrder.totalAmount) }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getRoomBoard, getRoomStatusStats } from '@/api/room'
import { getOrder } from '@/api/order'
import { ROOM_STATUS_MAP } from '@/utils/constants'
import { formatDate, formatMoney } from '@/utils/format'

const loading = ref(false)
const autoRefresh = ref(false)
const boardData = ref([])
const stats = ref({})
const detailVisible = ref(false)
const detailLoading = ref(false)
const selectedRoom = ref(null)
const currentOrder = ref(null)
let refreshTimer = null

const floorRooms = computed(() => {
  const map = {}
  boardData.value.forEach(room => {
    const floor = room.floor
    if (!map[floor]) map[floor] = []
    map[floor].push(room)
  })
  const sorted = {}
  Object.keys(map).sort((a, b) => b - a).forEach(floor => {
    sorted[floor] = map[floor].sort((a, b) => a.roomNo.localeCompare(b.roomNo))
  })
  return sorted
})

const occupancyRate = computed(() => {
  const total = boardData.value.length
  if (total === 0) return 0
  const occupied = boardData.value.filter(r => r.status === 'occupied').length
  return ((occupied / total) * 100).toFixed(1)
})

function loadData() {
  loading.value = true
  Promise.all([
    getRoomBoard(),
    getRoomStatusStats()
  ]).then(([board, statsData]) => {
    boardData.value = board || []
    stats.value = statsData || {}
  }).catch(() => {
    boardData.value = []
    stats.value = {}
  }).finally(() => {
    loading.value = false
  })
}

function handleAutoRefreshChange(val) {
  if (val) {
    refreshTimer = setInterval(() => {
      loadData()
    }, 30000)
    ElMessage.success('已开启自动刷新（每30秒）')
  } else {
    if (refreshTimer) {
      clearInterval(refreshTimer)
      refreshTimer = null
    }
    ElMessage.info('已关闭自动刷新')
  }
}

function showRoomDetail(room) {
  selectedRoom.value = room
  currentOrder.value = null
  detailVisible.value = true
  if (room.status === 'occupied' && room.currentOrderId) {
    detailLoading.value = true
    getOrder(room.currentOrderId).then(data => {
      currentOrder.value = data
    }).catch(() => {
      currentOrder.value = null
    }).finally(() => {
      detailLoading.value = false
    })
  }
}

onMounted(() => {
  loadData()
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
}
.legend-bar {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}
.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}
.legend-dot {
  width: 14px;
  height: 14px;
  border-radius: 3px;
}
.floor-section {
  margin-bottom: 24px;
}
.floor-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 12px;
  padding-left: 8px;
  border-left: 4px solid #409EFF;
}
.room-card {
  background: #fff;
  border: 2px solid;
  border-radius: 8px;
  padding: 12px 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}
.room-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0,0,0,0.15);
}
.room-no {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}
.room-type {
  font-size: 12px;
  color: #606266;
  margin-top: 4px;
}
.room-price {
  font-size: 13px;
  color: #f56c6c;
  margin-top: 4px;
}
.room-status-tag {
  position: absolute;
  top: -8px;
  right: -8px;
  color: #fff;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
}
.section-title {
  font-size: 15px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 12px;
  padding-left: 8px;
  border-left: 4px solid #E6A23C;
}
</style>
