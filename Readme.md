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

- 애플리케이션 **실행 시점(런타임)**에 외부에서 실제 구현 객체를 생성하고 클라이언트에 전달해서 클라이언트와 서버의 실제 의존관계가 연결되는것을 **의존관계 주입** 이라 한다.
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

