-- 1. Category 테이블
CREATE TABLE `Category` (
	`category_id` INT AUTO_INCREMENT NOT NULL,
	`category_name` VARCHAR(50) NOT NULL,
    PRIMARY KEY (`category_id`)
);

-- 2. User 테이블
CREATE TABLE `User` (
	`user_id` INT AUTO_INCREMENT NOT NULL COMMENT '사용자 ID',
	`login_id` VARCHAR(50) NOT NULL,
	`password` VARCHAR(255) NOT NULL,
	`push_token` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`user_id`)
);

-- 3. User_Keyword 테이블
CREATE TABLE `User_Keyword` (
	`Keyword_Id` INT AUTO_INCREMENT NOT NULL,
	`user_id` INT NOT NULL COMMENT '사용자 ID',
	`category_id` INT NOT NULL,
    PRIMARY KEY (`Keyword_Id`),
    CONSTRAINT `FK_User_TO_User_Keyword` FOREIGN KEY (`user_id`) REFERENCES `User` (`user_id`) ON DELETE CASCADE,
    CONSTRAINT `FK_Category_TO_User_Keyword` FOREIGN KEY (`category_id`) REFERENCES `Category` (`category_id`) ON DELETE CASCADE
);

-- 4. Notice 테이블
CREATE TABLE `Notice` (
	`notice_id` INT AUTO_INCREMENT NOT NULL COMMENT '시스템 내부 공지사항 고유 번호',
	`origin_notice_id` VARCHAR(50) NOT NULL COMMENT '학교 홈페이지 원본 글 번호',
	`category_id` INT NOT NULL,
	`title` VARCHAR(255) NOT NULL COMMENT '공지 제목',
	`url` VARCHAR(512) NOT NULL,
	`content` TEXT COMMENT '공지 본문',
	`crawled_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`notice_id`),
    CONSTRAINT `FK_Category_TO_Notice` FOREIGN KEY (`category_id`) REFERENCES `Category` (`category_id`) ON DELETE CASCADE,
    UNIQUE KEY `uk_origin_category` (`origin_notice_id`, `category_id`)
);

-- 5. Push_Log 테이블
CREATE TABLE `Push_Log` (
	`log_id` INT AUTO_INCREMENT NOT NULL,
	`user_id` INT NOT NULL COMMENT '사용자 ID',
	`notice_id` INT NOT NULL COMMENT '발송된 공지사항 고유 번호',
	`status` VARCHAR(20) NOT NULL,
    `sent_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`log_id`),
    CONSTRAINT `FK_User_TO_Push_Log` FOREIGN KEY (`user_id`) REFERENCES `User` (`user_id`) ON DELETE CASCADE,
    CONSTRAINT `FK_Notice_TO_Push_Log` FOREIGN KEY (`notice_id`) REFERENCES `Notice` (`notice_id`) ON DELETE CASCADE
);
