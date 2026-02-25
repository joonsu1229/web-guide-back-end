# Web Guide API Specification

## 1. Analytics API
통계 및 분석 관련 데이터를 제공합니다.

### 1.1 인기 검색어 조회
- **URL:** `/api/analytics/popular-queries`
- **Method:** `GET`
- **Query Parameters:**
  - `limit` (Integer, Default: 10): 반환할 검색어 개수
- **Response:** `Map<String, Long>` (검색어: 검색 횟수)

### 1.2 전체 검색 횟수 조회
- **URL:** `/api/analytics/total-searches`
- **Method:** `GET`
- **Response:**
  ```json
  {
    "totalSearches": "Long"
  }
  ```

---

## 2. Category API
가이드 카테고리 관리 기능을 제공합니다.

### 2.1 카테고리 목록 조회
- **URL:** `/api/categories`
- **Method:** `GET`
- **Query Parameters:**
  - `portalId` (String, 필수): 포탈 식별자
  - `section` (String, 선택): 특정 섹션(예: 'portal') 필터링. 미지정 시 전체 조회.
- **Response:** `List<CategoryDto>`

### 2.2 카테고리 생성
- **URL:** `/api/categories`
- **Method:** `POST`
- **Request Body:**
  ```json
  {
    "name": "String",
    "description": "String",
    "displayOrder": "Integer",
    "section": "String",
    "parentId": "Long (선택)",
    "portalId": "String (필수)"
  }
  ```
- **Response:** `CategoryDto`

### 2.3 카테고리 수정
- **URL:** `/api/categories/{id}`
- **Method:** `PUT`
- **Path Variable:** `id` (Long): 수정할 카테고리 ID
- **Query Parameters:**
  - `portalId` (String, 필수): 포탈 식별자
- **Request Body:**
  ```json
  {
    "name": "String",
    "description": "String",
    "displayOrder": "Integer"
  }
  ```
- **Response:** `CategoryDto`

### 2.4 카테고리 비활성화
- **URL:** `/api/categories/{id}/deactivate`
- **Method:** `POST`
- **Path Variable:** `id` (Long): 비활성화할 카테고리 ID
- **Query Parameters:**
  - `portalId` (String, 필수): 포탈 식별자
- **Response:** `Void` (200 OK)

### 2.5 카테고리 삭제
- **URL:** `/api/categories/{id}`
- **Method:** `DELETE`
- **Path Variable:** `id` (Long): 삭제할 카테고리 ID
- **Query Parameters:**
  - `portalId` (String, 필수): 포탈 식별자
- **Response:** `Void` (204 No Content)

---

## 3. Guide Content API
가이드 본문 내용 관리 기능을 제공합니다.

### 3.1 현재 버전 콘텐츠 조회
- **URL:** `/api/contents`
- **Method:** `GET`
- **Query Parameters:**
  - `portalId` (String)
  - `categoryId` (Long)
- **Response:** `GuideContentDto`

### 3.2 콘텐츠 저장 (새 버전)
- **URL:** `/api/contents`
- **Method:** `POST`
- **Request Body:** `GuideContentDto`
- **Response:** `GuideContentDto`

### 3.3 콘텐츠 삭제 (Soft Delete)
- **URL:** `/api/contents`
- **Method:** `DELETE`
- **Query Parameters:**
  - `portalId` (String)
  - `categoryId` (Long)
- **Response:** `Void`

---

## 4. Notice API
공지사항 관리 및 검색 기능을 제공합니다.

### 4.1 공지사항 목록 조회
- **URL:** `/api/notice`
- **Method:** `GET`
- **Query Parameters:**
  - `category` (String, 선택)
  - `keyword` (String, 선택)
- **Response:** `List<NoticeDto>`

### 4.2 공지사항 상세 조회
- **URL:** `/api/notice/{id}`
- **Method:** `GET`
- **Path Variable:** `id` (Long)
- **Response:** `NoticeDto`

### 4.3 공지사항 검색
- **URL:** `/api/notice/search`
- **Method:** `GET`
- **Query Parameters:**
  - `keyword` (String, 필수)
  - `category` (String, 선택)
  - `portalId` (String, 선택)
- **Response:** `List<NoticeDto>`

### 4.4 공지사항 카테고리 목록 조회
- **URL:** `/api/notice/categories`
- **Method:** `GET`
- **Response:** `List<NoticeCategoryDto>`

---

## 5. Portal Menu API
포탈 메뉴 관리 기능을 제공합니다.

### 5.1 포탈 메뉴 목록 조회
- **URL:** `/api/portal-menus`
- **Method:** `GET`
- **Query Parameters:** `portalId` (String, 필수)
- **Response:** `List<PortalMenuDto>`

### 5.2 포탈 메뉴 생성
- **URL:** `/api/portal-menus`
- **Method:** `POST`
- **Request Body:** `PortalMenuDto`
- **Response:** `PortalMenuDto`

### 5.3 포탈 메뉴 수정
- **URL:** `/api/portal-menus/{id}`
- **Method:** `PUT`
- **Path Variable:** `id` (Long)
- **Request Body:** `PortalMenuDto`
- **Response:** `PortalMenuDto`

### 5.4 포탈 메뉴 삭제 (비활성화)
- **URL:** `/api/portal-menus/{id}`
- **Method:** `DELETE`
- **Path Variable:** `id` (Long)
- **Query Parameters:** `portalId` (String, 필수)
- **Response:** `Void` (204 No Content)

---

## 6. Search API
전체 가이드 검색 기능을 제공합니다.

### 6.1 통합 검색
- **URL:** `/api/search`
- **Method:** `GET`
- **Query Parameters:**
  - `query` (String, 필수): 검색어
  - `portalId` (String, Default: "P1")
  - `category` (Long, 선택)
  - `limit` (Integer, Default: 10)
- **Response:** `List<SearchResult>`

---

## 7. Data Transfer Objects (DTO) Mappings

### 7.1 CategoryDto
| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | Long | 카테고리 ID |
| `name` | String | 카테고리 이름 |
| `description` | String | 설명 |
| `depth` | Integer | 계층 깊이 |
| `displayOrder` | Integer | 출력 순서 |
| `section` | String | 섹션 구분 |
| `children` | List<CategoryDto> | 하위 카테고리 목록 |
| `parentId` | Long | 부모 카테고리 ID |
| `portalId` | String | 포탈 ID |

### 7.2 GuideContentDto
| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | Long | 콘텐츠 버전 ID |
| `contentBody` | String | HTML 본문 |
| `version` | Integer | 버전 번호 |
| `categoryId` | Long | 카테고리 ID |
| `menu` | String | 메뉴명 |
| `title` | String | 제목 |
| `description` | String | 설명 |

### 7.3 NoticeDto
| Field | Type | Description |
| :--- | :--- | :--- |
| `noticeId` | Long | 공지사항 ID |
| `category` | String | 카테고리 키 |
| `title` | String | 제목 |
| `summary` | String | 요약 |
| `content` | String | HTML 본문 |
| `views` | Integer | 조회수 |
| `useYn` | String | 사용 여부 (Y/N) |
| `isNew` | String | 신규 여부 (Y/N) |
| `regDt` | String (ISO) | 등록 일시 |
| `modDt` | String (ISO) | 수정 일시 |

### 7.4 PortalMenuDto
| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | Long | 메뉴 ID |
| `title` | String | 메뉴명 |
| `description` | String | 설명 |
| `icon` | String | 아이콘 키 |
| `className` | String | CSS 클래스명 |
| `tags` | List<String> | 태그 목록 |
| `section` | String | 섹션 구분 (Category 연결 키) |
| `portalId` | String | 포탈 ID |
| `active` | Boolean | 활성 여부 |

### 7.5 SearchResult
| Field | Type | Description |
| :--- | :--- | :--- |
| `document` | Object | 검색된 문서 상세 정보 |
| `document.id` | Long | 문서 ID |
| `document.guideId` | Long | 가이드 ID |
| `document.categoryId` | Long | 카테고리 ID |
| `document.version` | Integer | 버전 |
| `document.contentBody` | String | 검색된 본문 |
| `document.createdAt` | String (ISO) | 생성 일시 |
