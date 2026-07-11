USE jchotel;

-- ============================================
-- 锦程酒店PMS v3.0 升级脚本
-- 新增：房态(清扫/脏房)、附加消费、清扫任务、维修工单、发票、
--       客户标签/黑名单、房型多价格/设施、密码锁定、会员自动升级
-- ============================================

-- 1. t_room 状态扩展：新增 cleaning(清扫中)、dirty(脏房) 状态
-- 无需改表结构，在业务逻辑中支持新状态值即可

-- 2. t_room_type 扩展字段：多价格体系、设施、描述、图片
ALTER TABLE t_room_type ADD COLUMN IF NOT EXISTS `weekend_price` DECIMAL(10,2) DEFAULT NULL COMMENT '周末价';
ALTER TABLE t_room_type ADD COLUMN IF NOT EXISTS `member_price` DECIMAL(10,2) DEFAULT NULL COMMENT '会员价';
ALTER TABLE t_room_type ADD COLUMN IF NOT EXISTS `description` VARCHAR(500) DEFAULT NULL COMMENT '房型描述';
ALTER TABLE t_room_type ADD COLUMN IF NOT EXISTS `facilities` VARCHAR(500) DEFAULT NULL COMMENT '设施标签，逗号分隔(Wifi,空调,早餐,窗户,浴缸)';
ALTER TABLE t_room_type ADD COLUMN IF NOT EXISTS `image_url` VARCHAR(300) DEFAULT NULL COMMENT '房型图片URL';

-- 3. t_customer 扩展字段：标签、黑名单、生日、累计消费
ALTER TABLE t_customer ADD COLUMN IF NOT EXISTS `tags` VARCHAR(200) DEFAULT NULL COMMENT '客户标签，逗号分隔';
ALTER TABLE t_customer ADD COLUMN IF NOT EXISTS `is_blacklist` TINYINT DEFAULT 0 COMMENT '是否黑名单：0否/1是';
ALTER TABLE t_customer ADD COLUMN IF NOT EXISTS `blacklist_reason` VARCHAR(200) DEFAULT NULL COMMENT '黑名单原因';
ALTER TABLE t_customer ADD COLUMN IF NOT EXISTS `birthday` DATE DEFAULT NULL COMMENT '生日(从身份证解析)';
ALTER TABLE t_customer ADD COLUMN IF NOT EXISTS `total_spent` DECIMAL(12,2) DEFAULT 0 COMMENT '累计消费金额';
ALTER TABLE t_customer ADD COLUMN IF NOT EXISTS `last_stay_time` DATETIME DEFAULT NULL COMMENT '最后入住时间';

-- 4. t_order 扩展字段：押金退还、换房标记、续住、来源订单
ALTER TABLE t_order ADD COLUMN IF NOT EXISTS `deposit_refunded` TINYINT DEFAULT 0 COMMENT '押金是否已退还：0未退/1已退';
ALTER TABLE t_order ADD COLUMN IF NOT EXISTS `room_changed` TINYINT DEFAULT 0 COMMENT '是否换房：0否/1是';
ALTER TABLE t_order ADD COLUMN IF NOT EXISTS `original_room_id` BIGINT DEFAULT NULL COMMENT '原房间ID(换房前)';
ALTER TABLE t_order ADD COLUMN IF NOT EXISTS `parent_order_id` BIGINT DEFAULT NULL COMMENT '续住来源订单ID';
ALTER TABLE t_order ADD COLUMN IF NOT EXISTS `channel` VARCHAR(20) DEFAULT 'front' COMMENT '订单来源：front前台/phone电话/online线上';
ALTER TABLE t_order ADD COLUMN IF NOT EXISTS `extra_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '附加消费总额';
ALTER TABLE t_order ADD COLUMN IF NOT EXISTS `room_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '房费总额';

-- 5. sys_user 扩展字段：密码错误锁定
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS `login_fail_count` INT DEFAULT 0 COMMENT '连续登录失败次数';
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS `locked_until` DATETIME DEFAULT NULL COMMENT '锁定截止时间';
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间';

-- ============================================
-- 新表：附加消费/迷你吧明细
-- ============================================
CREATE TABLE IF NOT EXISTS `t_order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
  `item_name` VARCHAR(100) NOT NULL COMMENT '消费项目名称',
  `category` VARCHAR(30) DEFAULT 'other' COMMENT '类别: mini_bar迷你吧/laundry洗衣/food餐饮/damage赔偿/other其他',
  `price` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '单价',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
  `amount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '小计金额',
  `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作员ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单附加消费明细';

-- ============================================
-- 新表：清扫任务
-- ============================================
CREATE TABLE IF NOT EXISTS `t_cleaning_task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `room_id` BIGINT NOT NULL COMMENT '房间ID',
  `room_no` VARCHAR(20) NOT NULL COMMENT '房间号',
  `order_id` BIGINT DEFAULT NULL COMMENT '关联退房订单ID',
  `assignee_id` BIGINT DEFAULT NULL COMMENT '分配保洁员ID',
  `assignee_name` VARCHAR(50) DEFAULT NULL COMMENT '保洁员姓名',
  `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending待分配/assigned已分配/cleaning清扫中/done已完成/inspected已查房',
  `priority` VARCHAR(10) DEFAULT 'normal' COMMENT '优先级: low/normal/high/urgent',
  `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注(如退房原因/特殊要求)',
  `finish_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `inspect_time` DATETIME DEFAULT NULL COMMENT '查房完成时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客房清扫任务';

-- ============================================
-- 新表：维修工单
-- ============================================
CREATE TABLE IF NOT EXISTS `t_maintenance_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(32) NOT NULL COMMENT '工单号',
  `room_id` BIGINT NOT NULL COMMENT '房间ID',
  `room_no` VARCHAR(20) NOT NULL COMMENT '房间号',
  `title` VARCHAR(200) NOT NULL COMMENT '故障标题',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '故障描述',
  `category` VARCHAR(30) DEFAULT 'facility' COMMENT '类别: facility设施/electric电气/plumbing水暖/furniture家具/other其他',
  `priority` VARCHAR(10) DEFAULT 'normal' COMMENT '优先级: low/normal/high/urgent',
  `reporter_id` BIGINT DEFAULT NULL COMMENT '报修人ID',
  `reporter_name` VARCHAR(50) DEFAULT NULL COMMENT '报修人姓名',
  `assignee_id` BIGINT DEFAULT NULL COMMENT '维修人ID',
  `assignee_name` VARCHAR(50) DEFAULT NULL COMMENT '维修人姓名',
  `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending待处理/processing处理中/done已完成/verified已验收/cancelled取消',
  `solution` VARCHAR(500) DEFAULT NULL COMMENT '维修方案/结果',
  `cost` DECIMAL(10,2) DEFAULT 0 COMMENT '维修费用',
  `finish_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `verify_time` DATETIME DEFAULT NULL COMMENT '验收时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修工单';

-- ============================================
-- 新表：发票记录
-- ============================================
CREATE TABLE IF NOT EXISTS `t_invoice` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `invoice_no` VARCHAR(32) NOT NULL COMMENT '发票号',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
  `customer_id` BIGINT DEFAULT NULL COMMENT '客户ID',
  `customer_name` VARCHAR(50) DEFAULT NULL COMMENT '客户名称',
  `title` VARCHAR(200) NOT NULL COMMENT '发票抬头',
  `tax_no` VARCHAR(50) DEFAULT NULL COMMENT '纳税人识别号',
  `amount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '开票金额',
  `type` VARCHAR(20) DEFAULT 'normal' COMMENT '类型: normal普通/special专用',
  `status` VARCHAR(20) NOT NULL DEFAULT 'issued' COMMENT '状态: issued已开/red红冲/cancelled作废',
  `content` VARCHAR(200) DEFAULT '住宿费' COMMENT '开票内容',
  `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
  `operator_id` BIGINT DEFAULT NULL COMMENT '开票人ID',
  `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '开票人姓名',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开票时间',
  `cancel_time` DATETIME DEFAULT NULL COMMENT '作废/红冲时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_invoice_no` (`invoice_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_customer_id` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发票记录';

-- ============================================
-- 新表：换房记录
-- ============================================
CREATE TABLE IF NOT EXISTS `t_room_change_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
  `from_room_id` BIGINT NOT NULL COMMENT '原房间ID',
  `from_room_no` VARCHAR(20) NOT NULL COMMENT '原房间号',
  `to_room_id` BIGINT NOT NULL COMMENT '新房间ID',
  `to_room_no` VARCHAR(20) NOT NULL COMMENT '新房间号',
  `reason` VARCHAR(200) DEFAULT NULL COMMENT '换房原因',
  `price_diff` DECIMAL(10,2) DEFAULT 0 COMMENT '房费差价(正补负退)',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作员ID',
  `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作员姓名',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '换房时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='换房记录';

-- ============================================
-- 消费项目字典（预置迷你吧常见商品）
-- ============================================
CREATE TABLE IF NOT EXISTS `t_charge_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL COMMENT '项目名称',
  `category` VARCHAR(30) NOT NULL DEFAULT 'other' COMMENT '类别',
  `price` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '默认单价',
  `unit` VARCHAR(20) DEFAULT '个' COMMENT '单位',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1启用/0停用',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收费项目字典';

INSERT INTO t_charge_item (name, category, price, unit, sort) VALUES
('可乐', 'mini_bar', 8.00, '瓶', 1),
('雪碧', 'mini_bar', 8.00, '瓶', 2),
('矿泉水', 'mini_bar', 5.00, '瓶', 3),
('红牛', 'mini_bar', 12.00, '罐', 4),
('方便面', 'mini_bar', 10.00, '桶', 5),
('饼干', 'mini_bar', 15.00, '包', 6),
('巧克力', 'mini_bar', 18.00, '块', 7),
('啤酒', 'mini_bar', 15.00, '瓶', 8),
('洗衣-衬衫', 'laundry', 20.00, '件', 20),
('洗衣-T恤', 'laundry', 15.00, '件', 21),
('洗衣-裤子', 'laundry', 25.00, '件', 22),
('洗衣-外套', 'laundry', 35.00, '件', 23),
('早餐', 'food', 38.00, '份', 30),
('午餐', 'food', 58.00, '份', 31),
('晚餐', 'food', 68.00, '份', 32),
('物品损坏赔偿', 'damage', 0.00, '项', 50);
