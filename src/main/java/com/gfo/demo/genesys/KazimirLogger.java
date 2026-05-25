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
}