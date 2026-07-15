<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">清扫任务管理</span>
    </div>

    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">待分配</div>
          <div class="stat-value" style="color: #909399;">{{ stats.pending || 0 }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">已分配</div>
          <div class="stat-value" style="color: #E6A23C;">{{ stats.assigned || 0 }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">清扫中</div>
          <div class="stat-value" style="color: #409EFF;">{{ stats.cleaning || 0 }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">待检查</div>
          <div class="stat-value" style="color: #F56C6C;">{{ stats.inspecting || 0 }}</div>
        </div>
      </el-col>
    </el-row>

    <div class="search-bar">
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px;">
        <el-option v-for="(info, key) in CLEANING_STATUS_MAP" :key="key" :label="info.label" :value="key"></el-option>
      </el-select>
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon>查询
      </el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <div class="table-card">
      <el-table :data="list" stripe v-loading="loading">
        <el-table-column prop="roomNo" label="房间号" width="100"></el-table-column>
        <el-table-column prop="roomTypeName" label="房型" width="120"></el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="CLEANING_STATUS_MAP[scope.row.status]?.type" size="small">
              {{ CLEANING_STATUS_MAP[scope.row.status]?.label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assigneeName" label="分配人" width="120">
          <template #default="scope">{{ scope.row.assigneeName || '-' }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="scope">{{ formatDate(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip></el-table-column>
        <el-table-column label="操作" width="320">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 'pending'"
              type="text"
              @click="openAssignDialog(scope.row)"
            >分配</el-button>
            <el-button
              v-if="scope.row.status === 'assigned'"
              type="text"
              @click="handleStart(scope.row)"
            >开始清扫</el-button>
            <el-button
              v-if="scope.row.status === 'cleaning'"
              type="text"
              @click="handleFinish(scope.row)"
            >完成清扫</el-button>
            <el-button
              v-if="scope.row.status === 'inspecting'"
              type="text"
              @click="handleInspect(scope.row)"
            >检查通过</el-button>
            <el-button
              v-if="['pending', 'assigned'].includes(scope.row.status)"
              type="text"
              style="color: #f5222d;"
              @click="handleCancel(scope.row)"
            >取消</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        style="margin-top: 20px; text-align: right;"
        :current-page="query.page"
        :page-size="query.size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handlePageChange"
      ></el-pagination>
    </div>

    <el-dialog title="分配任务" v-model="assignDialogVisible" width="450px">
      <el-form :model="assignForm" label-width="80px">
        <el-form-item label="房间号">
          <el-input v-model="assignForm.roomNo" disabled></el-input>
        </el-form-item>
        <el-form-item label="分配给" required>
          <el-select v-model="assignForm.assigneeId" placeholder="请选择保洁人员" style="width: 100%;" filterable @change="handleAssigneeChange">
            <el-option v-for="u in housekeepingUsers" :key="u.id" :label="u.realName || u.username" :value="u.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="assignForm.remark" type="textarea" :rows="3"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAssign" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import {
  listCleaningTasks,
  assignCleaningTask,
  startCleaningTask,
  finishCleaningTask,
  inspectCleaningTask,
  cancelCleaningTask
} from '@/api/cleaning'
import { listUsers } from '@/api/user'
import { CLEANING_STATUS_MAP } from '@/utils/constants'
import { formatDate } from '@/utils/format'

const loading = ref(false)
const submitLoading = ref(false)
const list = ref([])
const total = ref(0)
const stats = ref({})
const housekeepingUsers = ref([])
const assignDialogVisible = ref(false)
const assignForm = reactive({
  id: null,
  roomNo: '',
  assigneeId: null,
  assigneeName: '',
  remark: ''
})

const query = reactive({
  page: 1,
  size: 10,
  status: ''
})

function loadList() {
  loading.value = true
  listCleaningTasks(query).then(data => {
    list.value = data?.list || []
    total.value = data?.total || 0
    calculateStats(data?.list || [])
  }).catch(() => {
    list.value = []
    total.value = 0
  }).finally(() => {
    loading.value = false
  })
}

function calculateStats(tasks) {
  const s = { pending: 0, assigned: 0, cleaning: 0, inspecting: 0 }
  tasks.forEach(t => {
    if (s[t.status] !== undefined) s[t.status]++
  })
  if (tasks.length === query.size) {
    s._full = true
  }
  stats.value = s
}

function loadHousekeepingUsers() {
  listUsers({ page: 1, size: 100, role: 'housekeeping' }).then(data => {
    housekeepingUsers.value = data?.list || []
  }).catch(() => {
    housekeepingUsers.value = []
  })
}

function handleSearch() {
  query.page = 1
  loadList()
}

function handleReset() {
  query.page = 1
  query.status = ''
  loadList()
}

function handlePageChange(page) {
  query.page = page
  loadList()
}

function handleAssigneeChange(userId) {
  const selected = housekeepingUsers.value.find(u => u.id === userId)
  assignForm.assigneeName = selected ? (selected.realName || selected.username) : ''
}

function openAssignDialog(row) {
  assignForm.id = row.id
  assignForm.roomNo = row.roomNo
  assignForm.assigneeId = null
  assignForm.assigneeName = ''
  assignForm.remark = row.remark || ''
  assignDialogVisible.value = true
}

function submitAssign() {
  if (!assignForm.assigneeId) {
    ElMessage.warning('请选择分配人员')
    return
  }
  submitLoading.value = true
  assignCleaningTask(assignForm.id, {
    assigneeId: assignForm.assigneeId,
    assigneeName: assignForm.assigneeName,
    remark: assignForm.remark
  }).then(() => {
    ElMessage.success('分配成功')
    assignDialogVisible.value = false
    loadList()
  }).catch(() => {}).finally(() => {
    submitLoading.value = false
  })
}

function handleStart(row) {
  ElMessageBox.confirm('确认开始清扫该房间？', '提示', { type: 'warning' }).then(() => {
    startCleaningTask(row.id).then(() => {
      ElMessage.success('已开始清扫')
      loadList()
    }).catch(() => {})
  }).catch(() => {})
}

function handleFinish(row) {
  ElMessageBox.confirm('确认完成清扫？', '提示', { type: 'warning' }).then(() => {
    finishCleaningTask(row.id).then(() => {
      ElMessage.success('已完成清扫，等待检查')
      loadList()
    }).catch(() => {})
  }).catch(() => {})
}

function handleInspect(row) {
  ElMessageBox.confirm('确认检查通过？', '提示', { type: 'warning' }).then(() => {
    inspectCleaningTask(row.id).then(() => {
      ElMessage.success('检查通过，任务完成')
      loadList()
    }).catch(() => {})
  }).catch(() => {})
}

function handleCancel(row) {
  ElMessageBox.confirm('确认取消该任务？', '提示', { type: 'warning' }).then(() => {
    cancelCleaningTask(row.id).then(() => {
      ElMessage.success('已取消任务')
      loadList()
    }).catch(() => {})
  }).catch(() => {})
}

onMounted(() => {
  loadList()
  loadHousekeepingUsers()
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
</style>
