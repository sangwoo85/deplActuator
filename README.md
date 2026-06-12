# depl

`depl`은 Spring Boot를 사용하지 않는 기존 Spring MVC 애플리케이션에서 JVM, DB, 시스템 상태를 간단히 확인하기 위한 actuator 유사 모니터링 라이브러리입니다.

Spring Boot Actuator 대체 용도로 사용할 수 있지만, 이 라이브러리는 Spring Boot 의존성이나 AutoConfiguration을 사용하지 않습니다. Java 8, Spring Framework 4.3.x, JEUS 같은 외부 WAS에 배포되는 기존 Spring MVC 환경을 기준으로 작성되었습니다.

## 환경

- Java 8
- Spring Framework 4.3.x
- Maven Java Library jar
- 외부 WAS 배포 환경: JEUS 등
- Spring Boot 미사용
- Lombok 미사용

## Maven Dependency

업무 프로젝트의 `pom.xml`에 아래 dependency를 추가합니다.

```xml
<dependency>
    <groupId>com.e9pay.common</groupId>
    <artifactId>depl</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Component Scan 설정

이 라이브러리는 Spring Boot AutoConfiguration을 제공하지 않습니다. 사용하는 업무 프로젝트에서 component-scan을 명시해야 합니다.

### XML 방식

```xml
<context:component-scan base-package="com.e9pay.common.depl" />
```

### Java Config 방식

```java
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.e9pay.common.depl")
public class DeplMonitorImportConfig {
}
```

또는 라이브러리에서 제공하는 `com.e9pay.common.depl.config.DeplMonitorConfig`를 import해서 사용할 수 있습니다.

## ActiveRequestInterceptor 설정

`ActiveRequestInterceptor`는 현재 처리 중인 HTTP 요청 수를 측정하기 위한 Spring MVC Interceptor입니다.

- ThreadMXBean으로 추정하지 않고 Spring MVC 요청 진입/종료 시점에서 직접 카운트합니다.
- `preHandle`에서 현재 요청 수와 전체 요청 수를 증가시킵니다.
- `afterCompletion`에서 정상/예외 여부와 관계없이 현재 요청 수를 감소시킵니다.
- `/v1/api/actuator/metrics/thread` 응답에 `activeHttpRequestCount`, `totalHttpRequestCount`, `maxActiveHttpRequestCount`가 포함됩니다.

`activeHttpRequestCount`는 JVM 전체 Thread 수가 아니라 현재 처리 중인 HTTP 요청 수입니다. 동기 Spring MVC 구조에서는 요청 1개가 일반적으로 WAS Worker Thread 1개를 점유하므로 현재 요청 처리 중인 Worker Thread 수에 가까운 값으로 볼 수 있습니다. 단, 비동기 요청, 별도 Executor, 배치 Thread, Scheduler Thread는 포함하지 않습니다.

이 Interceptor는 업무 프로젝트의 Spring MVC 설정에 직접 등록해야 합니다. actuator 조회 요청 자체가 카운트에 포함되지 않도록 `/v1/api/actuator/**` 경로는 exclude 하는 것을 권장합니다.

### XML 방식

```xml
<mvc:interceptors>
    <mvc:interceptor>
        <mvc:mapping path="/**" />
        <mvc:exclude-mapping path="/v1/api/actuator/**" />
        <bean class="com.e9pay.common.depl.web.ActiveRequestInterceptor" />
    </mvc:interceptor>
</mvc:interceptors>
```

`mvc` namespace가 없다면 XML 상단에 namespace를 추가해야 합니다.

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:mvc="http://www.springframework.org/schema/mvc"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="
           http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
           http://www.springframework.org/schema/mvc http://www.springframework.org/schema/mvc/spring-mvc.xsd">
    ...
</beans>
```

### Java Config 방식

```java
import com.e9pay.common.depl.web.ActiveRequestInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@EnableWebMvc
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ActiveRequestInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/v1/api/actuator/**");
    }
}
```

기존 프로젝트에서 이미 `WebMvcConfigurerAdapter`를 사용 중이면 새 설정 클래스를 만들지 말고 기존 설정 클래스의 `addInterceptors`에 `ActiveRequestInterceptor` 등록만 추가하면 됩니다.

## Endpoint

모든 actuator 유사 API는 `/v1/api/actuator/` 하위에 있습니다.

상세한 설치 절차, 호출 예시, 응답 필드 설명, 운영 체크리스트는 [API 사용법 문서](docs/API_USAGE.md)를 참고합니다.

| Method | URL | 설명 |
| --- | --- | --- |
| GET | `/v1/api/actuator/health` | 기본 health |
| GET | `/v1/api/actuator/health/db` | DB health |
| GET | `/v1/api/actuator/metrics` | 전체 metrics |
| GET | `/v1/api/actuator/metrics/thread` | JVM thread metrics |
| GET | `/v1/api/actuator/metrics/memory` | JVM memory metrics |
| GET | `/v1/api/actuator/metrics/system` | 시스템 정보 |

## 응답 JSON 예시

### GET /v1/api/actuator/health

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

### GET /v1/api/actuator/health/db

DataSource가 없는 경우:

```json
{
  "timestamp": 1718179200000,
  "status": "UNKNOWN",
  "db": "NO_DATASOURCE"
}
```

정상인 경우:

```json
{
  "timestamp": 1718179200000,
  "status": "UP",
  "db": "UP"
}
```

장애인 경우:

```json
{
  "timestamp": 1718179200000,
  "status": "DOWN",
  "db": "DOWN",
  "message": "Connection is not available"
}
```

### GET /v1/api/actuator/metrics

```json
{
  "status": "UP",
  "thread": {
    "current": 42,
    "peak": 50,
    "daemon": 20,
    "totalStarted": 120,
    "activeHttpRequestCount": 3,
    "totalHttpRequestCount": 152003,
    "maxActiveHttpRequestCount": 27,
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

### GET /v1/api/actuator/metrics/thread

```json
{
  "current": 85,
  "peak": 120,
  "daemon": 60,
  "totalStarted": 5420,
  "activeHttpRequestCount": 3,
  "totalHttpRequestCount": 152003,
  "maxActiveHttpRequestCount": 27,
  "state": {
    "NEW": 0,
    "RUNNABLE": 12,
    "BLOCKED": 1,
    "WAITING": 50,
    "TIMED_WAITING": 22,
    "TERMINATED": 0
  },
  "deadlock": false,
  "deadlockedThreadCount": 0
}
```

## 운영 보안 주의사항

이 endpoint들은 JVM, OS, DB 상태 정보를 노출합니다. 운영 환경에서는 외부에 공개하지 않아야 합니다.

권장 제한 방식:

- 내부 IP에서만 접근 허용
- WebtoB, JEUS, 방화벽 정책으로 URL 접근 제한
- Spring MVC Interceptor 또는 Filter로 인증/인가 적용
- `/v1/api/actuator/**` 경로를 외부망 라우팅에서 제외

## Spring Dependency를 provided로 둔 이유

이 라이브러리는 기존 업무 Spring MVC 프로젝트 안에서 dependency로 추가되어 동작하는 jar입니다. Spring Framework, Spring MVC, Spring JDBC, Servlet API는 업무 애플리케이션과 외부 WAS가 이미 제공하거나 관리하는 영역이므로 라이브러리 jar에 함께 포함하지 않습니다.

`provided` scope로 두면 다음 이점이 있습니다.

- 업무 프로젝트의 Spring 4.3.x 버전과 충돌을 줄일 수 있습니다.
- JEUS 같은 외부 WAS의 Servlet API와 중복 패키징을 피할 수 있습니다.
- Spring Boot 또는 별도 실행형 서버 구조를 강제하지 않습니다.
