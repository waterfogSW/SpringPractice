# 스프링 핵심 원리 - 기본편

## Section 0 : SOLID

- SRP : 단일 책임 원칙(Single responsibility principle)
    - 한 클래스는 하나의 책임만 가져야 한다. 클래스를 변경해야 하는 이유는 오직 하나여야 한다.
- OCP : 개방-폐쇄 원칙(Open/closed principle)
    - 소프트웨어 요소는 확장에는 열려 있으나 변경에는 닫혀 있어야 한다
- LSP : 리스코프 치환 원칙(Liskov substitution principle)
    - 프로그램의 객체는 프로그램의 정확성을 깨뜨리지 않으면서 하위 타입의 인스턴스로 바꿀 수 있어야 한다
- ISP : 인터페이스 분리 원칙(Interface segregation principle)
    - 특정 클라이언트를 위한 인터페이스 여러 개가 범용 인터페이스 하나보다 낫다.
- DIP : 의존관계 역전 원칙(Dependency inversion principle)
    - 추상화에 의존해야지, 구체화에 의존하면 안된다.

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

### OCP, DIP 원칙 위반 사례

`OrderServiceImple` 클래스가 `DiscountPolicy`라는 인터페이스에 의존하게 함으로써 유연한 설계가 가능하게 함.

![image](https://user-images.githubusercontent.com/28651727/148065072-388899c0-dc14-43bd-a6f8-30cb7aceceff.png)

고정할인 정책 `FixDiscountPolicy`와 정률 할인 정책 `RateDiscountPolicy`라는 구체 클래스는 `DiscountPolicy`라는 인터페이스를 구현함으로써 할인 정책 교체가 가능함.

```java
public class OrderServiceImple implements OrderService {
    //    private final DisountPolicy disountPolicy = new FixDiscountPolicy();
    private final DisountPolicy disountPolicy = new RateDiscountPolicy(); // 구체클래스에 의존하기 때문에 OCP를 위반한다.
}
```

`OrderServiceImple`클래스는 겉으로는 `DiscountPolicy`클래스에 의존하는것으로 보이나 
고정 할인 정책 `FixDiscountPolicy`에서 정률할인 정책 `RateDiscountPolicy`로 변경하기 위해서는 
위와 같이 클라이언트 코드(`OrderServic eImple`)의 수정이 불가피 하다.

결국 할인정책의 선택지가 2가지로 늘어났다는 점에서 확장에는 열려있으나 변경에는 닫혀있지 않은것으로, **OCP**를 위반한다.

`OrderServiceImple`은 `DisountPolicy`인터페이스 뿐만 아니라 `FixDiscountPolicy`에도 의존하고 있다.
구체화에 의존하므로 **DIP**를 위반한다.

### AppConfig

`AppConfig` : 애플리케이션 전체를 설정하고 구성하는 클래스. **구현 객체를 생성**하고 **연결**하는 책임을 가짐

- **DIP**, **OCP**를 위반하는 `OrderServiceImple`과 `MemberServiceImple`이 인터페이스에만 의존하도록 생성자 구현
- `AppConfig`는 애플리케이션의 실제 동작에 필요한 구현 객체를 생성. 
- 객체 인스턴스의 참조를 **생성자를 통해 주입**
  - `MemberServiceImple` -> `MemoryMemberRepository`
  - `OrderServiceImple` -> `MemoryMemberRepository`, `FixDiscountPolicy`

이를 통해 구체클래스 `MemberServiceImple`은 `MemberRepository`인터페이스에만 의존한다.(DIP를 준수한다)

### SOLID 원칙 적용

#### SRP : 단일 책임 원칙

한 클래스는 하나의 책임만 가져야 한다.

- 기존 클라이언트 객체는 직접 구현 객체를 생성하고 연결하고 실행하는 책임을 가지고 있었다.
- 구현 객체를 생성하고 연결하는 책임을 `AppConfig`가 담당하게 함으로써 클라이언트 객체는 실행하는 책임만 가짐.

#### DIP : 의존관계 역전 원칙

프로그래머는 추상화에 의존해야지 구체화에 의존하면 안된다.

- 기존 클라이언트 코드 `OrderServiceImple`은 `DiscountPolicy`라는 추상 뿐만 아니라, `FixDiscountPolicy`라는 구체화에도 의존하였다.
- `AppConfig`가 `OrderServiceImple`의 생성자를 통해 의존관계를 주입함으로써 인터페이스에만 의존하도록 설계를 변경. DIP 원칙을 준수하도록 했다.

#### OCP : 개방 폐쇄 원칙

확장에는 열려있으나, 변경에는 닫혀있어야 한다.

- 애플리케이션을 사용 영역과 구성영역(`AppConfig`)으로 나누었다.
- `AppConfig`가 의존관계를 변경하므로, 클라이언트 코드를 변경하지 않아도 된다.



