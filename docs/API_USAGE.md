# depl API 사용법

`depl`은 Spring Boot를 사용하지 않는 Spring Framework 4.3.x 기반 Spring MVC 프로젝트에서 actuator 유사 health/metrics API를 제공하는 Java 라이브러리입니다.

이 문서는 업무 애플리케이션에 `depl`을 추가하고 실제 endpoint를 호출하는 방법을 설명합니다.

## 1. 적용 대상

- Java 8 기반 애플리케이션
- Spring Framework 4.3.x 기반 Spring MVC 프로젝트
- JEUS 등 외부 WAS에 WAR로 배포되는 프로젝트
- Spring Boot를 사용하지 않는 프로젝트
- Spring Boot Actuator를 직접 사용할 수 없는 프로젝트

## 2. Maven dependency 추가

업무 프로젝트의 `pom.xml`에 아래 dependency를 추가합니다.

```xml
<dependency>
    <groupId>com.e9pay.common</groupId>
    <artifactId>depl</artifactId>
    <version>1.0.0</version>
</dependency>
```

`depl` 내부의 Spring 관련 dependency는 `provided` scope입니다. 따라서 업무 프로젝트가 사용하는 Spring Framework, Spring MVC, Spring JDBC, Servlet API 버전을 그대로 사용합니다.

## 3. Spring component-scan 설정

`depl`은 Spring Boot AutoConfiguration을 사용하지 않습니다. 업무 프로젝트에서 component-scan을 직접 추가해야 합니다.

### XML 설정 방식

Spring MVC XML 설정 파일에 아래 내용을 추가합니다.

```xml
<context:component-scan base-package="com.e9pay.common.depl" />
```

`context` namespace가 없다면 XML 상단에 namespace를 추가해야 합니다.

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="
           http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
           http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd">

    <context:component-scan base-package="com.e9pay.common.depl" />

</beans>
```

### Java Config 설정 방식

```java
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.e9pay.common.depl")
public class DeplMonitorImportConfig {
}
```

또는 라이브러리의 설정 클래스를 import할 수 있습니다.

```java
import com.e9pay.common.depl.config.DeplMonitorConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(DeplMonitorConfig.class)
public class DeplMonitorImportConfig {
}
```

## 4. JSON 응답 설정 확인

컨트롤러는 Spring 4.3 호환성을 위해 `@Controller`와 `@ResponseBody`를 사용합니다. 응답 객체는 `Map<String, Object>`입니다.

업무 프로젝트에 JSON 메시지 컨버터가 설정되어 있어야 정상적으로 JSON 응답이 내려갑니다. 일반적으로 Spring MVC에서 Jackson을 사용합니다.

예시 dependency:

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.8.11.6</version>
</dependency>
```

Spring MVC XML에서 `<mvc:annotation-driven />` 또는 동등한 설정이 필요할 수 있습니다.

```xml
<mvc:annotation-driven />
```

이미 업무 프로젝트에서 JSON API를 운영 중이라면 대부분 추가 설정 없이 동작합니다.

## 5. URL 규칙

모든 API는 아래 base path 하위에 있습니다.

```text
/v1/api/actuator
```

외부 WAS에 배포된 WAR의 context path가 있다면 실제 호출 URL은 context path를 포함합니다.

예시:

```text
http://localhost:8080/myapp/v1/api/actuator/health
```

context path가 root라면 아래처럼 호출합니다.

```text
http://localhost:8080/v1/api/actuator/health
```

## 6. Endpoint 목록

| Method | Endpoint | 설명 |
| --- | --- | --- |
| GET | `/v1/api/actuator/health` | 애플리케이션 기본 health |
| GET | `/v1/api/actuator/health/db` | DB 연결 health |
| GET | `/v1/api/actuator/metrics` | thread, memory, system 전체 metrics |
| GET | `/v1/api/actuator/metrics/thread` | JVM thread metrics |
| GET | `/v1/api/actuator/metrics/memory` | JVM memory metrics |
| GET | `/v1/api/actuator/metrics/system` | Java/OS 시스템 정보 |

## 7. API 호출 예시

아래 예시는 context path가 root인 경우입니다. 업무 프로젝트의 context path가 있다면 URL 앞에 context path를 붙입니다.

### 7.1 기본 health

```bash
curl -s http://localhost:8080/v1/api/actuator/health
```

응답 예시:

```json
{
  "status": "UP",
  "timestamp": 1718179200000,
  "system": {
    "javaVersion": "1.8.0_402",
    "osName": "Linux",
    "availableProcessors": 4
  }
}
```

필드 설명:

| 필드 | 설명 |
| --- | --- |
| `status` | 기본 health 상태. 정상 응답이면 `UP` |
| `timestamp` | 응답 생성 시각. epoch milliseconds |
| `system.javaVersion` | JVM Java 버전 |
| `system.osName` | OS 이름 |
| `system.availableProcessors` | JVM에서 사용 가능한 processor 수 |

### 7.2 DB health

```bash
curl -s http://localhost:8080/v1/api/actuator/health/db
```

DataSource가 없는 경우:

```json
{
  "timestamp": 1718179200000,
  "status": "UNKNOWN",
  "db": "NO_DATASOURCE"
}
```

DB가 정상인 경우:

```json
{
  "timestamp": 1718179200000,
  "status": "UP",
  "db": "UP"
}
```

DB 확인 중 예외가 발생한 경우:

```json
{
  "timestamp": 1718179200000,
  "status": "DOWN",
  "db": "DOWN",
  "message": "Connection is not available"
}
```

DB health 동작 방식:

| 조건 | status | db | 설명 |
| --- | --- | --- | --- |
| Spring Bean으로 등록된 `DataSource`가 없음 | `UNKNOWN` | `NO_DATASOURCE` | DB 상태를 판단하지 않음 |
| `DataSource`가 있고 `SELECT 1` 성공 | `UP` | `UP` | DB 연결 정상 |
| `SELECT 1` 실행 중 예외 발생 | `DOWN` | `DOWN` | `message`에 예외 메시지 포함 |

### 7.3 전체 metrics

```bash
curl -s http://localhost:8080/v1/api/actuator/metrics
```

응답 예시:

```json
{
  "status": "UP",
  "thread": {
    "current": 42,
    "peak": 50,
    "daemon": 20,
    "totalStarted": 120,
    "state": {
      "NEW": 0,
      "RUNNABLE": 10,
      "BLOCKED": 0,
      "WAITING": 25,
      "TIMED_WAITING": 7,
      "TERMINATED": 0
    },
    "deadlock": false,
    "deadlockedThreadCount": 0
  },
  "memory": {
    "heap": {
      "init": 268435456,
      "used": 134217728,
      "committed": 268435456,
      "max": 1073741824
    },
    "nonHeap": {
      "init": 2555904,
      "used": 50331648,
      "committed": 52428800,
      "max": -1
    },
    "runtime": {
      "max": 1073741824,
      "total": 268435456,
      "free": 134217728,
      "used": 134217728
    }
  },
  "system": {
    "javaVersion": "1.8.0_402",
    "javaVendor": "Oracle Corporation",
    "osName": "Linux",
    "osVersion": "5.15.0",
    "osArch": "amd64",
    "availableProcessors": 4,
    "userTimezone": "Asia/Seoul",
    "fileEncoding": "UTF-8",
    "timestamp": 1718179200000
  },
  "timestamp": 1718179200000
}
```

### 7.4 Thread metrics

```bash
curl -s http://localhost:8080/v1/api/actuator/metrics/thread
```

응답 예시:

```json
{
  "current": 42,
  "peak": 50,
  "daemon": 20,
  "totalStarted": 120,
  "state": {
    "NEW": 0,
    "RUNNABLE": 10,
    "BLOCKED": 0,
    "WAITING": 25,
    "TIMED_WAITING": 7,
    "TERMINATED": 0
  },
  "deadlock": false,
  "deadlockedThreadCount": 0
}
```

필드 설명:

| 필드 | 설명 |
| --- | --- |
| `current` | 현재 살아있는 JVM thread 수 |
| `peak` | JVM 시작 이후 최고 thread 수 |
| `daemon` | 현재 daemon thread 수 |
| `totalStarted` | JVM 시작 이후 생성된 전체 thread 누적 수 |
| `state` | `Thread.State`별 thread 수 |
| `deadlock` | JVM thread deadlock 감지 여부 |
| `deadlockedThreadCount` | deadlock 상태 thread 수 |

### 7.5 Memory metrics

```bash
curl -s http://localhost:8080/v1/api/actuator/metrics/memory
```

응답 예시:

```json
{
  "heap": {
    "init": 268435456,
    "used": 134217728,
    "committed": 268435456,
    "max": 1073741824
  },
  "nonHeap": {
    "init": 2555904,
    "used": 50331648,
    "committed": 52428800,
    "max": -1
  },
  "runtime": {
    "max": 1073741824,
    "total": 268435456,
    "free": 134217728,
    "used": 134217728
  }
}
```

필드 설명:

| 필드 | 설명 |
| --- | --- |
| `heap.init` | JVM heap 초기 메모리 |
| `heap.used` | JVM heap 사용량 |
| `heap.committed` | JVM heap committed 크기 |
| `heap.max` | JVM heap 최대 크기 |
| `nonHeap.*` | JVM non-heap 영역 정보 |
| `runtime.max` | Runtime 기준 최대 메모리 |
| `runtime.total` | Runtime 기준 현재 할당 메모리 |
| `runtime.free` | Runtime 기준 free 메모리 |
| `runtime.used` | `total - free` |

단위는 byte입니다. JVM이 최대값을 알 수 없는 경우 `max`가 `-1`일 수 있습니다.

### 7.6 System metrics

```bash
curl -s http://localhost:8080/v1/api/actuator/metrics/system
```

응답 예시:

```json
{
  "javaVersion": "1.8.0_402",
  "javaVendor": "Oracle Corporation",
  "osName": "Linux",
  "osVersion": "5.15.0",
  "osArch": "amd64",
  "availableProcessors": 4,
  "userTimezone": "Asia/Seoul",
  "fileEncoding": "UTF-8",
  "timestamp": 1718179200000
}
```

## 8. 운영 보안 권장사항

이 API는 JVM thread, memory, OS, DB 연결 상태를 노출합니다. 운영 환경에서는 외부 공개를 금지해야 합니다.

권장 방식:

- WebtoB, JEUS, L4, 방화벽에서 `/v1/api/actuator/**` 외부 접근 차단
- 사내망 또는 운영 모니터링 서버 IP만 허용
- Spring MVC Interceptor 또는 Servlet Filter로 접근 IP 제한
- 필요 시 별도 인증 헤더나 관리자 세션 검증 적용
- 외부 고객망 또는 인터넷에서 직접 호출되지 않도록 라우팅 제외

## 9. 장애 확인 포인트

### 404 Not Found

확인 항목:

- 업무 프로젝트에 `com.e9pay.common.depl` component-scan이 추가되어 있는지 확인
- Spring MVC DispatcherServlet이 `/v1/api/actuator/**` 요청을 처리하는지 확인
- WAR context path를 포함해서 호출했는지 확인

### 406 Not Acceptable 또는 JSON 변환 오류

확인 항목:

- 업무 프로젝트에 Jackson dependency가 있는지 확인
- Spring MVC에 `<mvc:annotation-driven />` 또는 동등한 메시지 컨버터 설정이 있는지 확인
- 요청 헤더의 `Accept`가 `application/json`을 허용하는지 확인

### `/health/db`가 `UNKNOWN`, `NO_DATASOURCE` 반환

확인 항목:

- Spring ApplicationContext에 `javax.sql.DataSource` Bean이 등록되어 있는지 확인
- `DataSource` Bean이 DispatcherServlet context 또는 parent context에서 주입 가능한지 확인

### `/health/db`가 `DOWN` 반환

확인 항목:

- DB 접속 정보, 계정, 네트워크, connection pool 상태 확인
- 사용하는 DB에서 `SELECT 1` 문법을 지원하는지 확인
- JEUS datasource/JNDI 설정이 정상인지 확인

## 10. 운영 적용 체크리스트

- `depl` dependency 추가
- `com.e9pay.common.depl` component-scan 추가
- JSON 메시지 컨버터 동작 확인
- context path 포함한 endpoint 호출 확인
- `/health/db`에서 DataSource 주입 확인
- 운영망 접근 제한 정책 적용
- 모니터링 시스템에서 endpoint 호출 주기 설정
