<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">客户管理</span>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>新增客户
      </el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="query.keyword" placeholder="姓名/手机号" clearable style="width: 220px;"></el-input>
      <el-select v-model="query.vipLevel" placeholder="VIP等级" clearable style="width: 140px;">
        <el-option label="普通会员" :value="0"></el-option>
        <el-option label="银卡会员" :value="1"></el-option>
        <el-option label="金卡会员" :value="2"></el-option>
        <el-option label="钻石会员" :value="3"></el-option>
      </el-select>
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon>查询
      </el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <div class="table-card">
      <el-table :data="list" stripe>
        <el-table-column prop="name" label="姓名"></el-table-column>
        <el-table-column prop="phone" label="手机号"></el-table-column>
        <el-table-column prop="idCard" label="身份证号" width="180"></el-table-column>
        <el-table-column label="性别">
          <template #default="scope">{{ genderText(scope.row.gender) }}</template>
        </el-table-column>
        <el-table-column label="VIP等级">
          <template #default="scope">
            <el-tag v-if="scope.row.vipLevel > 0" :type="vipType(scope.row.vipLevel)" size="small">{{ vipText(scope.row.vipLevel) }}</el-tag>
            <span v-else>普通会员</span>
          </template>
        </el-table-column>
        <el-table-column prop="checkInCount" label="入住次数"></el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip></el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button type="text" @click="openDialog(scope.row)">编辑</el-button>
            <el-button type="text" style="color: #f5222d;" @click="handleDelete(scope.row.id)">删除</el-button>
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
      <el-form :model="form" :rules="rules" ref="customerForm" label-width="90px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone"></el-input>
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input v-model="form.idCard"></el-input>
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio label="M">男</el-radio>
            <el-radio label="F">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="VIP等级">
          <el-select v-model="form.vipLevel" style="width: 100%;">
            <el-option label="普通会员" :value="0"></el-option>
            <el-option label="银卡会员" :value="1"></el-option>
            <el-option label="金卡会员" :value="2"></el-option>
            <el-option label="钻石会员" :value="3"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { listCustomers, addCustomer, updateCustomer, deleteCustomer } from '@/api/customer'
import { VIP_LEVEL_MAP } from '@/utils/constants'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'Customer',
  data() {
    return {
      query: { page: 1, size: 10, keyword: '', vipLevel: '' },
      list: [],
      total: 0,
      dialogVisible: false,
      isEdit: false,
      form: { name: '', phone: '', idCard: '', gender: 'M', vipLevel: 0, remark: '' },
      rules: {
        name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
      }
    }
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '编辑客户' : '新增客户'
    }
  },
  created() {
    this.loadList()
  },
  methods: {
    loadList() {
      listCustomers(this.query).then(data => {
        this.list = data?.list || []
        this.total = data?.total || 0
      }).catch(() => {
        this.list = []
        this.total = 0
      })
    },
    handleSearch() {
      this.query.page = 1
      this.loadList()
    },
    handleReset() {
      this.query = { page: 1, size: 10, keyword: '', vipLevel: '' }
      this.loadList()
    },
    handlePageChange(page) {
      this.query.page = page
      this.loadList()
    },
    genderText(gender) {
      if (gender === 'M') return '男'
      if (gender === 'F') return '女'
      return '-'
    },
    vipText(level) {
      return VIP_LEVEL_MAP[level] || '普通会员'
    },
    vipType(level) {
      if (level === 1) return 'info'
      if (level === 2) return 'warning'
      if (level === 3) return 'danger'
      return ''
    },
    openDialog(row) {
      this.dialogVisible = true
      this.isEdit = !!row
      if (row) {
        this.form = {
          id: row.id,
          name: row.name,
          phone: row.phone,
          idCard: row.idCard,
          gender: row.gender,
          vipLevel: row.vipLevel,
          remark: row.remark
        }
      } else {
        this.form = { name: '', phone: '', idCard: '', gender: 'M', vipLevel: 0, remark: '' }
      }
      this.$nextTick(() => {
        this.$refs.customerForm && this.$refs.customerForm.clearValidate()
      })
    },
    submitForm() {
      this.$refs.customerForm.validate(valid => {
        if (!valid) return
        const submitData = {
          name: this.form.name,
          phone: this.form.phone,
          idCard: this.form.idCard,
          gender: this.form.gender,
          vipLevel: this.form.vipLevel,
          remark: this.form.remark
        }
        const api = this.isEdit ? updateCustomer(this.form.id, submitData) : addCustomer(submitData)
        api.then(() => {
          ElMessage.success(this.isEdit ? '修改成功' : '新增成功')
          this.dialogVisible = false
          this.loadList()
        }).catch(() => {})
      })
    },
    handleDelete(id) {
      ElMessageBox.confirm('确认删除该客户？', '提示', { type: 'warning' }).then(() => {
        deleteCustomer(id).then(() => {
          ElMessage.success('删除成功')
          this.loadList()
        }).catch(() => {})
      }).catch(() => {})
    }
  }
}
</script>
