-- 锦程酒店运营管理系统 v3.0 数据库初始化脚本
-- 数据库：jchotel
-- 字符集：utf8mb4

CREATE DATABASE IF NOT EXISTS jchotel DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE jchotel;

-- 用户表
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    real_name VARCHAR(50) COMMENT '真实姓名',
    role VARCHAR(20) DEFAULT 'receptionist' COMMENT '角色：admin/manager/receptionist/housekeeping/engineering',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用/0禁用',
    login_fail_count INT DEFAULT 0 COMMENT '连续登录失败次数',
    locked_until DATETIME DEFAULT NULL COMMENT '锁定截止时间',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT='系统用户表';

-- 房型表
CREATE TABLE t_room_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(50) NOT NULL COMMENT '房型名称',
    bed_type VARCHAR(50) COMMENT '床型',
    capacity INT DEFAULT 1 COMMENT '容纳人数',
    weekend_price DECIMAL(10,2) DEFAULT NULL COMMENT '周末价',
    member_price DECIMAL(10,2) DEFAULT NULL COMMENT '会员价',
    description VARCHAR(500) DEFAULT NULL COMMENT '房型描述',
    facilities VARCHAR(500) DEFAULT NULL COMMENT '设施标签，逗号分隔',
    image_url VARCHAR(300) DEFAULT NULL COMMENT '房型图片URL',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT='房型表';

-- 客房表
CREATE TABLE t_room (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    room_no VARCHAR(20) NOT NULL UNIQUE COMMENT '房间号',
    type_id BIGINT NOT NULL COMMENT '房型ID',
    floor INT COMMENT '楼层',
    price DECIMAL(10,2) COMMENT '每晚价格',
    status VARCHAR(20) DEFAULT 'idle' COMMENT '状态：idle空闲/occupied入住中/maintenance维修中/cleaning清扫中/dirty脏房',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (type_id) REFERENCES t_room_type(id)
) COMMENT='客房表';

-- 客户表
CREATE TABLE t_customer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    phone VARCHAR(20) COMMENT '手机号',
    id_card VARCHAR(18) COMMENT '身份证号',
    gender CHAR(1) COMMENT '性别：M男/F女',
    vip_level INT DEFAULT 0 COMMENT 'VIP等级：0普通/1银卡/2金卡/3钻石',
    tags VARCHAR(200) DEFAULT NULL COMMENT '客户标签，逗号分隔',
    is_blacklist TINYINT DEFAULT 0 COMMENT '是否黑名单：0否/1是',
    blacklist_reason VARCHAR(200) DEFAULT NULL COMMENT '黑名单原因',
    birthday DATE DEFAULT NULL COMMENT '生日',
    total_spent DECIMAL(12,2) DEFAULT 0 COMMENT '累计消费金额',
    last_stay_time DATETIME DEFAULT NULL COMMENT '最后入住时间',
    remark VARCHAR(255) COMMENT '备注',
    check_in_count INT DEFAULT 0 COMMENT '入住次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT='客户表';

-- 订单表
CREATE TABLE t_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    room_id BIGINT NOT NULL COMMENT '客房ID',
    user_id BIGINT COMMENT '操作用户ID',
    check_in_time DATETIME NOT NULL COMMENT '入住时间',
    expected_check_out_time DATETIME NOT NULL COMMENT '预计退房时间',
    actual_check_out_time DATETIME COMMENT '实际退房时间',
    deposit DECIMAL(10,2) DEFAULT 0 COMMENT '押金',
    deposit_refunded TINYINT DEFAULT 0 COMMENT '押金是否已退还：0未退/1已退',
    room_amount DECIMAL(10,2) DEFAULT 0 COMMENT '房费总额',
    extra_amount DECIMAL(10,2) DEFAULT 0 COMMENT '附加消费总额',
    total_amount DECIMAL(10,2) DEFAULT 0 COMMENT '结算总金额',
    room_changed TINYINT DEFAULT 0 COMMENT '是否换房：0否/1是',
    original_room_id BIGINT DEFAULT NULL COMMENT '原房间ID(换房前)',
    parent_order_id BIGINT DEFAULT NULL COMMENT '续住来源订单ID',
    channel VARCHAR(20) DEFAULT 'front' COMMENT '订单来源：front/phone/online',
    status VARCHAR(20) DEFAULT 'checkedIn' COMMENT '状态：pending/checkedIn/checkedOut/cancelled',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (customer_id) REFERENCES t_customer(id),
    FOREIGN KEY (room_id) REFERENCES t_room(id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
) COMMENT='订单表';

-- 支付记录表
CREATE TABLE IF NOT EXISTS t_payment (
  id BIGINT NOT NULL AUTO_INCREMENT,
  order_id BIGINT NOT NULL COMMENT '订单ID',
  order_no VARCHAR(32) NOT NULL COMMENT '订单号',
  payment_method VARCHAR(20) NOT NULL DEFAULT 'cash' COMMENT '支付方式: cash/wechat/alipay/bank_card/deposit',
  amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '金额',
  type VARCHAR(20) NOT NULL DEFAULT 'pay' COMMENT '类型: pay/refund/deposit/deposit_refund',
  remark VARCHAR(200) COMMENT '备注',
  operator_id BIGINT COMMENT '操作员ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录';

-- 操作日志表
CREATE TABLE IF NOT EXISTS t_operation_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT COMMENT '操作员ID',
  username VARCHAR(50) COMMENT '操作员用户名',
  module VARCHAR(50) COMMENT '模块',
  operation VARCHAR(100) COMMENT '操作',
  method VARCHAR(200) COMMENT '请求方法',
  params TEXT COMMENT '请求参数',
  ip VARCHAR(50) COMMENT 'IP地址',
  status INT DEFAULT 1 COMMENT '状态: 1成功/0失败',
  error_msg VARCHAR(500) COMMENT '错误信息',
  cost_time BIGINT COMMENT '耗时(ms)',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_user_id (user_id),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志';

-- 订单附加消费明细
CREATE TABLE t_order_item (
  id BIGINT NOT NULL AUTO_INCREMENT,
  order_id BIGINT NOT NULL COMMENT '订单ID',
  order_no VARCHAR(32) NOT NULL COMMENT '订单号',
  item_name VARCHAR(100) NOT NULL COMMENT '消费项目名称',
  category VARCHAR(30) DEFAULT 'other' COMMENT '类别: mini_bar/laundry/food/damage/other',
  price DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '单价',
  quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
  amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '小计金额',
  remark VARCHAR(200) DEFAULT NULL COMMENT '备注',
  operator_id BIGINT DEFAULT NULL COMMENT '操作员ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单附加消费明细';

-- 客房清扫任务
CREATE TABLE t_cleaning_task (
  id BIGINT NOT NULL AUTO_INCREMENT,
  room_id BIGINT NOT NULL COMMENT '房间ID',
  room_no VARCHAR(20) NOT NULL COMMENT '房间号',
  order_id BIGINT DEFAULT NULL COMMENT '关联退房订单ID',
  assignee_id BIGINT DEFAULT NULL COMMENT '分配保洁员ID',
  assignee_name VARCHAR(50) DEFAULT NULL COMMENT '保洁员姓名',
  status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending/assigned/cleaning/done/inspected',
  priority VARCHAR(10) DEFAULT 'normal' COMMENT '优先级: low/normal/high/urgent',
  remark VARCHAR(200) DEFAULT NULL COMMENT '备注',
  finish_time DATETIME DEFAULT NULL COMMENT '完成时间',
  inspect_time DATETIME DEFAULT NULL COMMENT '查房完成时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_room_id (room_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客房清扫任务';

-- 维修工单
CREATE TABLE t_maintenance_order (
  id BIGINT NOT NULL AUTO_INCREMENT,
  order_no VARCHAR(32) NOT NULL COMMENT '工单号',
  room_id BIGINT NOT NULL COMMENT '房间ID',
  room_no VARCHAR(20) NOT NULL COMMENT '房间号',
  title VARCHAR(200) NOT NULL COMMENT '故障标题',
  description VARCHAR(500) DEFAULT NULL COMMENT '故障描述',
  category VARCHAR(30) DEFAULT 'facility' COMMENT '类别: facility/electric/plumbing/furniture/other',
  priority VARCHAR(10) DEFAULT 'normal' COMMENT '优先级: low/normal/high/urgent',
  reporter_id BIGINT DEFAULT NULL COMMENT '报修人ID',
  reporter_name VARCHAR(50) DEFAULT NULL COMMENT '报修人姓名',
  assignee_id BIGINT DEFAULT NULL COMMENT '维修人ID',
  assignee_name VARCHAR(50) DEFAULT NULL COMMENT '维修人姓名',
  status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending/processing/done/verified/cancelled',
  solution VARCHAR(500) DEFAULT NULL COMMENT '维修方案/结果',
  cost DECIMAL(10,2) DEFAULT 0 COMMENT '维修费用',
  finish_time DATETIME DEFAULT NULL COMMENT '完成时间',
  verify_time DATETIME DEFAULT NULL COMMENT '验收时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_order_no (order_no),
  KEY idx_room_id (room_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修工单';

-- 发票记录
CREATE TABLE t_invoice (
  id BIGINT NOT NULL AUTO_INCREMENT,
  invoice_no VARCHAR(32) NOT NULL COMMENT '发票号',
  order_id BIGINT NOT NULL COMMENT '订单ID',
  order_no VARCHAR(32) NOT NULL COMMENT '订单号',
  customer_id BIGINT DEFAULT NULL COMMENT '客户ID',
  customer_name VARCHAR(50) DEFAULT NULL COMMENT '客户名称',
  title VARCHAR(200) NOT NULL COMMENT '发票抬头',
  tax_no VARCHAR(50) DEFAULT NULL COMMENT '纳税人识别号',
  amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '开票金额',
  type VARCHAR(20) DEFAULT 'normal' COMMENT '类型: normal/special',
  status VARCHAR(20) NOT NULL DEFAULT 'issued' COMMENT '状态: issued/red/cancelled',
  content VARCHAR(200) DEFAULT '住宿费' COMMENT '开票内容',
  remark VARCHAR(200) DEFAULT NULL COMMENT '备注',
  operator_id BIGINT DEFAULT NULL COMMENT '开票人ID',
  operator_name VARCHAR(50) DEFAULT NULL COMMENT '开票人姓名',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开票时间',
  cancel_time DATETIME DEFAULT NULL COMMENT '作废/红冲时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_invoice_no (invoice_no),
  KEY idx_order_id (order_id),
  KEY idx_customer_id (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发票记录';

-- 换房记录
CREATE TABLE t_room_change_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  order_id BIGINT NOT NULL COMMENT '订单ID',
  order_no VARCHAR(32) NOT NULL COMMENT '订单号',
  from_room_id BIGINT NOT NULL COMMENT '原房间ID',
  from_room_no VARCHAR(20) NOT NULL COMMENT '原房间号',
  to_room_id BIGINT NOT NULL COMMENT '新房间ID',
  to_room_no VARCHAR(20) NOT NULL COMMENT '新房间号',
  reason VARCHAR(200) DEFAULT NULL COMMENT '换房原因',
  price_diff DECIMAL(10,2) DEFAULT 0 COMMENT '房费差价',
  operator_id BIGINT DEFAULT NULL COMMENT '操作员ID',
  operator_name VARCHAR(50) DEFAULT NULL COMMENT '操作员姓名',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '换房时间',
  PRIMARY KEY (id),
  KEY idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='换房记录';

-- 收费项目字典
CREATE TABLE t_charge_item (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL COMMENT '项目名称',
  category VARCHAR(30) NOT NULL DEFAULT 'other' COMMENT '类别',
  price DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '默认单价',
  unit VARCHAR(20) DEFAULT '个' COMMENT '单位',
  status TINYINT DEFAULT 1 COMMENT '状态：1启用/0停用',
  sort INT DEFAULT 0 COMMENT '排序',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收费项目字典';

-- 初始化用户（密码：123456，首次登录自动升级BCrypt）
INSERT INTO sys_user (username, password, real_name, role, status) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 'admin', 1),
('manager', 'e10adc3949ba59abbe56e057f20f883e', '运营经理', 'manager', 1),
('front', 'e10adc3949ba59abbe56e057f20f883e', '前台小王', 'receptionist', 1);

-- 初始化房型
INSERT INTO t_room_type (name, bed_type, capacity, description, facilities) VALUES
('单人间', '单人床 1.2m', 1, '经济实惠的单人房间，适合商务出差', 'Wifi,空调,电视,淋浴'),
('标准双床房', '单人床 1.2m × 2', 2, '标准双床配置，适合同事或朋友出行', 'Wifi,空调,电视,淋浴,窗户'),
('标准大床房', '大床 1.5m', 2, '温馨大床房间，适合情侣或夫妻', 'Wifi,空调,电视,淋浴,窗户'),
('豪华套房', '大床 1.8m + 客厅', 3, '宽敞豪华套房，带独立客厅区域', 'Wifi,空调,电视,浴缸,窗户,迷你吧,早餐');

-- 初始化客房
INSERT INTO t_room (room_no, type_id, floor, price, status, remark) VALUES
('101', 1, 1, 188.00, 'idle', ''),
('102', 2, 1, 268.00, 'idle', ''),
('103', 3, 1, 328.00, 'occupied', ''),
('104', 4, 1, 588.00, 'idle', ''),
('201', 1, 2, 208.00, 'idle', ''),
('202', 2, 2, 288.00, 'maintenance', '空调维修'),
('203', 3, 2, 358.00, 'idle', ''),
('204', 4, 2, 628.00, 'idle', ''),
('301', 1, 3, 228.00, 'idle', ''),
('302', 2, 3, 308.00, 'occupied', ''),
('303', 3, 3, 388.00, 'idle', ''),
('304', 4, 3, 668.00, 'idle', '');

-- 初始化收费项目
INSERT INTO t_charge_item (name, category, price, unit, sort) VALUES
('可乐', 'mini_bar', 8.00, '瓶', 1),
('雪碧', 'mini_bar', 8.00, '瓶', 2),
('矿泉水', 'mini_bar', 5.00, '瓶', 3),
('红牛', 'mini_bar', 12.00, '罐', 4),
('方便面', 'mini_bar', 10.00, '桶', 5),
('啤酒', 'mini_bar', 15.00, '瓶', 6),
('洗衣-衬衫', 'laundry', 20.00, '件', 20),
('洗衣-T恤', 'laundry', 15.00, '件', 21),
('洗衣-裤子', 'laundry', 25.00, '件', 22),
('早餐', 'food', 38.00, '份', 30),
('午餐', 'food', 58.00, '份', 31),
('晚餐', 'food', 68.00, '份', 32),
('物品损坏赔偿', 'damage', 0.00, '项', 50);

-- 初始化客户
INSERT INTO t_customer (name, phone, id_card, gender, vip_level, remark, check_in_count, birthday, total_spent) VALUES
('张伟', '13800138001', '110101199001011234', 'M', 2, '常客，喜高楼层', 5, '1990-01-01', 5600.00),
('李娜', '13900139002', '110102199203034567', 'F', 3, '钻石卡会员，需准备水果', 12, '1992-03-03', 18800.00),
('王强', '13700137003', '110103198805056789', 'M', 0, '', 1, '1988-05-05', 376.00),
('刘洋', '13600136004', '110104199507078901', 'M', 1, '发票抬头：某某科技', 3, '1995-07-07', 1200.00);
