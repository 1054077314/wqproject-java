-- Campus Share demo seed data (Flyway V2)
-- Compatible with H2(MODE=MySQL) and MySQL 8

DELETE FROM comments;
DELETE FROM favorites;
DELETE FROM appointments;
DELETE FROM product_images;
DELETE FROM products;
DELETE FROM categories;
DELETE FROM tokens;
DELETE FROM users;

-- users: 9 rows
INSERT INTO users (id, password, last_login, is_superuser, username, is_active, is_staff, created_at) VALUES (1, '$2b$10$n8/MjxwpTE.UMVI5TAo90eBiQ.c9/K33VqS5XGY7XkTwZi4j2vbgu', NULL, FALSE, 'test', TRUE, FALSE, '2026-05-29 03:37:34.743996');
INSERT INTO users (id, password, last_login, is_superuser, username, is_active, is_staff, created_at) VALUES (2, '$2b$10$n8/MjxwpTE.UMVI5TAo90eBiQ.c9/K33VqS5XGY7XkTwZi4j2vbgu', NULL, FALSE, 't', TRUE, FALSE, '2026-05-29 05:01:53.129322');
INSERT INTO users (id, password, last_login, is_superuser, username, is_active, is_staff, created_at) VALUES (3, '$2b$10$J0PKS6DdZC/4mRIGiRxJ.ut5s3ULZZlvaOwOs1f3AuQXb/tGldeiS', NULL, TRUE, 'admin', TRUE, TRUE, '2026-05-29 06:50:39.125892');
INSERT INTO users (id, password, last_login, is_superuser, username, is_active, is_staff, created_at) VALUES (4, '$2b$10$n8/MjxwpTE.UMVI5TAo90eBiQ.c9/K33VqS5XGY7XkTwZi4j2vbgu', NULL, FALSE, 'student1', TRUE, FALSE, '2026-05-29 06:54:08.866490');
INSERT INTO users (id, password, last_login, is_superuser, username, is_active, is_staff, created_at) VALUES (5, '$2b$10$n8/MjxwpTE.UMVI5TAo90eBiQ.c9/K33VqS5XGY7XkTwZi4j2vbgu', NULL, FALSE, 'student2', TRUE, FALSE, '2026-05-29 06:54:09.043269');
INSERT INTO users (id, password, last_login, is_superuser, username, is_active, is_staff, created_at) VALUES (6, '$2b$10$n8/MjxwpTE.UMVI5TAo90eBiQ.c9/K33VqS5XGY7XkTwZi4j2vbgu', NULL, FALSE, 'student3', TRUE, FALSE, '2026-05-29 06:54:09.218240');
INSERT INTO users (id, password, last_login, is_superuser, username, is_active, is_staff, created_at) VALUES (8, '$2b$10$n8/MjxwpTE.UMVI5TAo90eBiQ.c9/K33VqS5XGY7XkTwZi4j2vbgu', NULL, FALSE, 'student4', TRUE, FALSE, '2026-05-29 13:23:00.429417');
INSERT INTO users (id, password, last_login, is_superuser, username, is_active, is_staff, created_at) VALUES (9, '$2b$10$n8/MjxwpTE.UMVI5TAo90eBiQ.c9/K33VqS5XGY7XkTwZi4j2vbgu', NULL, FALSE, 'student5', TRUE, FALSE, '2026-05-29 13:23:00.881693');
INSERT INTO users (id, password, last_login, is_superuser, username, is_active, is_staff, created_at) VALUES (10, '$2b$10$n8/MjxwpTE.UMVI5TAo90eBiQ.c9/K33VqS5XGY7XkTwZi4j2vbgu', NULL, FALSE, 'student6', TRUE, FALSE, '2026-05-29 13:23:01.341061');

-- tokens are intentionally not seeded for public distribution.

-- categories: 8 rows
INSERT INTO categories (id, name, sort_order, created_at) VALUES (1, '运动器材', 30, '2026-05-29 05:01:53.134041');
INSERT INTO categories (id, name, sort_order, created_at) VALUES (2, '电子数码', 10, '2026-05-29 06:52:06.942003');
INSERT INTO categories (id, name, sort_order, created_at) VALUES (3, '图书教材', 20, '2026-05-29 06:52:06.948464');
INSERT INTO categories (id, name, sort_order, created_at) VALUES (4, '生活用品', 40, '2026-05-29 06:52:06.951746');
INSERT INTO categories (id, name, sort_order, created_at) VALUES (5, '服饰鞋包', 50, '2026-05-29 13:23:00.401618');
INSERT INTO categories (id, name, sort_order, created_at) VALUES (6, '宿舍小电器', 70, '2026-05-29 13:23:00.419940');
INSERT INTO categories (id, name, sort_order, created_at) VALUES (7, '美妆个护', 60, '2026-05-29 13:26:49.727573');
INSERT INTO categories (id, name, sort_order, created_at) VALUES (8, '票券卡券', 80, '2026-05-29 13:26:49.730442');

-- products: 24 rows
INSERT INTO products (id, title, description, price, category_id, seller_id, contact_info, `status`, reject_reason, is_deleted, created_at, updated_at) VALUES (1, 'MacBook Pro 2022', '自用笔记本，成色9新，M1芯片，16G内存', 4500, 2, 3, 'wx: student2_campus', 'active', '', FALSE, '2026-05-29 06:54:09.371043', '2026-06-09 04:55:23.987893');
INSERT INTO products (id, title, description, price, category_id, seller_id, contact_info, `status`, reject_reason, is_deleted, created_at, updated_at) VALUES (2, '高等数学同济版', '大一教材，少量笔记，几乎全新', 15, 3, 3, 'wx: student3_campus', 'active', '', FALSE, '2026-05-29 06:54:09.411274', '2026-06-09 04:55:23.987893');
INSERT INTO products (id, title, description, price, category_id, seller_id, contact_info, `status`, reject_reason, is_deleted, created_at, updated_at) VALUES (3, '小米台灯', '护眼台灯，用了半年，功能正常', 45, 6, 3, 'wx: student4_campus', 'active', '', FALSE, '2026-05-29 06:54:09.418942', '2026-06-09 04:55:23.987893');
INSERT INTO products (id, title, description, price, category_id, seller_id, contact_info, `status`, reject_reason, is_deleted, created_at, updated_at) VALUES (4, 'Nike运动鞋42码', '穿了两次，鞋底干净，原价599', 200, 5, 3, 'wx: student5_campus', 'active', '', FALSE, '2026-05-29 06:54:09.425945', '2026-06-09 04:55:23.987893');
INSERT INTO products (id, title, description, price, category_id, seller_id, contact_info, `status`, reject_reason, is_deleted, created_at, updated_at) VALUES (5, '蓝牙耳机AirPods Pro', '降噪耳机，电池健康，配件齐全', 800, 2, 3, 'wx: student6_campus', 'active', '', FALSE, '2026-05-29 06:54:09.433072', '2026-06-09 04:55:23.987893');
INSERT INTO products (id, title, description, price, category_id, seller_id, contact_info, `status`, reject_reason, is_deleted, created_at, updated_at) VALUES (6, 'iPad Air 4 64G 绿色 95新', '屏幕完好，电池健康，适合上课记笔记。', 2800, 2, 3, 'wx: student1_campus', 'active', '', FALSE, '2026-05-29 13:23:01.778961', '2026-06-09 04:55:23.987893');
INSERT INTO products (id, title, description, price, category_id, seller_id, contact_info, `status`, reject_reason, is_deleted, created_at, updated_at) VALUES (7, '考研英语二历年真题详解', '绿皮书套装，少量铅笔标注。', 15, 3, 3, 'wx: student2_campus', 'active', '', FALSE, '2026-05-29 13:23:01.786363', '2026-06-09 04:55:23.987893');
INSERT INTO products (id, title, description, price, category_id, seller_id, contact_info, `status`, reject_reason, is_deleted, created_at, updated_at) VALUES (8, '山地自行车 变速灵敏', '九成新，配锁和挡泥板。', 360, 1, 3, 'wx: student3_campus', 'active', '', FALSE, '2026-05-29 13:23:01.793434', '2026-06-09 04:55:23.987893');
INSERT INTO products (id, title, description, price, category_id, seller_id, contact_info, `status`, reject_reason, is_deleted, created_at, updated_at) VALUES (9, '宿舍收纳箱三件套', '透明款，容量大，搬宿舍多出来的收纳盒。', 28, 4, 3, 'wx: student4_campus', 'active', '', FALSE, '2026-05-29 13:23:01.799904', '2026-06-09 04:55:23.987893');
INSERT INTO products (id, title, description, price, category_id, seller_id, contact_info, `status`, reject_reason, is_deleted, created_at, updated_at) VALUES (10, 'Nike 运动鞋 42码', '穿过两次，鞋底干净。', 199, 5, 3, 'wx: student5_campus', 'active', '', FALSE, '2026-05-29 13:23:01.806466', '2026-06-09 04:55:23.987893');
INSERT INTO products (id, title, description, price, category_id, seller_id, contact_info, `status`, reject_reason, is_deleted, created_at, updated_at) VALUES (11, '罗技无线鼠标 M590', '静音按键，蓝牙和接收器都可用。', 75, 2, 3, 'wx: student6_campus', 'active', '', FALSE, '2026-05-29 13:23:01.812892', '2026-06-09 04:55:23.987893');
INSERT INTO products (id, title, description, price, category_id, seller_id, contact_info, `status`, reject_reason, is_deleted, created_at, updated_at) VALUES (12, '机械键盘 87键 茶轴', '键帽完整，背光正常。', 120, 2, 3, 'wx: student1_campus', 'active', '', FALSE, '2026-05-29 13:23:01.819747', '2026-06-09 04:55:23.987893');
INSERT INTO products (id, title, description, price, category_id, seller_id, contact_info, `status`, reject_reason, is_deleted, created_at, updated_at) VALUES (13, '高数同济第七版上下册', '教材和习题册一起出。', 22, 3, 3, 'wx: student2_campus', 'active', '', FALSE, '2026-05-29 13:23:01.826229', '2026-06-09 04:55:23.987893');
INSERT INTO products (id, title, description, price, category_id, seller_id, contact_info, `status`, reject_reason, is_deleted, created_at, updated_at) VALUES (14, '四六级听力耳机', '学校考试可用，电池新换。', 35, 2, 3, 'wx: student3_campus', 'active', '', FALSE, '2026-05-29 13:23:01.833869', '2026-06-09 04:55:23.987893');
INSERT INTO products (id, title, description, price, category_id, seller_id, contact_info, `status`, reject_reason, is_deleted, created_at, updated_at) VALUES (15, '瑜伽垫加厚防滑', '粉色 8mm，加厚款，使用次数很少。', 30, 1, 3, 'wx: student4_campus', 'active', '', FALSE, '2026-05-29 13:23:01.845027', '2026-06-09 04:55:23.987893');
INSERT INTO products (id, title, description, price, category_id, seller_id, contact_info, `status`, reject_reason, is_deleted, created_at, updated_at) VALUES (16, '吹风机 负离子款', '功率适中，宿舍可用。', 38, 6, 3, 'wx: student5_campus', 'active', '', FALSE, '2026-05-29 13:23:01.852705', '2026-06-09 04:55:23.987893');
INSERT INTO products (id, title, description, price, category_id, seller_id, contact_info, `status`, reject_reason, is_deleted, created_at, updated_at) VALUES (17, '帆布托特包', '容量大，可装电脑和书。', 18, 5, 3, 'wx: student6_campus', 'active', '', FALSE, '2026-05-29 13:23:01.858792', '2026-06-09 04:55:23.987893');
INSERT INTO products (id, title, description, price, category_id, seller_id, contact_info, `status`, reject_reason, is_deleted, created_at, updated_at) VALUES (18, '未拆封洗衣液 2kg', '囤多了，原价购入，未开封。', 19.9, 4, 3, 'wx: student1_campus', 'active', '', FALSE, '2026-05-29 13:23:01.865435', '2026-06-09 04:55:23.987893');
INSERT INTO products (id, title, description, price, category_id, seller_id, contact_info, `status`, reject_reason, is_deleted, created_at, updated_at) VALUES (19, '校园咖啡券 5张', '校内咖啡店通用，本月底前有效。', 42, 8, 3, 'wx: student2_campus', 'active', '', FALSE, '2026-05-29 13:23:01.872406', '2026-06-09 04:55:23.987893');
INSERT INTO products (id, title, description, price, category_id, seller_id, contact_info, `status`, reject_reason, is_deleted, created_at, updated_at) VALUES (20, '篮球 7号 室内外通用', '手感好，气足，边缘轻微磨损。', 55, 1, 3, 'wx: student3_campus', 'active', '', FALSE, '2026-05-29 13:23:01.879513', '2026-06-09 04:55:23.987893');
INSERT INTO products (id, title, description, price, category_id, seller_id, contact_info, `status`, reject_reason, is_deleted, created_at, updated_at) VALUES (21, 'C语言程序设计教材', '课程指定教材，附实验指导书。', 12, 3, 3, 'wx: student4_campus', 'active', '', FALSE, '2026-05-29 13:23:01.886103', '2026-06-09 04:55:23.987893');
INSERT INTO products (id, title, description, price, category_id, seller_id, contact_info, `status`, reject_reason, is_deleted, created_at, updated_at) VALUES (22, '便携显示器 15.6寸', 'Type-C 连接，适合双屏学习，屏幕无坏点。', 520, 2, 3, 'wx: student5_campus', 'pending', '', FALSE, '2026-05-29 13:23:01.893061', '2026-06-09 04:55:23.987893');
INSERT INTO products (id, title, description, price, category_id, seller_id, contact_info, `status`, reject_reason, is_deleted, created_at, updated_at) VALUES (23, '香薰机小夜灯', '宿舍氛围灯，可加湿，配电源线。', 49, 4, 3, 'wx: student6_campus', 'active', '', FALSE, '2026-05-29 13:26:49.822887', '2026-06-09 04:55:23.987893');
INSERT INTO products (id, title, description, price, category_id, seller_id, contact_info, `status`, reject_reason, is_deleted, created_at, updated_at) VALUES (24, '口红小样套装', '色号不合适，试色一次，介意勿拍。', 25, 7, 3, 'wx: student1_campus', 'active', '', FALSE, '2026-05-29 13:26:49.829492', '2026-06-09 04:55:23.987893');

-- product_images: 24 rows
INSERT INTO product_images (id, product_id, image, is_deleted, created_at, updated_at) VALUES (1, 1, 'products/demo_product_01.jpg', FALSE, '2026-05-29 06:54:09.407710', '2026-06-09 04:55:23.998388');
INSERT INTO product_images (id, product_id, image, is_deleted, created_at, updated_at) VALUES (2, 2, 'products/demo_product_02.jpg', FALSE, '2026-05-29 06:54:09.415364', '2026-06-09 04:55:23.998388');
INSERT INTO product_images (id, product_id, image, is_deleted, created_at, updated_at) VALUES (3, 3, 'products/demo_product_03.jpg', FALSE, '2026-05-29 06:54:09.422653', '2026-06-09 04:55:23.998388');
INSERT INTO product_images (id, product_id, image, is_deleted, created_at, updated_at) VALUES (4, 4, 'products/demo_product_04.jpg', FALSE, '2026-05-29 06:54:09.429695', '2026-06-09 04:55:23.998388');
INSERT INTO product_images (id, product_id, image, is_deleted, created_at, updated_at) VALUES (5, 5, 'products/demo_product_05.jpg', FALSE, '2026-05-29 06:54:09.437067', '2026-06-09 04:55:23.998388');
INSERT INTO product_images (id, product_id, image, is_deleted, created_at, updated_at) VALUES (6, 6, 'products/demo_product_06.jpg', FALSE, '2026-05-29 13:23:01.782704', '2026-06-09 04:55:23.998388');
INSERT INTO product_images (id, product_id, image, is_deleted, created_at, updated_at) VALUES (7, 7, 'products/demo_product_07.jpg', FALSE, '2026-05-29 13:23:01.789918', '2026-06-09 04:55:23.998388');
INSERT INTO product_images (id, product_id, image, is_deleted, created_at, updated_at) VALUES (8, 8, 'products/demo_product_08.jpg', FALSE, '2026-05-29 13:23:01.796672', '2026-06-09 04:55:23.998388');
INSERT INTO product_images (id, product_id, image, is_deleted, created_at, updated_at) VALUES (9, 9, 'products/demo_product_09.jpg', FALSE, '2026-05-29 13:23:01.803071', '2026-06-09 04:55:23.998388');
INSERT INTO product_images (id, product_id, image, is_deleted, created_at, updated_at) VALUES (10, 10, 'products/demo_product_10.jpg', FALSE, '2026-05-29 13:23:01.809642', '2026-06-09 04:55:23.998388');
INSERT INTO product_images (id, product_id, image, is_deleted, created_at, updated_at) VALUES (11, 11, 'products/demo_product_11.jpg', FALSE, '2026-05-29 13:23:01.816172', '2026-06-09 04:55:23.998388');
INSERT INTO product_images (id, product_id, image, is_deleted, created_at, updated_at) VALUES (12, 12, 'products/demo_product_12.jpg', FALSE, '2026-05-29 13:23:01.822998', '2026-06-09 04:55:23.998388');
INSERT INTO product_images (id, product_id, image, is_deleted, created_at, updated_at) VALUES (13, 13, 'products/demo_product_13.jpg', FALSE, '2026-05-29 13:23:01.830199', '2026-06-09 04:55:23.998388');
INSERT INTO product_images (id, product_id, image, is_deleted, created_at, updated_at) VALUES (14, 14, 'products/demo_product_14.jpg', FALSE, '2026-05-29 13:23:01.837061', '2026-06-09 04:55:23.998388');
INSERT INTO product_images (id, product_id, image, is_deleted, created_at, updated_at) VALUES (15, 15, 'products/demo_product_15.jpg', FALSE, '2026-05-29 13:23:01.848351', '2026-06-09 04:55:23.998388');
INSERT INTO product_images (id, product_id, image, is_deleted, created_at, updated_at) VALUES (16, 16, 'products/demo_product_16.jpg', FALSE, '2026-05-29 13:23:01.855844', '2026-06-09 04:55:23.998388');
INSERT INTO product_images (id, product_id, image, is_deleted, created_at, updated_at) VALUES (17, 17, 'products/demo_product_17.jpg', FALSE, '2026-05-29 13:23:01.862017', '2026-06-09 04:55:23.998388');
INSERT INTO product_images (id, product_id, image, is_deleted, created_at, updated_at) VALUES (18, 18, 'products/demo_product_18.jpg', FALSE, '2026-05-29 13:23:01.868567', '2026-06-09 04:55:23.998388');
INSERT INTO product_images (id, product_id, image, is_deleted, created_at, updated_at) VALUES (19, 19, 'products/demo_product_19.jpg', FALSE, '2026-05-29 13:23:01.875866', '2026-06-09 04:55:23.998388');
INSERT INTO product_images (id, product_id, image, is_deleted, created_at, updated_at) VALUES (20, 20, 'products/demo_product_20.jpg', FALSE, '2026-05-29 13:23:01.882980', '2026-06-09 04:55:23.998388');
INSERT INTO product_images (id, product_id, image, is_deleted, created_at, updated_at) VALUES (21, 21, 'products/demo_product_21.jpg', FALSE, '2026-05-29 13:23:01.889777', '2026-06-09 04:55:23.998388');
INSERT INTO product_images (id, product_id, image, is_deleted, created_at, updated_at) VALUES (22, 22, 'products/demo_product_22.jpg', FALSE, '2026-05-29 13:23:01.896460', '2026-06-09 04:55:23.998388');
INSERT INTO product_images (id, product_id, image, is_deleted, created_at, updated_at) VALUES (23, 23, 'products/demo_product_23.jpg', FALSE, '2026-05-29 13:26:49.826135', '2026-06-09 04:55:23.998388');
INSERT INTO product_images (id, product_id, image, is_deleted, created_at, updated_at) VALUES (24, 24, 'products/demo_product_24.jpg', FALSE, '2026-05-29 13:26:49.832332', '2026-06-09 04:55:23.998388');

-- appointments: 8 rows
INSERT INTO appointments (id, buyer_id, product_id, `status`, created_at, updated_at) VALUES (1, 5, 1, 'pending', '2026-05-29 06:54:09.493149', '2026-05-30 05:20:06.472152');
INSERT INTO appointments (id, buyer_id, product_id, `status`, created_at, updated_at) VALUES (2, 3, 4, 'pending', '2026-05-29 13:01:08.977709', '2026-05-30 05:20:06.472152');
INSERT INTO appointments (id, buyer_id, product_id, `status`, created_at, updated_at) VALUES (3, 3, 1, 'pending', '2026-05-29 13:19:21.954055', '2026-05-30 05:20:06.472152');
INSERT INTO appointments (id, buyer_id, product_id, `status`, created_at, updated_at) VALUES (4, 4, 24, 'pending', '2026-05-30 04:45:49.290170', '2026-05-30 05:20:06.472152');
INSERT INTO appointments (id, buyer_id, product_id, `status`, created_at, updated_at) VALUES (5, 4, 23, 'confirmed', '2026-05-30 04:58:13.244275', '2026-05-30 05:23:58.372516');
INSERT INTO appointments (id, buyer_id, product_id, `status`, created_at, updated_at) VALUES (6, 4, 21, 'pending', '2026-05-30 05:24:30.636403', '2026-05-30 05:24:30.636426');
INSERT INTO appointments (id, buyer_id, product_id, `status`, created_at, updated_at) VALUES (7, 4, 20, 'rejected', '2026-05-31 04:30:52.224949', '2026-05-31 04:31:14.456584');
INSERT INTO appointments (id, buyer_id, product_id, `status`, created_at, updated_at) VALUES (8, 4, 17, 'confirmed', '2026-05-31 04:32:00.258512', '2026-05-31 04:32:15.349159');

-- favorites: 3 rows
INSERT INTO favorites (id, user_id, product_id, created_at) VALUES (1, 5, 1, '2026-05-29 06:54:09.477574');
INSERT INTO favorites (id, user_id, product_id, created_at) VALUES (2, 5, 2, '2026-05-29 06:54:09.481243');
INSERT INTO favorites (id, user_id, product_id, created_at) VALUES (3, 4, 24, '2026-05-30 04:45:51.692653');

-- comments: 11 rows
INSERT INTO comments (id, user_id, product_id, content, created_at) VALUES (1, 5, 1, '这个还在吗？', '2026-05-29 06:54:09.441715');
INSERT INTO comments (id, user_id, product_id, content, created_at) VALUES (2, 6, 1, '价格能便宜点吗？', '2026-05-29 06:54:09.445369');
INSERT INTO comments (id, user_id, product_id, content, created_at) VALUES (3, 5, 2, '还有其他颜色吗？', '2026-05-29 06:54:09.449156');
INSERT INTO comments (id, user_id, product_id, content, created_at) VALUES (4, 6, 2, '成色怎么样？', '2026-05-29 06:54:09.452672');
INSERT INTO comments (id, user_id, product_id, content, created_at) VALUES (5, 5, 3, '可以包邮吗？', '2026-05-29 06:54:09.456250');
INSERT INTO comments (id, user_id, product_id, content, created_at) VALUES (6, 6, 3, '什么时候方便看货？', '2026-05-29 06:54:09.459542');
INSERT INTO comments (id, user_id, product_id, content, created_at) VALUES (7, 5, 4, '能便宜点吗？', '2026-05-29 06:54:09.463113');
INSERT INTO comments (id, user_id, product_id, content, created_at) VALUES (8, 6, 4, '在哪里交易？', '2026-05-29 06:54:09.467000');
INSERT INTO comments (id, user_id, product_id, content, created_at) VALUES (9, 5, 5, '还有吗？', '2026-05-29 06:54:09.470317');
INSERT INTO comments (id, user_id, product_id, content, created_at) VALUES (10, 6, 5, '可以看看实物吗？', '2026-05-29 06:54:09.473964');
INSERT INTO comments (id, user_id, product_id, content, created_at) VALUES (11, 4, 24, '可以便宜点吗？', '2026-05-30 04:46:00.555069');


