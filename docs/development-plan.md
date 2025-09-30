# 헥토 Spring Cloud 기반 MSA 개발 계획

## 1. 프로젝트 목표 및 타당성 조사
본 프로젝트는 단순 기능 검증을 넘어 핀테크 서비스 수준의 대규모 트랜잭션 처리와 데이터 정합성 보장을 학습·실습하기 위한 교육용 프로젝트다. 헥토의 신규 채용 과정과 연계하여, 실제 현업 투입이 가능한 백엔드 개발자로 성장하는 것을 목표로 한다. 성능 목표는 Gatling 기반 부하 테스트로 검증하며, 100TPS 환경에서 p95 응답 시간이 500ms 이하임을 반복적으로 입증한다. KT Cloud 환경에 마이크로서비스 아키텍처(MSA) 기반의 모의 결제 백엔드 시스템을 배포하고 운영한다.

### 1.1 타당성 조사
- **기술**: Spring Cloud 기반 MSA, Kafka, Redis, Outbox 패턴은 산업 현장에서 검증된 스택이며 KT Cloud IaaS에서 Docker 배포 가능성이 높다.
- **성능**: 캐싱과 비동기 처리로 100TPS, p95 500ms 목표가 합리적이다. I/O 지연이 병목이므로 DB 인덱스 전략, 연결 풀, 배치 크기 튜닝이 핵심이다.
- **운영**: Prometheus + Grafana + OpenTelemetry로 관측성을 확보하고 DLQ 재처리로 운영 리스크를 완화한다. Jenkins로 롤백 시간을 단축한다.
- **보안/규제**: 실제 금융 정보를 저장하지 않으며 HMAC, 마스킹, 비밀 관리로 기본 준수 범위를 충족한다.
- **비용**: 관리형 MariaDB/Redis 활용 시 초기 구축/운영 비용을 절감할 수 있으며 Auto Scale은 단계적 비용 집행이 가능하다.
- **리스크 및 대응**: Kafka 장애 시 Outbox+재시도+DLQ로 유실 0% 설계, 성능 미달 시 캐시 확대/파티션 증설/비동기 전환 확대, 일정 지연 시 정산·대사 기능 제외로 범위 축소가 가능하다.
- **ROI**: p95 2.5초 → 500ms 개선 가정 시 고객 대기 시간이 대폭 단축되고 동일 인프라 대비 처리량 증가로 VM 수 감소가 가능하다.

## 2. 개발 범위
### 포함 기능
- **핵심 API**: 결제 승인, 매입, 환불을 우선 구현하고 정산/대사 기능은 확장 설계로 제시한다.
- **배치 작업**: 특정 기간 거래를 집계하여 정산 데이터를 생성한다.
- **시스템 안정성/효율**:
  - Redis를 활용한 캐싱 및 트래픽 제어
  - Kafka를 이용한 비동기 메시지 처리
  - Jenkins CI/CD 파이프라인 구축
  - Prometheus + Grafana 기반 모니터링
- **기본 보안**: HMAC, 개인정보 마스킹, 비밀 키 분리 적용

### 제외 기능
- 실제 PG 및 은행 시스템 연동, 카드 정보 저장, PCI-DSS 문서 작업, 정산·대사 기능의 실제 구현(설계만 제시)

## 3. 요구사항
### 기능적 요구사항
- **결제 승인**: 파라미터 검증, 멱등성 처리(409), `REQUESTED` 상태 저장
- **결제 매입**: `REQUESTED` 건만 매입 가능, 상태 `COMPLETED`, 이중부기 원장 기록
- **결제 환불**: `COMPLETED` 건만 환불, 환불 트랜잭션 생성, 상태 `REFUNDED`, 역원장 기록
- **정산 배치**: `COMPLETED` 거래 집계 및 `settlement_batch` 기록 생성
- **대사 기능(확장)**: CSV 업로드, 데이터 매칭, 불일치 처리, DLQ 전송, 재처리 API

### 비기능적 요구사항
- **성능**: p95 ≤ 500ms, 100TPS 처리
- **안정성/가용성**: 서비스 장애 격리, Outbox 패턴으로 데이터 유실 0%
- **데이터 정합성**: 이중부기, 멱등성 보장
- **확장성**: Stateless 서비스로 수평 확장
- **보안**: HMAC 검증, 민감 정보 마스킹, 비밀 정보 분리
- **관측성**: 표준화된 로깅, Prometheus Metrics, Grafana 대시보드, (확장) OpenTelemetry 트레이싱

### 요구사항 추적 매트릭스
| ID | 설명 | 수용 기준 | 검증 방법 |
| --- | --- | --- | --- |
| FR-001 | 결제 승인 API 제공 | 200 OK, DB에 `REQUESTED` 상태 저장 | 단위/통합 테스트 |
| FR-002 | 정산 배치 기능 | 5,000건 1분 내 처리 | 성능 테스트 |
| NFR-001 | 응답 지연 | p95 ≤ 500ms | 부하 테스트 |
| NFR-002 | 데이터 유실 방지 | 장애 시 데이터 유실 0% | 장애 시뮬레이션 |

## 4. 시스템 아키텍처
### 서비스 구성
- API Gateway → Payment → Settlement → Reconciliation → Notify(확장)
- 공통 인프라: Service Registry(Eureka/Consul), Config Server, MariaDB, Redis, Kafka(+DLQ), Prometheus, Grafana

### 데이터 흐름
클라이언트 → API Gateway → Payment(핵심 처리) → DB → Outbox → Kafka → 후속 서비스. Outbox 패턴으로 이벤트 발행 안정성을 확보한다.

### 배포 아키텍처
KT Cloud VM 1~2대에 Docker로 배포(서비스, Kafka, Redis, MariaDB, Jenkins). Managed DB/Redis 사용으로 운영 부담을 줄인다.

## 5. 테이블 및 API 명세
### 핵심 테이블
`payment`, `ledger_entry`, `outbox_event`, `idem_response_cache`, `settlement_batch`, `settlement_item`, `recon_file`, `recon_item` (확장). SQL 스키마는 문서 본문 참고.

### 주요 토픽/캐시 키
- Redis: rate limiting, 멱등성 캐시, 분산 락
- Kafka: `payment.authorized`, `payment.captured`, `payment.refunded`, (확장) `recon.unmatched`, `settlement.closed`

### API 예시
- `POST /payments/authorize`
- `POST /payments/capture/{paymentId}`
- `POST /payments/refund/{paymentId}`
- `POST /payments/upload` (확장)

요청/응답 예시는 문서 본문 참고.

## 6. 기술 스택
- **언어/프레임워크**: Java, Spring Boot, Spring Cloud
- **DB**: MariaDB (JPA/JDBC, 필요 시 Flyway)
- **캐시**: Redis
- **메시징**: Kafka
- **인프라/자동화**: Jenkins, Docker, Testcontainers
- **테스트/모니터링**: Gatling, Prometheus, Grafana

## 7. 상세 시나리오
- 정상 결제/매입, 네트워크 오류 멱등 처리, 대사 프로세스, Kafka 장애 복구 시나리오를 정의한다.

## 8. 개발 일정 및 관리 프로세스
8주 계획(주차별 목표, 산출물, 완료 조건)과 전형적인 개발 프로세스(계획→분석→설계→구현→테스트→종료)를 명시한다.

## 9. FE & BE 구성 방안
- **FE**: Thymeleaf 기반 최소 UI(결제 테스트, 대사 업로드, 관리자 대시보드, Grafana 연동)
- **BE**: MSA 원칙(Single Responsibility, Loose Coupling, DB per Service) 준수, Kafka 기반 비동기 통신

## 10. 기대 효과 및 기여 방안
핀테크 핵심 역량(대용량 트랜잭션, 정합성, 안정성) 학습 및 DevOps 역량 확보로 헥토와 같은 기업에 기여 가능.

## 11. 구현 계획 및 마일스톤
8주 일정의 상세 마일스톤을 정의(산출물, 완료 조건, 위험 및 완화 조치 포함).

## 12. 품질보증/테스트 전략
단위/통합/부하/보안/회귀 테스트 전략, 도구, 수용 기준 및 예시 테스트 케이스 매트릭스를 제공한다.

## 13. 애플리케이션 및 배포 아키텍처
MSA 구성요소와 KT Cloud 배포 전략을 시각화/설명한다.

## 14. 리스크 관리 및 대응 전략
주요 리스크, 평가(가능성×영향도), 대응 전략, 모니터링 체계를 정리한다.

