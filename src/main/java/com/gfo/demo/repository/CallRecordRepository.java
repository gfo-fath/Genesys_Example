package com.gfo.demo.repository;

import com.gfo.demo.entity.CallRecord;
import com.gfo.demo.entity.CallStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 呼叫记录数据访问接口
 * 提供呼叫记录的数据库操作功能
 */
@Repository
public interface CallRecordRepository extends JpaRepository<CallRecord, Long> {

    /**
     * 根据呼叫ID查找呼叫记录
     */
    Optional<CallRecord> findByCallId(String callId);

    /**
     * 根据Genesys呼叫ID查找呼叫记录
     */
    Optional<CallRecord> findByGenesysCallId(String genesysCallId);

    /**
     * 根据SIP呼叫ID查找呼叫记录
     */
    Optional<CallRecord> findBySipCallId(String sipCallId);

    /**
     * 根据座席ID查找呼叫记录
     */
    List<CallRecord> findByAgentId(String agentId);

    /**
     * 根据座席ID和日期范围查找呼叫记录
     */
    List<CallRecord> findByAgentIdAndStartTimeBetween(String agentId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据客户ID查找呼叫记录
     */
    List<CallRecord> findByCustomerId(Long customerId);

    /**
     * 根据电话号码查找呼叫记录
     */
    List<CallRecord> findByPhoneNumber(String phoneNumber);

    /**
     * 根据呼叫方向查找呼叫记录
     */
    List<CallRecord> findByDirection(String direction);

    /**
     * 根据呼叫状态查找呼叫记录
     */
    List<CallRecord> findByStatus(CallStatus status);

    /**
     * 根据日期范围查找呼叫记录
     */
    List<CallRecord> findByStartTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 分页查询呼叫记录
     */
    Page<CallRecord> findAll(Pageable pageable);

    /**
     * 根据座席ID分页查询呼叫记录
     */
    Page<CallRecord> findByAgentId(String agentId, Pageable pageable);

    /**
     * 根据日期范围和座席ID分页查询呼叫记录
     */
    Page<CallRecord> findByAgentIdAndStartTimeBetween(String agentId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 根据队列名称查找呼叫记录
     */
    List<CallRecord> findByQueueName(String queueName);

    /**
     * 统计指定日期范围内的呼叫数量
     */
    @Query("SELECT COUNT(cr) FROM CallRecord cr WHERE cr.startTime BETWEEN :startTime AND :endTime")
    Long countCallsByDateRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定座席在日期范围内的呼叫数量
     */
    @Query("SELECT COUNT(cr) FROM CallRecord cr WHERE cr.agentId = :agentId AND cr.startTime BETWEEN :startTime AND :endTime")
    Long countCallsByAgentAndDateRange(@Param("agentId") String agentId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定座席在日期范围内的通话总时长
     */
    @Query("SELECT SUM(cr.talkTime) FROM CallRecord cr WHERE cr.agentId = :agentId AND cr.startTime BETWEEN :startTime AND :endTime")
    Long sumTalkTimeByAgentAndDateRange(@Param("agentId") String agentId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定座席在日期范围内的保持总时长
     */
    @Query("SELECT SUM(cr.holdTime) FROM CallRecord cr WHERE cr.agentId = :agentId AND cr.startTime BETWEEN :startTime AND :endTime")
    Long sumHoldTimeByAgentAndDateRange(@Param("agentId") String agentId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 获取指定座席的平均通话时长
     */
    @Query("SELECT AVG(cr.talkTime) FROM CallRecord cr WHERE cr.agentId = :agentId AND cr.talkTime > 0")
    Double findAvgTalkTimeByAgent(@Param("agentId") String agentId);

    /**
     * 获取指定座席的平均处理时长
     */
    @Query("SELECT AVG(cr.talkTime + cr.holdTime) FROM CallRecord cr WHERE cr.agentId = :agentId")
    Double findAvgHandleTimeByAgent(@Param("agentId") String agentId);

    /**
     * 根据呼叫方向统计呼叫数量
     */
    @Query("SELECT cr.direction, COUNT(cr) FROM CallRecord cr WHERE cr.startTime BETWEEN :startTime AND :endTime GROUP BY cr.direction")
    List<Object[]> countCallsByDirection(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 根据呼叫状态统计呼叫数量
     */
    @Query("SELECT cr.status, COUNT(cr) FROM CallRecord cr WHERE cr.startTime BETWEEN :startTime AND :endTime GROUP BY cr.status")
    List<Object[]> countCallsByStatus(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 获取最近N条呼叫记录
     */
    List<CallRecord> findTop10ByOrderByStartTimeDesc();

    /**
     * 获取指定座席的最近N条呼叫记录
     */
    List<CallRecord> findTop10ByAgentIdOrderByStartTimeDesc(String agentId);

    /**
     * 删除指定日期之前的呼叫记录
     */
    Long deleteByStartTimeBefore(LocalDateTime beforeDate);

    /**
     * 根据客户电话号码模糊查询呼叫记录
     */
    List<CallRecord> findByPhoneNumberContaining(String phoneNumber);

    /**
     * 复杂查询：多条件搜索呼叫记录
     */
    @Query("SELECT cr FROM CallRecord cr WHERE " +
           "(:agentId IS NULL OR cr.agentId = :agentId) AND " +
           "(:phoneNumber IS NULL OR cr.phoneNumber LIKE %:phoneNumber%) AND " +
           "(:direction IS NULL OR cr.direction = :direction) AND " +
           "(:status IS NULL OR cr.status = :status) AND " +
           "(:startTime IS NULL OR cr.startTime >= :startTime) AND " +
           "(:endTime IS NULL OR cr.startTime <= :endTime)")
    Page<CallRecord> searchCallRecords(
            @Param("agentId") String agentId,
            @Param("phoneNumber") String phoneNumber,
            @Param("direction") String direction,
            @Param("status") CallStatus status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable
    );
}