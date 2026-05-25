package com.gfo.demo.genesys;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket呼叫处理器
 * 提供实时呼叫管理功能，支持座席与系统间的实时通信
 * 用于实时推送呼叫状态、座席状态、队列信息等
 */
@Component
public class WebSocketCallHandler implements WebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketCallHandler.class);

    @Autowired
    private RealCallService realCallService;

    @Autowired
    private GenesysSDKService genesysSDKService;

    @Autowired
    private SIPPhoneService sipPhoneService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 存储连接的WebSocket会话
    private final Map<String, WebSocketSession> agentSessions = new ConcurrentHashMap<>();
    private final Map<String, WebSocketSession> adminSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        String userType = getUserTypeFromSession(session);

        logger.info("WebSocket连接建立 - 会话ID: {}, 用户类型: {}", sessionId, userType);

        if ("agent".equals(userType)) {
            agentSessions.put(sessionId, session);
        } else if ("admin".equals(userType)) {
            adminSessions.put(sessionId, session);
        }

        // 发送连接成功消息
        sendMessage(session, createResponse("CONNECTED", "WebSocket连接成功", null));
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String sessionId = session.getId();
        String payload = message.getPayload().toString();

        logger.debug("收到WebSocket消息 - 会话ID: {}, 消息: {}", sessionId, payload);

        try {
            // 解析消息
            Map<String, Object> request = objectMapper.readValue(payload, Map.class);
            String action = (String) request.get("action");
            Map<String, Object> data = (Map<String, Object>) request.get("data");

            // 根据action处理不同消息
            handleAction(session, action, data);

        } catch (Exception e) {
            logger.error("处理WebSocket消息失败", e);
            sendMessage(session, createResponse("ERROR", "消息处理失败", e.getMessage()));
        }
    }

    /**
     * 处理不同的WebSocket动作
     */
    private void handleAction(WebSocketSession session, String action, Map<String, Object> data) {
        switch (action) {
            case "LOGIN":
                handleAgentLogin(session, data);
                break;
            case "LOGOUT":
                handleAgentLogout(session, data);
                break;
            case "MAKE_CALL":
                handleMakeCall(session, data);
                break;
            case "ANSWER_CALL":
                handleAnswerCall(session, data);
                break;
            case "END_CALL":
                handleEndCall(session, data);
                break;
            case "HOLD_CALL":
                handleHoldCall(session, data);
                break;
            case "RESUME_CALL":
                handleResumeCall(session, data);
                break;
            case "TRANSFER_CALL":
                handleTransferCall(session, data);
                break;
            case "CONFERENCE_CALL":
                handleConferenceCall(session, data);
                break;
            case "MUTE_CALL":
                handleMuteCall(session, data);
                break;
            case "SEND_DTMF":
                handleSendDTMF(session, data);
                break;
            case "GET_CALL_STATUS":
                handleGetCallStatus(session, data);
                break;
            case "GET_AGENT_STATUS":
                handleGetAgentStatus(session, data);
                break;
            case "SET_AGENT_STATUS":
                handleSetAgentStatus(session, data);
                break;
            case "GET_QUEUE_STATS":
                handleGetQueueStats(session, data);
                break;
            default:
                logger.warn("未知的WebSocket动作: {}", action);
                sendMessage(session, createResponse("ERROR", "未知的动作", action));
        }
    }

    /**
     * 处理座席登录
     */
    private void handleAgentLogin(WebSocketSession session, Map<String, Object> data) {
        String agentId = (String) data.get("agentId");
        String stationId = (String) data.get("stationId");

        logger.info("座席登录请求 - 座席ID: {}, 工位: {}", agentId, stationId);

        try {
            // 通过Genesys SDK登录座席
            genesysSDKService.agentLogin(agentId, stationId).thenAccept(success -> {
                if (success) {
                    // 存储设备映射
                    session.getAttributes().put("agentId", agentId);
                    session.getAttributes().put("stationId", stationId);

                    sendMessage(session, createResponse("LOGIN_SUCCESS", "座席登录成功",
                            Map.of("agentId", agentId, "stationId", stationId)));

                    // 广播座席状态更新
                    broadcastAgentStatus(agentId, "AVAILABLE");

                } else {
                    sendMessage(session, createResponse("LOGIN_FAILED", "座席登录失败", null));
                }
            });

        } catch (Exception e) {
            logger.error("座席登录失败", e);
            sendMessage(session, createResponse("ERROR", "登录失败", e.getMessage()));
        }
    }

    /**
     * 处理座席登出
     */
    private void handleAgentLogout(WebSocketSession session, Map<String, Object> data) {
        String agentId = (String) session.getAttributes().get("agentId");

        logger.info("座席登出请求 - 座席ID: {}", agentId);

        try {
            if (agentId != null) {
                genesysSDKService.agentLogout(agentId).thenAccept(success -> {
                    if (success) {
                        sendMessage(session, createResponse("LOGOUT_SUCCESS", "座席登出成功",
                                Map.of("agentId", agentId)));

                        // 广播座席状态更新
                        broadcastAgentStatus(agentId, "LOGGED_OUT");

                    } else {
                        sendMessage(session, createResponse("LOGOUT_FAILED", "座席登出失败", null));
                    }
                });
            }

        } catch (Exception e) {
            logger.error("座席登出失败", e);
            sendMessage(session, createResponse("ERROR", "登出失败", e.getMessage()));
        }
    }

    /**
     * 处理发起呼叫
     */
    private void handleMakeCall(WebSocketSession session, Map<String, Object> data) {
        String agentId = (String) session.getAttributes().get("agentId");
        String phoneNumber = (String) data.get("phoneNumber");
        String customerId = (String) data.get("customerId");

        logger.info("发起呼叫请求 - 座席ID: {}, 号码: {}", agentId, phoneNumber);

        try {
            if (agentId != null && phoneNumber != null) {
                realCallService.makeRealCall(agentId, phoneNumber, customerId)
                        .thenAccept(result -> {
                            if ("success".equals(result.get("status"))) {
                                sendMessage(session, createResponse("CALL_INITIATED", "呼叫发起成功", result));

                                // 广播呼叫状态更新
                                broadcastCallStatus((String) result.get("genesysCallId"), "INITIATED");

                            } else {
                                sendMessage(session, createResponse("CALL_FAILED", "呼叫发起失败", result));
                            }
                        });
            } else {
                sendMessage(session, createResponse("ERROR", "参数错误", "座席ID或电话号码为空"));
            }

        } catch (Exception e) {
            logger.error("发起呼叫失败", e);
            sendMessage(session, createResponse("ERROR", "呼叫失败", e.getMessage()));
        }
    }

    /**
     * 处理接听呼叫
     */
    private void handleAnswerCall(WebSocketSession session, Map<String, Object> data) {
        String agentId = (String) session.getAttributes().get("agentId");
        String callId = (String) data.get("callId");

        logger.info("接听呼叫请求 - 座席ID: {}, 呼叫ID: {}", agentId, callId);

        try {
            if (agentId != null && callId != null) {
                realCallService.answerCall(callId, agentId)
                        .thenAccept(result -> {
                            if ("success".equals(result.get("status"))) {
                                sendMessage(session, createResponse("CALL_ANSWERED", "呼叫接听成功", result));

                                // 广播呼叫状态更新
                                broadcastCallStatus(callId, "ANSWERED");

                            } else {
                                sendMessage(session, createResponse("ANSWER_FAILED", "呼叫接听失败", result));
                            }
                        });
            } else {
                sendMessage(session, createResponse("ERROR", "参数错误", "座席ID或呼叫ID为空"));
            }

        } catch (Exception e) {
            logger.error("接听呼叫失败", e);
            sendMessage(session, createResponse("ERROR", "接听失败", e.getMessage()));
        }
    }

    /**
     * 处理挂断呼叫
     */
    private void handleEndCall(WebSocketSession session, Map<String, Object> data) {
        String callId = (String) data.get("callId");

        logger.info("挂断呼叫请求 - 呼叫ID: {}", callId);

        try {
            if (callId != null) {
                realCallService.endCall(callId)
                        .thenAccept(result -> {
                            if ("success".equals(result.get("status"))) {
                                sendMessage(session, createResponse("CALL_ENDED", "呼叫挂断成功", result));

                                // 广播呼叫状态更新
                                broadcastCallStatus(callId, "ENDED");

                            } else {
                                sendMessage(session, createResponse("END_FAILED", "呼叫挂断失败", result));
                            }
                        });
            } else {
                sendMessage(session, createResponse("ERROR", "参数错误", "呼叫ID为空"));
            }

        } catch (Exception e) {
            logger.error("挂断呼叫失败", e);
            sendMessage(session, createResponse("ERROR", "挂断失败", e.getMessage()));
        }
    }

    /**
     * 处理保持呼叫
     */
    private void handleHoldCall(WebSocketSession session, Map<String, Object> data) {
        String callId = (String) data.get("callId");

        logger.info("保持呼叫请求 - 呼叫ID: {}", callId);

        try {
            if (callId != null) {
                realCallService.holdOrResumeCall(callId, true)
                        .thenAccept(result -> {
                            if ("success".equals(result.get("status"))) {
                                sendMessage(session, createResponse("CALL_HELD", "呼叫保持成功", result));
                                broadcastCallStatus(callId, "HELD");
                            } else {
                                sendMessage(session, createResponse("HOLD_FAILED", "呼叫保持失败", result));
                            }
                        });
            } else {
                sendMessage(session, createResponse("ERROR", "参数错误", "呼叫ID为空"));
            }

        } catch (Exception e) {
            logger.error("保持呼叫失败", e);
            sendMessage(session, createResponse("ERROR", "保持失败", e.getMessage()));
        }
    }

    /**
     * 处理恢复呼叫
     */
    private void handleResumeCall(WebSocketSession session, Map<String, Object> data) {
        String callId = (String) data.get("callId");

        logger.info("恢复呼叫请求 - 呼叫ID: {}", callId);

        try {
            if (callId != null) {
                realCallService.holdOrResumeCall(callId, false)
                        .thenAccept(result -> {
                            if ("success".equals(result.get("status"))) {
                                sendMessage(session, createResponse("CALL_RESUMED", "呼叫恢复成功", result));
                                broadcastCallStatus(callId, "RESUMED");
                            } else {
                                sendMessage(session, createResponse("RESUME_FAILED", "呼叫恢复失败", result));
                            }
                        });
            } else {
                sendMessage(session, createResponse("ERROR", "参数错误", "呼叫ID为空"));
            }

        } catch (Exception e) {
            logger.error("恢复呼叫失败", e);
            sendMessage(session, createResponse("ERROR", "恢复失败", e.getMessage()));
        }
    }

    /**
     * 处理转移呼叫
     */
    private void handleTransferCall(WebSocketSession session, Map<String, Object> data) {
        String callId = (String) data.get("callId");
        String targetAgentId = (String) data.get("targetAgentId");

        logger.info("转移呼叫请求 - 呼叫ID: {}, 目标座席: {}", callId, targetAgentId);

        try {
            if (callId != null && targetAgentId != null) {
                realCallService.transferCall(callId, targetAgentId)
                        .thenAccept(result -> {
                            if ("success".equals(result.get("status"))) {
                                sendMessage(session, createResponse("CALL_TRANSFERRED", "呼叫转移成功", result));
                                broadcastCallStatus(callId, "TRANSFERRED");
                            } else {
                                sendMessage(session, createResponse("TRANSFER_FAILED", "呼叫转移失败", result));
                            }
                        });
            } else {
                sendMessage(session, createResponse("ERROR", "参数错误", "呼叫ID或目标座席ID为空"));
            }

        } catch (Exception e) {
            logger.error("转移呼叫失败", e);
            sendMessage(session, createResponse("ERROR", "转移失败", e.getMessage()));
        }
    }

    /**
     * 处理会议呼叫
     */
    private void handleConferenceCall(WebSocketSession session, Map<String, Object> data) {
        String callId = (String) data.get("callId");
        String additionalAgentId = (String) data.get("additionalAgentId");

        logger.info("会议呼叫请求 - 呼叫ID: {}, 添加座席: {}", callId, additionalAgentId);

        try {
            if (callId != null && additionalAgentId != null) {
                realCallService.conferenceCall(callId, additionalAgentId)
                        .thenAccept(result -> {
                            if ("success".equals(result.get("status"))) {
                                sendMessage(session, createResponse("CONFERENCE_ADDED", "会议添加成功", result));
                                broadcastCallStatus(callId, "CONFERENCING");
                            } else {
                                sendMessage(session, createResponse("CONFERENCE_FAILED", "会议添加失败", result));
                            }
                        });
            } else {
                sendMessage(session, createResponse("ERROR", "参数错误", "呼叫ID或座席ID为空"));
            }

        } catch (Exception e) {
            logger.error("会议呼叫失败", e);
            sendMessage(session, createResponse("ERROR", "会议失败", e.getMessage()));
        }
    }

    /**
     * 处理静音呼叫
     */
    private void handleMuteCall(WebSocketSession session, Map<String, Object> data) {
        String callId = (String) data.get("callId");
        Boolean mute = (Boolean) data.get("mute");

        logger.info("静音呼叫请求 - 呼叫ID: {}, 静音: {}", callId, mute);

        try {
            if (callId != null && mute != null) {
                realCallService.muteCall(callId, mute)
                        .thenAccept(result -> {
                            if ("success".equals(result.get("status"))) {
                                sendMessage(session, createResponse("CALL_MUTED", "呼叫静音成功", result));
                            } else {
                                sendMessage(session, createResponse("MUTE_FAILED", "呼叫静音失败", result));
                            }
                        });
            } else {
                sendMessage(session, createResponse("ERROR", "参数错误", "呼叫ID或静音状态为空"));
            }

        } catch (Exception e) {
            logger.error("静音呼叫失败", e);
            sendMessage(session, createResponse("ERROR", "静音失败", e.getMessage()));
        }
    }

    /**
     * 处理发送DTMF
     */
    private void handleSendDTMF(WebSocketSession session, Map<String, Object> data) {
        String callId = (String) data.get("callId");
        String digit = (String) data.get("digit");

        logger.info("发送DTMF请求 - 呼叫ID: {}, 数字: {}", callId, digit);

        try {
            if (callId != null && digit != null) {
                realCallService.sendDTMF(callId, digit)
                        .thenAccept(result -> {
                            if ("success".equals(result.get("status"))) {
                                sendMessage(session, createResponse("DTMF_SENT", "DTMF发送成功", result));
                            } else {
                                sendMessage(session, createResponse("DTMF_FAILED", "DTMF发送失败", result));
                            }
                        });
            } else {
                sendMessage(session, createResponse("ERROR", "参数错误", "呼叫ID或DTMF数字为空"));
            }

        } catch (Exception e) {
            logger.error("发送DTMF失败", e);
            sendMessage(session, createResponse("ERROR", "DTMF发送失败", e.getMessage()));
        }
    }

    /**
     * 处理获取呼叫状态
     */
    private void handleGetCallStatus(WebSocketSession session, Map<String, Object> data) {
        String callId = (String) data.get("callId");

        logger.info("获取呼叫状态请求 - 呼叫ID: {}", callId);

        try {
            if (callId != null) {
                Map<String, Object> status = realCallService.getCallStatus(callId);
                sendMessage(session, createResponse("CALL_STATUS", "呼叫状态获取成功", status));
            } else {
                sendMessage(session, createResponse("ERROR", "参数错误", "呼叫ID为空"));
            }

        } catch (Exception e) {
            logger.error("获取呼叫状态失败", e);
            sendMessage(session, createResponse("ERROR", "获取状态失败", e.getMessage()));
        }
    }

    /**
     * 处理获取座席状态
     */
    private void handleGetAgentStatus(WebSocketSession session, Map<String, Object> data) {
        String agentId = (String) data.get("agentId");

        logger.info("获取座席状态请求 - 座席ID: {}", agentId);

        try {
            if (agentId != null) {
                genesysSDKService.getAgentStatus(agentId).thenAccept(status -> {
                    sendMessage(session, createResponse("AGENT_STATUS", "座席状态获取成功", status));
                });
            } else {
                sendMessage(session, createResponse("ERROR", "参数错误", "座席ID为空"));
            }

        } catch (Exception e) {
            logger.error("获取座席状态失败", e);
            sendMessage(session, createResponse("ERROR", "获取状态失败", e.getMessage()));
        }
    }

    /**
     * 处理设置座席状态
     */
    private void handleSetAgentStatus(WebSocketSession session, Map<String, Object> data) {
        String agentId = (String) session.getAttributes().get("agentId");
        String status = (String) data.get("status");

        logger.info("设置座席状态请求 - 座席ID: {}, 状态: {}", agentId, status);

        try {
            if (agentId != null && status != null) {
                genesysSDKService.setAgentStatus(agentId, status).thenAccept(success -> {
                    if (success) {
                        sendMessage(session, createResponse("STATUS_SET", "座席状态设置成功",
                                Map.of("agentId", agentId, "status", status)));

                        // 广播座席状态更新
                        broadcastAgentStatus(agentId, status);

                    } else {
                        sendMessage(session, createResponse("STATUS_SET_FAILED", "座席状态设置失败", null));
                    }
                });
            } else {
                sendMessage(session, createResponse("ERROR", "参数错误", "座席ID或状态为空"));
            }

        } catch (Exception e) {
            logger.error("设置座席状态失败", e);
            sendMessage(session, createResponse("ERROR", "设置状态失败", e.getMessage()));
        }
    }

    /**
     * 处理获取队列统计
     */
    private void handleGetQueueStats(WebSocketSession session, Map<String, Object> data) {
        String queueName = (String) data.get("queueName");

        logger.info("获取队列统计请求 - 队列名称: {}", queueName);

        try {
            if (queueName != null) {
                // 这里应该调用GCTI服务获取队列统计
                Map<String, Object> stats = Map.of(
                        "queueName", queueName,
                        "activeCalls", 5,
                        "waitingCalls", 2,
                        "availableAgents", 8,
                        "totalAgents", 12
                );

                sendMessage(session, createResponse("QUEUE_STATS", "队列统计获取成功", stats));
            } else {
                sendMessage(session, createResponse("ERROR", "参数错误", "队列名称为空"));
            }

        } catch (Exception e) {
            logger.error("获取队列统计失败", e);
            sendMessage(session, createResponse("ERROR", "获取统计失败", e.getMessage()));
        }
    }

    /**
     * 广播呼叫状态更新
     */
    private void broadcastCallStatus(String callId, String status) {
        Map<String, Object> message = createResponse("CALL_STATUS_UPDATE", "呼叫状态更新",
                Map.of("callId", callId, "status", status, "timestamp", System.currentTimeMillis()));

        // 广播给所有管理员会话
        adminSessions.values().forEach(session -> {
            sendMessage(session, message);
        });
    }

    /**
     * 广播座席状态更新
     */
    private void broadcastAgentStatus(String agentId, String status) {
        Map<String, Object> message = createResponse("AGENT_STATUS_UPDATE", "座席状态更新",
                Map.of("agentId", agentId, "status", status, "timestamp", System.currentTimeMillis()));

        // 广播给所有管理员会话
        adminSessions.values().forEach(session -> {
            sendMessage(session, message);
        });
    }

    /**
     * 创建响应消息
     */
    private Map<String, Object> createResponse(String action, String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("action", action);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        if (data != null) {
            response.put("data", data);
        }
        return response;
    }

    /**
     * 发送消息
     */
    private void sendMessage(WebSocketSession session, Map<String, Object> message) {
        try {
            if (session.isOpen()) {
                String jsonMessage = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(jsonMessage));
            }
        } catch (IOException e) {
            logger.error("发送WebSocket消息失败", e);
        }
    }

    /**
     * 从会话获取用户类型
     */
    private String getUserTypeFromSession(WebSocketSession session) {
        // 这里应该从会话属性或URL参数中获取用户类型
        // 简化处理，返回座席类型
        return "agent";
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String sessionId = session.getId();
        String agentId = (String) session.getAttributes().get("agentId");

        logger.info("WebSocket连接关闭 - 会话ID: {}, 座席ID: {}, 关闭状态: {}",
                sessionId, agentId, closeStatus);

        // 清理会话
        agentSessions.remove(sessionId);
        adminSessions.remove(sessionId);

        // 如果座席已登录，自动登出
        if (agentId != null) {
            try {
                genesysSDKService.agentLogout(agentId);
                broadcastAgentStatus(agentId, "DISCONNECTED");
            } catch (Exception e) {
                logger.error("座席自动登出失败", e);
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String sessionId = session.getId();
        logger.error("WebSocket传输错误 - 会话ID: {}", sessionId, exception);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 获取连接的座席会话数量
     */
    public int getConnectedAgentCount() {
        return agentSessions.size();
    }

    /**
     * 获取连接的管理员会话数量
     */
    public int getConnectedAdminCount() {
        return adminSessions.size();
    }

    /**
     * 广播系统消息
     */
    public void broadcastSystemMessage(String message) {
        Map<String, Object> systemMessage = createResponse("SYSTEM_MESSAGE", "系统消息",
                Map.of("message", message, "timestamp", System.currentTimeMillis()));

        // 广播给所有会话
        agentSessions.values().forEach(session -> sendMessage(session, systemMessage));
        adminSessions.values().forEach(session -> sendMessage(session, systemMessage));
    }
}