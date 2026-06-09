CREATE TABLE `Category` (
	`category_id`	INT	NOT NULL,
	`category_name`	VARCHAR	NULL
);

CREATE TABLE `User_Keyword` (
	`Keyword_Id`	INT	NOT NULL,
	`user_id`	INT	NOT NULL	COMMENT '사용자 ID',
	`category_id`	INT	NOT NULL
);

CREATE TABLE `Push_Log` (
	`log_id`	INT	NULL,
	`user_id`	INT	NOT NULL	COMMENT '사용자 ID',
	`notice_id`	INT	NOT NULL	COMMENT '삼육대학교 홈페이지의 원본 게시글 고유 번호',
	``	VARCHAR(255)	NOT NULL,
	`status`	VARCHAR(20)	NULL
);

CREATE TABLE `Notice` (
	`notice_id`	INT	NOT NULL	COMMENT '삼육대학교 홈페이지의 원본 게시글 고유 번호',
	`category_id`	INT	NOT NULL,
	`title`	VARCHAR(255)	NULL	COMMENT '공지 제목',
	`url`	VARCHAR(512)	NULL,
	`content`	TEXT	NULL,
	`department`	VARCHAR(50)	NULL,
	`crawled_at`	DATETIME	NULL,
	`is_processed`	Boolean	NULL,
	`origin_notice_id`	VARCHAR(50)	NULL
);

CREATE TABLE `User` (
	`user_id`	INT	NOT NULL	COMMENT '사용자 ID',
	`push_token`	VARCHAR(255)	NULL,
	`password`	VARCHAR(255)	NULL,
	`login_id`	VARCHAR	NULL
);

ALTER TABLE `Category` ADD CONSTRAINT `PK_CATEGORY` PRIMARY KEY (
	`category_id`
);

ALTER TABLE `User_Keyword` ADD CONSTRAINT `PK_USER_KEYWORD` PRIMARY KEY (
	`Keyword_Id`,
	`user_id`,
	`category_id`
);

ALTER TABLE `Push_Log` ADD CONSTRAINT `PK_PUSH_LOG` PRIMARY KEY (
	`log_id`,
	`user_id`,
	`notice_id`,
	``
);

ALTER TABLE `Notice` ADD CONSTRAINT `PK_NOTICE` PRIMARY KEY (
	`notice_id`,
	`category_id`
);

ALTER TABLE `User` ADD CONSTRAINT `PK_USER` PRIMARY KEY (
	`user_id`
);

ALTER TABLE `User_Keyword` ADD CONSTRAINT `FK_User_TO_User_Keyword_1` FOREIGN KEY (
	`user_id`
)
REFERENCES `User` (
	`user_id`
);

ALTER TABLE `User_Keyword` ADD CONSTRAINT `FK_Category_TO_User_Keyword_1` FOREIGN KEY (
	`category_id`
)
REFERENCES `Category` (
	`category_id`
);

ALTER TABLE `Push_Log` ADD CONSTRAINT `FK_User_TO_Push_Log_1` FOREIGN KEY (
	`user_id`
)
REFERENCES `User` (
	`user_id`
);

ALTER TABLE `Push_Log` ADD CONSTRAINT `FK_Notice_TO_Push_Log_1` FOREIGN KEY (
	`notice_id`
)
REFERENCES `Notice` (
	`notice_id`
);

ALTER TABLE `Push_Log` ADD CONSTRAINT `FK_Notice_TO_Push_Log_2` FOREIGN KEY (
	``
)
REFERENCES `Notice` (
	`category_id`
);

ALTER TABLE `Notice` ADD CONSTRAINT `FK_Category_TO_Notice_1` FOREIGN KEY (
	`category_id`
)
REFERENCES `Category` (
	`category_id`
);

