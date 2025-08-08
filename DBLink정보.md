# PostgreSQL ↔ Oracle 10g 연동 (oracle_fdw)

이 문서는 PostgreSQL 13(Rocky Linux 8.7) 서버에서 Oracle 10g 서버와 `oracle_fdw`를 이용해 DBLink 형태로 데이터를 연동한 작업 내용을 기록합니다.

---

## 1. 환경 정보

### PostgreSQL 서버
- OS: Rocky Linux 8.7 (Green Obsidian)
- IP: `10.90.40.89`
- PostgreSQL 버전: 13.x

### Oracle 서버
- OS: Oracle XE 10g (Docker 컨테이너 기반)
- IP: `10.90.40.231`
- SID: `XE`
- Listener Port: `1521`
