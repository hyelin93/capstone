# 교내 공지 푸시 알림 백엔드

삼육대학교 교내 공지를 크롤링해 저장하고, 공지 조회, 키워드 관리, 간단한 사용자 가입/로그인 API를 제공하는 Spring Boot 백엔드입니다.

현재 구현은 개발/통합 단계용입니다. DB는 H2 인메모리를 사용하므로 애플리케이션을 재시작하면 저장 데이터가 초기화됩니다.

## 기술 스택

- Java 17
- Spring Boot 4.0.6
- Gradle
- Spring WebMVC
- Spring Data JPA
- H2 Database
- Jsoup
- Lombok
- springdoc-openapi

## 빠른 실행

루트에서 실행하는 경우:

```bash
cd BE
./gradlew bootRun
```

정상 실행되면 기본 포트는 `8080`입니다.

```text
http://127.0.0.1:8080
```

OpenAPI 문서는 아래 경로에서 확인할 수 있습니다.

```text
http://127.0.0.1:8080/swagger-ui/index.html
http://127.0.0.1:8080/v3/api-docs
```

테스트:

```bash
cd BE
./gradlew test
```

컴파일만 확인:

```bash
cd BE
./gradlew clean compileJava
```

## 설정

기본 설정 파일은 `src/main/resources/application.properties`입니다.

```properties
spring.application.name=demo
spring.datasource.url=jdbc:h2:mem:notice-db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
spring.h2.console.enabled=true
```

H2 콘솔은 개발 편의용입니다.

```text
http://127.0.0.1:8080/h2-console
```

JDBC URL은 `jdbc:h2:mem:notice-db`를 사용합니다.

## 주요 기능

### 공지 크롤링

`NoticeCrawler`는 삼육대학교 공지 목록 HTML을 Jsoup으로 요청하고, 각 공지 상세 페이지 본문까지 읽어 `Notice` DTO로 변환합니다.

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
- 한 게시판이 최종 실패해도 다른 게시판 크롤링은 계속 진행합니다.
- 같은 크롤링 결과 안에서는 URL 기준으로 중복 제거합니다.
- URL은 쿼리 문자열과 fragment를 제거해 canonicalize합니다.
- 원본 공지 ID는 URL 마지막 path segment에서 추출합니다.
- 공지 본문은 상세 페이지의 `.single_cont` 영역을 우선 사용합니다.

크롤링 대상 URL과 페이지네이션 메모는 루트의 `docs/syu-crawl-targets.md`를 참고합니다.

### 공지 저장

`NoticeService`는 크롤링 결과를 받아 아직 DB에 없는 신규 공지만 저장합니다.

저장 흐름:

```text
NoticeCrawler
-> List<Notice>
-> NoticeService
-> NoticeRepository.findExistingUrls(...)
-> NoticeEntity.from(...)
-> NoticeRepository.saveAll(...)
```

중복 저장 판단 기준은 `url`입니다. 현재는 애플리케이션 레벨에서 기존 URL을 조회해 중복을 제외합니다. DB unique index는 아직 없습니다.

### 정기 크롤링

`NoticeCrawlScheduler`는 매시 정각마다 공지 크롤링과 신규 공지 저장을 실행합니다.

```text
cron: 0 0 * * * *
zone: Asia/Seoul
```

스케줄링은 `DemoApplication`의 `@EnableScheduling`으로 활성화되어 있습니다.

## API

### 공지

#### 공지 목록 조회

```http
GET /notices
```

이 API는 요청 시 공지를 크롤링하고, 신규 공지를 저장한 뒤 저장된 공지 목록을 반환합니다.

쿼리 파라미터:

| 이름 | 필수 | 설명 |
|---|---:|---|
| `category` | 아니오 | `학사`, `행사`, `생활`, `취창업`, `외부`, `추천채용`, `채용공고`, `전체` 등 |
| `keyword` | 아니오 | 제목, 본문, 부서, 키워드에서 포함 검색 |
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
    "title": "공지 제목",
    "url": "https://www.syu.ac.kr/blog/example/",
    "content": "공지 본문 내용",
    "department": "학사지원팀",
    "keyword": "학사",
    "crawledAt": "2026-06-06T17:00:00",
    "processed": false,
    "originNoticeId": "example",
    "id": 1,
    "link": "https://www.syu.ac.kr/blog/example/",
    "category": "학사",
    "date": "2026-06-06"
  }
]
```

#### 최신 공지 크롤링 조회

```http
GET /notices/latest
```

`/notices`와 마찬가지로 공지를 크롤링하고 신규 공지를 저장한 뒤 저장된 전체 공지 목록을 반환합니다. 검색/페이징 파라미터는 적용하지 않습니다.

#### 공지 상세 조회

```http
GET /notices/{id}
```

저장된 공지 ID로 단건 조회합니다. 존재하지 않으면 `404 Not Found`를 반환합니다.

### 키워드

#### 키워드 등록

```http
POST /keywords
Content-Type: application/json

{
  "word": "장학"
}
```

이미 등록된 단어면 런타임 예외가 발생합니다.

#### 키워드 목록

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

#### 키워드 삭제

```http
DELETE /keywords/{id}
```

성공 시 문자열 `"키워드 삭제 완료"`를 반환합니다.

### 사용자

현재 사용자 API는 개발용 단순 구현입니다. 비밀번호 암호화, 세션/JWT, 권한 처리는 아직 없습니다.

#### 회원가입

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

#### 로그인

```http
POST /users/login
Content-Type: application/json

{
  "username": "student1",
  "password": "password"
}
```

응답:

```text
로그인 성공
```

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
│       └── java/com/example/demo/
└── README.md
```

## 주요 클래스

- `DemoApplication`
  - Spring Boot 진입점입니다.
  - `@EnableScheduling`으로 정기 크롤링을 활성화합니다.

- `NoticeController`
  - `/notices`, `/notices/latest`, `/notices/{id}` API를 제공합니다.

- `NoticeService`
  - 공지 크롤링, 신규 공지 저장, 저장된 공지 조회, 검색/페이징 필터를 담당합니다.

- `NoticeCrawler`
  - 학교 공지 목록 HTML 요청, 파싱, URL 정규화, 게시판별 재시도 처리를 담당합니다.

- `NoticeEntity`
  - DB에 저장되는 공지 JPA 엔티티입니다.

- `NoticeRepository`
  - 공지 최신순 조회와 기존 URL 조회를 담당합니다.

- `NoticeCrawlScheduler`
  - 매시 정각 공지 크롤링을 실행합니다.

- `KeywordController`, `KeywordService`
  - 키워드 등록, 조회, 삭제를 담당합니다.

- `UserController`, `UserService`
  - 개발용 회원가입/로그인을 담당합니다.

## 개발 시 주의사항

- `/notices`와 `/notices/latest`는 실제 학교 사이트에 HTTP 요청을 보냅니다. 네트워크 상태나 학교 사이트 HTML 변경에 따라 응답 시간이 길어지거나 실패할 수 있습니다.
- 학교 홈페이지 HTML 구조가 바뀌면 `NoticeCrawler.parseNoticeList(...)`의 selector를 먼저 확인합니다.
- 크롤링 대상 URL을 변경할 때는 루트의 `docs/syu-crawl-targets.md`도 함께 갱신합니다.
- 현재 DB는 H2 인메모리입니다. 운영 DB로 바꿀 때는 datasource, 계정 정보, JPA ddl 전략, H2 콘솔 비활성화 여부를 함께 조정해야 합니다.
- `notice.url`에 DB unique constraint가 없습니다. 스케줄러와 API가 동시에 같은 신규 공지를 저장하려 하면 중복 저장 가능성이 있습니다.
- 사용자 API는 보안 처리가 없는 개발용 구현입니다. 실제 서비스에서는 비밀번호 해싱, 인증 토큰, 입력 검증, 오류 응답 형식을 추가해야 합니다.
- 푸시 알림 토큰 등록 API인 `POST /notifications/token`은 아직 백엔드에 구현되어 있지 않습니다.
