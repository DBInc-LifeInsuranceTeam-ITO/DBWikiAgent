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

---

## 2. 설치 및 준비

### 2.1 필수 패키지 설치
```bash
dnf install -y postgresql13-devel gcc make
```

### 2.2 Oracle Instant Client 11.2 설치
압축 해제 후 `/opt/oracle/instantclient_11_2` 경로에 배치  
(SDK, `oci/include`, `rdbms/public` 포함)

환경 변수 설정(`/etc/profile.d/oracle.sh`):
```bash
export ORACLE_HOME=/opt/oracle/instantclient_11_2
export LD_LIBRARY_PATH=$ORACLE_HOME:$LD_LIBRARY_PATH
export PATH=$ORACLE_HOME:$PATH
```

### 2.3 `oracle_fdw` 빌드 및 설치
```bash
cd /usr/local/src
git clone https://github.com/laurenz/oracle_fdw.git
cd oracle_fdw
make
make install
```

---

## 3. PostgreSQL 설정

### 3.1 Extension 생성
```sql
CREATE EXTENSION oracle_fdw;
```

### 3.2 Oracle 서버 연결
```sql
CREATE SERVER oradb FOREIGN DATA WRAPPER oracle_fdw
OPTIONS (
  dbserver '//10.90.40.231:1521/XE'
);
```

### 3.3 User Mapping
```sql
CREATE USER MAPPING FOR postgres
SERVER oradb
OPTIONS (
  user 'system',
  password 'oracle'
);
```

---

## 4. Oracle 테이블 생성
```sql
CREATE TABLE change_history (
  ci_nm      CLOB                  NOT NULL,
  req_title  CLOB                  NOT NULL,
  req_per_id VARCHAR2(50)          NOT NULL,
  req_desc   CLOB,
  req_person VARCHAR2(100),
  req_no     VARCHAR2(30)          NOT NULL,
  CONSTRAINT change_history_pk PRIMARY KEY (req_no)
);
```

---

## 5. PostgreSQL에서 Foreign Table 생성
```sql
CREATE FOREIGN TABLE change_history_oracle (
  ci_nm text,
  req_title text,
  req_per_id varchar(50),
  req_desc text,
  req_person varchar(100),
  req_no varchar(30)
)
SERVER oradb
OPTIONS (
  schema 'SYSTEM',
  table  'CHANGE_HISTORY'
);
```

---

## 6. 동작 확인

### 6.1 Oracle에 데이터 입력
```sql
INSERT INTO change_history (
  ci_nm, req_title, req_per_id, req_desc, req_person, req_no
) VALUES (
  'TestA', 'Test Request', 'user01', 'Test Description', 'Admin', 'REQ001'
);
```

### 6.2 PostgreSQL에서 조회
```sql
SELECT * FROM change_history_oracle;
```

---

## 7. 참고 사항
- 현재 Oracle DB 캐릭터셋이 `ZHS16GBK`이므로 한글 데이터는 깨질 수 있음.
- 영어/숫자는 정상적으로 송수신됨.
- `oracle_fdw`는 양방향 실시간 싱크가 아닌, 쿼리 시점에 원격 데이터를 조회/조작하는 구조임.

---
