<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">个人中心</span>
    </div>

    <div class="profile-card">
      <div class="profile-info">
        <div class="avatar">
          <el-icon><UserFilled /></el-icon>
        </div>
        <div class="info">
          <div class="name">{{ authStore.user.realName || authStore.user.username }}</div>
          <div class="role">{{ roleLabel }}</div>
          <div class="username">账号：{{ authStore.user.username }}</div>
        </div>
      </div>
    </div>

    <div class="password-card">
      <div class="card-title">修改密码</div>
      <el-form :model="form" :rules="rules" ref="passwordForm" label-width="120px" style="max-width: 500px;">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="form.oldPassword" type="password" show-password></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="form.newPassword" type="password" show-password></el-input>
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitChange">确认修改</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { changePassword } from '@/api/auth'
import { useAuthStore } from '@/store/auth'
import { ElMessage } from 'element-plus'

export default {
  name: 'Profile',
  data() {
    const validateConfirm = (rule, value, callback) => {
      if (value !== this.form.newPassword) {
        callback(new Error('两次输入的新密码不一致'))
      } else {
        callback()
      }
    }
    return {
      form: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      rules: {
        oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '新密码长度不能少于6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认新密码', trigger: 'blur' },
          { validator: validateConfirm, trigger: 'blur' }
        ]
      },
      authStore: useAuthStore()
    }
  },
  computed: {
    roleLabel() {
      const map = { admin: '系统管理员', manager: '运营经理', receptionist: '前台接待' }
      return map[this.authStore.user.role] || '未知'
    }
  },
  methods: {
    submitChange() {
      this.$refs.passwordForm.validate(valid => {
        if (!valid) return
        changePassword({
          oldPassword: this.form.oldPassword,
          newPassword: this.form.newPassword
        }).then(() => {
          ElMessage.success('密码修改成功，请重新登录')
          this.authStore.clearAuth()
          this.$router.replace('/login')
        }).catch(() => {})
      })
    },
    resetForm() {
      this.$refs.passwordForm.resetFields()
    }
  }
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}
.page-header {
  margin-bottom: 20px;
}
.page-title {
  font-size: 20px;
  font-weight: bold;
  color: #1e3a5f;
}
.profile-card,
.password-card {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
}
.profile-info {
  display: flex;
  align-items: center;
}
.avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: #1e3a5f;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  margin-right: 20px;
}
.info .name {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 6px;
}
.info .role {
  font-size: 14px;
  color: #d4af37;
  margin-bottom: 6px;
}
.info .username {
  font-size: 14px;
  color: #909399;
}
.card-title {
  font-size: 16px;
  font-weight: bold;
  color: #1e3a5f;
  margin-bottom: 20px;
}
</style>
