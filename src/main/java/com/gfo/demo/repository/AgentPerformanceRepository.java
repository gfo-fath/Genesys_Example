package com.gfo.demo.repository;

import com.gfo.demo.entity.AgentPerformance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 座席绩效数据访问接口
 * 提供座席绩效数据的数据库操作功能
 */
@Repository
public interface AgentPerformanceRepository extends JpaRepository<AgentPerformance, Long> {

    /**
     * 根据座席ID查找绩效记录
     */
    List<AgentPerformance> findByAgentId(String agentId);

    /**
     * 根据座席ID和日期查找绩效记录
     */
    Optional<AgentPerformance> findByAgentIdAndPerformanceDate(String agentId, LocalDate performanceDate);

    /**
     * 根据日期查找所有座席的绩效记录
     */
    List<AgentPerformance> findByPerformanceDate(LocalDate performanceDate);

    /**
     * 根据日期范围查找绩效记录
     */
    List<AgentPerformance> findByPerformanceDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * 分页查询绩效记录
     */
    Page<AgentPerformance> findAll(Pageable pageable);

    /**
     * 根据座席ID分页查询绩效记录
     */
    Page<AgentPerformance> findByAgentId(String agentId, Pageable pageable);

    /**
     * 根据座席ID和日期范围分页查询绩效记录
     */
    Page<AgentPerformance> findByAgentIdAndPerformanceDateBetween(String agentId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * 根据日期分页查询绩效记录
     */
    Page<AgentPerformance> findByPerformanceDate(LocalDate performanceDate, Pageable pageable);

    /**
     * 获取最近N天的绩效记录
     */
    List<AgentPerformance> findTop7ByOrderByPerformanceDateDesc();

    /**
     * 获取指定座席的最近N条绩效记录
     */
    List<AgentPerformance> findTop10ByAgentIdOrderByPerformanceDateDesc(String agentId);

    /**
     * 统计指定日期范围内所有座席的总呼叫处理数
     */
    @Query("SELECT SUM(ap.callsHandled) FROM AgentPerformance ap WHERE ap.performanceDate BETWEEN :startDate AND :endDate")
    Long sumCallsHandledByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 统计指定日期范围内所有座席的总通话时长
     */
    @Query("SELECT SUM(ap.totalTalkTime) FROM AgentPerformance ap WHERE ap.performanceDate BETWEEN :startDate AND :endDate")
    Long sumTotalTalkTimeByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 获取指定日期范围内所有座席的平均客户满意度
     */
    @Query("SELECT AVG(ap.customerSatisfaction) FROM AgentPerformance ap WHERE ap.performanceDate BETWEEN :startDate AND :endDate AND ap.customerSatisfaction > 0")
    Double avgCustomerSatisfactionByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 获取指定座席在日期范围内的平均客户满意度
     */
    @Query("SELECT AVG(ap.customerSatisfaction) FROM AgentPerformance ap WHERE ap.agentId = :agentId AND ap.performanceDate BETWEEN :startDate AND :endDate AND ap.customerSatisfaction > 0")
    Double avgCustomerSatisfactionByAgentAndDateRange(@Param("agentId") String agentId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 获取指定座席在日期范围内的平均通话时长
     */
    @Query("SELECT AVG(ap.avgCallDuration) FROM AgentPerformance ap WHERE ap.agentId = :agentId AND ap.performanceDate BETWEEN :startDate AND :endDate AND ap.avgCallDuration > 0")
    Double avgCallDurationByAgentAndDateRange(@Param("agentId") String agentId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 获取指定座席在日期范围内的平均处理时长
     */
    @Query("SELECT AVG(ap.avgHandleTime) FROM AgentPerformance ap WHERE ap.agentId = :agentId AND ap.performanceDate BETWEEN :startDate AND :endDate AND ap.avgHandleTime > 0")
    Double avgHandleTimeByAgentAndDateRange(@Param("agentId") String agentId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 根据座席ID分组统计呼叫处理数
     */
    @Query("SELECT ap.agentId, SUM(ap.callsHandled) FROM AgentPerformance ap WHERE ap.performanceDate BETWEEN :startDate AND :endDate GROUP BY ap.agentId")
    List<Object[]> sumCallsHandledByAgent(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 根据座席ID分组统计通话时长
     */
    @Query("SELECT ap.agentId, SUM(ap.totalTalkTime) FROM AgentPerformance ap WHERE ap.performanceDate BETWEEN :startDate AND :endDate GROUP BY ap.agentId")
    List<Object[]> sumTalkTimeByAgent(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 获取绩效最好的N个座席（按呼叫处理数）
     */
    @Query("SELECT ap FROM AgentPerformance ap WHERE ap.performanceDate BETWEEN :startDate AND :endDate ORDER BY ap.callsHandled DESC")
    List<AgentPerformance> findTopPerformersByCallsHandled(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);

    /**
     * 获取客户满意度最高的N个座席
     */
    @Query("SELECT ap FROM AgentPerformance ap WHERE ap.performanceDate BETWEEN :startDate AND :endDate AND ap.customerSatisfaction > 0 ORDER BY ap.customerSatisfaction DESC")
    List<AgentPerformance> findTopPerformersBySatisfaction(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);

    /**
     * 获取平均通话时长最短的N个座席
     */
    @Query("SELECT ap FROM AgentPerformance ap WHERE ap.performanceDate BETWEEN :startDate AND :endDate AND ap.avgCallDuration > 0 ORDER BY ap.avgCallDuration ASC")
    List<AgentPerformance> findTopPerformersByAvgCallDuration(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);

    /**
     * 复杂查询：多条件搜索绩效记录
     */
    @Query("SELECT ap FROM AgentPerformance ap WHERE " +
           "(:agentId IS NULL OR ap.agentId = :agentId) AND " +
           "(:startDate IS NULL OR ap.performanceDate >= :startDate) AND " +
           "(:endDate IS NULL OR ap.performanceDate <= :endDate) AND " +
           "(:minCallsHandled IS NULL OR ap.callsHandled >= :minCallsHandled) AND " +
           "(:minSatisfaction IS NULL OR ap.customerSatisfaction >= :minSatisfaction)")
    Page<AgentPerformance> searchAgentPerformance(
            @Param("agentId") String agentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("minCallsHandled") Integer minCallsHandled,
            @Param("minSatisfaction") Double minSatisfaction,
            Pageable pageable
    );

    /**
     * 删除指定日期之前的绩效记录
     */
    Long deleteByPerformanceDateBefore(LocalDate beforeDate);

    /**
     * 获取座席的工作时长统计
     */
    @Query("SELECT ap.agentId, SUM(ap.loggedInTime) FROM AgentPerformance ap WHERE ap.performanceDate BETWEEN :startDate AND :endDate GROUP BY ap.agentId")
    List<Object[]> sumLoggedInTimeByAgent(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 获取座席的状态时长统计
     */
    @Query("SELECT ap.agentId, " +
           "SUM(ap.availableTime), " +
           "SUM(ap.busyTime), " +
           "SUM(ap.notReadyTime), " +
           "SUM(ap.breakTime) " +
           "FROM AgentPerformance ap WHERE ap.performanceDate BETWEEN :startDate AND :endDate GROUP BY ap.agentId")
    List<Object[]> sumStatusTimeByAgent(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}