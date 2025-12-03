# Web Guide Backend Service with PGRoonga Search

## 소개

Web Guide Backend Service는 웹 가이드 콘텐츠, 공지사항 등을 효과적으로 검색하고 관리하기 위해 구축된 고성능 백엔드 시스템입니다. 이 프로젝트는 최신 Java 21과 Spring Boot 3를 기반으로 하며, PostgreSQL의 PGRoonga 확장 기능을 활용하여 정확하고 빠른 키워드 기반 검색 결과를 제공합니다.

또한, 콘텐츠 관리, 검색 통계 분석 등 다양한 부가 기능을 제공합니다.

## 주요 기능

-   **키워드 검색**: PostgreSQL의 PGRoonga를 활용한 빠르고 정확한 키워드 기반 검색
-   **콘텐츠 관리**: 가이드, 공지사항, 카테고리 등 콘텐츠에 대한 CRUD 기능
-   **검색 통계**: 인기 검색어, 전체 검색 횟수 등 간단한 통계 API 제공

## 기술 스택

-   **Backend**: Spring Boot 3.5.4, Spring Web, Spring Data JPA
-   **Language**: Java 21
-   **Database**: PostgreSQL
-   **Search**: PostgreSQL (PGRoonga)
-   **Build Tool**: Maven
-   **Others**: Lombok, p6spy, Spring Cache

## 시작하기

### 사전 준비

-   Java 21 (JDK)
-   Maven 3.6+
-   Docker 및 Docker Compose (PostgreSQL 실행용)

### 설치 및 실행

1.  **저장소 복제**

    ```bash
    git clone https://repository-url/web-guide-back-end.git
    cd web-guide-back-end
    ```

2.  **환경 변수 설정**

    프로젝트 루트 디렉터리에 `.env` 파일을 생성하고 아래 내용을 채워주세요. 이 파일은 데이터베이스 연결 정보를 관리합니다.

    ```env
    # PostgreSQL 연결 정보
    SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/webguide
    SPRING_DATASOURCE_USERNAME=your-username
    SPRING_DATASOURCE_PASSWORD=your-password

    ```

## API 엔드포인트

### 검색

-   `GET /api/search`
    -   가이드 콘텐츠를 검색합니다.
    -   **Query Params**: `query`, `portalId`, `category`, `limit`

### 콘텐츠 관리 (가이드)

-   `GET /api/contents`
    -   특정 카테고리의 현재 가이드 내용을 조회합니다.
    -   **Query Params**: `portalId`, `categoryId`
-   `POST /api/contents`
    -   새로운 버전의 가이드 콘텐츠를 저장합니다.
-   `DELETE /api/contents`
    -   가이드를 논리적으로 삭제(soft-delete)합니다.

### 콘텐츠 관리 (공지사항)

-   `GET /api/notice`
    -   공지사항 목록을 조회합니다.
-   `POST /api/notice`
    -   새로운 공지사항을 생성합니다.
-   `GET /api/notice/{id}`
    -   특정 공지사항의 상세 내용을 조회합니다.
-   `PUT /api/notice/{id}`
    -   공지사항을 수정합니다.
-   `DELETE /api/notice/{id}`
    -   공지사항을 삭제합니다.
-   `GET /api/notice/search`
    -   공지사항을 검색합니다.
-   `GET /api/notice/categories`
    -   모든 공지사항 카테고리를 조회합니다.

### 카테고리

-   `GET /api/categories`
    -   카테고리 목록 또는 트리를 조회합니다.
-   `POST /api/categories`
    -   새로운 카테고리를 생성합니다.
-   `PUT /api/categories/{id}`
    -   카테고리를 수정합니다.
-   `DELETE /api/categories/{id}`
    -   카테고리를 삭제합니다.
-   `POST /api/categories/{id}/deactivate`
    -   카테고리와 그 하위 카테고리를 비활성화합니다.

### 포털 메뉴

-   `GET /api/portal-menus`
    -   지정된 포털의 메뉴 구조를 조회합니다.

### 검색 통계

-   `GET /api/analytics/popular-queries`
    -   가장 인기 있는 검색어 목록을 조회합니다.
-   `GET /api/analytics/total-searches`
    -   전체 누적 검색 횟수를 조회합니다.

## 기여 방법

프로젝트에 기여하고 싶으시다면 다음 절차를 따라주세요.

1.  이 저장소를 Fork합니다.
2.  새로운 기능 브랜치를 생성합니다 (`git checkout -b feature/AmazingFeature`).
3.  변경 사항을 커밋합니다 (`git commit -m 'Add some AmazingFeature'`).
4.  브랜치에 푸시합니다 (`git push origin feature/AmazingFeature`).
5.  Pull Request를 생성합니다.

---