package com.gfo.demo.genesys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class KazimirLogger {

    private static final Logger logger = LoggerFactory.getLogger(KazimirLogger.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateTimeFormatter FILE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ConcurrentMap<String, PrintWriter> logWriters = new ConcurrentHashMap<>();
    private final String logDirectory = "logs/kazimir";

    public KazimirLogger() {
        try {
            Files.createDirectories(Paths.get(logDirectory));
        } catch (IOException e) {
            logger.error("Failed to create log directory", e);
        }
    }

    public void logInteraction(String interactionId, String level, String message, Object... args) {
        String formattedMessage = String.format(message, args);
        String logEntry = String.format("[%s] [%s] [INTERACTION:%s] %s",
                LocalDateTime.now().format(TIMESTAMP_FORMATTER),
                level,
                interactionId,
                formattedMessage);

        writeToFile("interactions", logEntry);
        logger.info(logEntry);
    }

    public void logAgentActivity(String agentId, String activity, String details) {
        String logEntry = String.format("[%s] [INFO] [AGENT:%s] Activity: %s - %s",
                LocalDateTime.now().format(TIMESTAMP_FORMATTER),
                agentId,
                activity,
                details);

        writeToFile("agents", logEntry);
        logger.info(logEntry);
    }

    public void logSystemEvent(String component, String event, String status) {
        String logEntry = String.format("[%s] [INFO] [SYSTEM:%s] Event: %s - Status: %s",
                LocalDateTime.now().format(TIMESTAMP_FORMATTER),
                component,
                event,
                status);

        writeToFile("system", logEntry);
        logger.info(logEntry);
    }

    public void logError(String component, String error, Exception exception) {
        String logEntry = String.format("[%s] [ERROR] [COMPONENT:%s] Error: %s - Exception: %s",
                LocalDateTime.now().format(TIMESTAMP_FORMATTER),
                component,
                error,
                exception.getMessage());

        writeToFile("errors", logEntry);
        logger.error(logEntry, exception);
    }

    public void logPerformanceMetrics(String operation, long duration, int recordCount) {
        String logEntry = String.format("[%s] [PERF] Operation: %s - Duration: %dms - Records: %d",
                LocalDateTime.now().format(TIMESTAMP_FORMATTER),
                operation,
                duration,
                recordCount);

        writeToFile("performance", logEntry);
        logger.info(logEntry);
    }

    private void writeToFile(String logType, String logEntry) {
        try {
            String today = LocalDateTime.now().format(FILE_FORMATTER);
            String fileName = String.format("%s/%s_%s.log", logDirectory, logType, today);

            PrintWriter writer = logWriters.computeIfAbsent(fileName, this::createWriter);
            synchronized (writer) {
                writer.println(logEntry);
                writer.flush();
            }
        } catch (Exception e) {
            logger.error("Failed to write to log file", e);
        }
    }

    private PrintWriter createWriter(String fileName) {
        try {
            return new PrintWriter(new FileWriter(fileName, true), true);
        } catch (IOException e) {
            logger.error("Failed to create log writer for: " + fileName, e);
            return new PrintWriter(System.out); // Fallback to console
        }
    }

    public void closeAllWriters() {
        logWriters.values().forEach(PrintWriter::close);
        logWriters.clear();
    }

    // 新增的呼叫日志方法
    public void logCallStart(String agentId, String phoneNumber, String customerId) {
        String logEntry = String.format("[%s] [INFO] [CALL:START] Agent: %s, Phone: %s, Customer: %s",
                LocalDateTime.now().format(TIMESTAMP_FORMATTER),
                agentId, phoneNumber, customerId != null ? customerId : "Unknown");
        writeToFile("calls", logEntry);
        logger.info(logEntry);
    }

    public void logCallSuccess(String callId, String action) {
        String logEntry = String.format("[%s] [INFO] [CALL:SUCCESS] CallID: %s, Action: %s",
                LocalDateTime.now().format(TIMESTAMP_FORMATTER),
                callId, action);
        writeToFile("calls", logEntry);
        logger.info(logEntry);
    }

    public void logCallError(String agentId, String callId, String error) {
        String logEntry = String.format("[%s] [ERROR] [CALL:ERROR] Agent: %s, CallID: %s, Error: %s",
                LocalDateTime.now().format(TIMESTAMP_FORMATTER),
                agentId, callId, error);
        writeToFile("calls", logEntry);
        logger.error(logEntry);
    }

    public void logCallAnswer(String agentId, String callId) {
        String logEntry = String.format("[%s] [INFO] [CALL:ANSWER] Agent: %s, CallID: %s",
                LocalDateTime.now().format(TIMESTAMP_FORMATTER),
                agentId, callId);
        writeToFile("calls", logEntry);
        logger.info(logEntry);
    }

    public void logCallEnd(String callId) {
        String logEntry = String.format("[%s] [INFO] [CALL:END] CallID: %s",
                LocalDateTime.now().format(TIMESTAMP_FORMATTER),
                callId);
        writeToFile("calls", logEntry);
        logger.info(logEntry);
    }

    public void logCallHold(String callId, boolean hold) {
        String action = hold ? "HOLD" : "UNHOLD";
        String logEntry = String.format("[%s] [INFO] [CALL:%s] CallID: %s",
                LocalDateTime.now().format(TIMESTAMP_FORMATTER),
                action, callId);
        writeToFile("calls", logEntry);
        logger.info(logEntry);
    }

    public void logCallTransfer(String callId, String targetAgentId) {
        String logEntry = String.format("[%s] [INFO] [CALL:TRANSFER] CallID: %s, TargetAgent: %s",
                LocalDateTime.now().format(TIMESTAMP_FORMATTER),
                callId, targetAgentId);
        writeToFile("calls", logEntry);
        logger.info(logEntry);
    }

    public void logCallConference(String callId, String additionalAgentId) {
        String logEntry = String.format("[%s] [INFO] [CALL:CONFERENCE] CallID: %s, AdditionalAgent: %s",
                LocalDateTime.now().format(TIMESTAMP_FORMATTER),
                callId, additionalAgentId);
        writeToFile("calls", logEntry);
        logger.info(logEntry);
    }
}