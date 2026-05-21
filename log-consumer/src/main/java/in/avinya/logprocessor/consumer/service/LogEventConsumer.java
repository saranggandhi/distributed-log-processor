package in.avinya.logprocessor.consumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.avinya.logprocessor.consumer.model.LogEvent;
import in.avinya.logprocessor.consumer.repository.LogEventRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LogEventConsumer {
    private static final Logger logger = LoggerFactory.getLogger(LogEventConsumer.class);


    @Autowired
    private LogEventRepository logEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final Counter processedCounter;

    private final Counter errorCounter;

    /**
     * Initializes counters used for success/error monitoring.
     *
     * @param meterRegistry Micrometer registry used to publish metrics
     */
    public LogEventConsumer(MeterRegistry meterRegistry) {
        // Counter for successfully processed log events.
        this.processedCounter = Counter.builder("log_events_processed_total")
                .description("Total number of log events processed")
                .register(meterRegistry);
        // Counter for processing failures.
        this.errorCounter = Counter.builder("log_events_errors_total")
                .description("Total number of log event processing errors")
                .register(meterRegistry);
    }

    @KafkaListener(topics = "log-events", groupId = "log-consumer-group")
    public void consume(@Payload String message) {

        try {
            logger.debug("Received message from topic log-events");

            // Convert JSON payload into domain object.
            LogEvent logEvent = objectMapper.readValue(message, LogEvent.class);
            processLogEvent(logEvent);

            processedCounter.increment();

            logger.debug("Successfully processed log event: {}", logEvent.getId());

        } catch (Exception e) {
            logger.error("Failed to process message: {}", message, e);
            errorCounter.increment();
            throw new RuntimeException("Failed to process log event", e);
        }
    }

    public void processLogEvent(LogEvent logEvent) {
        // Mark consumer processing time before saving.
        logEvent.setProcessedAt(LocalDateTime.now());
        logEvent.setTimeStamp(logEvent.getTimeStamp() != null ? logEvent.getTimeStamp() : LocalDateTime.now());
        logEventRepository.save(logEvent);
        logger.info("Processed log event: {}", logEvent.getId());
    }
}
