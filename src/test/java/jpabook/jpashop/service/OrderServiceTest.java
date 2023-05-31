package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class OrderServiceTest {

    @Autowired
    EntityManager em;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Test
    public void 상품주문() {
        // given
        Member member = createMember();

        String name = "시골 JPA";
        int originStockCount = 10;
        int bookPrice = 10000;

        Item book = createBook(name, originStockCount, bookPrice);

        // when
        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        // then
        Order order = orderRepository.findOne(orderId);

        assertEquals("상품 주문시 상태는 ORDER", OrderStatus.ORDER, order.getStatus());
        assertEquals("주문한 상품 종류가 정확해야한다.", 1, order.getOrderItems().size());
        assertEquals("주문 가격은 가격 * 수량이다", bookPrice * orderCount, order.getTotalPrice());
        assertEquals("주문 수량만큼 재고가 줄어야한다.", originStockCount - orderCount, book.getStockQuantity());

    }

    @Test(expected = NotEnoughStockException.class)
    public void 재고수량초과() {
        // given
        Member member = createMember();

        String name = "시골 JPA";
        int originStockCount = 10;
        int bookPrice = 10000;

        Item book = createBook(name, originStockCount, bookPrice);
        int orderCount = 11;

        // when
        orderService.order(member.getId(), book.getId(), orderCount);

        // then
        fail("재고수량 부족으로 예외가 발생해야한다.");
    }

    @Test
    public void 주문취소() {
        // given
        Member member = createMember();

        String name = "시골 JPA";
        int originStockCount = 10;
        int bookPrice = 10000;
        Item item = createBook(name, originStockCount, bookPrice);

        int orderCount = 2;

        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

        // when
        orderService.cancel(orderId);

        // then
        Order order = orderRepository.findOne(orderId);
        assertEquals("주문 취소시 상태는 CANCEL", OrderStatus.CANCEL, order.getStatus());
        assertEquals("재고 복구 검증", originStockCount, item.getStockQuantity());
    }

    private Book createBook(String name, int originStockCount, int bookPrice) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(bookPrice);
        book.setStockQuantity(originStockCount);
        em.persist(book);
        return book;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "어딘가", "123456"));
        em.persist(member);
        return member;
    }

}