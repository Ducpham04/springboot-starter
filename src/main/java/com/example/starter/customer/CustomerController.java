package com.example.starter.customer;

import com.example.starter.common.response.ApiResponse;
import com.example.starter.common.response.PageResponse;
import com.example.starter.customer.dto.CustomerRequest;
import com.example.starter.customer.dto.CustomerResponse;
import com.example.starter.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Customers")
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PreAuthorize("hasAuthority('CUSTOMER_READ')")
    @GetMapping
    public ApiResponse<PageResponse<CustomerResponse>> search(
            @RequestParam(required = false) String keyword,
            @ParameterObject @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(customerService.search(keyword, pageable));
    }

    @PreAuthorize("hasAuthority('CUSTOMER_READ')")
    @GetMapping("/{id}")
    public ApiResponse<CustomerResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(customerService.getById(id));
    }

    @PreAuthorize("hasAuthority('CUSTOMER_WRITE')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        return ApiResponse.ok(customerService.create(request));
    }

    @PreAuthorize("hasAuthority('CUSTOMER_WRITE')")
    @PutMapping("/{id}")
    public ApiResponse<CustomerResponse> update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        return ApiResponse.ok(customerService.update(id, request));
    }

    @PreAuthorize("hasAuthority('CUSTOMER_DELETE')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ApiResponse.ok(null);
    }
}
