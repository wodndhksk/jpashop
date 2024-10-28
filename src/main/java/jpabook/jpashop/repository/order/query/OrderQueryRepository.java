package jpabook.jpashop.repository.order.query;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    public List<OrderQueryDto> findOrderQueryDtos() {
        List<OrderQueryDto> result = findOrders();

        result.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
            o.setOrderItems(orderItems);
        });
        return result;
    }

    /**
     * @메서드설명: order를 조회 후 orderItems를 따로 한꺼번에 조회한다음 조회한 orderItems 를 map 으로 변환하여
     * 반복문을 통해 order의 orderItems에 세팅하는 메서드
     *
     * @로직설명: 쿼리 최적화 (orderItems 를 조회 후 Map 의 key value 형태로 변환 -> 이때 key 값은 orderId 값)
     * 이후에 반복문을 돌며 setter 를 통해 각 Order(result) 의 orderItems 에 값을 주입(세팅)한다.
     * 따라서 최종 실행되는 쿼리 횟수는 2번이다.
     */
    public List<OrderQueryDto> findAllByDto_optimization() {

        // Order 조회
        List<OrderQueryDto> result = findOrders();

        // 조회된 각 Order 의 orderId 를 List 로 추출
        List<Long> orderIds = toOrderId(result);

        // orderItems 를 한번에 조회 후 Map 으로 변환
        Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(orderIds);

        // orderId 를 Map 의 key 값으로 찾아서 각각의 orderItems 에 세팅한다(setter).
        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

        return result;
    }

    private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
        // orderId로 orderItems 한꺼번에 조회 ( 조건문 where in (?, ? ...) 사용 )
        List<OrderItemQueryDto> orderItems = em.createQuery(
                        "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, i.price, oi.count) " +
                                " from OrderItem oi" +
                                " join oi.item i" +
                                " where oi.order.id in :orderIds", OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();

        // Map 으로 변환 (key 값을 o.getOrderId() 로 value 값을 o 로)
        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
                .collect(Collectors.groupingBy(o -> o.getOrderId()));
        return orderItemMap;
    }

    private static List<Long> toOrderId(List<OrderQueryDto> result) {
        List<Long> orderIds = result.stream()
                .map(o -> o.getOrderId())
                .collect(Collectors.toList());
        return orderIds;
    }

    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, i.price, oi.count) " +
                        " from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id = :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    private List<OrderQueryDto> findOrders() {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address) " +
                        " from Order o " +
                        " join o.member m" +
                        " join o.delivery d", OrderQueryDto.class
        ).getResultList();
    }

    public List<OrderFlatDto> findAllByDto_flat() {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderFlatDto(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count)" +
                        " from Order o" +
                        " join o.member m " +
                        " join o.delivery d " +
                        " join o.orderItems oi" +
                        " join oi.item i", OrderFlatDto.class)
                .getResultList();

    }
}
