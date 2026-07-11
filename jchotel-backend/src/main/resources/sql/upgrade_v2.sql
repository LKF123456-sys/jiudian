USE jchotel;

-- 支付记录表
CREATE TABLE IF NOT EXISTS `t_payment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
  `payment_method` VARCHAR(20) NOT NULL DEFAULT 'cash' COMMENT '支付方式: cash/wechat/alipay/bank_card',
  `amount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '金额',
  `type` VARCHAR(20) NOT NULL DEFAULT 'pay' COMMENT '类型: pay/refund',
  `remark` VARCHAR(200) COMMENT '备注',
  `operator_id` BIGINT COMMENT '操作员ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录';

-- 操作日志表
CREATE TABLE IF NOT EXISTS `t_operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT COMMENT '操作员ID',
  `username` VARCHAR(50) COMMENT '操作员用户名',
  `module` VARCHAR(50) COMMENT '模块',
  `operation` VARCHAR(100) COMMENT '操作',
  `method` VARCHAR(200) COMMENT '请求方法',
  `params` TEXT COMMENT '请求参数',
  `ip` VARCHAR(50) COMMENT 'IP地址',
  `status` INT DEFAULT 1 COMMENT '状态: 1成功/0失败',
  `error_msg` VARCHAR(500) COMMENT '错误信息',
  `cost_time` BIGINT COMMENT '耗时(ms)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志';

-- sys_user添加status字段（如果不存在）
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS `status` TINYINT DEFAULT 1 COMMENT '状态：1启用/0禁用' AFTER `role`;

-- 修复角色值：frontDesk -> receptionist
UPDATE sys_user SET role = 'receptionist' WHERE role = 'frontDesk';

-- 确保admin用户角色为admin
UPDATE sys_user SET role = 'admin' WHERE username = 'admin';

-- 确保role字段有默认值
ALTER TABLE sys_user MODIFY COLUMN `role` VARCHAR(20) DEFAULT 'receptionist' COMMENT '角色：admin 管理员 / manager 经理 / receptionist 前台';

-- 确保sys_user的status字段默认值为1
ALTER TABLE sys_user MODIFY COLUMN `status` TINYINT DEFAULT 1 COMMENT '状态：1启用/0禁用';
