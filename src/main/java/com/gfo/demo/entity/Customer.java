package com.gfo.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 客户实体类
 * 对应数据库中的customers表，用于存储客户基本信息
 */
@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 主键ID，自增

    @Column(nullable = false, unique = true)
    private String customerId; // 客户编号，唯一标识

    @Column(nullable = false)
    private String name; // 客户姓名

    @Column(nullable = false, unique = true)
    private String email; // 客户邮箱，唯一

    private String phone; // 客户电话
    private String address; // 客户地址

    @Column(name = "created_date")
    private LocalDateTime createdDate; // 创建时间

    @Column(name = "last_contact_date")
    private LocalDateTime lastContactDate; // 最后联系时间

    @Enumerated(EnumType.STRING)
    private CustomerStatus status; // 客户状态

    // 构造函数
    public Customer() {
        this.createdDate = LocalDateTime.now();
        this.status = CustomerStatus.ACTIVE;
    }

    // 带参数的构造函数
    public Customer(String customerId, String name, String email) {
        this();
        this.customerId = customerId;
        this.name = name;
        this.email = email;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getLastContactDate() { return lastContactDate; }
    public void setLastContactDate(LocalDateTime lastContactDate) { this.lastContactDate = lastContactDate; }

    public CustomerStatus getStatus() { return status; }
    public void setStatus(CustomerStatus status) { this.status = status; }
}

