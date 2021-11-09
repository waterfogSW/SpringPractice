package hello.core;

import hello.core.member.*;
import hello.core.order.*;

public class OrderApp {
    public static void main(String[] args) {
        AppConfig appConfig = new AppConfig();
        MemberService memberService = appConfig.memberService();
        OrderService orderService = appConfig.orderService();

        Long memberId = 1L;
        Member member = new Member(memberId, "memberA", Grade.VIP);
        memberService.join(member);

        Member findMember = memberService.findMember(1L);
        Order order = orderService.createOrder(1L, "itemA", 10000);
        System.out.println("order = " + order);
    }
}
