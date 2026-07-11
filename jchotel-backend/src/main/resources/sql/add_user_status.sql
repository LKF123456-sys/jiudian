USE jchotel;

-- 用户管理功能：sys_user 表增加 status 字段（1=启用，0=禁用）
ALTER TABLE sys_user ADD COLUMN status INT DEFAULT 1 COMMENT '状态：1启用 0禁用';

-- 为已有数据设置默认启用
UPDATE sys_user SET status = 1 WHERE status IS NULL;