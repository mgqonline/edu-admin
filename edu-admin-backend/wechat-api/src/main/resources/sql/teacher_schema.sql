-- Teacher-side tables
CREATE TABLE IF NOT EXISTS teacher_homework (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  teacher_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  content TEXT,
  status VARCHAR(32) NOT NULL,
  publish_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

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

CREATE TABLE IF NOT EXISTS teacher_fee_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  teacher_id BIGINT NOT NULL,
  course VARCHAR(255),
  amount DECIMAL(10,2) NOT NULL,
  date DATE NOT NULL
);

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

CREATE TABLE IF NOT EXISTS teacher_sign_session (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  teacher_id BIGINT NOT NULL,
  class_id VARCHAR(64),
  code VARCHAR(32) NOT NULL,
  expire_minutes INT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 教师微信绑定（openid -> teacher_id）
CREATE TABLE IF NOT EXISTS teacher_wechat_binding (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  teacher_id BIGINT NOT NULL,
  openid VARCHAR(64) NOT NULL,
  unionid VARCHAR(64),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_openid (openid)
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