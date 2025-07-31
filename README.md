
# 🏗️ Spring Boot 수집기 프로젝트 기본 디렉토리 구조

```text
📁 src
└── 📁 main
  ├── 📁 java
  │ └── 📁 com.ito.collector
  │  ├── 📁 controller  ✅ 클라이언트 요청 처리 (REST API)
  │  │  └── 📄 CmdbAssetController.java 
  │  ├── 📁 service   ✅ 비즈니스 로직 처리
  │  │  └── 📄 CmdbAssetService.java 
  │  ├── 📁 repository ✅ DB와의 인터페이스 (JPA)
  │  │  └── 📄 CmdbAssetRepository.java 
  │  ├── 📁 entity   ✅ DB 테이블 매핑 클래스 (@Entity)
  │  │  └── 📄 CmdbAsset.java  
  │  ├── 📁 config   ✅ 설정 관련 클래스
  │  │  ❌ 아직 클래스 없음 (필요시 추가)
  │  └── 📄 CollectorApplication.java ✅ 메인 실행 클래스 (@SpringBootApplication)
  └── 📁 resources
    └── 📄 application.properties ✅ DB 접속정보, 포트 등 환경 설정
