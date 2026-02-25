# Backend Base Context Documentation

> **[Global Constraint]**
> 향후 AI가 생성하는 모든 코드의 주석 및 답변은 반드시 **'한글(Korean)'**로 작성해야 합니다.

## 1. Tech Stack (기술 스택)

*   **Language:** Java 21
*   **Framework:** Spring Boot 3.5.4
*   **Database:** PostgreSQL (PGRoonga Search 지원)
*   **ORM:** Spring Data JPA
*   **Logging:** P6Spy (SQL 로깅)
*   **Dependencies:**
    *   **Web:** Spring Web, Spring WebFlux (Client)
    *   **Data:** Spring Data JPA, PostgreSQL Driver
    *   **Utility:** Lombok, Jackson (JSON), Spring Cache
    *   **Testing:** Spring Boot Starter Test

## 2. Convention (코딩 컨벤션 및 표준 규칙)

### 2.1 아키텍처 패턴 (Layered Architecture)
본 프로젝트는 전형적인 계층형 아키텍처를 따르며, 관심사의 분리를 철저히 준수합니다.

1.  **Controller Layer (`*.controller`)**
    *   HTTP 요청 및 응답 처리를 담당합니다.
    *   비즈니스 로직을 직접 구현하지 않고 Service 레이어를 호출합니다.
    *   입력 데이터 검증(Validation)을 수행합니다.
2.  **Service Layer (`*.service`, `*.service.impl`)**
    *   비즈니스 로직을 수행하며 트랜잭션(`@Transactional`)의 경계가 됩니다.
    *   인터페이스(`Service`)와 구현체(`ServiceImpl`)를 분리하여 유연성을 확보합니다.
3.  **Repository Layer (`*.repository`)**
    *   데이터베이스 접근을 담당합니다 (Spring Data JPA 사용).
4.  **DTO (Data Transfer Object) (`*.dto`)**
    *   계층 간 데이터 전송을 위해 사용됩니다.
    *   Entity 객체를 API 응답으로 직접 노출하지 않고 반드시 DTO로 변환하여 전달합니다.
5.  **Entity (`*.entity`)**
    *   데이터베이스 테이블과 매핑되는 객체입니다.
    *   가능한 한 불변성을 유지하며, 필요한 경우에만 Setter를 지양하고 의미 있는 비즈니스 메서드를 추가합니다.

### 2.2 공통 규칙
*   **Lombok 활용:** `@Getter`, `@RequiredArgsConstructor`, `@Builder` 등을 사용하여 보일러플레이트 코드를 최소화합니다.
*   **Exception Handling:** `@RestControllerAdvice`와 `GlobalExceptionHandler`를 통해 예외 처리를 중앙 집중화합니다.
*   **RESTful API:** HTTP Method(GET, POST, PUT, DELETE)를 의미에 맞게 사용하고, 리소스 중심의 URL 설계를 따릅니다.
*   **주석 및 문서화:** 모든 핵심 비즈니스 로직 및 public 메서드에는 한글로 된 설명을 포함합니다.

## 3. Project Structure Reference
```
src/main/java/com/webguide/search/
├── config/        # 설정 클래스 (WebConfig 등)
├── controller/    # API 컨트롤러
├── dto/           # 데이터 전송 객체
├── entity/        # JPA 엔티티
├── exception/     # 커스텀 예외 및 핸들러
├── repository/    # JPA 리포지토리
├── service/       # 서비스 인터페이스 및 구현체
└── util/          # 공통 유틸리티
```
