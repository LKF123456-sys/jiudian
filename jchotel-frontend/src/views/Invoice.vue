<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">发票管理</span>
      <el-button type="primary" @click="openCreateDialog()">
        <el-icon><Plus /></el-icon>开具发票
      </el-button>
    </div>

    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-label">本月开票金额</div>
          <div class="stat-value" style="color: #409EFF;">¥{{ formatMoney(stats.monthAmount || 0) }}</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-label">本月开票数</div>
          <div class="stat-value" style="color: #67C23A;">{{ stats.monthCount || 0 }}</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-label">总开票金额</div>
          <div class="stat-value" style="color: #E6A23C;">¥{{ formatMoney(stats.totalAmount || 0) }}</div>
        </div>
      </el-col>
    </el-row>

    <div class="search-bar">
      <el-input v-model="query.keyword" placeholder="发票号/订单号/客户名称" clearable style="width: 250px;"></el-input>
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px;">
        <el-option v-for="(info, key) in INVOICE_STATUS_MAP" :key="key" :label="info.label" :value="key"></el-option>
      </el-select>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        style="width: 260px;"
        value-format="YYYY-MM-DD"
      ></el-date-picker>
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon>查询
      </el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <div class="table-card">
      <el-table :data="list" stripe v-loading="loading">
        <el-table-column prop="invoiceNo" label="发票号" width="160"></el-table-column>
        <el-table-column prop="orderNo" label="订单号" width="160"></el-table-column>
        <el-table-column prop="customerName" label="客户名称" show-overflow-tooltip></el-table-column>
        <el-table-column label="金额" width="100">
          <template #default="scope">¥{{ formatMoney(scope.row.amount) }}</template>
        </el-table-column>
        <el-table-column prop="title" label="抬头" show-overflow-tooltip></el-table-column>
        <el-table-column prop="taxNo" label="税号" width="180" show-overflow-tooltip></el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="INVOICE_STATUS_MAP[scope.row.status]?.type" size="small">
              {{ INVOICE_STATUS_MAP[scope.row.status]?.label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="开具时间" width="160">
          <template #default="scope">{{ formatDate(scope.row.issueTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 'issued'"
              type="text"
              style="color: #f5222d;"
              @click="openCancelDialog(scope.row)"
            >作废</el-button>
            <el-button
              v-if="scope.row.status === 'issued'"
              type="text"
              @click="openRedDialog(scope.row)"
            >红冲</el-button>
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

    <el-dialog title="开具发票" v-model="createDialogVisible" width="600px">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="100px">
        <el-form-item label="订单号" prop="orderNo">
          <div style="display: flex; gap: 8px; width: 100%;">
            <el-input v-model="createForm.orderNo" placeholder="请输入订单号" style="flex: 1;"></el-input>
            <el-button @click="queryOrder">查询</el-button>
          </div>
        </el-form-item>
        <el-form-item label="订单信息" v-if="orderInfo">
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="客户">{{ orderInfo.customerName }}</el-descriptions-item>
            <el-descriptions-item label="房间">{{ orderInfo.roomNo }}</el-descriptions-item>
            <el-descriptions-item label="金额">¥{{ formatMoney(orderInfo.totalAmount) }}</el-descriptions-item>
          </el-descriptions>
        </el-form-item>
        <el-form-item label="发票抬头" prop="title">
          <el-input v-model="createForm.title" placeholder="请输入发票抬头"></el-input>
        </el-form-item>
        <el-form-item label="税号">
          <el-input v-model="createForm.taxNo" placeholder="请输入税号（可选）"></el-input>
        </el-form-item>
        <el-form-item label="开票金额" prop="amount">
          <el-input-number v-model="createForm.amount" :min="0" :precision="2" style="width: 100%;"></el-input-number>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate" :loading="submitLoading">开具</el-button>
      </template>
    </el-dialog>

    <el-dialog :title="actionDialogTitle" v-model="actionDialogVisible" width="450px">
      <el-form :model="actionForm" label-width="80px">
        <el-form-item label="发票号">
          <el-input v-model="actionForm.invoiceNo" disabled></el-input>
        </el-form-item>
        <el-form-item label="原因" required>
          <el-input v-model="actionForm.reason" type="textarea" :rows="4" placeholder="请输入原因"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="actionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAction" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { listInvoices, getInvoice, createInvoice, cancelInvoice, redInvoice } from '@/api/invoice'
import { getOrderByOrderNo } from '@/api/order'
import { INVOICE_STATUS_MAP } from '@/utils/constants'
import { formatDate, formatMoney } from '@/utils/format'

const loading = ref(false)
const submitLoading = ref(false)
const list = ref([])
const total = ref(0)
const stats = ref({})
const dateRange = ref([])
const createDialogVisible = ref(false)
const actionDialogVisible = ref(false)
const createFormRef = ref(null)
const orderInfo = ref(null)
const actionDialogTitle = ref('')
const actionType = ref('')

const query = reactive({
  page: 1,
  size: 10,
  keyword: '',
  status: '',
  startTime: '',
  endTime: ''
})

const createForm = reactive({
  orderId: null,
  orderNo: '',
  title: '',
  taxNo: '',
  amount: 0
})

const createRules = {
  orderNo: [{ required: true, message: '请输入订单号', trigger: 'blur' }],
  title: [{ required: true, message: '请输入发票抬头', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入开票金额', trigger: 'change' }]
}

const actionForm = reactive({
  id: null,
  invoiceNo: '',
  reason: ''
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
    status: query.status || undefined,
    startTime: query.startTime || undefined,
    endTime: query.endTime || undefined
  }
  listInvoices(params).then(data => {
    list.value = data?.list || []
    total.value = data?.total || 0
    stats.value = data?.stats || { monthAmount: 0, monthCount: 0, totalAmount: 0 }
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
  query.status = ''
  dateRange.value = []
  loadList()
}

function handlePageChange(page) {
  query.page = page
  loadList()
}

function openCreateDialog() {
  createForm.orderId = null
  createForm.orderNo = ''
  createForm.title = ''
  createForm.taxNo = ''
  createForm.amount = 0
  orderInfo.value = null
  createDialogVisible.value = true
}

function queryOrder() {
  if (!createForm.orderNo) {
    ElMessage.warning('请输入订单号')
    return
  }
  getOrderByOrderNo(createForm.orderNo.trim()).then(data => {
    orderInfo.value = data
    createForm.orderId = data.id
    createForm.amount = data.totalAmount || 0
    if (data.customerName) {
      createForm.title = data.customerName
    }
  }).catch(() => {
    orderInfo.value = null
    createForm.orderId = null
    ElMessage.error('未找到该订单，请确认订单号是否正确')
  })
}

function submitCreate() {
  createFormRef.value.validate(valid => {
    if (!valid) return
    if (!createForm.orderId) {
      ElMessage.warning('请先查询并选择有效订单')
      return
    }
    submitLoading.value = true
    createInvoice({
      orderId: createForm.orderId,
      title: createForm.title,
      taxNo: createForm.taxNo,
      amount: createForm.amount
    }).then(() => {
      ElMessage.success('开具成功')
      createDialogVisible.value = false
      loadList()
    }).catch(() => {}).finally(() => {
      submitLoading.value = false
    })
  })
}

function openCancelDialog(row) {
  actionType.value = 'cancel'
  actionDialogTitle.value = '作废发票'
  actionForm.id = row.id
  actionForm.invoiceNo = row.invoiceNo
  actionForm.reason = ''
  actionDialogVisible.value = true
}

function openRedDialog(row) {
  actionType.value = 'red'
  actionDialogTitle.value = '红冲发票'
  actionForm.id = row.id
  actionForm.invoiceNo = row.invoiceNo
  actionForm.reason = ''
  actionDialogVisible.value = true
}

function submitAction() {
  if (!actionForm.reason) {
    ElMessage.warning('请输入原因')
    return
  }
  submitLoading.value = true
  const api = actionType.value === 'cancel'
    ? cancelInvoice(actionForm.id, actionForm.reason)
    : redInvoice(actionForm.id, actionForm.reason)
  api.then(() => {
    ElMessage.success(actionType.value === 'cancel' ? '作废成功' : '红冲成功')
    actionDialogVisible.value = false
    loadList()
  }).catch(() => {}).finally(() => {
    submitLoading.value = false
  })
}

onMounted(() => {
  loadList()
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
