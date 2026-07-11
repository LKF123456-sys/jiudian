<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">消费品管理</span>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>新增项目
      </el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="query.keyword" placeholder="名称" clearable style="width: 200px;"></el-input>
      <el-select v-model="query.category" placeholder="分类" clearable style="width: 140px;">
        <el-option v-for="c in categories" :key="c" :label="c" :value="c"></el-option>
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px;">
        <el-option label="启用" :value="1"></el-option>
        <el-option label="禁用" :value="0"></el-option>
      </el-select>
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon>查询
      </el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <div class="table-card">
      <el-table :data="list" stripe v-loading="loading">
        <el-table-column prop="name" label="名称"></el-table-column>
        <el-table-column label="价格" width="120">
          <template #default="scope">¥{{ formatMoney(scope.row.price) }}</template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="120"></el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.enabled ? 'success' : 'info'" size="small">
              {{ scope.row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip></el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button type="text" @click="openDialog(scope.row)">编辑</el-button>
            <el-button type="text" @click="toggleStatus(scope.row)">
              {{ scope.row.enabled ? '禁用' : '启用' }}
            </el-button>
            <el-button type="text" style="color: #f5222d;" @click="handleDelete(scope.row)">删除</el-button>
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

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入名称"></el-input>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%;">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="enabled">
          <el-switch v-model="form.enabled" active-text="启用" inactive-text="禁用"></el-switch>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { listChargeItems, addChargeItem, updateChargeItem, deleteChargeItem } from '@/api/chargeItem'
import { formatMoney } from '@/utils/format'

const categories = ['食品', '饮料', '日用品', '其他']

const loading = ref(false)
const submitLoading = ref(false)
const list = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const query = reactive({
  page: 1,
  size: 10,
  keyword: '',
  category: '',
  status: ''
})

const form = reactive({
  id: null,
  name: '',
  price: 0,
  category: '食品',
  enabled: true,
  remark: ''
})

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'change' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑项目' : '新增项目')

function loadList() {
  loading.value = true
  const params = { ...query }
  if (params.status !== '' && params.status !== null && params.status !== undefined) {
    params.enabled = params.status
  }
  listChargeItems(params).then(data => {
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
  query.category = ''
  query.status = ''
  loadList()
}

function handlePageChange(page) {
  query.page = page
  loadList()
}

function openDialog(row) {
  dialogVisible.value = true
  isEdit.value = !!row
  if (row) {
    form.id = row.id
    form.name = row.name
    form.price = row.price
    form.category = row.category
    form.enabled = row.enabled
    form.remark = row.remark || ''
  } else {
    form.id = null
    form.name = ''
    form.price = 0
    form.category = '食品'
    form.enabled = true
    form.remark = ''
  }
}

function submitForm() {
  formRef.value.validate(valid => {
    if (!valid) return
    submitLoading.value = true
    const submitData = {
      name: form.name,
      price: form.price,
      category: form.category,
      enabled: form.enabled,
      remark: form.remark
    }
    const api = isEdit.value ? updateChargeItem(form.id, submitData) : addChargeItem(submitData)
    api.then(() => {
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
      dialogVisible.value = false
      loadList()
    }).catch(() => {}).finally(() => {
      submitLoading.value = false
    })
  })
}

function toggleStatus(row) {
  const newStatus = !row.enabled
  const action = newStatus ? '启用' : '禁用'
  ElMessageBox.confirm(`确认${action}该项目？`, '提示', { type: 'warning' }).then(() => {
    updateChargeItem(row.id, { enabled: newStatus }).then(() => {
      ElMessage.success(`${action}成功`)
      loadList()
    }).catch(() => {})
  }).catch(() => {})
}

function handleDelete(row) {
  ElMessageBox.confirm('确认删除该项目？', '提示', { type: 'warning' }).then(() => {
    deleteChargeItem(row.id).then(() => {
      ElMessage.success('删除成功')
      loadList()
    }).catch(() => {})
  }).catch(() => {})
}

onMounted(() => {
  loadList()
})
</script>
