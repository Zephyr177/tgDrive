
-- 修改role列为NOT NULL（如果还不是的话）
-- SQLite不支持直接修改列约束，所以我们通过重建表来实现
CREATE TABLE users_new (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    role TEXT NOT NULL,
    reserved_1 TEXT DEFAULT NULL,
    reserved_2 TEXT DEFAULT NULL,
    reserved_3 TEXT DEFAULT NULL
);

-- 删除旧表
DROP TABLE IF EXISTS users;

-- 重命名新表
ALTER TABLE users_new RENAME TO users;

-- 确保admin和visitor账户存在且密码使用Argon2加密
-- admin密码: 123456
-- visitor密码: hello

-- 更新或插入admin账户
INSERT OR REPLACE INTO users (id, username, password, role)
VALUES (
    (SELECT id FROM users WHERE username = 'admin'),
    'admin',
    '$argon2id$v=19$m=16384,t=2,p=1$PR7R68i1IaECQu3O5b6meg$YwySH08w432Za4Igj+zNcAaJZ2D1Qs5UgfROIXb/+u4',
    'admin'
    );

-- 更新或插入visitor账户
INSERT OR REPLACE INTO users (id, username, password, role)
VALUES (
    (SELECT id FROM users WHERE username = 'visitor'),
    'visitor',
    '$argon2id$v=19$m=16384,t=2,p=1$6yZI0SIgjFCjqjChnnPYRQ$KYy5FZmZnMxCGjGVWWzd6DB8g8DczIUrOlANXaFnyts',
    'visitor'
    );