package com.gfo.demo.service;

import com.gfo.demo.entity.Customer;
import com.gfo.demo.repository.CustomerRepository;
import com.gfo.demo.genesys.KazimirLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private KazimirLogger kazimirLogger;

    @Cacheable(value = "customers", key = "#customerId")
    public Optional<Customer> findByCustomerId(String customerId) {
        long startTime = System.currentTimeMillis();

        Optional<Customer> cachedCustomer = Optional.ofNullable((Customer) cacheService.getCachedCustomer(customerId));
        if (cachedCustomer.isPresent()) {
            kazimirLogger.logPerformanceMetrics("Customer Cache Hit", System.currentTimeMillis() - startTime, 1);
            return cachedCustomer;
        }

        Optional<Customer> customer = customerRepository.findByCustomerId(customerId);
        customer.ifPresent(c -> cacheService.cacheCustomer(customerId, c));

        kazimirLogger.logPerformanceMetrics("Customer Database Query", System.currentTimeMillis() - startTime, customer.isPresent() ? 1 : 0);
        return customer;
    }

    @CachePut(value = "customers", key = "#result.customerId")
    public Customer saveCustomer(Customer customer) {
        long startTime = System.currentTimeMillis();

        Customer savedCustomer = customerRepository.save(customer);
        cacheService.cacheCustomer(savedCustomer.getCustomerId(), savedCustomer);

        kazimirLogger.logInteraction(savedCustomer.getCustomerId(), "INFO", "Customer saved successfully");
        kazimirLogger.logPerformanceMetrics("Customer Save", System.currentTimeMillis() - startTime, 1);

        return savedCustomer;
    }

    @CacheEvict(value = "customers", key = "#customerId")
    public void deleteCustomer(String customerId) {
        long startTime = System.currentTimeMillis();

        customerRepository.findByCustomerId(customerId).ifPresent(customer -> {
            customerRepository.delete(customer);
            cacheService.evictCustomerCache(customerId);

            kazimirLogger.logInteraction(customerId, "INFO", "Customer deleted successfully");
        });

        kazimirLogger.logPerformanceMetrics("Customer Delete", System.currentTimeMillis() - startTime, 1);
    }

    public List<Customer> findCustomersInactiveSince(LocalDateTime date) {
        long startTime = System.currentTimeMillis();

        List<Customer> customers = customerRepository.findInactiveCustomers(date);

        kazimirLogger.logPerformanceMetrics("Inactive Customers Query", System.currentTimeMillis() - startTime, customers.size());
        return customers;
    }

    public List<Customer> searchCustomers(String name, String email) {
        long startTime = System.currentTimeMillis();

        List<Customer> customers = customerRepository.findByNameOrEmail(name, email);

        kazimirLogger.logPerformanceMetrics("Customer Search", System.currentTimeMillis() - startTime, customers.size());
        return customers;
    }

    public Long getCustomerCountByStatus(String status) {
        try {
            com.gfo.demo.entity.CustomerStatus statusEnum = com.gfo.demo.entity.CustomerStatus.valueOf(status.toUpperCase());
            return customerRepository.countByStatus(statusEnum);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: " + status + ". Valid values are: ACTIVE, INACTIVE, SUSPENDED");
        }
    }
}