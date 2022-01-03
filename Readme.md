# 스프링 핵심 원리 - 기본편

## Section 1 : 객체 지향 설계와 스프링

스프링은 DI(의존성주입)과 DI 컨테이너를 제공하여 다형성 + OCP,DIP를 가능하도록 지원한다. 클라이언트 코드의 변경 없이 기능을 확장 쉽게 부품을 교체하듯이 개발할 수 있다.

순수하게 자바로 OCP, DIP 원칙들을 지키면서 개발을 하다 보면 결국 스프링 프레임 워크를 만들게 된다.

공연을 설계하듯 배역만 만들어 두고 배우는 언제든지 유연하게 변경할 수 있도록 만드는것이 좋은 객체지향 설계이다.

이상적으로는 모든 설계에 인터페이스를 부여하자.

하지만 인터페이스를 도입하면 추상화라는 비용이 발생한다. 따라서 기능을 확장할 가능성이 없다면, 구체 클래스를 직접사용하고, 향후 필요한 경우에 리팩터링해서 인터페이스를 도입하는 것도 방법.

## Section 2 : 순수 자바 예제

### 비즈니스 요구사항과 설계

- 회원
    - 회원을 가입하고 조회할 수 있다.
    - 회원은 일반과 VIP 두 가지 등급이 있다.
    - 회원 데이터는 자체 DB를 구축할 수 있고, 외부 시스템과 연동할 수 있다. (미확정)
- 주문과 할인 정책
    - 회원은 상품을 주문할 수 있다.
    - 회원 등급에 따라 할인 정책을 적용할 수 있다.
    - 할인 정책은 모든 VIP는 1000원을 할인해주는 고정 금액 할인을 적용해달라. (나중에 변경 될 수 있다.)
    - 할인 정책은 변경 가능성이 높다. 회사의 기본 할인 정책을 아직 정하지 못했고, 오픈 직전까지 고민을 미루고 싶다. 최악의 경우 할인을 적용하지 않을 수 도 있다. (미확정)

> 당장 결정하기 어려운 부분들은 인터페이스만 설계하여 나중에 갈아끼우면 된다.

### Test Code 작성

given -> when -> then

```java
class MemberServiceTest {
    MemberService memberService;

    @BeforeEach
    public void beforeEach() {
        AppConfig appConfig = new AppConfig();
        memberService = appConfig.memberService();
    }

    @Test
    void join() {
        // given
        Member member = new Member(1L, "memberA", Grade.VIP);

        // when
        memberService.join(member);
        Member findMember = memberService.findMember(1L);

        // then
        Assertions.assertThat(member).isEqualTo(findMember);
    }
}
```

### 역할과 구현의 분리 
- OrderService
  - OrderServiceImple
- MemberRepository
  - MemoryMemberRepository
  - DbMemberRepository
- DiscountPolicy
  - FixDiscountPolicy
  - RateDiscountPolicy

역할과 구현을 분리하여 설계를 하면 역할에 대해 유연하게 구현 객체를 변경할 수 있게 된다.

### 단위테스트 
- 스프링이나 다른 컨테이너의 도움 없이 순수한 자바코드를 통한 테스트
- 프로그램이 방대해질수록 확인 작업이 느려지기 때문에 단위테스트가 중요하다.

## Section 3 : 객체지향 원리 적용

### OCP, DIP 원칙 준수를 위한 의존관계 해소

`MemberServiceImpl` 클래스가 `MemberRepository`인터페이스 뿐만 아니라 `MemoryMemberRepository`구체 클래스까지 의존하고 있다.

```java
public class MemberServiceImple implements MemberService {
    private final MemberRepository memberRepository = new MemoryMemberRepository();
    
}
```

