# 삼육대학교 교내 공지 푸시 알림

삼육대학교 공지사항을 크롤링해 저장하고, 사용자가 공지를 조회하거나 키워드를 등록하면 관련 공지 발생 시 웹 푸시 알림을 받을 수 있도록 만든 캡스톤 프로젝트입니다.

저장소는 Spring Boot 백엔드와 React 프론트엔드를 함께 포함합니다.

## 주요 기능

- 삼육대학교 공지 게시판 크롤링
- 공지 목록, 상세 조회, 카테고리 필터링
- 키워드 등록, 조회, 삭제
- 간단한 회원가입과 로그인
- Firebase Cloud Messaging 기반 웹 푸시 토큰 등록
- 신규 공지 제목이 등록 키워드와 매칭될 때 푸시 알림 발송

## 저장소 구조

```text
.
├── BE/                 # Spring Boot 백엔드
├── FE/                 # React + Vite 프론트엔드
├── schema.sql          # DB 설계 참고용 스키마
├── data.sql            # Category 초기 데이터 참고용 스크립트
└── README.md
```

## 기술 스택

### 백엔드

- Java 17
- Spring Boot 4.0.6
- Spring WebMVC
- Spring Data JPA
- MySQL
- H2, 테스트 전용
- Jsoup
- Firebase Admin SDK
- Gradle

### 프론트엔드

- React 19
- TypeScript
- Vite
- React Router
- TanStack Query
- Axios
- Firebase Web SDK
- React Hook Form
- Zustand

## 빠른 실행

### 1. 백엔드 실행

백엔드는 MySQL 연결 정보를 환경 변수로 받습니다. FCM 없이 로컬 API만 확인하려면 `firebase.enabled=false`로 실행합니다.

```bash
cd BE

export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export MYSQL_DATABASE=capstone
export MYSQL_USER=your_user
export MYSQL_PASSWORD=your_password

./gradlew bootRun --args='--firebase.enabled=false'
```

백엔드 기본 주소:

```text
http://127.0.0.1:8080
```

OpenAPI UI:

```text
http://127.0.0.1:8080/swagger-ui/index.html
```

FCM 발송까지 테스트하려면 Firebase 서비스 계정 파일을 `BE/src/main/resources/firebase-service-key.json`에 둔 뒤 Firebase를 활성화해 실행합니다.

### 2. 프론트엔드 실행

```bash
cd FE
npm install
npm run dev
```

Vite 개발 서버 기본 주소:

```text
http://localhost:5173
```

개발 환경에서는 `FE/vite.config.ts`의 proxy 설정으로 `/notices`, `/keywords`, `/users`, `/notifications` 요청이 `http://localhost:8080` 백엔드로 전달됩니다. 따라서 `FE/.env`의 `VITE_API_BASE_URL`은 빈 값으로 둘 수 있습니다.

## 검증 명령어

백엔드 테스트:

```bash
cd BE
./gradlew test
```

백엔드 컴파일:

```bash
cd BE
./gradlew clean compileJava
```

프론트엔드 빌드:

```bash
cd FE
npm run build
```

프론트엔드 lint:

```bash
cd FE
npm run lint
```

## 주요 API

| Method | Path | 설명 |
|---|---|---|
| `GET` | `/notices` | 공지 크롤링 후 저장된 공지 목록 조회. `category`, `keyword`, `page`, `size` 지원 |
| `GET` | `/notices/latest` | 최신 공지 크롤링 후 전체 공지 조회 |
| `GET` | `/notices/{id}` | 공지 상세 조회 |
| `POST` | `/notices/test` | 키워드 알림 테스트용 공지 생성 |
| `GET` | `/keywords` | 키워드 목록 조회 |
| `POST` | `/keywords` | 키워드 등록 |
| `DELETE` | `/keywords/{id}` | 키워드 삭제 |
| `POST` | `/users/signup` | 회원가입 |
| `POST` | `/users/login` | 로그인 |
| `POST` | `/notifications/token` | FCM 웹 푸시 토큰 등록 |

자세한 백엔드 API와 설정은 `BE/README.md`를 참고합니다.

## 프론트엔드 라우트

| Path | 화면 |
|---|---|
| `/login` | 로그인 |
| `/signup` | 회원가입 |
| `/notices` | 공지 목록 |
| `/notices/:noticeId` | 공지 상세 |
| `/keywords` | 키워드 화면 |
| `/keywords/manage` | 키워드 관리 |

## 환경 변수

### 백엔드

| 이름 | 설명 |
|---|---|
| `MYSQL_HOST` | MySQL 호스트 |
| `MYSQL_PORT` | MySQL 포트 |
| `MYSQL_DATABASE` | 사용할 데이터베이스 이름 |
| `MYSQL_USER` | DB 사용자 |
| `MYSQL_PASSWORD` | DB 비밀번호 |
| `firebase.enabled` | Firebase Admin SDK 초기화 여부 |

### 프론트엔드

| 이름 | 설명 |
|---|---|
| `VITE_API_BASE_URL` | API base URL. 로컬 Vite proxy 사용 시 빈 값 |
| `VITE_FIREBASE_API_KEY` | Firebase Web SDK API key |
| `VITE_FIREBASE_AUTH_DOMAIN` | Firebase auth domain |
| `VITE_FIREBASE_PROJECT_ID` | Firebase project ID |
| `VITE_FIREBASE_MESSAGING_SENDER_ID` | Firebase messaging sender ID |
| `VITE_FIREBASE_APP_ID` | Firebase app ID |
| `VITE_FIREBASE_VAPID_KEY` | Web Push VAPID key |

## DB 스크립트

루트의 `schema.sql`과 `data.sql`은 DB 설계와 Category 초기 데이터 참고용입니다. 현재 백엔드 애플리케이션은 `spring.jpa.hibernate.ddl-auto=update` 설정으로 JPA 엔티티 기반 테이블을 갱신하며, 루트 SQL 파일을 자동 실행하지 않습니다.

현재 JPA 모델의 주요 테이블은 `notice`, `users`, `keyword`입니다.

## 현재 제한사항

- 회원가입과 로그인은 개발용 단순 구현입니다. 비밀번호 해싱, JWT/세션, 권한 검사는 아직 없습니다.
- 공지 크롤링은 학교 홈페이지 HTML 구조와 네트워크 상태에 영향을 받습니다.
- 키워드 알림은 사용자별 키워드가 아니라 전체 키워드 목록을 기준으로 동작합니다.
- Firebase 서비스 계정 파일은 저장소에 포함되어 있지 않습니다.
- 프론트엔드 Firebase Web 설정은 `FE/.env` 값을 사용합니다. 운영 배포 시 환경별 값을 분리해야 합니다.

## 문서

- 백엔드 상세 문서: `BE/README.md`
- 프론트엔드 상세 문서: `FE/README.md`
