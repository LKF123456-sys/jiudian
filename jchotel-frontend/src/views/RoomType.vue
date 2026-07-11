<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">房型维护</span>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>新增房型
      </el-button>
    </div>

    <div class="table-card">
      <el-table :data="list" stripe>
        <el-table-column prop="name" label="房型名称"></el-table-column>
        <el-table-column prop="bedType" label="床型"></el-table-column>
        <el-table-column prop="capacity" label="容纳人数"></el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button type="text" @click="openDialog(scope.row)">编辑</el-button>
            <el-button type="text" style="color: #f5222d;" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="typeForm" label-width="100px">
        <el-form-item label="房型名称" prop="name">
          <el-input v-model="form.name"></el-input>
        </el-form-item>
        <el-form-item label="床型" prop="bedType">
          <el-input v-model="form.bedType"></el-input>
        </el-form-item>
        <el-form-item label="容纳人数" prop="capacity">
          <el-input-number v-model="form.capacity" :min="1" style="width: 100%;"></el-input-number>
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
import { listRoomTypes, addRoomType, updateRoomType, deleteRoomType } from '@/api/room'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'RoomType',
  data() {
    return {
      list: [],
      dialogVisible: false,
      isEdit: false,
      form: { name: '', bedType: '', capacity: 1 },
      rules: {
        name: [{ required: true, message: '请输入房型名称', trigger: 'blur' }],
        bedType: [{ required: true, message: '请输入床型', trigger: 'blur' }],
        capacity: [{ required: true, message: '请输入容纳人数', trigger: 'change' }]
      }
    }
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '编辑房型' : '新增房型'
    }
  },
  created() {
    this.loadList()
  },
  methods: {
    loadList() {
      listRoomTypes().then(data => {
        this.list = data
      })
    },
    openDialog(row) {
      this.dialogVisible = true
      this.isEdit = !!row
      this.form = row ? { ...row } : { name: '', bedType: '', capacity: 1 }
      this.$nextTick(() => {
        this.$refs.typeForm && this.$refs.typeForm.clearValidate()
      })
    },
    submitForm() {
      this.$refs.typeForm.validate(valid => {
        if (!valid) return
        const api = this.isEdit ? updateRoomType(this.form.id, this.form) : addRoomType(this.form)
        api.then(() => {
          ElMessage.success(this.isEdit ? '修改成功' : '新增成功')
          this.dialogVisible = false
          this.loadList()
        })
      })
    },
    handleDelete(id) {
      ElMessageBox.confirm('确认删除该房型？', '提示', { type: 'warning' }).then(() => {
        deleteRoomType(id).then(() => {
          ElMessage.success('删除成功')
          this.loadList()
        })
      })
    }
  }
}
</script>
