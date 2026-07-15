<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">操作日志</span>
    </div>

    <div class="search-bar">
      <el-input v-model="query.keyword" placeholder="用户名/模块/操作" clearable style="width: 220px;"></el-input>
      <el-select v-model="query.module" placeholder="模块" clearable style="width: 140px;">
        <el-option v-for="m in moduleOptions" :key="m.value" :label="m.label" :value="m.value"></el-option>
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px;">
        <el-option label="成功" :value="1"></el-option>
        <el-option label="失败" :value="0"></el-option>
      </el-select>
      <el-date-picker
        v-model="dateRange"
        type="datetimerange"
        range-separator="至"
        start-placeholder="开始时间"
        end-placeholder="结束时间"
        style="width: 360px;"
        value-format="YYYY-MM-DD HH:mm:ss"
      ></el-date-picker>
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon>查询
      </el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <div class="table-card">
      <el-table :data="list" stripe v-loading="loading">
        <el-table-column prop="username" label="用户名" width="120"></el-table-column>
        <el-table-column label="模块" width="120">
          <template #default="scope">{{ moduleLabel(scope.row.module) }}</template>
        </el-table-column>
        <el-table-column prop="operation" label="操作" show-overflow-tooltip></el-table-column>
        <el-table-column prop="ip" label="IP地址" width="140"></el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
              {{ scope.row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="耗时" width="100">
          <template #default="scope">{{ scope.row.costTime || 0 }}ms</template>
        </el-table-column>
        <el-table-column label="操作时间" width="170">
          <template #default="scope">{{ formatDate(scope.row.createTime) }}</template>
        </el-table-column>
      </el-table>
      <el-pagination
        style="margin-top: 20px; text-align: right;"
        :current-page="query.page"
        :page-size="query.size"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        :page-sizes="[10, 20, 50, 100]"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      ></el-pagination>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { listOperationLogs } from '@/api/operationLog'
import { formatDate } from '@/utils/format'

const moduleOptions = [
  { label: '认证管理', value: 'Auth' },
  { label: '用户管理', value: 'User' },
  { label: '客房管理', value: 'Room' },
  { label: '客户管理', value: 'Customer' },
  { label: '订单管理', value: 'Order' },
  { label: '入住管理', value: 'CheckIn' },
  { label: '清扫管理', value: 'CleaningTask' },
  { label: '维修管理', value: 'MaintenanceOrder' },
  { label: '发票管理', value: 'Invoice' },
  { label: '消费品管理', value: 'ChargeItem' },
  { label: '报表中心', value: 'Report' },
  { label: '系统设置', value: 'System' },
  { label: '操作日志', value: 'OperationLog' }
]

const moduleMap = {}
moduleOptions.forEach(m => { moduleMap[m.value] = m.label })

function moduleLabel(val) {
  return moduleMap[val] || val
}

const loading = ref(false)
const list = ref([])
const total = ref(0)
const dateRange = ref([])

const query = reactive({
  page: 1,
  size: 10,
  keyword: '',
  module: '',
  status: null,
  startTime: '',
  endTime: ''
})

function loadList() {
  loading.value = true
  if (dateRange.value && dateRange.value.length === 2) {
    query.startTime = dateRange.value[0]
    query.endTime = dateRange.value[1]
  } else {
    query.startTime = ''
    query.endTime = ''
  }
  const params = {
    page: query.page,
    size: query.size,
    keyword: query.keyword || undefined,
    module: query.module || undefined,
    status: query.status,
    startTime: query.startTime || undefined,
    endTime: query.endTime || undefined
  }
  listOperationLogs(params).then(data => {
    list.value = data?.list || []
    total.value = data?.total || 0
  }).catch(() => {
    list.value = []
    total.value = 0
  }).finally(() => {
    loading.value = false
  })
}

function handleSearch() {
  query.page = 1
  loadList()
}

function handleReset() {
  query.page = 1
  query.keyword = ''
  query.module = ''
  query.status = null
  dateRange.value = []
  loadList()
}

function handlePageChange(page) {
  query.page = page
  loadList()
}

function handleSizeChange(size) {
  query.size = size
  query.page = 1
  loadList()
}

onMounted(() => {
  loadList()
})
</script>
