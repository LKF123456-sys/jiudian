<template>
  <div class="page-container">
    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane label="办理入住 / 预约" name="checkin">
        <div style="max-width: 600px; margin: 20px auto;">
          <el-form :model="form" :rules="rules" ref="checkinForm" label-width="120px">
            <el-form-item label="选择客户" prop="customerId">
              <el-select v-model="form.customerId" filterable remote reserve-keyword :remote-method="searchCustomers"
                         :loading="customerLoading" placeholder="请输入姓名或手机号搜索" style="width: 100%;">
                <el-option v-for="c in customers" :key="c.id" :label="c.name + ' - ' + c.phone" :value="c.id"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="text" @click="openCustomerDialog">+ 快速新增客户</el-button>
            </el-form-item>
            <el-form-item label="选择客房" prop="roomId">
              <el-select v-model="form.roomId" filterable placeholder="请先选择入住/退房时间" style="width: 100%;">
                <el-option v-for="r in availableRooms" :key="r.id" :label="r.roomNo + ' - ' + r.typeName + ' - ¥' + formatMoney(r.price) + '/晚'" :value="r.id"></el-option>
              </el-select>
              <div v-if="roomLoadStatus" style="color: #909399; font-size: 12px; margin-top: 4px;">{{ roomLoadStatus }}</div>
            </el-form-item>
            <el-form-item label="入住时间" prop="checkInTime">
              <el-date-picker v-model="form.checkInTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"
                              placeholder="选择入住时间" :disabled-date="disablePastDates"
                              :disabled-hours="disabledCheckInHours" :disabled-minutes="disabledCheckInMinutes"
                              @change="onCheckInTimeChange" style="width: 100%;"></el-date-picker>
            </el-form-item>
            <el-form-item label="预计退房时间" prop="expectedCheckOutTime">
              <el-date-picker v-model="form.expectedCheckOutTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"
                              placeholder="选择预计退房时间" :disabled-date="disableBeforeCheckIn"
                              :disabled-hours="disabledCheckOutHours" :disabled-minutes="disabledCheckOutMinutes"
                              @change="onCheckOutTimeChange" style="width: 100%;"></el-date-picker>
            </el-form-item>
            <el-form-item label="押金" prop="deposit">
              <el-input-number v-model="form.deposit" :min="0" :precision="2" style="width: 100%;"></el-input-number>
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitCheckin">提交</el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>

      <el-tab-pane label="在住列表" name="staying">
        <div class="search-bar">
          <el-select v-model="stayingStatus" placeholder="在住状态" clearable style="width: 140px;" @change="handleSearchOrders">
            <el-option label="待入住" value="pending"></el-option>
            <el-option label="已入住" value="checkedIn"></el-option>
          </el-select>
          <el-input v-model="query.keyword" placeholder="客户姓名/房间号" clearable style="width: 220px;"
                    @keyup.enter="handleSearchOrders"></el-input>
          <el-button type="primary" @click="handleSearchOrders">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="handleResetOrders">重置</el-button>
        </div>
        <div class="table-card">
          <el-table :data="orders" stripe>
            <el-table-column prop="orderNo" label="订单号" width="160"></el-table-column>
            <el-table-column prop="customerName" label="客户"></el-table-column>
            <el-table-column prop="roomNo" label="房间号"></el-table-column>
            <el-table-column label="入住时间">
              <template #default="scope">{{ formatDate(scope.row.checkInTime) }}</template>
            </el-table-column>
            <el-table-column label="预计退房">
              <template #default="scope">{{ formatDate(scope.row.expectedCheckOutTime) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="scope">
                <el-tag v-if="scope.row.status === 'checkedIn'" type="warning" size="small">已入住</el-tag>
                <el-tag v-else-if="scope.row.status === 'pending'" type="info" size="small">待入住</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="押金">
              <template #default="scope">¥{{ formatMoney(scope.row.deposit) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="scope">
                <el-button v-if="scope.row.status === 'pending'" type="primary" size="small" @click="handleSettle(scope.row)">办理入住</el-button>
                <el-button v-if="scope.row.status === 'checkedIn'" type="primary" size="small" @click="openCheckout(scope.row)">办理退房</el-button>
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
      </el-tab-pane>
    </el-tabs>

    <el-dialog title="快速新增客户" v-model="customerDialogVisible" width="500px">
      <el-form :model="customerForm" :rules="customerRules" ref="customerFormRef" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="customerForm.name"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="customerForm.phone"></el-input>
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input v-model="customerForm.idCard"></el-input>
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="customerForm.gender">
            <el-radio label="M">男</el-radio>
            <el-radio label="F">女</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="customerDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCustomer">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog title="办理退房" v-model="checkoutDialogVisible" width="400px" @closed="onCheckoutDialogClosed">
      <el-form :model="checkoutForm" ref="checkoutFormRef" label-width="120px">
        <el-form-item label="实际退房时间" prop="actualCheckOutTime">
          <el-date-picker v-model="checkoutForm.actualCheckOutTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"
                          placeholder="选择实际退房时间" :disabled-date="disableFutureDates" style="width: 100%;"></el-date-picker>
        </el-form-item>
      </el-form>
      <div v-if="checkoutResult.orderId" style="background: #f6ffed; padding: 12px; border-radius: 4px; margin-bottom: 16px;">
        <p>订单号：{{ checkoutResult.orderNo }}</p>
        <p>房间：{{ checkoutResult.roomNo }}（{{ checkoutResult.roomTypeName }}）</p>
        <p>客户：{{ checkoutResult.customerName }}</p>
        <p>住宿夜数：{{ checkoutResult.nights }} 晚</p>
        <p>房费：¥{{ formatMoney(checkoutResult.roomAmount) }}</p>
        <p v-if="checkoutResult.extraAmount > 0">附加消费：¥{{ formatMoney(checkoutResult.extraAmount) }}</p>
        <p>押金：¥{{ formatMoney(checkoutResult.deposit) }}</p>
        <p style="font-weight: bold; font-size: 16px; color: #f56c6c; margin-top: 8px;">
          {{ checkoutResult.balance >= 0 ? '应补金额：¥' + formatMoney(checkoutResult.balance) : '应退金额：¥' + formatMoney(Math.abs(checkoutResult.balance)) }}
        </p>
      </div>
      <template #footer>
        <template v-if="checkoutResult.orderId">
          <el-button type="primary" @click="printReceipt">打印小票</el-button>
          <el-button @click="checkoutDialogVisible = false">完成</el-button>
        </template>
        <template v-else>
          <el-button @click="checkoutDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCheckout">确认退房</el-button>
        </template>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { listCustomers, addCustomer } from '@/api/customer'
import { listRooms } from '@/api/room'
import { listOrders, checkin, checkout, settlePending } from '@/api/order'
import { formatDate, formatMoney, nowString, tomorrowString } from '@/utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

export default {
  name: 'Checkin',
  data() {
    return {
      activeTab: 'checkin',
      customers: [],
      customerLoading: false,
      rooms: [],
      availableRooms: [],
      roomLoadStatus: '',
      form: { customerId: '', roomId: '', checkInTime: '', expectedCheckOutTime: '', deposit: 0, remark: '' },
      rules: {
        customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
        roomId: [{ required: true, message: '请选择客房', trigger: 'change' }],
        checkInTime: [{ required: true, message: '请选择入住时间', trigger: 'change' }],
        expectedCheckOutTime: [{ required: true, message: '请选择预计退房时间', trigger: 'change' }],
        deposit: [{ required: true, message: '请输入押金', trigger: 'change' }]
      },
      customerDialogVisible: false,
      customerForm: { name: '', phone: '', idCard: '', gender: 'M', vipLevel: 0, remark: '' },
      customerRules: {
        name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
      },
      query: { page: 1, size: 10, status: 'checkedIn', keyword: '' },
      stayingStatus: 'checkedIn',
      orders: [],
      total: 0,
      checkoutDialogVisible: false,
      checkoutForm: { orderId: '', actualCheckOutTime: '' },
      checkoutResult: {}
    }
  },
  created() {
    this.form.checkInTime = nowString()
    this.form.expectedCheckOutTime = tomorrowString()
    this.loadInitialCustomers()
    this.loadOrders()
    this.loadAvailableRooms()
  },
  methods: {
    loadInitialCustomers() {
      listCustomers({ page: 1, size: 50 }).then(data => {
        this.customers = data.list
      })
    },
    searchCustomers(query) {
      if (query) {
        this.customerLoading = true
        listCustomers({ page: 1, size: 50, keyword: query }).then(data => {
          this.customers = data.list
          this.customerLoading = false
        }).catch(() => {
          this.customerLoading = false
        })
      } else {
        this.loadInitialCustomers()
      }
    },
    loadAvailableRooms() {
      if (!this.form.checkInTime || !this.form.expectedCheckOutTime) {
        this.availableRooms = []
        this.roomLoadStatus = '请先选择入住和退房时间'
        return
      }
      const checkInTime = new Date(this.form.checkInTime)
      const expectedCheckOutTime = new Date(this.form.expectedCheckOutTime)
      if (expectedCheckOutTime <= checkInTime) {
        this.availableRooms = []
        this.roomLoadStatus = '退房时间必须晚于入住时间'
        return
      }
      this.roomLoadStatus = '正在查询可用客房...'
      this.form.roomId = ''
      request.get('/rooms/available', {
        params: {
          checkInTime: this.form.checkInTime,
          expectedCheckOutTime: this.form.expectedCheckOutTime
        }
      }).then(rooms => {
        this.availableRooms = rooms || []
        if (this.availableRooms.length === 0) {
          this.roomLoadStatus = '该时段暂无可选客房'
        } else {
          this.roomLoadStatus = `共找到 ${this.availableRooms.length} 间可用客房`
        }
      }).catch(() => {
        this.roomLoadStatus = '查询可用客房失败，请稍后重试'
        this.availableRooms = []
      })
    },
    loadRoomsFallback() {
      listRooms({ page: 1, size: 1000 }).then(data => {
        this.rooms = data.list
        if (this.availableRooms.length === 0 && this.form.checkInTime && this.form.expectedCheckOutTime) {
          this.availableRooms = data.list.filter(r => r.status !== 'maintenance')
        }
      })
    },
    loadOrders() {
      listOrders(this.query).then(data => {
        this.orders = data?.list || []
        this.total = data?.total || 0
      }).catch(() => {
        this.orders = []
        this.total = 0
      })
    },
    handleSearchOrders() {
      this.query.status = this.stayingStatus || ''
      this.query.page = 1
      this.loadOrders()
    },
    handleResetOrders() {
      this.query = { page: 1, size: 10, status: 'checkedIn', keyword: '' }
      this.stayingStatus = 'checkedIn'
      this.loadOrders()
    },
    handlePageChange(page) {
      this.query.page = page
      this.loadOrders()
    },
    isToday(time) {
      const d = new Date(time)
      const today = new Date()
      return d.getFullYear() === today.getFullYear() && d.getMonth() === today.getMonth() && d.getDate() === today.getDate()
    },
    disablePastDates(time) {
      const todayStart = new Date(new Date().toDateString())
      return time.getTime() < todayStart.getTime()
    },
    disableFutureDates(time) {
      const tomorrowStart = new Date(new Date().toDateString())
      tomorrowStart.setDate(tomorrowStart.getDate() + 1)
      return time.getTime() >= tomorrowStart.getTime()
    },
    disabledCheckInHours(date) {
      if (!date) return []
      if (!this.isToday(date)) return []
      const now = new Date()
      const hours = []
      for (let i = 0; i < now.getHours(); i++) hours.push(i)
      return hours
    },
    disabledCheckInMinutes(date) {
      if (!date) return []
      if (!this.isToday(date)) return []
      const now = new Date()
      const selectedHour = date.getHours()
      if (selectedHour < now.getHours()) {
        const mins = []
        for (let i = 0; i < 60; i++) mins.push(i)
        return mins
      }
      if (selectedHour === now.getHours()) {
        const mins = []
        for (let i = 0; i < now.getMinutes() - 15; i++) mins.push(i)
        return mins
      }
      return []
    },
    disableBeforeCheckIn(time) {
      if (!this.form.checkInTime) {
        const todayStart = new Date(new Date().toDateString())
        return time.getTime() < todayStart.getTime()
      }
      const checkIn = new Date(this.form.checkInTime)
      return time.getTime() < checkIn.getTime()
    },
    disabledCheckOutHours(date) {
      if (!date || !this.form.checkInTime) return []
      const checkIn = new Date(this.form.checkInTime)
      const selectedDate = new Date(date)
      const isCheckInDay = selectedDate.getFullYear() === checkIn.getFullYear() &&
                           selectedDate.getMonth() === checkIn.getMonth() &&
                           selectedDate.getDate() === checkIn.getDate()
      if (!isCheckInDay) return []
      const hours = []
      for (let i = 0; i < checkIn.getHours(); i++) hours.push(i)
      return hours
    },
    disabledCheckOutMinutes(date) {
      if (!date || !this.form.checkInTime) return []
      const checkIn = new Date(this.form.checkInTime)
      const selectedDate = new Date(date)
      const isCheckInDay = selectedDate.getFullYear() === checkIn.getFullYear() &&
                           selectedDate.getMonth() === checkIn.getMonth() &&
                           selectedDate.getDate() === checkIn.getDate()
      if (!isCheckInDay) return []
      if (date.getHours() < checkIn.getHours()) {
        const mins = []
        for (let i = 0; i < 60; i++) mins.push(i)
        return mins
      }
      if (date.getHours() === checkIn.getHours()) {
        const mins = []
        for (let i = 0; i <= checkIn.getMinutes(); i++) mins.push(i)
        return mins
      }
      return []
    },
    onCheckInTimeChange(val) {
      if (val && this.form.expectedCheckOutTime) {
        const ci = new Date(val)
        const co = new Date(this.form.expectedCheckOutTime)
        if (co <= ci) {
          this.form.expectedCheckOutTime = ''
        }
      }
      this.loadAvailableRooms()
    },
    onCheckOutTimeChange() {
      this.loadAvailableRooms()
    },
    submitCheckin() {
      this.$refs.checkinForm.validate(valid => {
        if (!valid) return
        const checkInTime = new Date(this.form.checkInTime)
        const expectedCheckOutTime = new Date(this.form.expectedCheckOutTime)
        if (expectedCheckOutTime <= checkInTime) {
          ElMessage.warning('预计退房时间必须晚于入住时间')
          return
        }
        if (checkInTime.getTime() < Date.now() - 15 * 60 * 1000) {
          ElMessage.warning('入住时间不能早于当前时间（允许15分钟误差）')
          return
        }
        const submitData = {
          customerId: this.form.customerId,
          roomId: this.form.roomId,
          checkInTime: this.form.checkInTime,
          expectedCheckOutTime: this.form.expectedCheckOutTime,
          deposit: this.form.deposit,
          remark: this.form.remark
        }
        checkin(submitData).then(data => {
          const isCheckin = data && data.status === 'checkedIn'
          ElMessageBox.alert(isCheckin ? '入住办理成功' : '预约成功', '提示', {
            confirmButtonText: '确定',
            type: 'success',
            center: true
          })
          this.resetForm()
          this.loadRoomsFallback()
          this.loadOrders()
          this.activeTab = 'staying'
        }).catch(() => {})
      })
    },
    resetForm() {
      this.form = {
        customerId: '',
        roomId: '',
        checkInTime: nowString(),
        expectedCheckOutTime: tomorrowString(),
        deposit: 0,
        remark: ''
      }
      this.loadAvailableRooms()
      this.$refs.checkinForm && this.$refs.checkinForm.clearValidate()
    },
    openCustomerDialog() {
      this.customerDialogVisible = true
      this.customerForm = { name: '', phone: '', idCard: '', gender: 'M', vipLevel: 0, remark: '' }
    },
    submitCustomer() {
      this.$refs.customerFormRef.validate(valid => {
        if (!valid) return
        addCustomer(this.customerForm).then(() => {
          ElMessage.success('客户新增成功')
          this.customerDialogVisible = false
          this.loadInitialCustomers()
        })
      })
    },
    handleSettle(row) {
      ElMessageBox.confirm(`确认将订单 ${row.orderNo} 办理入住？`, '办理入住', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        settlePending(row.id).then(() => {
          ElMessageBox.alert('办理入住成功', '提示', {
            confirmButtonText: '确定',
            type: 'success',
            center: true
          })
          this.loadRoomsFallback()
          this.loadOrders()
        }).catch(() => {})
      }).catch(() => {})
    },
    openCheckout(row) {
      this.checkoutDialogVisible = true
      this.checkoutForm = { orderId: row.id, actualCheckOutTime: nowString() }
      this.checkoutResult = {}
    },
    onCheckoutDialogClosed() {
      this.checkoutResult = {}
      this.checkoutForm = { orderId: '', actualCheckOutTime: '' }
    },
    submitCheckout() {
      if (!this.checkoutForm.actualCheckOutTime) {
        ElMessage.warning('请选择实际退房时间')
        return
      }
      const checkoutTime = new Date(this.checkoutForm.actualCheckOutTime)
      const now = new Date()
      if (checkoutTime > now) {
        ElMessage.warning('实际退房时间不能晚于当前时间')
        return
      }
      const order = this.orders.find(o => o.id === this.checkoutForm.orderId)
      if (order && order.checkInTime) {
        const checkInTime = new Date(order.checkInTime)
        if (checkoutTime <= checkInTime) {
          ElMessage.warning('退房时间必须晚于入住时间')
          return
        }
      }
      checkout(this.checkoutForm.orderId, { actualCheckOutTime: this.checkoutForm.actualCheckOutTime }).then(data => {
        this.checkoutResult = data || {}
        this.loadOrders()
        this.loadRoomsFallback()
      }).catch(() => {})
    },
    formatDate,
    formatMoney,
    printReceipt() {
      const r = this.checkoutResult
      if (!r || !r.orderNo) {
        ElMessage.warning('暂无结算信息可打印')
        return
      }
      const printWindow = window.open('', '_blank')
      printWindow.document.write(`
        <html>
        <head>
          <title>结算单 - ${r.orderNo}</title>
          <style>
            body { font-family: 'Microsoft YaHei', sans-serif; padding: 20px; max-width: 320px; margin: 0 auto; }
            h1 { text-align: center; font-size: 20px; margin-bottom: 10px; }
            .divider { border-top: 1px dashed #333; margin: 10px 0; }
            .row { display: flex; justify-content: space-between; margin: 6px 0; font-size: 14px; }
            .label { color: #666; }
            .total { font-weight: bold; font-size: 16px; }
            .items { margin: 8px 0; padding: 0; list-style: none; font-size: 13px; }
            .items li { display: flex; justify-content: space-between; padding: 2px 0; }
            .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #999; }
            @media print { body { padding: 10px; } }
          </style>
        </head>
        <body>
          <h1>锦程酒店结算单</h1>
          <div class="divider"></div>
          <div class="row"><span class="label">订单号：</span><span>${r.orderNo}</span></div>
          <div class="row"><span class="label">客户姓名：</span><span>${r.customerName || ''}</span></div>
          <div class="row"><span class="label">房间号：</span><span>${r.roomNo}</span></div>
          <div class="row"><span class="label">房型：</span><span>${r.roomTypeName || ''}</span></div>
          <div class="divider"></div>
          <div class="row"><span class="label">入住时间：</span><span>${r.checkInTime ? r.checkInTime.replace('T', ' ').substring(0, 16) : ''}</span></div>
          <div class="row"><span class="label">退房时间：</span><span>${r.actualCheckOutTime ? r.actualCheckOutTime.replace('T', ' ').substring(0, 16) : ''}</span></div>
          <div class="row"><span class="label">住宿夜数：</span><span>${r.nights} 晚</span></div>
          <div class="row"><span class="label">平均房价：</span><span>¥${this.formatMoney(r.avgPrice)}/晚</span></div>
          <div class="divider"></div>
          <div class="row"><span class="label">房费总计：</span><span>¥${this.formatMoney(r.roomAmount)}</span></div>
          ${r.extraAmount > 0 ? `<div class="row"><span class="label">附加消费：</span><span>¥${this.formatMoney(r.extraAmount)}</span></div>` : ''}
          ${r.items && r.items.length > 0 ? `
            <ul class="items">
              ${r.items.map(item => `<li><span>${item.itemName} x${item.quantity}</span><span>¥${this.formatMoney(item.amount)}</span></li>`).join('')}
            </ul>
          ` : ''}
          <div class="row" style="font-weight:bold"><span class="label">消费合计：</span><span>¥${this.formatMoney(r.totalAmount)}</span></div>
          <div class="row"><span class="label">押金：</span><span>¥${this.formatMoney(r.deposit)}</span></div>
          <div class="row total">
            <span>${r.balance >= 0 ? '应补金额：' : '应退金额：'}</span>
            <span>¥${this.formatMoney(Math.abs(r.balance))}</span>
          </div>
          <div class="divider"></div>
          <div class="footer">
            <p>打印时间：${nowString()}</p>
            <p>感谢您的光临，欢迎再次入住！</p>
          </div>
        </body>
        </html>
      `)
      printWindow.document.close()
      printWindow.focus()
      setTimeout(() => {
        printWindow.print()
        printWindow.close()
      }, 300)
    }
  }
}
</script>
