# 🧠 DBWikiAgent

## 📌 프로젝트 개요

**DBWikiAgent**는 서버 자산(CMDB) 정보를 기반으로  
MediaWiki 페이지를 자동 생성 및 갱신해주는 Spring Boot 기반 자동화 도구입니다.

- Wiki 페이지는 `hostname`을 기준으로 자동으로 생성되며,
  서버 자산 정보와 변경 이력 두 가지를 모두 포함합니다.

---

## 🔄 처리 흐름 요약

```text
[1] 프로그램 시작 또는 자정 스케줄러 작동
     ↓
[2] 엑셀 파일(server_linux.xlsx) 읽어서 DB(cmdb_server_assets) 업데이트
     ↓
[3] DB에서 자산 정보 + 변경 이력 조회
     ↓
[4] Wiki 콘텐츠 렌더링 (표 + 줄글)
     ↓
[5] MediaWiki 페이지 업데이트 (자동 생성 or 수정)

```

# 🏗️ DBWikiAgent 디렉터리 구조

```text
📁 src
└── 📁 main
    ├── 📁 java
    │   └── 📁 com.ito.collector
    │       ├── 📁 adapter
    │       │   └── 📄 MediaWikiAdapter.java
    │       │       🔹 MediaWiki API와 직접 통신 (로그인, CSRF 토큰, 페이지 조회/수정 등)
    │
    │       ├── 📁 entity
    │       │   ├── 📄 CmdbAsset.java
    │       │   │   🔹 CMDB 자산 테이블(`cmdb_server_assets`)과 매핑되는 모델
    │       │   └── 📄 ChangeHistory.java
    │       │       🔹 변경이력 테이블(`change_history`)과 매핑되는 모델
    │
    │       ├── 📁 repository
    │       │   ├── 📄 CmdbAssetRepository.java
    │       │   │   🔹 CMDB 자산용 JPA Repository
    │       │   └── 📄 ChangeHistoryRepository.java
    │       │       🔹 변경이력 조회용 JPA Repository
    │
    │       ├── 📁 service
    │       │   ├── 📄 ExcelAssetUpdaterService.java
    │       │   │   🔹 엑셀 파일 데이터를 읽어 DB(cmdb_server_assets)로 업데이트
    │       │   ├── 📄 CmdbAssetService.java
    │       │   │   🔹 자산 데이터 조회용 단순 서비스
    │       │   ├── 📄 ChangeHistoryService.java
    │       │   │   🔹 호스트별 변경이력 조회 및 줄글 Wiki 포맷 생성
    │       │   ├── 📄 WikiRenderService.java
    │       │   │   🔹 CMDB 자산정보 + 변경이력 → Wiki용 전체 콘텐츠 생성
    │       │   ├── 📄 WikiUploadService.java
    │       │   │   🔹 렌더링된 콘텐츠를 MediaWiki 페이지에 업로드
    │       │   └── 📄 CmdbAssetUploadScheduler.java
    │       │       🔹 매일 자정, 전체 자산의 위키페이지를 자동 갱신 (스케줄러지만 service에 포함)
    │
    │       └── 📄 CollectorApplication.java
    │           🔹 Spring Boot 메인 실행 클래스
    │           🔹 CommandLineRunner로 앱 시작 시 엑셀 → DB → 위키 렌더링까지 자동 수행
    │
    └── 📁 resources
        ├── 📄 application.properties
        │   🔹 DB 접속 정보, 위키 API URL, 로그인 계정 설정 등
        ├── 📄 logback-spring.xml
        │   🔹 로그 설정 파일 (ex. /log/collector/ 경로에 로그 저장)
        └── 📄 server_linux.xlsx
            🔹 엑셀 기반 CMDB 자산 정보 파일 (필요 시 업데이트용)