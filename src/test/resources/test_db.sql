CREATE TABLE users (
    id INT PRIMARY KEY,
    username VARCHAR(32) NOT NULL,
    rating INT NOT NULL
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
    (0, 'user0', 0),
    (1, 'user1', 3),
    (2, 'user1', 5);

INSERT INTO groups VALUES
    (0, 'group1', 'First group'),
    (1, 'group2', 'Second group');
