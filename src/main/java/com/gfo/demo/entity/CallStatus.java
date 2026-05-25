package com.gfo.demo.entity;

/**
 * 呼叫状态枚举
 */
public enum CallStatus {
    INITIATED,     // 已发起
    DIALING,       // 拨号中
    RINGING,       // 响铃中
    ANSWERED,      // 已接听
    IN_PROGRESS,   // 进行中
    HELD,          // 保持中
    TRANSFERRED,   // 已转移
    CONFERENCED,   // 会议中
    COMPLETED,     // 已完成
    FAILED,        // 失败
    BUSY,          // 忙线
    NO_ANSWER,     // 无应答
    CANCELLED      // 已取消
}
