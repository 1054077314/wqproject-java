-- Enrich demo data for richer dashboards / pagination (additive; does not rewrite V2).
-- Passwords: student* / student1234  (same bcrypt as V2)

-- ---------------------------------------------------------------------------
-- Fix status / ownership on existing seed so charts & profile demos look real
-- ---------------------------------------------------------------------------
UPDATE products SET status = 'sold', updated_at = CURRENT_TIMESTAMP
WHERE id IN (17, 23) AND status <> 'sold';

UPDATE products SET status = 'offline', updated_at = CURRENT_TIMESTAMP
WHERE id IN (9, 18) AND status = 'active';

UPDATE products SET status = 'rejected', reject_reason = '图片不清晰，请补充实拍图后重新提交', updated_at = CURRENT_TIMESTAMP
WHERE id = 15 AND status = 'active';

UPDATE products SET seller_id = 4 WHERE id IN (1, 6, 12);
UPDATE products SET seller_id = 5 WHERE id IN (2, 7, 13);
UPDATE products SET seller_id = 6 WHERE id IN (3, 8, 14);
UPDATE products SET seller_id = 8 WHERE id IN (4, 10, 16);
UPDATE products SET seller_id = 9 WHERE id IN (5, 11, 19);
UPDATE products SET seller_id = 10 WHERE id IN (20, 21, 24);

-- ---------------------------------------------------------------------------
-- Extra users (ids 11-20)
-- ---------------------------------------------------------------------------
INSERT INTO users (id, password, last_login, is_superuser, username, is_active, is_staff, created_at) VALUES
(11, '$2b$10$n8/MjxwpTE.UMVI5TAo90eBiQ.c9/K33VqS5XGY7XkTwZi4j2vbgu', NULL, FALSE, 'student7', TRUE, FALSE, CURRENT_TIMESTAMP),
(12, '$2b$10$n8/MjxwpTE.UMVI5TAo90eBiQ.c9/K33VqS5XGY7XkTwZi4j2vbgu', NULL, FALSE, 'student8', TRUE, FALSE, CURRENT_TIMESTAMP),
(13, '$2b$10$n8/MjxwpTE.UMVI5TAo90eBiQ.c9/K33VqS5XGY7XkTwZi4j2vbgu', NULL, FALSE, 'student9', TRUE, FALSE, CURRENT_TIMESTAMP),
(14, '$2b$10$n8/MjxwpTE.UMVI5TAo90eBiQ.c9/K33VqS5XGY7XkTwZi4j2vbgu', NULL, FALSE, 'student10', TRUE, FALSE, CURRENT_TIMESTAMP),
(15, '$2b$10$n8/MjxwpTE.UMVI5TAo90eBiQ.c9/K33VqS5XGY7XkTwZi4j2vbgu', NULL, FALSE, 'student11', TRUE, FALSE, CURRENT_TIMESTAMP),
(16, '$2b$10$n8/MjxwpTE.UMVI5TAo90eBiQ.c9/K33VqS5XGY7XkTwZi4j2vbgu', NULL, FALSE, 'student12', TRUE, FALSE, CURRENT_TIMESTAMP),
(17, '$2b$10$n8/MjxwpTE.UMVI5TAo90eBiQ.c9/K33VqS5XGY7XkTwZi4j2vbgu', NULL, FALSE, 'student13', TRUE, FALSE, CURRENT_TIMESTAMP),
(18, '$2b$10$n8/MjxwpTE.UMVI5TAo90eBiQ.c9/K33VqS5XGY7XkTwZi4j2vbgu', NULL, FALSE, 'student14', TRUE, FALSE, CURRENT_TIMESTAMP),
(19, '$2b$10$n8/MjxwpTE.UMVI5TAo90eBiQ.c9/K33VqS5XGY7XkTwZi4j2vbgu', NULL, FALSE, 'alumni01', TRUE, FALSE, CURRENT_TIMESTAMP),
(20, '$2b$10$n8/MjxwpTE.UMVI5TAo90eBiQ.c9/K33VqS5XGY7XkTwZi4j2vbgu', NULL, FALSE, 'disabled_user', FALSE, FALSE, CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------------------
-- Extra products (25-70): mixed status; some created_at = today for dashboard KPI
-- Reuse existing demo images under media/products/
-- ---------------------------------------------------------------------------
INSERT INTO products (id, title, description, price, category_id, seller_id, contact_info, status, reject_reason, is_deleted, created_at, updated_at) VALUES
(25, '二手显示器 24寸 IPS', '办公学习两用，接口齐全，无明显坏点。', 380.00, 2, 11, 'wx: student7_campus', 'active', '', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(26, '考研数学线代讲义', '张宇线代基础+强化，笔记清晰。', 28.00, 3, 12, 'wx: student8_campus', 'active', '', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(27, '宿舍小风扇 台夹两用', '静音档可用，毕业清仓。', 25.00, 6, 13, 'wx: student9_campus', 'active', '', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(28, '耐克运动短袖 L', '穿过两次，无破损无异味。', 69.00, 5, 14, 'wx: student10_campus', 'active', '', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(29, '未拆封面膜 10片', '囤多了，效期一年以上。', 39.00, 7, 15, 'wx: student11_campus', 'active', '', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(30, '校园食堂月卡余额转让', '余额约 86 元，当面核销。', 80.00, 8, 16, 'wx: student12_campus', 'pending', '', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(31, 'Switch 健身环套装', '环+腿带齐全，主机另出。', 220.00, 1, 17, 'wx: student13_campus', 'active', '', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(32, '人体工学靠垫', '久坐护腰，九成新。', 45.00, 4, 18, 'wx: student14_campus', 'active', '', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(33, '罗技键鼠套装', '无线键鼠，电池新换。', 88.00, 2, 4, 'wx: student1_campus', 'active', '', FALSE, '2026-07-10 10:00:00', '2026-07-10 10:00:00'),
(34, '英语四级词汇闪卡', '墨墨导出打印版，可议价。', 12.00, 3, 5, 'wx: student2_campus', 'active', '', FALSE, '2026-07-11 11:00:00', '2026-07-11 11:00:00'),
(35, '电热水壶 宿舍可用', '防干烧，一年质保内。', 55.00, 6, 6, 'wx: student3_campus', 'active', '', FALSE, '2026-07-12 12:00:00', '2026-07-12 12:00:00'),
(36, '帆布鞋 42码', '米色，鞋底干净。', 49.00, 5, 8, 'wx: student4_campus', 'active', '', FALSE, '2026-07-13 09:00:00', '2026-07-13 09:00:00'),
(37, '防晒霜小样合集', '试色/试用不合适，介意勿拍。', 18.00, 7, 9, 'wx: student5_campus', 'active', '', FALSE, '2026-07-14 14:00:00', '2026-07-14 14:00:00'),
(38, '羽毛球拍双拍套装', '送球三个，手胶新换。', 95.00, 1, 10, 'wx: student6_campus', 'active', '', FALSE, '2026-07-15 15:00:00', '2026-07-15 15:00:00'),
(39, '收纳柜三层', '可拆装，搬宿舍用过一次。', 60.00, 4, 11, 'wx: student7_campus', 'active', '', FALSE, '2026-07-16 16:00:00', '2026-07-16 16:00:00'),
(40, '电影票团购券 2张', '本周末前有效，影城通用。', 50.00, 8, 12, 'wx: student8_campus', 'active', '', FALSE, '2026-07-17 17:00:00', '2026-07-17 17:00:00'),

(41, '机械键盘红轴', '键帽完整，带掌托。', 160.00, 2, 13, 'wx: student9_campus', 'sold', '', FALSE, '2026-06-20 10:00:00', CURRENT_TIMESTAMP),
(42, '高等代数教材', '课程指定教材，几乎全新。', 20.00, 3, 14, 'wx: student10_campus', 'sold', '', FALSE, '2026-06-21 10:00:00', CURRENT_TIMESTAMP),
(43, '台灯护眼国A', '插电即用，亮度三档。', 70.00, 6, 15, 'wx: student11_campus', 'sold', '', FALSE, '2026-06-22 10:00:00', CURRENT_TIMESTAMP),
(44, '羽绒服 M 码', '去年冬天买的，洗过一次。', 150.00, 5, 16, 'wx: student12_campus', 'offline', '', FALSE, '2026-06-01 10:00:00', CURRENT_TIMESTAMP),
(45, '晾衣架落地式', '可折叠，宿舍空间友好。', 35.00, 4, 17, 'wx: student13_campus', 'offline', '', FALSE, '2026-06-02 10:00:00', CURRENT_TIMESTAMP),
(46, '蓝牙音箱小巧款', '音质一般，凑合听播客。', 40.00, 2, 18, 'wx: student14_campus', 'rejected', '疑似非本人实拍，请补充多角度照片', FALSE, '2026-06-03 10:00:00', CURRENT_TIMESTAMP),
(47, '护肤水乳套装', '开封试过一次，余量 95%。', 120.00, 7, 19, 'wx: alumni01_campus', 'rejected', '描述与图片不符', FALSE, '2026-06-04 10:00:00', CURRENT_TIMESTAMP),
(48, '待审核：二手平板', 'iPad 使用痕迹正常，配件盒在。', 1500.00, 2, 4, 'wx: student1_campus', 'pending', '', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(49, '待审核：滑板', '入门级，轴承顺滑。', 180.00, 1, 5, 'wx: student2_campus', 'pending', '', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(50, '待审核：书桌书架', '可拆卸，自提优先。', 90.00, 4, 6, 'wx: student3_campus', 'pending', '', FALSE, '2026-07-20 08:00:00', '2026-07-20 08:00:00'),

(51, 'Type-C 扩展坞', 'HDMI+USB3，笔记本分屏用。', 99.00, 2, 8, 'wx: student4_campus', 'active', '', FALSE, '2026-07-01 08:00:00', '2026-07-01 08:00:00'),
(52, '概率论教材+习题', '两本一起出。', 25.00, 3, 9, 'wx: student5_campus', 'active', '', FALSE, '2026-07-02 08:00:00', '2026-07-02 08:00:00'),
(53, '电吹风折叠款', '旅行便携，功率够用。', 42.00, 6, 10, 'wx: student6_campus', 'active', '', FALSE, '2026-07-03 08:00:00', '2026-07-03 08:00:00'),
(54, '工装裤 30码', '卡其色，版型正常。', 58.00, 5, 11, 'wx: student7_campus', 'active', '', FALSE, '2026-07-04 08:00:00', '2026-07-04 08:00:00'),
(55, '漱口水未开封', '两瓶，效期充足。', 22.00, 7, 12, 'wx: student8_campus', 'active', '', FALSE, '2026-07-05 08:00:00', '2026-07-05 08:00:00'),
(56, '篮球鞋 43码', '外场轻微磨损，内里干净。', 210.00, 5, 13, 'wx: student9_campus', 'active', '', FALSE, '2026-07-06 08:00:00', '2026-07-06 08:00:00'),
(57, '瑜伽砖+伸展带', '粉色套装，几乎全新。', 26.00, 1, 14, 'wx: student10_campus', 'active', '', FALSE, '2026-07-07 08:00:00', '2026-07-07 08:00:00'),
(58, '桌面收纳盒', '亚克力，适合放文具。', 16.00, 4, 15, 'wx: student11_campus', 'active', '', FALSE, '2026-07-08 08:00:00', '2026-07-08 08:00:00'),
(59, '咖啡兑换券 3张', '校内连锁，本月有效。', 36.00, 8, 16, 'wx: student12_campus', 'active', '', FALSE, '2026-07-09 08:00:00', '2026-07-09 08:00:00'),
(60, '移动硬盘 1T', 'USB3.0，健康状态正常。', 180.00, 2, 17, 'wx: student13_campus', 'active', '', FALSE, '2026-06-25 08:00:00', '2026-06-25 08:00:00'),

(61, '线性代数视频课笔记', '手写扫描 PDF 打印版。', 15.00, 3, 18, 'wx: student14_campus', 'active', '', FALSE, '2026-06-26 08:00:00', '2026-06-26 08:00:00'),
(62, '加湿器静音', '上加水，毕业急出。', 65.00, 6, 19, 'wx: alumni01_campus', 'active', '', FALSE, '2026-06-27 08:00:00', '2026-06-27 08:00:00'),
(63, '双肩背包 电脑仓', '可装 15.6，防水涂层。', 79.00, 5, 4, 'wx: student1_campus', 'active', '', FALSE, '2026-06-28 08:00:00', '2026-06-28 08:00:00'),
(64, '哑铃一对 5kg', '家用健身，送手套。', 85.00, 1, 5, 'wx: student2_campus', 'active', '', FALSE, '2026-06-29 08:00:00', '2026-06-29 08:00:00'),
(65, '洗衣液补充装', '未拆封，三袋。', 29.00, 4, 6, 'wx: student3_campus', 'active', '', FALSE, '2026-06-30 08:00:00', '2026-06-30 08:00:00'),
(66, '口红正装豆沙色', '试色一次，盒在。', 88.00, 7, 8, 'wx: student4_campus', 'active', '', FALSE, '2026-05-20 08:00:00', '2026-05-20 08:00:00'),
(67, '跳绳计数款', '轴承顺滑，适合减脂。', 19.00, 1, 9, 'wx: student5_campus', 'active', '', FALSE, '2026-05-21 08:00:00', '2026-05-21 08:00:00'),
(68, 'USB 小台灯', '插电脑就能用。', 15.00, 6, 10, 'wx: student6_campus', 'offline', '', FALSE, '2026-05-22 08:00:00', CURRENT_TIMESTAMP),
(69, '待审核：相机脚架', '轻便旅行脚架，高度可调。', 110.00, 2, 11, 'wx: student7_campus', 'pending', '', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(70, '已售：二手耳机', '降噪耳机，成交归档示例。', 320.00, 2, 12, 'wx: student8_campus', 'sold', '', FALSE, '2026-05-25 08:00:00', CURRENT_TIMESTAMP);

INSERT INTO product_images (id, product_id, image, is_deleted, created_at, updated_at) VALUES
(25, 25, 'products/demo_product_01.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(26, 26, 'products/demo_product_02.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(27, 27, 'products/demo_product_03.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(28, 28, 'products/demo_product_04.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(29, 29, 'products/demo_product_05.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(30, 30, 'products/demo_product_06.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(31, 31, 'products/demo_product_07.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(32, 32, 'products/demo_product_08.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(33, 33, 'products/demo_product_09.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(34, 34, 'products/demo_product_10.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(35, 35, 'products/demo_product_11.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(36, 36, 'products/demo_product_12.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(37, 37, 'products/demo_product_13.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(38, 38, 'products/demo_product_14.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(39, 39, 'products/demo_product_15.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(40, 40, 'products/demo_product_16.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(41, 41, 'products/demo_product_17.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(42, 42, 'products/demo_product_18.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(43, 43, 'products/demo_product_19.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(44, 44, 'products/demo_product_20.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(45, 45, 'products/demo_product_21.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(46, 46, 'products/demo_product_22.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(47, 47, 'products/demo_product_23.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(48, 48, 'products/demo_product_24.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(49, 49, 'products/demo_product_01.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(50, 50, 'products/demo_product_02.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(51, 51, 'products/demo_product_03.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(52, 52, 'products/demo_product_04.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(53, 53, 'products/demo_product_05.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(54, 54, 'products/demo_product_06.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(55, 55, 'products/demo_product_07.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(56, 56, 'products/demo_product_08.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(57, 57, 'products/demo_product_09.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(58, 58, 'products/demo_product_10.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(59, 59, 'products/demo_product_11.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(60, 60, 'products/demo_product_12.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(61, 61, 'products/demo_product_13.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(62, 62, 'products/demo_product_14.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(63, 63, 'products/demo_product_15.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(64, 64, 'products/demo_product_16.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(65, 65, 'products/demo_product_17.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(66, 66, 'products/demo_product_18.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(67, 67, 'products/demo_product_19.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(68, 68, 'products/demo_product_20.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(69, 69, 'products/demo_product_21.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(70, 70, 'products/demo_product_22.jpg', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------------------
-- Appointments / favorites / comments / sample audit rows
-- ---------------------------------------------------------------------------
INSERT INTO appointments (id, buyer_id, product_id, status, created_at, updated_at) VALUES
(9, 5, 25, 'pending', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 12, 25, 'pending', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 13, 33, 'pending', '2026-07-18 10:00:00', '2026-07-18 10:00:00'),
(12, 14, 38, 'confirmed', '2026-07-19 10:00:00', CURRENT_TIMESTAMP),
(13, 16, 41, 'confirmed', '2026-06-28 10:00:00', '2026-06-28 12:00:00'),
(14, 17, 42, 'confirmed', '2026-06-29 10:00:00', '2026-06-29 12:00:00'),
(15, 18, 43, 'confirmed', '2026-06-30 10:00:00', '2026-06-30 12:00:00'),
(16, 18, 56, 'pending', '2026-07-20 10:00:00', '2026-07-20 10:00:00'),
(17, 19, 60, 'rejected', '2026-07-10 10:00:00', '2026-07-11 10:00:00'),
(18, 11, 63, 'cancelled', '2026-07-12 10:00:00', '2026-07-12 11:00:00'),
(19, 12, 64, 'pending', '2026-07-21 10:00:00', '2026-07-21 10:00:00'),
(20, 5, 70, 'confirmed', '2026-05-26 10:00:00', '2026-05-26 12:00:00');

-- product 38 confirmed → mark sold to keep state machine consistent
UPDATE products SET status = 'sold', updated_at = CURRENT_TIMESTAMP WHERE id = 38;

INSERT INTO favorites (id, user_id, product_id, created_at) VALUES
(4, 11, 25, CURRENT_TIMESTAMP),
(5, 11, 26, CURRENT_TIMESTAMP),
(6, 12, 27, CURRENT_TIMESTAMP),
(7, 13, 28, CURRENT_TIMESTAMP),
(8, 14, 31, CURRENT_TIMESTAMP),
(9, 15, 33, CURRENT_TIMESTAMP),
(10, 16, 51, CURRENT_TIMESTAMP),
(11, 17, 56, CURRENT_TIMESTAMP),
(12, 18, 60, CURRENT_TIMESTAMP),
(13, 19, 62, CURRENT_TIMESTAMP),
(14, 4, 25, CURRENT_TIMESTAMP),
(15, 5, 32, CURRENT_TIMESTAMP),
(16, 6, 40, CURRENT_TIMESTAMP);

INSERT INTO comments (id, user_id, product_id, content, created_at) VALUES
(12, 11, 25, '显示器用了多久？有盒子吗？', CURRENT_TIMESTAMP),
(13, 12, 25, '可以小刀吗？校内面交。', CURRENT_TIMESTAMP),
(14, 13, 26, '讲义有电子版吗？', CURRENT_TIMESTAMP),
(15, 14, 31, '健身环有没有划痕？', CURRENT_TIMESTAMP),
(16, 15, 33, '键鼠还在保吗？', '2026-07-11 12:00:00'),
(17, 16, 38, '拍子还在吗？我想预约。', '2026-07-18 12:00:00'),
(18, 17, 48, '平板电池健康多少？', CURRENT_TIMESTAMP),
(19, 18, 56, '球鞋尺码标准吗？', '2026-07-07 12:00:00'),
(20, 19, 60, '硬盘有没有重要数据残留？', '2026-07-01 12:00:00'),
(21, 4, 62, '加湿器噪音大吗？', '2026-07-02 12:00:00'),
(22, 5, 63, '背包能不能装水杯侧袋？', '2026-07-03 12:00:00'),
(23, 6, 25, '求带上支架一起出。', CURRENT_TIMESTAMP);

INSERT INTO audit_logs (id, actor_id, actor_username, action, resource_type, resource_id, detail, created_at) VALUES
(1, 3, 'admin', 'product.approve', 'product', 25, 'seed demo', CURRENT_TIMESTAMP),
(2, 3, 'admin', 'product.reject', 'product', 46, '疑似非本人实拍，请补充多角度照片', '2026-06-03 12:00:00'),
(3, 10, 'student6', 'appointment.confirm', 'appointment', 12, 'product_id=38,sold=true', CURRENT_TIMESTAMP),
(4, 3, 'admin', 'user.disable', 'user', 20, 'disabled_user', CURRENT_TIMESTAMP),
(5, 16, 'student12', 'product.offline', 'product', 44, NULL, '2026-06-05 12:00:00');
