package com.gfo.demo.genesys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Kazimir日志分析器
 * 提供Genesys Kazimir日志的分析、解析和故障排除功能
 * 支持日志模式识别、错误检测、性能分析等功能
 */
@Service
public class KazimirLogAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(KazimirLogAnalyzer.class);

    @Autowired
    private KazimirLogger kazimirLogger;

    // 日志文件路径
    private String logFilePath = "logs/genesys-example.log";

    // 错误模式正则表达式
    private static final Pattern ERROR_PATTERN = Pattern.compile(".*(ERROR|FATAL|EXCEPTION).*");
    private static final Pattern WARN_PATTERN = Pattern.compile(".*(WARN|WARNING).*");
    private static final Pattern CALL_PATTERN = Pattern.compile(".*呼叫.*(ID|id):?\\s*([A-Za-z0-9_]+).*");
    private static final Pattern AGENT_PATTERN = Pattern.compile(".*座席.*(ID|id):?\\s*([A-Za-z0-9_]+).*");
    private static final Pattern DURATION_PATTERN = Pattern.compile(".*持续时间:?\\s*(\\d+).*");
    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile(".*(\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}).*");

    /**
     * 分析指定时间范围内的日志
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日志分析结果
     */
    public Map<String, Object> analyzeLogs(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> analysis = new HashMap<>();

        try {
            List<String> logLines = readLogFile();

            // 基本统计
            analysis.put("totalLines", logLines.size());
            analysis.put("analysisPeriod", Map.of(
                    "startTime", startTime,
                    "endTime", endTime
            ));

            // 错误分析
            Map<String, Object> errorAnalysis = analyzeErrors(logLines, startTime, endTime);
            analysis.put("errorAnalysis", errorAnalysis);

            // 性能分析
            Map<String, Object> performanceAnalysis = analyzePerformance(logLines, startTime, endTime);
            analysis.put("performanceAnalysis", performanceAnalysis);

            // 呼叫分析
            Map<String, Object> callAnalysis = analyzeCalls(logLines, startTime, endTime);
            analysis.put("callAnalysis", callAnalysis);

            // 座席分析
            Map<String, Object> agentAnalysis = analyzeAgents(logLines, startTime, endTime);
            analysis.put("agentAnalysis", agentAnalysis);

            logger.info("日志分析完成 - 时间段: {} 到 {}", startTime, endTime);

        } catch (Exception e) {
            logger.error("日志分析失败", e);
            analysis.put("error", "日志分析失败: " + e.getMessage());
        }

        return analysis;
    }

    /**
     * 分析错误日志
     */
    private Map<String, Object> analyzeErrors(List<String> logLines, LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> errorAnalysis = new HashMap<>();

        List<String> errorLines = new ArrayList<>();
        List<String> warnLines = new ArrayList<>();
        Map<String, Integer> errorCounts = new HashMap<>();

        for (String line : logLines) {
            LocalDateTime logTime = extractTimestamp(line);
            if (logTime != null && isInTimeRange(logTime, startTime, endTime)) {
                if (ERROR_PATTERN.matcher(line).matches()) {
                    errorLines.add(line);

                    // 统计错误类型
                    String errorType = extractErrorType(line);
                    errorCounts.put(errorType, errorCounts.getOrDefault(errorType, 0) + 1);

                } else if (WARN_PATTERN.matcher(line).matches()) {
                    warnLines.add(line);
                }
            }
        }

        errorAnalysis.put("errorCount", errorLines.size());
        errorAnalysis.put("warningCount", warnLines.size());
        errorAnalysis.put("errorTypes", errorCounts);
        errorAnalysis.put("recentErrors", errorLines.stream().limit(10).collect(Collectors.toList()));
        errorAnalysis.put("recentWarnings", warnLines.stream().limit(10).collect(Collectors.toList()));

        return errorAnalysis;
    }

    /**
     * 分析性能数据
     */
    private Map<String, Object> analyzePerformance(List<String> logLines, LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> performanceAnalysis = new HashMap<>();

        List<Long> durations = new ArrayList<>();
        Map<String, Integer> operationCounts = new HashMap<>();

        for (String line : logLines) {
            LocalDateTime logTime = extractTimestamp(line);
            if (logTime != null && isInTimeRange(logTime, startTime, endTime)) {

                // 提取持续时间
                Matcher durationMatcher = DURATION_PATTERN.matcher(line);
                if (durationMatcher.matches()) {
                    try {
                        long duration = Long.parseLong(durationMatcher.group(1));
                        durations.add(duration);
                    } catch (NumberFormatException e) {
                        // 忽略解析错误
                    }
                }

                // 统计操作类型
                String operation = extractOperation(line);
                if (operation != null) {
                    operationCounts.put(operation, operationCounts.getOrDefault(operation, 0) + 1);
                }
            }
        }

        // 计算性能指标
        if (!durations.isEmpty()) {
            double avgDuration = durations.stream().mapToLong(Long::longValue).average().orElse(0.0);
            long maxDuration = durations.stream().mapToLong(Long::longValue).max().orElse(0);
            long minDuration = durations.stream().mapToLong(Long::longValue).min().orElse(0);

            performanceAnalysis.put("avgDuration", avgDuration);
            performanceAnalysis.put("maxDuration", maxDuration);
            performanceAnalysis.put("minDuration", minDuration);
            performanceAnalysis.put("totalOperations", durations.size());
        }

        performanceAnalysis.put("operationCounts", operationCounts);

        return performanceAnalysis;
    }

    /**
     * 分析呼叫数据
     */
    private Map<String, Object> analyzeCalls(List<String> logLines, LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> callAnalysis = new HashMap<>();

        Set<String> callIds = new HashSet<>();
        Map<String, Integer> callStatusCounts = new HashMap<>();

        for (String line : logLines) {
            LocalDateTime logTime = extractTimestamp(line);
            if (logTime != null && isInTimeRange(logTime, startTime, endTime)) {

                // 提取呼叫ID
                Matcher callMatcher = CALL_PATTERN.matcher(line);
                if (callMatcher.matches()) {
                    String callId = callMatcher.group(2);
                    callIds.add(callId);
                }

                // 统计呼叫状态
                String callStatus = extractCallStatus(line);
                if (callStatus != null) {
                    callStatusCounts.put(callStatus, callStatusCounts.getOrDefault(callStatus, 0) + 1);
                }
            }
        }

        callAnalysis.put("uniqueCallIds", callIds.size());
        callAnalysis.put("callStatusCounts", callStatusCounts);
        callAnalysis.put("callIds", new ArrayList<>(callIds));

        return callAnalysis;
    }

    /**
     * 分析座席数据
     */
    private Map<String, Object> analyzeAgents(List<String> logLines, LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> agentAnalysis = new HashMap<>();

        Set<String> agentIds = new HashSet<>();
        Map<String, Integer> agentActivityCounts = new HashMap<>();

        for (String line : logLines) {
            LocalDateTime logTime = extractTimestamp(line);
            if (logTime != null && isInTimeRange(logTime, startTime, endTime)) {

                // 提取座席ID
                Matcher agentMatcher = AGENT_PATTERN.matcher(line);
                if (agentMatcher.matches()) {
                    String agentId = agentMatcher.group(2);
                    agentIds.add(agentId);
                    agentActivityCounts.put(agentId, agentActivityCounts.getOrDefault(agentId, 0) + 1);
                }
            }
        }

        agentAnalysis.put("uniqueAgentIds", agentIds.size());
        agentAnalysis.put("agentActivityCounts", agentActivityCounts);
        agentAnalysis.put("agentIds", new ArrayList<>(agentIds));

        return agentAnalysis;
    }

    /**
     * 检测系统问题
     */
    public List<Map<String, Object>> detectIssues(LocalDateTime startTime, LocalDateTime endTime) {
        List<Map<String, Object>> issues = new ArrayList<>();

        try {
            List<String> logLines = readLogFile();

            // 检测错误率过高
            Map<String, Object> errorAnalysis = analyzeErrors(logLines, startTime, endTime);
            int errorCount = (int) errorAnalysis.get("errorCount");
            int totalLines = logLines.size();

            if (totalLines > 0) {
                double errorRate = (double) errorCount / totalLines * 100;
                if (errorRate > 5.0) { // 错误率超过5%
                    Map<String, Object> issue = new HashMap<>();
                    issue.put("type", "HIGH_ERROR_RATE");
                    issue.put("severity", "HIGH");
                    issue.put("description", String.format("错误率过高: %.2f%%", errorRate));
                    issue.put("errorCount", errorCount);
                    issue.put("totalLines", totalLines);
                    issues.add(issue);
                }
            }

            // 检测呼叫失败
            Map<String, Object> callAnalysis = analyzeCalls(logLines, startTime, endTime);
            Map<String, Integer> callStatusCounts = (Map<String, Integer>) callAnalysis.get("callStatusCounts");

            Integer failedCalls = callStatusCounts.getOrDefault("FAILED", 0);
            Integer totalCalls = callStatusCounts.values().stream().mapToInt(Integer::intValue).sum();

            if (totalCalls > 0) {
                double failureRate = (double) failedCalls / totalCalls * 100;
                if (failureRate > 10.0) { // 失败率超过10%
                    Map<String, Object> issue = new HashMap<>();
                    issue.put("type", "HIGH_CALL_FAILURE_RATE");
                    issue.put("severity", "MEDIUM");
                    issue.put("description", String.format("呼叫失败率过高: %.2f%%", failureRate));
                    issue.put("failedCalls", failedCalls);
                    issue.put("totalCalls", totalCalls);
                    issues.add(issue);
                }
            }

            // 检测性能问题
            Map<String, Object> performanceAnalysis = analyzePerformance(logLines, startTime, endTime);
            Double avgDuration = (Double) performanceAnalysis.get("avgDuration");

            if (avgDuration != null && avgDuration > 30000) { // 平均响应时间超过30秒
                Map<String, Object> issue = new HashMap<>();
                issue.put("type", "SLOW_PERFORMANCE");
                issue.put("severity", "MEDIUM");
                issue.put("description", String.format("系统响应缓慢，平均响应时间: %.2f秒", avgDuration / 1000.0));
                issue.put("avgDuration", avgDuration);
                issues.add(issue);
            }

            logger.info("问题检测完成 - 发现 {} 个问题", issues.size());

        } catch (Exception e) {
            logger.error("问题检测失败", e);
        }

        return issues;
    }

    /**
     * 生成系统健康报告
     */
    public Map<String, Object> generateHealthReport() {
        Map<String, Object> report = new HashMap<>();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusHours(1);
        LocalDateTime oneDayAgo = now.minusDays(1);

        try {
            // 最近1小时的快速分析
            Map<String, Object> hourlyAnalysis = analyzeLogs(oneHourAgo, now);
            report.put("hourlyAnalysis", hourlyAnalysis);

            // 最近24小时的详细分析
            Map<String, Object> dailyAnalysis = analyzeLogs(oneDayAgo, now);
            report.put("dailyAnalysis", dailyAnalysis);

            // 问题检测
            List<Map<String, Object>> issues = detectIssues(oneDayAgo, now);
            report.put("detectedIssues", issues);

            // 系统健康状态
            String healthStatus = determineHealthStatus(issues);
            report.put("healthStatus", healthStatus);

            report.put("reportGeneratedAt", now);

            logger.info("系统健康报告生成完成 - 状态: {}", healthStatus);

        } catch (Exception e) {
            logger.error("生成系统健康报告失败", e);
            report.put("error", "生成报告失败: " + e.getMessage());
        }

        return report;
    }

    /**
     * 确定系统健康状态
     */
    private String determineHealthStatus(List<Map<String, Object>> issues) {
        boolean hasHighSeverity = issues.stream()
                .anyMatch(issue -> "HIGH".equals(issue.get("severity")));

        boolean hasMediumSeverity = issues.stream()
                .anyMatch(issue -> "MEDIUM".equals(issue.get("severity")));

        if (hasHighSeverity) {
            return "CRITICAL";
        } else if (hasMediumSeverity) {
            return "WARNING";
        } else {
            return "HEALTHY";
        }
    }

    /**
     * 读取日志文件
     */
    private List<String> readLogFile() throws IOException {
        Path path = Paths.get(logFilePath);
        if (Files.exists(path)) {
            return Files.readAllLines(path);
        }
        return new ArrayList<>();
    }

    /**
     * 提取时间戳
     */
    private LocalDateTime extractTimestamp(String logLine) {
        Matcher matcher = TIMESTAMP_PATTERN.matcher(logLine);
        if (matcher.matches()) {
            try {
                String timestampStr = matcher.group(1);
                return LocalDateTime.parse(timestampStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (Exception e) {
                // 忽略解析错误
            }
        }
        return null;
    }

    /**
     * 检查时间是否在范围内
     */
    private boolean isInTimeRange(LocalDateTime logTime, LocalDateTime startTime, LocalDateTime endTime) {
        return (logTime.isEqual(startTime) || logTime.isAfter(startTime)) &&
               (logTime.isEqual(endTime) || logTime.isBefore(endTime));
    }

    /**
     * 提取错误类型
     */
    private String extractErrorType(String logLine) {
        if (logLine.contains("NullPointerException")) {
            return "NullPointerException";
        } else if (logLine.contains("SQLException")) {
            return "SQLException";
        } else if (logLine.contains("IOException")) {
            return "IOException";
        } else if (logLine.contains("RuntimeException")) {
            return "RuntimeException";
        } else if (logLine.contains("TimeoutException")) {
            return "TimeoutException";
        } else {
            return "Other";
        }
    }

    /**
     * 提取操作类型
     */
    private String extractOperation(String logLine) {
        if (logLine.contains("呼叫")) {
            return "CALL";
        } else if (logLine.contains("座席")) {
            return "AGENT";
        } else if (logLine.contains("登录")) {
            return "LOGIN";
        } else if (logLine.contains("登出")) {
            return "LOGOUT";
        } else if (logLine.contains("转移")) {
            return "TRANSFER";
        } else if (logLine.contains("会议")) {
            return "CONFERENCE";
        } else {
            return null;
        }
    }

    /**
     * 提取呼叫状态
     */
    private String extractCallStatus(String logLine) {
        if (logLine.contains("成功")) {
            return "SUCCESS";
        } else if (logLine.contains("失败")) {
            return "FAILED";
        } else if (logLine.contains("超时")) {
            return "TIMEOUT";
        } else if (logLine.contains("取消")) {
            return "CANCELLED";
        } else {
            return null;
        }
    }

    /**
     * 设置日志文件路径
     */
    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    /**
     * 获取日志文件路径
     */
    public String getLogFilePath() {
        return logFilePath;
    }
}