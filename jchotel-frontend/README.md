# 锦程酒店运营管理系统 - 前端

基于 Vue 3 + Vite + Element Plus 的酒店管理系统前端。

## 技术栈

- Vue 3.5+ (Composition API)
- Vite 8.x
- Element Plus 2.x
- Pinia 3.x (状态管理)
- Vue Router 4.x
- Axios (HTTP 请求)
- ECharts 6.x (图表)
- Day.js (日期处理)

## 开发

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview
```

## 目录结构

```
src/
├── api/             # API接口封装
│   ├── auth.js      # 认证相关
│   ├── room.js      # 客房管理
│   ├── order.js     # 订单管理
│   ├── customer.js  # 客户管理
│   ├── payment.js   # 支付收银
│   ├── invoice.js   # 发票管理
│   ├── cleaning.js  # 清扫任务
│   ├── maintenance.js # 维修工单
│   ├── dashboard.js # 仪表盘统计
│   ├── report.js    # 报表统计
│   ├── chargeItem.js # 收费项目
│   ├── user.js      # 用户管理
│   ├── operationLog.js # 操作日志
│   ├── reminder.js  # 待办提醒
│   └── request.js   # Axios封装（拦截器、统一错误处理）
├── assets/          # 静态资源
├── components/      # 公共组件
│   └── Layout.vue   # 主布局组件
├── router/          # 路由配置
│   └── index.js
├── store/           # Pinia状态管理
│   └── auth.js      # 认证状态
├── utils/           # 工具函数
│   ├── constants.js # 常量定义
│   └── format.js    # 格式化工具
├── views/           # 页面组件
│   ├── Login.vue        # 登录页
│   ├── Dashboard.vue    # 首页仪表盘
│   ├── Checkin.vue      # 入住退房办理
│   ├── Room.vue         # 客房管理
│   ├── RoomBoard.vue    # 房态看板
│   ├── RoomType.vue     # 房型管理
│   ├── Order.vue        # 订单管理
│   ├── OrderStats.vue   # 营收统计
│   ├── Customer.vue     # 客户管理
│   ├── Cleaning.vue     # 清扫管理
│   ├── Maintenance.vue  # 维修管理
│   ├── Invoice.vue      # 发票管理
│   ├── ChargeItem.vue   # 收费项目配置
│   ├── User.vue         # 用户管理
│   ├── OperationLog.vue # 操作日志
│   ├── Report.vue       # 报表页面
│   └── Profile.vue      # 个人中心
├── App.vue
├── main.js
└── style.css
```

## 环境配置

前端默认连接 `http://localhost:8080` 后端API，如需修改请查看 `src/api/request.js` 中的baseURL配置。

## 主要页面说明

| 页面 | 路由 | 功能 |
|------|------|------|
| 登录 | /login | 账号密码登录、验证码（预留） |
| 仪表盘 | /dashboard | 经营数据概览、图表、房态统计 |
| 入住办理 | /checkin | 预约入住、散客快速入住 |
| 房态看板 | /room-board | 楼层房态总览、颜色标识状态 |
| 客房管理 | /room | 客房列表、增删改查、状态操作 |
| 房型管理 | /room-type | 房型配置 |
| 订单管理 | /order | 订单列表、详情、消费录入 |
| 营收统计 | /order-stats | 按时间段/支付方式/房型统计营收 |
| 客户管理 | /customer | 客户档案、VIP、黑名单 |
| 清扫管理 | /cleaning | 清扫任务分配、状态流转、查房 |
| 维修管理 | /maintenance | 维修工单、派工、验收 |
| 发票管理 | /invoice | 发票开具、红冲、作废 |
| 收费项目 | /charge-item | 迷你吧/洗衣/餐饮等项目配置 |
| 用户管理 | /user | 系统用户、角色分配 |
| 操作日志 | /operation-log | 系统操作审计日志 |
| 个人中心 | /profile | 修改密码、个人信息 |
