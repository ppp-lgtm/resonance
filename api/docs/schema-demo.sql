-- ============================================================
-- Resonance Demo Data Script
-- 演示数据：管理员账号 + 完整示例内容
-- 数据库: resonance (MySQL)
-- 注意: admin_user.id 固定为 1，确保前台公开接口能正确查询
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. 管理员账号 (BCrypt hash of "admin123")
-- ============================================================
TRUNCATE TABLE `admin_user`;
INSERT INTO `admin_user` (`id`, `username`, `password_hash`, `display_name`, `avatar_url`, `last_login_at`, `created_at`, `updated_at`)
VALUES (1, 'admin', '$2b$10$YQ/Zm.VGNT1Pq1xXrLOpWuJ3yJR5yR6KZ0z.B7sF9hE2xO1qP8mWy', '张明远', NULL, NOW(), NOW(), NOW());

-- ============================================================
-- 2. 个人简介 (profile)
-- ============================================================
TRUNCATE TABLE `profile`;
INSERT INTO `profile` (`id`, `admin_id`, `name`, `titles_json`, `slogan`, `bio`, `location`, `years_experience`, `focus_areas_json`, `working_mode`, `avatar_url`, `resume_url`, `created_at`, `updated_at`)
VALUES (1, 1,
  '张明远',
  '["全栈开发工程师", "UI/UX 设计爱好者", "开源贡献者"]',
  '用代码构建美好数字体验',
  '热爱技术，热爱开源。热衷于用代码解决实际问题。软件工程硕士在读，研究方向：开发者工具链、图形界面工程化。',
  '杭州',
  5,
  '["服务设计", "产品工程", "团队协作", "持续学习"]',
  '远程 / 驻场 / 咨询',
  NULL,
  NULL,
  NOW(), NOW());

-- ============================================================
-- 3. 联系方式 (contact_info)
-- ============================================================
TRUNCATE TABLE `contact_info`;
INSERT INTO `contact_info` (`id`, `admin_id`, `platform`, `icon`, `value_`, `link`, `copyable`, `is_visible`, `sort_order`, `created_at`, `updated_at`) VALUES
(1, 1, '邮箱', '✉️', 'zhangmy@example.com', NULL, 1, 1, 1, NOW(), NOW()),
(2, 1, 'GitHub', '🐙', '@zhangmy', 'https://github.com/zhangmy', 0, 1, 2, NOW(), NOW()),
(3, 1, 'LinkedIn', '💼', '张明远', 'https://linkedin.com/in/zhangmy', 0, 1, 3, NOW(), NOW()),
(4, 1, '个人网站', '🌐', 'zhangmy.dev', 'https://zhangmy.dev', 0, 1, 4, NOW(), NOW());

-- ============================================================
-- 4. 技能清单 (skill)
-- ============================================================
TRUNCATE TABLE `skill`;
INSERT INTO `skill` (`id`, `admin_id`, `name`, `category`, `icon`, `proficiency`, `sort_order`, `is_visible`, `created_at`, `updated_at`) VALUES
(1, 1, 'Vue 3', '前端', '💚', 95, 1, 1, NOW(), NOW()),
(2, 1, 'TypeScript', '前端', '🔷', 90, 2, 1, NOW(), NOW()),
(3, 1, 'Tailwind CSS', '前端', '🎨', 92, 3, 1, NOW(), NOW()),
(4, 1, 'Spring Boot', '后端', '🍃', 88, 4, 1, NOW(), NOW()),
(5, 1, 'Java 17', '后端', '☕', 85, 5, 1, NOW(), NOW()),
(6, 1, 'MySQL', '数据库', '🐬', 82, 6, 1, NOW(), NOW()),
(7, 1, 'Redis', '数据库', '🔴', 78, 7, 1, NOW(), NOW()),
(8, 1, 'Docker', 'DevOps', '🐳', 80, 8, 1, NOW(), NOW()),
(9, 1, 'Figma', '设计', '🖌️', 75, 9, 1, NOW(), NOW());

-- ============================================================
-- 5. 项目作品 (project + project_image)
-- ============================================================
TRUNCATE TABLE `project_image`;
TRUNCATE TABLE `project`;

INSERT INTO `project` (`id`, `admin_id`, `title`, `summary`, `description_`, `tags_json`, `github_url`, `demo_url`, `video_url`, `is_featured`, `is_published`, `sort_order`, `completion_year`, `status_`, `overview`, `created_at`, `updated_at`) VALUES
(1, 1,
  '共鸣 · 个人数字简历系统',
  '基于 Vue 3 + Spring Boot 前后端分离的个人简历平台',
  '## 项目背景\n\n为了向 HR 与技术面试官全面展示能力，我设计并实现了这套前后端分离的数字简历系统。前台采用瑞士杂志风，后台提供可视化 CRUD 管理。\n\n## 技术亮点\n- JWT + Spring Security 安全体系\n- H2 本地快速启动 / MySQL 生产切换\n- 本地上传文件兜底 + 可替换 MinIO\n- 首屏 6 接口合为 1 个 `/api/public/overview`，显著提升 LCP\n',
  '["Vue 3", "TypeScript", "Spring Boot", "Tailwind CSS"]',
  'https://github.com/zhangmy/resonance',
  'https://zhangmy.dev',
  NULL,
  1, 1, 1, NULL, 'ONLINE',
  '一个将个人品牌与作品集完美融合的平台，支持动态内容管理与多端适配。',
  NOW(), NOW()),
(2, 1,
  'CodeLens · 代码评审 Copilot',
  '集成 GitHub App 的 LLM 自动化代码审查平台',
  '## 简介\n\n基于 GitHub App Webhook 监听 PR 事件，调用自研 Agent 自动生成 Review Comments、风险点与可执行建议。\n\n## 功能\n- 自动分析 PR diff\n- 生成中文/英文 Review\n- 安全漏洞检测\n- 代码风格检查\n',
  '["Next.js", "LangChain", "PostgreSQL", "GitHub Apps"]',
  'https://github.com/zhangmy/codelens',
  'https://codelens.dev',
  NULL,
  1, 1, 2, NULL, 'PREPARING',
  '让每个 PR 都得到专业级别的代码审查，提升团队代码质量。',
  NOW(), NOW()),
(3, 1,
  'TinyShop · 微型电商实验',
  '面向大作业的 Spring Boot 单体电商，含支付与库存',
  '## 介绍\n\n课程作业级电商，支持商品、下单、微信沙箱支付、库存一致性。\n\n## 技术栈\n- Spring Boot 3.x\n- MyBatis Plus\n- Redis 缓存\n- 微信支付沙箱\n',
  '["Spring Boot", "MyBatis", "MySQL", "Redis"]',
  'https://github.com/zhangmy/tinyshop',
  NULL,
  NULL,
  0, 1, 3, 2024, 'PLANNING',
  '从零到一搭建的完整电商系统，涵盖商品管理、订单流程、支付对接。',
  NOW(), NOW());

-- 项目图片
INSERT INTO `project_image` (`id`, `project_id`, `url`, `alt_text`, `sort_order`, `created_at`) VALUES
(1, 1, 'https://images.unsplash.com/photo-1517180102446-f3ece451e9d8?w=1200&q=80', '作品平台首页', 0, NOW()),
(2, 2, 'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=1200&q=80', '代码评审截图', 0, NOW()),
(3, 3, 'https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?w=1200&q=80', '商品页面', 0, NOW());

-- ============================================================
-- 6. 荣誉奖项 (award)
-- ============================================================
TRUNCATE TABLE `award`;
INSERT INTO `award` (`id`, `admin_id`, `title`, `issuer`, `award_date`, `description_`, `certificate_url`, `cover_url`, `sort_order`, `created_at`, `updated_at`) VALUES
(1, 1, '全国大学生软件设计大赛 一等奖', '教育部计算机教学指导委员会', '2024-08-01', '作为队长带队完成基于 WebGL 的在线建模平台，全国 2846 支队伍，排名前 0.5%', NULL, NULL, 1, NOW(), NOW()),
(2, 1, 'ICPC 亚洲区域赛 铜牌', 'ACM-ICPC 组委会', '2023-11-01', '现场赛 5/12 题通过，排名 73/428', NULL, NULL, 2, NOW(), NOW()),
(3, 1, '国家励志奖学金', '北京理工大学学生处', '2023-12-01', '专业前 3%，综测年级第 2', NULL, NULL, 3, NOW(), NOW());

-- ============================================================
-- 7. 教育经历 (education)
-- ============================================================
TRUNCATE TABLE `education`;
INSERT INTO `education` (`id`, `admin_id`, `school`, `degree`, `major`, `start_date`, `end_date`, `description_`, `sort_order`, `created_at`, `updated_at`) VALUES
(1, 1, '北京理工大学', '硕士', '软件工程（智能软件开发方向）', '2024-09-01', '2027-06-01', '研究方向：人机交互、开发者工具链、图形用户界面工程化', 1, NOW(), NOW()),
(2, 1, '北京理工大学', '学士', '软件工程', '2020-09-01', '2024-06-01', 'GPA 3.8 / 4.0，专业前 3%，校优秀毕业生', 2, NOW(), NOW());

-- ============================================================
-- 重置 AUTO_INCREMENT 值
-- ============================================================
ALTER TABLE `admin_user` AUTO_INCREMENT = 2;
ALTER TABLE `profile` AUTO_INCREMENT = 2;
ALTER TABLE `contact_info` AUTO_INCREMENT = 5;
ALTER TABLE `skill` AUTO_INCREMENT = 10;
ALTER TABLE `project` AUTO_INCREMENT = 4;
ALTER TABLE `project_image` AUTO_INCREMENT = 4;
ALTER TABLE `award` AUTO_INCREMENT = 4;
ALTER TABLE `education` AUTO_INCREMENT = 3;

SET FOREIGN_KEY_CHECKS = 1;
