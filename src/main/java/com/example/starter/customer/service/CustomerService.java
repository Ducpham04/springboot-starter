package com.example.starter.customer.service;

import com.example.starter.common.exception.AppException;
import com.example.starter.common.exception.ErrorCode;
import com.example.starter.common.paging.SpecificationBuilder;
import com.example.starter.common.response.PageResponse;
import com.example.starter.customer.dto.CustomerRequest;
import com.example.starter.customer.dto.CustomerResponse;
import com.example.starter.customer.entity.Customer;
import com.example.starter.customer.repository.CustomerRepository;
import com.example.starter.order.entity.SalesOrder;
import com.example.starter.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    public PageResponse<CustomerResponse> search(String keyword, Pageable pageable) {
        var spec = SpecificationBuilder.<Customer>create()
                .likeAny(keyword, "name", "phone", "email")
                .build();
        Page<Customer> page = customerRepository.findAll(spec, pageable);
        Map<Long, Stats> stats = stats(page.getContent().stream().map(Customer::getId).toList());
        return PageResponse.of(page, c -> toResponse(c, stats.getOrDefault(c.getId(), Stats.EMPTY), null));
    }

    public CustomerResponse getById(Long id) {
        Customer customer = findOrThrow(id);
        Stats stats = stats(List.of(id)).getOrDefault(id, Stats.EMPTY);
        var spec = SpecificationBuilder.<SalesOrder>create().equal("customer.id", id).build();
        List<CustomerResponse.RecentOrder> recent = orderRepository
                .findAll(spec, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))
                .map(o -> new CustomerResponse.RecentOrder(o.getId(), o.getCode(), o.getTotal(),
                        o.getStatus().name(), o.getCreatedAt()))
                .getContent();
        return toResponse(customer, stats, recent);
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        String phone = normalizePhone(request.phone());
        if (customerRepository.existsByPhone(phone)) {
            throw new AppException(ErrorCode.CUSTOMER_PHONE_EXISTS, phone);
        }
        Customer customer = new Customer();
        apply(customer, request, phone);
        return toResponse(customerRepository.save(customer), Stats.EMPTY, null);
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = findOrThrow(id);
        String phone = normalizePhone(request.phone());
        if (customerRepository.existsByPhoneAndIdNot(phone, id)) {
            throw new AppException(ErrorCode.CUSTOMER_PHONE_EXISTS, phone);
        }
        apply(customer, request, phone);
        return toResponse(customer, stats(List.of(id)).getOrDefault(id, Stats.EMPTY), null);
    }

    @Transactional
    public void delete(Long id) {
        Customer customer = findOrThrow(id);
        if (orderRepository.existsByCustomerId(id)) {
            throw new AppException(ErrorCode.CUSTOMER_IN_USE);
        }
        customerRepository.delete(customer);
    }

    /** +84912345678 -> 0912345678 so the same number is never stored twice. */
    static String normalizePhone(String phone) {
        String p = phone.trim();
        return p.startsWith("+84") ? "0" + p.substring(3) : p;
    }

    private Customer findOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND, id));
    }

    private static void apply(Customer c, CustomerRequest r, String phone) {
        c.setName(r.name().trim());
        c.setPhone(phone);
        c.setEmail(blankToNull(r.email()));
        c.setAddress(blankToNull(r.address()));
        c.setNote(blankToNull(r.note()));
    }

    private Map<Long, Stats> stats(List<Long> ids) {
        Map<Long, Stats> map = new HashMap<>();
        if (ids.isEmpty()) {
            return map;
        }
        for (Object[] row : orderRepository.customerStats(ids)) {
            map.put((Long) row[0], new Stats((Long) row[1], (BigDecimal) row[2], (Instant) row[3]));
        }
        return map;
    }

    private static CustomerResponse toResponse(Customer c, Stats s, List<CustomerResponse.RecentOrder> recent) {
        return new CustomerResponse(c.getId(), c.getName(), c.getPhone(), c.getEmail(), c.getAddress(), c.getNote(),
                s.count(), s.total(), s.last(), c.getCreatedAt(), recent);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private record Stats(long count, BigDecimal total, Instant last) {
        static final Stats EMPTY = new Stats(0, BigDecimal.ZERO, null);
    }
}
