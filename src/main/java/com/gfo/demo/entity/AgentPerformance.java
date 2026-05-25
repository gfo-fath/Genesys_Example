package com.gfo.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 座席绩效实体类
 * 用于记录和分析座席的工作绩效数据
 */
@Entity
@Table(name = "agent_performance")
public class AgentPerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String agentId;

    @Column(nullable = false)
    private String agentName;

    @Column(nullable = false)
    private LocalDate performanceDate;

    @Column(name = "login_time")
    private LocalDateTime loginTime;

    @Column(name = "logout_time")
    private LocalDateTime logoutTime;

    @Column
    private Long loggedInTime; // 登录时长（秒）

    @Column(name = "calls_handled")
    private Integer callsHandled = 0; // 处理呼叫数

    @Column(name = "calls_inbound")
    private Integer callsInbound = 0; // 呼入呼叫数

    @Column(name = "calls_outbound")
    private Integer callsOutbound = 0; // 呼出呼叫数

    @Column(name = "total_talk_time")
    private Long totalTalkTime = 0L; // 总通话时长（秒）

    @Column(name = "total_hold_time")
    private Long totalHoldTime = 0L; // 总保持时长（秒）

    @Column(name = "avg_call_duration")
    private Double avgCallDuration = 0.0; // 平均通话时长（秒）

    @Column(name = "avg_handle_time")
    private Double avgHandleTime = 0.0; // 平均处理时长（秒）

    @Column(name = "first_call_resolution")
    private Double firstCallResolution = 0.0; // 首次呼叫解决率（%）

    @Column(name = "customer_satisfaction")
    private Double customerSatisfaction = 0.0; // 客户满意度（1-5分）

    @Column(name = "calls_abandoned")
    private Integer callsAbandoned = 0; // 放弃呼叫数

    @Column(name = "calls_transferred")
    private Integer callsTransferred = 0; // 转移呼叫数

    @Column(name = "calls_conferenced")
    private Integer callsConferenced = 0; // 会议呼叫数

    @Column(name = "wrap_up_time")
    private Long wrapUpTime = 0L; // 整理时长（秒）

    @Column(name = "available_time")
    private Long availableTime = 0L; // 空闲时长（秒）

    @Column(name = "busy_time")
    private Long busyTime = 0L; // 忙碌时长（秒）

    @Column(name = "not_ready_time")
    private Long notReadyTime = 0L; // 示忙时长（秒）

    @Column(name = "break_time")
    private Long breakTime = 0L; // 休息时长（秒）

    @Column(name = "training_time")
    private Long trainingTime = 0L; // 培训时长（秒）

    @Column(name = "coaching_time")
    private Long coachingTime = 0L; // 辅导时长（秒）

    @Column(columnDefinition = "TEXT")
    private String notes; // 备注

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 构造函数
    public AgentPerformance() {
        this.createdAt = LocalDateTime.now();
        this.performanceDate = LocalDate.now();
    }

    public AgentPerformance(String agentId, String agentName) {
        this();
        this.agentId = agentId;
        this.agentName = agentName;
    }

    // 计算方法
    public void calculateMetrics() {
        // 计算平均通话时长
        if (callsHandled > 0) {
            this.avgCallDuration = totalTalkTime.doubleValue() / callsHandled;
        }

        // 计算平均处理时长
        if (callsHandled > 0) {
            long totalHandleTime = totalTalkTime + totalHoldTime + wrapUpTime;
            this.avgHandleTime = (double) (totalHandleTime / callsHandled);
        }

        // 计算登录时长
        if (loginTime != null && logoutTime != null) {
            this.loggedInTime = java.time.Duration.between(loginTime, logoutTime).getSeconds();
        }
    }

    // 增加呼叫统计
    public void incrementCallsHandled(String direction) {
        this.callsHandled++;
        if ("INBOUND".equals(direction)) {
            this.callsInbound++;
        } else if ("OUTBOUND".equals(direction)) {
            this.callsOutbound++;
        }
    }

    // 增加通话时长
    public void addTalkTime(long seconds) {
        this.totalTalkTime += seconds;
    }

    // 增加保持时长
    public void addHoldTime(long seconds) {
        this.totalHoldTime += seconds;
    }

    // 增加转移呼叫数
    public void incrementCallsTransferred() {
        this.callsTransferred++;
    }

    // 增加会议呼叫数
    public void incrementCallsConferenced() {
        this.callsConferenced++;
    }

    // 增加放弃呼叫数
    public void incrementCallsAbandoned() {
        this.callsAbandoned++;
    }

    // 更新客户满意度
    public void updateCustomerSatisfaction(double score) {
        // 简单的移动平均计算
        if (customerSatisfaction == 0.0) {
            this.customerSatisfaction = score;
        } else {
            this.customerSatisfaction = (this.customerSatisfaction + score) / 2;
        }
    }

    // 生命周期回调
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        calculateMetrics();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }

    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }

    public LocalDate getPerformanceDate() { return performanceDate; }
    public void setPerformanceDate(LocalDate performanceDate) { this.performanceDate = performanceDate; }

    public LocalDateTime getLoginTime() { return loginTime; }
    public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }

    public LocalDateTime getLogoutTime() { return logoutTime; }
    public void setLogoutTime(LocalDateTime logoutTime) { this.logoutTime = logoutTime; }

    public Long getLoggedInTime() { return loggedInTime; }
    public void setLoggedInTime(Long loggedInTime) { this.loggedInTime = loggedInTime; }

    public Integer getCallsHandled() { return callsHandled; }
    public void setCallsHandled(Integer callsHandled) { this.callsHandled = callsHandled; }

    public Integer getCallsInbound() { return callsInbound; }
    public void setCallsInbound(Integer callsInbound) { this.callsInbound = callsInbound; }

    public Integer getCallsOutbound() { return callsOutbound; }
    public void setCallsOutbound(Integer callsOutbound) { this.callsOutbound = callsOutbound; }

    public Long getTotalTalkTime() { return totalTalkTime; }
    public void setTotalTalkTime(Long totalTalkTime) { this.totalTalkTime = totalTalkTime; }

    public Long getTotalHoldTime() { return totalHoldTime; }
    public void setTotalHoldTime(Long totalHoldTime) { this.totalHoldTime = totalHoldTime; }

    public Double getAvgCallDuration() { return avgCallDuration; }
    public void setAvgCallDuration(Double avgCallDuration) { this.avgCallDuration = avgCallDuration; }

    public Double getAvgHandleTime() { return avgHandleTime; }
    public void setAvgHandleTime(Double avgHandleTime) { this.avgHandleTime = avgHandleTime; }

    public Double getFirstCallResolution() { return firstCallResolution; }
    public void setFirstCallResolution(Double firstCallResolution) { this.firstCallResolution = firstCallResolution; }

    public Double getCustomerSatisfaction() { return customerSatisfaction; }
    public void setCustomerSatisfaction(Double customerSatisfaction) { this.customerSatisfaction = customerSatisfaction; }

    public Integer getCallsAbandoned() { return callsAbandoned; }
    public void setCallsAbandoned(Integer callsAbandoned) { this.callsAbandoned = callsAbandoned; }

    public Integer getCallsTransferred() { return callsTransferred; }
    public void setCallsTransferred(Integer callsTransferred) { this.callsTransferred = callsTransferred; }

    public Integer getCallsConferenced() { return callsConferenced; }
    public void setCallsConferenced(Integer callsConferenced) { this.callsConferenced = callsConferenced; }

    public Long getWrapUpTime() { return wrapUpTime; }
    public void setWrapUpTime(Long wrapUpTime) { this.wrapUpTime = wrapUpTime; }

    public Long getAvailableTime() { return availableTime; }
    public void setAvailableTime(Long availableTime) { this.availableTime = availableTime; }

    public Long getBusyTime() { return busyTime; }
    public void setBusyTime(Long busyTime) { this.busyTime = busyTime; }

    public Long getNotReadyTime() { return notReadyTime; }
    public void setNotReadyTime(Long notReadyTime) { this.notReadyTime = notReadyTime; }

    public Long getBreakTime() { return breakTime; }
    public void setBreakTime(Long breakTime) { this.breakTime = breakTime; }

    public Long getTrainingTime() { return trainingTime; }
    public void setTrainingTime(Long trainingTime) { this.trainingTime = trainingTime; }

    public Long getCoachingTime() { return coachingTime; }
    public void setCoachingTime(Long coachingTime) { this.coachingTime = coachingTime; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "AgentPerformance{" +
                "id=" + id +
                ", agentId='" + agentId + '\'' +
                ", agentName='" + agentName + '\'' +
                ", performanceDate=" + performanceDate +
                ", callsHandled=" + callsHandled +
                ", avgCallDuration=" + avgCallDuration +
                ", customerSatisfaction=" + customerSatisfaction +
                '}';
    }
}