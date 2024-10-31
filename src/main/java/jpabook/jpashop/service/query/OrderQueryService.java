package jpabook.jpashop.service.query;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
@Transactional
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepository;

    public List<OrderQDto> ordersQueryV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderQDto> result = orders.stream()
                .map(o -> new OrderQDto(o))
                .collect(toList());

        return result;
    }
}
