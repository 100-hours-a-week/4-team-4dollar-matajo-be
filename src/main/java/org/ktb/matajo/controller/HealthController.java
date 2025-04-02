package org.ktb.matajo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.ktb.matajo.global.common.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@Tag(name = "Health Check API", description = "시스템 상태 확인을 위한 API")
public class HealthController {
    @GetMapping("/health")
    @Operation(summary = "기본 상태 확인", description = "시스템의 기본적인 상태 정보를 제공합니다.")
    public ResponseEntity<CommonResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> healthData = new HashMap<>();
        healthData.put("status", "UP");
        healthData.put("timestamp", LocalDateTime.now().toString());
        healthData.put("service", "Matajo Service");

        CommonResponse<Map<String, Object>> response = CommonResponse.success(
                "시스템이 정상적으로 동작 중입니다.",
                healthData
        );

        return ResponseEntity.ok(response);
    }
}
