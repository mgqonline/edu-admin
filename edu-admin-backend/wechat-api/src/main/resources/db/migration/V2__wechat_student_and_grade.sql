-- 微信学员绑定与学生成绩表（供小程序使用）
CREATE TABLE IF NOT EXISTS student_wechat (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  openid VARCHAR(64) NOT NULL UNIQUE,
  unionid VARCHAR(64),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS student_grade (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  course_id BIGINT,
  type VARCHAR(32), -- 随堂测试/阶段考试等
  score INT,
  exam_date DATETIME,
  remark VARCHAR(255)
);