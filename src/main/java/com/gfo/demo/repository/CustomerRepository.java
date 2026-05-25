package com.gfo.demo.repository;

import com.gfo.demo.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCustomerId(String customerId);

    Optional<Customer> findByEmail(String email);

    List<Customer> findByStatus(String status);

    @Query("SELECT c FROM Customer c WHERE c.name LIKE %:name% OR c.email LIKE %:email%")
    List<Customer> findByNameOrEmail(@Param("name") String name, @Param("email") String email);

    @Query("SELECT c FROM Customer c WHERE c.lastContactDate < :date")
    List<Customer> findInactiveCustomers(@Param("date") java.time.LocalDateTime date);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.status = :status")
    Long countByStatus(@Param("status") com.gfo.demo.entity.CustomerStatus status);

    @Query(value = "SELECT * FROM customers WHERE EXTRACT(MONTH FROM created_date) = :month AND EXTRACT(YEAR FROM created_date) = :year", nativeQuery = true)
    List<Customer> findCustomersByMonthAndYear(@Param("month") int month, @Param("year") int year);
}