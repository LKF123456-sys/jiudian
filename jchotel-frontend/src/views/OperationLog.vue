<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">操作日志</span>
    </div>

    <div class="search-bar">
      <el-input v-model="query.keyword" placeholder="用户名/模块/操作" clearable style="width: 220px;"></el-input>
      <el-select v-model="query.module" placeholder="模块" clearable style="width: 140px;">
        <el-option v-for="m in modules" :key="m" :label="m" :value="m"></el-option>
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px;">
        <el-option label="成功" value="success"></el-option>
        <el-option label="失败" value="fail"></el-option>
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
        <el-table-column prop="module" label="模块" width="120"></el-table-column>
        <el-table-column prop="operation" label="操作" show-overflow-tooltip></el-table-column>
        <el-table-column prop="ip" label="IP地址" width="140"></el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'success' ? 'success' : 'danger'" size="small">
              {{ scope.row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="耗时" width="100">
          <template #default="scope">{{ scope.row.duration || 0 }}ms</template>
        </el-table-column>
        <el-table-column label="操作时间" width="170">
          <template #default="scope">{{ formatDate(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="详情" width="80" v-if="false">
          <template #default="scope">
            <el-button type="text" @click="showDetail(scope.row)">详情</el-button>
          </template>
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

    <el-dialog title="日志详情" v-model="detailVisible" width="500px">
      <el-descriptions :column="1" border v-if="currentLog">
        <el-descriptions-item label="用户名">{{ currentLog.username }}</el-descriptions-item>
        <el-descriptions-item label="模块">{{ currentLog.module }}</el-descriptions-item>
        <el-descriptions-item label="操作">{{ currentLog.operation }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentLog.ip }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentLog.status === 'success' ? 'success' : 'danger'" size="small">
            {{ currentLog.status === 'success' ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="耗时">{{ currentLog.duration || 0 }}ms</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ formatDate(currentLog.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="错误信息" v-if="currentLog.errorMsg">{{ currentLog.errorMsg }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { listOperationLogs } from '@/api/operationLog'
import { formatDate } from '@/utils/format'

const modules = [
  '认证管理', '用户管理', '客房管理', '客户管理',
  '订单管理', '入住管理', '清扫管理', '维修管理',
  '发票管理', '消费品管理', '报表中心', '系统设置'
]

const loading = ref(false)
const list = ref([])
const total = ref(0)
const dateRange = ref([])
const detailVisible = ref(false)
const currentLog = ref(null)

const query = reactive({
  page: 1,
  size: 10,
  keyword: '',
  module: '',
  status: '',
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
  listOperationLogs(query).then(data => {
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
  query.status = ''
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

function showDetail(row) {
  currentLog.value = row
  detailVisible.value = true
}

onMounted(() => {
  loadList()
})
</script>
