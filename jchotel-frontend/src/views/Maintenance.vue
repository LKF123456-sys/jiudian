<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">维修工单管理</span>
      <el-button type="primary" @click="openCreateDialog()">
        <el-icon><Plus /></el-icon>创建工单
      </el-button>
    </div>

    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">待处理</div>
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
          <div class="stat-label">维修中</div>
          <div class="stat-value" style="color: #409EFF;">{{ stats.processing || 0 }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">已完成</div>
          <div class="stat-value" style="color: #67C23A;">{{ stats.completed || 0 }}</div>
        </div>
      </el-col>
    </el-row>

    <div class="search-bar">
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px;">
        <el-option v-for="(info, key) in MAINTENANCE_STATUS_MAP" :key="key" :label="info.label" :value="key"></el-option>
      </el-select>
      <el-input v-model="query.keyword" placeholder="房间号" clearable style="width: 150px;"></el-input>
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon>查询
      </el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <div class="table-card">
      <el-table :data="list" stripe v-loading="loading">
        <el-table-column prop="roomNo" label="房间号" width="100"></el-table-column>
        <el-table-column prop="description" label="问题描述" show-overflow-tooltip></el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="MAINTENANCE_STATUS_MAP[scope.row.status]?.type" size="small">
              {{ MAINTENANCE_STATUS_MAP[scope.row.status]?.label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reporterName" label="报修人" width="100">
          <template #default="scope">{{ scope.row.reporterName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="assigneeName" label="处理人" width="100">
          <template #default="scope">{{ scope.row.assigneeName || '-' }}</template>
        </el-table-column>
        <el-table-column label="维修费用" width="100">
          <template #default="scope">
            <span v-if="scope.row.cost !== null && scope.row.cost !== undefined">¥{{ formatMoney(scope.row.cost) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="scope">{{ formatDate(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="280">
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
            >开始维修</el-button>
            <el-button
              v-if="scope.row.status === 'processing'"
              type="text"
              @click="openFinishDialog(scope.row)"
            >完成维修</el-button>
            <el-button
              v-if="scope.row.status === 'completed'"
              type="text"
              @click="handleVerify(scope.row)"
            >验收</el-button>
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

    <el-dialog :title="createDialogTitle" v-model="createDialogVisible" width="500px">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="90px">
        <el-form-item label="房间" prop="roomId">
          <el-select v-model="createForm.roomId" placeholder="请选择房间" style="width: 100%;" filterable>
            <el-option v-for="r in rooms" :key="r.id" :label="`${r.roomNo} - ${r.typeName}`" :value="r.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="问题描述" prop="description">
          <el-input v-model="createForm.description" type="textarea" :rows="4" placeholder="请描述问题"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog title="分配工单" v-model="assignDialogVisible" width="450px">
      <el-form :model="assignForm" label-width="80px">
        <el-form-item label="房间号">
          <el-input v-model="assignForm.roomNo" disabled></el-input>
        </el-form-item>
        <el-form-item label="分配给" required>
          <el-select v-model="assignForm.assigneeId" placeholder="请选择维修人员" style="width: 100%;" filterable @change="handleAssigneeChange">
            <el-option v-for="u in engineeringUsers" :key="u.id" :label="u.realName || u.username" :value="u.id"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAssign" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog title="完成维修" v-model="finishDialogVisible" width="500px">
      <el-form :model="finishForm" :rules="finishRules" ref="finishFormRef" label-width="90px">
        <el-form-item label="房间号">
          <el-input v-model="finishForm.roomNo" disabled></el-input>
        </el-form-item>
        <el-form-item label="解决方案" prop="solution">
          <el-input v-model="finishForm.solution" type="textarea" :rows="4" placeholder="请描述解决方案"></el-input>
        </el-form-item>
        <el-form-item label="维修费用" prop="cost">
          <el-input-number v-model="finishForm.cost" :min="0" :precision="2" style="width: 100%;" placeholder="维修费用"></el-input-number>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="finishDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitFinish" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import {
  listMaintenanceOrders,
  createMaintenanceOrder,
  assignMaintenanceOrder,
  finishMaintenanceOrder,
  verifyMaintenanceOrder,
  cancelMaintenanceOrder
} from '@/api/maintenance'
import { listRooms } from '@/api/room'
import { listUsers } from '@/api/user'
import { MAINTENANCE_STATUS_MAP } from '@/utils/constants'
import { formatDate, formatMoney } from '@/utils/format'

const loading = ref(false)
const submitLoading = ref(false)
const list = ref([])
const total = ref(0)
const stats = ref({})
const rooms = ref([])
const engineeringUsers = ref([])
const createDialogVisible = ref(false)
const assignDialogVisible = ref(false)
const finishDialogVisible = ref(false)
const createFormRef = ref(null)
const finishFormRef = ref(null)

const query = reactive({
  page: 1,
  size: 10,
  status: '',
  keyword: ''
})

const createForm = reactive({
  roomId: null,
  description: ''
})

const createRules = {
  roomId: [{ required: true, message: '请选择房间', trigger: 'change' }],
  description: [{ required: true, message: '请输入问题描述', trigger: 'blur' }]
}

const assignForm = reactive({
  id: null,
  roomNo: '',
  assigneeId: null,
  assigneeName: ''
})

const finishForm = reactive({
  id: null,
  roomNo: '',
  solution: '',
  cost: 0
})

const finishRules = {
  solution: [{ required: true, message: '请输入解决方案', trigger: 'blur' }],
  cost: [{ required: true, message: '请输入维修费用', trigger: 'change' }]
}

const createDialogTitle = ref('创建工单')

function loadList() {
  loading.value = true
  listMaintenanceOrders(query).then(data => {
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

function calculateStats(orders) {
  const s = { pending: 0, assigned: 0, processing: 0, completed: 0 }
  orders.forEach(o => {
    if (s[o.status] !== undefined) s[o.status]++
  })
  stats.value = s
}

function loadRooms() {
  listRooms({ page: 1, size: 200 }).then(data => {
    rooms.value = data?.list || []
  }).catch(() => {
    rooms.value = []
  })
}

function loadEngineeringUsers() {
  listUsers({ page: 1, size: 100, role: 'engineering' }).then(data => {
    engineeringUsers.value = data?.list || []
  }).catch(() => {
    engineeringUsers.value = []
  })
}

function handleSearch() {
  query.page = 1
  loadList()
}

function handleReset() {
  query.page = 1
  query.status = ''
  query.keyword = ''
  loadList()
}

function handlePageChange(page) {
  query.page = page
  loadList()
}

function openCreateDialog() {
  createDialogTitle.value = '创建工单'
  createForm.roomId = null
  createForm.description = ''
  createDialogVisible.value = true
}

function submitCreate() {
  createFormRef.value.validate(valid => {
    if (!valid) return
    submitLoading.value = true
    createMaintenanceOrder(createForm).then(() => {
      ElMessage.success('创建成功')
      createDialogVisible.value = false
      loadList()
    }).catch(() => {}).finally(() => {
      submitLoading.value = false
    })
  })
}

function handleAssigneeChange(userId) {
  const selected = engineeringUsers.value.find(u => u.id === userId)
  assignForm.assigneeName = selected ? (selected.realName || selected.username) : ''
}

function openAssignDialog(row) {
  assignForm.id = row.id
  assignForm.roomNo = row.roomNo
  assignForm.assigneeId = null
  assignForm.assigneeName = ''
  assignDialogVisible.value = true
}

function submitAssign() {
  if (!assignForm.assigneeId) {
    ElMessage.warning('请选择维修人员')
    return
  }
  submitLoading.value = true
  assignMaintenanceOrder(assignForm.id, {
    assigneeId: assignForm.assigneeId,
    assigneeName: assignForm.assigneeName
  }).then(() => {
    ElMessage.success('分配成功')
    assignDialogVisible.value = false
    loadList()
  }).catch(() => {}).finally(() => {
    submitLoading.value = false
  })
}

function handleStart(row) {
  ElMessageBox.confirm('确认开始维修？', '提示', { type: 'warning' }).then(() => {
    startMaintenanceOrder(row.id).then(() => {
      ElMessage.success('已开始维修')
      loadList()
    }).catch(() => {})
  }).catch(() => {})
}

function openFinishDialog(row) {
  finishForm.id = row.id
  finishForm.roomNo = row.roomNo
  finishForm.solution = ''
  finishForm.cost = 0
  finishDialogVisible.value = true
}

function submitFinish() {
  finishFormRef.value.validate(valid => {
    if (!valid) return
    submitLoading.value = true
    finishMaintenanceOrder(finishForm.id, {
      solution: finishForm.solution,
      cost: finishForm.cost
    }).then(() => {
      ElMessage.success('已完成维修')
      finishDialogVisible.value = false
      loadList()
    }).catch(() => {}).finally(() => {
      submitLoading.value = false
    })
  })
}

function handleVerify(row) {
  ElMessageBox.confirm('确认验收通过？', '提示', { type: 'warning' }).then(() => {
    verifyMaintenanceOrder(row.id).then(() => {
      ElMessage.success('验收通过')
      loadList()
    }).catch(() => {})
  }).catch(() => {})
}

function handleCancel(row) {
  ElMessageBox.confirm('确认取消该工单？', '提示', { type: 'warning' }).then(() => {
    cancelMaintenanceOrder(row.id).then(() => {
      ElMessage.success('已取消工单')
      loadList()
    }).catch(() => {})
  }).catch(() => {})
}

onMounted(() => {
  loadList()
  loadRooms()
  loadEngineeringUsers()
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
