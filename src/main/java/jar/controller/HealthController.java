package jar.controller;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "ai-lost-found-backend");
        response.put("timestamp", Instant.now().toString());

        try (var conn = dataSource.getConnection()) {
            boolean valid = conn.isValid(2);
            response.put("database", valid ? "CONNECTED" : "DEGRADED");
        } catch (Exception ex) {
            response.put("database", "DISCONNECTED");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "AI Lost and Found System API");
        response.put("status", "RUNNING");
        response.put("healthEndpoint", "/api/health");
        response.put("documentation", "REST API for AI Lost and Found Matching");
        return ResponseEntity.ok(response);
    }
}
