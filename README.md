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

### GitHub에 코드가 보이지 않을 때

이 저장소는 기본적으로 로컬 브랜치(`main`)만 존재합니다. 아직 원격 저장소가
연결되지 않았다면 GitHub에는 커밋이 올라가지 않습니다. 아래 절차를 따라
원격 저장소를 추가하고 최신 커밋을 푸시하세요.

1. GitHub에서 비어 있는 저장소를 생성합니다. 예: `https://github.com/<org>/SWEliteProject.git`
2. 원격을 등록합니다.

   ```bash
   git remote add origin https://github.com/<org>/SWEliteProject.git
   ```

3. 현재 브랜치를 GitHub로 푸시합니다.

   ```bash
   git push -u origin main
   ```

4. GitHub에서 코드가 올라갔는지 확인합니다. 다른 브랜치 이름을 사용했다면
   위 명령의 `main` 부분을 해당 브랜치명으로 변경하세요.

### 주요 엔드포인트

| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| `POST` | `/payments/authorize` | 결제 승인 요청 (멱등성 키로 중복 방지) |
| `POST` | `/payments/capture/{paymentId}` | 승인 건 매입 처리 |
| `POST` | `/payments/refund/{paymentId}` | 매입 완료 건 환불 |

각 API의 요청/응답 스키마는 향후 OpenAPI 문서로 제공될 예정입니다. 현재는 `src/test/java/com/hecto/payments/paymentservice/PaymentControllerTest.java`를 참고하세요.
