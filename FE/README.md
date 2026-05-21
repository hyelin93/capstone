# 소프트웨어 디자인패턴 - 교내 공지 푸시 알림 프로젝트

삼육대학교의 공지 조회, 키워드 관리, 푸시 알림 기능을 제공하는 프론트엔드 프로젝트입니다.

백엔드는 별도 팀원이 개발하므로, 프론트엔드는 기능 단위로 분리하고 mock 데이터와 adapter 계층을 활용해 API 명세 변경에 대응하기 쉽게 구성합니다.

## 기술 스택

- `React`: 사용자 화면을 컴포넌트 기반으로 구현합니다.
- `TypeScript`: 공지, 키워드, 알림 데이터 타입을 명확하게 관리합니다.
- `Vite`: 빠른 개발 서버와 프론트엔드 빌드 환경을 제공합니다.
- `React Router`: 공지 목록, 공지 상세, 키워드 관리 화면의 라우팅을 담당합니다.
- `TanStack Query`: 서버 데이터 조회, 캐싱, 재요청, 로딩 상태 관리를 담당합니다.
- `Axios`: 백엔드 API 요청을 위한 HTTP 클라이언트입니다.
- `React Hook Form`: 키워드 등록 등 폼 입력과 검증을 효율적으로 처리합니다.
- `Zustand`: 전역 상태가 필요한 경우 가볍게 관리하기 위한 상태 관리 도구입니다.
- `React Icons`: 버튼, 탭, 상태 표시 등에 필요한 아이콘을 사용합니다.
- `ESLint`: 코드 품질과 기본 규칙을 검사합니다.

## 설치한 주요 종속성

### dependencies

- `react`, `react-dom`
  - React 앱 실행에 필요한 핵심 라이브러리입니다.

- `react-router-dom`
  - `/`, `/notices/:noticeId`, `/keywords`, `/keywords/manage` 같은 화면 이동을 처리합니다.

- `@tanstack/react-query`
  - 공지 목록, 공지 상세, 키워드 목록처럼 서버에서 받아오는 데이터를 캐싱하고 재요청합니다.
  - 백엔드 API가 붙기 전에는 mock API와 함께 사용하고, 이후 실제 API로 교체할 수 있습니다.

- `axios`
  - 백엔드 API 요청을 공통 인스턴스로 관리합니다.
  - `VITE_API_BASE_URL` 환경 변수를 통해 서버 주소를 분리합니다.

- `react-hook-form`
  - 키워드 등록 폼처럼 입력값을 다루는 화면에서 사용합니다.
  - 입력 상태, submit, validation 처리를 간단하게 관리할 수 있습니다.

- `zustand`
  - 로그인 사용자, 앱 설정, 전역 UI 상태처럼 여러 화면에서 공유되는 상태가 필요할 때 사용합니다.
  - 현재 단계에서는 필수 사용보다 확장 대비 목적이 큽니다.

- `react-icons`
  - 공통 버튼, 뒤로가기, 삭제, 알림 권한 버튼 등에 아이콘을 적용할 때 사용합니다.

### devDependencies

- `vite`, `@vitejs/plugin-react`
  - React 개발 서버와 프로덕션 빌드를 담당합니다.

- `typescript`
  - 타입 기반 개발을 지원합니다.

- `eslint`, `typescript-eslint`, `eslint-plugin-react-hooks`, `eslint-plugin-react-refresh`
  - TypeScript와 React 코드의 기본 lint 규칙을 검사합니다.

- `@types/react`, `@types/react-dom`, `@types/node`
  - React, React DOM, Node 관련 TypeScript 타입을 제공합니다.

- `@ljharb/tsconfig`
  - 일부 설치 패키지의 tsconfig 참조 오류를 해결하기 위해 추가했습니다.

## 폴더 구조

```text
FE/
├─ public/
│  └─ firebase-messaging-sw.js
│
├─ src/
│  ├─ app/
│  │  ├─ App.tsx
│  │  ├─ router.tsx
│  │  ├─ providers.tsx
│  │  └─ constants.ts
│  │
│  ├─ pages/
│  │  ├─ NoticeMainPage.tsx
│  │  ├─ NoticeDetailPage.tsx
│  │  ├─ KeywordPage.tsx
│  │  ├─ KeywordManagePage.tsx
│  │  └─ NotFoundPage.tsx
│  │
│  ├─ features/
│  │  ├─ notices/
│  │  ├─ keywords/
│  │  └─ notifications/
│  │
│  ├─ shared/
│  │  ├─ api/
│  │  ├─ components/
│  │  ├─ hooks/
│  │  ├─ styles/
│  │  ├─ utils/
│  │  └─ types/
│  │
│  ├─ assets/
│  │  ├─ images/
│  │  └─ icons/
│  │
│  ├─ main.tsx
│  └─ vite-env.d.ts
│
├─ .env
├─ package.json
├─ tsconfig.json
└─ vite.config.ts
```

## 구조 개괄

### `src/app`

앱 전체 설정을 담당합니다.

- `App.tsx`: 앱의 최상위 컴포넌트입니다.
- `router.tsx`: React Router 라우팅 설정을 관리합니다.
- `providers.tsx`: QueryClient, Router 등 전역 Provider를 모읍니다.
- `constants.ts`: 앱 전역 상수를 관리합니다.

### `src/pages`

라우트에 직접 연결되는 화면 단위 컴포넌트를 둡니다.

페이지는 기능 구현을 직접 많이 갖기보다, `features`의 컴포넌트와 hook을 조합하는 역할을 합니다.

### `src/features`

기능 기준으로 코드를 분리합니다.

- `notices`: 공지 목록, 공지 상세, 카테고리 필터, 무한 스크롤 관련 기능
- `keywords`: 키워드 목록, 등록, 삭제, 관리 기능
- `notifications`: 푸시 권한 요청, FCM 토큰 등록, 알림 처리 기능

각 feature 내부는 필요에 따라 다음처럼 나눕니다.

```text
components/  화면 조각 컴포넌트
hooks/       데이터 조회, mutation, UI 로직 hook
api/         API 요청 함수
types/       feature 전용 타입
mocks/       백엔드 연동 전 임시 데이터
adapters/    백엔드 응답을 프론트 모델로 변환
constants/   feature 전용 상수
utils/       feature 전용 유틸 함수
```

### `src/shared`

여러 기능에서 재사용하는 공통 코드를 둡니다.

- `api`: axios 인스턴스, 공통 에러 처리
- `components`: Layout, Header, Button, Modal, Loading 같은 공통 UI
- `hooks`: debounce, intersection observer 같은 공통 hook
- `styles`: reset, global, theme 스타일
- `utils`: 날짜 포맷, 텍스트 처리 등 공통 유틸
- `types`: 공통 타입

### `src/assets`

이미지와 아이콘 같은 정적 리소스를 관리합니다.

## API 연동 방식

백엔드가 별도로 개발되므로 초기에는 `mocks` 데이터를 사용해 화면을 먼저 구현합니다.

백엔드 API가 완성되면 `features/*/api`와 `features/*/adapters`만 실제 응답 형식에 맞춰 수정합니다.

예를 들어 공지 기능은 화면 컴포넌트가 백엔드 응답을 직접 알지 않고 `Notice` 타입만 사용하도록 구성합니다.

```text
백엔드 응답
→ adapter
→ 프론트 타입
→ hook
→ component
```

이 방식은 백엔드 응답 필드명이 바뀌어도 UI 컴포넌트 수정 범위를 줄여줍니다.

## 실행 방법

```bash
npm install
npm run dev
```

빌드:

```bash
npm run build
```

lint:

```bash
npm run lint
```
