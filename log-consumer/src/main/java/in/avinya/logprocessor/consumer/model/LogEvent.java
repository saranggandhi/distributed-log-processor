package in.avinya.logprocessor.consumer.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;


@Entity
@Table(name = "log_events")
public class LogEvent {

    @Id
    private String id;

    @Column(name = "orgnization_id", nullable = false )
    private String orgnizationId;

    @Column(name = "level", nullable = false)
    private String level;

    @Column(name ="message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "source")
    private String source;

    @Column(name = "timestamp", nullable = false)
    // put default datetime stamp as now if it's null when inserting into db, so that we can track when the event was created if the producer doesn't set it.
    @ColumnDefault("CURRENT_TIMESTAMP")
    private LocalDateTime timeStamp;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrgnizationId() {
        return orgnizationId;
    }

    public void setOrgnizationId(String orgnizationId) {
        this.orgnizationId = orgnizationId;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }



 }
