## 학습 정리

### 2장 동작 원리와 프론트/백 책임 분배
- Oauth2 Code Grant 동작 방식은 다음과 같다
1. 로그인 페이지
2. 성공 후 코드 발급 (redirect_url)
3. 코드를 통해 access 토큰 요청
4. access 토큰 발급 완료
5. access 토큰을 통해 사용자 정보 요청
6. 사용자 정보 획득 완료

#### JWT 방식에서 OAuth2 클라이언트 구성시 고민점
- JWT 방식에서 JWT 발급 문제와 웹/하이브리드/네이티브앱별 특징에 의해 Oauth2 Code Grant 방식 동작의 책임을 프론트측에 둘지 백엔드 측에 둘지 고민이 필요하다.

**JWT 발급 문제**
- 프론트엔드에서의 로그인 처리
  - 프론트에서 소셜 로그인 버튼 클릭
  - 소셜 로그인창이 열림
  - 로그인 성공 후 소셜 제공자가 redirect_url로 사용자를 리다이렉션(code 포함)
  - code 정보를 캐치하여 백엔드에 전송
  - 백엔드는 JWT를 생성하여 프론트엔드에 반환
  - 문제점은 프론트에서 하이퍼링크(redirect_url)로 실행했기 때문에 JWT를 받을 로직이 없음
  - 방법은 있음
- 백엔드에서의 로그인 처리 
  - 위 문제를 해결하기 위해 백엔드가 소셜 로그인 페이지로 사용자를 리다이렉션
  - 로그인 성공 후 리다이렉트 받은 코드를 백엔드에서 처리하여 JWT를 생성 후 클라이언트에게 반환
  - 클라이언트는 백엔드에 로그인 요청만 날림

**웹/하이브리드/네이티브앱별 특징**
  - 웹 환경에서는 UI 변경이 자연스러움
  - 하이브리드 앱은 웹뷰로 보이기 때문에 UX 적으로 좋지 않을 수 있음
  - 또한, 쿠키 소멸 현상이 일어날수 있음
  - 네이티브 앱은 리다이렉션 처리가 어렵고, 쿠키 대신 기기의 저장소를 사용
  - 소셜 로그인 SDK를 사용하거나, 커스텀 URL 스킴을 사용

#### 프론트/백 책임 분배
**모든 책임을 프론트가 맡음**
![02-front.png](./image/02-front.png)
- 프론트단에서 (로그인 → 코드 발급 → Access 토큰 → 유저 정보 획득) 과정을 모두 수행
- 그 후 백엔드단에서 (유저 정보 → JWT 발급) 방식으로 주로 네이티브앱에서 사용하는 방식.
- 프론트에서 보낸 유저 정보의 진위 여부를 따지기 위해 추가적인 보안 로직이 필요하다.
  - API 통신에서 HTTPS는 필수
  - 모바일 앱에서 위조를 방지하기 위한 토큰(대칭, 비대칭 등, 다른 위조된 프론트와 구별하기 위한 고유 토큰)을 서버측에 요청하여 검증 로직을 추가해도 좋다

**책임을 프론트와 백이 나눠 가짐**
![02-front_backend.png](./image/02-front_backend.png)
- 잘못된 방식, 대부분의 웹 블로그가 이 방식으로 구현
- 프론트단에서 (로그인 → 코드 발급) 후 코드를 백엔드로 전송 백엔드단에서 (코드 → 토큰 발급 → 유저 정보 획득 → JWT 발급)
- 두번째 그림은 생략했지만 프론트단에서  (로그인 → 코드 발급 → Access 토큰) 후 Access 토큰을 백엔드로 전송 백엔드단에서 (Access 토큰 → 유저 정보 획득 → JWT 발급)
- 둘다 잘못된 방식
- 카카오와 같은 대형 서비스 개발 포럼 및 보안 규격에서 위와 같은 코드/Access 토큰을 전송하는 방법을 지양함
- 하지만 토이로 구현하기 쉬워 자주 사용한다.

**모든 책임을 백엔드가 맡음**
![02-backend.png](./image/02-backend.png)
- 프론트단에서 백엔드의 OAuth2 로그인 경로로 하이퍼링킹을 진행 후 백엔드단에서 (로그인 페이지 요청 → 코드 발급 → Access 토큰 → 유저 정보 획득 → JWT 발급) 방식으로 주로 웹앱/모바일앱 통합 환경 서버에서 사용하는 방식.
- 백엔드에서 JWT를 발급하는 방식의 고민과 프론트측에서 받는 로직을 처리해야 한다.

#### 카카오 dev 톡에 적혀 있는 프론트/백 책임 분배
![02-kakao.png](./image/02-kakao.png)
![02-kakao2.png](./image/02-kakao2.png)
- 카카오 dev 톡에 적혀 있는 프론트와 백엔드가 책임을 나눠 가지는 질문에 대한 카카오 공식 답변
- 네이티브 앱에 대해서는 모든 책임을 프론트가 일임하고 코드나 Access 토큰을 전달하는 행위 자체를 지양
- 다른 자료들 에서도 코드나 Access 토큰을 전달하는 행위를 금지
- 웹에서 구현하는 경우 프론트에서 정보 위조가 가능하기 때문에 프론트측에서 책임을 가지는거 자체를 지양
  - 모 유명한 기업에서 근무하신 분 의견은 JWT를 발급, 관리, 인증하는 것에 대한 프론트의 역할이 조금이라도 섞인다는 것이 말도 안됨
  - 프론트로 넘어가는 순간 외부인이 조작할 여지가 있기 때문이다.

### 4장 동작 원리
![04-architecture.png](./image/04-architecture.png)

### 5장 변수 역할
- OAuth2를 구현하기 위해선 registration와 provider 정보를 입력해야 한다. 
- registration은 아래 정보를 입력한다
  - client-name: 서비스명
  - client-id: 서비스에서 발급 받은 아이디
  - client-secret: 서비스에서 발급 받은 비밀번호
  - redirect-uri: 서비스에 등록한 우리쪽 로그인 성공 URI
  - authorization-grant-type: 인증 방식
  - scope: 리소스 서버에서 가져올 데이터 범위
- provider는 아래 정보를 입력한다
  - authorization-uri: 서비스 로그인 창 주소
  - token-uri: 토큰 발급 서버 주소
  - user-info-uri: 사용자 정보 획득 주소
  - user-name-attribute: 응답 데이터 변수
- provider 같은 경우 구글, okta, 페이스북, 깃허브는 내부적으로 데이터를 가지고 있어서 등록하지 않아도 된다.

```yml
#registration
spring.security.oauth2.client.registration.naver.client-name=naver
spring.security.oauth2.client.registration.naver.client-id=발급아이디
spring.security.oauth2.client.registration.naver.client-secret=발급비밀번호
spring.security.oauth2.client.registration.naver.redirect-uri=http://localhost:8080/login/oauth2/code/naver
spring.security.oauth2.client.registration.naver.authorization-grant-type=authorization_code
spring.security.oauth2.client.registration.naver.scope=name,email

#provider
spring.security.oauth2.client.provider.naver.authorization-uri=https://nid.naver.com/oauth2.0/authorize
spring.security.oauth2.client.provider.naver.token-uri=https://nid.naver.com/oauth2.0/token
spring.security.oauth2.client.provider.naver.user-info-uri=https://openapi.naver.com/v1/nid/me
spring.security.oauth2.client.provider.naver.user-name-attribute=response
```
- 네이버는 회원 조회 시 JSON 형태로 반환되므로 user-name-attribute 값을 response로 설정
```java
스프링 시큐리티에서는 하위 필드를 명시할 수 없고, 최상위 필드만 user_name으로 설정이 가능하다. 
네이버의 응답값 최상위 필드는 resultCode, message, response이므로 response를 user_name으로 설정하고,
이후 자바 코드로 response의 id를 user_name으로 지정한다.
```

### 9장 OAuth2UserService 응답 받기
- oauth2.0 의존성에 의해서 아래는 구현되어 있다. 
  - OAuth2AuthorizationRequestRedirectFilter   
  - OAuth2LoginAuthentacationFilter
  - OAuth2LoginAuthencationProvider
- 네이버와 구글에 맞게 데이터를 받기 위한 DTO를 분리
- 네이버 데이터
```json
{
  resultcode=00, message=success, response={id=123123123, name=개발자유미}
}
```
- 구글 데이터
```json
{
  resultcode=00, message=success, id=123123123, name=개발자유미
}
```
