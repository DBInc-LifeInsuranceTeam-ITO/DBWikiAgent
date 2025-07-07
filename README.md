┌────────────┐
│ PostgreSQL │  ← cmdb_asset 테이블
└────┬───────┘
     │
     ▼
┌──────────────────────┐
│ CmdbAssetRepository  │ ← JPA Repository
└────┬─────────────────┘
     │
     ▼
┌──────────────────────┐
│ ItsmDbAdapter        │ ← DB로부터 데이터 fetch()
└────┬─────────────────┘
     │
     ▼
┌──────────────────────┐
│ CollectorService      │ ← 주기적으로 데이터 수집 & 가공
│  └─ CSV 읽기          │ ← csvAdapter
│  └─ 위키 업로드        │ ← wikiAdapter.uploadToWiki
└──────────────────────┘
     │
     ▼
┌──────────────────────┐
│ MediaWiki (위키 서버)│ ← 시스템 정보 페이지 생성