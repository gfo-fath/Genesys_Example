package com.gfo.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 呼叫记录实体类
 * 用于存储详细的呼叫记录信息，包括呼叫时间、持续时间、结果等
 */
@Entity
@Table(name = "call_records")
public class CallRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String callId;

    @Column(nullable = false)
    private String genesysCallId;

    @Column(nullable = false)
    private String sipCallId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(nullable = false)
    private String agentId;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String direction; // INBOUND, OUTBOUND

    @Column(nullable = false)
    private String type; // VOICE, VIDEO, CHAT

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "answer_time")
    private LocalDateTime answerTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column
    private Long duration; // 持续时间（秒）

    @Column
    private Long talkTime; // 通话时间（秒）

    @Column
    private Long holdTime; // 保持时间（秒）

    @Enumerated(EnumType.STRING)
    private CallStatus status;

    @Column(columnDefinition = "TEXT")
    private String recordingUrl;

    @Column
    private String queueName;

    @Column
    private String ivrPath;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column
    private Integer satisfactionScore; // 满意度评分 1-5

    @Column
    private String dispositionCode; // 呼叫结果代码

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 构造函数
    public CallRecord() {
        this.createdAt = LocalDateTime.now();
        this.status = CallStatus.INITIATED;
    }

    public CallRecord(String callId, String genesysCallId, String sipCallId, String agentId,
                     String phoneNumber, String direction, String type) {
        this();
        this.callId = callId;
        this.genesysCallId = genesysCallId;
        this.sipCallId = sipCallId;
        this.agentId = agentId;
        this.phoneNumber = phoneNumber;
        this.direction = direction;
        this.type = type;
        this.startTime = LocalDateTime.now();
    }

    // 计算方法
    public void calculateDuration() {
        if (endTime != null && startTime != null) {
            this.duration = java.time.Duration.between(startTime, endTime).getSeconds();
        }
    }

    public void calculateTalkTime() {
        if (answerTime != null) {
            LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
            this.talkTime = java.time.Duration.between(answerTime, end).getSeconds();
        }
    }

    // 生命周期回调
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        calculateDuration();
        calculateTalkTime();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCallId() { return callId; }
    public void setCallId(String callId) { this.callId = callId; }

    public String getGenesysCallId() { return genesysCallId; }
    public void setGenesysCallId(String genesysCallId) { this.genesysCallId = genesysCallId; }

    public String getSipCallId() { return sipCallId; }
    public void setSipCallId(String sipCallId) { this.sipCallId = sipCallId; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getAnswerTime() { return answerTime; }
    public void setAnswerTime(LocalDateTime answerTime) { this.answerTime = answerTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Long getDuration() { return duration; }
    public void setDuration(Long duration) { this.duration = duration; }

    public Long getTalkTime() { return talkTime; }
    public void setTalkTime(Long talkTime) { this.talkTime = talkTime; }

    public Long getHoldTime() { return holdTime; }
    public void setHoldTime(Long holdTime) { this.holdTime = holdTime; }

    public CallStatus getStatus() { return status; }
    public void setStatus(CallStatus status) { this.status = status; }

    public String getRecordingUrl() { return recordingUrl; }
    public void setRecordingUrl(String recordingUrl) { this.recordingUrl = recordingUrl; }

    public String getQueueName() { return queueName; }
    public void setQueueName(String queueName) { this.queueName = queueName; }

    public String getIvrPath() { return ivrPath; }
    public void setIvrPath(String ivrPath) { this.ivrPath = ivrPath; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Integer getSatisfactionScore() { return satisfactionScore; }
    public void setSatisfactionScore(Integer satisfactionScore) { this.satisfactionScore = satisfactionScore; }

    public String getDispositionCode() { return dispositionCode; }
    public void setDispositionCode(String dispositionCode) { this.dispositionCode = dispositionCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "CallRecord{" +
                "id=" + id +
                ", callId='" + callId + '\'' +
                ", agentId='" + agentId + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", direction='" + direction + '\'' +
                ", status=" + status +
                ", duration=" + duration +
                '}';
    }
}

