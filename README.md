# Distributed Job Processor

분산 환경에서 안정적이고 확장 가능한 작업 처리를 위한 프레임워크

## 시스템 개요
- Apache Elastic-Job-lite 기반의 분산 작업 처리 시스템
- ZooKeeper를 활용한 작업 조정 및 고가용성(HA) 지원
- PostgreSQL LISTEN/NOTIFY 기능을 활용한 동적 작업 관리
- 실시간 모니터링 및 Slack 알림 통합

## 아키텍처
![System Architecture](docs/images/architecture.png)

### 주요 컴포넌트
1. **Job Registration Center**
   - 작업 스펙 기반 동적 작업 등록
   - ZooKeeper 기반 작업 조정
   - 작업 생명주기 관리
   - UTC 기반 시간 처리

2. **Distributed Task**
   - 3단계 작업 처리 프로세스
     * 초기화 (initialize): 데이터 소스 준비
     * 계산 (calculate): 실제 작업 수행
     * 검증 (validate): 결과 유효성 검증
   - 작업별 독립적인 실행 환경
   - 실패 시 자동 알림

3. **Database Change Listener**
   - PostgreSQL LISTEN/NOTIFY 기반 실시간 변경 감지
   - 작업 스펙 변경 시 동적 작업 갱신
   - 장애 상황 실시간 알림

4. **모니터링 & 알림**
   - Slack 통합 실시간 알림
   - 작업 실행 이력 관리
   - 시스템 상태 모니터링

## 기술 스택
- Java 11+
- Spring Boot
- Apache Elastic-Job-lite
- Apache ZooKeeper
- PostgreSQL (LISTEN/NOTIFY 기능 활용)
- Slack API (알림)

## 패키지 구조
```
src/main/java/com/devtaco/distribute
├── config/ # 애플리케이션 설정
├── job/ # 작업 정의 및 실행
│ ├── jobImpl/ # 구체적인 작업 구현체
├── model/ # 도메인 모델
│ ├── JobSpec    # 작업 스펙 정의
│ ├── ImplSpec   # 구현 스펙
│ └── SourceData # 소스 데이터 관리
├── repository/ # 데이터 접근 계층
├── service/ # 비즈니스 로직
│ └── SpecUpdateListener # DB 변경 감지
└── zk/ # ZooKeeper 관련 구현
resources
├── application-xxx.yml
```

## 주요 기능
1. **분산 작업 처리**
   - 작업 단위 분할 및 분산 실행
   - 노드 간 작업 조정
   - 작업 실행 상태 동기화
   - UTC 기반 시간 처리

2. **확장성**
   - 동적 작업 등록/해제
   - 커스텀 작업 구현 용이
   - 노드 확장에 따른 자동 작업 재분배
   - PostgreSQL 이벤트 기반 동적 갱신

3. **안정성**
   - 작업 실행 3단계 검증
   - 장애 노드 자동 감지
   - 실시간 알림 시스템
   - 데이터베이스 변경 실시간 감지

## 설정 가이드
### 기본 설정
```yaml
elastic-job:
  zookeeper:
    server-lists: localhost:2181
    namespace: distributed-job

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/job_db
    username: root
    password: password
```

### 작업 스펙 설정
1. 데이터베이스에 작업 스펙 정의
2. `ImplSpec` 클래스를 통한 작업 구현
3. `JobRegistration`에서 자동 작업 등록
4. PostgreSQL NOTIFY 이벤트를 통한 동적 갱신

## 개발 가이드
### 새로운 작업 추가
1. `DistributeTask` 인터페이스 구현
2. 3단계 프로세스 구현:
   - `initialize()`: 작업 초기화
   - `calculate()`: 실제 작업 수행
   - `validateCalculation()`: 결과 검증

### 모델 설계
- `JobSpec`: 작업 기본 속성 정의
- `ImplSpec`: 구체적인 작업 스펙 구현
- `SourceData`: 가중치 기반 소스 데이터 관리
- `CalculateValueObj`: 계산 결과 관리

## 운영 가이드
### 모니터링
- ZooKeeper 상태 확인
- PostgreSQL LISTEN 상태 확인
- 작업 실행 이력 조회
- Slack 알림 설정

### 장애 대응
- 노드 실패 시 자동 작업 재분배
- 작업 실패 시 알림 발송
- 데이터베이스 연결 실패 시 자동 재시도
- 수동 작업 재실행 방법
