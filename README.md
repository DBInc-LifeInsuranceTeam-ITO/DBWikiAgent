
# 🏗️ DBWikiAgent 디렉토리 구조

```text
📁 src
└── 📁 main
  ├── 📁 java
  │ └── 📁 com.ito.collector
  │  ├── 📁 adapter
  │  │  └── 📄 MediaWikiAdapter.java       ✅ MediaWiki API와 통신 (로그인, 페이지 조회/수정)
  │  ├── 📁 entity
  │  │  └── 📄 CmdbAsset.java              ✅ DB 테이블(cmdb_server_assets)과 매핑되는 자산 모델 클래스
  │  ├── 📁 repository
  │  │  └── 📄 CmdbAssetRepository.java    ✅ JpaRepository 상속 - DB CRUD 인터페이스
  │  ├── 📁 service
  │  │  ├── 📄 CmdbAssetService.java       ✅ 자산 목록 조회 비즈니스 로직
  │  │  └── 📄 CmdbAssetUploadService.java ✅ 자산정보 기반 위키페이지 자동 갱신 로직
  │  └── 📄 CollectorApplication.java       ✅ Spring Boot 메인 실행 클래스 + CommandLineRunner로 자동 실행
  └── 📁 resources
    └── 📄 application.properties           ✅ DB 설정, 위키 URL, 로그인 정보 등 환경설정 파일


