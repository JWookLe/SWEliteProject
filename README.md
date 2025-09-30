# SWEliteProject

헥토의 "Spring Cloud 모듈을 활용한 MSA 개발" 프로젝트 저장소입니다. 전체 개발 범위, 요구사항, 아키텍처, 일정, 품질 전략 등은 [`docs/development-plan.md`](docs/development-plan.md)에 정리되어 있습니다. 작업을 시작하기 전에 반드시 해당 문서를 확인하세요.

## Payment 서비스 (초기 버전)

### 로컬 실행

```bash
mvn spring-boot:run
```

### 테스트 실행

```bash
mvn test
```

### 주요 엔드포인트

| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| `POST` | `/payments/authorize` | 결제 승인 요청 (멱등성 키로 중복 방지) |
| `POST` | `/payments/capture/{paymentId}` | 승인 건 매입 처리 |
| `POST` | `/payments/refund/{paymentId}` | 매입 완료 건 환불 |

각 API의 요청/응답 스키마는 향후 OpenAPI 문서로 제공될 예정입니다. 현재는 `src/test/java/com/hecto/payments/paymentservice/PaymentControllerTest.java`를 참고하세요.
