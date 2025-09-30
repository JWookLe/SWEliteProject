# SWEliteProject

Spring Cloud 기반 MSA 학습을 위한 백엔드 결제 서비스 예제입니다. 전체 아키텍처와 범위, 요구사항, 일정은 [`docs/development-plan.md`](docs/development-plan.md) 문서를 먼저 확인해 주세요.

## 개발 환경
- Java 21
- Spring Boot 3.3.5
- Gradle 8.10.2 (Wrapper 분실 시 `gradle wrapper --gradle-version 8.10.2` 실행)
- H2 인메모리 데이터베이스 (로컬 테스트)

## Gradle 초기 설정
프로젝트에는 `build.gradle`, `settings.gradle`, `gradle/wrapper/gradle-wrapper.properties`가 추가되었습니다. 최초 1회 Gradle이 설치된 환경에서 아래 명령을 실행해 Wrapper JAR(`gradle/wrapper/gradle-wrapper.jar`)와 스크립트(`gradlew`, `gradlew.bat`)를 생성해 주세요.

```bash
# macOS / Linux / WSL
gradle wrapper --gradle-version 8.10.2

# Windows PowerShell
gradle wrapper --gradle-version 8.10.2
```

Wrapper가 생성되면 이후에는 로컬에 Gradle이 설치되어 있지 않아도 `./gradlew` 또는 `gradlew.bat`를 사용해 빌드할 수 있습니다.

## 로컬 실행
```bash
./gradlew bootRun              # macOS / Linux / WSL
./gradlew.bat bootRun          # Windows
```

애플리케이션은 H2 인메모리 DB를 사용하며 기본 포트(8080)에서 실행됩니다.

## 테스트 실행
```bash
./gradlew test
./gradlew.bat test
```

## 주요 API
| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| `POST` | `/payments/authorize` | 결제 승인 요청 (멱등키로 중복 방지) |
| `POST` | `/payments/capture/{paymentId}` | 승인 건 매입 처리 |
| `POST` | `/payments/refund/{paymentId}` | 매입 완료 건 환불 |

요청/응답 JSON 스키마는 `src/test/java/com/hecto/payments/paymentservice/PaymentControllerTest.java`의 테스트 케이스를 참고하세요.

## 참고: 문서
- `docs/development-plan.md`: 전체 개발 계획, 일정, 시스템 구성 개요
