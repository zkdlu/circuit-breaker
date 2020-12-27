# circuit breaker

MSA에서 A서비스가 B서비스를 API호출하여 사용하는데 B서비스에 장애가 발생할 경우,  A서비스까지 장애가 전파 될 수 있다.

일부는 timeout과 try-catch로 해결이 가능하겠지만, request가 timeout이 될 때까지 thread pool이나 db connection pool을 선점하고 있으면 리소스가 부족해지면서, 같은 리소스를 사용하는 부분에 장애가 전파 될 수 있다.



Circuit Breaker 패턴은 A서비스와 B서비스 중간에 위치하여  B서비스에 대한 모든 호출은 Circuit breaker을 통하고 정상적인 상황에서는 트래픽을 bypass 해준다. 만약 B서비스에 장애를 감지하면 호출을 강제로 끊어 장애가 퍼지는 것을 방지한다.



## Hystrix

Netfilx에서 Circuit Breaker Pattern을 구현한 오픈소스 라이브러리



## 예제

1. 의존성 추가

   ```gradle
   dependencies {
       implementation 'org.springframework.cloud:spring-cloud-starter-netflix-hystrix'
   }
   ```

2. @EnableCircuitBreaker 어노테이션 추가

   ```java
   @EnableCircuitBreaker
   @SpringBootApplication
   public class CircuitBreakerApplication {
   ```

3. TestController.java 작성

   ```java
   @RestController
   public class TestController {
       private final TestService testService;
   
       public TestController(TestService testService) {
           this.testService = testService;
       }
   
       @GetMapping("/")
       public String index(String key) {
           return testService.getResult(key);
       }
       
      @GetMapping("/test")
       public String test(String key) {
           if (!key.equals("test")) {
               throw new RuntimeException("Invalid Key");
           }
   
           return "OK";
       }    
   }
   ```

   > 원래 다른 서비스를 호출하여야 하지만 테스트용으로,  http://localhost:8080/ 를 호출하면 서비스는 http://localhost:8080/test로 진입하도록 함.

4. TestService.java 작성

   ```java
   @Service
   public class TestService {
       private final RestTemplate restTemplate;
   
       public TestService(RestTemplate restTemplate) {
           this.restTemplate = restTemplate;
       }
   
       @Bean
       public RestTemplate restTemplate() {
           return new RestTemplate();
       }
   
       @HystrixCommand(fallbackMethod = "fallbackResult")
       public String getResult(String key) {
           return restTemplate.getForObject("http://localhost:8080/test?key=" + key, String.class);
       }
   
       public String fallbackResult(String key) {
           System.out.println("Hello " + key);
           return "return fallback";
       }
   }
   ```

   > Circuit Breaker를 사용할 메서드에 @HystrixCommand 메서드와 함께 fallback 메서드를 정의한다.

