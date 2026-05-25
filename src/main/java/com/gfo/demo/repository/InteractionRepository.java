package com.gfo.demo.repository;

import com.gfo.demo.entity.Interaction;
import com.gfo.demo.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, Long> {

    Optional<Interaction> findByInteractionId(String interactionId);

    List<Interaction> findByCustomer(Customer customer);

    List<Interaction> findByType(String type);

    List<Interaction> findByStatus(String status);

    List<Interaction> findByAgentId(String agentId);

    @Query("SELECT i FROM Interaction i WHERE i.customer.id = :customerId AND i.startTime BETWEEN :startDate AND :endDate")
    List<Interaction> findByCustomerAndDateRange(@Param("customerId") Long customerId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    @Query("SELECT i FROM Interaction i WHERE i.queueName = :queueName AND i.status = 'ACTIVE'")
    List<Interaction> findActiveInteractionsByQueue(@Param("queueName") String queueName);

    @Query("SELECT COUNT(i) FROM Interaction i WHERE i.agentId = :agentId AND i.startTime >= :startDate")
    Long countInteractionsByAgentToday(@Param("agentId") String agentId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT AVG(i.duration) FROM Interaction i WHERE i.type = :type AND i.startTime >= :startDate")
    Double averageDurationByType(@Param("type") String type, @Param("startDate") LocalDateTime startDate);

    @Query(value = "SELECT queue_name, COUNT(*) as interaction_count FROM interactions WHERE status = 'ACTIVE' GROUP BY queue_name", nativeQuery = true)
    List<Object[]> countActiveInteractionsByQueue();
}