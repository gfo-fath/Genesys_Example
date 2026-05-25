package com.gfo.demo.genesys;

import com.gfo.demo.entity.Interaction;
import com.gfo.demo.entity.InteractionStatus;
import com.gfo.demo.repository.InteractionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 真实电话呼叫服务
 * 整合Genesys SDK和SIP电话服务，提供完整的真实电话呼叫功能
 * 支持呼入、呼出、转接、会议等完整的呼叫中心功能
 */
@Service
public class RealCallService {

    private static final Logger logger = LoggerFactory.getLogger(RealCallService.class);

    @Autowired
    private GenesysSDKService genesysSDKService;

    @Autowired
    private SIPPhoneService sipPhoneService;

    @Autowired
    private InteractionRepository interactionRepository;

    @Autowired
    private KazimirLogger kazimirLogger;

    /**
     * 发起真实电话呼叫
     * @param agentId 座席ID
     * @param phoneNumber 目标电话号码
     * @param customerId 客户ID（可选）
     * @return 呼叫结果
     */
    public CompletableFuture<Map<String, Object>> makeRealCall(String agentId, String phoneNumber, String customerId) {
        Map<String, Object> result = new HashMap<>();

        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("正在发起真实电话呼叫 - 座席: {}, 号码: {}, 客户: {}",
                        agentId, phoneNumber, customerId != null ? customerId : "未知");

                // 记录Kazimir日志
                kazimirLogger.logCallStart(agentId, phoneNumber, customerId);

                // 1. 通过Genesys SDK发起呼叫
                CompletableFuture<String> genesysCall = genesysSDKService.initiateRealCall(agentId, phoneNumber);

                // 2. 通过SIP服务发起呼叫
                CompletableFuture<String> sipCall = sipPhoneService.initiateSIPCall("DEVICE_" + agentId, phoneNumber);

                // 等待两个服务都完成
                String genesysCallId = genesysCall.get();
                String sipCallId = sipCall.get();

                // 创建交互记录
                Interaction interaction = createInteraction(agentId, phoneNumber, customerId, "VOICE", "OUTBOUND");
                interactionRepository.save(interaction);

                result.put("status", "success");
                result.put("genesysCallId", genesysCallId);
                result.put("sipCallId", sipCallId);
                result.put("interactionId", interaction.getId());
                result.put("message", "真实电话呼叫发起成功");

                logger.info("真实电话呼叫发起成功 - Genesys ID: {}, SIP ID: {}", genesysCallId, sipCallId);

                // 记录成功日志
                kazimirLogger.logCallSuccess(genesysCallId, "CALL_INITIATED");

                return result;

            } catch (Exception e) {
                logger.error("发起真实电话呼叫失败", e);

                result.put("status", "error");
                result.put("message", "呼叫发起失败: " + e.getMessage());

                // 记录失败日志
                kazimirLogger.logCallError(agentId, phoneNumber, e.getMessage());

                throw new RuntimeException("真实电话呼叫发起失败", e);
            }
        });
    }

    /**
     * 接听来电
     * @param callId 呼叫ID
     * @param agentId 座席ID
     * @return 接听结果
     */
    public CompletableFuture<Map<String, Object>> answerCall(String callId, String agentId) {
        Map<String, Object> result = new HashMap<>();

        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("正在接听电话呼叫 - 呼叫ID: {}, 座席: {}", callId, agentId);

                // 记录Kazimir日志
                kazimirLogger.logCallAnswer(agentId, callId);

                // 1. 通过Genesys SDK接听
                CompletableFuture<Boolean> genesysAnswer = genesysSDKService.answerCall(callId, agentId);

                // 2. 通过SIP服务接听
                CompletableFuture<Boolean> sipAnswer = sipPhoneService.answerSIPCall(callId, "DEVICE_" + agentId);

                // 等待两个服务都完成
                boolean genesysSuccess = genesysAnswer.get();
                boolean sipSuccess = sipAnswer.get();

                if (genesysSuccess && sipSuccess) {
                    result.put("status", "success");
                    result.put("message", "电话接听成功");

                    logger.info("电话接听成功 - 呼叫ID: {}", callId);

                    // 记录成功日志
                    kazimirLogger.logCallSuccess(callId, "CALL_ANSWERED");

                } else {
                    result.put("status", "error");
                    result.put("message", "电话接听失败");

                    logger.error("电话接听失败 - 呼叫ID: {}", callId);
                }

                return result;

            } catch (Exception e) {
                logger.error("接听电话呼叫失败", e);

                result.put("status", "error");
                result.put("message", "接听失败: " + e.getMessage());

                // 记录失败日志
                kazimirLogger.logCallError(agentId, callId, e.getMessage());

                throw new RuntimeException("电话接听失败", e);
            }
        });
    }

    /**
     * 挂断电话
     * @param callId 呼叫ID
     * @return 挂断结果
     */
    public CompletableFuture<Map<String, Object>> endCall(String callId) {
        Map<String, Object> result = new HashMap<>();

        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("正在挂断电话呼叫 - 呼叫ID: {}", callId);

                // 记录Kazimir日志
                kazimirLogger.logCallEnd(callId);

                // 1. 通过Genesys SDK挂断
                CompletableFuture<Boolean> genesysEnd = genesysSDKService.endCall(callId);

                // 2. 通过SIP服务挂断
                CompletableFuture<Boolean> sipEnd = sipPhoneService.endSIPCall(callId);

                // 等待两个服务都完成
                boolean genesysSuccess = genesysEnd.get();
                boolean sipSuccess = sipEnd.get();

                if (genesysSuccess && sipSuccess) {
                    result.put("status", "success");
                    result.put("message", "电话挂断成功");

                    logger.info("电话挂断成功 - 呼叫ID: {}", callId);

                    // 记录成功日志
                    kazimirLogger.logCallSuccess(callId, "CALL_ENDED");

                } else {
                    result.put("status", "error");
                    result.put("message", "电话挂断失败");

                    logger.error("电话挂断失败 - 呼叫ID: {}", callId);
                }

                return result;

            } catch (Exception e) {
                logger.error("挂断电话呼叫失败", e);

                result.put("status", "error");
                result.put("message", "挂断失败: " + e.getMessage());

                // 记录失败日志
                kazimirLogger.logCallError("UNKNOWN", callId, e.getMessage());

                throw new RuntimeException("电话挂断失败", e);
            }
        });
    }

    /**
     * 保持/恢复电话
     * @param callId 呼叫ID
     * @param hold true为保持，false为恢复
     * @return 操作结果
     */
    public CompletableFuture<Map<String, Object>> holdOrResumeCall(String callId, boolean hold) {
        Map<String, Object> result = new HashMap<>();
        String action = hold ? "保持" : "恢复";

        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("正在{}电话呼叫 - 呼叫ID: {}", action, callId);

                // 记录Kazimir日志
                kazimirLogger.logCallHold(callId, hold);

                // 1. 通过Genesys SDK操作
                CompletableFuture<Boolean> genesysResult = hold ?
                        genesysSDKService.holdCall(callId) :
                        genesysSDKService.resumeCall(callId);

                // 2. 通过SIP服务操作
                CompletableFuture<Boolean> sipResult = sipPhoneService.setCallHold(callId, hold);

                // 等待两个服务都完成
                boolean genesysSuccess = genesysResult.get();
                boolean sipSuccess = sipResult.get();

                if (genesysSuccess && sipSuccess) {
                    result.put("status", "success");
                    result.put("message", "电话" + action + "成功");

                    logger.info("电话{}成功 - 呼叫ID: {}", action, callId);

                    // 记录成功日志
                    kazimirLogger.logCallSuccess(callId, hold ? "CALL_HELD" : "CALL_RESUMED");

                } else {
                    result.put("status", "error");
                    result.put("message", "电话" + action + "失败");

                    logger.error("电话{}失败 - 呼叫ID: {}", action, callId);
                }

                return result;

            } catch (Exception e) {
                logger.error("{}电话呼叫失败", action, e);

                result.put("status", "error");
                result.put("message", action + "失败: " + e.getMessage());

                // 记录失败日志
                kazimirLogger.logCallError("UNKNOWN", callId, e.getMessage());

                throw new RuntimeException("电话" + action + "失败", e);
            }
        });
    }

    /**
     * 转移电话
     * @param callId 呼叫ID
     * @param targetAgentId 目标座席ID
     * @return 转移结果
     */
    public CompletableFuture<Map<String, Object>> transferCall(String callId, String targetAgentId) {
        Map<String, Object> result = new HashMap<>();

        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("正在转移电话呼叫 - 呼叫ID: {}, 目标座席: {}", callId, targetAgentId);

                // 记录Kazimir日志
                kazimirLogger.logCallTransfer(callId, targetAgentId);

                // 1. 通过Genesys SDK转移
                CompletableFuture<Boolean> genesysTransfer = genesysSDKService.transferCall(callId, targetAgentId);

                // 2. 通过SIP服务转移
                CompletableFuture<Boolean> sipTransfer = sipPhoneService.transferSIPCall(callId, "DEVICE_" + targetAgentId);

                // 等待两个服务都完成
                boolean genesysSuccess = genesysTransfer.get();
                boolean sipSuccess = sipTransfer.get();

                if (genesysSuccess && sipSuccess) {
                    result.put("status", "success");
                    result.put("targetAgentId", targetAgentId);
                    result.put("message", "电话转移成功");

                    logger.info("电话转移成功 - 呼叫ID: {}, 目标座席: {}", callId, targetAgentId);

                    // 记录成功日志
                    kazimirLogger.logCallSuccess(callId, "CALL_TRANSFERRED");

                } else {
                    result.put("status", "error");
                    result.put("message", "电话转移失败");

                    logger.error("电话转移失败 - 呼叫ID: {}", callId);
                }

                return result;

            } catch (Exception e) {
                logger.error("转移电话呼叫失败", e);

                result.put("status", "error");
                result.put("message", "转移失败: " + e.getMessage());

                // 记录失败日志
                kazimirLogger.logCallError("UNKNOWN", callId, e.getMessage());

                throw new RuntimeException("电话转移失败", e);
            }
        });
    }

    /**
     * 会议电话
     * @param callId 主呼叫ID
     * @param additionalAgentId 要添加的座席ID
     * @return 会议结果
     */
    public CompletableFuture<Map<String, Object>> conferenceCall(String callId, String additionalAgentId) {
        Map<String, Object> result = new HashMap<>();

        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("正在添加会议参与者 - 呼叫ID: {}, 座席: {}", callId, additionalAgentId);

                // 记录Kazimir日志
                kazimirLogger.logCallConference(callId, additionalAgentId);

                // 通过SIP服务添加会议参与者
                CompletableFuture<Boolean> sipConference = sipPhoneService.conferenceCall(callId, "DEVICE_" + additionalAgentId);

                boolean success = sipConference.get();

                if (success) {
                    result.put("status", "success");
                    result.put("additionalAgentId", additionalAgentId);
                    result.put("message", "会议参与者添加成功");

                    logger.info("会议参与者添加成功 - 呼叫ID: {}, 座席: {}", callId, additionalAgentId);

                    // 记录成功日志
                    kazimirLogger.logCallSuccess(callId, "CONFERENCE_ADDED");

                } else {
                    result.put("status", "error");
                    result.put("message", "会议参与者添加失败");

                    logger.error("会议参与者添加失败 - 呼叫ID: {}", callId);
                }

                return result;

            } catch (Exception e) {
                logger.error("添加会议参与者失败", e);

                result.put("status", "error");
                result.put("message", "添加失败: " + e.getMessage());

                // 记录失败日志
                kazimirLogger.logCallError("UNKNOWN", callId, e.getMessage());

                throw new RuntimeException("会议添加失败", e);
            }
        });
    }

    /**
     * 静音/取消静音
     * @param callId 呼叫ID
     * @param mute true为静音，false为取消静音
     * @return 操作结果
     */
    public CompletableFuture<Map<String, Object>> muteCall(String callId, boolean mute) {
        Map<String, Object> result = new HashMap<>();
        String action = mute ? "静音" : "取消静音";

        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("正在{}电话呼叫 - 呼叫ID: {}", action, callId);

                // 通过Genesys SDK静音
                CompletableFuture<Boolean> genesysMute = genesysSDKService.muteCall(callId, mute);

                boolean success = genesysMute.get();

                if (success) {
                    result.put("status", "success");
                    result.put("message", "电话" + action + "成功");

                    logger.info("电话{}成功 - 呼叫ID: {}", action, callId);

                } else {
                    result.put("status", "error");
                    result.put("message", "电话" + action + "失败");

                    logger.error("电话{}失败 - 呼叫ID: {}", action, callId);
                }

                return result;

            } catch (Exception e) {
                logger.error("{}电话呼叫失败", action, e);

                result.put("status", "error");
                result.put("message", action + "失败: " + e.getMessage());

                throw new RuntimeException("电话" + action + "失败", e);
            }
        });
    }

    /**
     * 发送DTMF信号
     * @param callId 呼叫ID
     * @param digit DTMF数字
     * @return 发送结果
     */
    public CompletableFuture<Map<String, Object>> sendDTMF(String callId, String digit) {
        Map<String, Object> result = new HashMap<>();

        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("正在发送DTMF信号 - 呼叫ID: {}, 数字: {}", callId, digit);

                // 通过SIP服务发送DTMF
                CompletableFuture<Boolean> dtmfResult = sipPhoneService.sendDTMF(callId, digit);

                boolean success = dtmfResult.get();

                if (success) {
                    result.put("status", "success");
                    result.put("digit", digit);
                    result.put("message", "DTMF信号发送成功");

                    logger.info("DTMF信号发送成功 - 呼叫ID: {}, 数字: {}", callId, digit);

                } else {
                    result.put("status", "error");
                    result.put("message", "DTMF信号发送失败");

                    logger.error("DTMF信号发送失败 - 呼叫ID: {}", callId);
                }

                return result;

            } catch (Exception e) {
                logger.error("发送DTMF信号失败", e);

                result.put("status", "error");
                result.put("message", "发送失败: " + e.getMessage());

                throw new RuntimeException("DTMF发送失败", e);
            }
        });
    }

    /**
     * 获取呼叫状态
     * @param callId 呼叫ID
     * @return 呼叫状态信息
     */
    public Map<String, Object> getCallStatus(String callId) {
        Map<String, Object> status = new HashMap<>();

        try {
            logger.info("正在获取呼叫状态 - 呼叫ID: {}", callId);

            // 获取Genesys SDK呼叫状态
            GenesysSDKService.CallSession genesysSession = genesysSDKService.getCallSession(callId);

            // 获取SIP呼叫状态
            SIPPhoneService.SIPSession sipSession = sipPhoneService.getSIPSession(callId);

            status.put("callId", callId);

            if (genesysSession != null) {
                status.put("genesysStatus", genesysSession.getStatus());
                status.put("genesysAgentId", genesysSession.getAgentId());
                status.put("genesysDuration", genesysSession.getDuration());
                status.put("genesysMuted", genesysSession.isMuted());
            }

            if (sipSession != null) {
                status.put("sipStatus", sipSession.getStatus());
                status.put("sipFromDevice", sipSession.getFromDeviceId());
                status.put("sipToPhone", sipSession.getToPhoneNumber());
                status.put("sipHeld", sipSession.isHeld());
                status.put("sipConference", sipSession.isConference());
            }

            status.put("timestamp", System.currentTimeMillis());

            logger.info("呼叫状态获取成功 - 呼叫ID: {}", callId);

        } catch (Exception e) {
            logger.error("获取呼叫状态失败", e);
            status.put("error", "获取状态失败: " + e.getMessage());
        }

        return status;
    }

    /**
     * 创建交互记录
     * @param agentId 座席ID
     * @param phoneNumber 电话号码
     * @param customerId 客户ID
     * @param type 交互类型
     * @param direction 方向
     * @return 交互实体
     */
    private Interaction createInteraction(String agentId, String phoneNumber, String customerId, String type, String direction) {
        Interaction interaction = new Interaction();
        interaction.setInteractionId("INT_" + System.currentTimeMillis());
        interaction.setType(type);
        interaction.setDirection(direction);
        interaction.setAgentId(agentId);
        interaction.setContent("电话呼叫至: " + phoneNumber);
        interaction.setStartTime(LocalDateTime.now());
        interaction.setStatus(InteractionStatus.ACTIVE);

        // 这里应该设置客户信息，简化处理
        // interaction.setCustomer(customer);

        return interaction;
    }
}