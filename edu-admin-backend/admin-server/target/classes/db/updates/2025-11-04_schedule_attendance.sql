-- 更新日期：2025-11-04
-- 变更内容：新增考勤记录与课表分享记录两张表，并示例插入

-- 1) 考勤记录表
CREATE TABLE IF NOT EXISTS `attendance_record` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `scheduleId` BIGINT NOT NULL,
  `studentId` BIGINT NOT NULL,
  `signType` INT NOT NULL COMMENT '1:到课 2:迟到 3:早退 4:缺勤',
  `signTime` DATETIME NOT NULL,
  `remark` VARCHAR(255),
  `source` VARCHAR(32),
  `createdAt` DATETIME,
  KEY `idx_attend_schedule` (`scheduleId`),
  KEY `idx_attend_student` (`studentId`),
  KEY `idx_attend_time` (`signTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2) 课表分享记录
CREATE TABLE IF NOT EXISTS `schedule_share` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `dimension` VARCHAR(32) NOT NULL,
  `targetId` VARCHAR(64) NOT NULL,
  `rangeText` VARCHAR(64) NOT NULL,
  `shareToken` VARCHAR(64) NOT NULL,
  `expiresAt` DATETIME,
  `createdBy` VARCHAR(64),
  `createdAt` DATETIME,
  UNIQUE KEY `idx_share_token` (`shareToken`),
  KEY `idx_share_target` (`dimension`, `targetId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3) 示例数据插入（仅供参考，实际数据通过后端初始化接口生成）
-- INSERT INTO `schedule_share` (`dimension`,`targetId`,`rangeText`,`shareToken`,`expiresAt`,`createdBy`,`createdAt`) VALUES
-- ('class','1','2025-11','d7b2e8b7-xxxx-xxxx-xxxx-xxxxxxxx', DATE_ADD(NOW(), INTERVAL 7 DAY), 'system', NOW());
-- INSERT INTO `attendance_record` (`scheduleId`,`studentId`,`signType`,`signTime`,`remark`,`source`,`createdAt`) VALUES
-- (1,1001,1,NOW(),'正常','init_seed',NOW()),
-- (1,1002,2,NOW(),'迟到5分钟','init_seed',NOW()),
-- (1,1003,4,NOW(),'未到课','init_seed',NOW());