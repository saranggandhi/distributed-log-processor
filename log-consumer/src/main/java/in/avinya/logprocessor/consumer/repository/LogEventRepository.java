package  in.avinya.logprocessor.consumer.repository;


import in.avinya.logprocessor.consumer.model.LogEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogEventRepository extends JpaRepository<LogEvent,String> {

    List<LogEvent> findByOrgnizationIdAndTimeStampBetween(
            String orgnizationId,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("SELECT COUNT(1) FROM LogEvent le where le.orgnizationId = :orgId AND le.level = :level")
    long countByOrgnizationIdAndLevel(@Param("orgId") String orgnizationId, @Param("level") String level);;

    List<LogEvent> findByLevelAndTimeStampAfter(String level, LocalDateTime timeStamp);

}
