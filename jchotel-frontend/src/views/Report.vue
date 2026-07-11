<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">报表中心</span>
    </div>

    <el-card shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="交接班报表" name="shift">
          <template #label>
            <span><el-icon><User /></el-icon>交接班报表</span>
          </template>
          <div class="report-toolbar">
            <el-button type="primary" @click="loadShiftReport" :loading="loading">
              <el-icon><Refresh /></el-icon>刷新
            </el-button>
          </div>
          <div v-loading="loading" style="margin-top: 16px;">
            <el-row :gutter="20">
              <el-col :span="6">
                <div class="report-stat-card">
                  <div class="report-stat-label">本班次收款</div>
                  <div class="report-stat-value">¥{{ formatMoney(shiftData.totalRevenue || 0) }}</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="report-stat-card">
                  <div class="report-stat-label">现金收款</div>
                  <div class="report-stat-value" style="color: #67C23A;">¥{{ formatMoney(shiftData.cashRevenue || 0) }}</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="report-stat-card">
                  <div class="report-stat-label">入住数</div>
                  <div class="report-stat-value" style="color: #409EFF;">{{ shiftData.checkInCount || 0 }}</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="report-stat-card">
                  <div class="report-stat-label">退房数</div>
                  <div class="report-stat-value" style="color: #E6A23C;">{{ shiftData.checkOutCount || 0 }}</div>
                </div>
              </el-col>
            </el-row>
            <el-descriptions title="交班信息" :column="2" border style="margin-top: 20px;">
              <el-descriptions-item label="交班人">{{ shiftData.handoverName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="接班人">{{ shiftData.takerName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="交班时间">{{ formatDate(shiftData.handoverTime) }}</el-descriptions-item>
              <el-descriptions-item label="接班时间">{{ formatDate(shiftData.takeTime) }}</el-descriptions-item>
              <el-descriptions-item label="备注" :span="2">{{ shiftData.remark || '-' }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-tab-pane>

        <el-tab-pane label="入住率报表" name="occupancy">
          <template #label>
            <span><el-icon><DataAnalysis /></el-icon>入住率报表</span>
          </template>
          <div class="report-toolbar">
            <el-date-picker
              v-model="occupancyDateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 260px;"
              value-format="YYYY-MM-DD"
            ></el-date-picker>
            <el-button type="primary" @click="loadOccupancyReport" :loading="loading">
              <el-icon><Search /></el-icon>查询
            </el-button>
          </div>
          <div v-loading="loading" style="margin-top: 16px;">
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="report-stat-card">
                  <div class="report-stat-label">平均入住率</div>
                  <div class="report-stat-value" style="color: #409EFF;">{{ occupancyData.avgOccupancyRate || 0 }}%</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="report-stat-card">
                  <div class="report-stat-label">总客房数</div>
                  <div class="report-stat-value">{{ occupancyData.totalRooms || 0 }}</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="report-stat-card">
                  <div class="report-stat-label">平均入住数</div>
                  <div class="report-stat-value" style="color: #E6A23C;">{{ occupancyData.avgOccupied || 0 }}</div>
                </div>
              </el-col>
            </el-row>
            <el-table :data="occupancyData.details || []" stripe style="margin-top: 20px;">
              <el-table-column prop="date" label="日期" width="120"></el-table-column>
              <el-table-column prop="totalRooms" label="总客房数" width="100"></el-table-column>
              <el-table-column prop="occupied" label="入住数" width="100"></el-table-column>
              <el-table-column label="入住率">
                <template #default="scope">
                  <el-progress :percentage="Number(scope.row.rate || 0)" :color="getOccupancyColor(scope.row.rate)"></el-progress>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="支付方式统计" name="payment">
          <template #label>
            <span><el-icon><CreditCard /></el-icon>支付方式统计</span>
          </template>
          <div class="report-toolbar">
            <el-date-picker
              v-model="paymentDateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 260px;"
              value-format="YYYY-MM-DD"
            ></el-date-picker>
            <el-button type="primary" @click="loadPaymentStats" :loading="loading">
              <el-icon><Search /></el-icon>查询
            </el-button>
          </div>
          <div v-loading="loading" style="margin-top: 16px;">
            <el-row :gutter="20" style="margin-bottom: 20px;">
              <el-col :span="8">
                <div class="report-stat-card">
                  <div class="report-stat-label">总收款金额</div>
                  <div class="report-stat-value" style="color: #f56c6c;">¥{{ formatMoney(paymentData.totalAmount || 0) }}</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="report-stat-card">
                  <div class="report-stat-label">总订单数</div>
                  <div class="report-stat-value">{{ paymentData.totalCount || 0 }}</div>
                </div>
              </el-col>
            </el-row>
            <el-table :data="paymentData.list || []" stripe>
              <el-table-column prop="paymentMethod" label="支付方式" width="150"></el-table-column>
              <el-table-column label="金额" width="150">
                <template #default="scope">¥{{ formatMoney(scope.row.amount) }}</template>
              </el-table-column>
              <el-table-column prop="count" label="笔数" width="100"></el-table-column>
              <el-table-column label="占比">
                <template #default="scope">
                  <el-progress :percentage="Number(scope.row.percentage || 0)"></el-progress>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="房型收入统计" name="roomType">
          <template #label>
            <span><el-icon><House /></el-icon>房型收入统计</span>
          </template>
          <div class="report-toolbar">
            <el-date-picker
              v-model="roomTypeDateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 260px;"
              value-format="YYYY-MM-DD"
            ></el-date-picker>
            <el-button type="primary" @click="loadRoomTypeRevenue" :loading="loading">
              <el-icon><Search /></el-icon>查询
            </el-button>
          </div>
          <div v-loading="loading" style="margin-top: 16px;">
            <el-row :gutter="20" style="margin-bottom: 20px;">
              <el-col :span="8">
                <div class="report-stat-card">
                  <div class="report-stat-label">房型总收入</div>
                  <div class="report-stat-value" style="color: #67C23A;">¥{{ formatMoney(roomTypeData.totalAmount || 0) }}</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="report-stat-card">
                  <div class="report-stat-label">总入住间夜</div>
                  <div class="report-stat-value">{{ roomTypeData.totalNights || 0 }}</div>
                </div>
              </el-col>
            </el-row>
            <el-table :data="roomTypeData.list || []" stripe>
              <el-table-column prop="roomTypeName" label="房型" width="150"></el-table-column>
              <el-table-column prop="roomCount" label="房间数" width="100"></el-table-column>
              <el-table-column label="收入" width="150">
                <template #default="scope">¥{{ formatMoney(scope.row.amount) }}</template>
              </el-table-column>
              <el-table-column prop="nights" label="入住间夜" width="100"></el-table-column>
              <el-table-column prop="avgPrice" label="平均房价" width="120">
                <template #default="scope">¥{{ formatMoney(scope.row.avgPrice) }}</template>
              </el-table-column>
              <el-table-column label="收入占比">
                <template #default="scope">
                  <el-progress :percentage="Number(scope.row.percentage || 0)" color="#E6A23C"></el-progress>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import dayjs from 'dayjs'
import { User, Refresh, DataAnalysis, CreditCard, House, Search } from '@element-plus/icons-vue'
import { getShiftReport, getOccupancyReport, getPaymentStats, getRoomTypeRevenue } from '@/api/report'
import { formatDate, formatMoney } from '@/utils/format'

const loading = ref(false)
const activeTab = ref('shift')

const shiftData = ref({})

const occupancyDateRange = ref([])
const occupancyData = ref({})

const paymentDateRange = ref([])
const paymentData = ref({})

const roomTypeDateRange = ref([])
const roomTypeData = ref({})

function getDefaultDateRange() {
  const end = dayjs().format('YYYY-MM-DD')
  const start = dayjs().subtract(7, 'day').format('YYYY-MM-DD')
  return [start, end]
}

function loadShiftReport() {
  loading.value = true
  getShiftReport().then(data => {
    shiftData.value = data || {}
  }).catch(() => {
    shiftData.value = {}
  }).finally(() => {
    loading.value = false
  })
}

function loadOccupancyReport() {
  loading.value = true
  const params = {}
  if (occupancyDateRange.value && occupancyDateRange.value.length === 2) {
    params.startDate = occupancyDateRange.value[0]
    params.endDate = occupancyDateRange.value[1]
  }
  getOccupancyReport(params).then(data => {
    occupancyData.value = data || {}
  }).catch(() => {
    occupancyData.value = {}
  }).finally(() => {
    loading.value = false
  })
}

function loadPaymentStats() {
  loading.value = true
  const params = {}
  if (paymentDateRange.value && paymentDateRange.value.length === 2) {
    params.startDate = paymentDateRange.value[0]
    params.endDate = paymentDateRange.value[1]
  }
  getPaymentStats(params).then(data => {
    paymentData.value = data || {}
  }).catch(() => {
    paymentData.value = {}
  }).finally(() => {
    loading.value = false
  })
}

function loadRoomTypeRevenue() {
  loading.value = true
  const params = {}
  if (roomTypeDateRange.value && roomTypeDateRange.value.length === 2) {
    params.startDate = roomTypeDateRange.value[0]
    params.endDate = roomTypeDateRange.value[1]
  }
  getRoomTypeRevenue(params).then(data => {
    roomTypeData.value = data || {}
  }).catch(() => {
    roomTypeData.value = {}
  }).finally(() => {
    loading.value = false
  })
}

function handleTabChange(tab) {
  if (tab === 'shift') {
    loadShiftReport()
  } else if (tab === 'occupancy') {
    if (!occupancyDateRange.value || occupancyDateRange.value.length === 0) {
      occupancyDateRange.value = getDefaultDateRange()
    }
    loadOccupancyReport()
  } else if (tab === 'payment') {
    if (!paymentDateRange.value || paymentDateRange.value.length === 0) {
      paymentDateRange.value = getDefaultDateRange()
    }
    loadPaymentStats()
  } else if (tab === 'roomType') {
    if (!roomTypeDateRange.value || roomTypeDateRange.value.length === 0) {
      roomTypeDateRange.value = getDefaultDateRange()
    }
    loadRoomTypeRevenue()
  }
}

function getOccupancyColor(rate) {
  const r = Number(rate) || 0
  if (r >= 80) return '#67C23A'
  if (r >= 50) return '#E6A23C'
  return '#F56C6C'
}

onMounted(() => {
  loadShiftReport()
})
</script>

<style scoped>
.report-toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
}
.report-stat-card {
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ed 100%);
  border-radius: 8px;
  padding: 20px;
  text-align: center;
}
.report-stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}
.report-stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}
</style>
