<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">订单管理</span>
    </div>

    <div class="search-bar">
      <el-input v-model="query.keyword" placeholder="客户姓名/订单号" clearable style="width: 200px;" @keyup.enter="handleSearch"></el-input>
      <el-select v-model="query.status" placeholder="订单状态" clearable style="width: 140px;">
        <el-option label="待入住" value="pending"></el-option>
        <el-option label="已入住" value="checkedIn"></el-option>
        <el-option label="已退房" value="checkedOut"></el-option>
        <el-option label="已取消" value="cancelled"></el-option>
      </el-select>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="入住开始"
        end-placeholder="入住结束"
        value-format="YYYY-MM-DD"
        style="width: 280px;"
        :shortcuts="dateShortcuts"
      ></el-date-picker>
      <el-button type="primary" :loading="loading" @click="handleSearch">
        <el-icon><Search /></el-icon>查询
      </el-button>
      <el-button @click="resetQuery">
        <el-icon><Refresh /></el-icon>重置
      </el-button>
    </div>

    <div class="table-card">
      <el-table :data="list" stripe v-loading="loading" element-loading-text="加载中...">
        <el-table-column prop="orderNo" label="订单号" width="170"></el-table-column>
        <el-table-column prop="customerName" label="客户" width="100"></el-table-column>
        <el-table-column prop="customerPhone" label="手机号" width="120"></el-table-column>
        <el-table-column prop="roomNo" label="房间号" width="90"></el-table-column>
        <el-table-column label="入住时间" width="155">
          <template #default="scope">{{ formatDate(scope.row.checkInTime) }}</template>
        </el-table-column>
        <el-table-column label="预计退房" width="155">
          <template #default="scope">{{ formatDate(scope.row.expectedCheckOutTime) }}</template>
        </el-table-column>
        <el-table-column label="押金" width="90">
          <template #default="scope">¥{{ formatMoney(scope.row.deposit) }}</template>
        </el-table-column>
        <el-table-column label="房费" width="90">
          <template #default="scope">¥{{ formatMoney(scope.row.roomAmount) }}</template>
        </el-table-column>
        <el-table-column label="消费" width="90">
          <template #default="scope">¥{{ formatMoney(scope.row.extraAmount) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="scope">
            <el-tag :type="statusInfo(scope.row.status).type" size="small">{{ statusInfo(scope.row.status).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="scope">
            <el-button type="text" @click="showDetail(scope.row)">详情</el-button>
            <el-button v-if="scope.row.status === 'pending'" type="text" style="color: #1890ff;" @click="handleSettle(scope.row)">入住</el-button>
            <el-button v-if="scope.row.status === 'pending'" type="text" style="color: #f5222d;" @click="handleCancel(scope.row)">取消</el-button>
            <el-button v-if="scope.row.status === 'checkedIn'" type="text" style="color: #67c23a;" @click="showCheckout(scope.row)">退房</el-button>
            <el-button v-if="scope.row.status === 'checkedIn'" type="text" style="color: #e6a23c;" @click="showExtendStay(scope.row)">续住</el-button>
            <el-button v-if="scope.row.status === 'checkedIn'" type="text" style="color: #909399;" @click="showChangeRoom(scope.row)">换房</el-button>
            <el-button v-if="scope.row.status === 'checkedIn'" type="text" style="color: #409eff;" @click="showAddItem(scope.row)">加消费</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && list.length === 0" description="暂无订单数据"></el-empty>
      <el-pagination
        style="margin-top: 20px; text-align: right;"
        :current-page="query.page"
        :page-size="query.size"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      ></el-pagination>
    </div>

    <el-dialog title="订单详情" v-model="detailVisible" width="680px">
      <el-descriptions :column="2" border v-if="detail">
        <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusInfo(detail.status).type" size="small">{{ statusInfo(detail.status).label }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="客户">{{ detail.customerName }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detail.customerPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="房间号">{{ detail.roomNo }}</el-descriptions-item>
        <el-descriptions-item label="房型">{{ detail.typeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="入住时间">{{ formatDate(detail.checkInTime) }}</el-descriptions-item>
        <el-descriptions-item label="预计退房">{{ formatDate(detail.expectedCheckOutTime) }}</el-descriptions-item>
        <el-descriptions-item label="实际退房">{{ detail.actualCheckOutTime ? formatDate(detail.actualCheckOutTime) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="押金">¥{{ formatMoney(detail.deposit) }}</el-descriptions-item>
        <el-descriptions-item label="房费">¥{{ formatMoney(detail.roomAmount) }}</el-descriptions-item>
        <el-descriptions-item label="附加消费">¥{{ formatMoney(detail.extraAmount) }}</el-descriptions-item>
        <el-descriptions-item label="结算金额" :span="2">¥{{ formatMoney(detail.totalAmount) }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="detail && detail.status === 'checkedIn'" style="margin-top: 16px;">
        <el-divider content-position="left">附加消费清单</el-divider>
        <el-button type="primary" size="small" @click="showAddItem(detail)" style="margin-bottom: 10px;">添加消费</el-button>
        <el-table :data="orderItems" size="small" border>
          <el-table-column prop="itemName" label="项目" width="150"></el-table-column>
          <el-table-column prop="price" label="单价" width="100">
            <template #default="scope">¥{{ formatMoney(scope.row.price) }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80"></el-table-column>
          <el-table-column prop="amount" label="金额" width="100">
            <template #default="scope">¥{{ formatMoney(scope.row.amount) }}</template>
          </el-table-column>
          <el-table-column prop="remark" label="备注"></el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="scope">
              <el-button type="text" style="color: #f5222d;" @click="handleDeleteItem(detail.id, scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <el-dialog title="办理退房" v-model="checkoutVisible" width="500px">
      <el-form :model="checkoutForm" label-width="100px" v-if="checkoutForm">
        <el-form-item label="订单号">{{ checkoutForm.orderNo }}</el-form-item>
        <el-form-item label="房间">{{ checkoutForm.roomNo }}</el-form-item>
        <el-form-item label="客户">{{ checkoutForm.customerName }}</el-form-item>
        <el-form-item label="房费">¥{{ formatMoney(checkoutForm.roomAmount) }}</el-form-item>
        <el-form-item label="附加消费">¥{{ formatMoney(checkoutForm.extraAmount) }}</el-form-item>
        <el-form-item label="应付总额" style="font-size: 18px; font-weight: bold; color: #e6a23c;">¥{{ formatMoney(checkoutForm.totalAmount) }}</el-form-item>
        <el-form-item label="已收押金">¥{{ formatMoney(checkoutForm.deposit) }}</el-form-item>
        <el-form-item label="应退押金">
          <span :style="{color: checkoutForm.refundAmount >= 0 ? '#67c23a' : '#f5222d'}">¥{{ formatMoney(Math.abs(checkoutForm.refundAmount)) }}</span>
          <span v-if="checkoutForm.refundAmount < 0" style="margin-left: 8px; color: #f5222d;">（需补收）</span>
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="checkoutForm.payMethod" style="width: 100%;">
            <el-option label="现金" value="cash"></el-option>
            <el-option label="微信" value="wechat"></el-option>
            <el-option label="支付宝" value="alipay"></el-option>
            <el-option label="银行卡" value="card"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="checkoutForm.remark" type="textarea" :rows="2" placeholder="可选"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="checkoutVisible = false">取消</el-button>
        <el-button type="primary" :loading="actionLoading" @click="handleCheckout">确认退房</el-button>
      </template>
    </el-dialog>

    <el-dialog title="续住办理" v-model="extendVisible" width="450px">
      <el-form :model="extendForm" label-width="100px">
        <el-form-item label="订单号">{{ extendForm.orderNo }}</el-form-item>
        <el-form-item label="原退房时间">{{ formatDate(extendForm.oldCheckOut) }}</el-form-item>
        <el-form-item label="新退房时间" required>
          <el-date-picker
            v-model="extendForm.newCheckOut"
            type="datetime"
            placeholder="选择新退房时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            :disabled-date="disabledAfterDate"
            style="width: 100%;"
          ></el-date-picker>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="extendVisible = false">取消</el-button>
        <el-button type="primary" :loading="actionLoading" @click="handleExtendStay">确认续住</el-button>
      </template>
    </el-dialog>

    <el-dialog title="换房办理" v-model="changeRoomVisible" width="450px">
      <el-form :model="changeRoomForm" label-width="100px">
        <el-form-item label="订单号">{{ changeRoomForm.orderNo }}</el-form-item>
        <el-form-item label="原房间">{{ changeRoomForm.oldRoomNo }}</el-form-item>
        <el-form-item label="新房间" required>
          <el-select v-model="changeRoomForm.newRoomId" filterable placeholder="选择空闲房间" style="width: 100%;" @change="onRoomChange">
            <el-option
              v-for="room in availableRooms"
              :key="room.id"
              :label="`${room.roomNo} - ${room.typeName} (¥${room.price}/晚)`"
              :value="room.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="换房原因">
          <el-input v-model="changeRoomForm.reason" type="textarea" :rows="2" placeholder="可选"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="changeRoomVisible = false">取消</el-button>
        <el-button type="primary" :loading="actionLoading" @click="handleChangeRoom">确认换房</el-button>
      </template>
    </el-dialog>

    <el-dialog title="添加附加消费" v-model="addItemVisible" width="480px">
      <el-form :model="itemForm" label-width="90px">
        <el-form-item label="消费项目" required>
          <el-select v-model="itemForm.chargeItemId" filterable placeholder="选择项目或自定义" style="width: 100%;" @change="onChargeItemChange" allow-create default-first-option>
            <el-option
              v-for="item in chargeItems"
              :key="item.id"
              :label="`${item.name} (¥${item.price})`"
              :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="项目名称" required>
          <el-input v-model="itemForm.itemName" placeholder="请输入项目名称"></el-input>
        </el-form-item>
        <el-form-item label="单价" required>
          <el-input-number v-model="itemForm.price" :min="0.01" :precision="2" :step="1" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="数量" required>
          <el-input-number v-model="itemForm.quantity" :min="1" :step="1" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="金额" style="font-weight: bold;">¥{{ formatMoney((itemForm.price || 0) * (itemForm.quantity || 1)) }}</el-form-item>
        <el-form-item label="备注">
          <el-input v-model="itemForm.remark" placeholder="可选"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addItemVisible = false">取消</el-button>
        <el-button type="primary" :loading="actionLoading" @click="handleAddItem">确认添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {
  listOrders, getOrder, cancelOrder, settlePending, checkout,
  extendStay, changeRoom, getOrderItems, addOrderItem, deleteOrderItem
} from '@/api/order'
import { getAvailableRooms } from '@/api/room'
import { listAllEnabledChargeItems } from '@/api/chargeItem'
import { formatDate, formatMoney } from '@/utils/format'
import { ORDER_STATUS_MAP } from '@/utils/constants'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'Order',
  data() {
    return {
      loading: false,
      actionLoading: false,
      query: {
        page: 1,
        size: 10,
        keyword: '',
        status: '',
        startTime: '',
        endTime: ''
      },
      dateRange: [],
      list: [],
      total: 0,
      detailVisible: false,
      detail: null,
      orderItems: [],
      checkoutVisible: false,
      checkoutForm: null,
      extendVisible: false,
      extendForm: {
        orderId: null,
        orderNo: '',
        oldCheckOut: null,
        newCheckOut: null
      },
      changeRoomVisible: false,
      changeRoomForm: {
        orderId: null,
        orderNo: '',
        oldRoomNo: '',
        newRoomId: null,
        reason: ''
      },
      availableRooms: [],
      addItemVisible: false,
      addItemOrderId: null,
      itemForm: {
        chargeItemId: null,
        itemName: '',
        price: 0,
        quantity: 1,
        remark: ''
      },
      chargeItems: [],
      dateShortcuts: [
        { text: '今天', value: () => { const d = new Date(); return [d, d] } },
        { text: '近7天', value: () => { const end = new Date(); const start = new Date(); start.setTime(start.getTime() - 3600 * 1000 * 24 * 7); return [start, end] } },
        { text: '近30天', value: () => { const end = new Date(); const start = new Date(); start.setTime(start.getTime() - 3600 * 1000 * 24 * 30); return [start, end] } },
        { text: '本月', value: () => { const now = new Date(); return [new Date(now.getFullYear(), now.getMonth(), 1), now] } }
      ]
    }
  },
  created() {
    this.loadList()
    this.loadChargeItems()
  },
  methods: {
    formatDate,
    formatMoney,
    statusInfo(status) {
      return ORDER_STATUS_MAP[status] || { label: status, type: 'info' }
    },
    async loadList() {
      this.loading = true
      if (this.dateRange && this.dateRange.length === 2) {
        this.query.startTime = this.dateRange[0] + ' 00:00:00'
        this.query.endTime = this.dateRange[1] + ' 23:59:59'
      } else {
        this.query.startTime = ''
        this.query.endTime = ''
      }
      try {
        const data = await listOrders(this.query)
        this.list = data?.list || []
        this.total = data?.total || 0
      } catch (e) {
        this.list = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },
    loadChargeItems() {
      listAllEnabledChargeItems().then(data => {
        this.chargeItems = data || []
      }).catch(() => {})
    },
    handleSearch() {
      this.query.page = 1
      this.loadList()
    },
    resetQuery() {
      this.query = {
        page: 1,
        size: 10,
        keyword: '',
        status: '',
        startTime: '',
        endTime: ''
      }
      this.dateRange = []
      this.loadList()
    },
    handlePageChange(page) {
      this.query.page = page
      this.loadList()
    },
    handleSizeChange(size) {
      this.query.size = size
      this.query.page = 1
      this.loadList()
    },
    async showDetail(row) {
      try {
        this.detail = await getOrder(row.id)
        if (this.detail && this.detail.status === 'checkedIn') {
          const items = await getOrderItems(row.id)
          this.orderItems = items || []
        } else {
          this.orderItems = []
        }
        this.detailVisible = true
      } catch (e) {}
    },
    handleCancel(row) {
      ElMessageBox.confirm(`取消后订单 ${row.orderNo} 将失效，确认取消？`, '提示', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await cancelOrder(row.id)
          ElMessage.success('订单已取消')
          this.loadList()
        } catch (e) {}
      }).catch(() => {})
    },
    handleSettle(row) {
      ElMessageBox.confirm(`确认将订单 ${row.orderNo} 办理入住？`, '办理入住', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'info'
      }).then(async () => {
        try {
          await settlePending(row.id)
          ElMessageBox.alert('办理入住成功', '提示', {
            confirmButtonText: '确定',
            type: 'success',
            center: true
          })
          this.loadList()
        } catch (e) {}
      }).catch(() => {})
    },
    showCheckout(row) {
      this.checkoutForm = {
        orderId: row.id,
        orderNo: row.orderNo,
        roomNo: row.roomNo,
        customerName: row.customerName,
        roomAmount: row.roomAmount || 0,
        extraAmount: row.extraAmount || 0,
        deposit: row.deposit || 0,
        payMethod: 'cash',
        remark: ''
      }
      const total = (this.checkoutForm.roomAmount + this.checkoutForm.extraAmount) - this.checkoutForm.deposit
      this.checkoutForm.totalAmount = this.checkoutForm.roomAmount + this.checkoutForm.extraAmount
      this.checkoutForm.refundAmount = this.checkoutForm.deposit - (this.checkoutForm.roomAmount + this.checkoutForm.extraAmount)
      this.checkoutVisible = true
    },
    async handleCheckout() {
      this.actionLoading = true
      try {
        const result = await checkout(this.checkoutForm.orderId, {
          payMethod: this.checkoutForm.payMethod,
          remark: this.checkoutForm.remark
        })
        this.checkoutVisible = false
        await ElMessageBox.alert(
          `退房成功！\n订单号：${result.orderNo}\n应收房费：¥${formatMoney(result.roomAmount)}\n附加消费：¥${formatMoney(result.extraAmount)}\n押金抵扣：¥${formatMoney(this.checkoutForm.deposit)}\n${result.refundAmount >= 0 ? '应退押金' : '需补收'}：¥${formatMoney(Math.abs(result.refundAmount))}`,
          '退房成功',
          { confirmButtonText: '确定', type: 'success', center: true }
        )
        this.loadList()
      } catch (e) {
      } finally {
        this.actionLoading = false
      }
    },
    showExtendStay(row) {
      this.extendForm = {
        orderId: row.id,
        orderNo: row.orderNo,
        oldCheckOut: row.expectedCheckOutTime,
        newCheckOut: null
      }
      this.extendVisible = true
    },
    disabledAfterDate(date) {
      const oldDate = new Date(this.extendForm.oldCheckOut)
      return date.getTime() <= oldDate.getTime()
    },
    async handleExtendStay() {
      if (!this.extendForm.newCheckOut) {
        ElMessage.warning('请选择新退房时间')
        return
      }
      this.actionLoading = true
      try {
        const result = await extendStay(this.extendForm.orderId, {
          orderId: this.extendForm.orderId,
          newCheckOutTime: this.extendForm.newCheckOut
        })
        this.extendVisible = false
        await ElMessageBox.alert(
          `续住成功！\n新增${result.extraNights}晚\n新增房费：¥${formatMoney(result.extraRoomAmount)}`,
          '续住成功',
          { confirmButtonText: '确定', type: 'success', center: true }
        )
        this.loadList()
      } catch (e) {
      } finally {
        this.actionLoading = false
      }
    },
    async showChangeRoom(row) {
      this.changeRoomForm = {
        orderId: row.id,
        orderNo: row.orderNo,
        oldRoomNo: row.roomNo,
        newRoomId: null,
        reason: ''
      }
      try {
        const rooms = await getAvailableRooms({
          startTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
          endTime: row.expectedCheckOutTime
        })
        this.availableRooms = rooms || []
        this.changeRoomVisible = true
      } catch (e) {}
    },
    onRoomChange() {},
    async handleChangeRoom() {
      if (!this.changeRoomForm.newRoomId) {
        ElMessage.warning('请选择新房间')
        return
      }
      this.actionLoading = true
      try {
        const result = await changeRoom(this.changeRoomForm.orderId, {
          newRoomId: this.changeRoomForm.newRoomId,
          reason: this.changeRoomForm.reason
        })
        this.changeRoomVisible = false
        await ElMessageBox.alert(
          `换房成功！\n新房间：${result.newRoomNo}`,
          '换房成功',
          { confirmButtonText: '确定', type: 'success', center: true }
        )
        this.loadList()
      } catch (e) {
      } finally {
        this.actionLoading = false
      }
    },
    showAddItem(row) {
      this.addItemOrderId = row.id
      this.itemForm = {
        chargeItemId: null,
        itemName: '',
        price: 0,
        quantity: 1,
        remark: ''
      }
      this.addItemVisible = true
    },
    onChargeItemChange(val) {
      const item = this.chargeItems.find(c => c.id === val)
      if (item) {
        this.itemForm.itemName = item.name
        this.itemForm.price = item.price
      }
    },
    async handleAddItem() {
      if (!this.itemForm.itemName) {
        ElMessage.warning('请输入项目名称')
        return
      }
      if (!this.itemForm.price || this.itemForm.price <= 0) {
        ElMessage.warning('请输入有效单价')
        return
      }
      this.actionLoading = true
      try {
        await addOrderItem(this.addItemOrderId, { ...this.itemForm })
        this.addItemVisible = false
        ElMessage.success('添加消费成功')
        if (this.detailVisible) {
          const items = await getOrderItems(this.addItemOrderId)
          this.orderItems = items || []
          this.detail = await getOrder(this.addItemOrderId)
        }
        this.loadList()
      } catch (e) {
      } finally {
        this.actionLoading = false
      }
    },
    handleDeleteItem(orderId, itemId) {
      ElMessageBox.confirm('确认删除该消费项？', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          await deleteOrderItem(orderId, itemId)
          ElMessage.success('删除成功')
          const items = await getOrderItems(orderId)
          this.orderItems = items || []
          this.detail = await getOrder(orderId)
          this.loadList()
        } catch (e) {}
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-title {
  font-size: 20px;
  font-weight: bold;
  color: #1e3a5f;
}
.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
  align-items: center;
}
.table-card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}
</style>
