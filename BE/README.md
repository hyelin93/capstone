# 교내 공지 푸시 알림 백엔드

삼육대학교 공지사항을 크롤링해 저장하고, 공지 조회, 키워드 관리, 회원가입/로그인, FCM 푸시 토큰 등록과 키워드 기반 알림 발송을 제공하는 Spring Boot 백엔드입니다.

현재 구현은 개발/통합 단계입니다. 인증은 문자열 기반의 단순 가입/로그인만 제공하며, 공지 크롤링과 FCM 발송은 외부 서비스 상태에 영향을 받습니다.

## 기술 스택

- Java 17
- Spring Boot 4.0.6
- Gradle Wrapper
- Spring WebMVC
- Spring Data JPA
- MySQL Connector/J
- H2 Database, 테스트 전용
- Jsoup
- Firebase Admin SDK
- Lombok
- springdoc-openapi

## 빠른 실행

백엔드는 기본적으로 MySQL 연결 정보와 Firebase 설정이 필요합니다. 로컬에서 FCM 없이 API만 확인하려면 `firebase.enabled=false`로 실행합니다.

```bash
cd BE

export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export MYSQL_DATABASE=capstone
export MYSQL_USER=your_user
export MYSQL_PASSWORD=your_password

./gradlew bootRun --args='--firebase.enabled=false'
```

정상 실행되면 기본 포트는 `8080`입니다.

```text
http://127.0.0.1:8080
```

OpenAPI UI는 아래 경로에서 확인할 수 있습니다.

```text
http://127.0.0.1:8080/swagger-ui/index.html
http://127.0.0.1:8080/v3/api-docs
```

테스트 실행:

```bash
cd BE
./gradlew test
```

컴파일 확인:

```bash
cd BE
./gradlew clean compileJava
```

## 설정

메인 설정 파일은 `src/main/resources/application.properties`입니다.

| 항목 | 설명 |
|---|---|
| `MYSQL_HOST` | MySQL 호스트 |
| `MYSQL_PORT` | MySQL 포트 |
| `MYSQL_DATABASE` | 사용할 데이터베이스 이름 |
| `MYSQL_USER` | DB 사용자 |
| `MYSQL_PASSWORD` | DB 비밀번호 |
| `firebase.enabled` | Firebase 초기화 여부. 기본값은 활성화입니다. |

현재 메인 프로파일은 MySQL을 사용합니다.

```properties
spring.datasource.url=jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DATABASE}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.username=${MYSQL_USER}
spring.datasource.password=${MYSQL_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
spring.h2.console.enabled=false
```

테스트는 `src/test/resources/application.properties`에서 H2 인메모리 DB와 `firebase.enabled=false`를 사용합니다.

### Firebase

`firebase.enabled`가 `true`이거나 생략되면 `src/main/resources/firebase-service-key.json`을 읽어 Firebase Admin SDK를 초기화합니다. 이 파일은 현재 저장소에 포함되어 있지 않습니다.

로컬에서 푸시 발송까지 테스트하려면 Firebase 서비스 계정 JSON을 위 경로에 두고 실행합니다. 단순 API 개발이나 테스트 실행만 필요하면 아래처럼 Firebase를 비활성화합니다.

```bash
./gradlew bootRun --args='--firebase.enabled=false'
```

## 주요 기능

### 공지 크롤링

`NoticeCrawler`가 삼육대학교 공지 목록 HTML을 Jsoup으로 가져오고, 각 상세 페이지 본문을 읽어 `Notice` DTO로 변환합니다.

현재 크롤링 대상:

- 학사공지
- 행사공지
- 생활공지
- 취업·창업공지
- 외부공지
- 추천채용
- 채용공고

크롤링 정책:

- 게시판별 최대 3회 재시도합니다.
- 한 게시판이 최종 실패해도 나머지 게시판 크롤링은 계속합니다.
- 같은 크롤링 결과 안에서는 URL 기준으로 중복 제거합니다.
- URL은 query string과 fragment를 제거해 정규화합니다.
- 원본 공지 ID는 URL path의 마지막 segment에서 추출합니다.
- 공지 본문은 `.single_cont`, `.single_contbx`, `article .entry-content`, `.entry-content` 순서로 추출합니다.

### 공지 저장과 조회

`NoticeService`는 크롤링 결과 중 아직 저장되지 않은 URL만 저장합니다. 기존 공지에 본문이 비어 있고 새 크롤링 결과에 본문이 있으면 본문을 보강합니다.

저장 흐름:

```text
NoticeCrawler
-> List<Notice>
-> NoticeService
-> NoticeRepository.findExistingUrls(...)
-> NoticeAdapter.toEntity(...)
-> NoticeRepository.saveAll(...)
```

공지 엔티티는 `notice` 테이블을 사용하며, `url`에 unique constraint를 둡니다.

### 정기 크롤링

`NoticeCrawlScheduler`가 매시 정각마다 공지 크롤링과 신규 공지 저장을 실행합니다.

```text
cron: 0 0 * * * *
zone: Asia/Seoul
```

스케줄링은 `DemoApplication`의 `@EnableScheduling`으로 활성화되어 있습니다.

### 키워드 기반 푸시 알림

신규 공지가 저장될 때 `NotificationService`가 등록된 키워드 목록을 확인합니다. 공지 제목에 키워드가 포함되면 FCM 토큰이 있는 모든 사용자에게 알림을 보냅니다.

현재 키워드는 사용자별 필터가 아니라 전체 키워드 목록으로 동작합니다.

## API

### 공지 목록 조회

```http
GET /notices
```

요청 시 최신 공지를 크롤링하고, 신규 공지를 저장한 뒤 저장된 공지 목록을 반환합니다.

쿼리 파라미터:

| 이름 | 필수 | 설명 |
|---|---:|---|
| `category` | 아니오 | `학사`, `행사`, `생활`, `취창업`, `외부`, `추천채용`, `채용공고`, `전체` |
| `keyword` | 아니오 | 제목, 본문, 부서, 카테고리 키워드 포함 검색 |
| `page` | 아니오 | 0부터 시작하는 페이지 번호 |
| `size` | 아니오 | 페이지 크기 |

예시:

```bash
curl 'http://127.0.0.1:8080/notices?category=학사&page=0&size=10'
```

응답 예시:

```json
[
  {
    "noticeId": 1,
    "id": 1,
    "title": "공지 제목",
    "url": "https://www.syu.ac.kr/blog/example/",
    "link": "https://www.syu.ac.kr/blog/example/",
    "content": "공지 본문",
    "department": "학사지원팀",
    "keyword": "학사",
    "category": "학사",
    "crawledAt": "2026-06-16T15:00:00",
    "date": "2026-06-16",
    "processed": false,
    "originNoticeId": "example"
  }
]
```

### 최신 공지 크롤링 조회

```http
GET /notices/latest
```

`/notices`와 동일하게 최신 공지를 크롤링하고 신규 공지를 저장한 뒤 저장된 전체 공지 목록을 반환합니다. 검색과 페이징 파라미터는 적용하지 않습니다.

### 공지 상세 조회

```http
GET /notices/{id}
```

저장된 공지 ID로 단건 조회합니다. 본문이 비어 있으면 상세 페이지를 다시 크롤링해 본문을 보강합니다. 존재하지 않는 ID는 `404 Not Found`를 반환합니다.

### 테스트 공지 생성

```http
POST /notices/test
```

장학금 키워드 알림 테스트용 공지를 생성하고, 키워드가 매칭되면 FCM 알림 발송을 시도합니다. 요청 본문은 필요하지 않습니다.

### 키워드 등록

```http
POST /keywords
Content-Type: application/json

{
  "word": "장학"
}
```

이미 등록된 단어면 런타임 예외가 발생합니다.

### 키워드 목록 조회

```http
GET /keywords
```

응답 예시:

```json
[
  {
    "id": 1,
    "word": "장학"
  }
]
```

### 키워드 삭제

```http
DELETE /keywords/{id}
```

성공 시 문자열 `"키워드 삭제 완료"`를 반환합니다.

### 회원가입

```http
POST /users/signup
Content-Type: application/json

{
  "username": "student1",
  "password": "password"
}
```

응답:

```text
회원가입 성공
```

이미 존재하는 아이디면 아래 문자열을 반환합니다.

```text
이미 존재하는 아이디입니다.
```

### 로그인

```http
POST /users/login
Content-Type: application/json

{
  "username": "student1",
  "password": "password"
}
```

가능한 응답:

```text
로그인 성공
존재하지 않는 아이디입니다.
비밀번호가 틀렸습니다.
```

### 푸시 토큰 등록

```http
POST /notifications/token
Content-Type: application/json

{
  "username": "student1",
  "token": "fcm-registration-token"
}
```

성공 시 문자열 `"푸시 토큰 등록 성공"`을 반환합니다. `username` 또는 `token`이 비어 있으면 `400 Bad Request`, 사용자가 없으면 `404 Not Found`를 반환합니다.

## 데이터 모델

현재 애플리케이션이 JPA로 사용하는 주요 테이블은 아래와 같습니다.

| 엔티티 | 테이블 | 설명 |
|---|---|---|
| `NoticeEntity` | `notice` | 크롤링된 공지 저장. `url` unique constraint 보유 |
| `User` | `users` | 사용자 계정과 FCM 토큰 저장 |
| `Keyword` | `keyword` | 알림 매칭용 키워드 저장 |

루트의 `schema.sql`, `data.sql`은 별도 DB 설계/초기 데이터 스크립트입니다. 현재 Spring Boot 설정에서는 이 파일들이 자동 로드되지 않으며, JPA 엔티티 모델과 일부 테이블명이 다릅니다.

## 프로젝트 구조

```text
BE/
├── build.gradle
├── settings.gradle
├── src/
│   ├── main/
│   │   ├── java/com/example/demo/
│   │   │   ├── DemoApplication.java
│   │   │   ├── adapter/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── crawler/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── repository/
│   │   │   ├── scheduler/
│   │   │   └── service/
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       ├── java/com/example/demo/
│       └── resources/application.properties
└── README.md
```

## 주요 클래스

- `DemoApplication`: Spring Boot 진입점, 스케줄링 활성화
- `NoticeController`: 공지 목록, 상세, 최신 크롤링, 테스트 공지 API
- `NoticeService`: 공지 크롤링, 신규 저장, 검색/페이징, 상세 본문 보강
- `NoticeCrawler`: 학교 공지 HTML 요청과 파싱
- `NoticeAdapter`: `Notice` DTO와 `NoticeEntity` 변환
- `NoticeRepository`: 공지 최신순 조회, URL 중복 확인
- `NoticeCrawlScheduler`: 매시 정각 크롤링 실행
- `KeywordController`, `KeywordService`: 키워드 등록, 조회, 삭제
- `UserController`, `UserService`: 개발용 회원가입과 로그인
- `NotificationController`, `NotificationService`, `FcmService`: FCM 토큰 등록과 알림 발송

## 개발 시 주의사항

- `/notices`와 `/notices/latest`는 실제 학교 사이트에 HTTP 요청을 보냅니다. 네트워크 상태나 학교 사이트 HTML 변경에 따라 응답 시간이 길어지거나 실패할 수 있습니다.
- 학교 홈페이지 HTML 구조가 바뀌면 `NoticeCrawler`의 selector를 먼저 확인합니다.
- Firebase를 활성화한 상태에서 `firebase-service-key.json`이 없으면 애플리케이션 시작이 실패합니다.
- 사용자 API는 보안 처리가 없는 개발용 구현입니다. 실제 서비스에서는 비밀번호 해싱, 인증 토큰, 입력 검증, 오류 응답 형식을 추가해야 합니다.
- 현재 키워드 알림은 사용자별 키워드가 아니라 전체 키워드를 기준으로 모든 FCM 토큰 보유 사용자에게 발송됩니다.
