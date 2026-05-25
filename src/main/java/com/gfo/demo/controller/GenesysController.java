package com.gfo.demo.controller;

import com.gfo.demo.genesys.ComposerService;
import com.gfo.demo.genesys.GCTIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/genesys")
@CrossOrigin(origins = "*")
public class GenesysController {

    @Autowired
    private GCTIService gctiService;

    @Autowired
    private ComposerService composerService;

    @PostMapping("/gcti/agent/{agentId}/connect")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> connectAgent(@PathVariable String agentId) {
        return gctiService.connectToAgent(agentId)
                .thenApply(success -> {
                    if (success) {
                        return ResponseEntity.ok(Map.of(
                                "status", "success",
                                "agentId", agentId,
                                "message", "Agent connected successfully"
                        ));
                    } else {
                        return ResponseEntity.badRequest().body(Map.of(
                                "status", "error",
                                "agentId", agentId,
                                "message", "Failed to connect agent"
                        ));
                    }
                });
    }

    @PostMapping("/gcti/agent/{agentId}/call")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> makeCall(
            @PathVariable String agentId,
            @RequestParam String phoneNumber) {
        return gctiService.makeCall(agentId, phoneNumber)
                .<ResponseEntity<Map<String, Object>>>thenApply(callId -> ResponseEntity.ok(Map.of(
                        "status", "success",
                        "callId", callId,
                        "agentId", agentId,
                        "phoneNumber", phoneNumber
                )))
                .exceptionally(throwable -> ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", throwable.getMessage()
                )));
    }

    @PostMapping("/gcti/call/{callId}/transfer")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> transferCall(
            @PathVariable String callId,
            @RequestParam String targetAgentId) {
        return gctiService.transferCall(callId, targetAgentId)
                .thenApply(success -> {
                    if (success) {
                        return ResponseEntity.ok(Map.of(
                                "status", "success",
                                "callId", callId,
                                "targetAgentId", targetAgentId
                        ));
                    } else {
                        return ResponseEntity.badRequest().body(Map.of(
                                "status", "error",
                                "message", "Transfer failed"
                        ));
                    }
                });
    }

    @GetMapping("/gcti/agent/{agentId}/status")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> getAgentStatus(@PathVariable String agentId) {
        return gctiService.getAgentStatus(agentId)
                .thenApply(status -> ResponseEntity.ok(status));
    }

    @GetMapping("/gcti/queue/{queueName}/stats")
    public ResponseEntity<Map<String, Object>> getQueueStats(@PathVariable String queueName) {
        Map<String, Object> stats = gctiService.getQueueStatistics(queueName);
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/composer/workflow/{workflowName}/execute")
    public ResponseEntity<Map<String, Object>> executeWorkflow(
            @PathVariable String workflowName,
            @RequestBody Map<String, Object> parameters) {
        Map<String, Object> result = composerService.executeWorkflow(workflowName, parameters);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/composer/flow/{flowName}/create")
    public ResponseEntity<Map<String, Object>> createCallFlow(
            @PathVariable String flowName,
            @RequestParam String customerId,
            @RequestParam String phoneNumber) {
        String flowId = composerService.createCallFlow(flowName, customerId, phoneNumber);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "flowId", flowId,
                "flowName", flowName,
                "customerId", customerId
        ));
    }

    @GetMapping("/composer/customer/{customerId}/history")
    public ResponseEntity<Map<String, Object>> getInteractionHistory(@PathVariable String customerId) {
        Map<String, Object> history = composerService.getInteractionHistory(customerId);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/composer/customer/{customerId}/validate")
    public ResponseEntity<Map<String, Object>> validateCustomer(
            @PathVariable String customerId,
            @RequestBody Map<String, String> validationData) {
        boolean isValid = composerService.validateCustomerData(customerId, validationData);
        return ResponseEntity.ok(Map.of(
                "customerId", customerId,
                "valid", isValid,
                "timestamp", System.currentTimeMillis()
        ));
    }
}