CREATE TABLE users (
    id INT PRIMARY KEY,
    username VARCHAR(32) NOT NULL,
    rating INT NOT NULL,
    is_admin TINYINT NOT NULL DEFAULT 0,
    is_staff TINYINT NOT NULL DEFAULT 0,
);

CREATE TABLE groups (
    id INT PRIMARY KEY,
    slug VARCHAR(32) NOT NULL,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE members (
    group_id INT NOT NULL,
    user_id INT NOT NULL,
    banned TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE posts (
    id INT PRIMARY KEY,
    user_id INT NOT NULL,
    group_id INT,
    contents TEXT
);

CREATE TABLE group_invites (
    group_id INT NOT NULL,
    user_id INT NOT NULL,
    invited_by_id INT NOT NULL
);


INSERT INTO users VALUES
    (0, 'user0', 0, 0, 0),
    (1, 'user1', 3, 0, 0),
    (2, 'user2', 5, 0, 0),
    (3, 'employee', 5, 0, 1),
    (4, 'admin', 5, 1, 1);


INSERT INTO groups VALUES
    (0, 'group1', 'First group'),
    (1, 'group2', 'Second group');

INSERT INTO members VALUES
    (0, 0, 0),
    (0, 1, 0),
    (0, 2, 0),
    (0, 3, 0),
    (1, 0, 0),
    (1, 3, 0),
    (1, 4, 0);

INSERT INTO posts VALUES
    (0, 0, NULL, 'Simple post by user0'),
    (1, 0, 0, 'Group1 post by user0'),
    (2, 0, 0, 'Another Group1 post by user0'),
    (3, 0, 1, 'Group2 post by user0'),
    (4, 1, 0, 'Group1 post by user1');

