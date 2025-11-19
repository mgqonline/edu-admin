CREATE TABLE IF NOT EXISTS student_leave (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  reason VARCHAR(255) NOT NULL,
  start_time VARCHAR(32) NOT NULL,
  end_time VARCHAR(32) NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS homework_submission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  homework_id BIGINT NOT NULL,
  filename VARCHAR(255),
  size BIGINT,
  url VARCHAR(255),
  created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS sign_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  class_id VARCHAR(64) NOT NULL,
  code TEXT,
  method VARCHAR(16) NOT NULL,
  created_at DATETIME NOT NULL
);

-- 绑定微信 OpenID 与学员 ID
CREATE TABLE IF NOT EXISTS student_wechat (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  openid VARCHAR(128) NOT NULL,
  unionid VARCHAR(128),
  created_at DATETIME NOT NULL,
  UNIQUE KEY uniq_openid (openid)
);

-- 学员成绩记录
CREATE TABLE IF NOT EXISTS student_grade (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  course_id BIGINT,
  type VARCHAR(64),
  score INT,
  exam_date DATE,
  remark VARCHAR(255)
);

-- 支撑课表/缴费/课时/成绩查询的最小依赖表
CREATE TABLE IF NOT EXISTS class_info (
  id BIGINT PRIMARY KEY,
  name VARCHAR(128)
);

CREATE TABLE IF NOT EXISTS classes (
  id BIGINT PRIMARY KEY,
  class_info_id BIGINT,
  course_id BIGINT
);

CREATE TABLE IF NOT EXISTS class_enrollment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  studentId BIGINT NOT NULL,
  classId BIGINT NOT NULL,
  status VARCHAR(32) DEFAULT 'active'
);

CREATE TABLE IF NOT EXISTS schedule_info (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  date_only DATE NOT NULL,
  start_time_text VARCHAR(16),
  end_time_text VARCHAR(16),
  class_id BIGINT,
  course_id BIGINT,
  student_id BIGINT
);

CREATE TABLE IF NOT EXISTS finance_settlement (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  class_id BIGINT,
  total_fee DECIMAL(10,2) DEFAULT 0,
  hours INT DEFAULT 0,
  status VARCHAR(32) DEFAULT 'paid',
  op_type VARCHAR(32),
  method VARCHAR(64),
  created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS attendance_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  schedule_id BIGINT NOT NULL,
  sign_type INT
);

-- 家长留言（学员/家长给老师的消息）
CREATE TABLE IF NOT EXISTS parent_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  teacher_id BIGINT,
  parent_name VARCHAR(128),
  content TEXT NOT NULL,
  reply TEXT,
  status VARCHAR(32) DEFAULT 'open',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  replied_at DATETIME
);

  -- 学员意见反馈
CREATE TABLE IF NOT EXISTS student_feedback (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  content TEXT NOT NULL,
  status VARCHAR(32) DEFAULT 'new',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 教师微信绑定（openid -> teacher_id）
CREATE TABLE IF NOT EXISTS teacher_wechat_binding (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  teacher_id BIGINT NOT NULL,
  openid VARCHAR(64) NOT NULL,
  unionid VARCHAR(64),
  created_at DATETIME NOT NULL,
  UNIQUE KEY uk_openid (openid)
);

-- 教师课时费记录
CREATE TABLE IF NOT EXISTS teacher_fee_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  teacher_id BIGINT NOT NULL,
  course VARCHAR(255),
  amount DECIMAL(10,2) NOT NULL,
  date DATE NOT NULL
);

-- 教师签到会话
CREATE TABLE IF NOT EXISTS teacher_sign_session (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  teacher_id BIGINT NOT NULL,
  class_id VARCHAR(64),
  code VARCHAR(32) NOT NULL,
  expire_minutes INT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 教师作业
CREATE TABLE IF NOT EXISTS teacher_homework (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  teacher_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  content TEXT,
  status VARCHAR(32) NOT NULL,
  publish_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 教师参考答案文件
CREATE TABLE IF NOT EXISTS teacher_answer_file (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  teacher_id BIGINT NOT NULL,
  homework_id BIGINT NULL,
  title VARCHAR(255),
  filename VARCHAR(255),
  size BIGINT,
  url VARCHAR(512),
  uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 教师调课申请
CREATE TABLE IF NOT EXISTS teacher_adjust_request (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  teacher_id BIGINT NOT NULL,
  course VARCHAR(255),
  original_time VARCHAR(64),
  new_time VARCHAR(64),
  reason TEXT,
  status VARCHAR(32),
  submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 教师课堂记录
CREATE TABLE IF NOT EXISTS teacher_class_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  teacher_id BIGINT NOT NULL,
  class_id VARCHAR(64),
  content TEXT,
  performance VARCHAR(255),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 教室申请
CREATE TABLE IF NOT EXISTS teacher_room_request (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  teacher_id BIGINT NOT NULL,
  date_only DATE NOT NULL,
  start_time_text VARCHAR(16),
  end_time_text VARCHAR(16),
  capacity INT,
  reason TEXT,
  status VARCHAR(32) DEFAULT 'pending',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 教学计划
CREATE TABLE IF NOT EXISTS teacher_teach_plan (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  teacher_id BIGINT NOT NULL,
  class_id VARCHAR(64),
  title VARCHAR(255) NOT NULL,
  content TEXT,
  resource_url VARCHAR(512),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 班级通知
CREATE TABLE IF NOT EXISTS teacher_class_notice (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  teacher_id BIGINT NOT NULL,
  class_id VARCHAR(64),
  title VARCHAR(255) NOT NULL,
  content TEXT,
  status VARCHAR(32) DEFAULT 'published',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 教师基本资料
CREATE TABLE IF NOT EXISTS teacher_profile (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  teacher_id BIGINT NOT NULL,
  name VARCHAR(128),
  subjects VARCHAR(255),
  years INT,
  phone VARCHAR(32),
  UNIQUE KEY uk_teacher (teacher_id)
);