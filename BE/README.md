# 교내 공지 푸시 알림 백엔드

삼육대학교 교내 공지를 수집하고, 저장된 공지를 API로 제공하기 위한 Spring Boot 백엔드입니다. 현재 구현 범위는 **공지 크롤링, 중복 제거, DB 저장, 공지 조회 API, 정기 크롤링 스케줄러**입니다.

사용자, 키워드, 인증, 푸시 알림 전송 등 크롤링이 아닌 영역은 다른 팀원이 담당할 수 있도록 패키지와 통합 지점을 분리해 둡니다.

## 기술 스택

- Java 17
- Spring Boot 4.0.6
- Gradle
- Spring WebMVC
- Spring Data JPA
- H2 Database
- Jsoup
- JUnit Platform

## 현재 구현 범위

### 공지 크롤링

`NoticeCrawler`가 삼육대학교 공지 목록 페이지를 요청하고 HTML을 파싱합니다.

현재 대상 게시판:

- 학사공지
- 행사공지
- 생활공지
- 취업·창업공지
- 외부공지
- 추천채용
- 채용공고

크롤링 대상 URL과 페이지네이션 메모는 루트의 `docs/syu-crawl-targets.md`를 참고합니다.

### 공지 저장

`NoticeService`는 크롤링 결과를 받아 URL 기준으로 신규 공지만 저장합니다.

저장 흐름:

```text
NoticeCrawler
-> List<Notice>
-> NoticeService
-> NoticeRepository.findExistingUrls(...)
-> NoticeEntity.from(...)
-> NoticeRepository.saveAll(...)
```

중복 기준은 원본 공지 URL입니다. 같은 URL이 이미 DB에 있으면 다시 저장하지 않습니다.

주의: 현재 중복 방지는 `NoticeRepository.findExistingUrls(...)` 조회 결과를 기준으로 애플리케이션 레벨에서만 수행합니다. `notice.url` 컬럼에 DB unique constraint가 없으므로, 스케줄러와 테스트용 API가 동시에 실행되거나 두 트랜잭션이 같은 URL을 동시에 신규로 판단하면 중복 저장이 발생할 수 있습니다. 운영 DB로 전환할 때는 URL unique index를 추가하는 것이 필요합니다.

### 공지 조회 API

현재 제공 API:

```http
GET /notices
```

동작:

1. 공지 게시판들을 크롤링합니다.
2. 신규 공지를 DB에 저장합니다.
3. 저장된 공지 목록을 최신순으로 반환합니다.

응답 DTO:

```json
[
  {
    "id": "12345",
    "title": "공지 제목",
    "category": "학사",
    "author": "학사지원팀",
    "publishedDate": null,
    "url": "https://www.syu.ac.kr/...",
    "source": "학사"
  }
]
```

### 정기 크롤링

`NoticeCrawlScheduler`가 매시 정각마다 공지 크롤링과 신규 공지 저장을 실행합니다.

```text
cron: 0 0 * * * *
zone: Asia/Seoul
```

스케줄링은 `DemoApplication`의 `@EnableScheduling`으로 활성화되어 있습니다.

## 다른 파트와의 통합 지점

크롤링 파트는 공지를 수집하고 저장하는 역할까지만 담당합니다. 사용자, 키워드, 알림 담당자는 아래 지점을 기준으로 연동하면 됩니다.

### 사용자/인증 파트

예정 위치:

- `controller/UserController.java`
- `service/UserService.java`
- `entity/User.java`
- `repository/UserRepository.java`
- `dto/LoginRequest.java`
- `dto/SignupRequest.java`
- `dto/UserResponse.java`

현재 위 파일들은 생성되어 있으나 대부분 비어 있습니다. 사용자 파트는 공지 크롤링 로직에 직접 의존하지 않고, 사용자 식별자와 키워드/푸시 토큰 관계를 별도로 설계하는 것이 좋습니다.

### 키워드 파트

예정 위치:

- `entity/Keyword.java`
- `repository/KeywordRepository.java`

권장 통합 방식:

- 사용자가 등록한 키워드는 `NoticeEntity.keyword`, `NoticeEntity.title`, `NoticeEntity.department`, `NoticeEntity.content` 중 필요한 필드와 매칭합니다.
- 크롤링 저장 로직은 신규 공지를 저장하는 데 집중하고, 키워드 매칭/알림 대상 선정은 별도 서비스에서 처리합니다.
- 알림 처리 완료 여부는 `NoticeEntity.processed` 필드를 활용할 수 있습니다.

### 푸시 알림 파트

프론트엔드는 FCM 토큰 등록 API로 아래 경로를 기대하고 있습니다.

```http
POST /notifications/token
```

요청 예시:

```json
{
  "token": "fcm-token"
}
```

백엔드에는 아직 해당 API가 없습니다. 알림 담당자는 토큰 저장용 엔티티와 repository를 추가한 뒤, 신규 공지와 사용자 키워드를 매칭해 FCM 전송을 붙이면 됩니다.

권장 흐름:

```text
NoticeCrawlScheduler
-> NoticeService.crawlAndSaveLatestNotices()
-> 신규 NoticeEntity 저장
-> NotificationService가 미처리 공지 조회
-> 사용자 키워드와 매칭
-> FCM 발송
-> NoticeEntity.processed = true
```

현재 `NoticeEntity`에는 setter가 없으므로 `processed` 상태 변경이 필요하면 전용 메서드를 추가하는 방식이 적합합니다.

## 프로젝트 구조

```text
BE/
├── README.md
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/example/demo/
    │   │       ├── DemoApplication.java
    │   │       ├── adapter/
    │   │       │   └── NoticeAdapter.java
    │   │       ├── controller/
    │   │       │   ├── NoticeController.java
    │   │       │   └── UserController.java
    │   │       ├── crawler/
    │   │       │   └── NoticeCrawler.java
    │   │       ├── dto/
    │   │       │   ├── LoginRequest.java
    │   │       │   ├── Notice.java
    │   │       │   ├── SignupRequest.java
    │   │       │   └── UserResponse.java
    │   │       ├── entity/
    │   │       │   ├── Keyword.java
    │   │       │   ├── NoticeEntity.java
    │   │       │   └── User.java
    │   │       ├── repository/
    │   │       │   ├── KeywordRepository.java
    │   │       │   ├── NoticeRepository.java
    │   │       │   └── UserRepository.java
    │   │       ├── scheduler/
    │   │       │   └── NoticeCrawlScheduler.java
    │   │       └── service/
    │   │           ├── NoticeService.java
    │   │           └── UserService.java
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/com/example/demo/
            └── DemoApplicationTests.java
```

## 주요 클래스

- `DemoApplication`
  - Spring Boot 애플리케이션 진입점입니다.
  - 스케줄링을 활성화합니다.

- `NoticeController`
  - `/notices` API를 제공합니다.

- `NoticeService`
  - 크롤링 실행, 신규 공지 저장, 저장된 공지 조회를 담당합니다.

- `NoticeCrawler`
  - 삼육대학교 공지 목록 HTML을 요청하고 `Notice` DTO로 변환합니다.

- `Notice`
  - 크롤링 결과와 API 응답에 사용하는 공지 DTO입니다.

- `NoticeEntity`
  - DB에 저장되는 공지 JPA 엔티티입니다.

- `NoticeRepository`
  - 공지 조회와 기존 URL 확인을 담당합니다.

- `NoticeCrawlScheduler`
  - 정기 크롤링을 실행합니다.

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

현재 DB는 H2 인메모리 DB입니다. 애플리케이션을 재시작하면 저장 데이터가 초기화됩니다. 팀 통합 단계에서 MySQL, PostgreSQL 등 영속 DB로 변경할 경우 datasource 설정과 JPA ddl 전략을 함께 조정해야 합니다.

주의: 현재 H2는 개발용 임시 DB로 사용 중이며 `spring.h2.console.enabled=true`도 개발 편의를 위한 설정입니다. 팀 통합 이후에는 MySQL, PostgreSQL 등 실제 DB에 연결하도록 datasource, 계정 정보, JPA ddl 전략, H2 콘솔 비활성화 여부를 별도로 추가 작업해야 합니다.

## 실행 방법

백엔드 실행:

```bash
./gradlew bootRun
```

테스트:

```bash
./gradlew test
```

루트 디렉토리에서 실행하는 경우:

```bash
cd BE
./gradlew bootRun
```

## 개발 시 주의사항

- 크롤링 대상 URL을 변경할 때는 `docs/syu-crawl-targets.md`도 함께 갱신합니다.
- 학교 홈페이지 HTML 구조가 바뀌면 `NoticeCrawler.parseNoticeList(...)`와 selector를 먼저 확인합니다.
- 크롤링 실패는 `IllegalStateException`으로 전파됩니다. API 오류 응답 정책은 통합 단계에서 공통 예외 처리와 함께 정리하는 것이 좋습니다.
- 신규 공지 판단 기준은 URL입니다. URL 정규화 로직을 변경하면 중복 저장 정책이 달라질 수 있습니다.
- 사용자/키워드/알림 파트는 크롤러 내부 구현에 직접 의존하기보다 `NoticeRepository` 또는 별도 service API를 통해 저장된 공지를 조회하는 방식으로 통합합니다.
