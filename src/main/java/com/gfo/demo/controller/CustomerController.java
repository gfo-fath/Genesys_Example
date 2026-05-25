package com.gfo.demo.controller;

import com.gfo.demo.entity.Customer;
import com.gfo.demo.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 客户管理控制器
 * 提供客户信息的增删改查API接口
 */
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService; // 客户业务逻辑服务

    /**
     * 创建新客户
     * @param customer 客户信息
     * @return 创建成功的客户信息
     */
    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        Customer savedCustomer = customerService.saveCustomer(customer);
        return ResponseEntity.ok(savedCustomer);
    }

    /**
     * 根据客户ID获取客户信息
     * @param customerId 客户编号
     * @return 客户信息，如果不存在返回404
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<Customer> getCustomer(@PathVariable String customerId) {
        Optional<Customer> customer = customerService.findByCustomerId(customerId);
        return customer.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Customer>> searchCustomers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email) {
        List<Customer> customers = customerService.searchCustomers(name, email);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/inactive")
    public ResponseEntity<List<Customer>> getInactiveCustomers(
            @RequestParam(required = false) String since) {
        LocalDateTime dateTime = since != null ?
            LocalDateTime.parse(since) :
            LocalDateTime.now().minusDays(30);

        List<Customer> customers = customerService.findCustomersInactiveSince(dateTime);
        return ResponseEntity.ok(customers);
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable String customerId) {
        customerService.deleteCustomer(customerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/count")
    public ResponseEntity<Long> getCustomerCount(@RequestParam String status) {
        Long count = customerService.getCustomerCountByStatus(status);
        return ResponseEntity.ok(count);
    }
}