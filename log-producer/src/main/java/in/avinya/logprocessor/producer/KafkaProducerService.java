package in.avinya.logprocessor.producer;


import in.avinya.logprocessor.producer.model.LogEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;


@Service
public class KafkaProducerService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.kafka.topic.log-events}")
    private String logEventsTopic;

    /**
     * Serializes and publishes one log event to Kafka.
     * The organization id is used as message key to preserve per-organization ordering.
     */
    public void sendLogEvent(LogEvent logEvent) {
        try {
            // Convert the domain event into the JSON payload sent to Kafka.
            String message = objectMapper.writeValueAsString(logEvent);
            // Use organizationId as partition key for stable ordering in a partition.
            String partitionKey = logEvent.getOrgnizationId();

            // Send asynchronously and handle broker ack/failure callbacks.
            var future = kafkaTemplate.send(logEventsTopic, partitionKey, message);
            future.whenComplete((SendResult<String, String> result, Throwable ex) -> {
                if (ex == null) {
                    logger.debug("Sent log event: {} to partition: {}",
                            logEvent.getId(), result.getRecordMetadata().partition());
                } else {
                    logger.error("Failed to send log event: {}", logEvent.getId(), ex);
                    throw new RuntimeException("Failed to send log event", ex);
                }
            });

        } catch (Exception e) {
            logger.error("Failed to serialize log event: {}", logEvent.getId(), e);
            throw new RuntimeException("Failed to serialize log event", e);
        }
    }
}
