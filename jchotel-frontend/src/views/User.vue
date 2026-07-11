<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">用户管理</span>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>新增用户
      </el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="query.keyword" placeholder="用户名/姓名" clearable style="width: 200px;"></el-input>
      <el-select v-model="query.role" placeholder="角色" clearable style="width: 140px;">
        <el-option label="系统管理员" value="admin"></el-option>
        <el-option label="运营经理" value="manager"></el-option>
        <el-option label="前台接待" value="receptionist"></el-option>
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px;">
        <el-option label="启用" value="1"></el-option>
        <el-option label="禁用" value="0"></el-option>
      </el-select>
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon>查询
      </el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <div class="table-card">
      <el-table :data="list" stripe>
        <el-table-column prop="username" label="用户名" width="140"></el-table-column>
        <el-table-column prop="realName" label="姓名" width="120"></el-table-column>
        <el-table-column label="角色" width="120">
          <template #default="scope">
            <el-tag :type="roleTagType(scope.row.role)" size="small">
              {{ roleText(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-switch
              :model-value="scope.row.status === 1"
              @change="handleToggleStatus(scope.row)"
              active-text="启用"
              inactive-text="禁用"
              inline-prompt
            ></el-switch>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="scope">{{ formatTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" min-width="240">
          <template #default="scope">
            <el-button type="text" @click="openDialog(scope.row)">编辑</el-button>
            <el-button type="text" @click="handleResetPassword(scope.row)">重置密码</el-button>
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

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="480px">
      <el-form :model="form" :rules="rules" ref="userForm" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" placeholder="登录账号"></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="真实姓名"></el-input>
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width: 100%;" placeholder="请选择角色">
            <el-option label="系统管理员" value="admin"></el-option>
            <el-option label="运营经理" value="manager"></el-option>
            <el-option label="前台接待" value="receptionist"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item v-if="!isEdit" label="初始密码">
          <el-input value="123456" disabled></el-input>
          <div style="color: #999; font-size: 12px; margin-top: 4px;">新增用户默认密码为 123456，可登录后修改</div>
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
import { listUsers, addUser, updateUser, deleteUser, toggleUserStatus, resetUserPassword } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'UserManage',
  data() {
    return {
      query: { page: 1, size: 10, keyword: '', role: '', status: '' },
      list: [],
      total: 0,
      dialogVisible: false,
      isEdit: false,
      form: { username: '', realName: '', role: 'receptionist' },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
        ],
        realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        role: [{ required: true, message: '请选择角色', trigger: 'change' }]
      }
    }
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '编辑用户' : '新增用户'
    }
  },
  created() {
    this.loadList()
  },
  methods: {
    loadList() {
      listUsers(this.query).then(data => {
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
      this.query = { page: 1, size: 10, keyword: '', role: '', status: '' }
      this.loadList()
    },
    handlePageChange(page) {
      this.query.page = page
      this.loadList()
    },
    roleText(role) {
      const map = { admin: '系统管理员', manager: '运营经理', receptionist: '前台接待' }
      return map[role] || role
    },
    roleTagType(role) {
      const map = { admin: 'danger', manager: 'warning', receptionist: '' }
      return map[role] || 'info'
    },
    formatTime(time) {
      if (!time) return '-'
      return time.replace('T', ' ').substring(0, 19)
    },
    openDialog(row) {
      this.dialogVisible = true
      this.isEdit = !!row
      if (row) {
        this.form = {
          id: row.id,
          username: row.username,
          realName: row.realName,
          role: row.role
        }
      } else {
        this.form = { username: '', realName: '', role: 'receptionist' }
      }
      this.$nextTick(() => {
        this.$refs.userForm && this.$refs.userForm.clearValidate()
      })
    },
    submitForm() {
      this.$refs.userForm.validate(valid => {
        if (!valid) return
        const submitData = {
          realName: this.form.realName,
          role: this.form.role
        }
        let api
        if (this.isEdit) {
          api = updateUser(this.form.id, submitData)
        } else {
          submitData.username = this.form.username
          api = addUser(submitData)
        }
        api.then(() => {
          ElMessage.success(this.isEdit ? '修改成功' : '新增成功，默认密码：123456')
          this.dialogVisible = false
          this.loadList()
        }).catch(() => {})
      })
    },
    handleToggleStatus(row) {
      const newStatus = row.status === 1 ? 0 : 1
      const actionText = newStatus === 1 ? '启用' : '禁用'
      ElMessageBox.confirm(`确认${actionText}用户「${row.realName || row.username}」？`, '提示', { type: 'warning' }).then(() => {
        toggleUserStatus(row.id, newStatus).then(() => {
          ElMessage.success(`${actionText}成功`)
          this.loadList()
        })
      }).catch(() => {})
    },
    handleResetPassword(row) {
      ElMessageBox.confirm(`确认将用户「${row.realName || row.username}」的密码重置为 123456？`, '重置密码', { type: 'warning' }).then(() => {
        resetUserPassword(row.id).then(() => {
          ElMessage.success('密码已重置为 123456')
        })
      }).catch(() => {})
    },
    handleDelete(row) {
      ElMessageBox.confirm(`确认删除用户「${row.realName || row.username}」？此操作不可恢复。`, '警告', { type: 'warning' }).then(() => {
        deleteUser(row.id).then(() => {
          ElMessage.success('删除成功')
          this.loadList()
        })
      }).catch(() => {})
    }
  }
}
</script>
