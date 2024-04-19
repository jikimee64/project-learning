## 학습 정리

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

### 5장 DB연결 및 Entity 작성
```java
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
```
- spring.jpa.hibernate.naming.physical-strategy
  - Hibernate의 테이블 이름, 칼럼 이름 등 DB의 물리적인 네이밍 전략을 설정하는 속성
  - PhysicalNamingStrategyStandardImpl은 별도의 변환을 수행 하지 않고 엔티티에 정의된 이름 그대로 DB 스키마에 반영
  - 기본 전략은 org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy
    - 카멜케이스를 스네이크 케이슬 변환(소문자)

### 7장 로그인 필터 구현
- 필터
  - WAS와 서블릿 사이에 특정 처리를 수행하는 곳
  - HTTP 요청 -> WAS -> 필터1 -> 필터2 ... -> 서블릿 -> 컨트롤러
- DelegatingFilterProxy란?
  - 전통적인 서블릿 필터는 Spring IoC의 도움을 받을 수 없다.
  -  DelegatingFilterProxy는 이러한 한계를 극복하기 위해 만들어진 클래스
  - 서블릿 컨테이너의 필터 생명주기안에서 Spring의 FilterChainProxy 빈을 사용할 수 있다.
  - FilterChainProxy 빈을 찾은뒤 해당 빈에게 시큐리티 필터링 작업을 위임한다.
- FilterChainProxy
  - 스프링 시큐리티의 필터들을 관리하고 제어한다.(순서 제어 등)
![img.png](image/필터모식도.png)  
---
![img.png](image/로그인_모식도.png)
- UsernamePasswordAuthenticationFilter
  - 세션의 formLogin 방식으로 했으면 자동으로 적용이 되었다.
  - 하지만 JWT 기반에서 formLogin을 disabled했기 때문에 기본적으로 활성화 되어 있는 UsernamePasswordAuthenticationFilter는 동작하지 않는다. 
  - 따라서 상속을 받아서 직접 구현을 해줘야 동작한다.
  - authenticationManager를 통해 인증을 진행할 수 있도록 토큰값을 만들어서 넘겨준다.
  - 로그인 성공시 실행하는 메서드인 `successfulAuthentication`와 로그인 실패시 실행하는 메서드인 `unsuccessfulAuthentication`를 오버라이딩 하여 구현할 수 있다. 
  - 기본 옵션
    - /login PATH 경로 
    - form-data, 필드값은 username, password 

  - PATH 경로를 수정하고 싶을 경우
  ```java
  LoginFilter loginFilter = new LoginFilter(authenticationManager(authenticationConfigutration), jwtUtil);
  loginFilter.setFilterProcessesUrl("경로설정");
  
  http
      .addFilterAt(loginFilter, UsernamePasswrodAuthenticationFilter.class);
  ```

※ 스프링 시큐리티는 모든 처리를 필터단에서 처리한다.

### 8장 DB기반 로그인 검증 로직
- LoginFilter의 authenticationManager.authenticate(authToken);가 UserDetailsService의 loadUserByUsername 메서드를 호출한다.
  - 하지만 UserDetailsService의 구현체는 단 하나여야 스프링 시큐리티가 찾을 수 있다. 
- loadUserByUsername 메서드는 username을 받아서 계정이 존재하면 UserDetails를 반환한다.
- 비밀번호 검증 로직은 따로 존재하지 않는데 어디서 진행하는 걸까?
  - AutheticationManager 구현체인 ProviderManager에서 AuthenticationProvider 타입을 합성으로 가진다.
  - AuthenticationProvider의 구현체인 DaoAuthenticationProvider에서 비밀번호 검증을 진행한다.
  - 즉, 사용자에게 받은 데이터와 DB에서 조회한 데이터를 검증하고 성공/실패를 판단하는 구현부분
  - DB에 암호화되어 저장되어 있다면 로그인시 입력되는 비밀번호는 DaoAuthenticationProvider에서 자동으로 암호화를 한다.

### 9장 JWT 발급 및 검증 클래스
- 사용한 암호화 알고리즘이 HS256이면 256비트(32byte)의 키값을 사용해야 한다.
