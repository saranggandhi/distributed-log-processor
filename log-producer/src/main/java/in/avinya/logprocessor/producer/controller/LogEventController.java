package in.avinya.logprocessor.producer.controller;

import in.avinya.logprocessor.producer.KafkaProducerService;
import in.avinya.logprocessor.producer.model.LogEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/api/logs")
public class LogEventController {
    private static final Logger logger = LoggerFactory.getLogger(LogEventController.class);

    private final KafkaProducerService kafkaProducerService;

    private final Counter logCounter;


    public LogEventController(KafkaProducerService kafkaProducerService, MeterRegistry meterRegistry) {
        this.kafkaProducerService = kafkaProducerService;
        this.logCounter = Counter.builder("log_events_produced_total")
                .description("Total number of log events produced")
                .register(meterRegistry);
    }

    @PostMapping
    @Timed(value = "log_event_processing_time", description = "Time taken to process log event")
    @CircuitBreaker(name = "kafka-producer", fallbackMethod = "fallbackLogEvent")
    public ResponseEntity<Map<String, String>> createLogEvent(@RequestBody LogEvent logEvent) {
        try {
            // Generate ID if not provided
            if (logEvent.getId() == null) {
                logEvent.setId(java.util.UUID.randomUUID().toString());
            }

            kafkaProducerService.sendLogEvent(logEvent);
            logCounter.increment();

            logger.info("Log event created successfully: {}", logEvent.getId());

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "id", logEvent.getId(),
                    "message", "Log event queued for processing"
            ));

        } catch (Exception e) {
            logger.error("Failed to process log event", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "message", "Failed to process log event"));
        }
    }


    public ResponseEntity<Map<String, String>> fallbackLogEvent(LogEvent logEvent, Throwable ex) {
        logger.error("Circuit breaker triggered for log event: {}", logEvent.getId(), ex);
        return ResponseEntity.status(503)
                .body(Map.of("status", "error", "message", "Service unavailable. Please try again later."));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "UP-Healthy"));
    }
}
