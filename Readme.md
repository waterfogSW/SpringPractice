# 스프링 핵심 원리 - 기본편

## Section 0 : SOLID

- SRP : 단일 책임 원칙(Single responsibility principle)
    - 한 클래스는 하나의 책임만 가져야 한다. 클래스를 변경해야 하는 이유는 오직 하나여야 한다.
    - 변경의 파급효과가 적다면 SRP원칙을 잘 준수한 것으로 볼 수 있다.
- OCP : 개방-폐쇄 원칙(Open/closed principle)
    - 소프트웨어 요소는 확장에는 열려 있으나 변경에는 닫혀 있어야 한다.
    - OCP원칙을 지키기 위해 스프링 컨테이너를 활용
- LSP : 리스코프 치환 원칙(Liskov substitution principle)
    - 프로그램의 객체는 프로그램의 정확성을 깨뜨리지 않으면서 하위 타입의 인스턴스로 바꿀 수 있어야 한다
    - 예를들어 자동차 인터페이스의 엑셀 메서드를 구현할 경우, 엑셀이 후진하도록 구현해서는 안된다.
- ISP : 인터페이스 분리 원칙(Interface segregation principle)
    - 특정 클라이언트를 위한 인터페이스 여러 개가 범용 인터페이스 하나보다 낫다.
    - 인터페이스의 역할 구분이 명확해지고, 인터페이스의 변경이 다른 인터페이스의 클라이언트에 영향을 주지 않는다.
- DIP : 의존관계 역전 원칙(Dependency inversion principle)
    - 추상화에 의존해야지, 구체화에 의존하면 안된다.
    - 클라이언트 코드는 인터페이스에만 의존하고 인터페이스의 구현에 의존해선 안된다.

## Section 1 : 객체 지향 설계와 스프링

- 스프링은 DI(의존성주입)과 DI 컨테이너를 제공하여 다형성 + OCP,DIP를 가능하도록 지원한다. 
  - 클라이언트 코드의 변경 없이 기능을 확장 쉽게 부품을 교체하듯이 개발할 수 있다.
- 순수하게 자바로 OCP, DIP 원칙들을 지키면서 개발을 하다 보면 결국 스프링 프레임 워크를 만들게 된다.
- 공연을 설계하듯 배역만 만들어 두고 배우는 언제든지 유연하게 변경할 수 있도록 만드는것이 좋은 객체지향 설계이다.

**실무 고민**
- 이상적으로는 모든 설계에 인터페이스를 부여하자.
  - 하지만 인터페이스를 도입하면 추상화라는 비용이 발생한다. 따라서 기능을 확장할 가능성이 없다면,   
구체 클래스를 직접사용하고, 향후 필요한 경우에 리팩터링해서 인터페이스를 도입하는 것도 방법.

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

`OrderServiceImple`클래스는 겉으로는 `DiscountPolicy`클래스에 의존하는것으로 보이나 고정 할인 정책 `FixDiscountPolicy`에서 정률할인
정책 `RateDiscountPolicy`로 변경하기 위해서는 위와 같이 클라이언트 코드(`OrderServic eImple`)의 수정이 불가피 하다.

결국 할인정책의 선택지가 2가지로 늘어났다는 점에서 확장에는 열려있으나 변경에는 닫혀있지 않은것으로, **OCP**를 위반한다.

`OrderServiceImple`은 `DisountPolicy`인터페이스 뿐만 아니라 `FixDiscountPolicy`에도 의존하고 있다. 구체화에 의존하므로 **DIP**를 위반한다.

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

### IoC, DI, 컨테이너

> **프레임 워크 vs 라이브러리**
> - 프레임 워크는 내가 작성한 코드를 제어하고 대신 실행한다.
> - 반면 내가 작성한 코드가 직접 제어의 흐름을 담당하면 라이브러리이다.

#### IoC : 제어의 역전

- `AppConfig`가 구현 객체를 생성하고 연결하는 역할을 함으로써 기존 구현 객체는 자신의 로직을 실행하는 역할만 담당한다.
- 즉 프로그램의 제어흐름을 `AppConfig`가 가져간것으로 볼 수 있다. 이렇듯 제어흐름을 구현 객체가 아닌, 외부에서 관리하는 것을 제어의 역전 이라 한다.

#### DI : 의존 관계 주입

정적 클래스 의존관계

- 클래스가 사용하는 import 코드만으로도 의존 관계를 쉽게 파악할 수 있다.

동적인 객체 인스턴스 의존관계

- 애플리케이션 실행 시점(런타임)에 외부에서 실제 구현 객체를 생성하고 클라이언트에 전달해서 클라이언트와 서버의 실제 의존관계가 연결되는것을 **의존관계 주입** 이라 한다.
- 의존 관계 주입을 사용하면 클라이언트 코드를 변경하지 않고, 클라이언트가 호출하는 대상의 타입 인스턴스를 변경할 수 있다.

#### IoC 컨테이너, DI 컨테이너

- AppConfig 처럼 객체를 생성하고 관리하면서 의존관계를 연결해 주는 것을 IoC 컨테이너 또는 DI 컨테이너라 한다.
- 의존관계 주입에 초점을 맞추어 최근에는 주로 DI 컨테이너라 한다.
- 어샘블러, 오브젝트 팩토리 등으로 불리기도 한다.

### 스프링으로의 전환

`AppConfig`

```java

@Configuration // 설정 구성
public class AppConfig {
    @Bean // 스프링 컨테이너에 스프링 빈으로 등록
    public MemberService memberService() {
        return new MemberServiceImpl(memberRepository());
    }
    //...
}
```

- `@Configuration` : 설정 구성
- `@Bean` : 스프링 컨테이너에 스프링 빈으로 등록

```java
public class OrderApp {
    public static void main(String[] args) {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
        MemberService memberService = applicationContext.getBean("memberService", MemberService.class);
        OrderService orderService = applicationContext.getBean("orderService", OrderService.class);
        // ...
    }
}
```

- `AnnotationConfigApplicationContext` : 스프링 컨테이너 불러오기
    - 스프링 컨테이너는 `@Configuration` 어노테이션이 붙은 클래스를 설정 정보로 사용한다.
    - 여기서 `@Bean`이라 적힌 메서드를 모두 호출해서 반환된 객체를 스프링 컨테이너에 등록
    - 스프링 컨테이너에 등록된 객체를 스프링 빈 이라 함.
- `.getBean()`을 통해 스프링 빈을 불러올 수 있다.

## Section 4 : 스프링 컨테이너와 스프링 빈

### 스프링 컨테이너 생성

- `ApplicationContext`를 스프링 컨데이너라고 하며, 인터페이스이다.
- `new AnnotationConfigApplicationContext(AppConfig.class);`는 구현체이다.

> 스프링은 `BeanFactory`와 `ApplicationContext`로 구분된다.

1. 스프링 컨테이너 생성

- `new AnonotationConfigApplicationContext(AppConfig.class)`
- 스프링 컨테이너 생성시 `AppConfig.class`와 같은 구성정보를 지정해 주어야 한다.

2. 스프링 빈 등록

- 설정정보를 통해 스프링 빈을 등록한다.

```
@Bean
public MemberRepository memberRepository(){}
```

- 빈의 이름은 `@Bean(name="memberService2")`와 같이 이름을 직접 부여할 수도 있다.
- 빈의 이름을 직접부여할 경우 항상 **다른 이름**을 부여해야 한다.

3. 의존 관계 주입

- 스프링 컨테이너는 설정 정보를 참고하여 의존관계를 주입한다.

### 스프링 빈 조회

```java
public class ApplicationContextInfoTest {
    AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);

    @Test
    @DisplayName("모든 빈 출력하기")
    void findAllBean() {
        String[] beanDefinitionNames = ac.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            Object bean = ac.getBean(beanDefinitionName);
            System.out.println("name = " + beanDefinitionName + " object = " + bean);
        }
    }

    @Test
    @DisplayName("애플리케이션 빈 출력하기")
    void findApplicationBean() {
        String[] beanDefinitionNames = ac.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            BeanDefinition beanDefinition = ac.getBeanDefinition(beanDefinitionName);

            // Role : ROLE_APPLICATION : 사용자가 정의한 빈
            // Role : ROLE_INFRASTRUCTURE : 스프링 내부에서 사용하는 빈
            if (beanDefinition.getRole() == BeanDefinition.ROLE_APPLICATION) {
                Object bean = ac.getBean(beanDefinitionName);
                System.out.println("name = " + beanDefinitionName + " object = " + bean);
            }
        }
    }
}
```

- 모든 빈 출력하기
    - `ac.getBeanDefinitionNames()`
- 애플리케이션 빈 출력하기
    - `.getRole()`로 스프링 내부에서 사용하는 빈인지, 사용자가 정의한 빈인지 구분할 수 있다.
    - ROLE_APPLICATION : 사용자가 정의한 빈
    - ROLE_INFRASTRUCTURE : 스프링 내부에서 사용하는 빈
- 스프링 컨테이너에서 스프링 빈을 찾는 기본적인 방법
    - `ac.getBean(빈이름, 타입)`
    - `ac.getBean(타입)`

```java
public class ApplicationContextBasicFindTest {
    AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);

    @Test
    @DisplayName("빈 이름으로 조회")
    void findBeanByName() {
        MemberService memberService = ac.getBean("memberService", MemberService.class);
        assertThat(memberService).isInstanceOf(MemberServiceImpl.class);
    }

    @Test
    @DisplayName("이름 없이 타입으로만 조회")
    void findBeanByType() {
        MemberService memberService = ac.getBean(MemberService.class);
        assertThat(memberService).isInstanceOf(MemberServiceImpl.class);
    }

    @Test
    @DisplayName("구체 타입으로 조회")
        // 구체 타입으로 조회하는것은 DIP 원칙 위배이므로 권장되지 않는다.
    void findBeanByName2() {
        MemberService memberService = ac.getBean("memberService", MemberServiceImpl.class);
        assertThat(memberService).isInstanceOf(MemberServiceImpl.class);
    }

    @Test
    @DisplayName("빈 이름으로 조회 X")
    void findBeanByNameX() {
        assertThrows(NoSuchBeanDefinitionException.class,
                () -> ac.getBean("xxxx", MemberService.class));
    }
}
```

### 스프링 빈 조회 : 동일한 타입일 경우

```java
public class ApplicationContextSameBeanFindTest {
    AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(SameBeanConfig.class);

    @Test
    @DisplayName("타입으로 조회시 같은 타입이 둘 이상 있으면, 중복 오류가 발생한다")
    void findBeanByTypeDuplicate() {
        assertThrows(NoUniqueBeanDefinitionException.class, () -> ac.getBean(MemberRepository.class));
    }

    @Test
    @DisplayName("타입으로 조회시 같은 타입이 둘 이상 있으면, 빈 이름을 지정하면 된다")
    void findBeanByName() {
        MemberRepository memberRepository = ac.getBean("memberRepository1", MemberRepository.class);
        assertThat(memberRepository).isInstanceOf(MemberRepository.class);
    }

    @Test
    @DisplayName("특정 타입을 모두 조회하기")
    void findAllBeanByType() {
        Map<String, MemberRepository> beansOfType = ac.getBeansOfType(MemberRepository.class);
        assertThat(beansOfType.size()).isEqualTo(2);
    }

    @Configuration
    static class SameBeanConfig {
        @Bean
        public MemberRepository memberRepository1() {
            return new MemoryMemberRepository();
        }

        @Bean
        public MemberRepository memberRepository2() {
            return new MemoryMemberRepository();
        }
    }
}
```

### 스프링 빈 조회 : 상속관계

- 부모타입으로 조회하면, 자식 타입도 함께 조회된다.
- 모든 객체의 최고 부모인 `Object`타입으로 조회하면 모든 스프링 빈을 조회할 수 있다.

```java
public class ApplicationContextExtendsFindTest {
    AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(ApplicationContextExtendsFindTest.TestConfig.class);

    @Test
    @DisplayName("부모 타입으로 조회시, 자식이 둘 이상 있으면, 중복 오류가 발생한다")
    void findBeanByParentTypeDuplicate() {
        assertThrows(NoUniqueBeanDefinitionException.class, () -> ac.getBean(DiscountPolicy.class));
    }

    @Test
    @DisplayName("부모 타입으로 조회시, 자식이 둘 이상 있으면, 빈 이름을 지정하면 된다.")
    void findBeanByParentTypeBeanName() {
        DiscountPolicy discountPolicy = ac.getBean("rateDiscountPolicy", DiscountPolicy.class);
        assertThat(discountPolicy).isInstanceOf(RateDiscountPolicy.class);
    }

    @Test
    @DisplayName("부모 타입으로 모두 조회하기")
    void findAllByParentType() {
        Map<String, DiscountPolicy> beansOfType = ac.getBeansOfType(DiscountPolicy.class);
        assertThat(beansOfType.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("Object 타입으로 모두 조회하기")
    void findAllByObjectType() {
        Map<String, Object> beansOfType = ac.getBeansOfType(Object.class);
        for (String key : beansOfType.keySet()) {
            System.out.println("key = " + key + " value = " + beansOfType.get(key));
        }
    }


    @Configuration
    static class TestConfig {
        @Bean
        public DiscountPolicy rateDiscountPolicy() {
            return new RateDiscountPolicy();
        }

        @Bean
        public DiscountPolicy FixDiscountPolicy() {
            return new FixDiscountPolicy();
        }
    }
}
```

### BeanFactory, ApplicationContext

- BeanFactory
    - 스프링 컨테이너의 최상위 인터페이스로 스프링 빈을 관리하고 조회하는 역할을 담당한다.
    - `getBean()`을 제공한다

- ApplicationContext
    - BeanFactory를 상속하며, 빈을 관리하고 조회하는 기능 외 여러 부가기능을 제공한다.
    - `MessageSource`를 이용한 국제화 기능
    - `EnvironmentCapable` : 로컬, 개발, 운영 환경을 구분하여 처리
    - `ApplicationEventPublisher` : 이벤트를 발행하고 구독
    - `ResourceLoader` : 파일 클래스패스 외부 등에서 리소스 편리하게 조회

ApplicationContext는 BeanFactory를 상속받아 빈 관리기능 뿐만 아니라 앞서 말한 부가기능을 제공한다. 때문에 BeanFactory를 직접 사용할 일은 거의 없으며,
ApplicationContext를 주로 사용한다. 통상적으로 BeanFactory, ApplicationContext 둘다 스프링 컨테이너라 한다.

### XML 기반 스프링 빈 설정 정보 : `appConfig.xml`

```java
public class XmlAppContext {
    @Test
    void xmlAppContext() {
        ApplicationContext ac = new GenericXmlApplicationContext("appConfig.xml");
        MemberService memberService = ac.getBean("memberService", MemberService.class);
        assertThat(memberService).isInstanceOf(MemberService.class);
    }
}
```

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
    <bean id="memberService" class="hello.core.member.MemberServiceImpl">
        <constructor-arg name="memberRepository" ref="memberRepository"/>
    </bean>

    <bean id="memberRepository" class="hello.core.member.MemoryMemberRepository"/>

    <bean id="orderService" class="hello.core.order.OrderServiceImpl">
        <constructor-arg name="memberRepository" ref="memberRepository"/>
        <constructor-arg name="discountPolicy" ref="discountPolicy"/>
    </bean>

    <bean id="discountPolicy" class="hello.core.discount.RateDiscountPolicy"/>
</beans>
```

### 스프링 빈 설정 메타 정보 : BeanDefinition

- 스프링이 다양한 설정 형식을 지원할 수 있는 이유는 `BeanDefinition`이라는 추상화 덕분이다
- `BeanDefinition`은 빈 설정 메타정보 `역할`이라 볼 수 있다.
    - 스프링 컨테이너는 이 메타 정보 `BeanDefinition` 인터페이스에 의존하여 스프링 빈을 생성한다.
    - `BeanDefinition`의 구현은 `AppConfig.class`, `appConfig.xml`, ...등이 될 수 있다.


- 실무에서 `BeanDefinition`을 직접 정의하거나 사용할 일은 거의없다.
- 스프링이 다양한 형태의 설정정보를 `BeanDefinition`으로 추상화해서 사용한다 정도만 알고있으면 된다.

## Section 5 : 싱글톤 컨테이너

- 스프링은 기업용 온라인 서비스 기술을 지원하기 위해 탄생하였기 때문에, 대부분의 스프링 애플리케이션은 웹 애플리케이션이다.
- 웹 애플리케이션은 보통 여러 고객이 동시에 요청을 한다.

```java
public class SingletonTest {
    @Test
    @DisplayName("스프링 없는 순수한 DI 컨테이너")
    void pureContainer() {
        AppConfig appConfig = new AppConfig();
        //1. 조회: 호출할 땜 마다 객체를 생성
        MemberService memberService1 = appConfig.memberService();

        //2. 조회: 호출할 때 마다 객체를 생성
        MemberService memberService2 = appConfig.memberService();

        // 참조값이 다른것을 확인
        Assertions.assertThat(memberService1).isNotSameAs(memberService2);
    }
}
```

- 이때 스프링을 사용하지 않은 순수한 DI 컨테이너(`AppConfig`)는 매 요청마다 객체를 새로 생성한다.
- 이로인해 메모리 낭비가 심하다 -> 객체를 최초 1개만 생성하고 공유하도록 설계하면 된다.

### 싱글톤 패턴

- 클래스의 인스턴스가 딱 1개만 생성되는것을 보장하는 디자인 패턴

```java
public class SingletonService {
    // 1. static 영역에 객체를 딱 1개만 생성해 둔다.
    private static final SingletonService instance = new SingletonService();

    // 2. public 으로 열어서 객체 인스턴스가 필요하면, static메서드를 통해서만 조회하도록 허용한다.
    public static SingletonService getInstance() {
        return instance;
    }

    // 3. 생성자를 private 으로 선언하여 외부에서 생성하지 못하게 한다.
    // -> 좋은 설계는 컴파일 오류만으로 오류를 모두 잡을수 있도록 해야 한다.
    private SingletonService() {
    }

    public void logic() {
        System.out.println("싱글톤 객체 로직 호출");
    }
}
```

```java
public class SingletonTest {
    @Test
    @DisplayName("스프링 없는 순수한 DI 컨테이너")
    void pureContainer() {
        AppConfig appConfig = new AppConfig();
        //1. 조회: 호출할 땜 마다 객체를 생성
        MemberService memberService1 = appConfig.memberService();

        //2. 조회: 호출할 때 마다 객체를 생성
        MemberService memberService2 = appConfig.memberService();

        // 참조값이 다른것을 확인
        assertThat(memberService1).isNotSameAs(memberService2);
    }

    @Test
    @DisplayName("싱글톤 패턴을 적용한 객체 사용")
    void singletonServiceTest() {
        SingletonService singletonService1 = SingletonService.getInstance();
        SingletonService singletonService2 = SingletonService.getInstance();

        assertThat(singletonService1).isSameAs(singletonService2);
        // same == 대상의 주소를 비교(같은 객체를 참조하고 있는지 확인)
        // equal == 대상의 내용을 비교
    }
}
```

1. static 영역에 객체를 딱 1개만 생성해 둔다.
2. public 으로 열어서 객체 인스턴스가 필요하면, static메서드를 통해서만 조회하도록 허용한다.
3. 생성자를 private 으로 선언하여 외부에서 생성하지 못하게 한다.  
   -> 좋은 설계는 컴파일 오류만으로 오류를 모두 잡을수 있도록 해야 한다.

> isSameAs vs isEqualTo
> `isSameAs`는 참조하는 주소값이 같은지 비교하며, `isEqualTo`는 참조 대상의 내용이 같은지 확인한다.

**번외**

- LazyHolder를 이용한 Singleton 패턴
    - 클래스 초기화 단계에서부터 객체를 생성하여 메모리를 낭비하지 않고, 최초 호출되었을때로 객체 초기화를 미룬다.

```java
public class SingletonService {
    private SingletonService() {
    }

    ;

    private static class LazyHolder {
        static final SingletonService SINGLETON_SERVICE = new SingletonService();
    }

    public static SingletonService getInstance() {
        return LazyHolder.SINGLETON_SERVICE;
    }
}
```

### 싱글톤 패턴의 문제점

- 싱글톤 패턴 구현을 위한 코드가 많이들어감
- 의존관계상 구체클래스에 의존하여 DIP를 위반한다.
- 구체 클래스 의존으로 인해 OCP원칙을 위반할 가능성이 높다.
- 유연성이 떨어진다
- 안티패턴으로 불리기도 함

### 싱글톤 컨테이너

- 스프링 컨테이너는 앞서 언급한 싱글톤 패턴의 문제점을 해결하면서, 객체 인스턴스를 싱글톤으로 관리한다.
- 이렇게 싱글톤 객체를 생성하고 관리하는 기능을 싱글톤 레지스트리라 한다.

```java
public class SingletonTest {

    @Test
    @DisplayName("스프링 컨테이너와 싱글톤")
    void springContainer() {
        ApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
        //1. 조회: 호출할 땜 마다 객체를 생성
        MemberService memberService1 = ac.getBean("memberService", MemberService.class);

        //2. 조회: 호출할 때 마다 객체를 생성
        MemberService memberService2 = ac.getBean("memberService", MemberService.class);

        // 참조값이 다른것을 확인
        assertThat(memberService1).isSameAs(memberService2);
    }
}
```

- 스프링 컨테이너 덕분에 고객의 요청이 올 때 마다 객체를 생성하는 것이 아니라, 이미 만들어진 객체를 공유해서 효율적으로 재사용할 수 있다.

### [중요!]싱글톤 방식의 주의점

- 싱글톤 방식은 여러 클라이언트가 하나의 같은 객체 인스턴스를 공유하기 때문에 싱글톤 객체는 상태를 유지(Stateful)하게 설계해선 안된다.
- 반드시 무상태(Stateless)로 설계해야 한다.
    - 특정 클라이언트에 의존적인 필드가 있으면 안된다.
    - 가급적 읽기만 가능해야한다
    - 필드 대신 공유되지 않는 지역변수, 파라미터, ThreadLocal등을 사용해야 한다
- 스프링 빈의 필드에 공유값을 설정하면 큰 장애가 발생할 수 있다.

다음과 같이 `price`라는 공유 필드를 가지는 스프링 컨테이너가 있고, 해당 서비스에 대해 사용자 A와 사용자 B가 연달아 주문을하는 과정을 가정한다.

```java
package hello.core.singleton;

public class StatefulService {
    private int price; // 상태를 유지하는 필드

    public void order(String name, int price) {
        System.out.println("name = " + name + " price = " + price);
        this.price = price;
    }

    public int getPrice() {
        return price;
    }
}

```

```java
class StatefulServiceTest {
    @Test
    void statefulServiceSingleton() {
        ApplicationContext ac = new AnnotationConfigApplicationContext(TestConfig.class);
        StatefulService statefulService1 = ac.getBean(StatefulService.class);
        StatefulService statefulService2 = ac.getBean(StatefulService.class);

        // ThreadA : 사용자A 10000원 주문
        statefulService1.order("userA", 10000);
        // ThreadB : 사용자B 20000원 주문
        statefulService2.order("userB", 20000);

        // ThreadA : 사용자A 주문금액 조회
        Assertions.assertThat(statefulService1.getPrice()).isNotEqualTo(10000);
    }

    static class TestConfig {
        @Bean
        public StatefulService statefulService() {
            return new StatefulService();
        }
    }
}
```

- 사용자 A가 10000원을 주문하고, 해당 주문 가격이 공유 필드인 `price`에 저장된다.
- 사용자 B가 20000원을 주문하고, 해당 주문 가격이 공유 필드인 `price`에 저장된다.
- 사용자 A에 대한 주문금액을 확인하고자 `statefulService1.getPrice()`를 호출하였을때 주문금액은 10000원이 아닌 20000원이 조회된다.
- 실무에서 종종 발생하는 멀티스레드 관련 문제로, 이러한 공유필드는 매우 조심해야하며, 스프링 빈은 항상 Stateless로 설계해야한다.

**Stateless설계**

```java
package hello.core.singleton;

public class StatefulService {

    public int order(String name, int price) {
        System.out.println("name = " + name + " price = " + price);
        this.price = price;
        return price;
    }
}

```

```java
class StatefulServiceTest {
    @Test
    void statefulServiceSingleton() {
        ApplicationContext ac = new AnnotationConfigApplicationContext(TestConfig.class);
        StatefulService statefulService1 = ac.getBean(StatefulService.class);
        StatefulService statefulService2 = ac.getBean(StatefulService.class);

        // ThreadA : 사용자A 10000원 주문
        int priceA = statefulService1.order("userA", 10000);
        // ThreadB : 사용자B 20000원 주문
        int priceB = statefulService2.order("userB", 20000);

        // ThreadA : 사용자A 주문금액 조회
        Assertions.assertThat(priceA).isEqualTo(10000);
    }

    static class TestConfig {
        @Bean
        public StatefulService statefulService() {
            return new StatefulService();
        }
    }
}
```

### @Configuration과 싱글톤

```java
public class ConfigurationSingletonTest {
    @Test
    void configurationDeep() {
        ApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
        AppConfig bean = ac.getBean(AppConfig.class);

        System.out.println("bean = " + bean.getClass());
    }
}
```

**결과**

```
bean = class hello.core.AppConfig$$EnhancerBySpringCGLIB$$151abfc
```

- 상기 테스트의 결과로 AppConfig클래스가 호출되는 것이 아니라, `CGLIB`라는 단어가 붙은 클래스가 호출되는것을 확인할 수 있다.
- 스프링은 CGLIB이라는 **바이트코드 조작** 라이브러리를 사용해서 Appconfig클래스를 상속받은 임의의 다른 클래스를 만들고, 해당 클래스를 스프링 빈으로 등록한다.
- 이 임의의 다른 클래스가 싱글톤이 되도록 보장한다.
- `AppConfig$$EnhancerBySpringCGLIB$$`는 `AppConfig`의 자식 타입이므로 `AppConfig`로 조회 가능하다.

즉 `@Configuration`어노테이션은 바이트 코드 조작을 통해 해당 클래스 내의 스프링 빈들의 싱글톤을 보장한다.  
만약, `@Configuration`을 붙이지 않으면, 스프링 빈으로 등록되긴 하지만, 싱글톤을 보장하지 않는다.

따라서 스프링 설정 정보는 항상 `@Configuration`을 사용하여 싱글톤을 보장하면 된다.

## Section 6 : 컴포넌트 스캔

### 컴포넌트 스캔과 의존관계 자동 주입

- 스프링 빈이 많아지면 일일이 등록하기 귀찮아지고, 설정정보가 커져 누락하는 문제가 발생하게 된다.
- 스프링은 설정 정보 없이도 자동으로 스프링 빈을 등록하는 컴포넌트 스캔이라는 기능을 제공한다.
- 또한 의존관계를 자동으로 주입하는 `@Autowired`라는 기능을 제공한다.

컴포넌트 스캔은 `@Component`애노테이션이 붙은 클래스를 스캔해서 스프링 빈으로 등록한다.

- `@Configuration`애노테이션의 소스코드 또한 `@Component`이 붙어 있기 때문에 컴포넌트 스캔의 대상이 된다.
- 이때 스프링 빈의 기본 이름은 클래스 명을 사용하되, 맨 앞글자만 소문자를 사용한다.
- 생성자에 파라미터가 많더라도 알아서 주입한다.

### 컴포넌트 스캔의 탐색 위치, 스캔 대상

```java
package hello.core;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(
        basePackages = "hello.core",
        basePackageClasses = AutoAppConfig.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Configuration.class)
)
public class AutoAppConfig {

}
```

- `basePackages` : 탐색할 패키지의 시작 위치 지정. 해당 패키지와 해당 패키지의 하위 패키지를 탐색
- `basePackageClasses` : 지정한 클래스의 패키지를 탐색 시작 위치로 지정
- 만약 지정하지 않는다면, `@ComponentScan`이 붙은 설정 정보 클래스가 기준이 됨

> **권장하는 방법**  
> 관례상 패키지 위치를 지정하지 않고, 설정 정보 클래스의 위치를 프로젝트 최상단에 둔다.  
> 최근 스프링 부트도 이 방법을 기본으로 제공한다.  
> 프로젝트 메인 설정정보는 프로젝트를 대표하는 정보기 때문에 시작 루트 위치에 두는것이 좋다.
>
> 참고로 스프링 부트의 대표 시작 정보인 `@SpringBootApplication`은 프로젝트 시작 루트 위치에 두는것이 관례이다.  
> -> `@SpringBootApplication`에는 `@ComponentScan`이 포함되어 있다.

다음의 애노테이션은 컴포넌트 스캔의 대상이다

- `@Componet`
- `@Controller`
- `@Serivce`
- `@Repository`
- `@Configuration`

> 애노테이션이 특정 애노테이션을 들고있는것은 자바가 지원하는 기능이 아니라 스프링이 지원하는 기능이다.

### 스캔 필터

애노테이션을 통한 스캔 대상 제외

**포함 애노테이션**

```java
package hello.core.scan.filter;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyIncludeComponent {

}
```

```java
package hello.core.scan.filter;

@MyIncludeComponent
public class BeanA {

}
```

**제외 애노테이션**

```java
package hello.core.scan.filter;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyExcludeComponent {

}
```

```java
package hello.core.scan.filter;

@MyExcludeComponent
public class BeanB {
}
```

**컴포넌트 스캔 테스트**

```java
public class ComponentFilterAppConfigTest {

    @Test
    void filterScan() {
        ApplicationContext ac = new AnnotationConfigApplicationContext(ComponentFilterAppConfig.class);
        BeanA beanA = ac.getBean("beanA", BeanA.class);
        Assertions.assertThat(beanA).isNotNull();


        assertThrows(
                NoSuchBeanDefinitionException.class,
                () -> ac.getBean("beanB", BeanB.class)
        );
    }

    @Configuration
    @ComponentScan(
            includeFilters = @Filter(type = FilterType.ANNOTATION, classes = MyIncludeComponent.class),
            excludeFilters = @Filter(type = FilterType.ANNOTATION, classes = MyExcludeComponent.class)
    )
    static class ComponentFilterAppConfig {

    }
}
```

### 중복 등록

1. 자동 빈 등록 vs 자동 빈 등록

`ConflictingBeanDefinitionException`예외 발생

2. 수동 빈 등록 vs 자동 빈 등록

- 수동 빈 등록이란 `@ComponentScan`애노테이션이 붙은 클래스 내에 `@Bean`애노테이션으로 정의한것을 말함
- 이경우에 수동 빈 등록이 우선권을 가진다(수동 빈이 자동빈을 오버라이딩 함)

수동 빈 등록시 남는 로그

```text
Overriding bean definition for bean `memoryMemberRepository` with a different
definition: replacing
```

최근 스프링 부트에서는 수동 빈 등록과 자동 빈 등록이 충돌나면 오류가 발생하도록 기본값을 바꾸었다.

## Section 7 : 의존관계 자동 주입

### 다양한 의존관계 주입 방법

#### 생성자 주입

- 생성자를 통해서 의존 관계를 주입받는 방식
    - 생성자 호출 시점에 1번만 호출되는것이 보장된다.
    - **불변, 필수**의존관계에 사용
    - 제약을 명확히 하는 개발 습관이 중요하다.

```java
public class OrderServiceImpl implements OrderService {
    private final MemberRepository memberRepository;
    private final DiscountPolicy discountPolicy;

    @Autowired
    public OrderServiceImpl(MemberRepository memberRepository, DiscountPolicy discountPolicy) {
        this.memberRepository = memberRepository;
        this.discountPolicy = discountPolicy;
    }
    // ...
}
```

**[중요!]** - 스프링 빈의경우 생성자가 1개만 있으면, `@Autowired`가 없어도 자동으로 의존관계를 주입한다.

다음의 코드는 앞선 코드와 동일하게 동작한다.

```java
public class OrderServiceImpl implements OrderService {
    private final MemberRepository memberRepository;
    private final DiscountPolicy discountPolicy;

    public OrderServiceImpl(MemberRepository memberRepository, DiscountPolicy discountPolicy) {
        this.memberRepository = memberRepository;
        this.discountPolicy = discountPolicy;
    }
    // ...
}
```

#### 수정자 주입

- 수정자 메서드를 통해 의존관계를 주입하는 방법
    - **선택, 변경**가능성이 있는 의존관계에 사용

```java

@Component
public class OrderServiceImpl implements OrderService {
    private MemberRepository memberRepository;
    private DiscountPolicy discountPolicy;

    @Autowired
    void setDiscountPolicy(DiscountPolicy discountPolicy) {
        this.discountPolicy = discountPolicy;
    }

    @Autowired
    public void setMemberRepository(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Autowired
    public OrderServiceImpl(MemberRepository memberRepository, DiscountPolicy discountPolicy) {
        this.memberRepository = memberRepository;
        this.discountPolicy = discountPolicy;
    }
    // ...
}
```

- 생성자 주입과 수정자 주입이 같이 있으면, 생성자 주입이 먼저 발생.
- `@Autowired`는 주입할 대상이 없으면 오류가 발생한다.
- 주입 대상이 없어도 동작하게 하려면 `@Autowired(required = false)`로 지정하면 된다.

### 필드 주입

- 스프링 컨테이너가 아닌 순수 자바코드로 테스트할 방법이 없어 권장하지 않는 방법
- 애플리케이션 실제 코드와 관계없는 테스트 코드나, `@Configuration`에서 특별한 용도로만 사용

```java

@Component
public class OrderServiceImpl implements OrderService {
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private DiscountPolicy discountPolicy;
    // ...
}
```

#### 일반 메서드 주입

- 한번에 여러필드를 주입할 수 있다
- 일반적으로 잘 사용하지 않는다

```java

@Component
public class OrderServiceImpl implements OrderService {
    private MemberRepository memberRepository;
    private DiscountPolicy discountPolicy;

    @Autowired
    public void init(MemberRepository memberRepository, DiscountPolicy discountPolicy) {
        this.memberRepository = memberRepository;
        this.discountPolicy = discountPolicy;
    }
}
```

> 자동의존관계 주입은 스프링 컨테이너가 관리하는 스프링 빈이어야만 동작한다.

### 옵션 처리

```java
public class AutowiredTest {
    @Test
    void AutowiredOption() {
        ApplicationContext ac = new AnnotationConfigApplicationContext(TestBean.class);
    }

    static class TestBean {
        @Autowired(required = false)
        public void setNoBean1(Member noBean1) {
            System.out.println("noBean1 = " + noBean1);
        }

        @Autowired
        public void setNoBean2(@Nullable Member noBean2) {
            System.out.println("noBean2 = " + noBean2);
        }

        @Autowired
        public void setNoBean3(Optional<Member> noBean3) {
            System.out.println("noBean3 = " + noBean3);
        }
    }
}
```

**출력 결과**

```text
noBean2 = null
noBean3 = Optional.empty
```

> Member는 스프링 빈이 아니다.

1. `setNobean1`은 의존관계가 없으므로 `@Autowired(required = false)`에 의해 호출 자체가 안된다.
2. `@Nullabe`과 `Optional`은 스프링 전반에 걸쳐 지원된다.

### 생성자 주입을 사용해야 하는 이유

**불변**

- 대부분의 의존관계 주입은 한번일어나면 종료시점까지 의존 관계를 변경할 일이 없다.
- 생성자 주입은 객체 생성시 1번만 호출되므로 불변하게 설계할 수 있다.

**누락**

- 프레임 워크 없이 순수 자바 코드를 단위 테스트 하는 경우
- 수정자 주입을 사용하는 경우 반드시 수정자를 호출해 주어야한다. 즉 코드를 열람하여 확인해봐야 한다.
- 반면 생성자 주입을 사용하는 경우 어떤 의존 관계가 필요한지 테스트 코드 수준에서 파악 가능하다.
- 개발자가 필요한 의존관계를 누락한 경우 `final`키워드로 인해 어떤 의존관계를 누락했는지 컴파일 시점에 파악가능하다.

> 컴파일 오류는 세상에서 가장 빠르고 좋은 오류다!

### 롬복과 최신 트랜드

**기존 코드**

```java

@Component
public class OrderServiceImpl implements OrderService {
    private final MemberRepository memberRepository;
    private final DiscountPolicy discountPolicy;

    @Autowired
    public OrderServiceImpl(MemberRepository memberRepository, DiscountPolicy discountPolicy) {
        this.memberRepository = memberRepository;
        this.discountPolicy = discountPolicy;
    }
}
```

**생성자가 1개만 있으면, `@Autowired`를 생략할 수 있다.**

```java

@Component
public class OrderServiceImpl implements OrderService {
    private final MemberRepository memberRepository;
    private final DiscountPolicy discountPolicy;

    public OrderServiceImpl(MemberRepository memberRepository, DiscountPolicy discountPolicy) {
        this.memberRepository = memberRepository;
        this.discountPolicy = discountPolicy;
    }
}
```

**롬복 사용**

```java

@Component
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final MemberRepository memberRepository;
    private final DiscountPolicy discountPolicy;
}
```

롬복 라이브러리의 `@RequiredArgsConstructor`기능은 final 이 붙은 필드를 모아 생성자를 자동으로 만들어 준다.

> 최근에는 생성자를 1개두고 `@Autowired`를 생략하는 방법을 주로 사용한다.  
> Lombok 라이브러리를 함께 사용하면 기능은 다 제공하면서 코드를 깔끔하게 사용할 수 있다.

### 조회할 빈이 2개 이상인 경우

```java

@Component
public class FixDiscountPolicy implements DiscountPolicy {
    //...
}
```

```java

@Component
public class RateDiscountPolicy implements DiscountPolicy {
    //...
}
```

```java

@Component
public class OrderServiceImpl implements OrderService {
    private final MemberRepository memberRepository;
    private final DiscountPolicy discountPolicy;

    @Autowired
    public OrderServiceImpl(MemberRepository memberRepository, DiscountPolicy discountPolicy) {
        this.memberRepository = memberRepository;
        this.discountPolicy = discountPolicy;
    }
    //...
}
```

`@Autowired`는 타입으로 조회한다. 앞선 코드는 `ac.getBean(DiscountPolicy.class)`와 동일하게 동작하게 되는데
`DiscountPolicy`타입을 상속하는 클래스는 `FixDiscountPolicy`, `RateDiscountPolicy`두가지로 `NoUniqueBeanDefinitionException`이 발생하게 된다.

이러한 경우에 해결방법은 다음과 같다

- @Autowired 필드명 매치
- @Qualifier -> @Qualifier 끼리 매칭 -> 빈 이름 매칭
- @Primary 사용

#### @Autowired 필드명 매칭

**기존 코드**

```text
@Autowired
private DiscountPolicy discountPolicy
```

**필드명 빈 이름으로 변경**

```text
@Autowired
private DiscountPolicy rateDiscountPolicy
```

`@Autowired`는 타입 매칭을 시도하고, 결과에 여러 빈이 존재하면 필드명으로 매칭을 시도한다.  
필드명이 `rateDiscountPolicy`이므로 해당 이름을 가진 빈을 매칭한다.

#### @Qualifier 사용 (추가 구분자)

```text
@Component
@Qualifier("mainDiscountPolicy")
public class RateDiscountPolicy implements DiscountPolicy{
```

```text
public OrderServiceImpl(MemberRepository memberRepository, @Qualifier("mainDiscountPolicy") DiscountPolicy discountPolicy) {
        this.memberRepository = memberRepository;
        this.discountPolicy = discountPolicy;
    }
```

`@Qualifier("mainDiscountPolicy")`를 찾지 못할 경우, mainDiscountPolicy라는 이름의 스프링 빈을 추가로 찾는다.  
그렇더라도 `@Qualifier`는 `@Qualifier`를 찾는 용도로만 사용하는것이 좋다.

#### @Primary 사용 (우선 순위)

여러 스프링 빈이 매칭되면, `@Primary`애노테이션이 지정된 스프링 빈이 우선권을 가진다.

메인 DB, 보조 DB와 같이 운용할때 유용하게 사용할 수 있다.

#### @Qualifier vs @Primary

**@Primary, @Qualifier 활용**
코드에서 자주 사용하는 메인 데이터베이스의 커넥션을 획득하는 스프링 빈이 있고, 코드에서 특별한 기능으로 가끔 사용하는 서브 데이터베이스의 커넥션을 획득하는 스프링 빈이 있다고 생각해보자. 메인 데이터베이스의
커넥션을 획득하는 스프링 빈은 `@Primary` 를 적용해서 조회하는 곳에서 `@Qualifier`
지정 없이 편리하게 조회하고, 서브 데이터베이스 커넥션 빈을 획득할 때는 @Qualifier 를 지정해서 명시적으로 획득 하는 방식으로 사용하면 코드를 깔끔하게 유지할 수 있다. 물론 이때 메인 데이터베이스의 스프링
빈을 등록할 때 `@Qualifier` 를 지정해주는 것은 상관없다.

**우선순위**
`@Primary` 는 기본값 처럼 동작하는 것이고, `@Qualifier` 는 매우 상세하게 동작한다. 이런 경우 어떤 것이 우선권을 가져갈까? 스프링은 자동보다는 수동이, 넒은 범위의 선택권 보다는 좁은 범위의
선택권이 우선 순위가 높다. 따라서 여기서도 `@Qualifier` 가 우선권이 높다.

### 애노테이션 직접 만들기

`@Qualifier("mainDiscountPolicy")`의 mainDiscountPolicy와 같은 문자는 컴파일시점에서 타입 체크가 되지 않는다.

이때 새로운 애노테이션을 조합하여 사용할 수 있다.

```java

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Qualifier("mainDiscountPolicy")
public @interface MainDiscountPolicy {

}
```

**기존 코드**

```text
@Component
@Primary
public class RateDiscountPolicy implements DiscountPolicy{
```

```text
    public OrderServiceImpl(MemberRepository memberRepository, DiscountPolicy discountPolicy) {
        this.memberRepository = memberRepository;
        this.discountPolicy = discountPolicy;
    }
```

**수정 코드**

```text
@MainDiscountPolicy
public class RateDiscountPolicy implements DiscountPolicy{

```

```text
    public OrderServiceImpl(MemberRepository memberRepository, @MainDiscountPolicy DiscountPolicy discountPolicy) {
        this.memberRepository = memberRepository;
        this.discountPolicy = discountPolicy;
    }
```

하지만 스프링이 제공하는 기능을 뚜렷한 목적 없이 무분별하게 재정의 하는것은 유지보수에 혼란만 가중할 수 있다.

### 조회한 빈이 모두 필요할 때

```java
public class AllBeanTest {
    @Test
    void findAllBean() {
        ApplicationContext ac = new AnnotationConfigApplicationContext(AutoAppConfig.class, DiscountService.class);

        DiscountService discountService = ac.getBean(DiscountService.class);
        Member member = new Member(1L, "userA", Grade.VIP);
        int discountPrice = discountService.discount(member, 10000, "fixDiscountPolicy");

        assertThat(discountService).isInstanceOf(DiscountService.class);
        assertThat(discountPrice).isEqualTo(1000);

        int rateDiscountPrice = discountService.discount(member, 20000, "rateDiscountPolicy");

        assertThat(rateDiscountPrice).isEqualTo(2000);
    }

    static class DiscountService {
        private final Map<String, DiscountPolicy> policyMap;
        private final List<DiscountPolicy> policies;

        public DiscountService(Map<String, DiscountPolicy> policyMap, List<DiscountPolicy> policies) {
            this.policyMap = policyMap;
            this.policies = policies;
            System.out.println("policyMap = " + policyMap);
            System.out.println("policies = " + policies);
        }

        public int discount(Member member, int price, String discountCode) {
            DiscountPolicy discountPolicy = policyMap.get(discountCode);
            return discountPolicy.discount(member, price);
        }
    }
}
```

### 스프링 빈 자동, 수동의 올바른 실무 운영 기준

설정 정보를 기반으로 구성하는 부분과 실제 동작하는 부분을 명확하게 나누는것이 이상적이지만, 빈이 많아 설정 정보가 커지면 설정 정보관리에 부담이 커진다. 그리고 자동으로 빈 등록을 사용하더라도 OCP, DIP를
지킬 수 있다.

**수동빈 등록은 언제 사용하는것이 좋은가**

**업무 로직 빈**

- 웹을 지원하는 컨트롤러, 핵심 비즈니스 로직이 있는 서비스, 데이터 계층의 로직을 처리하는 리포지토리
- 비즈니스 요구사항을 개발할 때 추가되거나 변경된다

**기술 지원 빈**

- 기술적인 문제나 공통관심사(AOP)처리시 주로 사용
- DB 연결, 공통 로그 처리처럼 업무 로직을 지원하기 위한 하부기술 및 공통 기술

애플리케이션에 광범위하게 영향을 미치는 기술 지원 객체는 수동 빈으로 등록하여 설정 정보에 바로 나타나게 하는것이 유지보수하기 좋다.

**수동으로 빈을 등록할 경우**

```java

@Configuration
public class DiscountPolicyConfig {
    @Bean
    public DiscountPolicy rateDiscountPolicy() {
        return new RateDsicountPolicy();
    }

    @Bean
    public DiscountPolicy fixDiscountPolicy() {
        return new FixDiscountPolicy();
    }
}
```

**자동으로 빈을 등록할 경우**

다음과 같이 특정 패키지에 같이 묶어둔다.

- discount(폴더)
    - DiscountPolicy.java
    - FixDiscountPolicy.java
    - RateDiscountPolicy.java

-> 어떤 방식을 사용하든 결과적으로 한눈에 파악할 수 있어야 한다.

스프링 부트가 자동으로 등록하는 빈을 제외한 직접 기술 지원 객체를 스프링 빈으로 등록한다면, 수동으로 등록해서 명확하게 드러내는것이 좋다.

## Section 8 : 빈 생명주기 콜백

### 빈 생명주기 콜백 시작

```java
public class NetworkClient {
    private String url;

    public NetworkClient() {
        System.out.println("생성자 호출, url = " + url);
        connect();
        call("초기화 연결 메세지");
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void connect() {
        System.out.println("connect: " + url);
    }

    public void call(String msg) {
        System.out.println("call: " + url + " msg : " + msg);
    }

    public void disconnect() {
        System.out.println("close: " + url);
    }
}
```

```java
public class BeanLifeCycleTest {
    @Test
    public void lifeCycleTest() {
        ConfigurableApplicationContext ac = new AnnotationConfigApplicationContext(LifeCycleConfig.class);
        NetworkClient client = ac.getBean(NetworkClient.class);
        ac.close();
    }

    @Configuration
    static class LifeCycleConfig {
        @Bean
        public NetworkClient networkClient() {
            NetworkClient networkClient = new NetworkClient();
            networkClient.setUrl("http://hello-spring.dev");
            return networkClient;
        }
    }
}
```

**결과**

```text
url = null
connect: null
call: null msg : 초기화 연결 메세지
```

- 생성자 부분을 보면 url정보 없이 connect가 호출된다.
- 스프링 빈은 객체 생성 후 의존관계 주입이 다 끝난 다음에야 필요한 데이터를 사용할 수 있는 준비가 완료된다.

**스프링 빈의 이벤트 라이프 사이클**
스프링 컨테이너 생성 -> 스프링 빈 생성 -> 의존관계 주입 -> 초기화 콜백 -> 사용 -> 소멸전 콜백 -> 스프링 종료

- 초기화 콜백 : 빈이 생성되고, 빈의 의존관계 주입이 완료된 후 호출
- 소멸전 콜백 : 빈이 소멸도기 직전 호출

초기화 콜백을 통해 의존관계 주입이 완료된 시점을 알 수 있다.

> 객체의 생성과 초기화의 분리
> 생성자는 필수 파라미터만 받고 메모리를 할당해 객체를 생성하는 책임을 가진다.  
> 반면 초기화는 이러한 생성값을 활용하여 외부 커넥션을 연결하는 등의 무거운 동작을 수행한다.  
> 따라서 유지보수의 관점에서 생성자에서 이러한 무거운 초기화 작업을 하기보다 생성 부분과 초기화 부분을 명확하게 나누는것이 좋다.

스프링은 크게 3가지 방법으로 빈 생명주기 콜백을 지원한다

- 인터페이스(InitializingBean, DisposableBean)
- 설정 정보에 초기화 메서드, 종료 메서드 지정
- @PostConstruct, @PreDestroy

### 인터페이스

**초기화, 소멸 인터페이스**

```text
public class NetworkClient implements InitializingBean, DisposableBean {
    ...
    @Override
    public void afterPropertiesSet() throws Exception {
        connect();
        call("초기화 연결 메시지");
    }

    @Override
    public void destroy() throws Exception {
        disconnect();
    }
}
```

```text
생성자 호출, url = null
connect: http://hello-spring.dev
call: http://hello-spring.dev msg : 초기화 연결 메시지
21:30:40.792 [Test worker] DEBUG org.springframework.context.annotation.AnnotationConfigApplicationContext - Closing org.springframework.context.annotation.AnnotationConfigApplicationContext@64b0598, started on Sun Jan 16 21:30:40 KST 2022
close: http://hello-spring.dev
```

- 스프링 전용 인터페이스로 코드가 스프링 전용 인터페이스에 의존한다
- 초기화, 소멸 메서드의 이름을 변경할 수 없다.
- 외부 라이브러리에 적용할 수 없다.

> 매우 초창기에 나온 방법으로 지금은 거의 사용하지 않는다.

### 설정 정보 : 빈 등록 초기화, 소멸 메서드

```text
    public void init() throws Exception {
        connect();
        call("초기화 연결 메시지");
    }

    public void close() throws Exception {
        disconnect();
    }
```

```java
public class BeanLifeCycleTest {
    @Test
    public void lifeCycleTest() {
        ConfigurableApplicationContext ac = new AnnotationConfigApplicationContext(LifeCycleConfig.class);
        NetworkClient client = ac.getBean(NetworkClient.class);
        ac.close();
    }

    @Configuration
    static class LifeCycleConfig {
        @Bean(initMethod = "init", destroyMethod = "close")
        public NetworkClient networkClient() {
            NetworkClient networkClient = new NetworkClient();
            networkClient.setUrl("http://hello-spring.dev");
            return networkClient;
        }
    }
}
```

- 메서드 이름을 자유롭게 줄 수 있다.
- 스프링 빈이 스프링 코드에 의존하지 않는다
- 코드를 고칠수 없는 외부 라이브러리에도 사용할 수 있다.

`destroyMethod`속성의 특별한 기능
- 라이브러리는 대부분 소멸 메서드로 `close`, `shutdown`을 이름으로 사용한다.
- 이러한 이름의 메서드가 존재하면 메서드를 자동으로 호출한다. 
- 따라서 직접 스프링 빈으로 등록하면 종료 메서드는 따로 적어주지 않아도 잘 작동한다.
- 사용하지 않고 싶으면 `destroyMethod = ""`로 사용하면 된다.

### 애노테이션 @PosConstruct, @PreDestroy

```text
    @PostConstruct
    public void init() throws Exception {
        connect();
        call("초기화 연결 메시지");
    }

    @PreDestroy
    public void close() throws Exception {
        disconnect();
    }
```

- 최신 스프링에서 가장 권장하는 방법이다
- `javax.annotation.PostConstruct` -> 스프링에 종속적인 기술이 아니라 자바 표준이다
- 컴포넌트 스캔과 잘 어울린다.
- 외부 라이브러리에는 적용하지 못한다. 따라서 외부라이브러리에 적용해야 하는경우 앞선 빈의 속성 기능을 사용하면 된다.

**정리**
- PostConstruct, PreDestroy애노테이션을 사용하되, 외부라이브러리를 초기화하거나 종료할경우 Bean의 initMethod, destroyMethod를 사용하자

## 빈 스코프

스코프는 빈이 존재할 수 있는 범위를 뜻함.

스프링의 스코프
- 싱글톤 : 스프링의 시작과 종료까지 유지되는 가장 넓은 범위의 스코프
- 프로토 타입 : 빈의 생성과 의존관계 주입까지만 관여하고 더이상 관리하지 않는 스코프
- 웹 관련 스코프
  - request : 웹 요청이 들어오고 나갈때 까지 유지되는 스코프
  - session : 웹 세션이 생성되고 종료될 때 까지 유지되는 스코프
  - application : 웹의 서블릿 컨텍스와 같은 범위로 유지되는 스코프


### 프로토타입 스코프 빈

**싱글톤 스코프**
```java
public class SingletonTest {
    @Test
    void singletonBeanFind() {
        AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(SingletonBean.class);

        SingletonBean singletonBean1 = ac.getBean(SingletonBean.class);
        SingletonBean singletonBean2 = ac.getBean(SingletonBean.class);

        Assertions.assertThat(singletonBean1).isSameAs(singletonBean2);
        ac.close();
    }

    @Scope("singleton")
    static class SingletonBean {
        @PostConstruct
        public void init() {
            System.out.println("SingletonBean.init");
        }

        @PreDestroy
        public void destroy() {
            System.out.println("SingletonBean.destroy");
        }
    }
}
```

```text
SingletonBean.init
22:45:58.345 [Test worker] DEBUG org.springframework.context.annotation.AnnotationConfigApplicationContext - Closing org.springframework.context.annotation.AnnotationConfigApplicationContext@5167f57d, started on Mon Jan 17 22:45:58 KST 2022
SingletonBean.destroy
```

- 스프링 컨테이너 생성시점에 초기화 메서드 실행
- 스프링 컨테이너 종료시점에 종료 메서드 실행

**프로토타입 스코프**

```java
public class ProtoTypeTest {
    @Test
    void prototypeBeanFind() {
        AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(PrototypeBean.class);

        System.out.println("find prototypeBean1");
        PrototypeBean prototypeBean1 = ac.getBean(PrototypeBean.class);
      
        System.out.println("find prototypeBean1");
        PrototypeBean prototypeBean2 = ac.getBean(PrototypeBean.class);

        Assertions.assertThat(prototypeBean1).isNotSameAs(prototypeBean2);

        ac.close();
    }

    @Scope("prototype")
    static class PrototypeBean {
        @PostConstruct
        public void init() {
            System.out.println("PrototypeBean.init");
        }

        @PreDestroy
        public void destroy() {
            System.out.println("PrototypeBean.destroy");
        }
    }
}
```

```text
find prototypeBean1
PrototypeBean.init
find prototypeBean1
PrototypeBean.init
22:47:54.521 [Test worker] DEBUG org.springframework.context.annotation.AnnotationConfigApplicationContext - Closing org.springframework.context.annotation.AnnotationConfigApplicationContext@5167f57d, started on Mon Jan 17 22:47:54 KST 2022
```

- 스프링 빈 조회 시점에 생성 및 초기화 메서드 실행
- 프로토 타입 빈을 조회 할때마다 완전히 다른 스프링 빈이 생성된다.
- 스프링 컨테이너가 관리하지 않기때문에 스프링 컨테이너가 종료되더라도 종료 메서드가 실행되지 않는것을 확인할 수 있다.

**정리**
- 프로토타입 빈은 요청마다 새로 생성된다
- 스프링 컨테이너는 프로토타입 빈의 생성과 의존관계 주입, 초기화 까지만 관여한다.
- 종료 메서드가 호출되지 않는다.
- 따라서 프로토 타입 빈은 빈을 조회한 클라이언트가 관리해야 한다.(종료 메서드를 직접 호출해주어야 한다)

### 프로토타입 스코프 빈 - 싱글톤 빈과 함께 사용시 문제점

```java
public class SingletonWithPrototypeTest {
    @Test
    void prototypeFind() {
        AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(PrototypeBean.class);
        PrototypeBean prototypeBean1 = ac.getBean(PrototypeBean.class);
        prototypeBean1.addCount();
        assertThat(prototypeBean1.getCount()).isEqualTo(1);

        PrototypeBean prototypeBean2 = ac.getBean(PrototypeBean.class);
        prototypeBean2.addCount();
        assertThat(prototypeBean2.getCount()).isEqualTo(1);
    }

    @Test
    void singletonClientUsePrototype() {
        AnnotationConfigApplicationContext ac =
                new AnnotationConfigApplicationContext(ClientBean.class, PrototypeBean.class);


        ClientBean clientBean1 = ac.getBean(ClientBean.class);
        int count1 = clientBean1.logic();
        assertThat(count1).isEqualTo(1);

        ClientBean clientBean2 = ac.getBean(ClientBean.class);
        int count2 = clientBean2.logic();
        assertThat(count2).isEqualTo(2); // 싱글톤 빈과 함께 유지되기 때문에 프로토 타입 빈은 새로 생성되지 않는다. 

    }

    @Scope("singleton")
    @RequiredArgsConstructor
    static class ClientBean {
        private final PrototypeBean prototypeBean; // 싱글톤 빈의 생성시점에 의존 관계 주입이 발생한다.

        public int logic() {
            prototypeBean.addCount();
            return prototypeBean.getCount();
        }
    }

    @Scope("prototype")
    static class PrototypeBean {
        private int count = 0;

        public void addCount() {
            count++;
        }

        public int getCount() {
            return count;
        }

        @PostConstruct
        public void init() {
            System.out.println("PrototypeBean.init " + this);

        }

        @PreDestroy
        public void destroy() {
            System.out.println("PrototypeBean.destroy");
        }
    }
}
```

스프링은 일반적으로 싱글톤 빈을 사용하므로 싱글톤 빈이 프로토 타입 빈을 사용한다. 싱글톤 빈은 생성 시점에만 의존관계 주입을 받기때문에 프로토타입빈이 
생성되긴 하지만, 싱글톤 빈과 함께 계속 유지된다.

> 여러 싱글톤 빈에서 프로토타입 빈을 주입받으면 생성시점에 각각 새로 생성된 프로토타입 빈을 주입받는다. 따라서 두 싱글톤 빈의 프로토타입 빈은 다르다.

### Provider를 통한 문제 해결

```text
    @Scope("singleton")
    static class ClientBean {
        @Autowired
        private ObjectProvider<PrototypeBean> prototypeBeanProvider;

        public int logic() {
            PrototypeBean prototypeBean = prototypeBeanProvider.getObject();
            prototypeBean.addCount();
            return prototypeBean.ge~~tCount();
        }~~
    }
```

- `ObjectProvider.getObject()`를 통해 새로운 프로토 타입 빈이 생성된다.
- `ObjectProvider`는 스프링 컨테이너를 통해 해당 빈을 찾아서 반환한다.(대신 조회해준다)
- `ObjectProvider`는 의존성을 조회하는 DL(dependency lookup) 정도의 기능만을 제공한다.

### JSR-330 Provider

자바 표준 컨테이너 Provider
- `javax.inject:javax.inject:1`라이브러리를 gradle에 추가해야한다. 

```text
    @Scope("singleton")
    static class ClientBean {
        @Autowired
        private Provider<PrototypeBean> prototypeBeanProvider;

        public int logic() {
            PrototypeBean prototypeBean = prototypeBeanProvider.get();
            prototypeBean.addCount();
            return prototypeBean.getCount();
        }
    }
```

- `get()`메서드 하나로 기능이 매우 단순하다
- 별도 라이브러리가 필요하다
- 스프링이 아닌 다른 라이브러리에서도 사용가능

> 실무에서는 싱글톤 빈으로 대부분의 문제를 해결할 수 있기 때문에 프로토타입 빈을 직접적으로 사용하는 일은 매우 드물다.
> 스프링을 사용하다 보면 자바 표준과 스프링이 제공하는 기능이 겹칠때가 많다. 대부분 스프링이 더 다양하고 편리한 기능을 제공해 주기 때문에,
> 특별히 다른 컨테이너를 사용할 일이 없으면 스프링이 제공하는 기능을 사용하면 된다. 스프링은 사실상의 기술 표준이다. (De facto)

### 웹 스코프

- request : HTTP요청마다 별도의 빈 인스턴스가 생성, 요청이 들어오고 나갈때 까지 유지
- session : HTTP Session과 동일한 생명주기
- application : 서블릿 컨텍스트와 동일한 생명주기
- websocket : 웹소켓과 동일한 생명주기

> **웹 환경**  
> 스프링 부트는 웹라이브러리가 없으면 `AnnotationConfigApplicationContext`를 기반으로, 
> 있으면`AnnotationConfigServletWebServerApplicationContext`를 기반으로 구동

### Request 스코프

