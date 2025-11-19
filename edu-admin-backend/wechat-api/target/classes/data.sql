INSERT IGNORE INTO teacher_wechat_binding(teacher_id, openid, unionid, created_at) VALUES (5, 'dev-openid-teacher-5', NULL, NOW());

INSERT IGNORE INTO teacher_fee_record(teacher_id, course, amount, date) VALUES (5, '英语听力', 320.00, DATE_SUB(CURDATE(), INTERVAL 5 DAY));
INSERT IGNORE INTO teacher_fee_record(teacher_id, course, amount, date) VALUES (5, '数学提高', 450.00, DATE_SUB(CURDATE(), INTERVAL 3 DAY));
INSERT IGNORE INTO teacher_fee_record(teacher_id, course, amount, date) VALUES (5, '物理实验', 280.00, CURDATE());

INSERT IGNORE INTO teacher_class_notice(teacher_id, class_id, title, content, status, created_at) VALUES (5, 'class-001', '周五晚自习安排', '本周五晚自习时间调整至19:00-21:00', 'published', NOW());
INSERT IGNORE INTO teacher_class_notice(teacher_id, class_id, title, content, status, created_at) VALUES (5, 'class-002', '作业提交提醒', '英语听力作业请于本周日24:00前提交', 'published', NOW());

INSERT IGNORE INTO teacher_homework(teacher_id, title, content, status, publish_at) VALUES (5, '英语听力练习三', '完成听力材料第三单元并记录错题', 'published', NOW());
INSERT IGNORE INTO teacher_homework(teacher_id, title, content, status, publish_at) VALUES (5, '数学函数复习', '复习一次函数与二次函数，完成练习册P45-P50', 'published', NOW());

INSERT IGNORE INTO teacher_profile(teacher_id, name, subjects, years, phone) VALUES(5, '王老师', '英语、数学', 6, '13800001234');