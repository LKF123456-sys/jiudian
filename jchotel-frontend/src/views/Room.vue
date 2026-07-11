<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">客房管理</span>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>新增客房
      </el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="query.keyword" placeholder="房间号" clearable style="width: 200px;"></el-input>
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px;">
        <el-option label="空闲" value="idle"></el-option>
        <el-option label="入住中" value="occupied"></el-option>
        <el-option label="维修中" value="maintenance"></el-option>
      </el-select>
      <el-select v-model="query.typeId" placeholder="房型" clearable style="width: 140px;">
        <el-option v-for="t in roomTypes" :key="t.id" :label="t.name" :value="t.id"></el-option>
      </el-select>
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon>查询
      </el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <div class="table-card">
      <el-table :data="list" stripe>
        <el-table-column prop="roomNo" label="房间号" width="100"></el-table-column>
        <el-table-column prop="typeName" label="房型"></el-table-column>
        <el-table-column prop="floor" label="楼层"></el-table-column>
        <el-table-column prop="price" label="价格">
          <template #default="scope">¥{{ formatMoney(scope.row.price) }}</template>
        </el-table-column>
        <el-table-column label="状态">
          <template #default="scope">
            <el-tag :type="statusInfo(scope.row.status).type" size="small">{{ statusInfo(scope.row.status).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip></el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="scope">
            <el-button type="text" @click="openDialog(scope.row)">编辑</el-button>
            <el-tooltip v-if="scope.row.status === 'occupied'" content="入住中房间无法设为维修" placement="top">
              <el-button type="text" disabled>{{ scope.row.status === 'maintenance' ? '恢复空闲' : '设为维修' }}</el-button>
            </el-tooltip>
            <el-button v-else type="text" @click="setStatus(scope.row)">{{ scope.row.status === 'maintenance' ? '恢复空闲' : '设为维修' }}</el-button>
            <el-tooltip v-if="scope.row.status === 'occupied'" content="入住中房间无法删除" placement="top">
              <el-button type="text" style="color: #f5222d;" disabled>删除</el-button>
            </el-tooltip>
            <el-button v-else type="text" style="color: #f5222d;" @click="handleDelete(scope.row)">删除</el-button>
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
      <el-form :model="form" :rules="rules" ref="roomForm" label-width="80px">
        <el-form-item label="房间号" prop="roomNo">
          <el-input v-model="form.roomNo"></el-input>
        </el-form-item>
        <el-form-item label="房型" prop="typeId">
          <el-select v-model="form.typeId" style="width: 100%;">
            <el-option v-for="t in roomTypes" :key="t.id" :label="t.name" :value="t.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="楼层" prop="floor">
          <el-input-number v-model="form.floor" :min="1" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width: 100%;" :disabled="isEdit && form.status === 'occupied'">
            <el-option label="空闲" value="idle"></el-option>
            <el-option label="入住中" value="occupied"></el-option>
            <el-option label="维修中" value="maintenance"></el-option>
          </el-select>
          <div v-if="isEdit && form.status === 'occupied'" style="font-size: 12px; color: #909399; margin-top: 4px;">入住中状态不可修改，需通过退房流程变更</div>
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
import { listRooms, addRoom, updateRoom, deleteRoom, updateRoomStatus, listRoomTypes } from '@/api/room'
import { formatMoney } from '@/utils/format'
import { ROOM_STATUS_MAP } from '@/utils/constants'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'Room',
  data() {
    return {
      query: { page: 1, size: 10, keyword: '', status: '', typeId: '' },
      list: [],
      total: 0,
      roomTypes: [],
      dialogVisible: false,
      isEdit: false,
      form: { roomNo: '', typeId: '', floor: 1, price: 0, status: 'idle', remark: '' },
      rules: {
        roomNo: [{ required: true, message: '请输入房间号', trigger: 'blur' }],
        typeId: [{ required: true, message: '请选择房型', trigger: 'change' }],
        floor: [{ required: true, message: '请输入楼层', trigger: 'change' }],
        price: [{ required: true, message: '请输入价格', trigger: 'change' }],
        status: [{ required: true, message: '请选择状态', trigger: 'change' }]
      }
    }
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '编辑客房' : '新增客房'
    }
  },
  created() {
    this.loadRoomTypes()
    this.loadList()
  },
  methods: {
    loadRoomTypes() {
      listRoomTypes().then(data => {
        this.roomTypes = data || []
      }).catch(() => {
        this.roomTypes = []
      })
    },
    loadList() {
      listRooms(this.query).then(data => {
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
      this.query = { page: 1, size: 10, keyword: '', status: '', typeId: '' }
      this.loadList()
    },
    handlePageChange(page) {
      this.query.page = page
      this.loadList()
    },
    statusInfo(status) {
      return ROOM_STATUS_MAP[status] || { label: status, type: 'info' }
    },
    formatMoney,
    openDialog(row) {
      this.dialogVisible = true
      this.isEdit = !!row
      if (row) {
        this.form = { ...row }
      } else {
        this.form = { roomNo: '', typeId: '', floor: 1, price: 0, status: 'idle', remark: '' }
      }
      this.$nextTick(() => {
        this.$refs.roomForm && this.$refs.roomForm.clearValidate()
      })
    },
    submitForm() {
      this.$refs.roomForm.validate(valid => {
        if (!valid) return
        const submitData = {
          roomNo: this.form.roomNo,
          typeId: this.form.typeId,
          floor: this.form.floor,
          price: this.form.price,
          status: this.form.status,
          remark: this.form.remark
        }
        const api = this.isEdit ? updateRoom(this.form.id, submitData) : addRoom(submitData)
        api.then(() => {
          ElMessage.success(this.isEdit ? '修改成功' : '新增成功')
          this.dialogVisible = false
          this.loadList()
        }).catch(() => {})
      })
    },
    setStatus(row) {
      if (row.status === 'occupied') {
        ElMessage.warning('入住中的房间不能设为维修，请先办理退房')
        return
      }
      const newStatus = row.status === 'maintenance' ? 'idle' : 'maintenance'
      const actionText = newStatus === 'maintenance' ? '设为维修' : '恢复空闲'
      ElMessageBox.confirm(`确认将该房间${actionText}？`, '提示', { type: 'warning' }).then(() => {
        updateRoomStatus(row.id, newStatus).then(() => {
          ElMessage.success('状态更新成功')
          this.loadList()
        }).catch(() => {})
      }).catch(() => {})
    },
    handleDelete(row) {
      const id = typeof row === 'object' ? row.id : row
      const status = typeof row === 'object' ? row.status : null
      if (status === 'occupied') {
        ElMessage.warning('入住中的房间不能删除，请先办理退房')
        return
      }
      ElMessageBox.confirm('确认删除该客房？', '提示', { type: 'warning' }).then(() => {
        deleteRoom(id).then(() => {
          ElMessage.success('删除成功')
          this.loadList()
        }).catch(() => {})
      }).catch(() => {})
    }
  }
}
</script>
