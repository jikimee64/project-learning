## 추가로 조사 후 정리한 내용

### 3장 Security Config 클래스 (STATELESS)
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
       //..
    }
}
```
- @EnableWebSecurity를 하지 않으면?
  - 스프링 시큐리티가 제공하는 기본 설정을 사용
  - @EnableWebSecurity를 사용하고 `filterChain` 메서드를 커스터마이징 후 @Bean으로 등록하면 
스프링 시큐리티의 기본 설정은 무시되고 개발자가 커스터마이징한 설정이 적용

```java
        // csrf 비활성화
        http
                .csrf(AbstractHttpConfigurer::disable);
```
- csrf란?
  - Cross Site Request Forgery
  - 웹 어플리케이션 취약점 중 하나
  - 사용자가 인증된 상태에서(로그인한 상태) 사용자의 인증 쿠키를 이용해 의도하지 않은 요청을 보내어 공격자가 사용자의 권한을 이용하는 공격
  - csrf 토큰을 사용하여 방어
    - 서버는 클라이언트에게 토큰을 건네준다
    - 클라리언트는 GET을 제외한 form 형식의 요청에 csrf 토큰을 적재해서 보내준다
    - 서버는 csrf 토큰을 검증하여 요청을 처리한다. 적절하지 않은 csrf 토큰이면 403을 내려준다.
  - JWT 방식에서는 csrf disable 설정해도 괜찮음
    - JWT는 Authorization 헤더에 토큰을 적재해서 전송하기 때문에 csrf 공격에 취약하지 않음
    - JWT 토큰을 쿠키에 담아서 보내면 CSRF 공격에 취약할 수 있음

```java
        // form 로그인 방식 비활성화
        http
                .formLogin(AbstractHttpConfigurer::disable);
```
- form 로그인 비활성화
  - form 로그인 방식은 세션 기반 인증에서 사용된다.
  - 서버에서 인증 상태(세션)를 저장하지 않는 JWT 방식에서는 form 로그인 방식이 필요하지 않다.
  - 참고로 세션 방식을 사용할 경우 브라우저의 JESSION 쿠키에 세션 정보가 저장됨

```java
        // 세션 설정
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );
```
- 세션 비활성화
  - 세션 생성 정책을 STATELESS로 설정
  - JWT 방식을 사용하기 때문에 스프링 시큐리티에게 세션을 사용하지 않겠다고 지시
  - 따라서, **서버측 메모리에 세션을 저장하지 않아** 동일한 브라우저에서 요청하더라도 매번 새로운 사용자로 인식
